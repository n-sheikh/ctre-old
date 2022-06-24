/*
 * A GATE Batch pipeline
 *
 * (c) CLaC lab 2013
 * Author: Marc-André Faucher
 */

package clac;

import clac.creole.scope.Scoper;

import java.io.*;
import java.util.*;
import java.net.*;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.LanguageAnalyser;

import gate.creole.ANNIEConstants;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;

/**
 * A GATE pipeline used to extract the scope of a list of triggers
 */
public class BatchPipeline  {

    /** Parameters */
    private static String runName;
    private static ArrayList<String> exports;

    /** Flags */ //TODO: Make configurable
    private static final boolean DEBUG   = true;
    private static final boolean EXPORT  = false;

    /** The corpus pipeline */
    private SerialAnalyserController corpusPipeline;
    private Corpus corpus;


    /** Initialise the pipeline */
    public void init() throws GateException, IOException, URISyntaxException {
        // Initialize corpus and pipeline
	corpus = (Corpus) Factory.createResource("gate.corpora.CorpusImpl");
	corpusPipeline = (SerialAnalyserController)
        Factory.createResource("gate.creole.SerialAnalyserController");
        URL anniePath = new File(Gate.getPluginsHome(),
                                 ANNIEConstants.PLUGIN_DIR).toURI().toURL();

        Gate.getCreoleRegister().registerDirectories(anniePath);
        URL toolsPath = new File(Gate.getPluginsHome(),
                                 "Tools").toURI().toURL();
	System.out.println(toolsPath);
	Gate.getCreoleRegister().registerDirectories(toolsPath);

	URL stanfordPath = new URL(Gate.getGateHome().toURL(), "plugins/Stanford_CoreNLP");
        Gate.getCreoleRegister().registerDirectories( stanfordPath );

	
	System.out.println("Registering user plugins:");
        // Add user plugin paths to config file:
	for (URI path : getPluginPaths("resources/config/plugin-paths.conf")) {
		registerPluginDirectories(path);
	}

	FeatureMap params;
        String listsUrl;
        ProcessingResource gazetteer;


        //--/ START OF PIPELINE /--//
        System.out.println("Pipeline starts here");
	
	// GATE PreProcessing
	System.out.println("Document Reset PR");	
	addToPipeline("gate.creole.annotdelete.AnnotationDeletePR");

	System.out.println("CNC-Task 3 Gold Standard Transducer");
	listsUrl = BatchPipeline.class.getClassLoader().getResource("resources/transducers/ns-cnc-task3a3b-2022/gold_standard_annotations.jape").toString();
        params = Factory.newFeatureMap();
	params.put("inputASName", "Original markups");
        params.put("grammarURL", listsUrl);
        addToPipeline("gate.creole.Transducer", params);
		
	// ANNIE PRs
        System.out.println("Annie PR");
        addToPipeline("gate.creole.gazetteer.DefaultGazetteer");
        addToPipeline("gate.creole.tokeniser.DefaultTokeniser");
        addToPipeline("gate.creole.splitter.SentenceSplitter");
	addToPipeline("gate.creole.POSTagger");
	addToPipeline("gate.creole.ANNIETransducer");
	addToPipeline("gate.creole.orthomatcher.OrthoMatcher");

	/**
	System.out.println("Stanford POS Tagger");
	listsUrl = BatchPipeline.class.getClassLoader().getResource("resources/gate-EN-twitter.model").toString();
	FeatureMap featfeat = Factory.newFeatureMap();
	featfeat.put("modelFile",listsUrl);
	FeatureMap paramsparams = Factory.newFeatureMap();
	paramsparams.put("baseTokenAnnotationType","Token");
	paramsparams.put("failOnMissingInputAnnotations",false);
	ProcessingResource prpr = (LanguageAnalyser)Factory.createResource("gate.stanford.Tagger", featfeat, paramsparams);
	corpusPipeline.add(prpr);
	*/
	
	/**
	System.out.println("Twitter POS Tagger");
	addToPipeline("gate.twitter.pos.POSTaggerEN");
        */

	/// PARSER ///

        // Stanford parser
	// NOTE: set "reusePosTags" to false if using gate's Stanford Parser
        System.out.println("Stanford Parser");
        params = Factory.newFeatureMap();
        params.put("debug", false);
        params.put("addPosTags", true);
        params.put("reusePosTags", false);
        params.put("dependencyMode", "TypedCCprocessed");
        addToPipeline("gate.stanford.Parser", params);

	/// Number Normalizer ///
        System.out.println("Number Normalizer");
        listsUrl = BatchPipeline.class.getClassLoader()
                .getResource("resources/transducers/numbernormalizer/NumberNormalizer.jape").toString();
        params = Factory.newFeatureMap();
        params.put("grammarURL", listsUrl);
        addToPipeline("gate.creole.Transducer", params);

	
	// LEMMATIZER

        System.out.println("Lemmatizer");
        addToPipeline("gate.creole.morph.Morph");

        /// GAZETTEERS ///
        //TODO: parameters for including different gazetteers

	
        /// Sentiment ///
        // NRC Gazetteer (unigrams only)
        System.out.println("NRC Gazetteer");	
        listsUrl = BatchPipeline.class.getClassLoader()
                .getResource("resources/gazetteers/nrc/unigrams.def").toString();
        params = Factory.newFeatureMap();
        params.put("listsURL", listsUrl);
        params.put("caseSensitive", false);
        params.put("gazetteerFeatureSeparator", "}");
        addToPipeline("gate.creole.gazetteer.DefaultGazetteer", params);

        // BingLiu
        System.out.println("BingLiu Gazetteer");	
        listsUrl = BatchPipeline.class.getClassLoader()
                .getResource("resources/gazetteers/bingliu/BingLiu.def").toString();
        params = Factory.newFeatureMap();
        params.put("listsURL", listsUrl);
        params.put("caseSensitive", false);
        params.put("gazetteerFeatureSeparator", ":");
        addToPipeline("gate.creole.gazetteer.DefaultGazetteer", params);

        // A Finn
        System.out.println("AFinn Gazetteer");
        listsUrl = BatchPipeline.class.getClassLoader()
                .getResource("resources/gazetteers/afinn/afinn-111.def").toString();
        params = Factory.newFeatureMap();
        params.put("listsURL", listsUrl);
        params.put("caseSensitive", false);
        params.put("gazetteerFeatureSeparator", ":");
        addToPipeline("gate.creole.gazetteer.DefaultGazetteer", params);

        // MPQA
	System.out.println("MPQA Gazetteer");
        listsUrl = BatchPipeline.class.getClassLoader()
                .getResource("resources/gazetteers/mpqa/subjclueslen1-HLTEMNLP05.def").toString();
        params = Factory.newFeatureMap();
        params.put("listsURL", listsUrl);
        params.put("caseSensitive", false);
        params.put("gazetteerFeatureSeparator", ":");
        addToPipeline("gate.creole.gazetteer.DefaultGazetteer", params);

	System.out.println("MPQA Transducer");
	listsUrl = BatchPipeline.class.getClassLoader()
                .getResource("resources/transducers/mpqa/main.jape").toString();
        params = Factory.newFeatureMap();
        params.put("grammarURL", listsUrl);
        addToPipeline("gate.creole.Transducer", params);

        // Negator-trigger
	
        System.out.println("Negator-trigger");
        listsUrl = BatchPipeline.class.getClassLoader()
                .getResource("resources/gazetteers/negator-triggers/lists.def").toString();
        params = Factory.newFeatureMap();
        params.put("listsURL", listsUrl);
        params.put("caseSensitive", false);
        params.put("gazetteerFeatureSeparator", ":");
        gazetteer = (ProcessingResource) Factory.createResource(
                "gate.creole.gazetteer.DefaultGazetteer", params);
        params = Factory.newFeatureMap();
        params.put("inputFeatureNames",
                new ArrayList(Arrays.asList(new String[]{"Token.root"})));
        params.put("gazetteerInst", gazetteer);
        addToPipeline("gate.creole.gazetteer.FlexibleGazetteer", params);

        // Halil's Scale Shifters
        System.out.println("Halil's Scale Shifter");
        listsUrl = BatchPipeline.class.getClassLoader()
                .getResource("resources/gazetteers/halil/lists.def").toString();
        params = Factory.newFeatureMap();
        params.put("listsURL", listsUrl);
        params.put("caseSensitive", false);
        params.put("gazetteerFeatureSeparator", ":");
        addToPipeline("gate.creole.gazetteer.DefaultGazetteer", params);

	//Causality Gazetteers (Taken from MCM, Khoo - added by N.S. May 24th 2022)
	System.out.println("Causality-trigger");
        listsUrl = BatchPipeline.class.getClassLoader()
                .getResource("resources/gazetteers/causality-triggers/lists.def").toString();
        params = Factory.newFeatureMap();
        params.put("listsURL", listsUrl);
        params.put("caseSensitive", false);
        params.put("gazetteerFeatureSeparator", ":");
        gazetteer = (ProcessingResource) Factory.createResource(
                "gate.creole.gazetteer.DefaultGazetteer", params);
        params = Factory.newFeatureMap();
        params.put("inputFeatureNames",
                new ArrayList(Arrays.asList(new String[]{"Token.root"})));
        params.put("gazetteerInst", gazetteer);
        addToPipeline("gate.creole.gazetteer.FlexibleGazetteer", params);
	
	System.out.println("Causality Transducer");
	listsUrl = BatchPipeline.class.getClassLoader()
                .getResource("resources/transducers/causality-triggers.jape").toString();
        params = Factory.newFeatureMap();
        params.put("grammarURL", listsUrl);
        addToPipeline("gate.creole.Transducer", params);


	
        /// SCOPE ///

        // Trigger Transducer (Scoper input annotation format)
        System.out.println("Trigger transducer");
        listsUrl = BatchPipeline.class.getClassLoader()
                .getResource("resources/transducers/triggers/main.jape").toString();
        params = Factory.newFeatureMap();
        params.put("grammarURL", listsUrl);
        addToPipeline("gate.creole.Transducer", params);

        // Negator
        System.out.println("Negator");
        listsUrl = BatchPipeline.class.getClassLoader()
                .getResource("resources/transducers/negator-triggers/main.jape").toString();
        params = Factory.newFeatureMap();
        params.put("grammarURL", listsUrl);
        addToPipeline("gate.creole.Transducer", params);

        // ExtractDomainOfNegationTriggers
        System.out.println("Extract Domain of Negation Triggers");	
        params = Factory.newFeatureMap();
        listsUrl = (new File(new File("."),
                "src/resources/config/negator-all-scope.conf")).getAbsolutePath();
        params.put("fileName", listsUrl);
        addToPipeline("clac.creole.extractDomainOfNegationTriggers"
                                +".ExtractDomainOfNegationTriggers", params);

        // Scoper
        System.out.println("Scoper");
        params = Factory.newFeatureMap();
        params.put("triggerAnnName", Scoper.TRIGGER_ANNOTATION_TYPE);
        params.put("enableGrammarScope", false);
        addToPipeline("clac.creole.scope.Scoper", params);

	// VP
	listsUrl = BatchPipeline.class.getClassLoader()
                .getResource("resources/transducers/verb_cluster.jape").toString();
        params = Factory.newFeatureMap();
        params.put("grammarURL", listsUrl);
        addToPipeline("gate.creole.Transducer", params);
	
	listsUrl = BatchPipeline.class.getClassLoader()
                .getResource("resources/transducers/NonFiniteVerbGroups.jape").toString();
        params = Factory.newFeatureMap();
        params.put("grammarURL", listsUrl);
        addToPipeline("gate.creole.Transducer", params);

	listsUrl = BatchPipeline.class.getClassLoader()
                .getResource("resources/transducers/rb.jape").toString();
        params = Factory.newFeatureMap();
        params.put("grammarURL", listsUrl);
        addToPipeline("gate.creole.Transducer", params);

        //-/ END OF PIPELINE /-//
	System.out.println("End of pipeline");
        corpusPipeline.setCorpus(corpus);

    }

