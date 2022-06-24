import csv
import pickle
import numpy as np

from sklearn.metrics import accuracy_score, recall_score, precision_score, f1_score, matthews_corrcoef
from sklearn.dummy import DummyClassifier

import torch
import torch.nn as nn
from torch import optim
from torch.utils.data import DataLoader
import models
import sys


def train(loss_function, optimizer, model, trn_dl, run_config):
    epoch_trn_loss = []
    for ids, X, y in trn_dl:
        model.zero_grad()
        loss = loss_function(model(X, run_config.device), y.to(run_config.device))
        loss.backward()
        optimizer.step()
        epoch_trn_loss.append(loss.cpu().detach().numpy())
    return np.mean(epoch_trn_loss)


def baseline_test(folds, config_dict):
    dummy_metrics = []
    scorers = [accuracy_score, precision_score, recall_score, f1_score, matthews_corrcoef]
    for fold in folds:
        X = [sample['text'] for sample in fold["val_dataset"]]
        y = [sample['label'] for sample in fold["val_dataset"]]
        dummy_clf = DummyClassifier(strategy="constant", constant=1)
        dummy_clf.fit(X, y)
        preds = dummy_clf.predict(X)
        metric = {scorer.__name__: scorer(y, preds) for scorer in scorers}
        dummy_metrics.append(metric)
    with open(config_dict['output_folder_path'] + config_dict['run_identifier'] + f'/baseline_metrics.pkl', 'wb+') as f:
        pickle.dump(dummy_metrics, f)


def test(loss_function, model, trn_dl, run_config):
    scorers = [accuracy_score, precision_score, recall_score, f1_score, matthews_corrcoef]
    preds = []
    labels = []
    val_loss = []
    classified_samples = []
    for ids, X, y in trn_dl:
        with torch.no_grad():
            model.eval()
            loss = loss_function(model(X, run_config.device), y.to(run_config.device))
            val_loss.append(loss.cpu().detach().numpy())
            pred = torch.argmax(model(X, run_config.device), dim=1).item()
            preds.append(pred)
            labels.append(y)
            classified_samples.append([id, X, y, pred])
    metrics = {scorer.__name__: scorer(labels, preds) for scorer in scorers}
    metrics["test_loss"] = np.mean(val_loss)
    return metrics, classified_samples


# @ray.remote
def train_test_fold(hparam_id, hparam, fold_id, trn_dl, val_dl, collate_fn, run_config, data_config):
    optimizers = {'adam': optim.Adam}
    loss_functions = {'cross-entropy-loss': nn.CrossEntropyLoss()}
    # model_folder_path = config.output_folder_path + config.run_identifier + f'/{hparam_id}/models'
    classified_folder_path = data_config.output_folder_path + f'{hparam_id}/results/classified_samples'
    model = models.LLMModule(run_config.llm_name, pooling=hparam['pooling_strategy']).to(run_config.device)
    model.train()
    loss_function = loss_functions[hparam['loss_function']]
    optimizer = optimizers[hparam['optimizer']](model.parameters(), lr=hparam['learning_rate'])
    fold_metrics = {'train_loss': [], 'test_loss': [], 'accuracy_score': [], 'precision_score': [],
                    'recall_score': [], 'f1_score': [], 'matthews_corrcoef': []}
    print(f"Fold {fold_id}", flush=True)
    for epoch_id in range(run_config.max_epochs):
        print(f"Epoch {epoch_id}: Training", flush=True)
        epoch_train_loss = train(loss_function, optimizer, model, trn_dl, run_config)
        print(f"Train Loss for epoch {epoch_id}: {epoch_train_loss}", flush=True)
        # model_file_path = model_folder_path + f'/fold_{fold_id}_epoch_{epoch_id}.pt'
        # torch.save(model.state_dict(), model_file_path)
        print(f"Epoch {epoch_id}: Validation")
        epoch_metrics, classified_samples = test(loss_function, model, val_dl, run_config)
        with open(classified_folder_path + f'/fold_{fold_id}_epoch_{epoch_id}.csv', 'w+') as f:
            csvwriter = csv.writer(f)
            csvwriter.writerows(classified_samples)
        print(f"Classified Samples Saved", flush=True)
        print(f"Validation Metrics for epoch{epoch_id}:\n{epoch_metrics}", flush=True)
        epoch_metrics['train_loss'] = epoch_train_loss
        for metric in epoch_metrics.keys():
            fold_metrics[metric].append(epoch_metrics[metric])
    return fold_metrics


def cross_validate(hparam_id, hparam, folds, run_config, data_config, preprocessing_config):
    metrics = {'train_loss': [], 'test_loss': [], 'accuracy_score': [], 'precision_score': [], 'recall_score': [],
               'f1_score': [], 'matthews_corrcoef': []}
    trn_dls = [DataLoader(fold["trn_dataset"], batch_size=hparam["batch_size"],
                          collate_fn=preprocessing_config.collate_fn) for fold in folds]
    val_dls = [DataLoader(fold["val_dataset"], batch_size=1, collate_fn=preprocessing_config.collate_fn)
               for fold in folds]
    all_fold_metrics = [
        train_test_fold(hparam_id, hparam, fold_id, trn_dls[fold_id], val_dls[fold_id],
                        preprocessing_config.collate_fn, run_config, data_config) for fold_id in range(len(folds))]
    for fold_metrics in all_fold_metrics:
        for metric in fold_metrics.keys():
            metrics[metric].append(fold_metrics[metric])
    return metrics
