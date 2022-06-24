/* ExtractDomainOfNegationTriggers.java
 * Authors: Sabine Rosenberg
 * Date: Feb 2013
 * Purpose: This class is the main entry point for the module 
 * Point to version2: A/ have added the domain for multi word triggers
 * Point to version 3: works with collapsed deps()
 * works for modality triggers+ scope as well ... 
 *
 */

package ca.concordia.clac;

// Libraries to import
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import gate.*;
import gate.creole.*;
import gate.creole.metadata.*;
import gate.util.*;
import gate.annotation.*;
import gate.event.*;
import java.lang.*;
import java.io.*;
import java.net.*;


@CreoleResource(name = "ExtractDomainOfNegationTriggers",
		comment = "This resource annotates the relevent constituents within the domain of a negation trigger")


public class ExtractDomainOfNegationTriggers
	extends AbstractLanguageAnalyser
	implements ProcessingResource, ANNIEConstants {

	private URL fileName;
	private String dependenciesName;
	private String treeNodeName;
	//private String choice;
	private String tokenNodeName;
	private String sentenceNodeName;
	private boolean includeSubjectConstituent;
	private boolean includeSpanBeforeNot;
	private boolean usePropScope;
	//private boolean removeSbar;
  
        //an update by Canberk Ozdemir in order to point any Annotation Set for 
        // general annotation types used in this class as tokens, sentences etc.
        protected String annotationSetName;
       
    
    @Optional
    @RunTime
    @CreoleParameter(comment = "the set name for the used input annotation set name")
    public void setAnnotationSetName(String annotationSetName) {
        this.annotationSetName = annotationSetName;
    }

    public String getAnnotationSetName() {
        return this.annotationSetName;
    }
    
 

	@CreoleParameter(comment = "path to the scope mappings file",
			defaultValue = " ")
	public void setFileName(URL fileName) {
		this.fileName = fileName;
	}

	public URL getFileName() {
		return this.fileName;
	}

	@CreoleParameter(comment = "Name of Dependencies Annotation Set",
			defaultValue = "Dependency")
	public void setDependenciesName(String dependenciesName) {
		this.dependenciesName = dependenciesName;
	}

	public String getDependenciesName() {
		return this.dependenciesName;
	}

	@CreoleParameter(comment = "Name of Parse Tree Constituents Annotation Set",
			defaultValue = "SyntaxTreeNode")
	public void setTreeNodeName(String treeNodeName) {
		this.treeNodeName = treeNodeName;
	}

	public String getTreeNodeName() {
		return this.treeNodeName;
	}

	@CreoleParameter(comment = "Name of Token Annotation Set",
			defaultValue="Token")
	public void setTokenNodeName(String tokenNodeName) {
		this.tokenNodeName = tokenNodeName;
	}

	public String getTokenNodeName() {
		return this.tokenNodeName;
	}

	// Parameter for bioScope (annotation set name is from the xml)
	@CreoleParameter(comment = "name of Sentence AnnotationSet",
			defaultValue = "Sentence")
	public void setSentenceNodeName(String sentenceNodeName) {
		this.sentenceNodeName = sentenceNodeName;
	}

	public String getSentenceNodeName() {
		return this.sentenceNodeName;
	}

	//@RunTime
	@CreoleParameter(comment = "add wide scope",
			defaultValue = "false")
	public void setIncludeSubjectConstituent(Boolean includeSubjectConstituent) {
		this.includeSubjectConstituent = includeSubjectConstituent.booleanValue();
	}

	public Boolean getIncludeSubjectConstituent() {
		return new Boolean(this.includeSubjectConstituent);
	}

	@CreoleParameter(comment = "add wide scope for selfNeg Triggers",
			defaultValue = "false")
	public void setUsePropScope(Boolean usePropScope) {
		this.usePropScope = usePropScope.booleanValue();
	}

	public Boolean getUsePropScope() {
		return new Boolean(this.usePropScope);
	}

	/*@CreoleParameter(comment = "add span before not",
			defaultValue = "false")
	public void setUseLeftScopeBeforeNot(Boolean includeSpanBeforeNot) {
		this.includeSpanBeforeNot = includeSpanBeforeNot.booleanValue();
	}*/

	public Resource init() throws ResourceInstantiationException {
		//System.out.println("Initializing ExtractDomainOfNegationTriggers Module");
		return super.init();
	}

	public void reInit() throws ResourceInstantiationException {
		init();
	}
	private ArrayList <Annotation>notAnnotationsAlreadyAnnotated = null;
	private ArrayList <Long>negScopesAnnotated = null;

	/***************************************************************************
	 * method name: execute()
	 * function: this is the method that the GATE framework looks for in order for the 
	 * execution of the module to commence (the driver).
	 *
	 ****************************************************************************/

	public void execute()throws ExecutionException
	{

		if (document == null)
		{throw new GateRuntimeException("No document to process!");}
		//System.out.println("Executing ExtractDomainOfNegationTriggers...");
		//System.out.println(this.getDocument().getName());
		// read the parameters from the file.
		try
		{
                    
                    //This line was commented out by Nadia to deal with error generated by packaging
		    File fTest = new File(getClass().getResource(fileName.toString()).toExternalForm());
		    //File fTest = new File(fileName.toURI());

		    
			BufferedReader br = new BufferedReader(new FileReader(fTest));
			String strLine;
			while ((strLine = br.readLine()) != null)
			{
				String[] st = strLine.split(",");
				String inputAnnotationType= st[0];
				String outputAnnotationType= st[1];
				AnnotationSet triggerWordsAnnotationSet = this.getDocument().getAnnotations(annotationSetName).get(inputAnnotationType);
				if(triggerWordsAnnotationSet.size()!=0)
				{
					// **** get all annotations from xml list - based on the user requirements and prepare structures
					Annotation [] temp = new Annotation[triggerWordsAnnotationSet.size()];
					temp = triggerWordsAnnotationSet.toArray(temp);
					// get the type of annotation we are looking for....
					String testAnnotationType=temp[0].getFeatures().get("classType").toString().trim();
					// get all the sentence annotations
					AnnotationSet sentenceAnnotationSet =null;
					if(sentenceNodeName.equals("sentence"))
					{
						sentenceAnnotationSet = this.getDocument().getAnnotations("Original markups").get("sentence");
					}
					else
					{
						sentenceAnnotationSet = this.getDocument().getAnnotations(annotationSetName).get("Sentence");

					}
					includeSpanBeforeNot = false;
					// get all necessary annotations present in GATE
					AnnotationSet tokenAnnotationSet = this.getDocument().getAnnotations(annotationSetName).get(tokenNodeName);
					// get the needed annotation sets from the parser
					AnnotationSet syntaxTreeNodeAnnotationSetMaster = this.getDocument().getAnnotations(annotationSetName).get(treeNodeName);  
					AnnotationSet dependenciesAnnotationSet = this.getDocument().getAnnotations(annotationSetName).get(dependenciesName);   
					// our output annotation set
					AnnotationSet outputScopeAnnotations=this.getDocument().getAnnotations(annotationSetName);
					// these are all the deps that we have rules for ... 
					String[] possibleDepsList =null;
					String [] masterDepChecksList = {"neg","det","dep","pobj","pcomp","xcomp","advmod","amod","infmod","cc","nsubj","conj","nsubjpass","dobj","conj_negcc","preconj","conj_nor","conj_but","conj_and","conj_or","prep","ccomp","nn","expl","acomp","rcmod","auxpass","compl","cop","mark","aux"};

					if(inputAnnotationType.equals("implicitNegTriggers"))
					{
						String []impDepsList ={"det","pobj","pcomp","xcomp","advmod","amod","infmod","cc","conj","nsubjpass","prep","prep_of","prep_to","prep_in","prep_against","prep_with","prep_at","ccomp","conj_or","conj_and","dobj"};
						possibleDepsList =impDepsList;
					}
					else if(inputAnnotationType.equals("explicitNegTriggers"))
					{
						String[] expDepsList ={"neg","det","dep","pobj","pcomp","xcomp","advmod","amod","infmod","cc","nsubj","conj","dobj","conj_negcc","preconj","conj_nor","conj_but","conj_and","prep","prep_without","prep_of","prep_to","prep_in","prep_than","prepc_without","prep_for","prepc_than","prepc_instead_of","prep_instead_of","ccomp","expl","nsubjpass"};
						possibleDepsList = expDepsList;
						//System.out.println("here in exp....");
					}
					else if(inputAnnotationType.equals("modalTriggers"))
					{
						String [] modalDepsList
							={"aux","mark","cop","rcmod","complm","auxpass","prep_for","prepc_of","prep_of","prep_to","prep_in","prep_by","prep_from","prep_as","dobj","advmod","amod","infmod","acomp","nsubj","dep","ccomp","xcomp"};

						possibleDepsList =modalDepsList;
					}
					else if(inputAnnotationType.equals("valenceTriggers"))
					{
						String [] valDepsList
							={"aux","mark","cop","rcmod","complm","auxpass","prep_for","prepc_of","prep_of","prep_to","prep_in","prep_by","prep_from","prep_as","dobj","advmod","amod","infmod","acomp","dep","xcomp","ccomp"};

						possibleDepsList =valDepsList;
					}

					// is selfNeg ...
					else
					{
						if(usePropScope == true)
						{
							String [] selfDepsList ={"pobj","xcomp","advmod","amod","conj","nsubj","dobj","prep","ccomp","infmod","nn","acomp"};
							possibleDepsList = selfDepsList;
						}
						else
						{
							//System.out.println("here in self....");
							String [] selfDepsList ={"null"};
							possibleDepsList = selfDepsList;
						}
					}


					HashMap <Integer,ArrayList<Integer>> tokenPathsOfDoc = new HashMap<Integer,ArrayList<Integer>> ();

					AnnotationSet verbGrouperSet = this.getDocument().getAnnotations(annotationSetName).get("VG");

					// go through each sentence
					for(Annotation aSentence : sentenceAnnotationSet)
					{
						Long startS = aSentence.getStartNode().getOffset();
						Long endS = aSentence.getEndNode().getOffset();
						// get all tokens, dependencies, and parseTree constituents in the sentence
						AnnotationSet tokenAnnotationSetOfSentence = tokenAnnotationSet.getContained(startS,endS);
						ArrayList <Annotation> tokenListOfSentence= new ArrayList<Annotation>(tokenAnnotationSetOfSentence);
						OffsetComparator comparatorTestToks = new OffsetComparator();
						Collections.sort(tokenListOfSentence,comparatorTestToks);
						AnnotationSet depRelationsSetOfSentence = dependenciesAnnotationSet.getContained(startS,endS);
						ArrayList <Annotation> depsListOfSentence= new ArrayList<Annotation>(depRelationsSetOfSentence);
						OffsetComparator comparatorTestDeps = new OffsetComparator();
						Collections.sort(depsListOfSentence,comparatorTestDeps);

						AnnotationSet syntaxNodesOfSentence = syntaxTreeNodeAnnotationSetMaster.getContained(startS,endS);

						AnnotationSet verbGroupsOfSentence = verbGrouperSet.getContained(startS,endS);
						ArrayList depListOfSentence = new ArrayList();
						// to keep track of marked tokens ... 
						notAnnotationsAlreadyAnnotated =new ArrayList<Annotation>();
						// to keep track of marked spans ...
						negScopesAnnotated = new ArrayList<Long>();
						GenericScopeHelperClass.setTheList(negScopesAnnotated);
						if(syntaxNodesOfSentence.isEmpty()== false)
						{

							/****BEGIN SPECIAL CASES (NOT USING DEP RELATIONS FOR NEGATION & MODALITY ONLY)********************************************************/
							if(testAnnotationType.equals("NegTrigger"))
							{
								// not only, not just, not because ... 
								if(inputAnnotationType.equals("SelfNegOriginal") && usePropScope == false)
								{
									// do nothing
								}
								else
								{
									GenericScopeHelperClass.markNonDependencyCases(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet, outputScopeAnnotations, syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,outputAnnotationType);
								}
							}
							/****SPECIAL CASE FOR MODAL (NOT USING DEP RELATIONS)****************************************************************************/

							// MOVE THIS TO END :::*
							if(testAnnotationType.equals("ModalityTrigger"))
							{
								// NON DEP RULE FOR MODALS::: if the event in question is an infintival type:: then we want to say that it is modalized
								for(Annotation currentToken: tokenAnnotationSetOfSentence)
								{
									// return a boolean
									String posTagOfToken = InvestigateConstituentsFromParseTree.findPosTag (currentToken,syntaxNodesOfSentence);
									if(posTagOfToken.startsWith("V"))
										//if(posTagOfToken.startsWith("V")&& currentToken.getFeatures().get("root").equals("be")==false)
									{
										boolean foundInfVerb = GenericScopeHelperClass.findVerbGroupSpanInf(currentToken,verbGroupsOfSentence);
										// Annotation foundTrigger = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,currentToken);
										if (foundInfVerb == true && currentToken.getFeatures().get("root").equals("be")==false)
											//&& foundTrigger !=null)
										{
											GenericScopeHelperClass.annotateVerbsIfInfModalOrFreeVerb(currentToken,tokenAnnotationSetOfSentence,outputScopeAnnotations, syntaxNodesOfSentence, notAnnotationsAlreadyAnnotated,outputAnnotationType,depRelationsSetOfSentence,tokenPathsOfDoc,inputAnnotationType);


										}
										// new:::
										boolean foundFreeVerb = GenericScopeHelperClass.findVerbGroupSpanFree(currentToken, verbGroupsOfSentence,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
										if(foundFreeVerb ==true && currentToken.getFeatures().get("root").equals("be")==false)
										{
											GenericScopeHelperClass.annotateVerbsIfInfModalOrFreeVerb(currentToken,tokenAnnotationSetOfSentence,outputScopeAnnotations, syntaxNodesOfSentence, notAnnotationsAlreadyAnnotated,outputAnnotationType,depRelationsSetOfSentence,tokenPathsOfDoc,inputAnnotationType);
										}

										int currentSyntaxNodeID = InvestigateConstituentsFromParseTree.findSyntaxNode(currentToken,syntaxNodesOfSentence);

										GenericScopeHelperClass.findIfSpanIsAQuestionForModal(currentToken,"modalQuest",currentSyntaxNodeID,syntaxNodesOfSentence,tokenAnnotationSetOfSentence,outputAnnotationType,outputScopeAnnotations);
									}
								}


							}
							/****END SPECIAL CASES (NOT USING DEP RELATIONS)****************************************************************************/
							// go through each token in the sentence
							for(Annotation currentTokenAnnotation : tokenListOfSentence)
							{
								if(depRelationsSetOfSentence.isEmpty() == false)
								{
									ArrayList<String> depsOfCurrentTokenAnnotation = InvestigateDependencies.extractDependencies(currentTokenAnnotation, depsListOfSentence);
									if(depsOfCurrentTokenAnnotation.size()!=0)
									{
										depListOfSentence.add(currentTokenAnnotation); //entry even
										depListOfSentence.add(depsOfCurrentTokenAnnotation);//entry odd
									} //if
								}// if
							} 
							// go through all the dependencies in the sentence

							for(int i =0; i<depListOfSentence.size(); i+=2)
							{
								Annotation govenorToken = (Annotation)depListOfSentence.get(i);
								ArrayList<String> govenorTokenDeps = (ArrayList<String>) depListOfSentence.get(i+1);
								for(int k =0; k<govenorTokenDeps.size(); k+=2)
								{
									String depKind = govenorTokenDeps.get(k);
									Integer depID = Integer.parseInt(govenorTokenDeps.get(k+1));
									int indexOfDep =-1;
									// if(testAnnotationType.equals("NegTrigger")||testAnnotationType.equals("HedgeTrigger") ||testAnnotationType.equals("ModalityTrigger"))
									// {
									for(int m =0; m< possibleDepsList.length; m++)
									{
										if(depKind.equals(possibleDepsList[m]))

										{
											for(int n =0; n< masterDepChecksList.length; n++)
											{
												if (possibleDepsList[m].equals(masterDepChecksList[n]))
												{

													indexOfDep =n;
													break;  
												}

											} //for m
											if(possibleDepsList[m].startsWith("prep")&& possibleDepsList[m].equals("prep") == false)
											{
												indexOfDep = 31;

												break;

											}
										} // if depkind matches in list

									}//possible depList iter

									if(indexOfDep!=-1)
									{
                                                                            if(govenorToken!=null && depID!=null){
										//System.out.println("which case are we going to::"+ indexOfDep);
										String tempType;
										switch(indexOfDep)
										{ 
                                                                                    
											case 0:
												tempType = outputAnnotationType;
												DepRules.findScopeForNegDependency(govenorToken,triggerWordsAnnotationSet,tokenAnnotationSetOfSentence, tempType,syntaxNodesOfSentence,tokenPathsOfDoc, notAnnotationsAlreadyAnnotated, depID,outputScopeAnnotations,depRelationsSetOfSentence,includeSubjectConstituent,includeSpanBeforeNot);
												break;

											case 1:
												tempType= outputAnnotationType;
												DepRules.findScopeForDetDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,tokenPathsOfDoc,depRelationsSetOfSentence,testAnnotationType,includeSubjectConstituent,includeSpanBeforeNot);
												break;

											case 2:
												tempType = outputAnnotationType;
												DepRules.findScopeForDepDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,tokenPathsOfDoc,depRelationsSetOfSentence,testAnnotationType,includeSubjectConstituent);
											   	break;

											case 3:
												tempType = outputAnnotationType;
												DepRules.findScopeForPobjDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,tokenPathsOfDoc,depRelationsSetOfSentence,includeSubjectConstituent);
												break;

											case 4:
												tempType = outputAnnotationType;
												DepRules.findScopeForPCompDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,tokenPathsOfDoc,depRelationsSetOfSentence);
												break;

											case 5:
												tempType = outputAnnotationType;
												DepRules.findScopeForXCompDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,tokenPathsOfDoc,depRelationsSetOfSentence,testAnnotationType,includeSubjectConstituent);
												break;

											case 6:
												tempType = outputAnnotationType;
												DepRules.findScopeForAdvModDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,tokenPathsOfDoc,inputAnnotationType,testAnnotationType,depRelationsSetOfSentence,includeSubjectConstituent);
												break;

											case 7:
												tempType = outputAnnotationType;
												DepRules.findScopeForAModDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,tokenPathsOfDoc,depRelationsSetOfSentence,includeSubjectConstituent);
												break;

											case 8:
												tempType = outputAnnotationType;
												DepRules.findScopeForInfModDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,tokenPathsOfDoc,depRelationsSetOfSentence,includeSubjectConstituent);
												break;

											// new case - can we generalize for Neither/Nor?    
											case 9:
												tempType = outputAnnotationType;
												DepRules.findScopeForCCDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,depRelationsSetOfSentence,tokenPathsOfDoc);
												break;

											// new case - for nsubj relation (neg trigger pronouns)  
											case 10:
												tempType = outputAnnotationType;
												DepRules.findScopeForNSubjDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,depRelationsSetOfSentence,tokenPathsOfDoc,inputAnnotationType);
												break;

											case 11:
												tempType = outputAnnotationType;
												DepRules.findScopeForNConjDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,depRelationsSetOfSentence,tokenPathsOfDoc,inputAnnotationType,includeSubjectConstituent);
												break;

											// new for verb imp triggers
											case 12:
												tempType = outputAnnotationType;
												DepRules.findScopeForNSubjPassDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,depRelationsSetOfSentence,tokenPathsOfDoc,inputAnnotationType);
												break;

											case 13:
												tempType= outputAnnotationType;
												DepRules.findScopeForDobjDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,depRelationsSetOfSentence,tokenPathsOfDoc,inputAnnotationType,testAnnotationType,includeSubjectConstituent);
												break;

											case 14:
												tempType = outputAnnotationType;
												DepRules.findScopeForConjNegCCDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,depRelationsSetOfSentence,tokenPathsOfDoc,includeSubjectConstituent);
												break;

											// this is from the preconj dep...
											case 15:
												tempType = outputAnnotationType;
												DepRules.markNeither(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,tempType,depRelationsSetOfSentence,govenorToken,depID,tokenPathsOfDoc,includeSubjectConstituent);
												break;

											case 16:
												tempType = outputAnnotationType;
													DepRules.markConjNor(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,tempType,depRelationsSetOfSentence,govenorToken,depID,tokenPathsOfDoc);
												break;

											case 17:
												tempType = outputAnnotationType;
												DepRules.findScopeForConjNegBut(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,depRelationsSetOfSentence,tokenPathsOfDoc);
												break;

											case 18:
												tempType = outputAnnotationType;
												DepRules.findScopeForConjNegAnd(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,depRelationsSetOfSentence,tokenPathsOfDoc);
												break;
											
											// new for self neg scope
											case 19:
												tempType = outputAnnotationType;
												DepRules.findScopeForConjNegOr(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,depRelationsSetOfSentence,tokenPathsOfDoc);
												break;

											// new for or neg scope
											case 20:
												tempType = outputAnnotationType;
												DepRules.findScopeForPrepDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,tokenPathsOfDoc,depRelationsSetOfSentence,inputAnnotationType,includeSubjectConstituent);
												break;

											case 21:
												tempType = outputAnnotationType;
												DepRules.findScopeForCCompDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,depRelationsSetOfSentence,tokenPathsOfDoc,testAnnotationType,includeSubjectConstituent);
												break;

											case 22:
												tempType = outputAnnotationType;
												DepRules.findScopeFoNNDep(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,depRelationsSetOfSentence,tokenPathsOfDoc,testAnnotationType,includeSubjectConstituent);
												break;

											case 23:
												tempType = outputAnnotationType;
												DepRules.findScopeFoExplDep(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,depRelationsSetOfSentence,tokenPathsOfDoc,testAnnotationType,includeSubjectConstituent);
												break;

											case 24:
												tempType = outputAnnotationType;
												DepRules.findScopeForACompDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,tokenPathsOfDoc,depRelationsSetOfSentence,includeSubjectConstituent,testAnnotationType);
												break;

											case 25:
												tempType = outputAnnotationType;
												DepRules.findScopeForRCModDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,tokenPathsOfDoc,inputAnnotationType,testAnnotationType,depRelationsSetOfSentence);
												break;

											case 26:
												tempType = outputAnnotationType;
												DepRules.findScopeForAuxPassDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,depRelationsSetOfSentence,tokenPathsOfDoc);
												break;

											case 27:
												tempType = outputAnnotationType;
												DepRules.findScopeForComplDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,depRelationsSetOfSentence,tokenPathsOfDoc,testAnnotationType);
												break;

											case 28:
												tempType = outputAnnotationType;
												DepRules.markCop(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,depRelationsSetOfSentence,tokenPathsOfDoc,inputAnnotationType,testAnnotationType);
												break;

											case 29:
												tempType= outputAnnotationType;
												DepRules.findScopeForMarkDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,depRelationsSetOfSentence,tokenPathsOfDoc);
												break;

											case 30:
												tempType = outputAnnotationType;
												DepRules.findScopeForAuxDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,depRelationsSetOfSentence,tokenPathsOfDoc);
												break;

											case 31:
												tempType = outputAnnotationType;
												DepRules.findScopeForPrep_GenericDependency(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations,syntaxNodesOfSentence,notAnnotationsAlreadyAnnotated,govenorToken,depID,tempType,tokenPathsOfDoc,depRelationsSetOfSentence,inputAnnotationType,depKind,testAnnotationType,includeSubjectConstituent);
												break;
										} // end switch
									}// end if
                                                                        }// end if
								}//k
							}//i
							
							if(testAnnotationType.equals("NegTrigger") ||testAnnotationType.equals("ReportedSpeechVerbTrigger"))
							{
								if(inputAnnotationType.equals("SelfNegOriginal") && usePropScope == false)
								{
									// do nothing

								}
								else
								{
									GenericScopeHelperClass.annotateVerbsNotMarked(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations, syntaxNodesOfSentence, notAnnotationsAlreadyAnnotated,outputAnnotationType,depRelationsSetOfSentence,includeSubjectConstituent,tokenPathsOfDoc);
								}

								if(includeSubjectConstituent ==false)
								{
									GenericScopeHelperClass.annotateImpTriggersNotMarked(tokenAnnotationSetOfSentence,triggerWordsAnnotationSet,outputScopeAnnotations, syntaxNodesOfSentence, notAnnotationsAlreadyAnnotated,outputAnnotationType,depRelationsSetOfSentence,includeSubjectConstituent,tokenPathsOfDoc);
								}

							}

						}// end if to make sure there are synatx nodes for the sentence
						else
						{
							System.out.println("no nodes");
						}
						// new test
						//System.out.println("NEW SENTENCE:::");
						// GenericScopeHelperClass.printList();
					}// end iterating through sentences
				}// end there exsist triggers.....
			}//while
			br.close();
			//System.out.println("done");
		}// try
		catch (Exception e)
		{//Catch exception if any
			 e.printStackTrace();
		}
	}// execute method
	/****END EXECUTE METHOD*******************************************************************************************************************************************/


} // End class: ExtractScopeNegator


