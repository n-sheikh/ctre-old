/* CreateAnnotations.java
 * Authors: 
 * Date: Feb 2013
 * Purpose: This class implements the methods needed for creating the correct GATE Domain annotations.
 *
 *
 *
 */

package ca.concordia.clac;

import java.util.*;
import gate.*;
import gate.creole.*;
import gate.creole.metadata.*;
import gate.util.*;
import gate.annotation.*;
import gate.event.*;

public abstract class CreateAnnotations
{
    
    /***************************************************************************
     * method name: printToAnnotationSet()
     * function: this method, given the required parameters will add the scope 
     * annotations to the output annotation set in the required GATE format.     
     *
     ****************************************************************************/
	protected static void printToAnnotationSet(FeatureMap outFeatures,Annotation startToAnnotate, Annotation endToAnnotate, AnnotationSet outAnnotations, String AnnotationSetName, boolean changeOffset, Integer theNegID)
	{
        Long toPassNegID = Long.parseLong(String.valueOf(theNegID));
		if(changeOffset)
		{
			long sOffset =Long.parseLong(startToAnnotate.getFeatures().get("rootSoffset").toString().trim());
			long fOffset =Long.parseLong(endToAnnotate.getFeatures().get("rootEoffset").toString().trim());
			try
			{
				outAnnotations.add(sOffset,fOffset,AnnotationSetName,outFeatures);
                GenericScopeHelperClass.addtoAlreadyScopeAnnList(new Long(sOffset),new Long(fOffset), toPassNegID);
			}
			catch (InvalidOffsetException ioe) 
			{
				System.out.println ("Invalid Offset Exception caught: " + ioe);
				ioe.printStackTrace ();
			}
		}
		else
		{
			try
			{
				outAnnotations.add(startToAnnotate.getStartNode().getOffset(),endToAnnotate.getEndNode().getOffset(),AnnotationSetName,outFeatures);
                GenericScopeHelperClass.addtoAlreadyScopeAnnList(startToAnnotate.getStartNode().getOffset(),endToAnnotate.getEndNode().getOffset(), toPassNegID);
			}
			catch (InvalidOffsetException ioe) 
			{
				System.out.println ("Invalid Offset Exception caught: " + ioe);
				ioe.printStackTrace ();
			}
		}
		
		
	}
    /***************************************************************************
     * method name: annotateTheScopeForNonDepRule()
     * function: this method, given the required parameters will prepare the scope 
     * features for annotation in the case where it has been determined to not 
     * be reliant on dependnecy relations.
     *
     ****************************************************************************/
    protected static void annotateTheScopeForNonDepRule(Annotation currentToken, AnnotationSet syntaxTreeNodeAnnotationSet,AnnotationSet outputScopeAnnotations,Annotation triggerMarker,Annotation initTerm, String outputAnnotationType)
	{
        ArrayList<String> specificFeatures = new ArrayList<String>();
        specificFeatures.add("InitialTrigger");
        specificFeatures.add(initTerm.getFeatures().get("string").toString().trim());
        
        gate.FeatureMap outputFeaturesFinal = setTheFeaturesForAnnotation("Scope", outputAnnotationType, triggerMarker.getFeatures().get("String").toString().trim(), triggerMarker.getId(), currentToken.getFeatures().get("string").toString().trim(),specificFeatures);
        printToAnnotationSet(outputFeaturesFinal, currentToken,currentToken,outputScopeAnnotations, outputAnnotationType,false,triggerMarker.getId());
    }
    /***************************************************************************
     * method name: annotateTheScopeForASingleConstituent()
     * function: this method, given the required parameters will prepare the scope
     * features for annotation in the case where the negation trigger is a verb
     * - usually an implicit neg trigger. There are 2 different cases dealt with
     * in this method - the first one is if the originally determined scope needs
     * to be modified and the second not.
     *
     ****************************************************************************/
    protected static void annotateTheScopeForASingleConstituent(Annotation currentToken, AnnotationSet syntaxTreeNodeAnnotationSet,AnnotationSet outputScopeAnnotations,Annotation triggerMarker,String constituentClue,Annotation initTerm, String outputAnnotationType,int choice,AnnotationSet tokenAnnotationSetInSentence,Annotation initTrigger)
	{
        int syntaxNodeID = InvestigateConstituentsFromParseTree.findSyntaxNode (currentToken,syntaxTreeNodeAnnotationSet);
        int headID = InvestigateConstituentsFromParseTree.findTheConstInParseTree(syntaxNodeID,syntaxTreeNodeAnnotationSet,constituentClue);
        String negType =null;
        String classType =null;
        if(triggerMarker.getFeatures().get("classType")!=null)
        {
            classType = triggerMarker.getFeatures().get("classType").toString().trim();
            
        }
        else
        {
            classType = "ModalityTrigger";
        }

        
        if(triggerMarker.getFeatures().get("Type")!=null)
        {
            negType = triggerMarker.getFeatures().get("Type").toString().trim();
        }
        else
        {
            negType ="MODAL_GENERIC";
        }

        
        
        if ((headID !=-1))
        {
            ArrayList <Integer> idsOfConstituentsInScope=InvestigateConstituentsFromParseTree.changeSpanForSingleConst(triggerMarker,headID,syntaxTreeNodeAnnotationSet,choice);
            
            // new try our contrast thing-a-me
            idsOfConstituentsInScope = InvestigateConstituentsFromParseTree.changeSpanForPunctuation(idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet,tokenAnnotationSetInSentence,classType);
            
            ArrayList returnedListReadyForAnnotation=prepareScopeToAnnotate(syntaxTreeNodeAnnotationSet,idsOfConstituentsInScope);
            if (returnedListReadyForAnnotation.size()!=0)
            {
                String finalS =(String)returnedListReadyForAnnotation.get(0);
                Annotation startOffset =(Annotation)returnedListReadyForAnnotation.get(1);
                Annotation endOffset = (Annotation)returnedListReadyForAnnotation.get(2);
                ArrayList<String> specificFeatures = new ArrayList<String>();
                specificFeatures.add("InitialTrigger");
                // convert to string
                String textToAnn = "";
                // get the type of the of the marker
                if(choice ==0)
                {
                    
                    textToAnn = triggerMarker.getFeatures().get("String").toString().trim();
                }
                else if (choice ==1)
                {
                    textToAnn =triggerMarker.getFeatures().get("string").toString().trim();
                }
                specificFeatures.add(initTrigger.getFeatures().get("string").toString().trim());
                gate.FeatureMap outputFeaturesFinal = setTheFeaturesForAnnotation("Scope", outputAnnotationType, textToAnn, triggerMarker.getId(), finalS,specificFeatures);
                printToAnnotationSet(outputFeaturesFinal, startOffset, endOffset, outputScopeAnnotations, outputAnnotationType,false,triggerMarker.getId());
            } // returnedList!=null
        }// if heasID!=-1
    }
     /***************************************************************************
     * method name: annotateTheScopeForASingleConstituentNSUBJ()
     * function: this method, given the required parameters will prepare the scope 
     * features for annotation in the case where the negation trigger is a verb 
     * - usually an implicit neg trigger. There are 2 different cases dealt with 
     * in this method - the first one is if the originally determined scope needs 
     * to be modified and the second not.
     *
     ****************************************************************************/	
    protected static void annotateTheScopeForASingleConstituentNSUBJ(Annotation currentToken, AnnotationSet syntaxTreeNodeAnnotationSet,AnnotationSet outputScopeAnnotations,Annotation triggerMarker,String constituentClue,Annotation initTerm, String outputAnnotationType,int choice,AnnotationSet tokenAnnotationSetInSentence,Annotation initTrigger)
	{
        int syntaxNodeID = InvestigateConstituentsFromParseTree.findSyntaxNode (currentToken,syntaxTreeNodeAnnotationSet);
        //int headID = InvestigateConstituentsFromParseTree.findTheConstInParseTree(syntaxNodeID,syntaxTreeNodeAnnotationSet,constituentClue);
        String negType =null;
        String classType =null;
        if(triggerMarker.getFeatures().get("classType")!=null)
        {
            classType = triggerMarker.getFeatures().get("classType").toString().trim();
            
        }
        
        if(triggerMarker.getFeatures().get("Type")!=null)
        {
            negType = triggerMarker.getFeatures().get("Type").toString().trim();
        }

        
        
        if ((syntaxNodeID !=-1))
        {
                //ArrayList <Integer> idsOfConstituentsInScope=InvestigateConstituentsFromParseTree.changeSpanForSingleConst(triggerMarker,headID,syntaxTreeNodeAnnotationSet,choice);
            
                // new try our contrast thing-a-me
                //idsOfConstituentsInScope = InvestigateConstituentsFromParseTree.changeSpanForPunctuation(idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet,tokenAnnotationSetInSentence,classType);
                ArrayList<Integer>idsOfConstituentsInScope= new ArrayList<Integer>();
                idsOfConstituentsInScope.add(syntaxNodeID);
                ArrayList returnedListReadyForAnnotation=prepareScopeToAnnotate(syntaxTreeNodeAnnotationSet,idsOfConstituentsInScope);
                if (returnedListReadyForAnnotation.size()!=0)
                {
                    String finalS =(String)returnedListReadyForAnnotation.get(0);
                    Annotation startOffset =(Annotation)returnedListReadyForAnnotation.get(1);
                    Annotation endOffset = (Annotation)returnedListReadyForAnnotation.get(2);
                    ArrayList<String> specificFeatures = new ArrayList<String>();
                    specificFeatures.add("InitialTrigger");
                    // convert to string
                    String textToAnn = "";
                    // get the type of the of the marker
                    if(choice ==0)
                    {
                        
                        textToAnn = triggerMarker.getFeatures().get("String").toString().trim();
                    }
                    else if (choice ==1) 
                    {
                        textToAnn =triggerMarker.getFeatures().get("string").toString().trim();
                    }
                    specificFeatures.add(initTrigger.getFeatures().get("string").toString().trim());
                    gate.FeatureMap outputFeaturesFinal = setTheFeaturesForAnnotation("Scope", outputAnnotationType, textToAnn, triggerMarker.getId(), finalS,specificFeatures);
                    printToAnnotationSet(outputFeaturesFinal, startOffset, endOffset, outputScopeAnnotations, outputAnnotationType,false,triggerMarker.getId());
                } // returnedList!=null
            }// if heasID!=-1
    }
    /***************************************************************************
     * NEW(Feb 2nd::: method name: annotateTheScopeForMultiCase()
     * function: 
     *
     ****************************************************************************/	
    protected static void annotateTheScopeForMultiCase(Annotation currentToken, AnnotationSet syntaxTreeNodeAnnotationSet,AnnotationSet outputScopeAnnotations,Annotation triggerMarker,Annotation initTerm, String outputAnnotationType    )
	{
        int syntaxNodeID = InvestigateConstituentsFromParseTree.findSyntaxNode (currentToken,syntaxTreeNodeAnnotationSet);
        // find immediate parent
        int headID = InvestigateConstituentsFromParseTree.findTheParent(syntaxNodeID,syntaxTreeNodeAnnotationSet);
        String negType =null;
        if(triggerMarker.getFeatures().get("Type")!=null)
        {
            negType = triggerMarker.getFeatures().get("Type").toString().trim();
        }
        
        
        if ((headID !=-1))
        {
                Annotation headTerm = syntaxTreeNodeAnnotationSet.get((int)headID);
                ArrayList<String> specificFeatures = new ArrayList<String>();
                specificFeatures.add("Neg_Trigger");
                specificFeatures.add(triggerMarker.getFeatures().get("String").toString().trim());
                specificFeatures.add("TriggerID");
                specificFeatures.add(triggerMarker.getId().toString());
                specificFeatures.add("InitialTrigger");
                // convert to string
                specificFeatures.add(initTerm.getFeatures().get("string").toString().trim());
                gate.FeatureMap outputFeaturesFinal = setTheFeaturesForAnnotation("Scope", outputAnnotationType, currentToken.getFeatures().get("string").toString().trim(), currentToken.getId(),headTerm.getFeatures().get("text").toString().trim(),specificFeatures);
                printToAnnotationSet(outputFeaturesFinal, headTerm,headTerm, outputScopeAnnotations, outputAnnotationType,false,triggerMarker.getId());
                
        }// if heasID!=-1
    }

