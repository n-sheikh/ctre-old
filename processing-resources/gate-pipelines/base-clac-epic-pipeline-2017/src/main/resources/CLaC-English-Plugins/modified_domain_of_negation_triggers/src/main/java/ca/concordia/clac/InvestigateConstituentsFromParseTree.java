/* InvestigateConstituentsFromParseTree.java
 * Authors:
 * Date: February 2013
 * Purpose: This class implements the methods needed when traversing or querying the parse tree
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

public abstract class InvestigateConstituentsFromParseTree
{ 
   /***************************************************************************
   * method name: findSyntaxNode()
   * returns: an intger ID of the correct node in the parse tree
   * function: this method will traverse the parse tree and attempt to find a 
   * match of the constituent in the parse tree with the token annotation passed 
   * as an arguement.
   *
   ****************************************************************************/
	protected static int findSyntaxNode (Annotation currentTokenToCheck, AnnotationSet syntaxTreeNodeAnnotationSet)
	{
		int mySyntaxNodeID =-1;
		for(Annotation mySyntaxNode: syntaxTreeNodeAnnotationSet)
		{
            if(mySyntaxNode.coextensive(currentTokenToCheck))
			{
                // now check that this is a base token - has no consists feature
                FeatureMap syntaxFeatureMap = mySyntaxNode.getFeatures();
                if(syntaxFeatureMap.containsKey("consists") == false)
                {
                
                    mySyntaxNodeID = mySyntaxNode.getId();
                    break;
                }
			}
		}
		return mySyntaxNodeID;
		
		
	}
    /***************************************************************************
     * method name: findPosTag()
     * returns: a String representing the part_of_speech tag of the token given
     * function: this method will traverse the parse tree and upon finding the 
     * correct constituent, it will return the POS tag feature associated with
     * the found constituent.
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
     * method name: findTheConstInParseTree()
     * returns: an integer ID of the found constituent
     * function: this method will traverse the parse tree and upon finding the 
     * correct constituent (according to the String parameter), it will return 
     * the ID of the found constituent.
     *
     ****************************************************************************/
	protected static int findTheConstInParseTree(int currentHeadID,AnnotationSet syntaxTreeNodeAnnotationSet,String constToFind)
	{
		int headID = findTheParent(currentHeadID,syntaxTreeNodeAnnotationSet);
		// terminating clause 1: have reached 
		if (headIDIsAMatch(headID,syntaxTreeNodeAnnotationSet,constToFind))
		{
			return headID;
			
		}
		//terminating clause 2: at root
		else if(haveReachedRoot(headID,syntaxTreeNodeAnnotationSet))
		{
			return -1;
		}
		
		else
		{
            // recursive case
			headID=findTheConstInParseTree(headID,syntaxTreeNodeAnnotationSet,constToFind);
		}
		return headID;
	}
    /***************************************************************************
     * method name: findTheParentConstInParseTree()
     * returns: an integer ID of the found constituent
     * function: this method will traverse the parse tree and upon finding the 
     * correct parent constituent of the constituent passed as a parameter, 
     * it will return the ID of the found constituent.
     *
     ****************************************************************************/	
    protected static int findTheParentConstInParseTree(int syntaxNodeIDOfNegTrigger,int syntaxNodeIDOfInitTrigger,AnnotationSet syntaxTreeNodeAnnotationSet)
	{
		int headIDofNegTrigger = findTheParent(syntaxNodeIDOfNegTrigger,syntaxTreeNodeAnnotationSet);
        if (headIDIsAMatchinConstFound(headIDofNegTrigger,syntaxNodeIDOfInitTrigger,syntaxTreeNodeAnnotationSet))
		{
			return headIDofNegTrigger;
			
		}
        else
        {
            int headID = findTheParentConstOfInitInParseTree(headIDofNegTrigger, syntaxNodeIDOfInitTrigger,syntaxTreeNodeAnnotationSet);
            if(headID !=-1)
            {
                return  headIDofNegTrigger;
            }
            else
            {
                if(haveReachedRoot(headIDofNegTrigger,syntaxTreeNodeAnnotationSet))
                {
                    return -1;
                }
                else
                {
                
                    headIDofNegTrigger=findTheParentConstInParseTree(headIDofNegTrigger,syntaxNodeIDOfInitTrigger,syntaxTreeNodeAnnotationSet);
                }
                
            }
            
            
        } 
        return headIDofNegTrigger;
		
	}
    /***************************************************************************
     * method name: findTheCommonParentConstInParseTree()
     * returns: an integer ID of the found constituent
     * function: this method will traverse the parse tree and upon finding the 
     * correct constituent  - based on the condition that the found constituent 
     * contains both an ancestor of the negTrigger and of the init trigger - 
     * it will return the ID of the found constituent.
     *
     ****************************************************************************/	
	protected static int findTheCommonParentConstInParseTree(ArrayList<Integer> parentListOfNegTrigger,ArrayList<Integer> parentListOfInitTrigger)
	{
		            
        for(int indexN =0; indexN< parentListOfNegTrigger.size(); indexN++)
        {
            int currentParentNegID = parentListOfNegTrigger.get(indexN).intValue();
            for(int indexI =0; indexI< parentListOfInitTrigger.size();indexI++)
            {
                int currentParentInitID = parentListOfInitTrigger.get(indexI).intValue();
                if (currentParentNegID == currentParentInitID)
                {
                    return currentParentNegID;
                }
                
            }
        }
        return -1;
		
	}
    /***************************************************************************
     * method name:findTheCommonParentConstInParseTreeForVP()
     * returns: an integer ID of the found constituent
     * function: this method will traverse the parse tree and upon finding the 
     * correct constituent  - based on the condition that the found constituent 
     * contains both an ancestor of the negTrigger and of the init trigger - 
     * it will return the ID of the found constituent.
     *
     ****************************************************************************/	
	protected static boolean findTheCommonParentConstInParseTreeForVP(Annotation tokenToCheck,long headOfVP, HashMap<Integer,ArrayList<Integer>>tokenPathsOfDoc)
	{
        ArrayList<Integer> parentListOfChild = tokenPathsOfDoc.get(tokenToCheck.getId());
        for(int indexN =0; indexN< parentListOfChild.size(); indexN++)
        {
            int currentChildID = parentListOfChild.get(indexN).intValue();
            if (currentChildID == headOfVP)
            {
                return true;
            }
                
        }
        return false;
		
	}

    /***************************************************************************
     * method name: findTheParentConstOfInitInParseTree()
     * returns: an integer ID of the found constituent
     * function: this method will traverse the parse tree and upon finding the 
     * correct constituent  - based on the condition that the found constituent 
     * contains both an ancestor of the init trigger and that of the passed 
     * id of the neg trigger - it will return the ID of the found constituent.
     *
     ****************************************************************************/	
	protected static int findTheParentConstOfInitInParseTree(int headIDofNegTrigger,int syntaxNodeIDOfInitTrigger,AnnotationSet syntaxTreeNodeAnnotationSet)
	{
		int newHeadID = findTheParent(syntaxNodeIDOfInitTrigger,syntaxTreeNodeAnnotationSet);
		// terminating clause 1: have reached 
		if (headIDIsAMatchinConstFound(headIDofNegTrigger,newHeadID,syntaxTreeNodeAnnotationSet))
		{
			return newHeadID;
			
		}
		//terminating clause 2: at root
		else if(haveReachedRoot(newHeadID,syntaxTreeNodeAnnotationSet))
		{
			return -1;
		}
		
		else
		{
            // recursive case
			newHeadID=findTheParentConstOfInitInParseTree(headIDofNegTrigger,newHeadID,syntaxTreeNodeAnnotationSet);
		}
		return newHeadID;
	}
    /***************************************************************************
     * method name: headIDIsAMatchinConstFound()
     * returns: a boolean value indicative if a match is found
     * function: this method takes two id's of parse tree constituents, the first 
     * being the negation trigger or its ancestor. It will then chack if the 2nd 
     * id is a node contained within the 1st constituent. 
     * This method is a helper method to other methods in the class.
     *
     ****************************************************************************/
	protected static boolean headIDIsAMatchinConstFound(int headIDofNegTrigger,int newHeadID,AnnotationSet syntaxTreeNodeAnnotationSet)
	{
		Annotation mySyntaxNode = syntaxTreeNodeAnnotationSet.get((int)headIDofNegTrigger);
        if(mySyntaxNode !=null)
		{
            FeatureMap syntaxFeatureMap = mySyntaxNode.getFeatures();
            if(syntaxFeatureMap.containsKey("consists"))
            {
                String entireConsistsFeature = syntaxFeatureMap.get("consists").toString().trim();
                String tempSubstring = entireConsistsFeature.substring(1,entireConsistsFeature.length()-1);
                // extract ... 
                String[] extractedResults = tempSubstring.split("\\,");
                for(int i=0; i<extractedResults.length; i++)
                {
                    int m =Integer.parseInt(extractedResults[i].trim());
                    if(m == newHeadID) 
                    {

                        return true;
                    }
                }//for
            }//if
        }// if !null
        
		return false;
	}
    /***************************************************************************
     * method name: findTheParent()
     * returns: an integer value indicative of the parent id constituent.
     * This method is a helper method to other methods in the class.
     * function: this method takes the id of a constituent in the parse tree 
     * and will traverse the parse tree to find the parent of this node.  
     *
     ****************************************************************************/
	protected static int findTheParent(long syntaxNodeIDToCheck, AnnotationSet syntaxTreeNodeAnnotationSet)
	{
		for(Annotation syntaxNodeAnnotation :syntaxTreeNodeAnnotationSet)
		{
			FeatureMap syntaxFeatureMap = syntaxNodeAnnotation.getFeatures();
			if(syntaxFeatureMap.containsKey("consists") && (syntaxNodeAnnotation.getId() > syntaxNodeIDToCheck))
			{
				String entireConsistsFeature = syntaxFeatureMap.get("consists").toString().trim();
				String tempSubstring = entireConsistsFeature.substring(1,entireConsistsFeature.length()-1);
				String[] extractedResults = tempSubstring.split("\\,");
				for(int i=0; i<extractedResults.length; i++)
				{
					int m =Integer.parseInt(extractedResults[i].trim());
					if(m == syntaxNodeIDToCheck) 
					{return syntaxNodeAnnotation.getId();}
				}
			}
			
		}
		return -1;
	}
	/***************************************************************************
     * method name: headIDIsAMatch()
     * returns: a boolean value indicative if a match was found.
     * This method is a helper method to other methods in the class.
     * function: this method takes the id of a constituent in the parse tree 
     * and will extract its features to determine if the category(constituent type) 
     * is a match to the "constToMatch" arguement .  
     *
     ****************************************************************************/
	protected static boolean headIDIsAMatch(int currentHeadID,AnnotationSet syntaxTreeNodeAnnotationSet,String constToMatch)
	{
		Annotation mySyntaxNode = syntaxTreeNodeAnnotationSet.get((int)currentHeadID);
		if(mySyntaxNode !=null)
		{
			FeatureMap syntaxFeatureMap = mySyntaxNode.getFeatures();
			if(syntaxFeatureMap.get("cat").toString().trim().equals(constToMatch))
			{
				return true;
			}
			
			return false;
		}
		return false;
	}
	/***************************************************************************
     * method name: haveReachedRoot()
     * returns: a boolean value indicative if a match was found.
     * This method is a helper method to other methods in the class.
     * function: this method takes the id of a constituent in the parse tree 
     * and will extract its features to determine if this node is actually a 
     * root node (the base case for the recursive methods).  
     *
     ****************************************************************************/
	protected  static boolean haveReachedRoot(int currentHeadID,AnnotationSet syntaxTreeNodeAnnotationSet)
	{
		// find the syntaxNodeAnnotation with the correct cat...
		Annotation mySyntaxNode = syntaxTreeNodeAnnotationSet.get((int)currentHeadID);
		FeatureMap syntaxFeatureMap = mySyntaxNode.getFeatures();
		if((syntaxFeatureMap.get("cat").toString().trim().equals("ROOT"))||(syntaxFeatureMap.get("cat").toString().trim().equals("S1")))
		{
			return true;
		}
		
		return false;
	}
    /***************************************************************************
     * method name: changeSpanForSingleConstLeft()
     * returns: a revised list of constituents which represents the scope for 
     * the current negation trigger.
     * This method is a helper method to other methods in the class.
     * function: this method will find and remove the negation trigger (if it 
     * is a verb) from the currently determined scope. 
     *
     ****************************************************************************/
	protected static ArrayList changeSpanForSingleConstLeft(long syntaxNodeFound,AnnotationSet syntaxTreeNodeAnnotationSet, int choice)
	{
        String [] extractedResults = breakDownConstituent(syntaxNodeFound,syntaxTreeNodeAnnotationSet);
        ArrayList <Integer> listToReturn = new ArrayList();
        if(extractedResults !=null)
        {
                               
            for(int i = 0; i< extractedResults.length; i++)
            {
                listToReturn.add(Integer.parseInt(extractedResults[i].trim()));
            }
               
        }
		return listToReturn;
	}
    

  	/***************************************************************************
     * method name: changeSpanForSingleConst()
     * returns: a revised list of constituents which represents the scope for 
     * the current negation trigger.
     * This method is a helper method to other methods in the class.
     * function: this method will find and remove the negation trigger (if it 
     * is a verb) from the currently determined scope. 
     *
     ****************************************************************************/
	protected static ArrayList changeSpanForSingleConst(Annotation markerToRemoveFromSpan,long syntaxNodeFound,AnnotationSet syntaxTreeNodeAnnotationSet, int choice)
	{
        String [] extractedResults = breakDownConstituent(syntaxNodeFound,syntaxTreeNodeAnnotationSet);
        ArrayList <Integer> listToReturn = new ArrayList();
        if(extractedResults !=null)
        {
            int currentID =-1;
            int counter =0;
            boolean found = false;
            String textToCheck ="";
            if(choice ==0)
            {
                textToCheck = markerToRemoveFromSpan.getFeatures().get("String").toString().trim();
            }
            else if (choice ==1)
            {
               textToCheck = markerToRemoveFromSpan.getFeatures().get("string").toString().trim();
            }
           
            while(counter < extractedResults.length && found == false)
            {
                currentID =Integer.parseInt(extractedResults[counter].trim());
                Annotation scopeTest = syntaxTreeNodeAnnotationSet.get((int)currentID);
                String compareText = scopeTest.getFeatures().get("text").toString().trim();
                if ((compareText.indexOf(textToCheck) >= 0)||(compareText.equals(textToCheck)))
                { 
                    // we have a neg marker in our text span ... 
                    found = true;
                    // takes all ids that occur AFTER the neg term ... 
                    
                        for(int i = counter+1; i< extractedResults.length; i++)
                        {
                            listToReturn.add(Integer.parseInt(extractedResults[i].trim()));
                        }
                }
                else 
                {
                    counter++;
				
                }
            }// while
        }
		return listToReturn;
	}
    
    /***************************************************************************
     * method name: changeSpanForDepRules()
     * returns: a revised list of constituents which represents the scope for 
     * the current negation trigger.
     * This method is a helper method to other methods in the class.
     * function: this method will find and remove the negation trigger 
     * from the currently determined scope. 
     *
     ****************************************************************************/	
	protected static ArrayList <Integer> changeSpanForDepRules(int syntaxNodeIDOfNegTrigger,Annotation markerToRemoveFromSpan,long syntaxNodeFound,AnnotationSet syntaxTreeNodeAnnotationSet)
	{
        
        
        String [] extractedResults = breakDownConstituent(syntaxNodeFound,syntaxTreeNodeAnnotationSet);
        ArrayList <Integer> listToReturn = new ArrayList();
        if(extractedResults !=null)
        {
            int currentID =-1;
            String textToCheck = null;
            if(markerToRemoveFromSpan.getFeatures().get("String")!=null)
            {
                textToCheck = markerToRemoveFromSpan.getFeatures().get("String").toString().trim();
               
            }
            else
            {
                textToCheck = markerToRemoveFromSpan.getFeatures().get("string").toString().trim();
                
            }
            // new::
            if(textToCheck.toLowerCase().equals("instead of"))
            {
                textToCheck ="of";
            }
            else if(textToCheck.toLowerCase().equals("rather than"))
            {
                textToCheck ="than"; 
            }
            else if(textToCheck.toLowerCase().equals("other than"))
            {
                textToCheck ="than"; 
            }
            else if(textToCheck.toLowerCase().equals("with the exception of"))
            {
                textToCheck ="of"; 
            }
            // outer while loop for the HEAD scope marker ...
            for(int index =0; index< extractedResults.length; index++)
            {
                currentID =Integer.parseInt(extractedResults[index].trim());
                
                Annotation scopeTest = syntaxTreeNodeAnnotationSet.get((int)currentID);
                String compareText = scopeTest.getFeatures().get("text").toString().trim();
                
                String [] compareTextSplit = compareText.split(" ");
                boolean foundMatchToText = false;
                for(int i =0; i< compareTextSplit.length; i++)
                {
                    if(compareTextSplit[i].toLowerCase().contains(textToCheck.toLowerCase()))
                    {
                        foundMatchToText = true;
                        break;
                    }
                }
                
               if (foundMatchToText == true)
                { 
                    findTheBaseTriggerConstiuent(syntaxNodeIDOfNegTrigger,currentID,listToReturn,syntaxTreeNodeAnnotationSet,textToCheck);
                }
                else if(currentID > syntaxNodeIDOfNegTrigger)
                {
                    listToReturn.add(Integer.parseInt(extractedResults[index].trim()));
                    
                }
            }// for
        }//if
		return listToReturn;
	}
    /***************************************************************************
     * method name: changeSpanForDepRulesWithList()
     * returns: a revised list of constituents which represents the scope for 
     * the current negation trigger.
     * This method is a helper method to other methods in the class.
     * function: this method will find and remove the negation trigger 
     * from the currently determined scope. 
     *
     ****************************************************************************/	
	protected static ArrayList <Integer> changeSpanForDepRulesWithList(int syntaxNodeIDOfNegTrigger,Annotation markerToRemoveFromSpan,ArrayList<Integer> currentList,AnnotationSet syntaxTreeNodeAnnotationSet)
	{
        ArrayList <Integer> listToReturn = new ArrayList();
        Annotation negNode =  syntaxTreeNodeAnnotationSet.get(syntaxNodeIDOfNegTrigger);
        if(currentList !=null)
        {
            int currentID =-1;
            String textToCheck = null;
            
            if(markerToRemoveFromSpan.getFeatures().get("String")!=null)
            {
                textToCheck = markerToRemoveFromSpan.getFeatures().get("String").toString().trim();
                
            }
            else
            {
                textToCheck = markerToRemoveFromSpan.getFeatures().get("string").toString().trim();
                
            }
            // new::
            if(textToCheck.toLowerCase().equals("instead of"))
            {
                textToCheck ="of";
            }
            else if(textToCheck.toLowerCase().equals("rather than"))
            {
                textToCheck ="than"; 
            }
            else if(textToCheck.toLowerCase().equals("other than"))
            {
                textToCheck ="than"; 
            }
            else if(textToCheck.toLowerCase().equals("with the exception of"))
            {
                textToCheck ="of"; 
            }
            // outer loop for the HEAD scope marker ...
            for(int index =0; index< currentList.size(); index++)
            {
                currentID = currentList.get(index).intValue();
                Annotation scopeTest = syntaxTreeNodeAnnotationSet.get((int)currentID);
                String compareText = scopeTest.getFeatures().get("text").toString().trim();
                if(compareText.toLowerCase().equals(textToCheck.toLowerCase()))
                {
                    if(currentID == syntaxNodeIDOfNegTrigger)
                    {
                            //do nothing
                    }
                       
                }
                    
                else if(compareText.toLowerCase().contains(textToCheck.toLowerCase()))
                {
                    if(scopeTest.getFeatures().get("consists")!=null)
                    {
                        findTheBaseTriggerConstiuent(syntaxNodeIDOfNegTrigger,currentID,listToReturn,syntaxTreeNodeAnnotationSet,textToCheck);
                    }
                        
                    else if(scopeTest.getStartNode().getOffset() > negNode.getStartNode().getOffset())
                    {
                        listToReturn.add(currentList.get(index).intValue());
                            
                    }
                        
                        
                }
                
                else if(scopeTest.getStartNode().getOffset() > negNode.getStartNode().getOffset())
                {
                    listToReturn.add(currentList.get(index).intValue());
                    
                }
            }// for
        }//if
		return listToReturn;
	}


    /***************************************************************************
     * method name: findTheBaseTriggerConstiuent()
     * returns: an Integer list of constituent id's
     * This method is a helper method for the changeSpan methods.
     * function: the purpose of this method is to find the constituent in the 
     * given list of constituents that matches the id of the foundational 
     * negTrigger constituent (a terminal node). Upon finding the correct 
     * constituent we then only return the constituent id's that are greater 
     * than that of the negTrigger constituent.    
     *
     ****************************************************************************/	
    protected static ArrayList <Integer> findTheBaseTriggerConstiuent(int syntaxNodeIDOfNegTrigger,int syntaxNodeIDOfCurrentConstituent,ArrayList <Integer> listToReturn,AnnotationSet syntaxTreeNodeAnnotationSet,String textToCheck)
    {
        String [] extractedResults = breakDownConstituent(syntaxNodeIDOfCurrentConstituent,syntaxTreeNodeAnnotationSet);
         Annotation negNode =  syntaxTreeNodeAnnotationSet.get(syntaxNodeIDOfNegTrigger);
        if(extractedResults !=null)
        {
            for(int i=0; i< extractedResults.length; i++)
            {
                int currentID =Integer.parseInt(extractedResults[i].trim());
                Annotation scopeTestM = syntaxTreeNodeAnnotationSet.get((int)currentID);
                String compareTextM = scopeTestM.getFeatures().get("text").toString().trim();
                if(compareTextM.toLowerCase().equals(textToCheck.toLowerCase()))
                {
                    if(currentID == syntaxNodeIDOfNegTrigger)
                    {
                        //do nothing
                    }
                }
                
                else if(compareTextM.toLowerCase().contains(textToCheck.toLowerCase()))
                {
                    if(scopeTestM.getFeatures().get("consists")!=null)
                    {
                        findTheBaseTriggerConstiuent(syntaxNodeIDOfNegTrigger,currentID,listToReturn,syntaxTreeNodeAnnotationSet,textToCheck);
                    }
                    
                    else if(scopeTestM.getStartNode().getOffset() > negNode.getStartNode().getOffset())
                    {
                        listToReturn.add(currentID);
                        
                    }
                    
                  
                }
                
                else if(scopeTestM.getStartNode().getOffset() > negNode.getStartNode().getOffset())
                {
                    listToReturn.add(currentID);
                    
                }
            }
            
            
        }
       return listToReturn; 
        
    }
    /***************************************************************************
     * method name: breakDownConstituent()
     * returns: a String Array containing the contents of a constituent's "consists" 
     * feature.
     * This method is a helper method for the findTheBaseTriggerConstiuent method.
     * function: the purpose of this method is to extract the id's of the constituents 
     * that are children of the constituent passed in the arguement to the method.    
     *
     ****************************************************************************/	
    protected static String[]  breakDownConstituent(long syntaxNodeFound,AnnotationSet syntaxTreeNodeAnnotationSet)
    {
        Annotation syntaxNodeConst = syntaxTreeNodeAnnotationSet.get((int)syntaxNodeFound);
		FeatureMap syntaxFeatureMap = syntaxNodeConst.getFeatures();
        String[] extractedResults =null;
        if(syntaxFeatureMap.containsKey("consists"))
        {
            String entireConsistsFeature = syntaxFeatureMap.get("consists").toString().trim();
            String tempSubstring = entireConsistsFeature.substring(1,entireConsistsFeature.length()-1);
            extractedResults = tempSubstring.split("\\,");
        }
        return extractedResults;

        
    }
    /***************************************************************************
     * method name: findTheParentIDListInParseTree()
     * returns: a list of Integers containing the id's of the constituents along 
     * the path of a given token (until the root of the parse tree is reached).
     * This method is a helper method for the generatePathsForToken() in 
     * the GenericScopeHelper class.
     * function: the purpose of this method is to extract all of the ancestors of 
     * a given token and recursively place those id's in a list to be returned.
     *
     ****************************************************************************/	
    protected static ArrayList<Integer> findTheParentIDListInParseTree(int headID,AnnotationSet syntaxTreeNodeAnnotationSet, ArrayList<Integer> tempPathList)
    {
        int currentParentID = findTheParent(headID,syntaxTreeNodeAnnotationSet);
        // base case
        if(haveReachedRoot(currentParentID,syntaxTreeNodeAnnotationSet))
        {
            
        }
        else
        {
            Integer tempInt = new Integer(currentParentID);
            tempPathList.add(tempInt);
            //recursive case
            findTheParentIDListInParseTree(currentParentID,syntaxTreeNodeAnnotationSet, tempPathList);
            
            
        }
        return tempPathList;
        
    }

   
    /***************************************************************************
     * method name: changeSpanForCommaN()
     * returns: a revised list of constituents which represents the scope for
     * the current negation trigger.
     * This method is a helper method to other methods in the class.
     * function: this method will find and remove the negation trigger
     * from the currently determined scope.
     *
     ****************************************************************************/
	protected static ArrayList <Integer> changeSpanForCommaN(ArrayList<Integer> currentIdList,AnnotationSet syntaxTreeNodeAnnotationSet, AnnotationSet tokenAnnotationSetInSentence,Annotation triggerMarker)
	{
        ArrayList <Integer> listToReturn = new ArrayList();
        if(currentIdList !=null)
        {
            int currentID =-1;
            // go through each id
            boolean changedDeeper =false;
           
            for(int index =0; index< currentIdList.size(); index++)
            {
                if(changedDeeper==false)
                {
                    currentID = currentIdList.get(index).intValue();
                    
                    Annotation scopeTest = syntaxTreeNodeAnnotationSet.get((int)currentID);
                    if(scopeTest.getFeatures().get("text").toString().trim().contains(" though ")||scopeTest.getFeatures().get("text").toString().trim().contains(" however ")||scopeTest.getFeatures().get("text").toString().trim().contains("despite")||
                       scopeTest.getFeatures().get("text").toString().trim().contains(" because ")||
                       scopeTest.getFeatures().get("text").toString().trim().contains(" thus ")||
                       scopeTest.getFeatures().get("text").toString().trim().contains(" whereas ")||
                       scopeTest.getFeatures().get("text").toString().trim().contains(" since ")||
                       scopeTest.getFeatures().get("text").toString().trim().contains(" thereby ")||
                       //scopeTest.getFeatures().get("text").toString().trim().contains(",")||
                       scopeTest.getFeatures().get("text").toString().trim().contains(" but "))
                    {
                        String [] extractedResults = breakDownConstituent(currentID,syntaxTreeNodeAnnotationSet);
                        if(extractedResults!=null)
                        {
                            boolean foundTopLevel =false;
                            for(int i=0; i< extractedResults.length;i++)
                            {
                               
                                if(foundTopLevel ==false)
                                {
                                    // 2nd check
                                    if(changedDeeper==false)
                                    {
                                        
                                        int currentIDToCheck =Integer.parseInt(extractedResults[i].trim());
                                        Annotation constituentTestM = syntaxTreeNodeAnnotationSet.get((int)currentIDToCheck);
                                        
                                        // 1st check:: discourse marker followed by "S/ SBAR /VP"
                                        if((constituentTestM.getFeatures().get("text").toString().trim().equals("though")||
                                            constituentTestM.getFeatures().get("text").toString().trim().equals("however")||
                                            //constituentTestM.getFeatures().get("text").toString().trim().equals("even")||
                                            constituentTestM.getFeatures().get("text").toString().trim().equals("despite")||
                                            constituentTestM.getFeatures().get("text").toString().trim().equals("because")||
                                            constituentTestM.getFeatures().get("text").toString().trim().equals("thus")||
                                            constituentTestM.getFeatures().get("text").toString().trim().equals("whereas")||
                                            constituentTestM.getFeatures().get("text").toString().trim().equals("since")||
                                            constituentTestM.getFeatures().get("text").toString().trim().equals("thereby")||
                                            //constituentTestM.getFeatures().get("text").toString().trim().equals(",")||
                                            constituentTestM.getFeatures().get("text").toString().trim().equals("but"))&& i< extractedResults.length-1 && constituentTestM.getStartNode().getOffset()> triggerMarker.getStartNode().getOffset())
                                        {
                                            int currentIDToCheckL =Integer.parseInt(extractedResults[i+1].trim());
                                            Annotation constituentTestML = syntaxTreeNodeAnnotationSet.get((int)currentIDToCheckL);
                                            String test = constituentTestML.getFeatures().get("text").toString().trim();
                                            
                                            if(constituentTestML.getFeatures().get("cat").toString().trim().equals("SBAR") || constituentTestML.getFeatures().get("cat").toString().trim().equals("S")||constituentTestML.getFeatures().get("cat").toString().trim().equals("PP"))
                                            {
                                                foundTopLevel =true;
                                            }
                                        }
                                        // 2nd check -- ,
                                        else if (constituentTestM.getFeatures().get("text").toString().trim().equals(",")&& i< extractedResults.length-1 && constituentTestM.getStartNode().getOffset()> triggerMarker.getStartNode().getOffset())
                                        {
                                                Annotation constituentTestCheckForSBAR = syntaxTreeNodeAnnotationSet.get(Integer.parseInt(extractedResults[i+1].trim()));
                                                String test = constituentTestCheckForSBAR.getFeatures().get("text").toString().trim();
                                            
                                            if((constituentTestCheckForSBAR.getFeatures().get("cat").toString().trim().equals("S")||(constituentTestCheckForSBAR.getFeatures().get("cat").toString().trim().equals("SBAR")))&&
                                             constituentTestCheckForSBAR.getFeatures().get("text").toString().trim().startsWith("that")==false &&
                                             constituentTestCheckForSBAR.getFeatures().get("text").toString().trim().startsWith("who")==false &&
                                             constituentTestCheckForSBAR.getFeatures().get("text").toString().trim().startsWith("whom")==false &&
                                             constituentTestCheckForSBAR.getFeatures().get("text").toString().trim().startsWith("which") ==false)

                                                    
                                                {
                                                    
                                                    foundTopLevel =true;
                                                    
                                                    
                                                }
                                               
                                                else if (constituentTestCheckForSBAR.getFeatures().get("cat").toString().trim().equals("CC"))
                                                {
                                                    Annotation constituentTestCheckForSBAROnCC = syntaxTreeNodeAnnotationSet.get(Integer.parseInt(extractedResults[i+2].trim()));
                                                    if(constituentTestCheckForSBAR.getFeatures().get("text").toString().trim().equals("and") && (constituentTestCheckForSBAROnCC.getFeatures().get("cat").toString().trim().equals("S")||constituentTestCheckForSBAROnCC.getFeatures().get("cat").toString().trim().equals("SBAR")))
                                                    {
                                                       foundTopLevel =true;
                                                    }
                                                }
                                                
                                        }// end 2nd check*/
                                        
                                        // 3rd check - contains ... 
                                        else if(constituentTestM.getFeatures().get("text").toString().trim().contains(" though ")||
                                                constituentTestM.getFeatures().get("text").toString().trim().contains(" however ")||
                                                //constituentTestM.getFeatures().get("text").toString().trim().contains(" even ")||
                                                constituentTestM.getFeatures().get("text").toString().trim().contains(" despite ")||
                                                constituentTestM.getFeatures().get("text").toString().trim().contains(" because ")||
                                               constituentTestM.getFeatures().get("text").toString().trim().contains(" thus ")||
                                                constituentTestM.getFeatures().get("text").toString().trim().contains(" whereas ")||
                                               constituentTestM.getFeatures().get("text").toString().trim().contains(" since ")||
                                                constituentTestM.getFeatures().get("text").toString().trim().contains(" thereby ")||
                                                //constituentTestM.getFeatures().get("text").toString().trim().contains(",")||
                                               constituentTestM.getFeatures().get("text").toString().trim().contains(" but "))
                                        {
                                            changedDeeper = changeSpanForContrastWithOneConstiutent(listToReturn,syntaxTreeNodeAnnotationSet,tokenAnnotationSetInSentence,currentIDToCheck,false);
                                        
                                        }
                                       
                                        else if (foundTopLevel==false && changedDeeper ==false)
                                        {
                                            listToReturn.add(currentIDToCheck);
                                        }
                                       
                                    }//if changedDeeper ==false
                                   
                                }//if found topLevel==false
                                
                               
                            }// for
                        }// if extractedResults has items
                        else if (changedDeeper ==false)
                        {
                            listToReturn.add(currentIdList.get(index));
                        }
                        
                    }//if scopeTest contains comma
                    else if (changedDeeper ==false)
                    {
                        listToReturn.add(currentIdList.get(index));
                    }
                } 
            }//for
            
        }
        
        return listToReturn;
    }

    /***************************************************************************
    * method name: changeSpanForContrastWithOneConstituent()
    * returns: a revised list of constituents which represents the scope for
    * the current negation trigger.
    * This method is a helper method to other methods in the class.
    * function: this method will find and remove the negation trigger
    * from the currently determined scope.
    *
    ****************************************************************************/
    protected static boolean changeSpanForContrastWithOneConstiutent(ArrayList<Integer> listToReturn,AnnotationSet syntaxTreeNodeAnnotationSet,AnnotationSet tokenAnnotationSetInSentence, int currentIDToCheck,boolean foundSBAR)
    {
        String [] extractedResults = breakDownConstituent(currentIDToCheck,syntaxTreeNodeAnnotationSet);
        if(extractedResults!=null && foundSBAR ==false)
        {
           // boolean foundSBAR =false;
            int index =0;
            while(foundSBAR == false && index< extractedResults.length)
            {
                int currentIDToCheckDeeper =Integer.parseInt(extractedResults[index].trim());
                Annotation constituentTestT = syntaxTreeNodeAnnotationSet.get((int)currentIDToCheckDeeper);
                
                if(constituentTestT.getFeatures().get("text").toString().trim().equals("though")||
                   constituentTestT.getFeatures().get("text").toString().trim().equals("however")||
                   //constituentTestT.getFeatures().get("text").toString().trim().equals("even")||
                   constituentTestT.getFeatures().get("text").toString().trim().equals("despite")||
                   constituentTestT.getFeatures().get("text").toString().trim().equals("because")||
                   constituentTestT.getFeatures().get("text").toString().trim().equals("thus")||
                   constituentTestT.getFeatures().get("text").toString().trim().equals("whereas")||
                   constituentTestT.getFeatures().get("text").toString().trim().equals("since")||
                   constituentTestT.getFeatures().get("text").toString().trim().equals("thereby")||
                   //constituentTestT.getFeatures().get("text").toString().trim().equals(",")||
                   constituentTestT.getFeatures().get("text").toString().trim().equals("but"))
                {
                    
                    if(index<extractedResults.length-1)
                    {
                        Annotation constituentTestCheckForSBAR = syntaxTreeNodeAnnotationSet.get(Integer.parseInt(extractedResults[index+1].trim()));
                        String test = constituentTestCheckForSBAR.getFeatures().get("text").toString().trim();
                        if(constituentTestCheckForSBAR.getFeatures().get("cat").toString().trim().equals("S")||(constituentTestCheckForSBAR.getFeatures().get("cat").toString().trim().equals("SBAR")))
                           //||(constituentTestCheckForSBAR.getFeatures().get("cat").toString().trim().equals("VP")))
                            
                        {
                            
                            foundSBAR =true;
                            
                        }
                        
                        else
                        {
                        
                            listToReturn.add(currentIDToCheckDeeper);
                            index++;
                        }
                                                                                    
                    } //length
                    else
                    {
                      listToReturn.add(currentIDToCheckDeeper);
                        index++;
                    }
                    
                } //equals
                
                else if (constituentTestT.getFeatures().get("text").toString().trim().equals(","))
                {
                    if(index<extractedResults.length-1)
                    {
                        Annotation constituentTestCheckForSBAR = syntaxTreeNodeAnnotationSet.get(Integer.parseInt(extractedResults[index+1].trim()));
                        String test = constituentTestCheckForSBAR.getFeatures().get("text").toString().trim();
                        if((constituentTestCheckForSBAR.getFeatures().get("cat").toString().trim().equals("S")||(constituentTestCheckForSBAR.getFeatures().get("cat").toString().trim().equals("SBAR")))&&
                           constituentTestCheckForSBAR.getFeatures().get("text").toString().trim().startsWith("that")==false &&
                           constituentTestCheckForSBAR.getFeatures().get("text").toString().trim().startsWith("who")==false &&
                           constituentTestCheckForSBAR.getFeatures().get("text").toString().trim().startsWith("whom")==false &&
                           constituentTestCheckForSBAR.getFeatures().get("text").toString().trim().startsWith("which") ==false)
                            
                        {
                            
                            foundSBAR =true;
                           
                            
                        }
                       
                        else if (constituentTestCheckForSBAR.getFeatures().get("cat").toString().trim().equals("CC"))
                        {
                            Annotation constituentTestCheckForSBAROnCC = syntaxTreeNodeAnnotationSet.get(Integer.parseInt(extractedResults[index+2].trim()));
                            if(constituentTestCheckForSBAR.getFeatures().get("text").toString().trim().equals("and") && (constituentTestCheckForSBAROnCC.getFeatures().get("cat").toString().trim().equals("S")||constituentTestCheckForSBAROnCC.getFeatures().get("cat").toString().trim().equals("SBAR")))
                            {
                              foundSBAR =true;
                            }
                            else
                            {
                                
                                listToReturn.add(currentIDToCheckDeeper);
                                index++;
                            }
                        }
                        else if ((constituentTestCheckForSBAR.getFeatures().get("cat").toString().trim().equals("NP")||constituentTestCheckForSBAR.getFeatures().get("cat").toString().trim().equals("NNP")) && Character.isUpperCase(test.charAt(0))&& Character.isLowerCase(test.charAt(1)))
                        {
                            foundSBAR =true;
                            
                        }
                        
                        else if((constituentTestCheckForSBAR.getFeatures().get("cat").toString().trim().equals("PP")) && (constituentTestCheckForSBAR.getFeatures().get("text").toString().trim().startsWith("with ")||constituentTestCheckForSBAR.getFeatures().get("text").toString().trim().startsWith("without ") ))
                        {
                            foundSBAR=true;
                        }
                    
                        else if ((constituentTestCheckForSBAR.getFeatures().get("cat").toString().trim().equals("NP"))&& (constituentTestCheckForSBAR.getFeatures().get("text").toString().trim().startsWith("a ")||constituentTestCheckForSBAR.getFeatures().get("text").toString().trim().startsWith("the ")))
                        {
                            foundSBAR =true;
                            
                        }

                        
                        else
                        {
                            
                            listToReturn.add(currentIDToCheckDeeper);
                            index++;
                        }
                        
                    } //length
                    else
                    {
                        
                        listToReturn.add(currentIDToCheckDeeper);
                        index++;
                    }
                    
                } 
                
                else if(constituentTestT.getFeatures().get("text").toString().trim().contains(" though ")||
                        constituentTestT.getFeatures().get("text").toString().trim().contains(" however ")||
                        //constituentTestT.getFeatures().get("text").toString().trim().contains(" even ")||
                        constituentTestT.getFeatures().get("text").toString().trim().contains(" despite ")||
                        constituentTestT.getFeatures().get("text").toString().trim().contains(" because ")||
                        constituentTestT.getFeatures().get("text").toString().trim().contains(" thus ")||
                        constituentTestT.getFeatures().get("text").toString().trim().contains(" whereas ")||
                        constituentTestT.getFeatures().get("text").toString().trim().contains(" since ")||
                        constituentTestT.getFeatures().get("text").toString().trim().contains(" thereby ")||
                        constituentTestT.getFeatures().get("text").toString().trim().contains(",")||
                        constituentTestT.getFeatures().get("text").toString().trim().contains(" but "))
                {
                    
                    foundSBAR = changeSpanForContrastWithOneConstiutent(listToReturn,syntaxTreeNodeAnnotationSet,tokenAnnotationSetInSentence,currentIDToCheckDeeper,foundSBAR);
                    index++;
                    
                    
                }

                else if(foundSBAR==false)
                {
                    listToReturn.add(currentIDToCheckDeeper);
                    index++;
                }
                
                
            }// while
        }// if extractedResults has items
        return foundSBAR;

        
            
    }
    /***************************************************************************
     * method name: changeSpanForContrast()
     * returns: a revised list of constituents which represents the scope for
     * the current negation trigger.
     * This method is a helper method to other methods in the class.
     * function: this method will find and remove the negation trigger
     * from the currently determined scope.
     *
     ****************************************************************************/
	protected static ArrayList <Integer> changeSpanForPunctuation(ArrayList<Integer> currentIdList,AnnotationSet syntaxTreeNodeAnnotationSet, AnnotationSet tokenAnnotationSetInSentence,String classType)
	{
        ArrayList <Integer> listToReturn = new ArrayList();
        if(currentIdList !=null)
        {
            //String [] contrastWords = {"but",".","?","!",";",":","also","however","--",};
            //String [] contrastWords = {"because","but","although","eventhough",".","?","!",";","except",":","despite","also","however","--","unless"};
            //changed June 19th
            // String [] contrastWords = {"because","but","although","eventhough",".","?","!",";","except",":","despite","however","--","unless"};
             String [] contrastWords = {"because","but","although","eventhough","?","!",";","except",":","despite","however","--","unless"};
            if(classType.equals("NegTrigger") ==false)
            {
                contrastWords[contrastWords.length-1]="";
               
                contrastWords[contrastWords.length-2]="";
                contrastWords[contrastWords.length-3]="";
            }
            int currentID =-1;
            String textToCheck = "";
            Annotation toReturnMatch =null;
            boolean foundMatchToText = false;
            // go through each id
            for(int index =0; index< currentIdList.size(); index++)
            {
                currentID = currentIdList.get(index).intValue();
                
                Annotation scopeTest = syntaxTreeNodeAnnotationSet.get((int)currentID);
                // get all the tokens within this annotation- make a list and sort them ...
                Long startST = scopeTest.getStartNode().getOffset();
                Long endST = scopeTest.getEndNode().getOffset();
                AnnotationSet tokenAnnotationSetInScope = tokenAnnotationSetInSentence.getContained(startST,endST);
                ArrayList<Annotation> tokensInScope = new ArrayList<Annotation>(tokenAnnotationSetInScope);
                OffsetComparator offsetComparatorForTokens = new OffsetComparator();
                Collections.sort(tokensInScope, offsetComparatorForTokens);
                for(int i =0; i< tokensInScope.size(); i++)
                {
                    String compareText = tokensInScope.get(i).getFeatures().get("string").toString().trim().toLowerCase();
                    int indexC =0;
                    while(foundMatchToText == false && indexC < contrastWords.length)
                    {
                        textToCheck = contrastWords[indexC];
                        if (textToCheck.toLowerCase().equals(compareText.toLowerCase()))
                        {
                            foundMatchToText = true;
                            toReturnMatch = tokensInScope.get(i);
                            break;
                        }
                        else
                        {
                            indexC++;
                        }
                    }
                }
                
                if ((foundMatchToText == true))
                {
                    if (scopeTest.coextensive(toReturnMatch))
                    {
                    }
                    else if(toReturnMatch.withinSpanOf(scopeTest))
                    {
                        findTheBaseTriggerConstiuentForPunctuation(currentID,listToReturn,syntaxTreeNodeAnnotationSet,textToCheck,tokenAnnotationSetInSentence,toReturnMatch);
                        
                    }
                }
                else if(foundMatchToText == false)
                {
                    listToReturn.add(currentIdList.get(index));
                    
                }
            }// for
            //}//while
        }//if
		return listToReturn;
        
    }
    /***************************************************************************
     * method name: changeSpanForContrastNOR()
     * returns: a revised list of constituents which represents the scope for
     * the current negation trigger.
     * This method is a helper method to other methods in the class.
     * function: this method will find and remove the negation trigger
     * from the currently determined scope.
     *
     ****************************************************************************/
	protected static ArrayList <Integer> changeSpanForNor(ArrayList<Integer> currentIdList,AnnotationSet syntaxTreeNodeAnnotationSet, AnnotationSet tokenAnnotationSetInSentence,String classType)
	{
        ArrayList <Integer> listToReturn = new ArrayList();
        if(currentIdList !=null)
        {
            //String [] contrastWords = {"but",".","?","!",";",":","also","however","--",};
            String [] contrastWords = {"because","but","although","eventhough",".","?","!",";","except",":","despite","also","however","--","unless","nor"};
            if(classType.equals("NegTrigger") ==false)
            {
                contrastWords[contrastWords.length-1]="";
                
                contrastWords[contrastWords.length-2]="";
                contrastWords[contrastWords.length-3]="";
            }
            int currentID =-1;
            String textToCheck = "";
            Annotation toReturnMatch =null;
            boolean foundMatchToText = false;
            // go through each id
            for(int index =0; index< currentIdList.size(); index++)
            {
                currentID = currentIdList.get(index).intValue();
                
                Annotation scopeTest = syntaxTreeNodeAnnotationSet.get((int)currentID);
                // get all the tokens within this annotation- make a list and sort them ...
                Long startST = scopeTest.getStartNode().getOffset();
                Long endST = scopeTest.getEndNode().getOffset();
                AnnotationSet tokenAnnotationSetInScope = tokenAnnotationSetInSentence.getContained(startST,endST);
                ArrayList<Annotation> tokensInScope = new ArrayList<Annotation>(tokenAnnotationSetInScope);
                OffsetComparator offsetComparatorForTokens = new OffsetComparator();
                Collections.sort(tokensInScope, offsetComparatorForTokens);
                for(int i =0; i< tokensInScope.size(); i++)
                {
                    String compareText = tokensInScope.get(i).getFeatures().get("string").toString().trim().toLowerCase();
                    int indexC =0;
                    while(foundMatchToText == false && indexC < contrastWords.length)
                    {
                        textToCheck = contrastWords[indexC];
                        if (textToCheck.toLowerCase().equals(compareText.toLowerCase()))
                        {
                            foundMatchToText = true;
                            toReturnMatch = tokensInScope.get(i);
                            break;
                        }
                        else
                        {
                            indexC++;
                        }
                    }
                }
                
                if ((foundMatchToText == true))
                {
                    if (scopeTest.coextensive(toReturnMatch))
                    {
                    }
                    else if(toReturnMatch.withinSpanOf(scopeTest))
                    {
                        findTheBaseTriggerConstiuentForPunctuation(currentID,listToReturn,syntaxTreeNodeAnnotationSet,textToCheck,tokenAnnotationSetInSentence,toReturnMatch);
                        
                    }
                }
                else if(foundMatchToText == false)
                {
                    listToReturn.add(currentIdList.get(index));
                    
                }
            }// for
            //}//while
        }//if
		return listToReturn;
        
    }

   
    /***************************************************************************
     * method name: findTheBaseTriggerConstiuentForContrast()
     * returns: an Integer list of constituent id's
     * This method is a helper method for the changeSpan methods.
     * function: the purpose of this method is to find the constituent in the
     * given list of constituents that matches the id of the foundational
     * negTrigger constituent (a terminal node). Upon finding the correct
     * constituent we then only return the constituent id's that are greater
     * than that of the negTrigger constituent.
     *
     ****************************************************************************/
    protected static ArrayList <Integer> findTheBaseTriggerConstiuentForPunctuation(int syntaxNodeIDOfCurrentConstituent,ArrayList <Integer> listToReturn,AnnotationSet syntaxTreeNodeAnnotationSet,String textToCheck, AnnotationSet tokenAnnotationSetInSentence, Annotation toReturnMatch)
    {
        String [] extractedResults = breakDownConstituent(syntaxNodeIDOfCurrentConstituent,syntaxTreeNodeAnnotationSet);
        if(extractedResults !=null)
        {
            
            for(int i=0; i< extractedResults.length; i++)
            {
                int currentID =Integer.parseInt(extractedResults[i].trim());
                Annotation scopeTestM = syntaxTreeNodeAnnotationSet.get((int)currentID);
                Long startST = scopeTestM.getStartNode().getOffset();
                Long endST = scopeTestM.getEndNode().getOffset();
                AnnotationSet tokenAnnotationSetInScope = tokenAnnotationSetInSentence.getContained(startST,endST);
                ArrayList<Annotation> tokensInScope = new ArrayList<Annotation>(tokenAnnotationSetInScope);
                OffsetComparator offsetComparatorForTokens = new OffsetComparator();
                Collections.sort(tokensInScope, offsetComparatorForTokens);
                boolean foundMatchToText = false;
                for(int j=0; j< tokensInScope.size(); j++)
                {
                    String compareText = tokensInScope.get(j).getFeatures().get("string").toString().trim().toLowerCase();
                    if (textToCheck.toLowerCase().equals(compareText.toLowerCase()))
                    {
                        foundMatchToText = true;
                        break;
                    }
                }
                
                if (foundMatchToText == true)
                {
                    if (scopeTestM.coextensive(toReturnMatch))
                    {
                    }
                    else if(toReturnMatch.withinSpanOf(scopeTestM))
                    {
                        findTheBaseTriggerConstiuentForPunctuation(currentID,listToReturn,syntaxTreeNodeAnnotationSet,textToCheck,tokenAnnotationSetInSentence,toReturnMatch);
                        
                    }
                }
                else if(foundMatchToText ==false && scopeTestM.getEndNode().getOffset()< toReturnMatch.getEndNode().getOffset())
                {
                    listToReturn.add(Integer.parseInt(extractedResults[i].trim()));
                }
            }
            
        }
        
        return listToReturn;
        
    }
    

        /***************************************************************************
     * NEW::: for nsubj:: method name: cleanUpSpanFromParseTreeLeft()
     * returns: a revised list of constituents which represents the scope for
     * the current negation trigger.
     * This method is a helper method to other methods in the class.
     * function: this method will find and remove the negation trigger
     * from the currently determined scope.
     *
     ****************************************************************************/
	protected static ArrayList <Integer> cleanUpSpanFromParseTreeLeft(ArrayList<Integer> idsOfConstituentsInScope,AnnotationSet syntaxTreeNodeAnnotationSet)
	{
        ArrayList<Integer> newList = null;
        
        if(idsOfConstituentsInScope!=null)
        {
            int currentID = -1;
            boolean found =false;
            int index =0;
            int lastID = idsOfConstituentsInScope.get(idsOfConstituentsInScope.size()-1);
            Annotation scopeTestA = syntaxTreeNodeAnnotationSet.get(lastID);
          
            if(scopeTestA.getFeatures().get("text").toString().trim().indexOf(",")!=-1)
            {
                return newList;
            }
           
            else if(scopeTestA.getFeatures().get("text").toString().trim().endsWith("with"))
            {
                return newList;
            }
            else
            {
                newList = new ArrayList<Integer>();
                while(index < idsOfConstituentsInScope.size() && found ==false)
                {
                    currentID = idsOfConstituentsInScope.get(index).intValue();
                    Annotation scopeTest = syntaxTreeNodeAnnotationSet.get((int)currentID);
                    if(scopeTest.getFeatures().get("cat").toString().trim().startsWith("NP") ==false && scopeTest.getFeatures().get("cat").toString().trim().startsWith("DT") ==false && scopeTest.getFeatures().get("cat").toString().trim().startsWith("AUX") ==false && scopeTest.getFeatures().get("cat").toString().trim().startsWith("PRP") ==false && scopeTest.getFeatures().get("cat").toString().trim().startsWith("W") ==false)
                    {
                        index++;
                    }
                    else
                    {
                        newList.add(idsOfConstituentsInScope.get(index));
                        found =true;
                    }
                    
                }// while
                
                while(index+1 < idsOfConstituentsInScope.size())
                {
                    index++;
                    newList.add(idsOfConstituentsInScope.get(index));
                    
                }
            }//if
        }
		return newList;
    }
    /***************************************************************************
     * NEW::: method name:for nsubj:: changeSpanForLeftSpanScope()
     * returns: a revised list of constituents which represents the scope for 
     * the current negation trigger.
     * This method is a helper method to other methods in the class.
     * function: this method will find and remove the negation trigger 
     * from the currently determined scope. 
     *
     ****************************************************************************/	
	protected static ArrayList <Integer> changeSpanForLeftSpanScope(int syntaxNodeIDOfNegTrigger,Annotation markerToRemoveFromSpan,long syntaxNodeFound,AnnotationSet syntaxTreeNodeAnnotationSet)
	{
        
        Annotation negSyntaxNode = syntaxTreeNodeAnnotationSet.get(syntaxNodeIDOfNegTrigger);
        String [] extractedResults = breakDownConstituent(syntaxNodeFound,syntaxTreeNodeAnnotationSet);
        ArrayList <Integer> listToReturn = new ArrayList();
        if(extractedResults !=null)
        {
            int currentID =-1;
            String textToCheck = null;
            if(markerToRemoveFromSpan.getFeatures().get("String")!=null)
            {
                textToCheck = markerToRemoveFromSpan.getFeatures().get("String").toString().trim();
            }
            else
            {
                textToCheck = markerToRemoveFromSpan.getFeatures().get("string").toString().trim();
            }

            // outer while loop for the HEAD scope marker ...
            for(int index =0; index< extractedResults.length; index++)
            {
                currentID =Integer.parseInt(extractedResults[index].trim());
                Annotation scopeTest = syntaxTreeNodeAnnotationSet.get((int)currentID);
                String compareText = scopeTest.getFeatures().get("text").toString().trim();
                
                String [] compareTextSplit = compareText.split(" ");
                boolean foundMatchToText = false;
                for(int i =0; i< compareTextSplit.length; i++)
                {
                   
                    if(compareTextSplit[i].toLowerCase().equals(textToCheck.toLowerCase()))
                    {
                        foundMatchToText = true;
                        break;
                    }
                }
                
                
                if ((foundMatchToText == true) &&( currentID > syntaxNodeIDOfNegTrigger))
                { 
                    findTheBaseTriggerConstiuentForNsubj(syntaxNodeIDOfNegTrigger,currentID,listToReturn,syntaxTreeNodeAnnotationSet,textToCheck);
                }
                
                else if( currentID < syntaxNodeIDOfNegTrigger && foundMatchToText == false)
                {
                    listToReturn.add(Integer.parseInt(extractedResults[index].trim()));
                    
                }
            }// for
        }//if
		return listToReturn;
	}
    /***************************************************************************
     * NEW::: method name:for nsubj:: findTheBaseTriggerConstiuentForNsubj()
     * returns: an Integer list of constituent id's
     * This method is a helper method for the changeSpan methods.
     * function: the purpose of this method is to find the constituent in the 
     * given list of constituents that matches the id of the foundational 
     * negTrigger constituent (a terminal node). Upon finding the correct 
     * constituent we then only return the constituent id's that are greater 
     * than that of the negTrigger constituent.    
     *
     ****************************************************************************/	
    protected static ArrayList <Integer> findTheBaseTriggerConstiuentForNsubj(int syntaxNodeIDOfNegTrigger,int syntaxNodeIDOfCurrentConstituent,ArrayList <Integer> listToReturn,AnnotationSet syntaxTreeNodeAnnotationSet,String textToCheck)
    {
        String [] extractedResults = breakDownConstituent(syntaxNodeIDOfCurrentConstituent,syntaxTreeNodeAnnotationSet);
        if(extractedResults !=null)
        {
            
            for(int i=0; i< extractedResults.length; i++)
            {
                int currentID =Integer.parseInt(extractedResults[i].trim());
                Annotation scopeNegTest = syntaxTreeNodeAnnotationSet.get((int)syntaxNodeIDOfNegTrigger);
                Annotation scopeTestM = syntaxTreeNodeAnnotationSet.get((int)currentID);
                String compareTextM = scopeTestM.getFeatures().get("text").toString().trim();
                String [] compareTextSplit = compareTextM.split(" ");
                boolean foundMatchToText = false;
                for(int j=0; j< compareTextSplit.length; j++)
                {
                    if (compareTextSplit[j].toLowerCase().contains(textToCheck.toLowerCase()))
                    {
                        foundMatchToText = true;
                        break;
                    }
                }
                
                if (foundMatchToText == true)
                {
                   
                    if (currentID == syntaxNodeIDOfNegTrigger)
                    {
                        //do nothing
                    }
                  
                    else if(currentID > syntaxNodeIDOfNegTrigger)
                    {
                        findTheBaseTriggerConstiuentForNsubj(syntaxNodeIDOfNegTrigger,currentID,listToReturn,syntaxTreeNodeAnnotationSet,textToCheck);
                        
                    }
                }
                else if( currentID < syntaxNodeIDOfNegTrigger)
                {
                    listToReturn.add(Integer.parseInt(extractedResults[i].trim()));
                }
            }
            
        }
        
        return listToReturn; 
        
    }
    /***************************************************************************
     * NEW:::method name: for nsubj:: findTheCommonParentAndSConstInParseTree()
     * returns: an integer ID of the found constituent
     * function: this method will traverse the parse tree and upon finding the 
     * correct constituent  - based on the condition that the found constituent 
     * contains both an ancestor of the negTrigger and of the init trigger - 
     * it will return the ID of the found constituent.
     *
     ****************************************************************************/	
	protected static int findTheCommonParentAndSConstInParseTree(ArrayList<Integer> parentListOfNegTrigger,ArrayList<Integer> parentListOfInitTrigger, AnnotationSet syntaxTreeNodeAnnotationSet)
	{
       
        for(int indexN =0; indexN< parentListOfNegTrigger.size(); indexN++)
        {
            int currentParentNegID = parentListOfNegTrigger.get(indexN).intValue();
            for(int indexI =0; indexI< parentListOfInitTrigger.size();indexI++)
            {
                int currentParentInitID = parentListOfInitTrigger.get(indexI).intValue();
                Annotation test = syntaxTreeNodeAnnotationSet.get(currentParentInitID);
                if (currentParentNegID == currentParentInitID  && test.getFeatures().get("cat").toString().trim().equals("S"))
                {
                    return currentParentNegID;
                }
                
            }
        }

        for(int indexN =0; indexN< parentListOfNegTrigger.size(); indexN++)
        {
            int currentParentNegID = parentListOfNegTrigger.get(indexN).intValue();
            for(int indexI =0; indexI< parentListOfInitTrigger.size();indexI++)
            {
                int currentParentInitID = parentListOfInitTrigger.get(indexI).intValue();
                Annotation test = syntaxTreeNodeAnnotationSet.get(currentParentInitID);
                if (currentParentNegID == currentParentInitID  && test.getFeatures().get("cat").toString().trim().equals("SQ"))
                {
                    return currentParentNegID;
                }
                
            }
        }
        
        for(int indexN =0; indexN< parentListOfNegTrigger.size(); indexN++)
        {
            int currentParentNegID = parentListOfNegTrigger.get(indexN).intValue();
            for(int indexI =0; indexI< parentListOfInitTrigger.size();indexI++)
            {
                int currentParentInitID = parentListOfInitTrigger.get(indexI).intValue();
                Annotation test = syntaxTreeNodeAnnotationSet.get(currentParentInitID);
                if (currentParentNegID == currentParentInitID  && test.getFeatures().get("cat").toString().trim().equals("SBAR"))
                {
                    return currentParentNegID;
                }
                
            }
        }
        
        
        return -1;
		
	}
    /***************************************************************************
     * NEW:::method name: for nsubj::  findTheCommonParentAndSBARFirstConstInParseTree()
     * returns: an integer ID of the found constituent
     * function: this method will traverse the parse tree and upon finding the
     * correct constituent  - based on the condition that the found constituent
     * contains both an ancestor of the negTrigger and of the init trigger -
     * it will return the ID of the found constituent.
     *
     ****************************************************************************/
	protected static int findTheCommonParentAndSBARFirstConstInParseTree(ArrayList<Integer> parentListOfNegTrigger,ArrayList<Integer> parentListOfInitTrigger, AnnotationSet syntaxTreeNodeAnnotationSet)
	{
        for(int indexN =0; indexN< parentListOfNegTrigger.size(); indexN++)
        {
            int currentParentNegID = parentListOfNegTrigger.get(indexN).intValue();
            for(int indexI =0; indexI< parentListOfInitTrigger.size();indexI++)
            {
                int currentParentInitID = parentListOfInitTrigger.get(indexI).intValue();
                Annotation test = syntaxTreeNodeAnnotationSet.get(currentParentInitID);
                if (currentParentNegID == currentParentInitID  && test.getFeatures().get("cat").toString().trim().equals("SBAR"))
                {
                    return currentParentNegID;
                }
                
            }
        }
        
        for(int indexN =0; indexN< parentListOfNegTrigger.size(); indexN++)
        {
            int currentParentNegID = parentListOfNegTrigger.get(indexN).intValue();
            for(int indexI =0; indexI< parentListOfInitTrigger.size();indexI++)
            {
                int currentParentInitID = parentListOfInitTrigger.get(indexI).intValue();
                Annotation test = syntaxTreeNodeAnnotationSet.get(currentParentInitID);
                if (currentParentNegID == currentParentInitID  && test.getFeatures().get("cat").toString().trim().equals("SINV"))
                {
                    return currentParentNegID;
                }
                
            }
        }
        
        
        for(int indexN =0; indexN< parentListOfNegTrigger.size(); indexN++)
        {
            int currentParentNegID = parentListOfNegTrigger.get(indexN).intValue();
            for(int indexI =0; indexI< parentListOfInitTrigger.size();indexI++)
            {
                int currentParentInitID = parentListOfInitTrigger.get(indexI).intValue();
                Annotation test = syntaxTreeNodeAnnotationSet.get(currentParentInitID);
                if (currentParentNegID == currentParentInitID  && test.getFeatures().get("cat").toString().trim().equals("S"))
                {
                    return currentParentNegID;
                }
                
            }
        }
        
               
        
        
        return -1;
		
	}

    /***************************************************************************
     * method name: changeSpanForNSubjRules()*** FOR EXTENDED SPAN
     * returns: a revised list of constituents which represents the scope for 
     * the current negation trigger.
     * This method is a helper method to other methods in the class.
     * function: this method will find and remove the negation trigger 
     * from the currently determined scope. 
     *
     ****************************************************************************/	
	protected static ArrayList <Integer> changeSpanForNSubjRules(int syntaxNodeIDOfNegTrigger,Annotation markerToRemoveFromSpan,long syntaxNodeFound,AnnotationSet syntaxTreeNodeAnnotationSet)
	{
        
        
        String [] extractedResults = breakDownConstituent(syntaxNodeFound,syntaxTreeNodeAnnotationSet);
        ArrayList <Integer> listToReturn = new ArrayList();
        if(extractedResults !=null)
        {
            int currentID =-1;
          
            String textToCheck = markerToRemoveFromSpan.getFeatures().get("String").toString().trim();
            // outer while loop for the HEAD scope marker ...
            for(int index =0; index< extractedResults.length; index++)
            {
                currentID =Integer.parseInt(extractedResults[index].trim());
               
                
                Annotation scopeTest = syntaxTreeNodeAnnotationSet.get((int)currentID);
                String compareText = scopeTest.getFeatures().get("text").toString().trim();
               
                String [] compareTextSplit = compareText.split(" ");
                boolean foundMatchToText = false;
                for(int i =0; i< compareTextSplit.length; i++)
                {
                    if (compareTextSplit[i].toLowerCase().contains(textToCheck.toLowerCase()))
                    {
                        foundMatchToText = true;
                        break;
                    }
                }
                
                if ((foundMatchToText == true) &&( currentID > syntaxNodeIDOfNegTrigger))
                {
                    
                    findTheBaseTriggerConstiuentForNsubj(syntaxNodeIDOfNegTrigger,currentID,listToReturn,syntaxTreeNodeAnnotationSet,textToCheck);
                }
                
                else if( currentID < syntaxNodeIDOfNegTrigger && foundMatchToText == false)
                {
                    
                    listToReturn.add(Integer.parseInt(extractedResults[index].trim()));
                    
                }
            }// for
        }//if
		return listToReturn;
	}

} // end class



