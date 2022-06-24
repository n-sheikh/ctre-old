import torch
import torch.nn as nn
from transformers import AutoTokenizer, AutoModel, AutoConfig
from transformers import BertConfig, BertModel
import sys


class LLMModule(nn.Module):
    def __init__(self, llm, pooling='attn', classes=2, hdo=0.5, ado=0.5):
        super().__init__()
        self.pooling = pooling
        config = BertConfig(hidden_dropout_prob=hdo,
                            attention_probs_dropout_prob=ado)
        self.llm = BertModel.from_pretrained(llm, config=config)
        self.w_attn = nn.Linear(AutoConfig.from_pretrained(llm).hidden_size, 1)
        self.classifier = nn.Linear(
            AutoConfig.from_pretrained(llm).hidden_size,
            classes)

    def forward(self, X, device):
        in_ids = torch.LongTensor(X["input_ids"]).to(device)
        llm_out = self.llm(in_ids)
        clf_in = None
        if self.pooling == 'cls':
            clf_in = llm_out['last_hidden_state'][:, 0, :]
        elif self.pooling == 'attn':
            llm_token_emb = llm_out['last_hidden_state'][0]
            attn_score = torch.softmax(self.w_attn(llm_token_emb), dim=0)
            v = torch.transpose(llm_token_emb, 1, 0)
            clf_in = torch.matmul(v, attn_score).reshape(1, -1)
        elif self.pooling == 'mean':
            llm_token_emb = llm_out['last_hidden_state'][0]
            clf_in = (torch.sum(llm_token_emb, 0) / float(llm_token_emb.size()[0])).reshape(1, -1)
        clf_out = self.classifier(clf_in)
        return clf_out
