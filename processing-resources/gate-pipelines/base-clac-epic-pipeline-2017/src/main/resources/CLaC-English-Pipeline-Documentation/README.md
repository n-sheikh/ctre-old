# Clac Pipelines
Following are the definitions of the Clac pipelines.
The following abbreviations are used to describe the components of a pipeline: 
- JAPE : jape transducer
- GAZ : Annie Gazetter 
- OFF : Resource is switched off by default.
- PIPELINE : A pipeline (a pipeline can be a component of another pipeline)

## Clac English VP 
- JAPE : verb_cluster
- JAPE : non_finite_verb_cluster
- JAPE : rbach
## Clac English Scope
- GATE Morphological Analyser
- GAZ: Clac-Negator-Triggers
- Clac-Negator-Triggers
- GAZ: Clac-Scale-Shifters
- JAPE : Negator Triggers
- JAPE : Trigger Transducer
- Scoper
## Clac English Gazetteers
- GAZ : NRC Gazetteer
- GAZ : BingLiu Gazetteer
- GAZ : AFINN Gazetteer
- GAZ : MPQA Gazetteer
- GAZ : MPQA Transducer
## Clac-English-Trunk
- Document Reset PR
- ANNIE English Tokeniser
- OFF : Hashtag Tokeniser
- OFF : Twitter Tokenizer
- ANNIE Gazetteer : Very general name. Don't know what this does.
- OFF : Emoticons Gazetteer
- ANNIE Sentence Splitter
- ANNIE POS Tagger
- OFF : Tweet POS Tagger (EN)
- OFF : Stanford POS Tagger
- OFF : Tweet Normalizer (EN)
- ANNIE NE transducer
- OFF : ANNIE NE Twitter Transducer
- ANNIE OrthoMatcher
- OFF : Stanford NER
- StanfordParser
- CLaC-Number-Normalizerr
## Clac-English-PipeLine
- PIPELINE: Clac-English-Trunk
- PIPELINE: Clac-English-Gazetteers
- PIPELINE: Clac-English-Scope
- PIPELINE: Clac-English-VP