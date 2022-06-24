!WARNING! : This is an old pipeline which does not annotate scope, consider using [my batch pipeline](https://clac.encs.concordia.ca/gitlab/ma_fauch/batch-pipeline) instead.

GenericTriggerScope
===================

Pipeline which takes a list of annotation types and detects the scope based on the syntax tree and the dependencies.

Currently being run on bioscope:

> ant localjar
> java -jar TriggerScope.jar ../bioscope/doc/trigger-scope/ `ls -d ../bioscope/doc/preprocess/`

Instructions
------------

There are two ways to run this. It can be run through ant with a distributable jar using ant run with the GATE_HOME variable set, or the build.properties value set. Otherwise, it can be compiled to a system dependant jar by running ant localjar, which will include GATE libraries in the manifest classpath.

Local:

  # Compile it
  ant localjar
  # run
  java -jar build/jar/GateImport.jar <output directory> <filePath> [<filePath> [...]]

Distributable:

  ant jar # optional, ant run compiles if necessary

  ant run -Dargs='<output directory> <filePath> [<filePath> [...]]'

The output files will be in <output directory> with the same name as the original file. These can be loaded directly into GATE.
