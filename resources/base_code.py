from transformers import AutoModel
from transformers import AutoTokenizer, AutoConfig
from torch import optim
import torch.nn as nn
import torch
import numpy as np
import csv
from sklearn.metrics import classification_report
from sklearn.model_selection import train_test_split

test = False
bert_model = 'sbert'
output = 'cls'
based_on = 'user'
max_length = 300
#pooling_strategy = 'cat'

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
path = '/home/n_tahaei/IROSTEREO/data/'


def read_data_tweet_based():
    users_docs = []
    labels = list()
    tweets_list = list()
    with open(path+'output/my.csv', 'r') as f:
        for line in f.readlines():
            line = list(line.split('\t'))
            for i in range(len(line)):
                tweets = []
                l = str(line[i]).strip('\'').split(',')
                tweets.append(l[2])
                for j in range(3,len(l)):
                    tweets.append(l[j])
                labels.append(l[1])
                tweets_list.append(str(tweets))
        print(np.mean(all_len))
        print(np.max(all_len))
        print(np.min(all_len))
        return tweets_list, [int(x) for x in labels]


all_len=list()
from nltk import word_tokenize
def read_data_user_based():
    users_docs = []
    labels = []
    with open(path+'output/my.csv', 'r') as f:
        for index,line in enumerate(f.readlines()):
            tweets_list = list()
            line = list(line.split('\t'))
            labels.append(str(line[0]).strip('\'').split(',')[1])
            for i in range(len(line)):
                tweets = []
                l = str(line[i]).strip('\'').split(',')
                tweets.append(l[2])
                for j in range(3,len(l)):
                    tweets.append(l[j])
                tweets_list.append(str(tweets))
                for t in tweets:
                    all_len.append(len(word_tokenize(t)))
            users_docs.append(tweets_list)
        return users_docs, [int(x) for x in labels]


class Bert4Classification(nn.Module):
    def __init__(self, bert_version, num_class,pooling_strategy,max_num_sent):
        super().__init__()
        self.bert = AutoModel.from_pretrained(bert_version)
        #for param in self.bert.parameters():
            #param.requires_grad = False
        self.tokenizer = AutoTokenizer.from_pretrained(bert_version)
        self.config = AutoConfig.from_pretrained(bert_version)
        if pooling_strategy == 'cat':
            final_hidden = self.config.hidden_size*max_num_sent
        else:
            final_hidden = self.config.hidden_size
        self.classifier = nn.Linear(final_hidden, num_class).to(device)
        self.w_att = nn.Linear(self.config.hidden_size, 1)
        self.pooling_strategy = pooling_strategy
        self.max_num_sent = max_num_sent
        self.final_hidden = final_hidden

    def forward(self, x):
	
        outputs = self.bert(x['input_ids'].to(device), return_dict=True)
        if output == 'cls':
            outputs = outputs['last_hidden_state'][:, 0, :]
        elif output == 'mean':
            outputs = self.mean_pooling(outputs, x['attention_mask'])
        if self.pooling_strategy == 'cat':
            input4classifier = self.concat_vectors(outputs, self.final_hidden)
        elif self.pooling_strategy == 'attention':
            att_score = self.w_att(outputs)
            att_score = torch.softmax(att_score, dim=0)
            input4classifier = torch.matmul(torch.transpose(outputs, 1, 0), att_score)
            # note: att_score.shape = [4,1], outputs.shape = [4,768], input4classifier.shape = [768,1], input4classifier.view(1,-1).shape = [1,768]
        return torch.log_softmax(self.classifier(input4classifier.view(1, -1)), dim=1)

    def concat_vectors(self, model_output, final_hidden):

        catted = model_output.reshape(1, -1).to(device)
        if len(model_output) > 10:
            print(len(model_output))
            dif = 0
            catted = catted[:,0:final_hidden]
        else:
            dif = self.max_num_sent - len(model_output)
        print(dif)
        all_zero = torch.zeros(1, dif * self.config.hidden_size).to(device)
        return torch.cat((catted, all_zero), dim=1)

    def mean_pooling(self, model_output, attention_mask):
        token_embeddings = model_output[0]
        # note : token_embeddings.shape = [4,15,768], attention_mask.shape = [4,15], attention_mask.unsqueeze(-1).shape = [4,15,1], input_mask_expanded.shape[4,15,768]
        input_mask_expanded = attention_mask.unsqueeze(-1).expand(token_embeddings.size()).float()
        return torch.sum(token_embeddings * input_mask_expanded, 1) / torch.clamp(input_mask_expanded.sum(1), min=1e-9)