     /***************************************************************************
     * method name: annotateTheScopeForNeither()
     * function: this method, given the required parameters will prepare the scope 
     * features for annotation in the case where the found negation pattern is of 
     * the form "neither".  
     *
     ****************************************************************************/
    protected static void annotateTheScopeForNeither(Annotation neitherAsToken,AnnotationSet syntaxTreeNodeAnnotationSet,AnnotationSet outputScopeAnnotations,String outputAnnotationType,Annotation theNegTriggerForNeither,AnnotationSet tokenAnnotationSetInSentence)
    {
        int syntaxNodeIDOfNeither = InvestigateConstituentsFromParseTree.findSyntaxNode (neitherAsToken,syntaxTreeNodeAnnotationSet);
        int headID = InvestigateConstituentsFromParseTree.findTheConstInParseTree(syntaxNodeIDOfNeither,syntaxTreeNodeAnnotationSet,"S");
        if(headID ==-1)
        {
             headID = InvestigateConstituentsFromParseTree.findTheConstInParseTree(syntaxNodeIDOfNeither,syntaxTreeNodeAnnotationSet,"SINV");
            if(headID ==-1)
            {
                headID = InvestigateConstituentsFromParseTree.findTheConstInParseTree(syntaxNodeIDOfNeither,syntaxTreeNodeAnnotationSet,"SQ");
                if(headID ==-1)
                {
                    headID = InvestigateConstituentsFromParseTree.findTheConstInParseTree(syntaxNodeIDOfNeither,syntaxTreeNodeAnnotationSet,"NP"); 
                    if(headID ==-1)
                    {
                        headID = InvestigateConstituentsFromParseTree.findTheConstInParseTree(syntaxNodeIDOfNeither,syntaxTreeNodeAnnotationSet,"PP"); 
                    }
                }
            }
        }
        if ((headID !=-1))
        {
            ArrayList <Integer> idsOfConstituentsInScope =InvestigateConstituentsFromParseTree.changeSpanForDepRules(syntaxNodeIDOfNeither,theNegTriggerForNeither,headID,syntaxTreeNodeAnnotationSet);
            // new June 30th
            if(neitherAsToken.getFeatures().get("string").toString().trim().equals("nor"))
            {
                
                idsOfConstituentsInScope = InvestigateConstituentsFromParseTree.changeSpanForNor(idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet,tokenAnnotationSetInSentence,"NegTrigger");
            }
            else
            {
               idsOfConstituentsInScope = InvestigateConstituentsFromParseTree.changeSpanForPunctuation(idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet,tokenAnnotationSetInSentence,"NegTrigger");
            }
            ArrayList returnedListReadyForAnnotation = prepareScopeToAnnotate(syntaxTreeNodeAnnotationSet,idsOfConstituentsInScope);
            if (returnedListReadyForAnnotation.size()!=0)
            {
                String finalS =(String)returnedListReadyForAnnotation.get(0);
                Annotation startOffset =(Annotation)returnedListReadyForAnnotation.get(1);
                Annotation endOffset = (Annotation)returnedListReadyForAnnotation.get(2);
                
                ArrayList<String> specificFeatures = new ArrayList<String>();
                specificFeatures.add("InitialTrigger");
                // convert to string
                specificFeatures.add(neitherAsToken.getFeatures().get("string").toString().trim());
                gate.FeatureMap outputFeaturesFinal = setTheFeaturesForAnnotation("Scope", outputAnnotationType,theNegTriggerForNeither.getFeatures().get("String").toString().trim(), theNegTriggerForNeither.getId(),finalS,specificFeatures);
                printToAnnotationSet(outputFeaturesFinal, startOffset, endOffset, outputScopeAnnotations, outputAnnotationType,false, theNegTriggerForNeither.getId());
                
            }// returnedList!=null
        }// head!=-1

        
    }
    /***************************************************************************
     * method name: annotateTheScopeForDeps()
     * function: this method, given the required parameters will prepare the scope 
     * features for annotation in the case where the found scope + neg trigger is 
     * reliant on one of the dependency relation cases.  
     *
     ****************************************************************************/	
    protected static void annotateTheScopeForDeps(Annotation currentToken, AnnotationSet syntaxTreeNodeAnnotationSet,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc,AnnotationSet outputScopeAnnotations,Annotation triggerMarker,Annotation initTerm, String outputAnnotationType, AnnotationSet tokenAnnotationSetInSentence, AnnotationSet depRelationsSetOfSentence,int initialTriggerChoice,Annotation originalInitialTermForScopeAnn)
	{
        
        ArrayList<Integer> parentListOfNegTrigger = tokenPathsOfDoc.get(currentToken.getId());
        ArrayList<Integer> parentListOfInitTrigger =tokenPathsOfDoc.get(initTerm.getId());
        int syntaxNodeIDOfNegTrigger =  parentListOfNegTrigger.get(0).intValue();
        if(parentListOfInitTrigger.get(0)!=null)
        {
            int syntaxNodeIDOfInitTrigger =  parentListOfInitTrigger.get(0).intValue();
            int headID = InvestigateConstituentsFromParseTree.findTheCommonParentConstInParseTree(parentListOfNegTrigger,parentListOfInitTrigger);
            String negType = null;
            String classType =null;
            if(triggerMarker.getFeatures().get("classType")!=null)
            {
                classType = triggerMarker.getFeatures().get("classType").toString().trim();
                
            }
            else
            {
                classType = "ModalityTrigger";
            }
            
            if(triggerMarker.getFeatures().get("Type")!=null)
            {
                negType = triggerMarker.getFeatures().get("Type").toString().trim();
            }
            else
            {
                negType ="MODAL_GENERIC";
            }
            
            String triggerString =null;
            if(triggerMarker.getFeatures().get("String")!=null)
            {
                triggerString = triggerMarker.getFeatures().get("String").toString().trim();
            }
            else
            {
                triggerString = triggerMarker.getFeatures().get("string").toString().trim();
            }
            
            if (headID !=-1)
            {
                int newHeadID = -1;
                Annotation syntaxNodeConstHelp = syntaxTreeNodeAnnotationSet.get(headID); 
                if(syntaxNodeConstHelp.getFeatures().get("cat").toString().trim().equals("CONJP") || syntaxNodeConstHelp.getFeatures().get("cat").toString().trim().equals("ADJP") || syntaxNodeConstHelp.getFeatures().get("cat").toString().trim().equals("ADVP"))
                {
                    // just find the next parent ....
                    newHeadID = InvestigateConstituentsFromParseTree.findTheParent(headID,syntaxTreeNodeAnnotationSet);
                    /****************************************************************************************************/
                }
                else
                {
                    newHeadID = headID;
                }
                    if(newHeadID!=-1)
                    {
                        ArrayList <Integer> idsOfConstituentsInScope =new ArrayList<Integer>();
                        Annotation testHeadAnn = syntaxTreeNodeAnnotationSet.get(newHeadID);
                        idsOfConstituentsInScope.add(newHeadID);
                        boolean doSBAR=true;
                         if(triggerString.toLowerCase().equals("neither")== true)
                         {
                             for(Annotation aToken:tokenAnnotationSetInSentence)
                             {
                                 if(aToken.getFeatures().get("string").toString().trim().toLowerCase().equals("nor"))
                                 {
                                     doSBAR=false;
                                     break;
                                 }
                             }
                         }
                        else if(triggerString.toLowerCase().equals("nor")== true)
                        {
                            for(Annotation aToken:tokenAnnotationSetInSentence)
                            {
                                if(aToken.getFeatures().get("string").toString().trim().toLowerCase().equals("neither"))
                                {
                                    doSBAR=false;
                                    break;
                                }
                            }
                            
                        }
                        // new condition
                        if (doSBAR==true)
                        {
                            
                         idsOfConstituentsInScope =InvestigateConstituentsFromParseTree.changeSpanForCommaN(idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet,tokenAnnotationSetInSentence,triggerMarker);
                        }
                         
                        idsOfConstituentsInScope =InvestigateConstituentsFromParseTree.changeSpanForDepRulesWithList(syntaxNodeIDOfNegTrigger,triggerMarker,idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet);
                      if(triggerString.toLowerCase().equals("neither")||triggerString.toLowerCase().equals("nor")||triggerString.toLowerCase().equals("not")||triggerString.toLowerCase().equals("n't"))
                      {
                          idsOfConstituentsInScope = InvestigateConstituentsFromParseTree.changeSpanForNor(idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet,tokenAnnotationSetInSentence,classType);
                          
                      }
                      else
                      {
                      idsOfConstituentsInScope = InvestigateConstituentsFromParseTree.changeSpanForPunctuation(idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet,tokenAnnotationSetInSentence,classType);
                      }
                        ArrayList returnedListReadyForAnnotation = prepareScopeToAnnotate(syntaxTreeNodeAnnotationSet,idsOfConstituentsInScope);
                        
                        if (returnedListReadyForAnnotation.size()!=0)
                        {
                            String finalS =(String)returnedListReadyForAnnotation.get(0);
                            Annotation startOffset =(Annotation)returnedListReadyForAnnotation.get(1);
                            Annotation endOffset = (Annotation)returnedListReadyForAnnotation.get(2);
                            ArrayList<Object> specificFeatures = new ArrayList<Object>();
                            specificFeatures.add("InitialTrigger");
                            if(initialTriggerChoice ==0)
                            {
                                if(originalInitialTermForScopeAnn!=null)
                                {
                                    specificFeatures.add(originalInitialTermForScopeAnn.getFeatures().get("string").toString().trim());
                                }
                                else
                                {
                                    specificFeatures.add(initTerm.getFeatures().get("string").toString().trim());
                                }
                            }
                            else
                            {
                                specificFeatures.add(currentToken.getFeatures().get("string").toString().trim());
                            }
                            specificFeatures.add("The_Original_Constituent_ID");
                            specificFeatures.add(Integer.toString(newHeadID));
                            specificFeatures.add("Constituent ID List");
                            specificFeatures.add(idsOfConstituentsInScope);
                            if(triggerString.toLowerCase().equals("neither"))
                            {
                                // find if there is a "nor"
                                for(Annotation tokenN: tokenAnnotationSetInSentence)
                                {
                                    if(tokenN.getFeatures().get("string").toString().trim().toLowerCase().equals("nor"))
                                    {
                                        
                                        specificFeatures.add("nor_connective_tokenID");
                                        specificFeatures.add(tokenN.getId());
                                        break;
                                    }
                                        
                                }
                            }
                            if(triggerString.toLowerCase().equals("nor"))
                            {
                                // find if there is a "nor"
                                for(Annotation tokenN: tokenAnnotationSetInSentence)
                                {
                                    if(tokenN.getFeatures().get("string").toString().trim().toLowerCase().equals("neither"))
                                    {
                                        specificFeatures.add("neither_connective_tokenID");
                                        specificFeatures.add(tokenN.getId());
                                        break;
                                    }
                                    
                                }
                            }

                            gate.FeatureMap outputFeaturesFinal = setTheFeaturesForAnnotationNew("Scope", outputAnnotationType, triggerString, triggerMarker.getId(), finalS,specificFeatures);
                            printToAnnotationSet(outputFeaturesFinal, startOffset, endOffset, outputScopeAnnotations, outputAnnotationType,false,triggerMarker.getId());
                        } // returnedList!=null

                    } //newHeadID!=-1
                } // head!=-1
            }// parent check
        
        }//end method
    /***************************************************************************
     * method name: annotateTheScopeForDepsSpecialCase()
     * function: this method, given the required parameters will prepare the scope
     * features for annotation in the case where the found scope + neg trigger is
     * reliant on one of the dependency relation cases.
     *
     ****************************************************************************/
    protected static void annotateTheScopeForDepsSpecialCase(Annotation currentToken, AnnotationSet syntaxTreeNodeAnnotationSet,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc,AnnotationSet outputScopeAnnotations,Annotation triggerMarker,Annotation initTerm, String outputAnnotationType, AnnotationSet tokenAnnotationSetInSentence, AnnotationSet depRelationsSetOfSentence,int initialTriggerChoice,Annotation originalInitialTermForScopeAnn)
	{
        ArrayList<Integer> parentListOfNegTrigger = tokenPathsOfDoc.get(currentToken.getId());
        ArrayList<Integer> parentListOfInitTrigger =tokenPathsOfDoc.get(initTerm.getId());
        int syntaxNodeIDOfNegTrigger =  parentListOfNegTrigger.get(0).intValue();
        if(parentListOfInitTrigger.get(0)!=null)
        {
            int syntaxNodeIDOfInitTrigger =  parentListOfInitTrigger.get(0).intValue();
            int headID = InvestigateConstituentsFromParseTree.findTheCommonParentConstInParseTree(parentListOfNegTrigger,parentListOfInitTrigger);
            String negType = null;
            String classType =null;
            if(triggerMarker.getFeatures().get("classType")!=null)
            {
                classType = triggerMarker.getFeatures().get("classType").toString().trim();
                
            }
            
            if(triggerMarker.getFeatures().get("Type")!=null)
            {
                negType = triggerMarker.getFeatures().get("Type").toString().trim();
            }
            String triggerString =null;
            if(triggerMarker.getFeatures().get("String")!=null)
            {
                triggerString = triggerMarker.getFeatures().get("String").toString().trim();
            }
            else
            {
                triggerString = triggerMarker.getFeatures().get("string").toString().trim();
            }
           
            
            if (headID !=-1)
            {
                    ArrayList <Integer> idsOfConstituentsInScope =new ArrayList<Integer>();
                    Annotation testHeadAnn = syntaxTreeNodeAnnotationSet.get(headID);
                    idsOfConstituentsInScope.add(headID);
                    
                    idsOfConstituentsInScope =InvestigateConstituentsFromParseTree.changeSpanForCommaN(idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet,tokenAnnotationSetInSentence,triggerMarker);
                    
                    //idsOfConstituentsInScope =InvestigateConstituentsFromParseTree.changeSpanForDepRulesWithList(syntaxNodeIDOfNegTrigger,triggerMarker,idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet);
                    
                    idsOfConstituentsInScope = InvestigateConstituentsFromParseTree.changeSpanForPunctuation(idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet,tokenAnnotationSetInSentence,classType);
                    ArrayList returnedListReadyForAnnotation = prepareScopeToAnnotate(syntaxTreeNodeAnnotationSet,idsOfConstituentsInScope);
                    
                    if (returnedListReadyForAnnotation.size()!=0)
                    {
                        String finalS =(String)returnedListReadyForAnnotation.get(0);
                        Annotation startOffset =(Annotation)returnedListReadyForAnnotation.get(1);
                        Annotation endOffset = (Annotation)returnedListReadyForAnnotation.get(2);
                        ArrayList<Object> specificFeatures = new ArrayList<Object>();
                        specificFeatures.add("InitialTrigger");
                        if(initialTriggerChoice ==0)
                        {
                            if(originalInitialTermForScopeAnn!=null)
                            {
                                specificFeatures.add(originalInitialTermForScopeAnn.getFeatures().get("string").toString().trim());
                            }
                            else
                            {
                                specificFeatures.add(initTerm.getFeatures().get("string").toString().trim());
                            }
                        }
                        else
                        {
                            specificFeatures.add(currentToken.getFeatures().get("string").toString().trim());
                        }
                        specificFeatures.add("The_Original_Constituent_ID");
                        specificFeatures.add(Integer.toString(headID));
                        specificFeatures.add("Constituent ID List");
                        specificFeatures.add(idsOfConstituentsInScope);
                        gate.FeatureMap outputFeaturesFinal = setTheFeaturesForAnnotationNew("Scope", outputAnnotationType, triggerString, triggerMarker.getId(), finalS,specificFeatures);
                        printToAnnotationSet(outputFeaturesFinal, startOffset, endOffset, outputScopeAnnotations, outputAnnotationType,false,triggerMarker.getId());
                    } // returnedList!=null

            } // head!=-1
        }// parent check
    }//end method

