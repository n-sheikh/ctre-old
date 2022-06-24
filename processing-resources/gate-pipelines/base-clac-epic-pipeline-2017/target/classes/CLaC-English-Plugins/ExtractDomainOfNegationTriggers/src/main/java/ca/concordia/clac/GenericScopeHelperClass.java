/* GenericScopeHelperClass.java
 * Authors:
 * Date: February 2013
 * Purpose: This class contains helper methods used in other classes in this module
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
import java.util.regex.*;

public abstract class GenericScopeHelperClass
{
    protected static ArrayList <Long> negScopesAlreadyAnnotated;
    /***************************************************************************
     * method name: findIfAlreadyAnnotated()
     * returns: a boolean value
     * function: this method will check if a token has already been used in the 
     * determination of a negation being present within the sentence / constituent.
     *
     ****************************************************************************/
	protected static boolean findIfAlreadyAnnotated(ArrayList <Annotation>notAnnotationsAlreadyAnnotated, Annotation tokenToCheck)
	{
		int i=0;
		boolean found= false;
		while((found ==false) && (i< notAnnotationsAlreadyAnnotated.size()))
		{
			if(notAnnotationsAlreadyAnnotated.get(i).coextensive(tokenToCheck))
			{
				found = true;
				break;
			}
			else 
			{
				i++;
			}
		}
		return found;
	}

    /***************************************************************************
     * method name: findTriggerAnnotation()
     * returns: the trigger Annotation (i.e a negation trigger).
     * function: this method will traverse a given trigger set and try and match 
     * a given token with a trigger from the set.
     *
     ****************************************************************************/
    protected static Annotation findTriggerAnnotation(AnnotationSet triggerWordsAnnotationSet, Annotation currentTokenToCheck)
	{
		Annotation toReturn =null;
		boolean foundIDMatch = false;
		boolean annotationFound = false;
		int index = 0;
		Annotation [] tempArray = new Annotation[triggerWordsAnnotationSet.size()];
		tempArray = triggerWordsAnnotationSet.toArray(tempArray);
		Annotation triggerWord = null;
		while(annotationFound == false && index< triggerWordsAnnotationSet.size())
		{
			triggerWord = tempArray[index];
			foundIDMatch = triggerWord.coextensive(currentTokenToCheck);
			int counter =0;
			boolean triggerTypeFound = false;
            if((foundIDMatch))
            {
                toReturn = triggerWord;
                triggerTypeFound = true;
                annotationFound = true;
            }
            index++;
            
		}// while
		return toReturn;
	}
    /***************************************************************************
     * method name: findExistingAnnotation()
     * returns: the token Annotation found
     * function: this method will find if there exists a given annotation from a 
     * particular set according to a match made by the string feature.
     *
     ****************************************************************************/
    protected static Annotation findExistingAnnotation(AnnotationSet triggerWordsAnnotationSet, String stringToCheck)
	{
		Annotation toReturn =null;
		for(Annotation currentToken: triggerWordsAnnotationSet)
        {
            if(currentToken.getFeatures().get("string").toString().trim().toLowerCase().equals(stringToCheck))
            {
                toReturn = currentToken; 
                return toReturn;
            }
        }
		
		return toReturn;
	}
    /***************************************************************************
     * method name: findAnnByOffset()
     * returns: the token Annotation found
     * function: this method will find if there exists a given token annotation 
     * in the passed sentence set according to a match made by the offset features.
     *
     ****************************************************************************/
    protected static Annotation findAnnByOffset(AnnotationSet tokenAnnotationSetOfSentence,long offsetToFind)
    {
        Annotation annToReturn =null;
        for( Annotation myToken : tokenAnnotationSetOfSentence)
        {
            long endOffsetOfmyToken = myToken.getEndNode().getOffset();
            if(endOffsetOfmyToken == offsetToFind)
            {
                annToReturn = myToken;
                return annToReturn;
            }
            
        }
        offsetToFind = offsetToFind-1;
        for( Annotation myToken : tokenAnnotationSetOfSentence)
        {
            long endOffsetOfmyToken = myToken.getEndNode().getOffset();
            if(endOffsetOfmyToken == offsetToFind)
            {
                annToReturn = myToken;
                return annToReturn;
            }
            
        }
        
        return annToReturn;
    }
    /***************************************************************************
     * method name: generatePathsForToken()
     * function: this is a helper method which when given a token annotation will 
     * find all the constituents (paths to) in the parse tree from the given token 
     * and will consequently add each one to an array list and return it.
     *
     ****************************************************************************/
    protected static void generatePathsForToken(Annotation currentTokenAnnotation, HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc, AnnotationSet syntaxTreeNodeAnnotationSet)
    {
        ArrayList<Integer> singleTokenPathToRoot = new ArrayList<Integer>();
        Integer tokenID = currentTokenAnnotation.getId();
        int currentSyntaxNodeID = InvestigateConstituentsFromParseTree.findSyntaxNode(currentTokenAnnotation,syntaxTreeNodeAnnotationSet); 
        singleTokenPathToRoot.add(currentSyntaxNodeID);
        singleTokenPathToRoot = InvestigateConstituentsFromParseTree.findTheParentIDListInParseTree(currentSyntaxNodeID, syntaxTreeNodeAnnotationSet,singleTokenPathToRoot);
        // add to hashMap ...
        tokenPathsOfDoc.put(tokenID,singleTokenPathToRoot);
        
    }
    
    /****************************************************************************
     * method name: findTriggerAnnotationMulti()
     * returns: the trigger Annotation that is multiple Words (i.e a negation trigger).
     * function: this method will traverse a given trigger set and try and match 
     * the given tokens with a trigger from the set.
    *
    ****************************************************************************/
    // current token to check is of .... 
   protected static Annotation findTriggerAnnotationMulti(AnnotationSet triggerWordsAnnotationSet, Annotation currentTokenToCheck,String tokenIDToCheck)
	{
		Annotation toReturn =null;
		boolean foundIDMatch = false;
		boolean annotationFound = false;
		int index = 0;
		Annotation [] tempArray = new Annotation[triggerWordsAnnotationSet.size()];
		tempArray = triggerWordsAnnotationSet.toArray(tempArray);
		Annotation triggerWord = null;
		while(annotationFound == false && index< triggerWordsAnnotationSet.size())
		{
			triggerWord = tempArray[index];
            // new make sure that the id of the token is that of the one in the features for the multi word token trigger in the trigger set
            //1 check if we are looking at a multi word trigger ... 
            if(triggerWord.getFeatures().get("Type")!=null)
            {
            if(triggerWord.getFeatures().get("Type").toString().trim().equals("multiWordExplicitNeg"))
            {
             // 2 now check the id
                String idToCheck = triggerWord.getFeatures().get(tokenIDToCheck).toString().trim();
                Integer idOfTrigger = Integer.valueOf(idToCheck);
                // now try and match the id's
                if (idOfTrigger.equals(currentTokenToCheck.getId()))
                {
                    // have found a match ... (return the id ... )
                    toReturn = triggerWord;
                    annotationFound = true;
                    
                }
            }
            }
            index++;
            
		}// while
		return toReturn;
	}
    /***************************************************************************
      * method name: annotateVerbsNotMarked()
      * this method is called for any implicit negation triggers that are verbs
      * which have not been previously caught by one of the stated dependency cases.
      * function: this is a method which handles the preparation of the scope 
      * parameters for implicit negation triggers that are verbs. This method 
      * will then call the relevent method to annotate the scope.     
      *
      ****************************************************************************/
	protected static void annotateVerbsNotMarked(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet, AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,boolean includeSubjectConstituent,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc)
	{
        
		for(Annotation currentTokenV: tokenAnnotationSetOfSentence)
		{
            String posTagOfCurrentToken = InvestigateConstituentsFromParseTree.findPosTag (currentTokenV,syntaxTreeNodeAnnotationSet);
            Annotation foundDobj = InvestigateDependencies.findMoreDepsForToken(currentTokenV, "dobj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
             Annotation foundPrep = InvestigateDependencies.findMoreDepsForToken(currentTokenV, "prep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
            
            
			if((posTagOfCurrentToken.startsWith("V")) && (findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated,currentTokenV)== false)&& (foundDobj!=null || foundPrep!=null))
			{
				Annotation theTriggerFound = findTriggerAnnotation(triggerWordsAnnotationSet,currentTokenV);
				if(theTriggerFound!=null)
				{
                    if(includeSubjectConstituent ==true)
                    {
                        // NEW ONLY FOR CHALLENGE - USE THE PARSE TREE...
                        /***************************************************************************************/
                        generatePathsForToken(currentTokenV, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        //GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        
                        CreateAnnotations.annotateTheScopeUsingParseTree(currentTokenV,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,currentTokenV,outputAnnotationType);
                        //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                        /***************************************************************************************/
                    }

					CreateAnnotations.annotateTheScopeForASingleConstituent(currentTokenV, syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"VP",null,outputAnnotationType,0, tokenAnnotationSetOfSentence,currentTokenV);
					notAnnotationsAlreadyAnnotated.add(currentTokenV);
				}
			}//if is a v token
		}//end for
	}//end method
     /****************************************************************************
    * method name: annotateImpTriggersNotMarked() - new June 2013
    * this method is called for any implicit negation triggers 
    * which have not been previously caught by one of the stated dependency cases.
    * check if they have an nsubj relation .... 
    * function: this is a method which handles the preparation of the scope
    * parameters for implicit negation triggers that whose scope is the nsubj. 
    *  This method will then call the relevent method to annotate the scope.
    *
    ****************************************************************************/
    
    protected static void annotateImpTriggersNotMarked(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet, AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,boolean includeSubjectConstituent,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc)
	{
        
		for(Annotation currentTokenImp: tokenAnnotationSetOfSentence)
		{
            //String posTagOfCurrentToken = InvestigateConstituentsFromParseTree.findPosTag (currentTokenV,syntaxTreeNodeAnnotationSet);
            Annotation foundNsubj = InvestigateDependencies.findMoreDepsForToken(currentTokenImp, "nsubj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
            Annotation foundCsubj = InvestigateDependencies.findMoreDepsForToken(currentTokenImp, "csubj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
             Annotation foundNSubJPass = InvestigateDependencies.findMoreDepsForToken(currentTokenImp, "nsubjpass", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
            //Annotation foundNSubjPass= InvestigateDependencies.findMoreDepsForToken(currentTokenImp, "nsubjpass", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
            
            
			if((findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated,currentTokenImp)== false))
			{
                if(foundNsubj!=null)
                {
                    Annotation theTriggerFound = findTriggerAnnotation(triggerWordsAnnotationSet,currentTokenImp);
                    // only for imp triggers...
                    if(theTriggerFound!=null && theTriggerFound.getFeatures().get("Type").toString().trim().equals("implicitNeg"))
                    {
                        // annotateTheDepsLeft
                        generatePathsForToken(currentTokenImp, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        generatePathsForToken(foundNsubj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    
                        CreateAnnotations.annotateTheScopeForDepsLeft(currentTokenImp,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundNsubj,outputAnnotationType,tokenAnnotationSetOfSentence);
                        notAnnotationsAlreadyAnnotated.add(currentTokenImp);
                    }
                
                }// nsubj
                
                else if(foundCsubj!=null)
                {
                    Annotation theTriggerFound = findTriggerAnnotation(triggerWordsAnnotationSet,currentTokenImp);
                    // only for imp triggers...
                    if(theTriggerFound!=null && theTriggerFound.getFeatures().get("Type").toString().trim().equals("implicitNeg"))
                    {
                        // annotateTheDepsLeft
                        generatePathsForToken(currentTokenImp, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        generatePathsForToken(foundCsubj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        
                        CreateAnnotations.annotateTheScopeForDepsLeft(currentTokenImp,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundCsubj,outputAnnotationType,tokenAnnotationSetOfSentence);
                        notAnnotationsAlreadyAnnotated.add(currentTokenImp);
                    }
                    
                }// nsubj
                
                else if(foundNSubJPass!=null)
                {
                    Annotation theTriggerFound = findTriggerAnnotation(triggerWordsAnnotationSet,currentTokenImp);
                    // only for imp triggers...
                    if(theTriggerFound!=null && theTriggerFound.getFeatures().get("Type").toString().trim().equals("implicitNeg"))
                    {
                        // annotateTheDepsLeft
                        generatePathsForToken(currentTokenImp, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        generatePathsForToken(foundNSubJPass, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        
                        CreateAnnotations.annotateTheScopeForDepsLeft(currentTokenImp,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundNSubJPass,outputAnnotationType,tokenAnnotationSetOfSentence);
                        notAnnotationsAlreadyAnnotated.add(currentTokenImp);
                    }
                    
                }// nsubj
                
			}//if is a v token
		}//end for
	}//end method


    /***************************************************************************
     * method name: findIfSpanIsAQuestion()
     * this method is chwcks to see if the clause that contains the 
     *negation trigger is in fact a question
     *
     ****************************************************************************/
    protected static boolean findIfSpanIsAQuestion(String negType, int headIDToCheck,AnnotationSet syntaxTreeNodeAnnotationSet, AnnotationSet tokenAnnotationSetOfSentence)
    {
        // new find if this is a question...
        if(negType.equals("explicitNeg"))
        {
            // check if current head is already the right cat
            Annotation checkConstSQ = syntaxTreeNodeAnnotationSet.get(headIDToCheck);
            int sID= InvestigateConstituentsFromParseTree.findTheConstInParseTree(headIDToCheck,syntaxTreeNodeAnnotationSet,"SQ");
            if(checkConstSQ.getFeatures().get("cat").toString().trim().equals("SQ"))
            {
                if(checkConstSQ.getFeatures().get("text").toString().trim().contains("that"))
                {
                    return false;
                }
                
                else
                {
                    return true;
                }
            }
            else if(sID!=-1)
            {
                Annotation helperConstT = syntaxTreeNodeAnnotationSet.get(sID);
                if(helperConstT.getFeatures().get("text").toString().trim().contains("that"))
                {
                    
                }
                else
                {
                    return true;
                }
                
            }
            else
            {
                // check if current head is already the right cat
                Annotation checkConstS = syntaxTreeNodeAnnotationSet.get(headIDToCheck);
                int sIDT= InvestigateConstituentsFromParseTree.findTheConstInParseTree(headIDToCheck,syntaxTreeNodeAnnotationSet,"S");
                if(checkConstSQ.getFeatures().get("cat").toString().trim().equals("S") && checkConstSQ.getFeatures().get("text").toString().trim().endsWith("?"))
                {
                    if(checkConstSQ.getFeatures().get("text").toString().trim().contains("that"))
                    {
                        return false;
                    }
                    else
                    {
                        return true;
                    }
                }
                else if(sIDT!=-1)
                {
                    Annotation helperConst = syntaxTreeNodeAnnotationSet.get(sIDT);
                    if(helperConst.getFeatures().get("text").toString().trim().endsWith("?"))
                    {
                        if(helperConst.getFeatures().get("text").toString().trim().contains("that"))
                        {
                            return false;
                        }
                        else
                        {
                            return true;
                        }
                    }
                    
                }//else if
                
            }//else
            
            
        }// negType
        return false;

    }
    /***************************************************************************
     * method name: markNonDependencyCases()
     * this method is called from the first condition clause in the execute()
     * in the ExtractScopeNegator class.
     * function: this method is called in the case(s) where the scope to be 
     * determined does not rely on dependency relations & is determined to not 
     * have the conventional scope.
     *
     ****************************************************************************/
    protected static void markNonDependencyCases(AnnotationSet tokenAnnotationSetOfSentence,AnnotationSet triggerWordsAnnotationSet, AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,String outputAnnotationType)
    {
        
        for(Annotation currentTokenA : tokenAnnotationSetOfSentence)
        {
            /****RULE A******************************************************************************/
            if(currentTokenA.getFeatures().get("string").toString().trim().toLowerCase().equals("not"))
            {
                // if the over next token is only (space inbetween) ... 
                int nextTokenID = currentTokenA.getId()+2;
                Annotation nextToken = tokenAnnotationSetOfSentence.get(nextTokenID);
                if(nextToken !=null)
                {
                    /*if(nextToken.getFeatures().get("string").toString().trim().equals("only")||nextToken.getFeatures().get("string").toString().trim().equals("just")||nextToken.getFeatures().get("string").toString().trim().equals("even")||nextToken.getFeatures().get("string").toString().trim().equals("because"))*/
                        
                        if(nextToken.getFeatures().get("string").toString().trim().equals("only")|| nextToken.getFeatures().get("string").toString().trim().equals("just")|| nextToken.getFeatures().get("string").toString().trim().equals("because"))
                    {
                        // find the matching neg trigger annotation
                        Annotation theTriggerFound = findTriggerAnnotation(triggerWordsAnnotationSet,currentTokenA);
                        // need to make a scope annotation... (only over that word)
                        if(theTriggerFound!=null)
                        {
                            //outputAnnotationType = "not+even/only/just/because";
                            CreateAnnotations.annotateTheScopeForNonDepRule(nextToken,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,nextToken,outputAnnotationType);
                            notAnnotationsAlreadyAnnotated.add(currentTokenA);
                        }
                    }
                }
            }// end if is "not+ only,just,even"
        }// for
    } // end method
 /**************************************************************************************/
    protected static void setTheList(ArrayList<Long> theNegScopeArrayListEmpty)
    {
        negScopesAlreadyAnnotated = theNegScopeArrayListEmpty;
    }
/**************************************************************************************/
    protected static void addtoAlreadyScopeAnnList(Long s, Long e, Long negID)
    {
        negScopesAlreadyAnnotated.add(s);
        negScopesAlreadyAnnotated.add(e);
        negScopesAlreadyAnnotated.add(negID);
    }
/**************************************************************************************/
    protected static void printList()
    {
        for(int i=0; i< negScopesAlreadyAnnotated.size(); i+=3)
        {
            System.out.print("start: "+negScopesAlreadyAnnotated.get(i));
            System.out.print("  end: "+negScopesAlreadyAnnotated.get(i+1));
             System.out.println("  NEGID: "+negScopesAlreadyAnnotated.get(i+2));
        }
    }
/**************************************************************************************/
    protected static boolean annIsWithinListAlready(Annotation theTokenToPutInScope, Annotation negTrigger)
    {
        Long NegIDConv = Long.parseLong(String.valueOf(negTrigger.getId()));
        //System.out.println("ID to check:"+ NegIDConv+"**");
       
        for(int i=0; i< negScopesAlreadyAnnotated.size(); i+=3)
        {
             
            if(theTokenToPutInScope.getStartNode().getOffset()>=negScopesAlreadyAnnotated.get(i) && theTokenToPutInScope.getEndNode().getOffset()<=negScopesAlreadyAnnotated.get(i+1) && NegIDConv.equals(negScopesAlreadyAnnotated.get(i+2)))
            {
                return true;
            }
        }
        return false;
    }
/**************************************************************************************/
    protected static boolean findVerbGroupSpanInf(Annotation testVerb,AnnotationSet verbGrouperSet)
    {
        for( Annotation currentVG: verbGrouperSet)
        {
            if (testVerb.withinSpanOf(currentVG) && currentVG.getFeatures().get("tense").toString().trim().equals("Inf"))
            {
                return true;
            }
            else if (testVerb.withinSpanOf(currentVG) && currentVG.getFeatures().get("type").toString().trim().equals("SPECIAL") && testVerb.getFeatures().get("string").toString().trim().equals("going")==false && testVerb.getFeatures().get("root").toString().trim().equals("have")==false )
                //&&  testVerb.getFeatures().get("root").toString().trim().equals("be")==false)
            {
                return true;
            }
        }
        
        
        return false;
    }
    
    /****************************************************************************/
    protected static boolean findVerbGroupSpanFree(Annotation testVerb,AnnotationSet verbGrouperSet, AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet depRelationsSetOfSentence)
    {
        for( Annotation currentVG: verbGrouperSet)
        {
            if (testVerb.coextensive(currentVG) && currentVG.getFeatures().get("type").toString().trim().equals("PART")) //(A)
                //&& currentVG.getFeatures().get("tense").toString().trim().equals("Pas")==false)
            {
                // partB test
                Annotation foundDobj = InvestigateDependencies.findMoreDepsForToken(testVerb, "dobj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                if(foundDobj!=null)
                {
                    
                    return true;
                }
            }
        }
        
        
        return false;
    }
    
    /***************************************************************************/
    /***************************************************************************
     * method name: annotateVerbsNotMarked FOR EVENTS (no trigger )()
     * this method is called for any implicit negation triggers that are verbs
     * which have not been previously caught by one of the stated dependency cases.
     * function: this is a method which handles the preparation of the scope
     * parameters for implicit negation triggers that are verbs. This method
     * will then call the relevent method to annotate the scope.
     *
     ****************************************************************************/
	protected static void annotateVerbsIfInfModalOrFreeVerb(Annotation currentTokenV,AnnotationSet tokenAnnotationSetOfSentence,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,String outputAnnotationType, AnnotationSet depRelationsSetOfSentence,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc, String nameOfType)
	{
        Annotation govTokenrcMod = InvestigateDependencies.findDepOfGovenor(currentTokenV, tokenAnnotationSetOfSentence,"rcmod",depRelationsSetOfSentence);
        if(findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated,currentTokenV)== false)
        {
            Annotation foundDobj = InvestigateDependencies.findMoreDepsForToken(currentTokenV, "dobj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
            Annotation foundXcomp = InvestigateDependencies.findMoreDepsForToken(currentTokenV, "xcomp", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
            if(govTokenrcMod != null && foundDobj ==null && foundXcomp ==null)
            {
                CreateAnnotations.annotateTheScopeForASingleConstituentLeft(govTokenrcMod, syntaxTreeNodeAnnotationSet,outputScopeAnnotations,govTokenrcMod ,"NP",govTokenrcMod, outputAnnotationType,1,tokenAnnotationSetOfSentence,govTokenrcMod);
            }
            //add a new annotation - the "initial trigger " ...
            
            else if(foundDobj!=null)
            {
                Annotation foundPrep = InvestigateDependencies.findMoreDepsForToken(foundDobj, "prep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                Annotation foundGovOfDep = InvestigateDependencies.findDepOfGovenor(currentTokenV, tokenAnnotationSetOfSentence,"dep",depRelationsSetOfSentence);
                ;
                if (foundPrep!=null)
                {
                    GenericScopeHelperClass.generatePathsForToken(currentTokenV, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(foundPrep, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(currentTokenV,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, currentTokenV,foundPrep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,1,null);
                    
                    CreateAnnotations.printTriggerSet(currentTokenV,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,"Generic Modal Triggers: "+ nameOfType);
                }
                else if (foundGovOfDep !=null)
                {
                    Annotation foundOtherPrep = InvestigateDependencies.findMoreDepsForToken(foundGovOfDep, "prep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                    if(foundOtherPrep!=null && foundOtherPrep.getStartNode().getOffset()> foundDobj.getStartNode().getOffset())
                    {
                        GenericScopeHelperClass.generatePathsForToken(currentTokenV, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundOtherPrep, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForDeps(currentTokenV,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, currentTokenV,foundOtherPrep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,1,null);
                        
                        CreateAnnotations.printTriggerSet(currentTokenV,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,"Generic Modal Triggers: "+ nameOfType);
                    }
                    
                    else
                    {
                        GenericScopeHelperClass.generatePathsForToken(currentTokenV, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundDobj, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForDeps(currentTokenV,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, currentTokenV,foundDobj,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,1,null);
                        
                        CreateAnnotations.printTriggerSet(currentTokenV,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,"Generic Modal Triggers: "+ nameOfType);
                    }
                }
                else
                {
                    GenericScopeHelperClass.generatePathsForToken(currentTokenV, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(foundDobj, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(currentTokenV,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, currentTokenV,foundDobj,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,1,null);
                    
                    CreateAnnotations.printTriggerSet(currentTokenV,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,"Generic Modal Triggers: "+ nameOfType);
                }
            }
            else
            {
                CreateAnnotations.annotateTheScopeForASingleConstituent(currentTokenV, syntaxTreeNodeAnnotationSet,outputScopeAnnotations,currentTokenV,"VP",null,outputAnnotationType,1,tokenAnnotationSetOfSentence,currentTokenV);
                
                CreateAnnotations.printTriggerSet(currentTokenV,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,"Generic Modal Triggers: "+ nameOfType);
            }
        }//if is a v token
		//}//end for
	}//end method
    /***************************************************************************/
    protected static void findIfSpanIsAQuestionForModal(Annotation currentToken,String negType, int headIDToCheck,AnnotationSet syntaxTreeNodeAnnotationSet, AnnotationSet tokenAnnotationSetOfSentence,String outputAnnotationType,AnnotationSet outputScopeAnnotations)
    {
        // new find if this is a question...
        if(negType.equals("modalQuest"))
        {
            // check if current head is already the right cat
            Annotation checkConstSQ = syntaxTreeNodeAnnotationSet.get(headIDToCheck);
            int sID= InvestigateConstituentsFromParseTree.findTheConstInParseTree(headIDToCheck,syntaxTreeNodeAnnotationSet,"SQ");
            if(checkConstSQ.getFeatures().get("cat").toString().trim().equals("SQ"))
            {
                CreateAnnotations.annotateTheScopeForASingleConstituent(currentToken, syntaxTreeNodeAnnotationSet,outputScopeAnnotations,currentToken,"SQ",null,outputAnnotationType,1,tokenAnnotationSetOfSentence,currentToken);
                //return true;
            }
            else if(sID!=-1)
            {
                //return true;
                CreateAnnotations.annotateTheScopeForASingleConstituent(currentToken, syntaxTreeNodeAnnotationSet,outputScopeAnnotations,currentToken,"SQ",null,outputAnnotationType,1,tokenAnnotationSetOfSentence,currentToken);
                
            }
            else
            {
                // check if current head is already the right cat
                Annotation checkConstS = syntaxTreeNodeAnnotationSet.get(headIDToCheck);
                int sIDT= InvestigateConstituentsFromParseTree.findTheConstInParseTree(headIDToCheck,syntaxTreeNodeAnnotationSet,"S");
                if(checkConstSQ.getFeatures().get("cat").toString().trim().equals("S") && checkConstSQ.getFeatures().get("text").toString().trim().endsWith("?"))
                {
                    //return true;
                    CreateAnnotations.annotateTheScopeForASingleConstituent(currentToken, syntaxTreeNodeAnnotationSet,outputScopeAnnotations,currentToken,"VP",null,outputAnnotationType,1,tokenAnnotationSetOfSentence,currentToken);
                }
                else if(sIDT!=-1)
                {
                    Annotation helperConst = syntaxTreeNodeAnnotationSet.get(sIDT);
                    if(helperConst.getFeatures().get("text").toString().trim().endsWith("?"))
                    {
                        //return true;
                        CreateAnnotations.annotateTheScopeForASingleConstituent(currentToken,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,currentToken,"VP",null,outputAnnotationType,1,tokenAnnotationSetOfSentence,currentToken);
                    }
                    
                }//else if
                
            }//else
            
            
        }// negType
        //return false;
        
    }
    /***************************************************************************/
}// end class



