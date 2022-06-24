from transformers import AutoTokenizer, AutoModel, AutoConfig
import torch
import csv
import itertools
import sys


class LLMCollateFn:
    def __init__(self, **kwargs):
        self.tokenizer = AutoTokenizer.from_pretrained(kwargs['llm'])

    def __call__(self, batch):
        sentences = []
        labels = []
        ids = []
        for sample in batch:
            ids.append(sample["id"])
            sentences.append(sample["text"])
            labels.append(sample["label"])
        return ids, self.tokenizer(sentences, padding=True), torch.LongTensor(labels)


def connl_file_to_feature_vector(file_path, max_nos_tokens):
    with open(file_path) as f:
        csvreader = csv.reader(f, delimiter="\t")
        rows = []
        for row in csvreader:
            features = row[1: len(row) - 1]
            rows.append([int(el) for el in features])
        feature_vector = []
        padding_vector = []
        for i in range(len(rows[0])):
            padding_vector.append(0)
        for i in range(max_nos_tokens):
            if i < len(rows):
                feature_vector = feature_vector + rows[i]
            else:
                feature_vector = feature_vector + padding_vector
    return feature_vector


class FVConcatCollateFn:
    def __init__(self, **kwargs):
        self.connl_folder_path = kwargs['connl_folder_path']
        self.max_nos_tokens = int(kwargs['max_nos_tokens'])

    def __call__(self, batch):
        sentences = []
        labels = []
        ids = []
        feature_vectors = []
        for sample in batch:
            sentences.append(sample["text"])
            labels.append(sample["label"])
            ids.append(sample["id"])
        for i in range(len(sentences)):
            connl_file_path = f"{self.connl_folder_path}/{ids[i]}.cnnl"
            feature_vectors.append(connl_file_to_feature_vector(connl_file_path, self.max_nos_tokens))
        return ids, torch.LongTensor(feature_vectors), torch.LongTensor(labels)