        /***************************************************************************
     * method name: annotateTheScopeForDepsWholeClause:::()
     * function: this method, given the required parameters will prepare the scope
     * features for annotation in the case where the found scope + neg trigger is
     * reliant on one of the dependency relation cases.
     *
     ****************************************************************************/
    protected static void annotateTheScopeForDepsWholeClause(Annotation currentToken, AnnotationSet syntaxTreeNodeAnnotationSet,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc,AnnotationSet outputScopeAnnotations,Annotation triggerMarker,Annotation initTerm, String outputAnnotationType, AnnotationSet tokenAnnotationSetInSentence, AnnotationSet depRelationsSetOfSentence,int initialTriggerChoice,Annotation originalInitialTermForScopeAnn)
	{
        ArrayList<Integer> parentListOfNegTrigger = tokenPathsOfDoc.get(currentToken.getId());
        ArrayList<Integer> parentListOfInitTrigger =tokenPathsOfDoc.get(initTerm.getId());
        int syntaxNodeIDOfNegTrigger =  parentListOfNegTrigger.get(0).intValue();
        if(parentListOfInitTrigger.get(0)!=null)
        {
            int syntaxNodeIDOfInitTrigger =  parentListOfInitTrigger.get(0).intValue();
            int headID = InvestigateConstituentsFromParseTree.findTheCommonParentConstInParseTree(parentListOfNegTrigger,parentListOfInitTrigger);
            String negType = null;
            String classType =null;
            if(triggerMarker.getFeatures().get("classType")!=null)
            {
                classType = triggerMarker.getFeatures().get("classType").toString().trim();
                
            }
            
            if(triggerMarker.getFeatures().get("Type")!=null)
            {
                negType = triggerMarker.getFeatures().get("Type").toString().trim();
            }
            String triggerString =null;
            if(triggerMarker.getFeatures().get("String")!=null)
            {
                triggerString = triggerMarker.getFeatures().get("String").toString().trim();
            }
            else
            {
                triggerString = triggerMarker.getFeatures().get("string").toString().trim();
            }
            
            if (headID !=-1)
            {
                int newHeadID = -1;
                Annotation syntaxNodeConstHelp = syntaxTreeNodeAnnotationSet.get(headID);
                if(syntaxNodeConstHelp.getFeatures().get("cat").toString().trim().equals("CONJP") || syntaxNodeConstHelp.getFeatures().get("cat").toString().trim().equals("ADJP") || syntaxNodeConstHelp.getFeatures().get("cat").toString().trim().equals("ADVP"))
                {
                    // just find the next parent ....
                    newHeadID = InvestigateConstituentsFromParseTree.findTheParent(headID,syntaxTreeNodeAnnotationSet);
                    /****************************************************************************************************/
                }
                else
                {
                    newHeadID = headID;
                }
                if(newHeadID!=-1)
                {
                    ArrayList <Integer> idsOfConstituentsInScope =new ArrayList<Integer>();
                    Annotation testHeadAnn = syntaxTreeNodeAnnotationSet.get(newHeadID);
                    idsOfConstituentsInScope.add(newHeadID);
                    idsOfConstituentsInScope =InvestigateConstituentsFromParseTree.changeSpanForDepRulesWithList(syntaxNodeIDOfNegTrigger,triggerMarker,idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet);
                   
                    idsOfConstituentsInScope = InvestigateConstituentsFromParseTree.changeSpanForPunctuation(idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet,tokenAnnotationSetInSentence,classType);
                    ArrayList returnedListReadyForAnnotation = prepareScopeToAnnotate(syntaxTreeNodeAnnotationSet,idsOfConstituentsInScope);
                    if (returnedListReadyForAnnotation.size()!=0)
                    {
                        String finalS =(String)returnedListReadyForAnnotation.get(0);
                        Annotation startOffset =(Annotation)returnedListReadyForAnnotation.get(1);
                        Annotation endOffset = (Annotation)returnedListReadyForAnnotation.get(2);
                        ArrayList<Object> specificFeatures = new ArrayList<Object>();
                        specificFeatures.add("InitialTrigger");
                        if(initialTriggerChoice ==0)
                        {
                            if(originalInitialTermForScopeAnn!=null)
                            {
                                specificFeatures.add(originalInitialTermForScopeAnn.getFeatures().get("string").toString().trim());
                            }
                            else
                            {
                                specificFeatures.add(initTerm.getFeatures().get("string").toString().trim());
                            }
                        }
                        else
                        {
                            specificFeatures.add(currentToken.getFeatures().get("string").toString().trim());
                        }
                        specificFeatures.add("The_Original_Constituent_ID");
                        specificFeatures.add(Integer.toString(newHeadID));
                        specificFeatures.add("Constituent ID List");
                        specificFeatures.add(idsOfConstituentsInScope);
                        gate.FeatureMap outputFeaturesFinal = setTheFeaturesForAnnotationNew("Scope", outputAnnotationType, triggerString, triggerMarker.getId(), finalS,specificFeatures);
                        printToAnnotationSet(outputFeaturesFinal, startOffset, endOffset, outputScopeAnnotations, outputAnnotationType,false,triggerMarker.getId());
                    } // returnedList!=null
                } //newHeadID!=-1
            } // head!=-1
        }// parent check
    }//end method

