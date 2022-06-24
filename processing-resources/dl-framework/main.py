from torch.utils.data import DataLoader

import collate_functions
import utilities
import pipeline
import process_results
import models

run_config_path = '/home/nadia/Documents/CLaC-Lab/ctre/cnc-task-3/experiments/cnc-task-3a/run_config.txt'
data_config_path = '/home/nadia/Documents/CLaC-Lab/ctre/cnc-task-3/experiments/cnc-task-3a/data_config.txt'
preprocessing_config_path = '/home/nadia/Documents/CLaC-Lab/ctre/cnc-task-3/experiments/cnc-task-3a/preprocessing_config.txt'
run_config_dict = utilities.generate_config_dict(run_config_path)
data_config_dict = utilities.generate_config_dict(data_config_path)
preprocessing_config_dict = utilities.generate_config_dict(preprocessing_config_path)
hparams = utilities.generate_hyperparameter_sets(run_config_dict)
run_config = utilities.run_config_from_config_dict(run_config_dict, hparams)
data_config = utilities.data_config_from_config_dict(data_config_dict, run_config)
preprocessing_config = utilities.preprocessing_config_from_config_dict(preprocessing_config_dict)
#utilities.generate_output_folder_structure(run_config, data_config, preprocessing_config)
folds = utilities.generate_folds(data_config)

for fold in folds:
    for key in fold.keys():
        dl = DataLoader(fold["trn_dataset"], batch_size=1, collate_fn=preprocessing_config.collate_fn)
        for idx, X, y in dl:
            print(idx)
            print(X)
            print(y)
            break
        break
    break

#all_metrics = [pipeline.cross_validate(i, run_config.hparams[i], folds, run_config, data_config, preprocessing_config)
#               for i in range(len(run_config.hparams))]
#utilities.save_metrics(run_config, data_config, all_metrics)


#all_mc_samples = process_results.identify_misclassified_sample_ids_across_fpe(config_obj)
#all_enc_samples = process_results.identify_encountered_misclassified_samples_across_fpe(all_mc_samples)

#print(all_mc_samples.keys())
#fold_0_keys = all_mc_samples['fold_0'].keys()
#print(fold_0_keys)
#print(len(all_mc_samples['fold_0']['hparam_config_id_0'][0][0]))
#for key in all_mc_samples.keys():
#    process_results.construct_misclassified_matrix(all_mc_samples[key], all_enc_samples[key])
#    break

#print(all_enc_samples.keys())
#print(all_enc_samples['fold_0'].keys())


#result_report.results_report(config_dict)

