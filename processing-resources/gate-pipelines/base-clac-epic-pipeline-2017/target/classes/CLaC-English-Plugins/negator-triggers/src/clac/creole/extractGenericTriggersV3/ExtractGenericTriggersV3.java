/* ExtractGenericTriggersV2.java
 * Authors:
 * Date: February 2012
 * Purpose: This class is the main entry point for the module
 * The point of this update is to allow for the addition of multi word triggers
 *
 *
 */

package clac.creole.extractGenericTriggersV3;
//import the required libraries
import java.util.*;
import gate.*;
import gate.creole.*;
import gate.creole.metadata.*;
import gate.util.*;
import gate.annotation.*;
import gate.event.*;
// for reading the xml and map...
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.net.*;
import java.io.*;


/** 
 * This class is the implementation of the resource ExtractNegTriggers.
 * Dependency: an xml file of negative trigger words 
 */
@CreoleResource(name = "ExtractGenericTriggersV3",
		comment = "Add a descriptive comment about this resource")
public class ExtractGenericTriggersV3  extends AbstractLanguageAnalyser
	implements ProcessingResource, ANNIEConstants {
	private String annotationType;
	private URL fileName;
	private String treeNodeName;
	private String tokenNodeName;
        protected String annotationSetName;

        //@Optional
    @RunTime
    @CreoleParameter(comment = "the set name for the used input annotation set name")
    public void setAnnotationSetName(String annotationSetName) {
        this.annotationSetName = annotationSetName;
    }

    public String getAnnotationSetName() {
        return this.annotationSetName;
    }
        
	@CreoleParameter(comment = "path to the parameters for the triggers",
			defaultValue = " ")
	public void setFileName(URL fileName) {
		this.fileName = fileName;
	}

	public URL getFileName() {
		return this.fileName;
	}

	@CreoleParameter(comment = "Name of Parse Tree Constituents Annotation Set",
			defaultValue="SyntaxTreeNode")
	public void setParseTreeAnnotationParemeter(String treeNodeName) {
		this.treeNodeName = treeNodeName;
	}

	public String getParseTreeAnnotationParemeter() {
		return this.treeNodeName;
	}

	@CreoleParameter(comment = "Name of Token Annotation Set",
			defaultValue="Token")
	public void setTokenAnnotationParemeter(String tokenNodeName) {
		this.tokenNodeName = tokenNodeName;
	}

	public String getTokenAnnotationParemeter() {
		return this.tokenNodeName;
	}

	public Resource init() throws ResourceInstantiationException {
		//System.out.println("Initializing ExtractGenericTriggersv3 Module");
		return super.init();
	}
	public void reInit() throws ResourceInstantiationException {
		init();
	}

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

		//System.out.println("Executing ExtractGenericTriggersV3 Module...");
		AnnotationSet tokenAnnotationSet = this.getDocument().getAnnotations(annotationSetName).get(tokenNodeName);
		AnnotationSet HLAnnotationSet = this.getDocument().getAnnotations("Original markups").get("HL");
		AnnotationSet outputAnnotations=this.getDocument().getAnnotations(annotationSetName);
		AnnotationSet syntaxTreeNodeAnnotationSet = this.getDocument().getAnnotations(annotationSetName).get(treeNodeName);  
		long countforNewIds =0;
		long HLendOffset =-1;
		if (HLAnnotationSet.size()!=0)
		{

			HLendOffset = HLAnnotationSet.lastNode().getOffset();

		}
                String token ="";
		try
		{
			File fTest = new File(fileName.toURI());    
			BufferedReader br = new BufferedReader(new FileReader(fTest));
			String strLine;
                        
			while ((strLine = br.readLine()) != null)
			{
				String[] st = strLine.split(",");
				File file = new File(st[0]);
				URL triggerFile = null;

				try
				{

					triggerFile = file.toURL();
				}

				catch (MalformedURLException me)
				{

					System.out.println("Converting process error");

				}
				annotationType = st[1];
				Map<String, List<GenericTrigger>> genTriggers=XMLReader.readTriggers(triggerFile);
				for (Annotation tokenAnnotation: tokenAnnotationSet)
				{
                                        token = (String)tokenAnnotation.getFeatures().get("string");
					if(HLAnnotationSet.size() ==0 || ( tokenAnnotation.getStartNode().getOffset() > HLendOffset))
					{

						String tokenString = tokenAnnotation.getFeatures().get("string").toString().trim().toLowerCase();
						String tokenRoot= tokenAnnotation.getFeatures().get("root").toString().trim();
						// find an inital match
						GenericTrigger match =null;
						long sOffset =-1;
						long fOffset=-1;
						long interOffset =-1;
						// New case (Feb 2012) .. test multiwords::: -need to make sure that it does not get annotated twice so need to pull from the trigger list
						if (checkIfMultiWordTrigger(tokenAnnotation,tokenString,genTriggers,annotationType,tokenAnnotationSet, outputAnnotations) == true)
						{
							//done
						}


						// Now test single words ....this is the base case... ( note the actual string is the root)...
						//else if (genTriggers.containsKey(tokenRoot))
						else if ((genTriggers.containsKey(tokenString))||(genTriggers.containsKey(tokenRoot)))
						{
							List<GenericTrigger> matchedList = null;
							if(annotationType.equals("explicitNegTriggers")&& (genTriggers.containsKey(tokenString))&&tokenAnnotation.getFeatures().get("string").toString().trim().equals("NO")==false)
							{
								matchedList=genTriggers.get(tokenString);
							}
							else if(annotationType.equals("explicitNegTriggers")==false)
							{
								if(genTriggers.containsKey(tokenRoot))
								{
									matchedList=genTriggers.get(tokenRoot);
								}
								else
								{
									matchedList=genTriggers.get(tokenString);

								}
							}
							// ALL Triggers will have the following annotation features...
							if (matchedList!=null)
							{

								match = matchedList.get(0);
								// System.out.println(match.getTriggerType());
								String posTagOfToken = findPosTag(tokenAnnotation,syntaxTreeNodeAnnotationSet);
								//if((match.getPosType().startsWith("V") && posTagOfToken.startsWith("V")) || (match.getPosType().startsWith("V")== false))
								//{

								// ADD A NEW ANNOTATION SET
								sOffset = tokenAnnotation.getStartNode().getOffset();
								fOffset = tokenAnnotation.getEndNode().getOffset();
								// 1st TRIGGER type: Negation Triggers     
								if(match.getTriggerType().equals("NegTrigger"))
								{

									NegTrigger s= (NegTrigger)match;
									// make a cusom annotation for nobody, nothing
									if((tokenString.toLowerCase().equals("nobody")) || (tokenString.toLowerCase().equals("nothing")))
									{
										/* NEW PUT AS ORIGINAL */
										gate.FeatureMap triggerFeaturesOrig = Factory.newFeatureMap();
										triggerFeaturesOrig.put("Ann_Type",annotationType);
										triggerFeaturesOrig.put("Type", match.getTriggerSubType());
										triggerFeaturesOrig.put("Pos_category", posTagOfToken);
										triggerFeaturesOrig.put("Prior_Polarity", s.getPriorPolarityVal());
										triggerFeaturesOrig.put("String", tokenString);
										triggerFeaturesOrig.put("classType","NegTrigger");

										if(s.getSource()!=null)
										{
											triggerFeaturesOrig.put("source", s.getSource());

										}

										if(s.getAdditionalCat()!=null)
										{
											triggerFeaturesOrig.put("extensionCat",s.getAdditionalCat());
										}
										printToAnnotationSet(sOffset,fOffset,annotationType,triggerFeaturesOrig,outputAnnotations);
										// end put new .... 
										String nounPartOfWord  = tokenString.toLowerCase().substring(2);
										int nounPartOfWordLength = nounPartOfWord.length();
										sOffset = tokenAnnotation.getStartNode().getOffset();
										fOffset = tokenAnnotation.getStartNode().getOffset()+2;
										countforNewIds++;

										/* mark "no"
										   long nsoffset = tokenAnnotation.getStartNode().getOffset();
										   long nfoffset = tokenAnnotation.getStartNode().getOffset()+2;
										   gate.FeatureMap nTokenFeatures= Factory.newFeatureMap();
										   nTokenFeatures.put("category","DT");
										   nTokenFeatures.put("kind","word");
										   nTokenFeatures.put("length","2");
										   nTokenFeatures.put("root","no");
										   nTokenFeatures.put("string","no");
										   nTokenFeatures.put("customId", countforNewIds);
										   nTokenFeatures.put("originalTokenID", tokenAnnotation.getId());
										   printToAnnotationSet(nsoffset,nfoffset,"CustomToken",nTokenFeatures,outputAnnotations);*/

										/* mark body...
										   nsoffset = tokenAnnotation.getStartNode().getOffset()+2;
										   nfoffset = tokenAnnotation.getEndNode().getOffset();
										   gate.FeatureMap nTokenFeaturesT= Factory.newFeatureMap();
										   nTokenFeaturesT.put("category","NN");
										   nTokenFeaturesT.put("dependencies","[det("+countforNewIds+")]");
										   nTokenFeaturesT.put("kind","word");
										   nTokenFeaturesT.put("length",nounPartOfWordLength);
										   nTokenFeaturesT.put("root",nounPartOfWord);
										   nTokenFeaturesT.put("string",nounPartOfWord );
										   nTokenFeaturesT.put("originalTokenID", tokenAnnotation.getId());
										   printToAnnotationSet(nsoffset,nfoffset,"CustomToken",nTokenFeaturesT,outputAnnotations);*/

										/* mark the normal negTrigger as well.
										   gate.FeatureMap triggerFeatures = Factory.newFeatureMap();
										   triggerFeatures.put("Ann_Type",annotationType);
										   triggerFeatures.put("Type", match.getTriggerSubType());
										   triggerFeatures.put("Pos_category", posTagOfToken);
										   triggerFeatures.put("Prior_Polarity", s.getPriorPolarityVal());

										   triggerFeatures.put("String","no");
										   triggerFeatures.put("classType","NegTrigger");
										   if(s.getSource()!=null)
										   {
										   triggerFeatures.put("source", s.getSource());

										   }
										   printToAnnotationSet(sOffset,fOffset,"customTokenTrigger",triggerFeatures,outputAnnotations);*/
									}// end is nobody or nothing::CASE_1

									//CASE 2: Mark any affixal Neg Triggers (from a WordList...)
									else if(s.getPrefix()!=null)
									{
										// make an annotation for the entire token ... 
										gate.FeatureMap origFeatures = Factory.newFeatureMap();
										origFeatures.put("Ann_type","SelfNegOriginal");
										origFeatures.put("Type","Affixal_Negation");
										origFeatures.put("Affix_Neg_Trigger",s.getPrefix());
										origFeatures.put("String",tokenAnnotation.getFeatures().get("string").toString().trim());
										origFeatures.put("classType","NegTrigger");
										printToAnnotationSet(sOffset,fOffset,"SelfNegOriginal",origFeatures,outputAnnotations);


										String tokenNoPrefix  = " ";
										String tokenOrig =" ";
										if(tokenString.startsWith("dis"))
										{
											interOffset = tokenAnnotation.getStartNode().getOffset()+3;
											tokenOrig =tokenAnnotation.getFeatures().get("string").toString().trim();
											tokenNoPrefix = tokenOrig.substring(3,tokenOrig.length());
										}
										else if(tokenString.endsWith("less"))
										{
											interOffset = tokenAnnotation.getEndNode().getOffset()-4;
											tokenOrig =tokenAnnotation.getFeatures().get("string").toString().trim();
											tokenNoPrefix = tokenOrig.substring(0,tokenOrig.length()-4);
										}
										else
										{
											tokenOrig =tokenAnnotation.getFeatures().get("string").toString().trim();
											interOffset = tokenAnnotation.getStartNode().getOffset()+2;
											tokenNoPrefix = tokenOrig.substring(2,tokenOrig.length());
										}
										gate.FeatureMap scopeFeatures = Factory.newFeatureMap();
										scopeFeatures.put("Ann_type","Neg_Scope");
										scopeFeatures.put("Type","Affixal_Negation");
										scopeFeatures.put("Affix_Neg_Trigger",s.getPrefix());
										scopeFeatures.put("Text_span",tokenNoPrefix);
										scopeFeatures.put("Original_Token_ID",tokenAnnotation.getId());
										if(tokenString.endsWith("less"))
										{
											printToAnnotationSet(sOffset,interOffset,"selfNeg_Scope",scopeFeatures,outputAnnotations);
										}
										else
										{
											printToAnnotationSet(interOffset,fOffset,"selfNeg_Scope",scopeFeatures,outputAnnotations);
										}

										gate.FeatureMap triggerFeatures = Factory.newFeatureMap();
										triggerFeatures.put("Ann_Type",annotationType);
										triggerFeatures.put("Type", match.getTriggerSubType());
										triggerFeatures.put("String",s.getPrefix());
										triggerFeatures.put("classType","NegTrigger");
										triggerFeatures.put("Original_Token_ID",tokenAnnotation.getId());
										if(s.getSource()!=null)
										{
											triggerFeatures.put("source", s.getSource());

										}
										if(tokenString.endsWith("less"))
										{
											printToAnnotationSet(interOffset,fOffset,annotationType,triggerFeatures,outputAnnotations);
										}
										else
										{
											printToAnnotationSet(sOffset,interOffset,annotationType,triggerFeatures,outputAnnotations);
										}
									}// if prefix_suffix

									// CASE 3 for all other NegTriggers
									else
									{
										gate.FeatureMap triggerFeatures = Factory.newFeatureMap();
										triggerFeatures.put("Ann_Type",annotationType);
										triggerFeatures.put("Type", match.getTriggerSubType());
										triggerFeatures.put("Pos_category", posTagOfToken);
										triggerFeatures.put("Prior_Polarity", s.getPriorPolarityVal());
										triggerFeatures.put("String", tokenString);
										triggerFeatures.put("classType","NegTrigger");

										if(s.getSource()!=null)
										{
											triggerFeatures.put("source", s.getSource());

										}

										if(s.getAdditionalCat()!=null)
										{
											triggerFeatures.put("extensionCat",s.getAdditionalCat());
										}
										printToAnnotationSet(sOffset,fOffset,annotationType,triggerFeatures,outputAnnotations);
									} // last negCase(default) 

								} // if isNegTrigger
								// 2nd TRIGGER type: Valence Triggers**need to add a subtype ...
								else if(match.getTriggerType().equals("ValenceTrigger"))
								{
									gate.FeatureMap triggerFeatures = Factory.newFeatureMap();
									triggerFeatures.put("String", tokenString);
									ValenceTrigger s= (ValenceTrigger)match;
									triggerFeatures.put("Inital_Degree", s.getInitDegree());
									triggerFeatures.put("Description", s.getDescription());
									triggerFeatures.put("classType","ValenceTrigger");
									triggerFeatures.put("String", tokenString);
									printToAnnotationSet(sOffset,fOffset,annotationType,triggerFeatures,outputAnnotations);

								} // if isValenceTrigger
								// 3rd TRIGGER type: Hedge Triggers
								else if(match.getTriggerType().equals("HedgeTrigger"))
								{
									//System.out.println("here");
									gate.FeatureMap triggerFeatures = Factory.newFeatureMap();
									triggerFeatures.put("String", tokenString);
									HedgeTrigger s= (HedgeTrigger)match;
									String posIndicator = posTagOfToken.substring(0,1);
									if(s.getPosCat().startsWith(posIndicator))
									{
										triggerFeatures.put("Type", match.getTriggerSubType());
										triggerFeatures.put("Strength", s.getStrength());
										triggerFeatures.put("source", s.getSource());
										triggerFeatures.put("classType","HedgeTrigger");
										printToAnnotationSet(sOffset,fOffset,annotationType,triggerFeatures,outputAnnotations);
									}

								} // if isHedgeTrigger
								// 4th TRIGGER type: ReportedSepeechVerb Triggers
								else if(match.getTriggerType().equals("ReportedSpeechVerbTrigger"))
								{
									if(posTagOfToken.startsWith("V"))
									{
										// don't have anything custom to put at this time ... 
										gate.FeatureMap triggerFeatures = Factory.newFeatureMap();
										triggerFeatures.put("String", tokenString);
										triggerFeatures.put("classType","ReportedSpeechVerbTrigger");
										printToAnnotationSet(sOffset,fOffset,annotationType,triggerFeatures,outputAnnotations);
									}

								} // if isModalityTrigger
								else if(match.getTriggerType().equals("ModalityTrigger"))
								{
									ModalityTrigger m= (ModalityTrigger)match;
									if(m.getPosType().equals("") == false)
									{
										//System.out.println(posTagOfToken);
										//System.out.println(tokenString);
										//System.out.println(m.getPosType());
										if(posTagOfToken.contains(m.getPosType()))

										{
											// don't have anything custom to put at this time ... 
											gate.FeatureMap triggerFeatures = Factory.newFeatureMap();
											triggerFeatures.put("String", tokenString);
											triggerFeatures.put("Type", m.getTriggerSubType());
											triggerFeatures.put("Source_Type", m.getSourceType());
											triggerFeatures.put("source", m.getSource());
											triggerFeatures.put("Prior_Polarity", m.getPriorPolarityVal());
											triggerFeatures.put("classType","ModalityTrigger");
											printToAnnotationSet(sOffset,fOffset,annotationType,triggerFeatures,outputAnnotations);
										}
									}
									// have no pos tag feature....
									else
									{
										// don't have anything custom to put at this time ... 
										gate.FeatureMap triggerFeatures = Factory.newFeatureMap();
										triggerFeatures.put("String", tokenString);
										// ModalityTrigger m= (ModalityTrigger)match;
										triggerFeatures.put("Type", m.getTriggerSubType());
										triggerFeatures.put("Source_Type", m.getSourceType());
										triggerFeatures.put("source", m.getSource());
										triggerFeatures.put("Prior_Polarity", m.getPriorPolarityVal());


										triggerFeatures.put("classType","ModalityTrigger");
										printToAnnotationSet(sOffset,fOffset,annotationType,triggerFeatures,outputAnnotations);

									}

								} // if isReportedSpeechVerbTrigger
								// 5th TRIGGER type: Just a Generic Trigger
								else if(match.getTriggerType().equals("GenericTrigger"))
								{

									// don't have anything custom to put at this time ... 
									gate.FeatureMap triggerFeatures = Factory.newFeatureMap();
									triggerFeatures.put("String", tokenString);
									triggerFeatures.put("classType","GenericTrigger");
									printToAnnotationSet(sOffset,fOffset,annotationType,triggerFeatures,outputAnnotations);
								}
								//case of genericTrigger
								//} // new if check
							}
						}// end if containsKey
					} // if HL ... 
				}//end for
			}//while
			br.close();
		}// try
		catch (Exception e)
		{//Catch exception if any
            System.out.println(token);
			e.printStackTrace();
		}
	}// end main (execute)
	/***************************************************************************
	 * method name: findPosTag()
	 * returns: the string represntation of the part_of _speech tag associated 
	 * with the token.
	 * function: this method will find the pos tag feature of a given token by 
	 * finding the matching constituent in the parse tree.           
	 *
	 ****************************************************************************/
	protected static String findPosTag (Annotation currentTokenToCheck, AnnotationSet syntaxTreeNodeAnnotationSet)
	{
		String posTag= null;
		for(Annotation mySyntaxNode: syntaxTreeNodeAnnotationSet)
		{
			if(mySyntaxNode.coextensive(currentTokenToCheck))
			{
				FeatureMap syntaxFeatureMap = mySyntaxNode.getFeatures();
				if(syntaxFeatureMap.containsKey("consists") == false)
				{
					posTag = mySyntaxNode.getFeatures().get("cat").toString().trim();
					return posTag;
				}
			}
		}
		return posTag;
	}
	/***************************************************************************
	 * method name: printToAnnotationSet()
	 * function: this method, given the required parameters will add the scope 
	 * annotations to the output annotation set in the required GATE format.     
	 *
	 ****************************************************************************/
	private static void printToAnnotationSet(long sOffset, long endOffset,String nameOfAnnotationSet,gate.FeatureMap annotationFeatures,AnnotationSet outputAnnotations)
	{
		try
		{
			outputAnnotations.add(sOffset,endOffset,nameOfAnnotationSet,annotationFeatures);
		}

		catch (InvalidOffsetException ioe) 
		{
			System.out.println ("Invalid Offset Exception caught: " + ioe);
			ioe.printStackTrace ();
		}

	}
	/********************************************************************************************/
	// have the token string - now we want the next token in the string::
	private static boolean checkIfMultiWordTrigger(Annotation tokenAnnotation,String tokenString,Map<String, List<GenericTrigger>> genTriggers,String annotationType,AnnotationSet tokenAnnotationSet,AnnotationSet outputAnnotations) 
	{

		if((tokenString.toLowerCase().equals("rather")) && (genTriggers.containsKey("rather than")))
		{
			long startOfNextToken= tokenAnnotation.getEndNode().getOffset()+1;
			//System.out.println("rather...");
			AnnotationSet nextTokenAnnSet = tokenAnnotationSet.get(startOfNextToken,startOfNextToken+4);
			for(Annotation nextToken: nextTokenAnnSet)
			{

				if(nextToken !=null)
				{
					if(nextToken.getFeatures().get("string").toString().trim().toLowerCase().equals("than"))
					{
						long sOffset =-1;
						long fOffset=-1;
						String tokenStringMulti = "rather than";
						List<GenericTrigger> matchedList=genTriggers.get(tokenStringMulti);
						// match is of type genTrigger
						GenericTrigger match = matchedList.get(0);
						sOffset = tokenAnnotation.getStartNode().getOffset();
						fOffset = nextToken.getEndNode().getOffset(); 
						Integer lastTokenInString = nextToken.getId();
						Integer firstTokenInString = tokenAnnotation.getId();
						annotateWithFeatures(match, tokenStringMulti, annotationType, sOffset,fOffset,outputAnnotations,lastTokenInString.toString(),firstTokenInString.toString());
						return true;
					}//than
				}//not null
			}//check nextTokenSet
		}// is this the trigger list that has multiwords & token == rather?


		if((tokenString.toLowerCase().equals("could")) && (genTriggers.containsKey("could not")))
		{
			long startOfNextToken= tokenAnnotation.getEndNode().getOffset()+1;
			//System.out.println("rather...");
			AnnotationSet nextTokenAnnSet = tokenAnnotationSet.get(startOfNextToken,startOfNextToken+3);
			for(Annotation nextToken: nextTokenAnnSet)
			{

				if(nextToken !=null)
				{
					if(nextToken.getFeatures().get("string").toString().trim().toLowerCase().equals("not"))
					{
						long sOffset =-1;
						long fOffset=-1;
						String tokenStringMulti = "could not";
						List<GenericTrigger> matchedList=genTriggers.get(tokenStringMulti);
						// match is of type genTrigger
						GenericTrigger match = matchedList.get(0);
						sOffset = tokenAnnotation.getStartNode().getOffset();
						fOffset = nextToken.getEndNode().getOffset();
						Integer lastTokenInString = nextToken.getId();
						Integer firstTokenInString = tokenAnnotation.getId();
						annotateWithFeatures(match, tokenStringMulti, annotationType, sOffset,fOffset,outputAnnotations,lastTokenInString.toString(),firstTokenInString.toString());
						return true;
					}//than
				}//not null
			}//check nextTokenSet
		}// is this the trigger list that has multiwords & token == could not?
		/********************************************************************************************/

		if((tokenString.toLowerCase().equals("no")) && (genTriggers.containsKey("no longer")))
		{
			long startOfNextToken= tokenAnnotation.getEndNode().getOffset()+1;
			//System.out.println("rather...");
			AnnotationSet nextTokenAnnSet = tokenAnnotationSet.get(startOfNextToken,startOfNextToken+6);
			for(Annotation nextToken: nextTokenAnnSet)
			{

				if(nextToken !=null)
				{
					if(nextToken.getFeatures().get("string").toString().trim().toLowerCase().equals("longer"))
					{
						long sOffset =-1;
						long fOffset=-1;
						String tokenStringMulti = "no longer";
						List<GenericTrigger> matchedList=genTriggers.get(tokenStringMulti);
						// match is of type genTrigger
						GenericTrigger match = matchedList.get(0);
						sOffset = tokenAnnotation.getStartNode().getOffset();
						fOffset = nextToken.getEndNode().getOffset();
						Integer lastTokenInString = nextToken.getId();
						Integer firstTokenInString = tokenAnnotation.getId();
						annotateWithFeatures(match, tokenStringMulti, annotationType, sOffset,fOffset,outputAnnotations,lastTokenInString.toString(),firstTokenInString.toString());
						return true;
					}//than
				}//not null
			}//check nextTokenSet
		}// is this the trigger list that has multiwords & token == rather?
		/********************************************************************************************/
		else if((tokenString.toLowerCase().equals("instead")) && (genTriggers.containsKey("instead of")))
		{
			long startOfNextToken= tokenAnnotation.getEndNode().getOffset()+1;
			//System.out.println("rather...");
			AnnotationSet nextTokenAnnSet = tokenAnnotationSet.get(startOfNextToken,startOfNextToken+2);
			for(Annotation nextToken: nextTokenAnnSet)
			{

				if(nextToken !=null)
				{
					if(nextToken.getFeatures().get("string").toString().trim().toLowerCase().equals("of"))
					{
						long sOffset =-1;
						long fOffset=-1;
						String tokenStringMulti = "instead of";
						List<GenericTrigger> matchedList=genTriggers.get(tokenStringMulti);
						// match is of type genTrigger
						GenericTrigger match = matchedList.get(0);
						sOffset = tokenAnnotation.getStartNode().getOffset();
						fOffset = nextToken.getEndNode().getOffset(); 
						Integer lastTokenInString = nextToken.getId();
						Integer firstTokenInString = tokenAnnotation.getId();
						annotateWithFeatures(match, tokenStringMulti, annotationType, sOffset,fOffset,outputAnnotations,lastTokenInString.toString(),firstTokenInString.toString());
						return true;
					}//of
				}//not null
			}//check nextTokenSet
		}// is this the trigger list that has multiwords & token == instead?
		/********************************************************************************************/
		else if((tokenString.toLowerCase().equals("turned")) && (genTriggers.containsKey("turned down")))
		{
			long startOfNextToken= tokenAnnotation.getEndNode().getOffset()+1;
			//System.out.println("rather...");
			AnnotationSet nextTokenAnnSet = tokenAnnotationSet.get(startOfNextToken,startOfNextToken+4);
			for(Annotation nextToken: nextTokenAnnSet)
			{

				if(nextToken !=null)
				{
					if(nextToken.getFeatures().get("string").toString().trim().toLowerCase().equals("down"))
					{
						long sOffset =-1;
						long fOffset=-1;
						String tokenStringMulti = "turned down";
						List<GenericTrigger> matchedList=genTriggers.get(tokenStringMulti);
						// match is of type genTrigger
						GenericTrigger match = matchedList.get(0);
						sOffset = tokenAnnotation.getStartNode().getOffset();
						fOffset = nextToken.getEndNode().getOffset(); 
						Integer lastTokenInString = nextToken.getId();
						Integer firstTokenInString = tokenAnnotation.getId();
						annotateWithFeatures(match, tokenStringMulti, annotationType, sOffset,fOffset,outputAnnotations,lastTokenInString.toString(),firstTokenInString.toString());
						return true;
					}//down
				}//not null
			}//check nextTokenSet
		}// is this the trigger list that has multiwords & token == rather?
		/********************************************************************************************/
		else if((tokenString.toLowerCase().equals("by")) && (genTriggers.containsKey("by no means")))
		{
			long startOfNextToken= tokenAnnotation.getEndNode().getOffset()+1;
			AnnotationSet nextTokenAnnSet = tokenAnnotationSet.get(startOfNextToken,startOfNextToken+2);
			for(Annotation nextToken: nextTokenAnnSet)
			{
				if(nextToken !=null)
				{
					if(nextToken.getFeatures().get("string").toString().trim().toLowerCase().equals("no"))
					{
						long startOfMeansToken= nextToken.getEndNode().getOffset()+1;
						AnnotationSet meansTokenAnnSet = tokenAnnotationSet.get(startOfMeansToken,startOfMeansToken+5);
						for(Annotation meansToken: meansTokenAnnSet)
						{
							if(meansToken !=null)
							{
								if(meansToken.getFeatures().get("string").toString().trim().toLowerCase().equals("means"))
								{
									long sOffset =-1;
									long fOffset=-1;
									String tokenStringMulti = "by no means";
									List<GenericTrigger> matchedList=genTriggers.get(tokenStringMulti);
									// match is of type genTrigger
									GenericTrigger match = matchedList.get(0);
									sOffset = tokenAnnotation.getStartNode().getOffset();
									fOffset = meansToken.getEndNode().getOffset();
									Integer lastTokenInString = meansToken.getId();
									Integer firstTokenInString = tokenAnnotation.getId();
									annotateWithFeatures(match, tokenStringMulti, annotationType, sOffset,fOffset,outputAnnotations, lastTokenInString.toString(),firstTokenInString.toString());
									return true;


								}
							}
						}


					}
				}
			}

		}
		/********************************************************************************************/
		else if((tokenString.toLowerCase().equals("with")) && (genTriggers.containsKey("with the exception of")))
		{
			long startOfNextToken= tokenAnnotation.getEndNode().getOffset()+1;
			AnnotationSet nextTokenAnnSet = tokenAnnotationSet.get(startOfNextToken,startOfNextToken+3);
			for(Annotation nextToken: nextTokenAnnSet)
			{
				if(nextToken !=null)
				{
					if(nextToken.getFeatures().get("string").toString().trim().toLowerCase().equals("the"))
					{
						long startOfExceptToken= nextToken.getEndNode().getOffset()+1;
						AnnotationSet exceptTokenAnnSet = tokenAnnotationSet.get(startOfExceptToken,startOfExceptToken+9);
						for(Annotation exceptToken: exceptTokenAnnSet)
						{
							if(exceptToken !=null)
							{ 
								if(exceptToken.getFeatures().get("string").toString().trim().toLowerCase().equals("exception"))
								{
									long startOfofToken= exceptToken.getEndNode().getOffset()+1;
									AnnotationSet ofTokenAnnSet = tokenAnnotationSet.get(startOfofToken,startOfofToken+2);
									for(Annotation ofToken: ofTokenAnnSet)
									{
										if(ofToken !=null)
										{ 
											if(ofToken.getFeatures().get("string").toString().trim().toLowerCase().equals("of"))
											{
												long sOffset =-1;
												long fOffset=-1;
												String tokenStringMulti = "with the exception of";
												List<GenericTrigger> matchedList=genTriggers.get(tokenStringMulti);
												// match is of type genTrigger
												GenericTrigger match = matchedList.get(0);
												sOffset = tokenAnnotation.getStartNode().getOffset();
												fOffset = ofToken.getEndNode().getOffset(); 
												Integer lastTokenInString = ofToken.getId();
												Integer firstTokenInString = tokenAnnotation.getId();
												annotateWithFeatures(match, tokenStringMulti, annotationType, sOffset,fOffset,outputAnnotations, lastTokenInString.toString(),firstTokenInString.toString());
												return true;

											}//of
										}//!null of token
									}// for ofToken
								}// if string is exception
							}// if exceptToken !null
						}// for
					}//string is the
				}//not null
			}//check nextTokenSet
		}// is this the trigger list that has multiwords & token == with?
		return false;

	}//end method
	/********************************************************************************************/
	private static void annotateWithFeatures(GenericTrigger match, String tokenStringMulti, String annotationType, long sOffset,long fOffset,AnnotationSet outputAnnotations,String lastTokenId,String firstTokenId)
	{
		gate.FeatureMap triggerFeatures = Factory.newFeatureMap();
		triggerFeatures.put("Ann_Type",annotationType);
		triggerFeatures.put("Type", match.getTriggerSubType());
		triggerFeatures.put("String", tokenStringMulti);
		triggerFeatures.put("classType","NegTrigger");
		triggerFeatures.put("ID_OfLastTokenForScope",lastTokenId);
		triggerFeatures.put("ID_OfFirstTokenForScope",firstTokenId);
		printToAnnotationSet(sOffset,fOffset,annotationType,triggerFeatures,outputAnnotations);
	}
	/********************************************************************************************/

} // class ExtractGenericTriggers