        /***************************************************************************
     * method name: annotateTheScopeForDepsLeft()
     * function: this method, given the required parameters will prepare the scope 
     * features for annotation in the case where the found scope + neg trigger is 
     * reliant on one of the dependency relation cases. 
     * This is a case for when scope is on left hand side. (i.e " I saw nothing")
     *
     ****************************************************************************/	
    protected static void annotateTheScopeForDepsLeft(Annotation currentToken, AnnotationSet syntaxTreeNodeAnnotationSet,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc,AnnotationSet outputScopeAnnotations,Annotation triggerMarker,Annotation initTerm, String outputAnnotationType, AnnotationSet tokenAnnotationSetInSentence)
	{
        ArrayList<Integer> parentListOfNegTrigger = tokenPathsOfDoc.get(currentToken.getId());
        ArrayList<Integer> parentListOfInitTrigger =tokenPathsOfDoc.get(initTerm.getId());
        int syntaxNodeIDOfNegTrigger =  parentListOfNegTrigger.get(0).intValue();
        if(parentListOfInitTrigger.get(0)!=null)
        {
            int syntaxNodeIDOfInitTrigger =  parentListOfInitTrigger.get(0).intValue();
            int headID = InvestigateConstituentsFromParseTree.findTheCommonParentConstInParseTree(parentListOfNegTrigger,parentListOfInitTrigger);
            String negType =null;
            String classType =null;
            if(triggerMarker.getFeatures().get("classType")!=null)
            {
                classType = triggerMarker.getFeatures().get("classType").toString().trim();
                
            }
            
            if(triggerMarker.getFeatures().get("Type")!=null)
            {
                negType = triggerMarker.getFeatures().get("Type").toString().trim();
            }
            String triggerString = triggerMarker.getFeatures().get("String").toString().trim();
            boolean notDone =false;
            if (headID !=-1)
            { 
                int newHeadID = -1;
                Annotation syntaxNodeConstHelp = syntaxTreeNodeAnnotationSet.get(headID); 
               
                 ArrayList <Integer> idsOfConstituentsInScope =InvestigateConstituentsFromParseTree.changeSpanForLeftSpanScope(syntaxNodeIDOfNegTrigger,triggerMarker,headID,syntaxTreeNodeAnnotationSet);
                idsOfConstituentsInScope = InvestigateConstituentsFromParseTree.changeSpanForPunctuation(idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet,tokenAnnotationSetInSentence,classType);
                    
                    ArrayList returnedListReadyForAnnotation = prepareScopeToAnnotate(syntaxTreeNodeAnnotationSet,idsOfConstituentsInScope);
                    if (returnedListReadyForAnnotation.size()!=0)
                    {
                        String finalS =(String)returnedListReadyForAnnotation.get(0);
                        Annotation startOffset =(Annotation)returnedListReadyForAnnotation.get(1);
                        Annotation endOffset = (Annotation)returnedListReadyForAnnotation.get(2);
                        ArrayList<Object> specificFeatures = new ArrayList<Object>();
                        specificFeatures.add("InitialTrigger");
                        // convert to string
                        specificFeatures.add(initTerm.getFeatures().get("string").toString().trim());
                        specificFeatures.add("The_Original_Constituent_ID");
                        specificFeatures.add(Integer.toString(headID));
                        specificFeatures.add("Constituent ID List");
                        specificFeatures.add(idsOfConstituentsInScope);
                        gate.FeatureMap outputFeaturesFinal = setTheFeaturesForAnnotationNew("Scope", outputAnnotationType,triggerString, triggerMarker.getId(),finalS,specificFeatures);
                        printToAnnotationSet(outputFeaturesFinal, startOffset, endOffset, outputScopeAnnotations, outputAnnotationType,false,triggerMarker.getId());
                    } // returnedList!=null
            } // head!=-1
        }// parent check
    }//end method
    /***************************************************************************
     * method name: prepareScopeToAnnotate()
     * function: this helper method, given the required parameters will ensure the 
     * correct span for the scope annotation, as well as the start and end offsets.      
     *
     ****************************************************************************/
    private static ArrayList prepareScopeToAnnotate(AnnotationSet syntaxTreeNodeAnnotationSet,ArrayList <Integer> idsOfConstituentsInScope)
    {
        ArrayList returnedListForAnnotation = new ArrayList();
        if(idsOfConstituentsInScope.size()!=0)
        {
            String finalS =null;
            Annotation startOffset =null;
            Annotation endOffset =null;
            for(int m=0; m< idsOfConstituentsInScope.size(); m++)
            {
                Annotation scopeM = syntaxTreeNodeAnnotationSet.get((int)idsOfConstituentsInScope.get(m));
                String temp = scopeM.getFeatures().get("text").toString().trim();
                if(m ==0)
                {
                    startOffset = scopeM;
                    finalS = temp;	 
                }
                else
                {
                    finalS = finalS + " "+temp;
                }
                
                if(m== idsOfConstituentsInScope.size()-1)
                {
                    endOffset = scopeM;
                }
                
            }//for
        returnedListForAnnotation.add(finalS);
        returnedListForAnnotation.add(startOffset);
        returnedListForAnnotation.add(endOffset);

 
        }//if
    return returnedListForAnnotation;
   }
        
