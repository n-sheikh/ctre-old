import csv
import sys
import numpy as np


def load_misclassified_samples_from_file(path):
    false_positives = []
    false_negatives = []
    with open(path) as f:
        csvreader = csv.reader(f)
        for row in csvreader:
            if row[2] == '0':
                false_positives.append(row[0])
            elif row[2] == '1':
                false_negatives.append(row[0])
        return false_positives, false_negatives


def identify_misclassified_sample_ids_across_fpe(config):
    all_misclassified_samples = {}
    output_folder_path = config.output_folder_path + config.run_identifier
    #print(output_folder_path)
    #print(list(range(config.nos_folds)))
    #print(list(range(config.max_epochs)))
    #print(list(range(len(config.hparams))))
    for i in range(config.nos_folds):
        all_misclassified_samples[f'fold_{i}'] = {}
        for j in range(len(config.hparams)):
            all_misclassified_samples[f'fold_{i}'][f'hparam_config_id_{j}'] = []
            for k in range(config.max_epochs):
                path = output_folder_path + f'/{j}/results/misclassified_samples/fold_{i}_epoch_{k}.csv'
                all_misclassified_samples[f'fold_{i}'][f'hparam_config_id_{j}'].append(load_misclassified_samples_from_file(path))
    return all_misclassified_samples


def identify_encountered_misclassified_samples_in_fold_across_pe(fold_misclassified_samples):
    false_positives = []
    false_negatives = []
    for key in fold_misclassified_samples:
        for idx in range(len(fold_misclassified_samples[key])):
            false_positives = false_positives + fold_misclassified_samples[key][idx][0]
            false_negatives = false_negatives + fold_misclassified_samples[key][idx][1]
    return set(false_positives), set(false_negatives)


def identify_encountered_misclassified_samples_across_fpe(all_misclassified_samples):
    encountered_samples = {}
    for key, value in all_misclassified_samples.items():
        print(key)
        print(value)

        val = identify_encountered_misclassified_samples_in_fold_across_pe(value)
        encountered_samples[key] = val
        break
    return encountered_samples


def construct_misclassified_matrix(fold_misclassified_samples, fold_encountered_samples):
    cvs_keys = list(fold_misclassified_samples.keys())
    nos_epochs = len(fold_misclassified_samples[cvs_keys[0]])
    nos_cvs = len(cvs_keys)
    fp_nos_enc_mc_samples = len(fold_encountered_samples[0])
    fn_nos_enc_mc_samples = len(fold_encountered_samples[1])
    np_length = nos_epochs
    fp_np_width = nos_cvs * fp_nos_enc_mc_samples
    fn_np_width = nos_cvs * fn_nos_enc_mc_samples
    false_positives = np.ones((np_length, fp_np_width))
    false_negatives = np.ones((np_length, fn_np_width))
    #for cvs in fold_misclassified_samples.keys():




'''
    def generate_output_folder_structure(config_dict, config):
    output_folder_path = config.output_folder_path + config.run_identifier
    os.mkdir(output_folder_path)
    with open(output_folder_path + '/config.json', 'w+') as f:
        json.dump(config_dict, f)
    for i in range(len(config.hparams)):
        cv_path = output_folder_path + f'/{i}'
        os.mkdir(cv_path)
        with open(cv_path + '/config.json', 'w+') as f:
            json.dump(config.hparams[i], f)
        os.mkdir(cv_path + '/checkpoints')
        os.mkdir(cv_path + '/results')
        os.mkdir(cv_path + '/results/classified_samples')
        print(config_obj)
        print(config_obj)

'''







#def results_report(config_obj):
#    indentify_file_paths_for_misclassified_samples(config_obj)