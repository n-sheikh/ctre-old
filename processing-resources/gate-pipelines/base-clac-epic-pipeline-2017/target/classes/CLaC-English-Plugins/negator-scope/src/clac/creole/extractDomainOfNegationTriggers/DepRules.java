/* DepRules.java
 * Authors:
 * Date: January 2012
 * Purpose: This class implements the methods for how to deal with each dependency relation found
 *
 *
 *
 */

package clac.creole.extractDomainOfNegationTriggers;

import java.util.*;
import gate.*;
import gate.creole.*;
import gate.creole.metadata.*;
import gate.util.*;
import gate.annotation.*;
import gate.event.*;
import java.util.regex.*;

public abstract class DepRules
{
    /***************************************************************************
     * method name:findScopeForNegDependency()
     * this method is called from the switch case in the execute() method from 
     * the ExtractNegatorScope class.
     * function: this method takes care of the neg dependency case. 
     * Specifically, it will subdivide the more general cases into more 
     * constrained cases within (new conditions).Depending on the new constraints, 
     * this method will then call the relevent method to annotate the scope.
     *
     ****************************************************************************/
    protected static void findScopeForNegDependency(Annotation govenorToken,AnnotationSet triggerWordsAnnotationSet, AnnotationSet tokenAnnotationSetOfSentence,String outputAnnotationType,AnnotationSet syntaxTreeNodeAnnotationSet,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc, ArrayList <Annotation>notAnnotationsAlreadyAnnotated, Integer depID, AnnotationSet outputScopeAnnotations,AnnotationSet depRelationsSetOfSentence,boolean includeSubjectConstituent, boolean includeSpanBeforeNot)
    {
        
        /***RULE 1 - for explicit marker **************************************************-- ie "not involved/ never been"*/
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        Annotation theTriggerFound =  GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
        if((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
        {
            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
            
            if(includeSubjectConstituent==true)
            {
                
                Annotation foundNsubjPassOnDep  = InvestigateDependencies.findMoreDepsForToken(govenorToken, "nsubjpass", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                 Annotation foundNsubjOnDep  = InvestigateDependencies.findMoreDepsForToken(govenorToken, "nsubj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                Annotation foundCCOmpOnDep  = InvestigateDependencies.findMoreDepsForToken(govenorToken, "ccomp", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                Annotation foundConjGov =  InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"conj",depRelationsSetOfSentence);
                Annotation foundDepOnGov =  InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"dep",depRelationsSetOfSentence);
                ArrayList<Annotation>foundDepAuxList =  InvestigateDependencies.findMoreDepsForTokenList(govenorToken, "dep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                 Annotation foundAuxAux =  InvestigateDependencies.findMoreDepsForToken(govenorToken, "aux", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                Annotation foundConjOnNegGov =  InvestigateDependencies.findMoreDepsForToken(govenorToken, "conj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                 Annotation foundExplOnNegGov =  InvestigateDependencies.findMoreDepsForToken(govenorToken, "expl", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                
                if(foundNsubjOnDep!=null && foundNsubjOnDep.getStartNode().getOffset()<govenorToken.getStartNode().getOffset())
                {
                     Annotation foundRcMod =  InvestigateDependencies.findMoreDepsForToken(foundNsubjOnDep, "rcmod", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                    
                    Annotation foundPrep =  InvestigateDependencies.findMoreDepsForToken(foundNsubjOnDep, "prep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                    
                    Annotation partmod= InvestigateDependencies.findMoreDepsForToken(foundNsubjOnDep, "partmod", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                    
                    if(foundPrep == null)
                    {
                        
                        if(partmod!=null)
                        {
                            foundPrep= InvestigateDependencies.findMoreDepsForToken(partmod, "prep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                        }

                    }
                    
                    if(foundAuxAux!=null&& foundAuxAux.getStartNode().getOffset()<theTriggerFound.getStartNode().getOffset())
                    {
                        // mark
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundAuxAux, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        
                        CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundAuxAux,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"NP",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                    }
                    else if(foundDepAuxList!=null)
                    {
                        for(Annotation foundDepAux:foundDepAuxList)
                        {
                            if(foundDepAux.getFeatures().get("category").toString().trim().startsWith("AUX")&&foundDepAux.getStartNode().getOffset()<theTriggerFound.getStartNode().getOffset())
                            {
                                //mark
                                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(foundDepAux, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                
                                CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundDepAux,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"NP",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                                break;
                            }
                        }
                    }
                    // new may 10
                    if(foundNsubjOnDep.getFeatures().get("string").toString().trim().toLowerCase().equals("which"))
                    {
                        /***************************************************************************************/
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundNsubjOnDep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundNsubjOnDep,outputAnnotationType,"negB",tokenAnnotationSetOfSentence);
                        //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                        /***************************************************************************************/
                    }
                    else if (foundRcMod!=null)
                    {
                        /***************************************************************************************/
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundRcMod, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundRcMod,outputAnnotationType,"negA",tokenAnnotationSetOfSentence);
                        //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                        /***************************************************************************************/

                    }
                    else if(partmod!=null)
                    {
                        /***************************************************************************************/
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(partmod, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,partmod,outputAnnotationType,"negA",tokenAnnotationSetOfSentence);
                        //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                        /***************************************************************************************/
                        
                    }
                    else if (foundPrep!=null)
                    {
                        /***************************************************************************************/
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundPrep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundPrep,outputAnnotationType,"negA",tokenAnnotationSetOfSentence);
                        //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                        /***************************************************************************************/
                        
                    }
                    else
                    {

                    /***************************************************************************************/
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(foundNsubjOnDep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                     GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundNsubjOnDep,outputAnnotationType,"negA",tokenAnnotationSetOfSentence);
                    /***************************************************************************************/
                    }
                    
                }
                
                else if (foundNsubjPassOnDep!=null)
                {
                    if(foundAuxAux!=null&& foundAuxAux.getStartNode().getOffset()<theTriggerFound.getStartNode().getOffset())
                    {
                        // mark
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundAuxAux, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        
                        CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundAuxAux,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"NP",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                    }
                    else if(foundDepAuxList!=null)
                    {
                        for(Annotation foundDepAux:foundDepAuxList)
                        {
                            if(foundDepAux.getFeatures().get("category").toString().trim().startsWith("AUX")&&foundDepAux.getStartNode().getOffset()<theTriggerFound.getStartNode().getOffset())
                            {
                                //mark
                                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(foundDepAux, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                
                                CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundDepAux,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"NP",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                                break;
                            }
                        }
                    }

                    if(foundNsubjPassOnDep.getFeatures().get("string").toString().trim().toLowerCase().equals("which"))
                    {
                        /***************************************************************************************/
                         GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                         GenericScopeHelperClass.generatePathsForToken(foundNsubjPassOnDep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                         CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundNsubjPassOnDep,outputAnnotationType,"negB",tokenAnnotationSetOfSentence);
                         //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                         /***************************************************************************************/
                    }
                    else
                    {
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundNsubjPassOnDep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        
                        Annotation foundNSubjPassPrep = InvestigateDependencies.findMoreDepsForToken(foundNsubjPassOnDep, "prep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                        if(foundNSubjPassPrep!=null)
                        {
                            
                            GenericScopeHelperClass.generatePathsForToken(foundNSubjPassPrep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        
                            CreateAnnotations.annotateTheScopeForNSUBJPassScopeN(matchedDepToken,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,outputAnnotationType,govenorToken, theTriggerFound,tokenAnnotationSetOfSentence,foundNSubjPassPrep,foundNsubjPassOnDep,tokenPathsOfDoc);
                        }
                        else
                        {
                        
                             CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundNsubjPassOnDep,outputAnnotationType,"nsubjPass",tokenAnnotationSetOfSentence);
                        
                        }
                    } // not a which

                    
                } // there is a passive subj
                 /***************************************************************************************/
                // try again...
                else if(foundConjGov!=null && foundNsubjOnDep==null)
                {
                    Annotation foundNsubjOnGov  = InvestigateDependencies.findMoreDepsForToken(foundConjGov, "nsubj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                    Annotation foundNsubjPassOnGov  = InvestigateDependencies.findMoreDepsForToken(foundConjGov, "nsubjpass", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                    if(foundNsubjOnGov!=null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundNsubjOnGov, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                         GenericScopeHelperClass.generatePathsForToken(foundConjGov, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForLEFT_NEW(foundConjGov, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundNsubjOnGov,outputAnnotationType,"negA",tokenAnnotationSetOfSentence);
                        Annotation foundDepOnGovN  = InvestigateDependencies.findMoreDepsForToken(govenorToken, "dep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                         
                        Annotation foundAuxOnGov  = InvestigateDependencies.findMoreDepsForToken(govenorToken, "aux", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                       
                        if(foundDepOnGovN!=null && foundDepOnGovN.getFeatures().get("category").toString().trim().startsWith("AUX") && foundDepOnGovN.getStartNode().getOffset()< matchedDepToken.getStartNode().getOffset())
                        {
                            // ann single const
                            GenericScopeHelperClass.generatePathsForToken(foundDepOnGovN, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                           
                            CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundDepOnGovN,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"NP",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                        }
                        else if(foundAuxOnGov !=null && foundAuxOnGov.getFeatures().get("category").toString().trim().startsWith("AUX") && foundAuxOnGov.getStartNode().getOffset()< matchedDepToken.getStartNode().getOffset())
                        {
                            // ann single const
                            GenericScopeHelperClass.generatePathsForToken(foundAuxOnGov, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            
                            CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundAuxOnGov,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"NP",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                        }
                        
                        
                        
                    }// there is a nounSubjOnGov
                    else if(foundNsubjPassOnGov!=null)
                    {
                        
                        //TRY::
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundNsubjPassOnGov, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundConjGov, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        
                        // NEW JUNE 23rd:: span only till parent conj
                        CreateAnnotations.annotateTheScopeForLEFT_NEW(foundConjGov, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundNsubjPassOnGov,outputAnnotationType,"negA",tokenAnnotationSetOfSentence);
                        
                        // annotate the aux
                        Annotation foundDepOnGovN  = InvestigateDependencies.findMoreDepsForToken(govenorToken, "dep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                        
                        Annotation foundAuxOnGov  = InvestigateDependencies.findMoreDepsForToken(govenorToken, "aux", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                        
                        if(foundDepOnGovN!=null && foundDepOnGovN.getFeatures().get("category").toString().trim().startsWith("AUX") && foundDepOnGovN.getStartNode().getOffset()< matchedDepToken.getStartNode().getOffset())
                        {
                            // ann single const
                            GenericScopeHelperClass.generatePathsForToken(foundDepOnGovN, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            
                            CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundDepOnGovN,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"NP",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                        }
                        else if(foundAuxOnGov !=null && foundAuxOnGov.getFeatures().get("category").toString().trim().startsWith("AUX") && foundAuxOnGov.getStartNode().getOffset()< matchedDepToken.getStartNode().getOffset())
                        {
                            // ann single const
                            GenericScopeHelperClass.generatePathsForToken(foundAuxOnGov, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            
                            CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundAuxOnGov,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"NP",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                        }
                        
                        
                        
                    }// there is a nounSubjOnGovPass
                    else
                    {
                        
                        Annotation foundccomp =  InvestigateDependencies.findDepOfGovenor(foundConjGov, tokenAnnotationSetOfSentence,"ccomp",depRelationsSetOfSentence);
                        Annotation foundConjGovasDEP =  InvestigateDependencies.findDepOfGovenor(foundConjGov, tokenAnnotationSetOfSentence,"nsubj",depRelationsSetOfSentence);

                        if(foundccomp!=null)
                        {
                             Annotation foundSubjOnCCompDep = InvestigateDependencies.findMoreDepsForToken(foundccomp, "nsubj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                            if(foundSubjOnCCompDep!=null)
                            {
                                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(foundSubjOnCCompDep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                
                                CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundSubjOnCCompDep,outputAnnotationType,"negA",tokenAnnotationSetOfSentence);
                            }
                            else
                            {
                                /***************************************************************************************/
                                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                
                                CreateAnnotations.annotateTheScopeUsingParseTree(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType);
                                //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                /***************************************************************************************/
                            }
                            
                        } // found ccomp ie was
                        else
                        {
                        /***************************************************************************************/
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        
                        CreateAnnotations.annotateTheScopeUsingParseTree(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType);
                        //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                        /***************************************************************************************/
                        }
                    }//else


                }// end found conj gov of neg gov
              
                else if (foundExplOnNegGov!=null)
                {
                    
                    if(foundAuxAux!=null&& foundAuxAux.getStartNode().getOffset()<theTriggerFound.getStartNode().getOffset())
                    {
                        // mark
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundAuxAux, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        
                        CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundAuxAux,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"NP",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                    }
                    else if(foundDepAuxList!=null)
                    {
                        for(Annotation foundDepAux:foundDepAuxList)
                        {
                            if(foundDepAux.getFeatures().get("category").toString().trim().startsWith("AUX")&&foundDepAux.getStartNode().getOffset()<theTriggerFound.getStartNode().getOffset())
                            {
                                //mark
                                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(foundDepAux, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                
                                CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundDepAux,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"NP",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                                break;
                            }
                        }
                    } // mark the aux

                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(foundExplOnNegGov, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    
                    CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundExplOnNegGov,outputAnnotationType,"negA",tokenAnnotationSetOfSentence);
                    
                }// end expl

                else
                {
                    /*********************************************REMOVE JUNE 20******************************************/
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    
                    CreateAnnotations.annotateTheScopeUsingParseTree(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType);
                    //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    /***************************************************************************************/
                }
            } // if we want LHS of scope
            /***************************************************************************************/
            String posTagOfGov = InvestigateConstituentsFromParseTree.findPosTag (govenorToken,syntaxTreeNodeAnnotationSet);
            if((InvestigateDependencies.findMoreDeps(govenorToken, "nsubjpass", true,depRelationsSetOfSentence) == true) && (posTagOfGov.startsWith("V")))
            {
                Annotation foundNSubjPass = InvestigateDependencies.findMoreDepsForToken(govenorToken, "nsubjpass", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                // added this in ... took this out June 10th ...

               Annotation foundNSubjPassPrep = InvestigateDependencies.findMoreDepsForToken(foundNSubjPass, "prep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                if(foundNSubjPassPrep!=null)
                {
                     GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                     GenericScopeHelperClass.generatePathsForToken(foundNSubjPass, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                     GenericScopeHelperClass.generatePathsForToken(foundNSubjPassPrep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                     
                    CreateAnnotations.annotateTheScopeForNSUBJPassScopeN(matchedDepToken,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,outputAnnotationType,govenorToken, theTriggerFound,tokenAnnotationSetOfSentence,foundNSubjPassPrep,foundNSubjPass,tokenPathsOfDoc);
                    
                  //  CreateAnnotations.annotateTheScopeForNSUBJPassScopeN(matchedDepToken,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,"nSubjPassSetN",govenorToken, theTriggerFound,tokenAnnotationSetOfSentence,foundNSubjPassPrep,foundNSubjPass,tokenPathsOfDoc);
                    
                }
                else
               {
                   //CreateAnnotations.annotateTheScopeForNSUBJPassScope(foundNSubjPass,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,"nSubjPassSetN",govenorToken,"NP",theTriggerFound,matchedDepToken,tokenAnnotationSetOfSentence);
                   CreateAnnotations.annotateTheScopeForNSUBJPassScope(foundNSubjPass,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,outputAnnotationType,govenorToken,"NP",theTriggerFound,matchedDepToken,tokenAnnotationSetOfSentence);
                    
              }

            }
            //end nsubpass check
            Annotation foundPrep = InvestigateDependencies.findMoreDepsForToken(govenorToken, "prep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
            Annotation foundXcomp = InvestigateDependencies.findMoreDepsForToken(govenorToken, "xcomp", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
            Annotation foundDep = InvestigateDependencies.findMoreDepsForToken(govenorToken, "dep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
            boolean isATo =false;
            Annotation isGovOfAux = null;
            if(foundDep!=null)
            {
                isGovOfAux = InvestigateDependencies.findMoreDepsForToken(foundDep, "aux", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                if(isGovOfAux!=null)
                {
                    String posTagOfGovAux = InvestigateConstituentsFromParseTree.findPosTag (isGovOfAux,syntaxTreeNodeAnnotationSet);
                    if(posTagOfGovAux.equals("TO"))
                    {
                        isATo =true;
                    }
                }
            }
            if(foundPrep!=null && foundPrep.getStartNode().getOffset() > govenorToken.getStartNode().getOffset())
            {
                GenericScopeHelperClass.generatePathsForToken(foundPrep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                if(govenorToken.getEndNode().getOffset() > matchedDepToken.getEndNode().getOffset())
                {
                    //new
                    Annotation foundCop = InvestigateDependencies.findMoreDepsForToken(govenorToken, "cop", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                    // new also annotate the aux...
                    //new
                    Annotation foundAux = InvestigateDependencies.findMoreDepsForToken(govenorToken, "aux", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                    if(foundCop!=null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(foundCop, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        // annotate the left constituent...
                        
                        // NEW:: check that is not already annotated:: MARCH 17th
                        if(GenericScopeHelperClass.annIsWithinListAlready(foundCop,theTriggerFound) ==false && includeSpanBeforeNot ==true)
                        {
                            CreateAnnotations.annotateTheScopeForDepsLeft(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundCop,outputAnnotationType,tokenAnnotationSetOfSentence);
                        }
                        
                        GenericScopeHelperClass.generatePathsForToken(foundPrep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                        notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                        
                    }
                    else if(foundAux!=null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(foundAux, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        if(GenericScopeHelperClass.annIsWithinListAlready(foundAux,theTriggerFound) ==false && includeSpanBeforeNot ==true)
                        {

                        CreateAnnotations.annotateTheScopeForDepsLeft(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundAux,outputAnnotationType,tokenAnnotationSetOfSentence);
                        }
                        
                        GenericScopeHelperClass.generatePathsForToken(foundPrep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                        notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                        
                    }
                    else
                    {
                        GenericScopeHelperClass.generatePathsForToken(foundPrep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                        notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    }
                } // if gov token comes after the neg trigger and there is a prep relation
               
                else if(posTagOfGov.startsWith("V")||posTagOfGov.startsWith("AUX")||posTagOfGov.startsWith("MD")&& govenorToken.getEndNode().getOffset() <matchedDepToken.getEndNode().getOffset())
                {
                    
                    if(GenericScopeHelperClass.annIsWithinListAlready(govenorToken,theTriggerFound) ==false && includeSpanBeforeNot ==true)
                    {

                    CreateAnnotations.annotateTheScopeForDepsLeft(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence);
                    }
                    
                    GenericScopeHelperClass.generatePathsForToken(foundPrep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken); 
                    
                } // if gov token comes before neg trigger and the prep relation exists
                // annotate rhs side of scope - just the prep condition is met...
                else
                {
                    GenericScopeHelperClass.generatePathsForToken(foundPrep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                }
                
                
            } // end prep case 
            else if(foundXcomp!=null && foundXcomp.getStartNode().getOffset() > govenorToken.getStartNode().getOffset())
            {
                GenericScopeHelperClass.generatePathsForToken(foundXcomp, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                if(govenorToken.getEndNode().getOffset() > matchedDepToken.getEndNode().getOffset())
                {
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundXcomp,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken    );
                }
                else if(posTagOfGov.startsWith("V")||posTagOfGov.startsWith("AUX")||posTagOfGov.startsWith("MD")&& govenorToken.getEndNode().getOffset() <matchedDepToken.getEndNode().getOffset())
                {
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundXcomp,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken); 
                    
                }
                else
                {
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundXcomp,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                }
                
                
            } // end xcomp case 
            // no xcomp no prep
            //********NEW CASE ::: dep case///
            
            else if(foundDep!=null && isGovOfAux!=null && isATo == true)
            {
                GenericScopeHelperClass.generatePathsForToken(foundDep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundDep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                notAnnotationsAlreadyAnnotated.add(matchedDepToken); 
                
            } // end dep case 
            //******** END NEW CASE :::://
            else
            {
                if(govenorToken.getEndNode().getOffset()> matchedDepToken.getEndNode().getOffset())
                {
                    Annotation foundCop = InvestigateDependencies.findMoreDepsForToken(govenorToken, "cop", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                    Annotation foundAux = InvestigateDependencies.findMoreDepsForToken(govenorToken, "aux", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                    if(foundCop!=null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(foundCop, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        if(GenericScopeHelperClass.annIsWithinListAlready(foundCop,theTriggerFound) ==false && includeSpanBeforeNot ==true)
                        {

                            CreateAnnotations.annotateTheScopeForDepsLeft(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundCop,outputAnnotationType,tokenAnnotationSetOfSentence);
                        }
                            CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                            notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                       
                    }// there is a copula
                    else if(foundAux!=null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(foundAux, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        // annotate the copula
                        // NEW:: check that is not already annotated:: MARCH 17th
                        if(GenericScopeHelperClass.annIsWithinListAlready(foundAux,theTriggerFound) ==false && includeSpanBeforeNot==true)
                        {
                            CreateAnnotations.annotateTheScopeForDepsLeft(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundAux,outputAnnotationType,tokenAnnotationSetOfSentence);
                        }

                         CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                            notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    }// there is an aux
                    else
                    {
                        
                      CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                           notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                       
                    }
                }// gov token comes after the neg trigger
                
                else if(posTagOfGov.startsWith("V")||posTagOfGov.startsWith("AUX")||posTagOfGov.startsWith("MD")&& govenorToken.getEndNode().getOffset() < matchedDepToken.getEndNode().getOffset())
                {
                    if(GenericScopeHelperClass.annIsWithinListAlready(govenorToken,theTriggerFound) ==false && includeSpanBeforeNot ==true)
                    {
                        CreateAnnotations.annotateTheScopeForDepsLeft(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence);
                    }
                    
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    
                    Annotation founddep = InvestigateDependencies.findMoreDepsForToken(govenorToken, "dep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                   
                    Annotation foundAdvMod = InvestigateDependencies.findMoreDepsForToken(govenorToken, "advmod", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                    
                    if(founddep!=null && founddep.getStartNode().getOffset() > matchedDepToken.getStartNode().getOffset())
                    {
                        GenericScopeHelperClass.generatePathsForToken(founddep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        // annotate the rhs
                        CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,founddep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken); 
                    }
                    // new case
                    else if(foundAdvMod!=null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(foundAdvMod, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        // annotate the rhs
                        CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundAdvMod,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                    }
                    else
                    {
                        CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                    }
                }// gov comes before neg trigger && gov is an aux or a verb
                else
                {
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    
                    
                }
            }// no prep case
            
        }// if trigger !=null
    }//end method
    /***************************************************************************
     * method name: **MODIFIED** findScopeForDetDependency()
     * this method is called from the switch case in the execute() method from 
     * the ExtractNegatorScope class.
     * function: this method takes care of the det dependency case. 
     * Specifically, it will subdivide the more general cases into more 
     * constrained cases within (new conditions).Depending on the new constraints, 
     * this method will then call the relevent method to annotate the scope.
     *
     ****************************************************************************/
    protected static void findScopeForDetDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID, String outputAnnotationType,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc, AnnotationSet depRelationsSetOfSentence,String testAnnotationType,boolean includeSubjectConstituent,boolean includeSpanBeforeNot)
    {       Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
            Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
            if((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
            {
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    
                  if(includeSubjectConstituent==true)
                     {

                         // default case
                          Annotation foundNsubjOnDep  = InvestigateDependencies.findMoreDepsForToken(govenorToken, "nsubj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                         Annotation foundExp=  InvestigateDependencies.findMoreDepsForToken(govenorToken, "expl", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                         // if determiner is a noun perhaps need to find the verb ... to get to subj
                         Annotation foundDobjGov = InvestigateDependencies.findDepOfGovenor(govenorToken,tokenAnnotationSetOfSentence,"dobj",depRelationsSetOfSentence);
                         // if gov is a dep of the conjunct relation
                         Annotation foundConjGov =  InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"conj",depRelationsSetOfSentence);
                         //if gov is gov of conjunct relation and the dep is instead of nsubj?
                          Annotation conjDep=  InvestigateDependencies.findMoreDepsForToken(govenorToken, "conj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                         
                         Annotation foundNSubjOnConj =null;
                         Annotation foundConjOnDobj =null;
                         Annotation foundNSubjOnDobjGov =null;
                         
                         if(foundNsubjOnDep!=null)
                         {
                             GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                             GenericScopeHelperClass.generatePathsForToken(foundNsubjOnDep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                             GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                             
                             CreateAnnotations. annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundNsubjOnDep,outputAnnotationType,"det",tokenAnnotationSetOfSentence);
                         }
                         
                         
                         if(foundDobjGov !=null)
                         {
                             
                             foundNSubjOnDobjGov =InvestigateDependencies.findMoreDepsForToken(foundDobjGov, "nsubj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                             // only go futhur if there is no nsubj on direct obj - and is in a conjunction...
                             if(foundNSubjOnDobjGov ==null)
                             {
                                 foundConjOnDobj = InvestigateDependencies.findDepOfGovenor(foundDobjGov, tokenAnnotationSetOfSentence,"conj",depRelationsSetOfSentence);
                                 if(foundConjOnDobj!=null)
                                 {
                                     
                                     foundNSubjOnConj =InvestigateDependencies.findMoreDepsForToken(foundConjOnDobj, "nsubj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                                     // need to add a case here where there is a conjunction?

                                 }
                             }
                         }// set up for dobj gov
                         
                             if(foundNSubjOnDobjGov!=null)
                             {
                                 /***************************************************************************************/
                                 GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                 GenericScopeHelperClass.generatePathsForToken(foundNSubjOnDobjGov, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                 GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                 CreateAnnotations. annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundNSubjOnDobjGov,outputAnnotationType,"det",tokenAnnotationSetOfSentence);
                             }
                             else if(foundExp!=null)
                             {
                             
                             /***************************************************************************************/
                             GenericScopeHelperClass.generatePathsForToken(foundExp, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                             GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                             GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                             // change to the nsubj method:: June 20th...
                             CreateAnnotations. annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundExp,outputAnnotationType,"exp",tokenAnnotationSetOfSentence);
                             /***************************************************************************************/
                             }

                         
                         else if(foundConjGov!=null && foundNsubjOnDep == null)
                         {
                             Annotation foundNsubjOnGov  = InvestigateDependencies.findMoreDepsForToken(foundConjGov, "nsubj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                             if(foundNsubjOnGov!=null)
                             {
                                 GenericScopeHelperClass.generatePathsForToken(foundNsubjOnGov, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                 GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                 GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                 if(foundConjGov.getFeatures().get("category").toString().trim().startsWith("V"))
                                 {
                                    
                                     AnnotationSet testSet = tokenAnnotationSetOfSentence.get(foundConjGov.getEndNode().getOffset()+1);
                                     ArrayList <Annotation> testListOfSentence= new ArrayList<Annotation>(testSet);
                                     OffsetComparator comparatorTestToks = new OffsetComparator();
                                     Collections.sort( testListOfSentence,comparatorTestToks);
                                     Annotation test = testListOfSentence.get(0);
                                    GenericScopeHelperClass.generatePathsForToken(test, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                    CreateAnnotations. annotateTheScopeForLEFT_NEW(test, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundNsubjOnGov,outputAnnotationType,"conj",tokenAnnotationSetOfSentence);
                                     
                                 }
                                 else
                                 {
                                     // we actually just mark the subj node..
                                     AnnotationSet testSet = tokenAnnotationSetOfSentence.get(foundNsubjOnGov.getEndNode().getOffset()+1);
                                     ArrayList <Annotation> testListOfSentence= new ArrayList<Annotation>(testSet);
                                     OffsetComparator comparatorTestToks = new OffsetComparator();
                                     Collections.sort( testListOfSentence,comparatorTestToks);
                                     Annotation test = testListOfSentence.get(0);
                                     GenericScopeHelperClass.generatePathsForToken(test, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                     CreateAnnotations. annotateTheScopeForLEFT_NEW(test, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundNsubjOnGov,outputAnnotationType,"conj",tokenAnnotationSetOfSentence);
                                 }
                             }
                         }//found conj on gov
                         else if(foundNSubjOnConj!=null && foundNsubjOnDep == null)
                         {
                             GenericScopeHelperClass.generatePathsForToken(foundNSubjOnConj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                             GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                             GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                             GenericScopeHelperClass.generatePathsForToken(foundConjOnDobj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                             CreateAnnotations. annotateTheScopeForLEFT_NEW(foundConjOnDobj,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundNSubjOnConj,outputAnnotationType,"conj",tokenAnnotationSetOfSentence);
                             
                         }
                         else if(conjDep!=null&& conjDep.getStartNode().getOffset()< govenorToken.getStartNode().getOffset()&& conjDep.getFeatures().get("category").toString().trim().equals("PRP"))
                         {
                             GenericScopeHelperClass.generatePathsForToken(conjDep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                             GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                             GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                             CreateAnnotations. annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,conjDep,outputAnnotationType,"det",tokenAnnotationSetOfSentence);
                         }
                         else
                         {
                            
                         /***************************************************************************************/
                         GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                         GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    
                         CreateAnnotations.annotateTheScopeUsingParseTree(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType);
                         //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                         /***************************************************************************************/
                         }
                     }// include nsubjConstituent
                
                boolean doOtherScope =true;
                if(matchedDepToken.getFeatures().get("string").toString().trim().toLowerCase().startsWith("neither"))
                {
                Annotation govTokenNobj = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"nsubj",depRelationsSetOfSentence);
                if(govTokenNobj !=null && doOtherScope ==true)
                {
                    // the verb comes after the nsubj?
                    if(govTokenNobj.getStartNode().getOffset() > govenorToken.getStartNode().getOffset())
                    {
                        Annotation govTokenXCompObj = InvestigateDependencies.findDepOfGovenor(govTokenNobj, tokenAnnotationSetOfSentence,"xcomp",depRelationsSetOfSentence);
                        if(govTokenXCompObj!=null)
                        {
                            
                            GenericScopeHelperClass.generatePathsForToken(govTokenXCompObj, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(govTokenNobj, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            
                            if(GenericScopeHelperClass.annIsWithinListAlready(govTokenXCompObj,theTriggerFound) ==false && includeSpanBeforeNot==true)
                            {
                                CreateAnnotations.annotateTheScopeForDepsLeft(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govTokenXCompObj,outputAnnotationType,tokenAnnotationSetOfSentence);
                            }
                            
                            
                            CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govTokenNobj,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                            notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                            doOtherScope =false;
                        }// there is an xcomp
                        else
                        {
                            Annotation foundPrep = InvestigateDependencies.findMoreDepsForToken(govenorToken, "prep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                                if(foundPrep!=null)
                                {
                                    GenericScopeHelperClass.generatePathsForToken(foundPrep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                                    
                                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                    doOtherScope =false;
                                }
                                else
                                {
                                    
                                    GenericScopeHelperClass.generatePathsForToken(govTokenNobj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                    
                                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govTokenNobj,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                    doOtherScope =false;
                                    
                                    
                                }
                            
                            }
                        }// if nsubjGov comes after the gov token
                    }// there is a nsubj
                    else
                    {
                        // need a default case...
                        GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                        
                        CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                        notAnnotationsAlreadyAnnotated.add(matchedDepToken);

                    }
                }// is neither
                
                else if(matchedDepToken.getFeatures().get("string").toString().trim().toLowerCase().startsWith("no"))
                {
                    Annotation govTokenDobj = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"dobj",depRelationsSetOfSentence);
                    Annotation govTokenDep = InvestigateDependencies.findMoreDepsForToken(govenorToken, "dep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);

                    // check that gov of dobj is right next to the dep...
                    if(govTokenDobj !=null && govTokenDobj.getEndNode().getOffset()+1 == matchedDepToken.getStartNode().getOffset())
                    {
                        
                         GenericScopeHelperClass.generatePathsForToken(govTokenDobj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        if(GenericScopeHelperClass.annIsWithinListAlready(govTokenDobj,theTriggerFound) ==false && includeSpanBeforeNot==false)
                        {
                            CreateAnnotations.annotateTheScopeForDepsLeft(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govTokenDobj,outputAnnotationType,tokenAnnotationSetOfSentence);
                        }
                        
                        if(includeSpanBeforeNot==false)
                        {
                            Annotation foundPrep = InvestigateDependencies.findMoreDepsForToken(govenorToken, "prep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                            Annotation foundPrepOnDobjGov = InvestigateDependencies.findMoreDepsForToken(govTokenDobj, "prep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                        
                            if(foundPrep!=null)
                            {
                                GenericScopeHelperClass.generatePathsForToken(foundPrep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                doOtherScope =false;
                            }
                            else if(foundPrepOnDobjGov!=null)
                            {
                                GenericScopeHelperClass.generatePathsForToken(foundPrepOnDobjGov, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrepOnDobjGov,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                doOtherScope =false;
                            }
                            else
                            {
                                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                doOtherScope =false;
                            
                            }
                        }// includeSpan beforeNot =true
                        else if(includeSpanBeforeNot==true)
                        {
                            Annotation foundPrep = InvestigateDependencies.findMoreDepsForToken(govenorToken, "prep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                            Annotation foundPrepOnDobjGov = InvestigateDependencies.findMoreDepsForToken(govTokenDobj, "prep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                            
                            if(foundPrep!=null)
                            {
                                GenericScopeHelperClass.generatePathsForToken(foundPrep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(govTokenDobj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                CreateAnnotations. annotateTheScopeForDepsSpecialCase(govTokenDobj,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                doOtherScope =false;
                            }
                            else if(foundPrepOnDobjGov!=null)
                            {
                                GenericScopeHelperClass.generatePathsForToken(foundPrepOnDobjGov, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(govTokenDobj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                CreateAnnotations. annotateTheScopeForDepsSpecialCase(govTokenDobj,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrepOnDobjGov,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                doOtherScope =false;
                            }
                            else
                            {
                                GenericScopeHelperClass.generatePathsForToken(govTokenDobj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                CreateAnnotations. annotateTheScopeForDepsSpecialCase(govTokenDobj,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                doOtherScope =false;
                                
                            }

                        }
                        
                    }// no dobj
                    else if(govTokenDep!=null && govTokenDep.getEndNode().getOffset()+1 == matchedDepToken.getStartNode().getOffset())
                    {
                         String posTagOfGov = InvestigateConstituentsFromParseTree.findPosTag (govTokenDep,syntaxTreeNodeAnnotationSet);
                        if(posTagOfGov.startsWith("AUX"))
                        {
                            GenericScopeHelperClass.generatePathsForToken(govTokenDep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            if(GenericScopeHelperClass.annIsWithinListAlready(govTokenDep,theTriggerFound) ==false && includeSpanBeforeNot==true)
                            {
                                CreateAnnotations.annotateTheScopeForDepsLeft(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govTokenDep,outputAnnotationType,tokenAnnotationSetOfSentence);
                            }
                        }
                    }
                    Annotation govTokenNobj = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"nsubj",depRelationsSetOfSentence);
                    if(govTokenNobj !=null && doOtherScope ==true)
                    {
                        if(govTokenNobj.getStartNode().getOffset() > govenorToken.getStartNode().getOffset())
                        {
                            Annotation govTokenXCompObj = InvestigateDependencies.findDepOfGovenor(govTokenNobj, tokenAnnotationSetOfSentence,"xcomp",depRelationsSetOfSentence);
                            if(govTokenXCompObj!=null)
                            {

                                GenericScopeHelperClass.generatePathsForToken(govTokenXCompObj, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(govTokenNobj, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                                if(GenericScopeHelperClass.annIsWithinListAlready(govTokenXCompObj,theTriggerFound) ==false && includeSpanBeforeNot==true)
                                {
                                    CreateAnnotations.annotateTheScopeForDepsLeft(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govTokenXCompObj,outputAnnotationType,tokenAnnotationSetOfSentence);
                                }
                                
                                
                                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govTokenNobj,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                doOtherScope =false;
                            }
                            else
                            {
                                
                                Annotation next = tokenAnnotationSetOfSentence.get(matchedDepToken.getId()+2);
                                if(next!=null)
                                {
                                    if(next.getFeatures().get("string").toString().trim().toLowerCase().equals("one"))
                                    {
                                        GenericScopeHelperClass.generatePathsForToken(govTokenNobj, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                                        CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govTokenNobj,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govenorToken);
                                        notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                        doOtherScope =false;
                                    }
                                    else
                                    {
                                        GenericScopeHelperClass.generatePathsForToken(govTokenNobj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                                        
                                        CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govTokenNobj,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                        notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                        doOtherScope =false;
                                        
                                    }
                                }
                                else
                                {
                                    Annotation foundPrep = InvestigateDependencies.findMoreDepsForToken(govenorToken, "prep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                                    if(foundPrep!=null)
                                    {
                                        GenericScopeHelperClass.generatePathsForToken(foundPrep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                                            
                                        CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                        notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                        doOtherScope =false;
                                    }
                                    else
                                    {
                                        
                                        GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                                        
                                        CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                        notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                        doOtherScope =false;

                                        
                                    }

                                }
                                
                                
                            }
                        }// if nsubjGov comes after the gov token
                        else // if nsubj comes before///
                        {
                            Annotation foundPrep = InvestigateDependencies.findMoreDepsForToken(govenorToken, "prep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                            if(foundPrep!=null)
                            {
                                GenericScopeHelperClass.generatePathsForToken(foundPrep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                                
                                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                doOtherScope =false;
                                
                            }
                            else
                            {
                                Annotation foundPrepOnNsubj = InvestigateDependencies.findMoreDepsForToken(govTokenNobj, "prep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                                if(foundPrepOnNsubj!=null)
                                {
                                    GenericScopeHelperClass.generatePathsForToken(foundPrepOnNsubj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrepOnNsubj,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                    doOtherScope =false;
                                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                      
                                }
                                else
                                {
                                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                    doOtherScope =false;
                                    notAnnotationsAlreadyAnnotated.add(matchedDepToken); 
                                }
                            }

                            
                        }//else
                        
                    }//nsubj is there
                    
                    Annotation govTokenNobjPass = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"nsubjpass",depRelationsSetOfSentence);
                    if (govTokenNobjPass!=null && doOtherScope == true)
                    {
                        
                        if(govTokenNobjPass.getStartNode().getOffset() > govenorToken.getStartNode().getOffset())
                        {
                           GenericScopeHelperClass.generatePathsForToken(govTokenNobjPass, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                           CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govTokenNobjPass,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                           notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                           doOtherScope =false;
                         
                       }
                    }
                    
                    if(doOtherScope==true)
                    {
                        GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                        
                          Annotation foundPrep = InvestigateDependencies.findMoreDepsForToken(govenorToken, "prep", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                        Annotation foundrcMod = InvestigateDependencies.findMoreDepsForToken(govenorToken, "rcmod", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                        if(foundPrep!=null)
                        {
                            GenericScopeHelperClass.generatePathsForToken(foundPrep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                            notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                           
                        }
                        
                        else if(foundrcMod!=null)
                        {
                            GenericScopeHelperClass.generatePathsForToken(foundrcMod, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundrcMod,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                            notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                            
                        }
                        else
                        {
                            Annotation govIobj = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"iobj",depRelationsSetOfSentence);

                            if(govIobj!=null)
                            {
                                Annotation foundPDobj = InvestigateDependencies.findMoreDepsForToken(govIobj, "dobj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                                
                                if(foundPDobj!=null)
                                {
                                    GenericScopeHelperClass.generatePathsForToken(foundPDobj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                
                                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPDobj,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                    //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                }
                                // mark the verb as well?
                                
                                GenericScopeHelperClass.generatePathsForToken(govIobj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                CreateAnnotations.annotateTheScopeForDepsLeft(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govIobj,outputAnnotationType,tokenAnnotationSetOfSentence);
                                notAnnotationsAlreadyAnnotated.add(matchedDepToken); 
                            }// no indirect
                            else
                            {
                                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                            }
                        }
                      
                        
                    }
                }//outer if
                // not "no"
                //if(doOtherScope == true)
                else
                {
                                       
                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
            
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                }
            }//if check for match == true
       //} // new else
    }// end method
    /***************************************************************************
     * method name: findScopeForDepDependency()
     * this method is called from the switch case in the execute() method from 
     * the ExtractNegatorScope class.
     * function: this method takes care of the dep dependency case. 
     * Specifically, it will subdivide the more general cases into more 
     * constrained cases within (new conditions).Depending on the new constraints, 
     * this method will then call the relevent method to annotate the scope.
     *
     ****************************************************************************/
    protected static void findScopeForDepDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc, AnnotationSet depRelationsSetOfSentence,String testAnnotationType,boolean includeSubjectConstituent)
    {
        if(testAnnotationType.equals("NegTrigger"))
        {
            Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
            Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
            boolean foundExcep=false;
            
            /**** Rule 2a **/
            String posTagOfDep = InvestigateConstituentsFromParseTree.findPosTag (matchedDepToken,syntaxTreeNodeAnnotationSet);
            if((posTagOfDep.startsWith("J")==false)&&(theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
            {
                AnnotationSet testSet = tokenAnnotationSetOfSentence.get(theTriggerFound.getEndNode().getOffset()+1);
                ArrayList <Annotation> testListOfSentence= new ArrayList<Annotation>(testSet);
                if(testListOfSentence.size()!=0)
                {
                   
                    OffsetComparator comparatorTestToks = new OffsetComparator();
                    Collections.sort(testListOfSentence,comparatorTestToks);
                    Annotation test = testListOfSentence.get(0);
                    if(theTriggerFound.getFeatures().get("String").toString().trim().toLowerCase().equals("no"))
                    {
                        if (test.getFeatures().get("string").toString().trim().equals(","))
                        {
                            foundExcep =true;
                        }

                    }
                }
                if(foundExcep==false)
                {
                                
                    String posTagOfgovenor = InvestigateConstituentsFromParseTree.findPosTag (govenorToken,syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                
                    // CASE 1
                    if(posTagOfgovenor.startsWith("N") && posTagOfDep.startsWith("V")!=true)
                    {
                        if(includeSubjectConstituent == true)
                        {
                            /***************************************************************************************/
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                         
                            CreateAnnotations.annotateTheScopeUsingParseTree(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType);
                            //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                            /***************************************************************************************/
                        }
                    

                        CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                        notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    }
                    // CASE 2
                    else if(posTagOfgovenor.startsWith("V") && posTagOfDep.startsWith("V")!=true)
                    {
                        if(includeSubjectConstituent == true)
                        {
                        
                            /***************************************************************************************/
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                         
                            CreateAnnotations.annotateTheScopeUsingParseTree(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType);
                            //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                            /***************************************************************************************/
                        }
                    

                        // new
                        if(govenorToken.getStartNode().getOffset()< matchedDepToken.getStartNode().getOffset())
                        {
                            // do the left deps ... //
                            // changed - need to just annotate the verb on the left
                            CreateAnnotations.annotateTheScopeForNonDepRule(govenorToken, syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,matchedDepToken, outputAnnotationType);
                        
                        }
                    
                        CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                        notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    }
                
                    // CASE 3
                    else if (matchedDepToken.getStartNode().getOffset() < govenorToken.getStartNode().getOffset())
                    {
                        theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
                        if(theTriggerFound!=null)
                        {
                            if(includeSubjectConstituent == true)
                            {
                                /***************************************************************************************/
                                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            
                                CreateAnnotations.annotateTheScopeUsingParseTree(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType);
                                //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                /***************************************************************************************/
                            }

                           
                            if(govenorToken.getFeatures().get("string").toString().trim().toLowerCase().equals("one") && matchedDepToken.getFeatures().get("string").toString().trim().toLowerCase().equals("no"))
                            {
                                Annotation govTokenNobj = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"nsubj",depRelationsSetOfSentence);
                                if(govTokenNobj!=null)
                                {
                                    GenericScopeHelperClass.generatePathsForToken(govTokenNobj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govTokenNobj,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
        
                                }
                                else
                                {
                                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                
                                }
                            
                            }
                        
                            else
                            {
                                                        
                                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                            }
                        }// if trigger!null
                    }
                    
                } // end case 3
                
            }//if !null .. 
            /****RULE 2b ** -gov is the trigger**/
            else if ((GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false) && (govenorToken.getStartNode().getOffset() < matchedDepToken.getStartNode().getOffset()))
            {                
                
                theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
                if(theTriggerFound!=null)
                {
                    // new to not mark scope of No,...
                    AnnotationSet testSet = tokenAnnotationSetOfSentence.get(theTriggerFound.getEndNode().getOffset()+1);
                    ArrayList <Annotation> testListOfSentence= new ArrayList<Annotation>(testSet);
                    OffsetComparator comparatorTestToks = new OffsetComparator();
                    Collections.sort( testListOfSentence,comparatorTestToks);
                    Annotation test = testListOfSentence.get(0);
                    if(theTriggerFound.getFeatures().get("String").toString().trim().toLowerCase().equals("no"))
                    {
                        if (test.getFeatures().get("string").toString().trim().equals(","))
                        {
                            foundExcep =true;
                        }
                        
                    }
                    if(foundExcep==false)
                    {
                    
                        if(includeSubjectConstituent == true)
                        {
                            Annotation foundNsubjOnGov = InvestigateDependencies.findMoreDepsForToken(govenorToken, "nsubj", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                            if(foundNsubjOnGov!=null)
                            {
                                
                                /**************************************************************************************/
                                GenericScopeHelperClass.generatePathsForToken(foundNsubjOnGov, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            
                                CreateAnnotations.annotateTheScopeUsingParseTree(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundNsubjOnGov,outputAnnotationType);
                                //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                /***************************************************************************************/
                            
                            }
                            else
                            {
                                
                                /**************************************************************************************/
                                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            
                                CreateAnnotations.annotateTheScopeUsingParseTree(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType);
                                //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                            /***************************************************************************************/
                            }
                        
                        
                        }

                        String posTagOfgovenor = InvestigateConstituentsFromParseTree.findPosTag (govenorToken,syntaxTreeNodeAnnotationSet);
                        if(posTagOfgovenor.startsWith("V"))
                        {
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                            notAnnotationsAlreadyAnnotated.add(govenorToken);
                        }
                        else if(posTagOfgovenor.startsWith("N"))
                        {
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                        
                            CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                            notAnnotationsAlreadyAnnotated.add(govenorToken);
                        }
                        // new case ::: (the gov token is not...)
                        else if(govenorToken.getFeatures().get("string").toString().trim().toLowerCase().equals("not"))
                        {
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                            notAnnotationsAlreadyAnnotated.add(govenorToken);
                        
                        }
                        // new case ::: (the gov token is not...)
                        else if(govenorToken.getFeatures().get("string").toString().trim().toLowerCase().equals("except"))
                        {
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                            notAnnotationsAlreadyAnnotated.add(govenorToken);
                        
                        }
                    
                    }
                }
            }// trigger found?
                /****RULE 2c **/
            else if ((GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false) && (govenorToken.getStartNode().getOffset() > matchedDepToken.getStartNode().getOffset())&& (matchedDepToken.getFeatures().get("string").toString().trim().toLowerCase().equals("but")||matchedDepToken.getFeatures().get("root").toString().trim().toLowerCase().equals("is")))
            {
                
                theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
                if(theTriggerFound!=null)
                {
                    if(includeSubjectConstituent == true)
                    {
                            
                            /**************************************************************************************/
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            
                            CreateAnnotations.annotateTheScopeUsingParseTree(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType);
                            //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                            /***************************************************************************************/
                        
                        
                    }

                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                    notAnnotationsAlreadyAnnotated.add(govenorToken);
                }
                
            }
        }// testAnn = negTrigger
        //else if (testAnnotationType.equals("ModalityTrigger"))
        else if (testAnnotationType.equals("NegTrigger")==false)
        {
            Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
            //else if
            if (govenorToken.getStartNode().getOffset() < matchedDepToken.getStartNode().getOffset())
            {
               
                Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
                if(theTriggerFound!=null && GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false)
                {
                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                   
                    notAnnotationsAlreadyAnnotated.add(govenorToken);
                }
                
                
            }
            
        }

  
    }// end method
    /***************************************************************************
     * method name: findScopeForPobjDependency()
     * this method is called from the switch case in the execute() method from
     * the ExtractNegatorScope class.
     * function: this method takes care of the pobj dependency case. 
     * This method will then call the relevent method to annotate the scope.
     *
     ****************************************************************************/
    protected static void findScopeForPobjDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc,AnnotationSet depRelationsSetOfSentence,boolean includeSubjectConstituent)
    {
            Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
            Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
            if((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false))
            {
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                
                CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                notAnnotationsAlreadyAnnotated.add(govenorToken);
            }// if pobj
        //new could be a multi word trigger i.e with the excption of ... (of is the govenor)
        Annotation theTriggerFoundForMultiWordTrigger = GenericScopeHelperClass.findTriggerAnnotationMulti(triggerWordsAnnotationSet,govenorToken,"ID_OfLastTokenForScope");
        if((theTriggerFoundForMultiWordTrigger!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false))
        {
            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
            
            CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFoundForMultiWordTrigger,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
            notAnnotationsAlreadyAnnotated.add(govenorToken);

        }
    }
    /***************************************************************************
     * method name: findScopeForPCompDependency()
     * this method is called from the switch case in the execute() method from 
     * the ExtractNegatorScope class.
     * function: this method takes care of the pcomp dependency case. 
     * This method will then call the relevent method to annotate the scope.
     *
     ****************************************************************************/
    protected static void findScopeForPCompDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc, AnnotationSet depRelationsSetOfSentence)
    {
            Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
            Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
            if((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false))
            {
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                
                CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                notAnnotationsAlreadyAnnotated.add(govenorToken);
            }// if pcomp
        //new could be a multi word trigger i.e instead of ... (of is the govenor)
        Annotation theTriggerFoundForMultiWordTrigger = GenericScopeHelperClass.findTriggerAnnotationMulti(triggerWordsAnnotationSet,govenorToken,"ID_OfLastTokenForScope");
        if((theTriggerFoundForMultiWordTrigger!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false))
        {
            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
            
            CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFoundForMultiWordTrigger,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
            notAnnotationsAlreadyAnnotated.add(govenorToken);
            
        }

    }
    /***************************************************************************
     * method name: findScopeForXCompDependency()
     * this method is called from the switch case in the execute() method from 
     * the ExtractNegatorScope class.
     * function: this method takes care of the xcomp dependency case. 
     * This method will then call the relevent method to annotate the scope.
     *
     ****************************************************************************/
    protected static void findScopeForXCompDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc, AnnotationSet depRelationsSetOfSentence,String testAnnotationType,boolean includeSubjectConstituent)
    {
            Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
            Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
            Annotation theTriggerFoundDep = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
            // trigger is gov token
            if((theTriggerFound!=null))
            {
                 
                if(includeSubjectConstituent ==true && testAnnotationType.equals("NegTrigger"))
                {
                    Annotation foundSubjOnGov =  InvestigateDependencies.findMoreDepsForToken(govenorToken, "nsubj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);

                    
                    if(foundSubjOnGov!=null)
                    {
                            /***************************************************************************************/
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(foundSubjOnGov, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForLEFT_NEW(govenorToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundSubjOnGov,outputAnnotationType,"xcomp",tokenAnnotationSetOfSentence);
                            //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                            /***************************************************************************************/
                    }

                } // include subj=true
                if(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false)
                {
                    Annotation foundccomp = InvestigateDependencies.findMoreDepsForToken(matchedDepToken, "ccomp", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                   
                    if(foundccomp!=null&& testAnnotationType.equals("NegTrigger"))
                    {
                        GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                         GenericScopeHelperClass.generatePathsForToken(foundccomp, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundccomp,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                        notAnnotationsAlreadyAnnotated.add(govenorToken);
                    }
                    else
                    {
                        GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,matchedDepToken);
                        notAnnotationsAlreadyAnnotated.add(govenorToken);
                    }
                }
            }
        
        // new case where the LHS is the domain (the dep is the neg Trigger)
        else if (theTriggerFoundDep!=null)
        {
            if(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false)
            {
                // now test for rcmod...
                Annotation govTokenrcMod = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"rcmod",depRelationsSetOfSentence);
                Annotation foundDobj = InvestigateDependencies.findMoreDepsForToken(matchedDepToken, "dobj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                 
                // larger left span
                if(govTokenrcMod!=null && foundDobj==null)
                {
                    //TRY:: XCOMP
                    // new case June 21st
                    Annotation foundSubjOnDep =  InvestigateDependencies.findMoreDepsForToken(govTokenrcMod, "nsubj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                    
                    
                    if(foundSubjOnDep!=null && testAnnotationType.equals("NegTrigger") )
                    {
                        /***************************************************************************************/
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(govTokenrcMod, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFoundDep,foundSubjOnDep,outputAnnotationType,"xcomp",tokenAnnotationSetOfSentence);
                        //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                        /***************************************************************************************/
                    }
                    GenericScopeHelperClass.generatePathsForToken(govTokenrcMod, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDepsLeft(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFoundDep,govTokenrcMod,outputAnnotationType,tokenAnnotationSetOfSentence);
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                }
            }
        }
        
    }
    /***************************************************************************
     * method name: findScopeForAdvModDependency()
     * this method is called from the switch case in the execute() method from 
     * the ExtractNegatorScope class.
     * function: this method takes care of the advmod dependency case. 
     * This method will then call the relevent method to annotate the scope.
     *
     ****************************************************************************/
    protected static void findScopeForAdvModDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc, String inputTypeTrigger, String testAnnotationType,AnnotationSet depRelationsSetOfSentence, boolean includeSubjectConstituent)
    {
        
            Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
            Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
            if(theTriggerFound!=null && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
            {
                if(includeSubjectConstituent ==true)
                {
                    Annotation foundNsubj = InvestigateDependencies.findMoreDepsForToken(govenorToken, "nsubj", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    //There was no trace... 
                    Annotation foundExpl = InvestigateDependencies.findMoreDepsForToken(govenorToken, "expl", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    if(foundNsubj!=null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundNsubj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundNsubj,outputAnnotationType,"advmod",tokenAnnotationSetOfSentence);
                        //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                        /***************************************************************************************/
                    
                    }
                    else if(foundExpl!=null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundExpl, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundExpl,outputAnnotationType,"advmod",tokenAnnotationSetOfSentence);
                        //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                        /***************************************************************************************/
                    }
                }// includeSubj

                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                if(govenorToken.getEndNode().getOffset()> matchedDepToken.getEndNode().getOffset())
                {
                    
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                   notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                }
                // new especially relevent for self negs where the trigger comes after ie "spoke unconcernedly")
               // else if((inputTypeTrigger.equals("SelfNegOriginal")||testAnnotationType.equals("ModalityTrigger")) && govenorToken.getEndNode().getOffset()< matchedDepToken.getEndNode().getOffset())
                 else if((inputTypeTrigger.equals("SelfNegOriginal")||testAnnotationType.equals("NegTrigger")==false) && govenorToken.getEndNode().getOffset()< matchedDepToken.getEndNode().getOffset())
                {
                    Annotation govTokenrcMod = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"rcmod",depRelationsSetOfSentence);
                    if(govTokenrcMod  == null)
                    {
                        CreateAnnotations.annotateTheScopeForDepsLeft(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence);
                        notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    }
                }
                
                else if(inputTypeTrigger.equals("explicitNegTriggers")&& govenorToken.getEndNode().getOffset()< matchedDepToken.getEndNode().getOffset())
                {
                    Annotation foundCComp = InvestigateDependencies.findMoreDepsForToken(govenorToken, "ccomp", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    Annotation foundXcomp = InvestigateDependencies.findMoreDepsForToken(govenorToken, "xcomp", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    
                    Annotation foundPrep = InvestigateDependencies.findMoreDepsForToken(govenorToken, "prep", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    
                    if(foundCComp!=null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundCComp, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                       
                        CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundCComp,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                        notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    }
                    else if(foundXcomp!=null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundXcomp, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        
                        CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundXcomp,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                        notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    }
                    else if(foundPrep!=null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundPrep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        
                        CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                        notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    }


                }

             
            }// if check ...
                Annotation theTriggerFoundN = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
        if(theTriggerFoundN!=null&& (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false))
        {
            if(testAnnotationType.equals("NegTrigger")==false && testAnnotationType.equals("ModalityTrigger")==false)
            {

                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                
                Annotation foundPrepN = InvestigateDependencies.findMoreDepsForToken(govenorToken, "prep", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                
                
                if(govenorToken.getStartNode().getOffset()< matchedDepToken.getStartNode().getOffset())
                {
                    if(foundPrepN!=null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(foundPrepN, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFoundN,foundPrepN,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                    }
                    else
                    {
                        CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFoundN,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                    }
                    notAnnotationsAlreadyAnnotated.add(govenorToken);
                }
                // annotate left ... 
                else
                {
                    if(foundPrepN!=null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(foundPrepN, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFoundN,foundPrepN,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                    }
                    
                    CreateAnnotations.annotateTheScopeForDepsLeft(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFoundN,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence);
                    notAnnotationsAlreadyAnnotated.add(govenorToken);

                }
            }
        }

    }
  
    /***************************************************************************
     * method name: findScopeForAModDependency()
     * this method is called from the switch case in the execute() method from 
     * the ExtractNegatorScope class.
     * function: this method takes care of the amod dependency case. 
     * Specifically, it will subdivide the more general cases into more 
     * constrained cases within (new conditions).Depending on the new constraints, 
     * this method will then call the relevent method to annotate the scope.
     *
     ****************************************************************************/
    protected static void findScopeForAModDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc, AnnotationSet depRelationsSetOfSentence,boolean includeSubjectConstituent)
    {
            Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
            boolean toAnnotate = false;
            Annotation theTriggerFound =null;
            theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
            if(theTriggerFound!=null && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
            {
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                if(includeSubjectConstituent == true)
                {
                    // new July 8th
                    Annotation foundNsubj = InvestigateDependencies.findMoreDepsForToken(govenorToken, "nsubj", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    
                    Annotation foundDet = InvestigateDependencies.findMoreDepsForToken(govenorToken, "det", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    Annotation foundPoss = InvestigateDependencies.findMoreDepsForToken(govenorToken, "poss", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    
                    Annotation foundAdvmod = InvestigateDependencies.findMoreDepsForToken(govenorToken, "advmod", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                     
                    
                    if(foundDet !=null)
                    {
                        CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundDet,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"DT",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                        
                    }
                    else if(foundPoss !=null)
                    {
                       
                        CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundPoss,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"DT",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                        
                    }
                   
                    else if(foundAdvmod !=null && foundAdvmod.getStartNode().getOffset()< govenorToken.getStartNode().getOffset())
                    {
                       
                        CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundAdvmod,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"DT",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                        
                    }
                  
                }//if include subj
                
                Annotation foundRcMod = InvestigateDependencies.findMoreDepsForToken(govenorToken, "rcmod", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                
                Annotation foundPrepT= InvestigateDependencies.findMoreDepsForToken(govenorToken, "prep", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                Annotation foundPrepOf= InvestigateDependencies.findMoreDepsForToken(govenorToken, "prep_of", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                Annotation foundPrepOfT= InvestigateDependencies.findMoreDepsForToken(govenorToken, "prepc_of", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);

                if(foundRcMod!=null)
                {
                    GenericScopeHelperClass.generatePathsForToken(foundRcMod, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundRcMod,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null );
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    
                }
                
                else if(foundPrepT!=null&& foundPrepT.getFeatures().get("string").toString().trim().equals("of"))
                {
                    GenericScopeHelperClass.generatePathsForToken(foundPrepT, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrepT,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null );
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    
                }
                else if(foundPrepOf!=null)
                {
                    GenericScopeHelperClass.generatePathsForToken(foundPrepOf, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrepOf,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null );
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    
                }
                else if(foundPrepOfT!=null)
                {
                    GenericScopeHelperClass.generatePathsForToken(foundPrepOfT, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrepOfT,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null );
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    
                }
                else
                {
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null );
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                }
            }
            else if ((matchedDepToken.getFeatures().get("string").toString().trim().endsWith("ed"))&& (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
                
            {
                theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
                if(theTriggerFound!=null)
                {
                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    
                    CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null  );
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                }
            }
        // could be that the trigger is in a conj_and relation with the matchedDeptoken (so should take the amod relation)
        else
        {
            Annotation foundConjAnd = InvestigateDependencies.findMoreDepsForToken(matchedDepToken, "conj", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
            if(foundConjAnd !=null)
            {
                
                 Annotation theTriggerFoundNew = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,foundConjAnd);
                if(theTriggerFoundNew!=null && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, foundConjAnd)==false))
                {
                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(foundConjAnd, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    if(includeSubjectConstituent==true)
                    {

                        
                        Annotation foundDet = InvestigateDependencies.findMoreDepsForToken(govenorToken, "det", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                        Annotation foundPoss = InvestigateDependencies.findMoreDepsForToken(govenorToken, "poss", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                        
                        Annotation foundAdvmod = InvestigateDependencies.findMoreDepsForToken(govenorToken, "advmod", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                        if(foundDet !=null)
                        {
                            CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundDet,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFoundNew,"DT",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                            
                            CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(govenorToken,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFoundNew,"N",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                            
                        }
                      
                        else if(foundPoss !=null)
                        {
                            
                            CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundPoss,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFoundNew,"DT",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                            
                        }
                        
                    }
                    
                    
                    
                    CreateAnnotations.annotateTheScopeForDeps(foundConjAnd,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFoundNew,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null   );
                    notAnnotationsAlreadyAnnotated.add(foundConjAnd);

                }
            }

            
        }

    }
     /***************************************************************************************************************************************************/
    protected static void findScopeForACompDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc, AnnotationSet depRelationsSetOfSentence,boolean includeSubjectConstituent,String testAnnotationType)
    {
        if(testAnnotationType.equals("NegTrigger"))
        {
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        boolean toAnnotate = false;
        Annotation theTriggerFound =null;
        Annotation foundxcomp = InvestigateDependencies.findMoreDepsForToken(matchedDepToken, "xcomp", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
        theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
        
        if(theTriggerFound!=null && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false)&& matchedDepToken.getFeatures().get("string").toString().trim().toLowerCase().equals("unable")==false && matchedDepToken.getFeatures().get("string").toString().trim().toLowerCase().equals("impossible")==false && matchedDepToken.getFeatures().get("string").toString().trim().toLowerCase().equals("uncertain")==false)

        {
            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
            
            if(includeSubjectConstituent == true)
            {
                Annotation foundDet = InvestigateDependencies.findMoreDepsForToken(govenorToken, "det", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                Annotation foundPoss = InvestigateDependencies.findMoreDepsForToken(govenorToken, "poss", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                
                Annotation foundAdvmod = InvestigateDependencies.findMoreDepsForToken(govenorToken, "advmod", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                Annotation foundnsubj = InvestigateDependencies.findMoreDepsForToken(govenorToken, "nsubj", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                
                if(foundnsubj !=null)
                {
                    /***************************************************************************************/
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(foundnsubj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundnsubj,outputAnnotationType,"acomp",tokenAnnotationSetOfSentence);
                    //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    /***************************************************************************************/

                    
                }
                // new MArch 12
                else if(foundDet !=null)
                {
                    CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundDet,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"DT",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                    
                }
               
                else if(foundPoss !=null)
                {
                    CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundPoss,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"DT",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                    
                }
               
                else if(foundAdvmod !=null && foundAdvmod.getStartNode().getOffset()< govenorToken.getStartNode().getOffset())
                {
                    CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundAdvmod,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"DT",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                    
                }
               
            }//if include
            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
            CreateAnnotations.annotateTheScopeForDepsLeft(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence);
            notAnnotationsAlreadyAnnotated.add(matchedDepToken);
        }
        }// is negTrigger
       // else if (testAnnotationType.equals("ModalityTrigger"))
        else if (testAnnotationType.equals("NegTrigger") ==false)
        {
            Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
            Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
            if(theTriggerFound!=null && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false))
            {
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null   );
                notAnnotationsAlreadyAnnotated.add(govenorToken);
            }
            
        }
        
            
    }

    /***************************************************************************
     * method name: findScopeForInfModDependency()
     * this method is called from the switch case in the execute() method from
     * the ExtractNegatorScope class.
     * function: this method takes care of the infmod dependency case.
     * Specifically, it will subdivide the more general cases into more 
     * constrained cases within (new conditions).Depending on the new constraints, 
     * this method will then call the relevent method to annotate the scope.
     *
     ****************************************************************************/
    protected static void findScopeForInfModDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc,AnnotationSet depRelationsSetOfSentence,boolean includeSubjectConstituent)
    {
            Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
            Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
            if((theTriggerFound!=null) &&(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false))
            {
                if(includeSubjectConstituent ==true)
                {
               
                /**************************************************************************************/
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                
                CreateAnnotations.annotateTheScopeUsingParseTree(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType);
                //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                /***************************************************************************************/
                }
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);

                String posTagOfDep = InvestigateConstituentsFromParseTree.findPosTag (matchedDepToken,syntaxTreeNodeAnnotationSet);
                
                if(posTagOfDep.startsWith("V"))
                {
                    CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                    notAnnotationsAlreadyAnnotated.add(govenorToken);
                }
                else if(posTagOfDep.startsWith("N"))
                {
                    CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                    notAnnotationsAlreadyAnnotated.add(govenorToken);
                    
                }
            }
    }
    /***************************************************************************
     * method name: findScopeForPrepDependency()**** not collapsed option
     * this method is called from the switch case in the execute() method from 
     * the ExtractNegatorScope class.
     * function: this method takes care of the prep dependency case. 
     * Specifically, it will subdivide the more general cases into more 
     * constrained cases within (new conditions).Depending on the new constraints, 
     * this method will then call the relevent method to annotate the scope.
     *
     ****************************************************************************/
    protected static void findScopeForPrepDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc,AnnotationSet depRelationsSetOfSentence, String inputTypeTrigger,boolean includeSubjectConstituent)
    {
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        if(matchedDepToken.getId() > govenorToken.getId())
        {
            //if(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false)
            //{
                Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
                Annotation theTriggerFoundForGov = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
                
            if(theTriggerFound!=null && GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false)
                {
                    // dependent has a dep relation ...
                    Annotation foundDep = InvestigateDependencies.findMoreDepsForToken(matchedDepToken, "dep", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    if(foundDep!=null)
                    {
                         Annotation foundPobj = InvestigateDependencies.findMoreDepsForToken(foundDep, "pobj", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                          // that dep has a pobj relation
                        if(foundPobj!=null)
                        {
                            
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(foundPobj, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPobj,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                            notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                        }
                    }
                  
                }
                      
            else if(theTriggerFoundForGov!=null && GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false)
            {
                if(includeSubjectConstituent==true)
                {
                    if(inputTypeTrigger.equals("SelfNegOriginal"))
                    {
                        Annotation foundNsubjOnGov = InvestigateDependencies.findMoreDepsForToken(govenorToken, "nsubj", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                        Annotation foundNsubjPassOnGov = InvestigateDependencies.findMoreDepsForToken(govenorToken, "nsubjpass", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                        if(foundNsubjOnGov!=null)
                        {
                            
                            GenericScopeHelperClass.generatePathsForToken(foundNsubjOnGov, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            
                            CreateAnnotations.annotateTheScopeForLEFT_NEW(govenorToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFoundForGov,foundNsubjOnGov,outputAnnotationType,"prep",tokenAnnotationSetOfSentence);
                            
                        }
                        else if(foundNsubjPassOnGov!=null)
                        {
                            
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(foundNsubjPassOnGov, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            
                            CreateAnnotations.annotateTheScopeForLEFT_NEW(govenorToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFoundForGov,foundNsubjPassOnGov,outputAnnotationType,"prep",tokenAnnotationSetOfSentence);
                            
                        }

                    
                    }
                }
                
                    Annotation foundPobj = InvestigateDependencies.findMoreDepsForToken(matchedDepToken, "pobj", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    // that dep has a pobj relation
                    if(foundPobj!=null)
                    {
                        

                        Annotation foundNsubjPass = InvestigateDependencies.findDepOfGovenor(govenorToken,tokenAnnotationSetOfSentence,"nsubjpass",depRelationsSetOfSentence);
                         Annotation foundNsubj = InvestigateDependencies.findDepOfGovenor(govenorToken,tokenAnnotationSetOfSentence,"nsubj",depRelationsSetOfSentence);
                        
                        if(foundNsubjPass!=null && foundNsubjPass.getStartNode().getOffset()> govenorToken.getStartNode().getOffset())
                        {
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(foundNsubjPass, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFoundForGov,foundNsubjPass,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                            notAnnotationsAlreadyAnnotated.add(govenorToken);

                        }
                        
                        else if(foundNsubj!=null && foundNsubj.getStartNode().getOffset()> govenorToken.getStartNode().getOffset())
                        {
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(foundNsubj, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFoundForGov,foundNsubj,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                            notAnnotationsAlreadyAnnotated.add(govenorToken);
                            
                        }
                        else
                        {
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(foundPobj, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFoundForGov,foundPobj,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                            notAnnotationsAlreadyAnnotated.add(govenorToken);
                        }
                    }
            }
        }
    }

    /***************************************************************************
     * method name: findScopeForPrepGenericDependency()** for collapsed deps...
     * this method is called from the switch case in the execute() method from 
     * the ExtractNegatorScope class.
     * function: this method takes care of the prep dependency case. 
     * Specifically, it will subdivide the more general cases into more 
     * constrained cases within (new conditions).Depending on the new constraints, 
     * this method will then call the relevent method to annotate the scope.
     *
     ****************************************************************************/
    protected static void findScopeForPrep_GenericDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc,AnnotationSet depRelationsSetOfSentence, String inputTypeTrigger, String depName,String testAnnotationType, boolean includeSubjectConstituent)
    {
        if(testAnnotationType.equals("NegTrigger"))
        {
        if (inputTypeTrigger.equals("SelfNegOriginal") || depName.endsWith("of") || depName.endsWith("to") || depName.endsWith("in")||depName.endsWith("for")||(depName.endsWith("with"))||(depName.endsWith("at")))
        {
            if(tokenAnnotationSetOfSentence!=null){
            Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        if(matchedDepToken!=null){
            if (matchedDepToken.getId() > govenorToken.getId())
            {                
                if(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false)                {
                    // is a mulitnegTrigger gov = exception, 
                    if (govenorToken.getFeatures().get("string").toString().trim().toLowerCase().equals("exception"))
                    {
                         Annotation foundParentPWithobj = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"prep_with",depRelationsSetOfSentence);
                        if(foundParentPWithobj!=null)
                        {
                        // get the "with"
                            Annotation theWith = tokenAnnotationSetOfSentence.get(govenorToken.getId()-4);
                            Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotationMulti(triggerWordsAnnotationSet,theWith,"ID_OfFirstTokenForScope");
                            if(theTriggerFound!=null)
                            {
                                if(includeSubjectConstituent ==true)
                                {
                                
                                /***************************************************************************************/
                                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                
                                CreateAnnotations.annotateTheScopeUsingParseTree(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType);
                                //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                                /***************************************************************************************/
                                }
                                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            
                                String idToCheckForLast = theTriggerFound.getFeatures().get("ID_OfLastTokenForScope").toString().trim(); 
                                Annotation testLastToken = tokenAnnotationSetOfSentence.get(Integer.valueOf(idToCheckForLast));
                                if(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false)
                                {
                                    GenericScopeHelperClass.generatePathsForToken(foundParentPWithobj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                     GenericScopeHelperClass.generatePathsForToken(testLastToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                                    CreateAnnotations.annotateTheScopeForDeps(testLastToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                     notAnnotationsAlreadyAnnotated.add(govenorToken);
                                }
                            }// triggerFound
                        }// parentWith
                    }// is exception
                    // not is exception
                    else if(inputTypeTrigger.equals("SelfNegOriginal") || depName.endsWith("to") || depName.endsWith("in")||depName.endsWith("for"))
                    {
                        Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
                        if(theTriggerFound!=null)
                        {
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null   );
                            notAnnotationsAlreadyAnnotated.add(govenorToken);
                        }
                    }
                   
                    else if (depName.endsWith("of"))
                    {
                        
                        if(govenorToken.getFeatures().get("string").toString().trim().toLowerCase().equals("none")||govenorToken.getFeatures().get("string").toString().trim().toLowerCase().equals("neither"))
                        {
                            Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
                            if(theTriggerFound!=null)
                            {
                                
                                Annotation foundNsubjPassGov = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"nsubjpass",depRelationsSetOfSentence);
                                Annotation foundNsubjGov = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"nsubj",depRelationsSetOfSentence);
                                if(foundNsubjPassGov !=null)
                                {
                                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                    GenericScopeHelperClass.generatePathsForToken(foundNsubjPassGov, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                                    CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundNsubjPassGov,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                    notAnnotationsAlreadyAnnotated.add(govenorToken);
                                }
                                else if(foundNsubjGov!=null)
                                {
                                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                    GenericScopeHelperClass.generatePathsForToken(foundNsubjGov, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                                    CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundNsubjGov,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                    notAnnotationsAlreadyAnnotated.add(govenorToken);
                                }
                                // is still none
                                else
                                {
                                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                                    CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                    notAnnotationsAlreadyAnnotated.add(govenorToken);
                                }
                            
                            }// triggerFound!=null
                        } // is none
                        
                        // other of cases
                        else
                        {
                            // new check if instead of is within the span between gov and dep ... (prep_of...)
                            long startTestOffset = govenorToken.getStartNode().getOffset();
                            long endTestOffset = matchedDepToken.getEndNode().getOffset();
                            AnnotationSet testSpan = tokenAnnotationSetOfSentence.getContained(startTestOffset, endTestOffset);
                            List <Annotation> testSpanAsList = new ArrayList<Annotation>(testSpan);
                            OffsetComparator offsetComparator = new OffsetComparator();
                            Collections.sort(testSpanAsList, offsetComparator);
                            boolean triggerFound =false;
                            int index =0;
                            boolean done =false;
                            while (triggerFound == false && index < testSpanAsList.size())
                            {
                                String testForTrigger = testSpanAsList.get(index).getFeatures().get("string").toString().trim();
                                if(index < testSpanAsList.size()-1)
                                {
                                    String testForTriggerT = testSpanAsList.get(index+1).getFeatures().get("string").toString().trim();
                                    if(testForTrigger.toLowerCase().equals("instead") && testForTriggerT.toLowerCase().equals("of"))
                                    {
                                        Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotationMulti(triggerWordsAnnotationSet,testSpanAsList.get(index),"ID_OfFirstTokenForScope");
                                        if (theTriggerFound!=null && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, testSpanAsList.get(index))==false))
                                        {
                                            String idToCheckForLast = theTriggerFound.getFeatures().get("ID_OfLastTokenForScope").toString().trim(); 
                                            Annotation testLastToken = tokenAnnotationSetOfSentence.get(Integer.valueOf(idToCheckForLast));
                                            if(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, testLastToken)==false)
                                            {
                                                // have our triggerFound
                    
                                                triggerFound = true;
                                                GenericScopeHelperClass.generatePathsForToken(testLastToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                                                CreateAnnotations.annotateTheScopeForDeps(testLastToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null   );
                                                notAnnotationsAlreadyAnnotated.add(testSpanAsList.get(index));
                                                done = true;
                                            }
                                        }
                                        else
                                        {
                                            index++;
                                        }
                                    }
                                    else
                                    {
                                        index++;
                                    }
                                }// new if ...
                                else
                                {
                                    index++;
                                }
                            }// while

                            if(done ==false)
                            {
                                Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
                                if(theTriggerFound!=null)
                                {
                                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                                    CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null   );
                                    notAnnotationsAlreadyAnnotated.add(govenorToken);
                                }
                            }
                        }
                        
                    } // is of
                    
                    
                }// is the gov id
                // new ...
                if(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false && inputTypeTrigger.equals("explicitNegTriggers")==false && inputTypeTrigger.equals("implicitNegTriggers")==false)
                {
                    // check that this token is not the gov of a prep...
                     Annotation foundOtherPrep= InvestigateDependencies.findMoreDepsForToken(matchedDepToken, "prep", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    
                    Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
                    if(theTriggerFound!=null && foundOtherPrep==null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForDepsLeft(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence);
                        notAnnotationsAlreadyAnnotated.add(govenorToken);
                    }

                }
            }//dep > gov id
            }
            }
        }// the trigger type...
        
        else if (depName.equals("prep_than")||depName.equals("prepc_than"))
        {
                Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
                
                long startTestOffset = govenorToken.getStartNode().getOffset();
                long endTestOffset = matchedDepToken.getEndNode().getOffset();
                AnnotationSet testSpan = tokenAnnotationSetOfSentence.getContained(startTestOffset, endTestOffset);
                List <Annotation> testSpanAsList = new ArrayList<Annotation>(testSpan);
                OffsetComparator offsetComparator = new OffsetComparator();
                Collections.sort(testSpanAsList, offsetComparator);
                boolean triggerFound =false;
                int index =0;
                while (triggerFound == false && index < testSpanAsList.size())
                {
                    String testForTrigger = testSpanAsList.get(index).getFeatures().get("string").toString().trim();
                    Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotationMulti(triggerWordsAnnotationSet,testSpanAsList.get(index),"ID_OfFirstTokenForScope");
                    if (theTriggerFound!=null && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, testSpanAsList.get(index))==false))
                    {
                        String idToCheckForLast = theTriggerFound.getFeatures().get("ID_OfLastTokenForScope").toString().trim(); 
                        Annotation testLastToken = tokenAnnotationSetOfSentence.get(Integer.valueOf(idToCheckForLast));
                        if(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, testLastToken)==false)
                        {
                            // have our triggerFound
                            triggerFound = true;
                            GenericScopeHelperClass.generatePathsForToken(testLastToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForDeps(testLastToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                            notAnnotationsAlreadyAnnotated.add(testSpanAsList.get(index));
                        }
                    }
                    else
                    {
                        index++;
                    }
                }// while
            //}
        }
         else if (depName.equals("prepc_instead_of")||depName.equals("prep_instead_of") )
            
        {
            Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
            
            long startTestOffset = govenorToken.getStartNode().getOffset();
            long endTestOffset = matchedDepToken.getEndNode().getOffset();
            AnnotationSet testSpan = tokenAnnotationSetOfSentence.getContained(startTestOffset, endTestOffset);
            List <Annotation> testSpanAsList = new ArrayList<Annotation>(testSpan);
            OffsetComparator offsetComparator = new OffsetComparator();
            Collections.sort(testSpanAsList, offsetComparator);
            boolean triggerFound =false;
            int index =0;
            while (triggerFound == false && index < testSpanAsList.size())
            {
                String testForTrigger = testSpanAsList.get(index).getFeatures().get("string").toString().trim();
                Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotationMulti(triggerWordsAnnotationSet,testSpanAsList.get(index),"ID_OfFirstTokenForScope");
                if (theTriggerFound!=null && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, testSpanAsList.get(index))==false))
                {
                    String idToCheckForLast = theTriggerFound.getFeatures().get("ID_OfLastTokenForScope").toString().trim(); 
                    Annotation testLastToken = tokenAnnotationSetOfSentence.get(Integer.valueOf(idToCheckForLast));
                    if(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, testLastToken)==false)
                    {
                        // have our triggerFound
                        triggerFound = true;
                        GenericScopeHelperClass.generatePathsForToken(testLastToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForDeps(testLastToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                        notAnnotationsAlreadyAnnotated.add(testSpanAsList.get(index));
                    }
                }
                else
                {
                    index++;
                }
            }// while
           
        }

        
        else if (depName.equals("prep_without")||depName.equals("prepc_without"))
        {
            Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
            long startTestOffset = govenorToken.getStartNode().getOffset();
            long endTestOffset = matchedDepToken.getEndNode().getOffset();
            AnnotationSet testSpan = tokenAnnotationSetOfSentence.getContained(startTestOffset, endTestOffset);
            List <Annotation> testSpanAsList = new ArrayList<Annotation>(testSpan);
            OffsetComparator offsetComparator = new OffsetComparator();
            Collections.sort(testSpanAsList, offsetComparator);
            
            Annotation triggerToken =null;
            for(Annotation current: testSpanAsList)
            {
                if(current.getFeatures().get("string").toString().trim().toLowerCase().equals("without"))
                {
                    triggerToken = current;
                    break;
                }
            }
            if(triggerToken!=null)
            {
                Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,triggerToken);
                if(theTriggerFound!=null)
                {
                    if(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, triggerToken)==false)
                    {
                        
                         ArrayList<Annotation> foundOtherPrepOnGovList= InvestigateDependencies.findMoreDepsForTokenList(govenorToken, "prep", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                        Annotation foundOtherPrepOnGov =null;
                        for(Annotation cFoundOtherPrepOnGovList:foundOtherPrepOnGovList)
                        {
                            if((cFoundOtherPrepOnGovList.equals(matchedDepToken)==false)&& (cFoundOtherPrepOnGovList.getStartNode().getOffset()> triggerToken.getStartNode().getOffset()))
                            {
                                foundOtherPrepOnGov= cFoundOtherPrepOnGovList;
                                
                            }
                        }
                        // new case
                        if(foundOtherPrepOnGov!=null)
                        {
                            GenericScopeHelperClass.generatePathsForToken(triggerToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(foundOtherPrepOnGov, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForDeps(triggerToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundOtherPrepOnGov,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                            notAnnotationsAlreadyAnnotated.add(triggerToken);
                            
                        }
                        // default old case
                        else
                        {
                        
                            GenericScopeHelperClass.generatePathsForToken(triggerToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForDeps(triggerToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                            notAnnotationsAlreadyAnnotated.add(triggerToken);
                        }
                    }
                    
                    
                }
                
            }//triggerToken

        }// prep_without
        else if (depName.equals("prep_against"))
        {
            Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
            long startTestOffset = govenorToken.getStartNode().getOffset();
            long endTestOffset = matchedDepToken.getEndNode().getOffset();
            AnnotationSet testSpan = tokenAnnotationSetOfSentence.getContained(startTestOffset, endTestOffset);
            List <Annotation> testSpanAsList = new ArrayList<Annotation>(testSpan);
            OffsetComparator offsetComparator = new OffsetComparator();
            Collections.sort(testSpanAsList, offsetComparator);
        
            Annotation triggerToken =null;
             for(Annotation current: testSpanAsList)
            {
               if(current.getFeatures().get("string").toString().trim().toLowerCase().equals("against"))
               {
                   triggerToken = current;
                   break;
               }
            }
                if(triggerToken!=null)
                {
                    Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,triggerToken);  
                    if(theTriggerFound!=null)
                    {
                        if(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, triggerToken)==false)
                        {
                            GenericScopeHelperClass.generatePathsForToken(triggerToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForDeps(triggerToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                            notAnnotationsAlreadyAnnotated.add(triggerToken);
                        }
                        
                        
                    }
                    
            }//triggerToken

        }// prep_against
        }// negTrigger
        //else if (testAnnotationType.equals("ModalityTrigger"))
       else if (testAnnotationType.equals("NegTrigger")==false)
        {
            if (depName.endsWith("for"))
            {
                Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
                
                if(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false)
                {
                    Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
                    if(theTriggerFound!=null)
                    {
                        Annotation foundInfMod =  InvestigateDependencies.findMoreDepsForToken(matchedDepToken, "infmod", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                        
                        if(foundInfMod!=null)
                        {
                           
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(foundInfMod, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundInfMod,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null   );
                            notAnnotationsAlreadyAnnotated.add(govenorToken);
                            
                            
                        }
                        // no infmod ....
                        else
                        {
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null   );
                            notAnnotationsAlreadyAnnotated.add(govenorToken);
                            
                            
                        }
                    }
                }
            }// is a prep_for
            else if (depName.endsWith("of")|| (depName.endsWith("to"))||(depName.endsWith("in"))||(depName.endsWith("by"))||(depName.endsWith("from"))||(depName.endsWith("as")))
            {
                Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
                Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
                if(theTriggerFound!=null &&(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false))
                {
                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null   );
                    notAnnotationsAlreadyAnnotated.add(govenorToken);
                    
                   
                    
                }
                
               
                else if((GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false) && (depName.endsWith("by")))
                    
                {
                   
                    Annotation theTriggerFoundTwo = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
                    Annotation foundCCComp=  InvestigateDependencies.findMoreDepsForToken(govenorToken, "ccomp", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    if(foundCCComp!=null && theTriggerFoundTwo!=null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(foundCCComp, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFoundTwo,foundCCComp,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null   );
                        notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                        
                        
                    }
                }
                
            }// of/by/in/to
        } //modality trigger

    }// method

    /***************************************************************************
     * method name: findScopeForCCDependency()
     * generic coordination dependency relation
     *
     ****************************************************************************/
    protected static void findScopeForCCDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc    )
    {
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotationMulti(triggerWordsAnnotationSet,matchedDepToken,"ID_OfFirstTokenForScope");
        Annotation theTriggerFoundSingle =  GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
        
        if((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
        {
             String idToCheckForLast = theTriggerFound.getFeatures().get("ID_OfLastTokenForScope").toString().trim();
             Annotation testLastToken = tokenAnnotationSetOfSentence.get(Integer.valueOf(idToCheckForLast));
             if(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, testLastToken)==false)
             {
                 Annotation foundConjObject = InvestigateDependencies.findMoreDepsForToken(govenorToken, "conj", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                 if(foundConjObject!=null)
                 {
                    Annotation foundPrepObject = InvestigateDependencies.findMoreDepsForToken(foundConjObject, "prep", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                     if(foundPrepObject!=null)
                     {
                         GenericScopeHelperClass.generatePathsForToken(foundConjObject, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                         GenericScopeHelperClass.generatePathsForToken(foundPrepObject, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                         CreateAnnotations.annotateTheScopeForDeps(foundConjObject,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrepObject,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                         notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    
                     }
                
                     else
                     {
                         // no prep
                         CreateAnnotations.annotateTheScopeForMultiCase(foundConjObject,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,govenorToken,outputAnnotationType);
                         notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                     }
                 }//foundConjObj!=null
             }// new test
            
        }// trigger!=null
        else if((theTriggerFoundSingle!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false)&&matchedDepToken.getFeatures().get("string").toString().trim().toLowerCase().equals("nor"))
        {
            CreateAnnotations.annotateTheScopeForNeither(matchedDepToken,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,outputAnnotationType,theTriggerFoundSingle,tokenAnnotationSetOfSentence);
            notAnnotationsAlreadyAnnotated.add(matchedDepToken);
        }
        
    }
    /***************************************************************************
     * method name: markNeither
     * this method is called from the case where the trigger is for sure only a 
     * "negTrigger" in the execute() method from the ExtractNegatorScope class.
     * function: this method takes care of the "neither" pattern.
     ****************************************************************************/
    protected static void markNeither(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet, AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,Annotation govenorToken,Integer depID,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc,boolean includeSubjectConstituent)
	{
        // this case just does neither
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        Annotation theTriggerFound =  GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
        // new:: check if govenor token is a nsubjPass:: or noun subj:::
        Annotation govTokenSubjPass = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"nsubjpass",depRelationsSetOfSentence);
        Annotation govTokenSubj = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"nsubj",depRelationsSetOfSentence);
        
        if(matchedDepToken.getFeatures().get("string").toString().trim().toLowerCase().equals("neither") &&  (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false) && govTokenSubjPass!=null )
        {
            GenericScopeHelperClass.generatePathsForToken(govTokenSubjPass, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
            CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govTokenSubjPass,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govTokenSubjPass    );
            notAnnotationsAlreadyAnnotated.add(matchedDepToken);
        }
        else if(matchedDepToken.getFeatures().get("string").toString().trim().toLowerCase().equals("neither") &&  (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false) && govTokenSubj!=null )
        {
            GenericScopeHelperClass.generatePathsForToken(govTokenSubj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
            CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govTokenSubj,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govTokenSubj);
            notAnnotationsAlreadyAnnotated.add(matchedDepToken);
        }
        else if(matchedDepToken.getFeatures().get("string").toString().trim().toLowerCase().equals("neither") &&  (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
        {
            if(includeSubjectConstituent ==true)
            {
                Annotation foundSubj= InvestigateDependencies.findMoreDepsForToken(govenorToken,"nsubj",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                
                Annotation foundExpl= InvestigateDependencies.findMoreDepsForToken(govenorToken,"expl",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                
                Annotation foundSubjPass = InvestigateDependencies.findMoreDepsForToken(govenorToken,"nsubjpass",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                
                Annotation govTokenISAdobj = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"dobj",depRelationsSetOfSentence);
                if(foundSubj !=null)
                {
                    
                    GenericScopeHelperClass.generatePathsForToken(foundSubj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    
                    CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundSubj,outputAnnotationType,"neither",tokenAnnotationSetOfSentence);
                    
                }
                else if(foundExpl !=null)
                {
                    
                    GenericScopeHelperClass.generatePathsForToken(foundExpl, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    
                    CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundExpl,outputAnnotationType,"neither",tokenAnnotationSetOfSentence);
                   
                }
                else if(foundSubjPass !=null)
                {
                   
                    GenericScopeHelperClass.generatePathsForToken(foundSubjPass, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    
                    CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundSubjPass,outputAnnotationType,"neither",tokenAnnotationSetOfSentence);
                    
                }
                else if(govTokenISAdobj!=null)
                {
                     Annotation conjTest = InvestigateDependencies.findDepOfGovenor(govTokenISAdobj, tokenAnnotationSetOfSentence,"conj",depRelationsSetOfSentence);
                    if(conjTest!=null)
                    {
                         Annotation foundSubjOnCongGov = InvestigateDependencies.findMoreDepsForToken(conjTest,"nsubj",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                        if(foundSubjOnCongGov!=null)
                        {
                            // mark the verb
                        CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(govTokenISAdobj,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"V",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                        // just mark the subj
                       
                        GenericScopeHelperClass.generatePathsForToken(foundSubjOnCongGov, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(conjTest, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        CreateAnnotations. annotateTheScopeForLEFT_NEW(conjTest,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundSubjOnCongGov,outputAnnotationType,"conj",tokenAnnotationSetOfSentence);
                        }
                        
                    }
                   
                    
                }

            }// end include subj

            
            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
            
            CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
            notAnnotationsAlreadyAnnotated.add(matchedDepToken);

            notAnnotationsAlreadyAnnotated.add(matchedDepToken);
        }

        
    }// method
    /***************************************************************************
     * method name: markNor**NEW CASE() - from conj_nor relation
     * this method is called from the case where the trigger is for sure only a 
     * "negTrigger" in the execute() method from the ExtractNegatorScope class.
     * function: this method takes care of the "nor" pattern. 
     ****************************************************************************/
    protected static void markConjNor(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet, AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,Annotation govenorToken,Integer depID,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc    )
	{
        // this case just does neither
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        int syntaxNodeID = InvestigateConstituentsFromParseTree.findSyntaxNode (matchedDepToken,syntaxTreeNodeAnnotationSet);
        long startTestOffset = govenorToken.getStartNode().getOffset();
        long endTestOffset = matchedDepToken.getEndNode().getOffset();
        AnnotationSet testSpan = tokenAnnotationSetOfSentence.getContained(startTestOffset, endTestOffset);
        List <Annotation> testSpanAsList = new ArrayList<Annotation>(testSpan);
        OffsetComparator offsetComparator = new OffsetComparator();
        Collections.sort(testSpanAsList, offsetComparator);
        
       
        Annotation triggerToken =null;
        for(Annotation current: testSpanAsList)
        {
            if(current.getFeatures().get("string").toString().trim().toLowerCase().equals("nor"))
            {
                triggerToken = current;
                break;
            }
        }
        
        if(triggerToken!=null)
        {
            Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,triggerToken);  
            if(theTriggerFound!=null)
            {
                
                
                Annotation govTokenSubjPass = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"nsubjpass",depRelationsSetOfSentence);
                Annotation govTokenSubj = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"nsubj",depRelationsSetOfSentence);
                if (govTokenSubjPass!=null)
                {
                    GenericScopeHelperClass.generatePathsForToken(govTokenSubjPass, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(triggerToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(triggerToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govTokenSubjPass,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govTokenSubjPass    );
                    notAnnotationsAlreadyAnnotated.add(triggerToken);
                }
                else if(govTokenSubj!=null)
                {
                    GenericScopeHelperClass.generatePathsForToken(govTokenSubj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(triggerToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(triggerToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govTokenSubj,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,govTokenSubj);
                    notAnnotationsAlreadyAnnotated.add(triggerToken);
                }
                
                
                else if(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, triggerToken)==false)
                {
                    GenericScopeHelperClass.generatePathsForToken(triggerToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(triggerToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                    notAnnotationsAlreadyAnnotated.add(triggerToken);
                }
                
                
            }
            
        }//triggerToken
    }// method

    /***************************************************************************
     * NEW:: method name: findScopeFor_ConjNegCC_Dependency() ** for collapsedDeps...
     * 
     *
     ****************************************************************************/
    protected static void findScopeForConjNegCCDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc, boolean includeSubjectConstituent)
    {
       
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        
        long startTestOffset = govenorToken.getStartNode().getOffset();
        long endTestOffset = matchedDepToken.getEndNode().getOffset();
        AnnotationSet testSpan = tokenAnnotationSetOfSentence.getContained(startTestOffset, endTestOffset);
        List <Annotation> testSpanAsList = new ArrayList<Annotation>(testSpan);
        OffsetComparator offsetComparator = new OffsetComparator();
        Collections.sort(testSpanAsList, offsetComparator);


        boolean triggerFound =false;
        int index =0;
        while (triggerFound == false && index < testSpanAsList.size())
        {
            String testForTrigger = testSpanAsList.get(index).getFeatures().get("string").toString().trim();
            Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotationMulti(triggerWordsAnnotationSet,testSpanAsList.get(index),"ID_OfFirstTokenForScope");
            if (theTriggerFound!=null && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, testSpanAsList.get(index))==false))
            {
                String idToCheckForLast = theTriggerFound.getFeatures().get("ID_OfLastTokenForScope").toString().trim(); 
                Annotation testLastToken = tokenAnnotationSetOfSentence.get(Integer.valueOf(idToCheckForLast));
                if(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, testLastToken)==false)
                {
                    // have our triggerFound
                    triggerFound = true;
                    Annotation foundPrepObject = InvestigateDependencies.findMoreDepsForToken(matchedDepToken, "prep", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    if(foundPrepObject!=null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(testLastToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundPrepObject, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForDeps(testLastToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrepObject,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                        notAnnotationsAlreadyAnnotated.add(testSpanAsList.get(index));
                        
                    }
                    
                    else
                    {
                        GenericScopeHelperClass.generatePathsForToken(testLastToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForDeps(testLastToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                        notAnnotationsAlreadyAnnotated.add(testSpanAsList.get(index));
                    }

                }
            }
            else
            {
                index++;
            }
        }// while
        if(triggerFound ==false)
        {
            Annotation triggerToken =null;
            for(Annotation current: testSpanAsList)
            {
                if(current.getFeatures().get("string").toString().trim().toLowerCase().equals("not"))
                {
                    triggerToken = current;
                    break;
                }
            }
            if(triggerToken!=null)
            { 
                Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,triggerToken);
                if ((theTriggerFound!=null)&&(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, triggerToken)==false))
                {
                    if(includeSubjectConstituent ==true)
                    {
                        Annotation foundSubj= InvestigateDependencies.findMoreDepsForToken(matchedDepToken,"nsubj",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                        Annotation foundExpl= InvestigateDependencies.findMoreDepsForToken(matchedDepToken,"expl",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                        
                        Annotation foundSubjOnGov= InvestigateDependencies.findMoreDepsForToken(govenorToken,"nsubj",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                        Annotation govIsADobj= InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"dobj",
                                                                                        depRelationsSetOfSentence);
                        
                        if(foundSubj !=null)
                        {
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(foundSubj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundSubj,outputAnnotationType,"nconj",tokenAnnotationSetOfSentence);
                        }
                        
                       
                        else if(foundSubjOnGov!=null && foundExpl==null)
                        {
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(foundSubjOnGov, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            
                            
                            if(govenorToken.getFeatures().get("category").toString().trim().startsWith("V"))
                            {
                                CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundSubjOnGov,outputAnnotationType,"vConj",tokenAnnotationSetOfSentence);
                            }
                            else if (govenorToken.getFeatures().get("category").toString().trim().startsWith("N")||govenorToken.getFeatures().get("category").toString().trim().startsWith("J"))
                            {
                                CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundSubjOnGov,outputAnnotationType,"nConj",tokenAnnotationSetOfSentence);
                               
                                Annotation foundDep  = InvestigateDependencies.findMoreDepsForToken(govenorToken,"dep",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                                
                                if(foundDep!=null && foundDep.getFeatures().get("category").toString().trim().startsWith("AUX") && foundDep.getStartNode().getOffset() < govenorToken.getStartNode().getOffset())
                                {
                                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                    GenericScopeHelperClass.generatePathsForToken(foundDep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                    CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundDep,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"NP",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                                }
                            }
                            else
                            {
                                CreateAnnotations.annotateTheScopeForLEFT_NEW(govenorToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundSubjOnGov,outputAnnotationType,"conj",tokenAnnotationSetOfSentence);
                            }
                            
                            
                            
                        }// found subjOnGov!=null...
                        else if (govIsADobj!=null)
                        {
                            // is a relative clause
                            Annotation foundRelOnVerb  = InvestigateDependencies.findMoreDepsForToken(govIsADobj, "rel", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                            // there is a noun subject attached to the verb
                            Annotation foundSubjOnVerb  = InvestigateDependencies.findMoreDepsForToken(govIsADobj, "nsubj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                            Annotation foundDetOnConjTerm = InvestigateDependencies.findMoreDepsForToken(govenorToken, "det", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                            if(foundRelOnVerb!=null)
                            {
                                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(foundRelOnVerb, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                
                                if(foundDetOnConjTerm!=null)
                                {
                                    // don't mark from det onwards
                                    GenericScopeHelperClass.generatePathsForToken(foundDetOnConjTerm, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                    CreateAnnotations.annotateTheScopeForLEFT_NEW(foundDetOnConjTerm, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundRelOnVerb,outputAnnotationType,"conj",tokenAnnotationSetOfSentence);
                                    
                                }
                                else
                                {
                                    CreateAnnotations.annotateTheScopeForLEFT_NEW(govenorToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundRelOnVerb,outputAnnotationType,"conj",tokenAnnotationSetOfSentence);
                                }
                            }
                            // want to mark up to the verb ...
                            else if(foundSubjOnVerb!=null)
                            {
                                
                                AnnotationSet testSet = tokenAnnotationSetOfSentence.get(govIsADobj.getEndNode().getOffset()+1);
                                ArrayList <Annotation> testListOfSentence= new ArrayList<Annotation>(testSet);
                                OffsetComparator comparatorTestToks = new OffsetComparator();
                                Collections.sort(testListOfSentence,comparatorTestToks);
                                Annotation test = testListOfSentence.get(0);
                               
                                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(govIsADobj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(foundSubjOnVerb, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                
                                GenericScopeHelperClass.generatePathsForToken(test, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                                CreateAnnotations. annotateTheScopeForLEFT_NEW(test, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundSubjOnVerb,outputAnnotationType,"conj",tokenAnnotationSetOfSentence);
                                
                                
                            }
                            
                        }
                        
                    }// include subj
                    GenericScopeHelperClass.generatePathsForToken(triggerToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(triggerToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                    notAnnotationsAlreadyAnnotated.add(triggerToken);
                }
            }
        }
    }
    /***************************************************************************
     * NEW:: method name: findScopeFor_ConjNegBut() ** for collapsedDeps...
     *
     *
     ****************************************************************************/
    protected static void findScopeForConjNegBut(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc    )
    {
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
        if((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
        {
            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
            CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
            notAnnotationsAlreadyAnnotated.add(matchedDepToken);
        }

        
    }
    /***************************************************************************
     * NEW:: method name: findScopeFor_ConjNegAND() **  - for collapsedDeps...
     *
     *
     ****************************************************************************/
    protected static void findScopeForConjNegAnd(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc    )
    {
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
       
        if((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
        {
            Annotation foundPrep = InvestigateDependencies.findMoreDepsForToken(govenorToken, "prep_of", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
            Annotation foundCCcomp = InvestigateDependencies.findMoreDepsForToken(govenorToken, "ccomp", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
            Annotation foundDobj = InvestigateDependencies.findMoreDepsForToken(govenorToken, "dobj", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
           
            Annotation foundDobjOnDep = InvestigateDependencies.findMoreDepsForToken(matchedDepToken, "dobj", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
            if(foundDobj!=null)
            {
                if(foundCCcomp!=null)
                {
                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(foundCCcomp, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundCCcomp,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    
                }
                else
                {
                    
                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(foundDobj, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundDobj,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                }
            }//dobj!=null
            else if(foundCCcomp!=null)
            {
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(foundCCcomp, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundCCcomp,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                
            }
            // new could be a prep_of...
            else if(foundPrep!=null)
            {
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(foundPrep, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                
                
            }

           
            else if(foundDobjOnDep==null)
          
            {
                
                // see if gov has a dobj dep
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
            }
        }
        
    }
    /***************************************************************************
     * NEW:: method name: findScopeFor_ConjNegOR() **  - for collapsedDeps...
     *
     *
     ****************************************************************************/
    protected static void findScopeForConjNegOr(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc)
    {
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
        if((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
        {
            Annotation foundCCcomp = InvestigateDependencies.findMoreDepsForToken(govenorToken, "ccomp", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
            Annotation foundDobj = InvestigateDependencies.findMoreDepsForToken(govenorToken, "dobj", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
             Annotation foundPrep = InvestigateDependencies.findMoreDepsForToken(govenorToken, "prep_of", true,tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
            if(foundDobj!=null)
            {
                if(foundCCcomp!=null)
                {
                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(foundCCcomp, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundCCcomp,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    
                }
                else
                {

                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(foundDobj, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundDobj,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                }
            }//dobj!=null
            else if(foundCCcomp!=null)
            {
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(foundCCcomp, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundCCcomp,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                
            }
            // new could be a prep_of... 
            else if(foundPrep!=null)
            {
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(foundPrep, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                notAnnotationsAlreadyAnnotated.add(matchedDepToken);

                
            }
            
        }//trigger_found
        
    }
    
    /***************************************************************************
     * NEW:: method name: findScopeForNSubjDependency()
     * for neg trigger pronouns (no one, nothing...)
     *
     ****************************************************************************/
    protected static void findScopeForNSubjDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc, String inputTypeTrigger)
    {
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        // dep should be the nsubj ...
        Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
        if((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false) && inputTypeTrigger.equals("SelfNegOriginal")==false)
        {
            String posTagOfGov = InvestigateConstituentsFromParseTree.findPosTag (govenorToken,syntaxTreeNodeAnnotationSet);
            if(posTagOfGov.startsWith("V")==false && govenorToken.getFeatures().get("string").toString().trim().endsWith("ion")==false)
            {
               
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                 
                CreateAnnotations.annotateTheScopeForNSubjLeftSpan(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,"nsubj");
               // notAnnotationsAlreadyAnnotated.add(govenorToken);
            }
            else if(posTagOfGov.startsWith("V") == true && inputTypeTrigger.equals("implicitNegTriggers")==true)
            {
                // need to annotate the scope that includes the nsubj+the govenor (i.e absent)
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                CreateAnnotations.annotateTheScopeForNSubjLeftSpan(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,"nsubj");
               // notAnnotationsAlreadyAnnotated.add(govenorToken);  
            }
                            
        }// trigger!=null
        else if ((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false) && inputTypeTrigger.equals("SelfNegOriginal")==true)
        {
            Annotation testXComp=InvestigateDependencies.findMoreDepsForToken(govenorToken,"xcomp",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
            Annotation testPrep=InvestigateDependencies.findMoreDepsForToken(govenorToken,"prep",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
            Annotation testCComp=InvestigateDependencies.findMoreDepsForToken(govenorToken,"ccomp",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
            if (testXComp == null && testPrep ==null && testCComp ==null)
            {
               
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                CreateAnnotations.annotateTheScopeForNSubjLeftSpan(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,"nsubj");
                notAnnotationsAlreadyAnnotated.add(govenorToken); 
            }
        }
        // new if "none" is the nsubj ** new only annotate for none / nothing ...
        else if(theTriggerFound ==null && inputTypeTrigger.equals("explicitNegTriggers"))
        {
             Annotation theTriggerFoundTwo = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
            if((theTriggerFoundTwo!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
            {
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                //left span 
                CreateAnnotations.annotateTheScopeForNSubjLeftSpan(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFoundTwo,govenorToken,outputAnnotationType,"nsubj");
                // right span
                Annotation testRCMOD=InvestigateDependencies.findMoreDepsForToken(matchedDepToken,"rcmod",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                Annotation testInfmod=InvestigateDependencies.findMoreDepsForToken(matchedDepToken,"infmod",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
               
              
                
                    CreateAnnotations.annotateTheScopeForDepsWholeClause(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFoundTwo,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                

            }
            
            
        }
    }
    /***************************************************************************
     * NEW:: method name: findScopeForNSubjPassDependency()
     * 
     *
     ****************************************************************************/
    protected static void findScopeForNSubjPassDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc, String inputTypeTrigger    )
    {
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        // dep should be the nsubj ...
        Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
        if((theTriggerFound!=null))
           {
            //make sure that the posToken is not verb...
            String posTagOfGov = InvestigateConstituentsFromParseTree.findPosTag (govenorToken,syntaxTreeNodeAnnotationSet);
            if(posTagOfGov.startsWith("V")==true)
            {
                // need to annotate the scope that includes the nsubj+the govenor (i.e absent)
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                //NEW::: 
                CreateAnnotations.annotateTheScopeForNSUBJPassScope(govenorToken,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,outputAnnotationType,govenorToken,"NP",theTriggerFound,matchedDepToken,tokenAnnotationSetOfSentence);
            }
            
        }// trigger!=null
        
        else
        {
            theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
            if(theTriggerFound!=null &&(GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
            {
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                
                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                
            }
            
        }
        
    }

    /*********************************************************************************************/
    protected static void findScopeForNConjDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc, String inputTypeTrigger,boolean includeSubjectConstituent)
    
    {
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
        String posTagOfDep = InvestigateConstituentsFromParseTree.findPosTag (matchedDepToken,syntaxTreeNodeAnnotationSet);
        
        if((inputTypeTrigger.equals("SelfNegOriginal")== false) && (theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false) && inputTypeTrigger.equals("implicitNegTriggers")== false)
      
        {
            
            if(includeSubjectConstituent ==true)
            {
                Annotation foundSubj= InvestigateDependencies.findMoreDepsForToken(matchedDepToken,"nsubj",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                Annotation foundExpl= InvestigateDependencies.findMoreDepsForToken(matchedDepToken,"expl",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);

                Annotation foundSubjOnGov= InvestigateDependencies.findMoreDepsForToken(govenorToken,"nsubj",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                Annotation govIsADobj= InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"dobj",
                                                                                    depRelationsSetOfSentence);
                if(foundSubj !=null)
                {
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(foundSubj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                   
                    
                    CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundSubj,outputAnnotationType,"nconj",tokenAnnotationSetOfSentence);
                }
                
                
                else if(foundSubjOnGov!=null && foundExpl==null)
                {
                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                     GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(foundSubjOnGov, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                  
                    
                    if(govenorToken.getFeatures().get("category").toString().trim().startsWith("V"))
                    {
                        CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundSubjOnGov,outputAnnotationType,"vConj",tokenAnnotationSetOfSentence);
                    }
                    else if (govenorToken.getFeatures().get("category").toString().trim().startsWith("N")||govenorToken.getFeatures().get("category").toString().trim().startsWith("J"))
                    {
                        CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundSubjOnGov,outputAnnotationType,"nConj",tokenAnnotationSetOfSentence);
                        Annotation foundDep  = InvestigateDependencies.findMoreDepsForToken(govenorToken,"dep",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                       
                        if(foundDep!=null && foundDep.getFeatures().get("category").toString().trim().startsWith("AUX") && foundDep.getStartNode().getOffset() < govenorToken.getStartNode().getOffset())
                        {
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(foundDep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                             GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundDep,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"NP",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                        }
                    }
                    else
                    {
                       CreateAnnotations.annotateTheScopeForLEFT_NEW(govenorToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundSubjOnGov,outputAnnotationType,"conj",tokenAnnotationSetOfSentence); 
                    }
                    

                    
                }// found subjOnGov!=null...
                else if (govIsADobj!=null)
                {
                    
                    // is a relative clause
                    Annotation foundRelOnVerb  = InvestigateDependencies.findMoreDepsForToken(govIsADobj, "rel", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                    // there is a noun subject attached to the verb
                    Annotation foundSubjOnVerb  = InvestigateDependencies.findMoreDepsForToken(govIsADobj, "nsubj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                    Annotation foundDetOnConjTerm = InvestigateDependencies.findMoreDepsForToken(govenorToken, "det", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
                    if(foundRelOnVerb!=null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundRelOnVerb, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        
                        if(foundDetOnConjTerm!=null)
                        {
                            // don't mark from det onwards
                            GenericScopeHelperClass.generatePathsForToken(foundDetOnConjTerm, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            CreateAnnotations.annotateTheScopeForLEFT_NEW(foundDetOnConjTerm, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundRelOnVerb,outputAnnotationType,"conj",tokenAnnotationSetOfSentence);
                            
                        }
                        else
                        {
                            
                            CreateAnnotations.annotateTheScopeForLEFT_NEW(govenorToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundRelOnVerb,outputAnnotationType,"conj",tokenAnnotationSetOfSentence);
                        }
                    }
                    // want to mark up to the verb ... 
                    else if(foundSubjOnVerb!=null)
                    {
                          
                            AnnotationSet testSet = tokenAnnotationSetOfSentence.get(govIsADobj.getEndNode().getOffset()+1);
                            ArrayList <Annotation> testListOfSentence= new ArrayList<Annotation>(testSet);
                            OffsetComparator comparatorTestToks = new OffsetComparator();
                            Collections.sort(testListOfSentence,comparatorTestToks);
                            Annotation test = testListOfSentence.get(0);
                            
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(govIsADobj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(foundSubjOnVerb, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);

                            GenericScopeHelperClass.generatePathsForToken(test, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            CreateAnnotations. annotateTheScopeForLEFT_NEW(test, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundSubjOnVerb,outputAnnotationType,"conj",tokenAnnotationSetOfSentence);
                            

                    }
                    
                }
                
            }// include subj
            
            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);

            Annotation foundParentDobj = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"dobj",depRelationsSetOfSentence);
            if(foundParentDobj!=null)
            {
                // check if there is a prep on pdobj
                Annotation foundPrep = InvestigateDependencies.findMoreDepsForToken(foundParentDobj,"prep",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                if(foundPrep!=null)
                {
                    GenericScopeHelperClass.generatePathsForToken(foundPrep, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundPrep,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    
                }
                else
                {
                    CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                    notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                }
            }
            // no parent pdobj
            else
            {
                
                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
            }
        }// found
      
        else if((inputTypeTrigger.equals("SelfNegOriginal")== true) && (theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
        {
            Annotation foundParentAdvMod = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"advmod",depRelationsSetOfSentence);
            Annotation foundParentAMod = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"amod",depRelationsSetOfSentence);
            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
            
            if (includeSubjectConstituent == true)
            {
                if(foundParentAMod!=null)
                {
                     Annotation foundDet = InvestigateDependencies.findMoreDepsForToken(foundParentAMod,"det",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    Annotation foundPoss = InvestigateDependencies.findMoreDepsForToken(foundParentAMod,"poss",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    if(foundDet!=null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundDet, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundDet,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"NP",govenorToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                        
                    }
                    else if(foundPoss!=null)
                    {
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundPoss, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundPoss,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"NP",govenorToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                    }
                }
                
            }
            if(foundParentAdvMod!=null)
            {
                GenericScopeHelperClass.generatePathsForToken(foundParentAdvMod, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                
                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundParentAdvMod,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                notAnnotationsAlreadyAnnotated.add(matchedDepToken);

            }
            else if(foundParentAMod!=null)
            {
                GenericScopeHelperClass.generatePathsForToken(foundParentAMod, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundParentAMod,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                
                
            }
            
            
        }
    }

    /********************************************NEW For selfNeg & implicitNeg*************************************************/
    protected static void findScopeForCCompDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc,String testAnnotationType,boolean includeSubjectConstituent)
    {
        if(testAnnotationType.equals("NegTrigger"))
        {
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
        if((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false))
        {
            if(includeSubjectConstituent ==true)
            {
               
                /***************************************************************************************/
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                
                CreateAnnotations.annotateTheScopeUsingParseTree(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType);
                //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                /***************************************************************************************/
            }

            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
            CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
            notAnnotationsAlreadyAnnotated.add(govenorToken); 
         }
        }
        //else if (testAnnotationType.equals("ModalityTrigger"))
        else if (testAnnotationType.equals("NegTrigger") ==false)
        {
            Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
            // believe that X inhibits Y ... check that the gov token is a verb, event trigger (i.e inhibits is the dep)
            
            Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
            if(theTriggerFound!=null && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false))
            {
                
                String posTagOfGov = InvestigateConstituentsFromParseTree.findPosTag(govenorToken,syntaxTreeNodeAnnotationSet);
                String posTagOfDep= InvestigateConstituentsFromParseTree.findPosTag(matchedDepToken,syntaxTreeNodeAnnotationSet);
                
                if(posTagOfGov.startsWith("V")==true)
                {
                    
                    Annotation foundComplementizer = InvestigateDependencies.findMoreDepsForToken(matchedDepToken,"complm",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    Annotation foundNsubj = InvestigateDependencies.findMoreDepsForToken(matchedDepToken,"nsubj",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    Annotation foundCop = InvestigateDependencies.findMoreDepsForToken(matchedDepToken,"cop",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    if(foundComplementizer == null)
                    {
                        if (posTagOfDep.startsWith("V") == false && foundCop!=null)
                        {
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            // new
                            if(govenorToken.getStartNode().getOffset()< matchedDepToken.getStartNode().getOffset())
                            {
                                CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,foundCop);
                                notAnnotationsAlreadyAnnotated.add(govenorToken);
                            }
                            else
                            {
                                CreateAnnotations.annotateTheScopeForDepsLeft(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence);
                                notAnnotationsAlreadyAnnotated.add(govenorToken);
                            }
                            
                            
                        }
                        else
                        {
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            if(govenorToken.getStartNode().getOffset()< matchedDepToken.getStartNode().getOffset())
                            {
                                CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                notAnnotationsAlreadyAnnotated.add(govenorToken);
                            }
                            else
                            {
                                CreateAnnotations.annotateTheScopeForDepsLeft(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence);
                                notAnnotationsAlreadyAnnotated.add(govenorToken);
                            }
                            
                        }
                    }// comp ==null
                    else if (foundComplementizer.getFeatures().get("string").toString().trim().toLowerCase().equals("that") ==false)
                    {
                        if (posTagOfDep.startsWith("V") == false && foundCop!=null)
                        {
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            if(govenorToken.getStartNode().getOffset()< matchedDepToken.getStartNode().getOffset())
                            {
                                CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,foundCop);
                                notAnnotationsAlreadyAnnotated.add(govenorToken);
                            }
                            else
                            {
                                CreateAnnotations.annotateTheScopeForDepsLeft(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence);
                                notAnnotationsAlreadyAnnotated.add(govenorToken);
                            }
                            
                            
                        }
                        else
                        {
                            
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            if(govenorToken.getStartNode().getOffset()< matchedDepToken.getStartNode().getOffset())
                            {
                                CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                notAnnotationsAlreadyAnnotated.add(govenorToken);
                            }
                            else
                            {
                                CreateAnnotations.annotateTheScopeForDepsLeft(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence);
                                notAnnotationsAlreadyAnnotated.add(govenorToken);
                            }
                            notAnnotationsAlreadyAnnotated.add(govenorToken);
                        }
                    }// not that
                    // new :::
                    else if(foundNsubj ==null)
                    {
                        if (posTagOfDep.startsWith("V") == false && foundCop!=null)
                        {
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            if(govenorToken.getStartNode().getOffset()< matchedDepToken.getStartNode().getOffset())
                            {
                                CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,foundCop);
                                notAnnotationsAlreadyAnnotated.add(govenorToken);
                            }
                            else
                            {
                                CreateAnnotations.annotateTheScopeForDepsLeft(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence);
                                notAnnotationsAlreadyAnnotated.add(govenorToken);
                            }
                            notAnnotationsAlreadyAnnotated.add(govenorToken);
                            
                            
                        }
                        else
                        {
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            if(govenorToken.getStartNode().getOffset()< matchedDepToken.getStartNode().getOffset())
                            {
                                CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                notAnnotationsAlreadyAnnotated.add(govenorToken);
                            }
                            else
                            {
                                CreateAnnotations.annotateTheScopeForDepsLeft(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence);
                                notAnnotationsAlreadyAnnotated.add(govenorToken);
                            }
                            notAnnotationsAlreadyAnnotated.add(govenorToken);
                        }
                    } //no nounsubj
                    // new ......
                    //else if (theTriggerFound.getFeatures().get("Type").toString().trim().equals("REPORTING"))
                    else
                    {
                        if (posTagOfDep.startsWith("V") == false && foundCop!=null)
                        {
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            if(govenorToken.getStartNode().getOffset()< matchedDepToken.getStartNode().getOffset())
                            {
                                CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,foundCop);
                                notAnnotationsAlreadyAnnotated.add(govenorToken);
                            }
                            else
                            {
                                CreateAnnotations.annotateTheScopeForDepsLeft(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence);
                                notAnnotationsAlreadyAnnotated.add(govenorToken);
                            }
                            notAnnotationsAlreadyAnnotated.add(govenorToken);
                            
                            
                        }
                        //test:::
                        
                        else
                        {
                            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                            if(govenorToken.getStartNode().getOffset()< matchedDepToken.getStartNode().getOffset())
                            {
                                CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                                notAnnotationsAlreadyAnnotated.add(govenorToken);
                            }
                            else
                            {
                                CreateAnnotations.annotateTheScopeForDepsLeft(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence);
                                notAnnotationsAlreadyAnnotated.add(govenorToken);
                            }
                            notAnnotationsAlreadyAnnotated.add(govenorToken);
                        }
                        
                    }
                }
            }
        }

               
    }
    /********************************************NEW For"Nothing" having the dobj relation*************************************************/
    protected static void findScopeForDobjDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc, String inputTypeTrigger, String testAnnotationType,boolean includeSubjectConstituent )
    {
        if (testAnnotationType.equals("NegTrigger"))
        {
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
        Annotation theTriggerFoundGov = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
        if((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
        {
            
            if(includeSubjectConstituent ==true)
            {
                Annotation foundSubj= InvestigateDependencies.findMoreDepsForToken(govenorToken,"nsubj",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
               
                Annotation foundConj = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"conj",depRelationsSetOfSentence);
                if(foundSubj !=null)
                {
                   
                    /***************************************************************************************/
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(foundSubj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    
                    CreateAnnotations.annotateTheScopeForLEFT_NEW(matchedDepToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFound,foundSubj,outputAnnotationType,"neg",tokenAnnotationSetOfSentence);
                    //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    /***************************************************************************************/
                }
               
                else if(foundConj!=null)
                {
                  Annotation foundSubjOnConj= InvestigateDependencies.findMoreDepsForToken(foundConj,"nsubj",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    if(foundSubjOnConj!=null)
                    {
                        /***************************************************************************************/
                        GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundSubjOnConj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundSubjOnConj,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"NP",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                        //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                        /***************************************************************************************/
                    }
                    
                }
            } // if include==true...

            if(matchedDepToken.getFeatures().get("string").toString().trim().toLowerCase().equals("nothing")|| matchedDepToken.getFeatures().get("string").toString().trim().toLowerCase().equals("none"))
            {
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                CreateAnnotations.annotateTheScopeForDepsLeft(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence);
                // new want the right side as well.
                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                notAnnotationsAlreadyAnnotated.add(matchedDepToken); 
            }
           
            else if (inputTypeTrigger.equals("SelfNegOriginal"))
            {
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                CreateAnnotations.annotateTheScopeForDepsLeft(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence);
                notAnnotationsAlreadyAnnotated.add(matchedDepToken); 
                
            }
            
        }
        else if ((theTriggerFoundGov!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false))
        {
           
            if(includeSubjectConstituent ==true)
            {
                Annotation foundSubj= InvestigateDependencies.findMoreDepsForToken(govenorToken,"nsubj",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
               
                Annotation foundConj = InvestigateDependencies.findDepOfGovenor(govenorToken, tokenAnnotationSetOfSentence,"conj",depRelationsSetOfSentence);
                if(foundSubj !=null)
                {
                    
                    /***************************************************************************************/
                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(foundSubj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    
                    CreateAnnotations.annotateTheScopeForLEFT_NEW(govenorToken, syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFoundGov,foundSubj,outputAnnotationType,"neg",tokenAnnotationSetOfSentence);
                    //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                    /***************************************************************************************/
                                         
                }
             
                else if(foundConj!=null)
                {
                    Annotation foundSubjOnConj= InvestigateDependencies.findMoreDepsForToken(foundConj,"nsubj",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    if(foundSubjOnConj!=null)
                    {
                        /***************************************************************************************/
                        GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundSubjOnConj, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundSubjOnConj,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFoundGov,"NP",govenorToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,matchedDepToken);
                        //notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                        /***************************************************************************************/
                    }
                    
                }
            } // if include==true...
            
            if (inputTypeTrigger.equals("implicitNegTriggers"))
            {
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations,theTriggerFoundGov,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                notAnnotationsAlreadyAnnotated.add(govenorToken);
            }
        }
        }// negTrigger
        //else if (testAnnotationType.equals("ModalityTrigger"))
        else if (testAnnotationType.equals("NegTrigger")==false)
        {
            Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
            Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
            if((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, govenorToken)==false))
            {
                if(govenorToken.getFeatures().get("string").toString().trim().equals("set")&& matchedDepToken.getFeatures().get("string").toString().trim().toLowerCase().equals("goal"))
                {
                    Annotation foundOfObject = InvestigateDependencies.findMoreDepsForToken(matchedDepToken,"prepc_of",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                    if(foundOfObject!=null)
                    {
                       
                        GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                        GenericScopeHelperClass.generatePathsForToken(foundOfObject, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                        CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundOfObject,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                       
                        notAnnotationsAlreadyAnnotated.add(govenorToken);
                        notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                        
                    }
                    
                }// end if ..
                // NEW::: for all verbs that are triggers and have a dobj
                else
                {
                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    CreateAnnotations.annotateTheScopeForDeps(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
                    notAnnotationsAlreadyAnnotated.add(govenorToken);
                    
                    
                }
                
            }
            
        }

    }
   

    /********************************************NEW ForSelfNegs*************************************************/
    protected static void findScopeFoNNDep(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc,String testAnnotationType,boolean includeSubjectConstituent)
    {
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
        if((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
        {
            
            if(includeSubjectConstituent ==true)
            {
                Annotation foundDet= InvestigateDependencies.findMoreDepsForToken(govenorToken,"det",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
                if(foundDet !=null)
                {
                    
                    /***************************************************************************************/
                    CreateAnnotations.annotateTheScopeForASingleConstituentNSUBJ(foundDet,syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"DT",matchedDepToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
                    /***************************************************************************************/
                }
            }
            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
            CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
            notAnnotationsAlreadyAnnotated.add(matchedDepToken);
            
        }// if
    }
/************************************NEW FOR EXPLETIVE PRONOUNS************************************************************************************************/
    // we want this dep to occur only if includeSubjectConstituent== true && regardless if other side is annotated....
    protected static void findScopeFoExplDep(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc,String testAnnotationType,boolean includeSubjectConstituent)
    {
        if(includeSubjectConstituent ==true)
        {
            // example sentence"there is nothing we can do about it..."
            // dep is the nsubj of nothing
            // i.e dep is "There", gov "is"
            
           Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
           Annotation testTrigger = InvestigateDependencies.findMoreDepsForToken(govenorToken,"nsubj",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
           if(testTrigger!=null)
           {
               // see if testTrigger is a negTrigger
              Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,testTrigger);
              // it is a negTrigger
              if(theTriggerFound!=null)
              {
                  GenericScopeHelperClass.generatePathsForToken(testTrigger, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                  GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                  
                CreateAnnotations.annotateTheScopeForDepsLeft(testTrigger,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence);
              }
           }// there is the nsubj dep
            else
            {
                // expl is on "nothing"
                 Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,govenorToken);
                // it is a negTrigger
                if(theTriggerFound!=null)
                {
                    GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                    GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                    
                    CreateAnnotations.annotateTheScopeForDepsLeft(govenorToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,matchedDepToken,outputAnnotationType,tokenAnnotationSetOfSentence);
                }
                
                
                
            }// else
            
        }// include subj is true...
    }
    /********************************************NEW For HedgeTriggers*************************************************/
    protected static void findScopeForAuxDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc    )
    
    {
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
        if((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
        {
            
            String posTagOfGov = InvestigateConstituentsFromParseTree.findPosTag (govenorToken,syntaxTreeNodeAnnotationSet);
            Annotation foundCop = InvestigateDependencies.findMoreDepsForToken(govenorToken,"cop",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
            if(foundCop!=null && posTagOfGov.startsWith("V")==false)
            {
                GenericScopeHelperClass.generatePathsForToken(foundCop, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundCop,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null   );
                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                
            }
            
            else
            {
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null   );
                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
            }
            
            
            
        }
    }

    /********************************************NEW For HedgeTriggers*************************************************/
    protected static void findScopeForAuxPassDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc    )
    
    {
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
        if((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
        {
            
            String posTagOfGov = InvestigateConstituentsFromParseTree.findPosTag (govenorToken,syntaxTreeNodeAnnotationSet);
            Annotation foundCop = InvestigateDependencies.findMoreDepsForToken(govenorToken,"cop",true, tokenAnnotationSetOfSentence,depRelationsSetOfSentence);
            if(foundCop!=null && posTagOfGov.startsWith("V")==false)
            {
                GenericScopeHelperClass.generatePathsForToken(foundCop, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,foundCop,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null   );
                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
                
            }
            
            else
            {
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
                CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null   );
                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
            }
            
            
            
        }
    }
    /********************************************NEW For HedgeTriggers*************************************************/
    protected static void findScopeForMarkDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc    )
    
    {
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
        if((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
        {
            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
            CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null   );
            notAnnotationsAlreadyAnnotated.add(matchedDepToken);
            
            
            
            
            
        }
        
    }
    

    /********************************************NEW For Modality *************************************************/
    protected static void findScopeForComplDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc,String testAnnotationType    )
    {
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
      
        if(theTriggerFound!=null && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
        {
            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxTreeNodeAnnotationSet);
            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxTreeNodeAnnotationSet);
            CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxTreeNodeAnnotationSet,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null   );
            notAnnotationsAlreadyAnnotated.add(matchedDepToken);
        }
        
        
    }
    /********************************************NEW For"Become" being a modality trigger*************************************************/
    protected static void markCop(AnnotationSet tokenAnnotationSetOfSentence,AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations,AnnotationSet syntaxNodesOfSentence,ArrayList<Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,AnnotationSet depRelationsSetOfSentence,HashMap<Integer,ArrayList<Integer>>tokenPathsOfDoc,String inputAnnotationType,String testAnnotationType    )
    {
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
        if((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false) && matchedDepToken.getFeatures().get("root").toString().trim().toLowerCase().equals("become"))
        {
            GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxNodesOfSentence);
            GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxNodesOfSentence);
            CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxNodesOfSentence,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
            notAnnotationsAlreadyAnnotated.add(matchedDepToken);
            
        }
        else if((theTriggerFound!=null) && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false))
        {
           if(govenorToken.getStartNode().getOffset()>matchedDepToken.getStartNode().getOffset())
           {
               GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxNodesOfSentence);
               GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxNodesOfSentence);
               CreateAnnotations.annotateTheScopeForDeps(matchedDepToken,syntaxNodesOfSentence,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence,depRelationsSetOfSentence,0,null);
               notAnnotationsAlreadyAnnotated.add(matchedDepToken);
           }
            else
            {
                GenericScopeHelperClass.generatePathsForToken(govenorToken, tokenPathsOfDoc, syntaxNodesOfSentence);
                GenericScopeHelperClass.generatePathsForToken(matchedDepToken, tokenPathsOfDoc,syntaxNodesOfSentence);
                CreateAnnotations.annotateTheScopeForDepsLeft(matchedDepToken,syntaxNodesOfSentence,tokenPathsOfDoc,outputScopeAnnotations, theTriggerFound,govenorToken,outputAnnotationType,tokenAnnotationSetOfSentence);
                notAnnotationsAlreadyAnnotated.add(matchedDepToken);
            }
        }
        
        
    }
    /***************************************************************************
     * method name: findScopeForRCModDependency()
     * this method is called from the switch case in the execute() method from
     * the ExtractNegatorScope class.
     * function: this method takes care of the advmod dependency case.
     * This method will then call the relevent method to annotate the scope.
     *
     ****************************************************************************/
    protected static void findScopeForRCModDependency(AnnotationSet tokenAnnotationSetOfSentence, AnnotationSet triggerWordsAnnotationSet,AnnotationSet outputScopeAnnotations, AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList <Annotation> notAnnotationsAlreadyAnnotated,Annotation govenorToken,Integer depID,String outputAnnotationType,HashMap<Integer,ArrayList<Integer>> tokenPathsOfDoc, String inputTypeTrigger, String testAnnotationType,AnnotationSet depRelationsSetOfSentence)
    {
        
        Annotation matchedDepToken = tokenAnnotationSetOfSentence.get(depID);
        Annotation theTriggerFound = GenericScopeHelperClass.findTriggerAnnotation(triggerWordsAnnotationSet,matchedDepToken);
        
        Annotation foundXcomp= InvestigateDependencies.findMoreDepsForToken(matchedDepToken, "xcomp", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
        Annotation foundDobj= InvestigateDependencies.findMoreDepsForToken(matchedDepToken, "dobj", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
        Annotation foundRel= InvestigateDependencies.findMoreDepsForToken(matchedDepToken, "rel", true,tokenAnnotationSetOfSentence, depRelationsSetOfSentence);
        if(theTriggerFound!=null && (GenericScopeHelperClass.findIfAlreadyAnnotated(notAnnotationsAlreadyAnnotated, matchedDepToken)==false) && foundDobj==null && foundXcomp ==null && foundRel ==null)
        {
            // we want to annotate the NP headed by the rcmod govenor token
            CreateAnnotations.annotateTheScopeForASingleConstituentLeft(govenorToken, syntaxTreeNodeAnnotationSet,outputScopeAnnotations,theTriggerFound,"NP",govenorToken, outputAnnotationType,0,tokenAnnotationSetOfSentence,govenorToken);
            
            
        }// if check ...
        
    }
/****************************************************************************/

}// end class

    /************************************************************************************************************************************/