    /** Add an annotationModifier to the pipeline */
    public void addAnnotationModifier(String inAnns, String[] anns, String outAnns,
            String newAnn, boolean delete) throws GateException {
        FeatureMap params = Factory.newFeatureMap();
        params.put("inputAnnotationSetName", inAnns);
        params.put("annotationNamesToBeModified", new ArrayList(Arrays.asList(anns)));
        params.put("outputAnnotationSetName", outAnns);
        params.put("outputAnnotationName", newAnn);
        params.put("deleteInputAnnotations", delete);
        addToPipeline("clac.creole.annotationModifier.annotationModifier", params);
    }

    /** Returns a list of user specified paths from the config file */
    public List<URI> getPluginPaths(String configFile)
            throws URISyntaxException, FileNotFoundException {
        InputStream in = BatchPipeline.class.getClassLoader()
                         .getResourceAsStream(configFile);
        Scanner s = new Scanner(in);
        ArrayList<URI> list = new ArrayList<URI>();
        while (s.hasNext()){
            list.add(new File(s.next()).toURI());
        }
        s.close();
	for (int i = 0; i < list.size(); i++){
	    System.out.println(list.get(i).toString());
	}
        return list;
    }

    /** Registers all plugin directories in the user specified path */
    public void registerPluginDirectories(URI path)
            throws MalformedURLException {
        File file = new File(path);
        // Get a list of subdirectories
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        });
        for (int i = 0; i < directories.length; i++) {
            File plugin = new File(new File(path), directories[i]);
            try {
                Gate.getCreoleRegister().registerDirectories(plugin.toURI().toURL());
                if (DEBUG) System.out.println(plugin);
            } catch (GateException e) {
                // Ignore directories that don't contain a plugin.
            }
        }
    }

    /** Onliner which adds a new PR to the pipeline */
    public void addToPipeline(String pr, FeatureMap params) throws GateException {
        ProcessingResource newPR =
            (ProcessingResource) Factory.createResource(pr, params);
        corpusPipeline.add(newPR);
    }
    public void addToPipeline(String pr) throws GateException {
	try{
	    ProcessingResource newPR =
                (ProcessingResource) Factory.createResource(pr);
        corpusPipeline.add(newPR);
	}catch(Exception e){
	    System.out.println(e.getMessage());
	    e.printStackTrace();
	}
    }

    /** Get the corpus. */
    public Corpus getCorpus() {
        return corpus;
    }

    /** Run the pipeline */
    public void execute() throws GateException {
        corpusPipeline.execute();
    }

    /**
     * @param args: A directory where to store corpus items and a list of file paths
     */
    public static void main(String args[])
            throws GateException, IOException,
                   URISyntaxException, FileNotFoundException {
        org.apache.log4j.BasicConfigurator.configure();
        // Needs at least 2 arguments
	System.out.println("Reading in console arguments");
        if (args.length < 3) {
            throw new GateException("Insufficient parameters.");
        }

        // Initialise the GATE library

	System.out.println("Initialise the GATE library");
	Gate.init();
        runName = args[0];
	
	System.out.println("Create or open output directory");	
        // Create or open output directory
        File corpusDir = new File(args[1]);
        if (!corpusDir.isDirectory()) {
            throw new GateException("Second argument must be a directory "
                                   +"where the corpus will be stored.");
        }

        // Log System.err to file instead of stdout
        OutputStream output = new FileOutputStream("messages.log");
        PrintStream printErr = new PrintStream(output);
        System.setErr(printErr);

        // Setup pipeline
	System.out.println("Create a batch pipeline object");	
        BatchPipeline app = new BatchPipeline();
	System.out.println("Intialize a batch pipeline");		
        app.init();
        Corpus corpus = app.getCorpus();

        // Add documents to corpus
        for(int i = 2; i < args.length; i++) {
            File inputFile  = new File(args[i]);
            File outputFile = new File(corpusDir, inputFile.getName());

            if (DEBUG) System.out.println(outputFile.toURI());

            Document doc = (Document) Factory.newDocument(inputFile.toURI().toURL());
            corpus.add(doc);

            try {
                app.execute();
                // Save output
                try {
                    System.out.println("Writing to file");
                    FileWriter outputFileWriter = new FileWriter(outputFile);
                    FeatureMap docFeatures = doc.getFeatures();
                    outputFileWriter.write(doc.toXml());
                    outputFileWriter.close();
                }
                catch (IOException e) {
                    System.err.println("ERROR: file or directory does not exist: "
                                      +outputFile.toURI().toString());
                }

            }
            finally {
                corpus.clear();
                Factory.deleteResource(doc);
            }
        }
    }
}