if bert_model is 'bert_base':
    model = Bert4Classification('bert-base-uncased', 2, 'attention', 1).to(device)
elif bert_model == 'sbert':
    model = Bert4Classification('sentence-transformers/all-MiniLM-L6-v2', 2, 'attention', 1).to(device)
elif bert_model == 'bertweet':
    model = Bert4Classification('vinai/bertweet-base', 2, 'attention', 1).to(device)

#dev=[torch.cuda.device(i) for i in range(torch.cuda.device_count())]
#model = torch.nn.DataParallel(model,device_ids=dev)


optimizer = optim.Adam(model.parameters(), lr=0.000005)
loss_function = nn.CrossEntropyLoss()


if based_on == 'tweet':
    samples, labels = read_data_tweet_based()
elif based_on == 'user':
    samples, labels = read_data_user_based()


tokenizer=AutoTokenizer.from_pretrained('sentence-transformers/all-MiniLM-L6-v2')

samples_train, samples_test, labels_train, labels_test = train_test_split(samples, labels, test_size=0.2,
                                                                          random_state=2, shuffle=True)
samples_train, samples_validate, labels_train, labels_validate = train_test_split(samples_train, labels_train,
                                                                                  test_size=0.2, random_state=2,
                                                                                  shuffle=True)
print("Batch All")
# model.load_state_dict(torch.load("./eval/Model_Epoch3"))
print(f'Run: {bert_model}, output is: {output}, test mode: {test}, based_on:{based_on}, max_length:200, sbert')
if not test:
    n_epoch = 10
    for epoch in range(n_epoch):
        model.train()
        all_loss = list()
        print("training phase starts for epoch" + str(epoch + 1))
        for train_label, train_sample in zip(labels_train, samples_train):
           # for sample in train_sample:
            model.zero_grad()
            x = tokenizer(train_sample, return_tensors="pt", padding="max_length", is_split_into_words=False, truncation=True, max_length=200)
            pred = model(x)
            target = torch.tensor([train_label], dtype=torch.long).to(device)
            loss = loss_function(pred, target)
            loss.backward()
            optimizer.step()
            all_loss.append(loss.cpu().detach().numpy())
        print(f'training epoch {epoch} mean of loss from training samples:', np.mean(all_loss))
        torch.save(model.state_dict(), f'/home/n_tahaei/IROSTEREO/data/eval/Model_{max_length}_{bert_model}_Epoch' + str(epoch + 1))
        model.eval()
        all_pred = list()
        print("validation phase starts for epoch" + str(epoch + 1))
        for validate_label, validate_sample in zip(labels_validate, samples_validate):
            # for sample in validate_sample:
            x = tokenizer(validate_sample, return_tensors="pt", padding="max_length", is_split_into_words=False, truncation=True, max_length=200)
            pred = model(x)
            pred = pred.cpu().detach().numpy()
            pred = np.argmax(pred)
            all_pred.append(pred)
        # print('prediction for the validation data: ', str(all_pred))
        print(classification_report(labels_validate, all_pred),flush=True)


if test:
    epoch = 9
    saved_model = Bert4Classification('sentence-transformers/all-MiniLM-L6-v2', 2, 'attention', 1).to(device)
    saved_model.load_state_dict(torch.load('/home/n_tahaei/IROSTEREO/data/eval/Model_{max_length}_{bert_model}_Epoch' + str(epoch + 1)))
    all_pred = list()
    with torch.no_grad():
        model.eval()
        for i, test_sample in enumerate(samples_test):
            #for sample in test_sample:
            pred = saved_model(test_sample)
            pred=pred.cpu().detach().numpy()
            pred = np.argmax(pred)
            all_pred.append(pred)
        print(all_pred)

    with open(f'/home/n_tahaei/IROSTEREO/data/eval/prediction_{max_length}_{bert_model}_Epoch_{str(epoch+1)}', 'w') as f:
        writer = csv.writer(f, delimiter='\t')
        writer.writerows(list(zip(labels_test, all_pred)))

    print('test prediction: ', classification_report(labels_test, all_pred))
