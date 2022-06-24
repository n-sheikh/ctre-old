/*
 * A GATE pipeline used to extract the scope of a list of triggers
 * 
 * (c) CLaC lab 2013
 * Author: Marc-André Faucher 
 */

package clac;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;

import gate.creole.ANNIEConstants;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;

/**
 * A GATE pipeline used to extract the scope of a list of triggers
 */
public class TriggerScope  {

	/** The corpus pipeline */
	private SerialAnalyserController corpusPipeline;
	private Corpus corpus;

	/** Initialise the pipeline */
	public void init() throws GateException, IOException {
		// Load the corpus parser
		corpus = (Corpus) Factory.createResource("gate.corpora.CorpusImpl");

		corpusPipeline = (SerialAnalyserController) 
				Factory.createResource("gate.creole.SerialAnalyserController");

		// Register the ANNIE plugins directory
		Gate.getCreoleRegister().registerDirectories(
				new File(Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR).toURI().toURL());

		String listsUrl;
		FeatureMap params;

		/// START OF PIPELINE ///

		/*
		// Add Tokeniser PR
		ProcessingResource tokeniserPR = (ProcessingResource)
				Factory.createResource("gate.creole.tokeniser.DefaultTokeniser");
		corpusPipeline.add(tokeniserPR);
		
		// Add Sentence Splitter PR
		ProcessingResource sentenceSplitterPR = (ProcessingResource)
				Factory.createResource("gate.creole.splitter.SentenceSplitter");
		corpusPipeline.add(sentenceSplitterPR);
		
		//Load the Stanford parser
		params = Factory.newFeatureMap();
		params.put("addPosTags", true);
		Gate.getCreoleRegister().registerDirectories( new File(
				Gate.getPluginsHome(), "Parser_Stanford").toURI().toURL() );
		ProcessingResource stanfordParserPR = (ProcessingResource)
				Factory.createResource("gate.stanford.Parser", params);
		corpusPipeline.add(stanfordParserPR);
		*/

		/*
		// MPQA
		listsUrl = TriggerScope.class.getClassLoader()
				.getResource("resources/gazetteers/mpqa/subjclueslen1-HLTEMNLP05.def").toString();
		params = Factory.newFeatureMap();
		params.put("listsURL", listsUrl);
		params.put("caseSensitive", false);
		params.put("gazetteerFeatureSeparator", ":");
		ProcessingResource mpqaGazetteerPR = (ProcessingResource)
				Factory.createResource("gate.creole.gazetteer.DefaultGazetteer", params);
		corpusPipeline.add(mpqaGazetteerPR);
		*/

		// Halil Triggers (ALL)
		listsUrl = TriggerScope.class.getClassLoader()
				.getResource("resources/gazetteers/halil-valence-strict/lists.def").toString();
		//		.getResource("resources/gazetteers/halilAll-strict/lists.def").toString();
		params = Factory.newFeatureMap();
		params.put("listsURL", listsUrl);
		params.put("caseSensitive", false);
		//params.put("gazetteerFeatureSeparator", ":");
		ProcessingResource halilGazetteerPR = (ProcessingResource)
				Factory.createResource("gate.creole.gazetteer.DefaultGazetteer", params);
		corpusPipeline.add(halilGazetteerPR);
		
		// Trigger Transducer
		listsUrl = TriggerScope.class.getClassLoader()
				.getResource("resources/transducers/triggers/main.jape").toString();
		params = Factory.newFeatureMap();
		params.put("grammarURL", listsUrl);
		ProcessingResource triggerTransducerPR = (ProcessingResource)
				Factory.createResource("gate.creole.Transducer", params);
		corpusPipeline.add(triggerTransducerPR);

		// Set the corpus for the pipeline
		corpusPipeline.setCorpus(corpus);
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
	public static void main(String args[]) throws GateException, IOException {
		// Needs at least 2 arguments
		if (args.length < 2) {
			throw new GateException("Insufficient parameters.");
		}

		// Initialise the GATE library
		Gate.init();

		// Create or open 
		File corpusDir = new File(args[0]);
		if (!corpusDir.isDirectory()) {
			throw new GateException("Argument 1 must be a directory "
					               +"where the corpus will be stored.");
		}

		// Setup pipeline
		TriggerScope app = new TriggerScope();
		app.init();
		Corpus corpus = app.getCorpus();

		for(int i = 1; i < args.length; i++) {
			File inputFile  = new File(args[i]);
			File outputFile = new File(corpusDir, inputFile.getName());
			System.out.println(outputFile.toURI());

			Document doc = (Document) Factory.newDocument(inputFile.toURI().toURL());
			corpus.add(doc);

			try {
				app.execute();
				// Save output
				try {
					FileWriter outputFileWriter = new FileWriter(outputFile);
					FeatureMap docFeatures = doc.getFeatures();
					outputFileWriter.write(doc.toXml());
					outputFileWriter.close();
				}
				catch (IOException e) {
					// @TODO Log it?
				}

			} // try (parse doc)
			finally {
				// Release memory
				corpus.clear();
				Factory.deleteResource(doc);
			} 
		}// for each of args
	} // main

}