    /***************************************************************************
     * method name:setTheFeaturesForAnnotation()
     * function: this helper method, given the required parameters will set the features 
     * for the scope annotation - first all of the generic features as well any 
     * specific ones that have been passed to the method accordingly.    
     *
     ****************************************************************************/
    private static gate.FeatureMap setTheFeaturesForAnnotation(String annType, String type, String inputTrigger, Integer idOfInputTrigger, String textSpan,ArrayList<String> featuresToAdd)
    {
        gate.FeatureMap outputFeatures = Factory.newFeatureMap();
        outputFeatures.put("Ann_type",annType);
        outputFeatures.put("Type",type);
        outputFeatures.put("Input_Trigger", inputTrigger);
        outputFeatures.put("TriggerID",idOfInputTrigger);
        outputFeatures.put("Text_span",textSpan);
        
        if(featuresToAdd!=null)
        {
            for(int i =0; i< featuresToAdd.size(); i+=2)
            {
            // label is even
            // attribute is odd;
                outputFeatures.put(featuresToAdd.get(i),featuresToAdd.get(i+1));
            }
        }
        return outputFeatures;
    }
    /***************************************************************************
     * method name:setTheFeaturesForAnnotationNew()
     * function: this helper method, given the required parameters will set the features
     * for the scope annotation - first all of the generic features as well any
     * specific ones that have been passed to the method accordingly.
     *
     ****************************************************************************/
    private static gate.FeatureMap setTheFeaturesForAnnotationNew(String annType, String type, String inputTrigger, Integer idOfInputTrigger, String textSpan,ArrayList<Object> featuresToAdd)
    {
        gate.FeatureMap outputFeatures = Factory.newFeatureMap();
        outputFeatures.put("Ann_type",annType);
        outputFeatures.put("Type",type);
        outputFeatures.put("Input_Trigger", inputTrigger);
        outputFeatures.put("TriggerID",idOfInputTrigger);
        outputFeatures.put("Text_span",textSpan);
        
        if(featuresToAdd!=null)
        {
            for(int i =0; i< featuresToAdd.size(); i+=2)
            {
                // label is even
                // attribute is odd;
                outputFeatures.put(featuresToAdd.get(i),featuresToAdd.get(i+1));
            }
        }
        return outputFeatures;
    }
    
  
    /***************************************************************************
     * method name: annotateTheScopeForNSUBJPassScope
     * function: this method, given the required parameters will prepare the scope 
     * features for annotation in the case where the negation trigger is part of the neg 
     * relation and the inital trigger is a verb.
     *
     ****************************************************************************/	
    protected static void annotateTheScopeForNSUBJPassScope(Annotation currentToken, AnnotationSet syntaxTreeNodeAnnotationSet,AnnotationSet outputScopeAnnotations,String outputAnnotationType, Annotation initTerm,String constituentClue,Annotation triggerMarker, Annotation negTriggerAsToken,AnnotationSet tokenAnnotationSetInSentence)
	{
        // need to pass the syntaxNodeIDOfTheNegTrigger ... 
        int syntaxNodeIDOfNegTrigger = InvestigateConstituentsFromParseTree.findSyntaxNode (negTriggerAsToken,syntaxTreeNodeAnnotationSet);
        int syntaxNodeID = InvestigateConstituentsFromParseTree.findSyntaxNode (currentToken,syntaxTreeNodeAnnotationSet);
        int headID = InvestigateConstituentsFromParseTree.findTheConstInParseTree(syntaxNodeID,syntaxTreeNodeAnnotationSet,constituentClue);
        
        
        if ((headID !=-1))
        {
            // new try June 10th
            Annotation syntaxNodeConstHelp = syntaxTreeNodeAnnotationSet.get(headID);
            
            ArrayList <Integer> idsOfConstituentsInScope =InvestigateConstituentsFromParseTree.changeSpanForLeftSpanScope(syntaxNodeIDOfNegTrigger,triggerMarker,headID,syntaxTreeNodeAnnotationSet);
            idsOfConstituentsInScope = InvestigateConstituentsFromParseTree.changeSpanForPunctuation(idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet,tokenAnnotationSetInSentence,triggerMarker.getFeatures().get("classType").toString().trim());
            
            ArrayList returnedListReadyForAnnotation = prepareScopeToAnnotate(syntaxTreeNodeAnnotationSet,idsOfConstituentsInScope);
            if (returnedListReadyForAnnotation.size()!=0)
            {
                String finalS =(String)returnedListReadyForAnnotation.get(0);
                Annotation startOffset =(Annotation)returnedListReadyForAnnotation.get(1);
                Annotation endOffset = (Annotation)returnedListReadyForAnnotation.get(2);
                ArrayList<Object> specificFeatures = new ArrayList<Object>();
                specificFeatures.add("InitialTrigger");
                // convert to string
                specificFeatures.add(initTerm.getFeatures().get("string").toString().trim());
                specificFeatures.add("The_Original_Constituent_ID");
                specificFeatures.add(Integer.toString(headID));
                specificFeatures.add("Constituent ID List");
                specificFeatures.add(idsOfConstituentsInScope);
                gate.FeatureMap outputFeaturesFinal = setTheFeaturesForAnnotationNew("Scope", outputAnnotationType,triggerMarker.getFeatures().get("String").toString().trim(), triggerMarker.getId(),finalS,specificFeatures);
                printToAnnotationSet(outputFeaturesFinal, startOffset, endOffset, outputScopeAnnotations, outputAnnotationType,false,triggerMarker.getId());
                // end new try June 10th
            } // returnedList!=null

                /* try the old version ...
                Annotation headTerm = syntaxTreeNodeAnnotationSet.get((int)headID);
                ArrayList<String> specificFeatures = new ArrayList<String>();
                specificFeatures.add("The_Constituent_ID");
                specificFeatures.add(Integer.toString(headID));
                specificFeatures.add("Neg_Trigger");
                specificFeatures.add(triggerMarker.getFeatures().get("String").toString().trim());
                specificFeatures.add("TriggerID");
                specificFeatures.add(triggerMarker.getId().toString());
                gate.FeatureMap outputFeaturesFinal = setTheFeaturesForAnnotation("Scope", outputAnnotationType, theVerb.getFeatures().get("string").toString().trim(), theVerb.getId(),headTerm.getFeatures().get("text").toString().trim(),specificFeatures);
                printToAnnotationSet(outputFeaturesFinal, headTerm,headTerm, outputScopeAnnotations, outputAnnotationType,false,triggerMarker.getId());*/
                
                
           
            
        }// if heasID!=-1
    }
    /***************************************************************************
     * method name: annotateTheScopeForNSUBJPassScopeN
     * function: this method, given the required parameters will prepare the scope
     * features for annotation in the case where the negation trigger is part of the neg
     * relation and the inital trigger is a verb.
     *
     ****************************************************************************/
    protected static void annotateTheScopeForNSUBJPassScopeN(Annotation currentToken, AnnotationSet syntaxTreeNodeAnnotationSet,AnnotationSet outputScopeAnnotations,String outputAnnotationType, Annotation initTerm,Annotation triggerMarker,AnnotationSet tokenAnnotationSetInSentence, Annotation prepToken,Annotation nSubjPass, HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc)
	{
        
        ArrayList<Integer> parentListOfNegTrigger = tokenPathsOfDoc.get(currentToken.getId());
        
        ArrayList<Integer> parentListOfInitTrigger =tokenPathsOfDoc.get(nSubjPass.getId());
         ArrayList<Integer> parentListOfPrep =tokenPathsOfDoc.get(prepToken.getId());
        int syntaxNodeIDOfNegTrigger =  parentListOfNegTrigger.get(0).intValue();
        
        if(parentListOfInitTrigger.get(0)!=null)
        {
            int syntaxNodeIDOfInitTrigger =  parentListOfInitTrigger.get(0).intValue();
            int headID = InvestigateConstituentsFromParseTree.findTheCommonParentConstInParseTree(parentListOfPrep,parentListOfInitTrigger);
            String negType =null;
            String classType =null;
            if(triggerMarker.getFeatures().get("classType")!=null)
            {
                classType = triggerMarker.getFeatures().get("classType").toString().trim();
                
            }
            
            if(triggerMarker.getFeatures().get("Type")!=null)
            {
                negType = triggerMarker.getFeatures().get("Type").toString().trim();
            }
            String triggerString = triggerMarker.getFeatures().get("String").toString().trim();

        
        //int headID = InvestigateConstituentsFromParseTree.findTheConstInParseTree(syntaxNodeID,syntaxTreeNodeAnnotationSet,constituentClue);
        
        
        if ((headID !=-1))
        {
            // new try June 10th
            Annotation syntaxNodeConstHelp = syntaxTreeNodeAnnotationSet.get(headID);
            
            ArrayList <Integer> idsOfConstituentsInScope =InvestigateConstituentsFromParseTree.changeSpanForLeftSpanScope(syntaxNodeIDOfNegTrigger,triggerMarker,headID,syntaxTreeNodeAnnotationSet);
            idsOfConstituentsInScope = InvestigateConstituentsFromParseTree.changeSpanForPunctuation(idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet,tokenAnnotationSetInSentence,triggerMarker.getFeatures().get("classType").toString().trim());
            
            ArrayList returnedListReadyForAnnotation = prepareScopeToAnnotate(syntaxTreeNodeAnnotationSet,idsOfConstituentsInScope);
            if (returnedListReadyForAnnotation.size()!=0)
            {
                String finalS =(String)returnedListReadyForAnnotation.get(0);
                Annotation startOffset =(Annotation)returnedListReadyForAnnotation.get(1);
                Annotation endOffset = (Annotation)returnedListReadyForAnnotation.get(2);
                ArrayList<Object> specificFeatures = new ArrayList<Object>();
                specificFeatures.add("InitialTrigger");
                // convert to string
                specificFeatures.add(initTerm.getFeatures().get("string").toString().trim());
                specificFeatures.add("The_Original_Constituent_ID");
                specificFeatures.add(Integer.toString(headID));
                specificFeatures.add("Constituent ID List");
                specificFeatures.add(idsOfConstituentsInScope);
                gate.FeatureMap outputFeaturesFinal = setTheFeaturesForAnnotationNew("Scope", outputAnnotationType,triggerMarker.getFeatures().get("String").toString().trim(), triggerMarker.getId(),finalS,specificFeatures);
                printToAnnotationSet(outputFeaturesFinal, startOffset, endOffset, outputScopeAnnotations, outputAnnotationType,false,triggerMarker.getId());
                // end new try June 10th
            } // returnedList!=null
            
            /* try the old version ...
             Annotation headTerm = syntaxTreeNodeAnnotationSet.get((int)headID);
             ArrayList<String> specificFeatures = new ArrayList<String>();
             specificFeatures.add("The_Constituent_ID");
             specificFeatures.add(Integer.toString(headID));
             specificFeatures.add("Neg_Trigger");
             specificFeatures.add(triggerMarker.getFeatures().get("String").toString().trim());
             specificFeatures.add("TriggerID");
             specificFeatures.add(triggerMarker.getId().toString());
             gate.FeatureMap outputFeaturesFinal = setTheFeaturesForAnnotation("Scope", outputAnnotationType, theVerb.getFeatures().get("string").toString().trim(), theVerb.getId(),headTerm.getFeatures().get("text").toString().trim(),specificFeatures);
             printToAnnotationSet(outputFeaturesFinal, headTerm,headTerm, outputScopeAnnotations, outputAnnotationType,false,triggerMarker.getId());*/
            
        }
            
            
        }// if heasID!=-1
    }
    /***************************************************************************
     * method name: annotateTheScopeForNSubjLeftSpan
     * function: this method, given the required parameters will prepare the scope
     * features for annotation in the case where we want the nSubj included in the scope
     * relation and the inital trigger is a verb.
     *
     ****************************************************************************/
    protected static void annotateTheScopeForNSubjLeftSpan(Annotation currentToken, AnnotationSet syntaxTreeNodeAnnotationSet,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc,AnnotationSet outputScopeAnnotations,Annotation triggerMarker,Annotation initTerm, String outputAnnotationType, String depType)
	{
        ArrayList<Integer> parentListOfNegTrigger = tokenPathsOfDoc.get(currentToken.getId());
        ArrayList<Integer> parentListOfInitTrigger =tokenPathsOfDoc.get(initTerm.getId());
        int syntaxNodeIDOfNegTrigger =  parentListOfNegTrigger.get(0).intValue();
        if(parentListOfInitTrigger.get(0)!=null)
        {
            int syntaxNodeIDOfInitTrigger =  parentListOfInitTrigger.get(0).intValue();
            // new May 13th ...
            int headID =-1;
            String posTagOfInit = InvestigateConstituentsFromParseTree.findPosTag (initTerm,syntaxTreeNodeAnnotationSet);
            
            if(posTagOfInit.startsWith("W"))
            {
                headID = InvestigateConstituentsFromParseTree.findTheCommonParentAndSBARFirstConstInParseTree(parentListOfNegTrigger,parentListOfInitTrigger,syntaxTreeNodeAnnotationSet);
            }
            
            else
            {
                headID = InvestigateConstituentsFromParseTree.findTheCommonParentAndSConstInParseTree(parentListOfNegTrigger,parentListOfInitTrigger,syntaxTreeNodeAnnotationSet);
                
            }
            
            String negType = triggerMarker.getFeatures().get("Type").toString().trim();
            String triggerString = triggerMarker.getFeatures().get("String").toString().trim();
            boolean notDone =false;
            if (headID !=-1)
            { 
                Annotation syntaxNodeConstHelp = syntaxTreeNodeAnnotationSet.get(headID);
                ArrayList <Integer> idsOfConstituentsInScope;
                if(depType.equals("conj"))
                {
                    idsOfConstituentsInScope =InvestigateConstituentsFromParseTree.changeSpanForLeftSpanScope(syntaxNodeIDOfNegTrigger,currentToken,headID,syntaxTreeNodeAnnotationSet);
                }
                else
                {
                idsOfConstituentsInScope =InvestigateConstituentsFromParseTree.changeSpanForLeftSpanScope(syntaxNodeIDOfNegTrigger,triggerMarker,headID,syntaxTreeNodeAnnotationSet);
                }
                
                if(idsOfConstituentsInScope.size()!=0)
                {
                    idsOfConstituentsInScope =InvestigateConstituentsFromParseTree.cleanUpSpanFromParseTreeLeft(idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet);
                }
                if(idsOfConstituentsInScope!=null)
                {
                    ArrayList returnedListReadyForAnnotation = prepareScopeToAnnotate(syntaxTreeNodeAnnotationSet,idsOfConstituentsInScope);
                    if (returnedListReadyForAnnotation.size()!=0)
                    {
                        String finalS =(String)returnedListReadyForAnnotation.get(0);
                        Annotation startOffset =(Annotation)returnedListReadyForAnnotation.get(1);
                        Annotation endOffset = (Annotation)returnedListReadyForAnnotation.get(2);
                        ArrayList<String> specificFeatures = new ArrayList<String>();
                        specificFeatures.add("InitialTrigger");
                        // convert to string
                        specificFeatures.add(initTerm.getFeatures().get("string").toString().trim());
                        gate.FeatureMap outputFeaturesFinal = setTheFeaturesForAnnotation("Scope", outputAnnotationType, triggerMarker.getFeatures().get("String").toString().trim(), triggerMarker.getId(), finalS,specificFeatures);
                        printToAnnotationSet(outputFeaturesFinal, startOffset, endOffset, outputScopeAnnotations, outputAnnotationType,false,triggerMarker.getId());
                    } // returnedList!=null
                    
                    
                }
                
            } // head!=-1
        }// parent check
    }//end method
    /***************************************************************************
     * method name: annotateTheScopeForNSubjLeftSpan
     * function: this method, given the required parameters will prepare the scope
     * features for annotation in the case where we want the nSubj included in the scope
     * relation and the inital trigger is a verb.
     *
     ****************************************************************************/
    protected static void annotateTheScopeForLEFT_NEW(Annotation currentToken, AnnotationSet syntaxTreeNodeAnnotationSet,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc,AnnotationSet outputScopeAnnotations,Annotation triggerMarker,Annotation initTerm, String outputAnnotationType, String depType,AnnotationSet tokenAnnotationSetInSentence)
	{
        ArrayList<Integer> parentListOfNegTrigger = tokenPathsOfDoc.get(currentToken.getId());
        ArrayList<Integer> parentListOfInitTrigger =tokenPathsOfDoc.get(initTerm.getId());
      
        int syntaxNodeOfInitTrigger = parentListOfInitTrigger.get(0).intValue();
       
        int syntaxNodeIDOfNegTrigger =  parentListOfNegTrigger.get(0).intValue();
        if(parentListOfInitTrigger.get(0)!=null)
        {
            int syntaxNodeIDOfInitTrigger =  parentListOfInitTrigger.get(0).intValue();
            
            int headID =-1;
            String posTagOfInit = InvestigateConstituentsFromParseTree.findPosTag (initTerm,syntaxTreeNodeAnnotationSet);
            if(depType.equals("vConj"))
            {
                headID = InvestigateConstituentsFromParseTree.findTheConstInParseTree(syntaxNodeIDOfInitTrigger,syntaxTreeNodeAnnotationSet,"VP");
            }
            else if(depType.equals("nConj"))
            {
                headID = InvestigateConstituentsFromParseTree.findTheConstInParseTree(syntaxNodeIDOfInitTrigger,syntaxTreeNodeAnnotationSet,"NP"); 
            }
            
            else
            {
                
                if(depType.equals("negA"))
                {
                    headID = InvestigateConstituentsFromParseTree.findTheConstInParseTree(syntaxNodeOfInitTrigger,syntaxTreeNodeAnnotationSet,"NP");
                }
                else if(depType.equals("negB"))
                {
                    headID = InvestigateConstituentsFromParseTree.findTheConstInParseTree(syntaxNodeOfInitTrigger,syntaxTreeNodeAnnotationSet,"SBAR");
                }
                else
                {
                    headID = InvestigateConstituentsFromParseTree.findTheCommonParentConstInParseTree(parentListOfNegTrigger,parentListOfInitTrigger);
                }
            }
            
            String negType = triggerMarker.getFeatures().get("Type").toString().trim();
            String triggerString = triggerMarker.getFeatures().get("String").toString().trim();
            boolean notDone =false;
            if (headID !=-1)
            {
                Annotation syntaxNodeConstHelp = syntaxTreeNodeAnnotationSet.get(headID);
                ArrayList <Integer> idsOfConstituentsInScope;
                if(depType.equals("conj"))
                {
                    idsOfConstituentsInScope =InvestigateConstituentsFromParseTree.changeSpanForLeftSpanScope(syntaxNodeIDOfNegTrigger,currentToken,headID,syntaxTreeNodeAnnotationSet);
                }
                else
                {
                    idsOfConstituentsInScope =InvestigateConstituentsFromParseTree.changeSpanForLeftSpanScope(syntaxNodeIDOfNegTrigger,triggerMarker,headID,syntaxTreeNodeAnnotationSet);
                }
                if(idsOfConstituentsInScope.size()!=0 && depType.equals("negA")==false && depType.equals("negB")==false)
                {
                    idsOfConstituentsInScope =InvestigateConstituentsFromParseTree.cleanUpSpanFromParseTreeLeft(idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet);
                    
                }
               
                if(idsOfConstituentsInScope!=null)
                {
                    ArrayList returnedListReadyForAnnotation = prepareScopeToAnnotate(syntaxTreeNodeAnnotationSet,idsOfConstituentsInScope);
                    if (returnedListReadyForAnnotation.size()!=0)
                    {
                        String finalS =(String)returnedListReadyForAnnotation.get(0);
                        Annotation startOffset =(Annotation)returnedListReadyForAnnotation.get(1);
                        Annotation endOffset = (Annotation)returnedListReadyForAnnotation.get(2);
                        ArrayList<String> specificFeatures = new ArrayList<String>();
                        specificFeatures.add("InitialTrigger");
                        // convert to string
                        specificFeatures.add(initTerm.getFeatures().get("string").toString().trim());
                        specificFeatures.add("The_Original_Constituent_ID_LEFT_SPAN_N");
                        specificFeatures.add(Integer.toString(headID));
                        gate.FeatureMap outputFeaturesFinal = setTheFeaturesForAnnotation("Scope", outputAnnotationType, triggerMarker.getFeatures().get("String").toString().trim(), triggerMarker.getId(), finalS,specificFeatures);
                        printToAnnotationSet(outputFeaturesFinal, startOffset, endOffset, outputScopeAnnotations, outputAnnotationType,false,triggerMarker.getId());
                    } // returnedList!=null
                    
                    
                }
                
            } // head!=-1
        }// parent check
    }//end method
       /***************************************************************************
     * method name: annotateTheScopeUsingParseTree
     * function: this method, given the required parameters will prepare the scope
     * features for annotation in the case where we want the nSubj included in the scope
     * relation (not using the dependencies).
     *
     ****************************************************************************/
    protected static void annotateTheScopeUsingParseTree(Annotation currentToken, AnnotationSet syntaxTreeNodeAnnotationSet,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc,AnnotationSet outputScopeAnnotations,Annotation triggerMarker,Annotation initTerm, String outputAnnotationType)
	{
        ArrayList<Integer> parentListOfNegTrigger = tokenPathsOfDoc.get(currentToken.getId());
        ArrayList<Integer> parentListOfInitTrigger =tokenPathsOfDoc.get(initTerm.getId());
        int syntaxNodeIDOfNegTrigger =  parentListOfNegTrigger.get(0).intValue();
        if(parentListOfInitTrigger.get(0)!=null)
        {
            int syntaxNodeIDOfInitTrigger =  parentListOfInitTrigger.get(0).intValue();
            int headID = InvestigateConstituentsFromParseTree.findTheCommonParentAndSConstInParseTree(parentListOfNegTrigger,parentListOfInitTrigger,syntaxTreeNodeAnnotationSet);
            String negType = triggerMarker.getFeatures().get("Type").toString().trim();
            String triggerString = triggerMarker.getFeatures().get("String").toString().trim();
            boolean notDone =false;
            if (headID !=-1)
            { 
                Annotation syntaxNodeConstHelp = syntaxTreeNodeAnnotationSet.get(headID);
               
                
                ArrayList <Integer> idsOfConstituentsInScope =InvestigateConstituentsFromParseTree.changeSpanForNSubjRules(syntaxNodeIDOfNegTrigger,triggerMarker,headID,syntaxTreeNodeAnnotationSet);
                
                // Now want to remove all left constituents until we get to the NP
               if(idsOfConstituentsInScope.size()!=0)
                {
                    idsOfConstituentsInScope =InvestigateConstituentsFromParseTree.cleanUpSpanFromParseTreeLeft(idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet);
                   
                   
                }
                if(idsOfConstituentsInScope!=null)
                {
                    ArrayList returnedListReadyForAnnotation = prepareScopeToAnnotate(syntaxTreeNodeAnnotationSet,idsOfConstituentsInScope);
                    if (returnedListReadyForAnnotation.size()!=0)
                    {
                        String finalS =(String)returnedListReadyForAnnotation.get(0);
                        Annotation startOffset =(Annotation)returnedListReadyForAnnotation.get(1);
                        Annotation endOffset = (Annotation)returnedListReadyForAnnotation.get(2);
                        gate.FeatureMap outputFeaturesFinal = setTheFeaturesForAnnotation("Scope", outputAnnotationType, triggerMarker.getFeatures().get("String").toString().trim(), triggerMarker.getId(), finalS,null);
                        printToAnnotationSet(outputFeaturesFinal, startOffset, endOffset, outputScopeAnnotations, outputAnnotationType,false,triggerMarker.getId());
                    } // returnedList!=null
                    
                }
                
            } // head!=-1
        }// parent check
    }//end method

/************************************************************************************************************************************
    
     * method name: annotateTheScopeForASingleConstituentLeft()
     * function: this method, given the required parameters will prepare the scope
     * features for annotation in the case where the negation trigger is a verb
     * - usually an implicit neg trigger. There are 2 different cases dealt with
     * in this method - the first one is if the originally determined scope needs
     * to be modified and the second not.
     *
     ****************************************************************************/
    protected static void annotateTheScopeForASingleConstituentLeft(Annotation currentToken, AnnotationSet syntaxTreeNodeAnnotationSet,AnnotationSet outputScopeAnnotations,Annotation triggerMarker,String constituentClue,Annotation initTerm, String outputAnnotationType,int choice,AnnotationSet tokenAnnotationSetInSentence,Annotation initTrigger    )
	{
        int syntaxNodeID = InvestigateConstituentsFromParseTree.findSyntaxNode (currentToken,syntaxTreeNodeAnnotationSet);
        int headID = InvestigateConstituentsFromParseTree.findTheConstInParseTree(syntaxNodeID,syntaxTreeNodeAnnotationSet,constituentClue);
        String negType =null;
        String classType =null;
        if(triggerMarker.getFeatures().get("classType")!=null)
        {
            classType = triggerMarker.getFeatures().get("classType").toString().trim();
            
        }
        else
        {
            classType = "ModalityTrigger";
        }
        
        if(triggerMarker.getFeatures().get("Type")!=null)
        {
            negType = triggerMarker.getFeatures().get("Type").toString().trim();
        }
        else
        {
            negType ="MODAL_GENERIC";
        }
        
        
        
        if ((headID !=-1))
        {
            ArrayList <Integer> idsOfConstituentsInScope=InvestigateConstituentsFromParseTree.changeSpanForSingleConstLeft(headID,syntaxTreeNodeAnnotationSet,choice);
            idsOfConstituentsInScope = InvestigateConstituentsFromParseTree.changeSpanForCommaN(idsOfConstituentsInScope,syntaxTreeNodeAnnotationSet,tokenAnnotationSetInSentence,triggerMarker);
            ArrayList returnedListReadyForAnnotation=prepareScopeToAnnotate(syntaxTreeNodeAnnotationSet,idsOfConstituentsInScope);
            if (returnedListReadyForAnnotation.size()!=0)
            {
                String finalS =(String)returnedListReadyForAnnotation.get(0);
                Annotation startOffset =(Annotation)returnedListReadyForAnnotation.get(1);
                Annotation endOffset = (Annotation)returnedListReadyForAnnotation.get(2);
                ArrayList<String> specificFeatures = new ArrayList<String>();
                specificFeatures.add("InitialTrigger");
                // convert to string
                String textToAnn = "";
                // get the type of the of the marker
                if(choice ==0)
                {
                    
                    textToAnn = triggerMarker.getFeatures().get("String").toString().trim();
                }
                else if (choice ==1)
                {
                    textToAnn =triggerMarker.getFeatures().get("string").toString().trim();
                }
                
                specificFeatures.add(initTrigger.getFeatures().get("string").toString().trim());
                gate.FeatureMap outputFeaturesFinal = setTheFeaturesForAnnotation("Scope", outputAnnotationType, textToAnn, triggerMarker.getId(), finalS,specificFeatures);
                 printToAnnotationSet(outputFeaturesFinal, startOffset, endOffset, outputScopeAnnotations, outputAnnotationType,false,triggerMarker.getId());
            } // returnedList!=null
        }// if heasID!=-1
    }
    
/****************************************************************************/
    /***************************************************************************
     * method name: printTriggerSet()
     * function: this method, given the required parameters will add the scope trigger
     * annotations to the output annotation set in the required GATE format.
     *
     ****************************************************************************/
    protected static void printTriggerSet(Annotation currentToken, AnnotationSet syntaxTreeNodeAnnotationSet,AnnotationSet outputScopeAnnotations,String outputAnnotationType)
    {
        
        gate.FeatureMap outputFeaturesFinal = Factory.newFeatureMap();
        outputFeaturesFinal.put("Type","GENERIC_MODAL_TRIGGER");
        outputFeaturesFinal.put("String",currentToken.getFeatures().get("string").toString().trim());
        outputFeaturesFinal.put("Token_ID",currentToken.getId());
        printToAnnotationSet(outputFeaturesFinal, currentToken,currentToken,outputScopeAnnotations, outputAnnotationType,false,currentToken.getId());
    }
/****************************************************************************/
  
}// end class




