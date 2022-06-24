from dataclasses import dataclass
import json
import os
import itertools
import pickle
from sklearn.model_selection import KFold
import torch
import cnc_utilities
import collate_functions
import shutil


@dataclass
class RunConfig:
    config_file_path: str
    run_identifier: str
    max_epochs: int
    llm_name: str
    hparams: list
    device: object


@dataclass
class DataConfig:
    config_file_path: str
    data_utility_cls: object
    nos_folds: int
    dataset_cls: object
    trn_data_path: str
    tst_data_path: str
    output_folder_path: str


@dataclass
class PreprocessingConfig:
    config_file_path: str
    collate_fn: object


def generate_config_dict(config_file_path):
    config_dict = {}
    with open(config_file_path) as f:
        for line in f:
            line = line.split(":")
            config_dict[line[0].strip()] = line[1].strip()
    config_dict['config_file_path'] = config_file_path
    return config_dict


def generate_hyperparameter_sets(run_config_dict):
    lf = run_config_dict['loss_functions'].split(',')
    lr = [float(lr.strip()) for lr in run_config_dict['learning_rates'].split(',')]
    bs = [int(bs.strip()) for bs in run_config_dict['batch_sizes'].split(',')]
    op = [op.strip() for op in run_config_dict['optimizers'].split(',')]
    hdo = [float(lr.strip()) for lr in run_config_dict['hidden_dropout_prob'].split(',')]
    ado = [float(lr.strip()) for lr in run_config_dict['attention_probs_dropout_prob'].split(',')]
    plg = [op.strip() for op in run_config_dict['pooling_strategy'].split(',')]
    hparams = []
    for loss_function, learning_rate, batch_size, optimizer, hidden_dropout_prob, attention_probs_dropout_prob, pooling_strategy in \
            itertools.product(lf, lr, bs, op, hdo, ado, plg):
        hparams.append({
            'loss_function': loss_function,
            'learning_rate': learning_rate,
            'batch_size': batch_size,
            'optimizer': optimizer,
            'hidden_dropout_prob': hidden_dropout_prob,
            'attention_probs_dropout_prob': attention_probs_dropout_prob,
            'pooling_strategy': pooling_strategy
        })
    return hparams


def run_config_from_config_dict(config_dict, hparams):
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    return RunConfig(config_dict['config_file_path'],
                     config_dict['run_identifier'],
                     int(config_dict['max_epochs']),
                     config_dict['llm_name'],
                     hparams,
                     device)


def data_config_from_config_dict(data_config_dict, run_config):
    return DataConfig(data_config_dict['config_file_path'],
                      eval(data_config_dict['data_utility_cls']),
                      int(data_config_dict['nos_of_folds']),
                      eval(f"{data_config_dict['data_utility_cls']}.{data_config_dict['dataset_cls']}"),
                      data_config_dict['trn_data_path'],
                      data_config_dict['tst_data_path'],
                      f'{data_config_dict["base_output_folder_path"]}{run_config.run_identifier}/')


def preprocessing_config_from_config_dict(config_dict):
    return PreprocessingConfig(config_dict['config_file_path'],
                               getattr(collate_functions, f"{config_dict['collate_fn_name']}")(**config_dict))


def generate_output_folder_structure(run_config, data_config, preprocessing_config):
    os.mkdir(data_config.output_folder_path)
    for i in range(len(run_config.hparams)):
        cv_path = data_config.output_folder_path + f'{i}' + '/'
        os.mkdir(cv_path)
        with open(cv_path + 'config.json', 'w+') as f:
            json.dump(run_config.hparams[i], f)
        os.mkdir(cv_path + 'checkpoints')
        os.mkdir(cv_path + 'results')
        os.mkdir(cv_path + 'results/classified_samples')
    shutil.copy(run_config.config_file_path, data_config.output_folder_path)
    shutil.copy(data_config.config_file_path, data_config.output_folder_path)
    shutil.copy(preprocessing_config.config_file_path, data_config.output_folder_path)


def generate_folds(data_config):
    data = getattr(data_config.data_utility_cls, 'load_data')(data_config.trn_data_path)
    kf = KFold(n_splits=data_config.nos_folds, shuffle=True, random_state=42)
    folds = []
    cntr = 1
    for train_index, test_index in kf.split(data):
        with open(data_config.output_folder_path + f'fold_{cntr}_train_index.pkl',
                  'wb+') as f:
            pickle.dump(train_index, f)
        with open(data_config.output_folder_path + f'/fold_{cntr}_test_index.pkl',
                  'wb+') as g:
            pickle.dump(test_index, g)
        cntr = cntr + 1
        fold = {"trn_dataset": data_config.dataset_cls(data, train_index),
                "val_dataset": data_config.dataset_cls(data, test_index)}
        folds.append(fold)
    return folds


def save_metrics(run_config, data_config, all_metrics):
    for i in range(len(run_config.hparams)):
        results_folder_path = data_config.output_folder_path + f'{i}/results'
        cv_metrics = all_metrics[i]
        for key in cv_metrics.keys():
            with open(f'{results_folder_path}/{key}.pkl', 'wb') as g:
                pickle.dump(cv_metrics[key], g)
