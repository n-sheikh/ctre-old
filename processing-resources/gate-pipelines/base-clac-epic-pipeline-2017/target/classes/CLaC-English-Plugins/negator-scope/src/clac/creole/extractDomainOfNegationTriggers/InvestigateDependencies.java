/* InvestigateDependencies.java
 * Authors:
 * Date: February 2013
 * Purpose: This class implements the methods needed for extracting the necessary dependency relations
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

public abstract class InvestigateDependencies
{
    /***************************************************************************
     * method name: extractDependenciesWithTokenForCustom()
     * returns: an ArrayList of the dependencies found.
     * function: this method will extract the Dependency Relations for a Token 
     * that had custom dependency annotations added to it in the phase when the initial triggers were marked.
     * (not by the Parser) - i.e. (Nobody/ Nothing)
     *
     ****************************************************************************/
	protected static ArrayList<String> extractDependenciesWithTokenForCustom(String entireDepRelation)
	{
		Pattern matchTheString = Pattern.compile("[a-z]+\\(+[0-9]+\\)");
		String[] extractedResults = entireDepRelation.split("\\,");
		ArrayList<String>resultsToReturn = new ArrayList <String>();
		for (int i=0; i<extractedResults.length; i++)
		{
			Matcher foundAFit = matchTheString.matcher(extractedResults[i]);
	     	if (foundAFit.find()) 
			{
				String initalString = foundAFit.group();
				String[] finalResults = initalString.split("\\(");
				for (int n=0; n<finalResults.length; n++)
				{
					if(finalResults[n].endsWith(")"))
					{resultsToReturn.add(finalResults[n].substring(0,finalResults[n].length()-1));}
					else
					{resultsToReturn.add(finalResults[n]);}
					
				} 
			} 
		}// done iterating the string
		return resultsToReturn;
	}
	/***************************************************************************
     * method name: extractDependencies()
     * returns: an ArrayList of the dependencies found.
     * function: this method will extract the Dependency Relations for a given Token 
     *
     ****************************************************************************/
     protected static ArrayList<String> extractDependencies(Annotation currentTokenToFindDepsFor,ArrayList<Annotation> depRelationsListOfSentence)
     {
         ArrayList<String>resultsToReturn = new ArrayList <String>();
         for(Annotation currentDepRelation: depRelationsListOfSentence)
         {
             String theIds = currentDepRelation.getFeatures().get("args").toString().trim();
             String theIdsWithoutBrackets = theIds.substring(1,theIds.length()-1);
             String [] args  = theIdsWithoutBrackets.split("\\,");
             Integer theGovId = Integer.parseInt(args[0].trim());
             Integer tokenID = currentTokenToFindDepsFor.getId();
             if(tokenID.equals(theGovId))
             {
                 String theKind = currentDepRelation.getFeatures().get("kind").toString().trim();
                 // add the type of relation
                 resultsToReturn.add(theKind.trim());
                  // add the dep id
                 resultsToReturn.add(args[1].trim());
                
             }
           
         }
         
         return resultsToReturn;
     } // method ...
    /***************************************************************************
     * method name: findMoreDeps()
     * returns: a boolean value.
     * function: this is a helper method that when required can be used to find 
     * potentially more dependencies for a given token.
     * - specifically if one is looking for a particular dependency for the token
     *
     ****************************************************************************/
	protected static boolean findMoreDeps(Annotation tokenToGetMoreDeps, String type, boolean exactMatch,AnnotationSet depRelationsSetOfSentence)
	{
		boolean toReturn = false;
         for(Annotation currentDepRelation: depRelationsSetOfSentence)
         {
             String theIds = currentDepRelation.getFeatures().get("args").toString().trim();
             String theIdsWithoutBrackets = theIds.substring(1,theIds.length()-1);
             String [] args  = theIdsWithoutBrackets.split("\\,");
             Integer theGovId = Integer.parseInt(args[0].trim());
             Integer tokenID = tokenToGetMoreDeps.getId();
             if(tokenID.equals(theGovId))
             {
                  String theKind = currentDepRelation.getFeatures().get("kind").toString().trim();
                 if(theKind.equals(type))
                 {
                     toReturn = true;
                     return toReturn;
                }
                 // new for collapsed option
                 else if(type.equals("prep") && theKind.startsWith("prep"))
                 {
                     toReturn = true;
                     return toReturn;
                     
                 }
            }

        }
        
        return toReturn;
	}
	
    /***************************************************************************
     * method name: findMoreDepsForToken()
     * returns: an annotation
     * function: this is a helper method that when required can be used to return
     * a dependency relation for a given token.
     *
     ****************************************************************************/
    protected static Annotation findMoreDepsForToken(Annotation tokenToGetMoreDeps, String type, boolean exactMatch, AnnotationSet tokenAnnotationSet,AnnotationSet depRelationsSetOfSentence)
    {
        Annotation myAnnotationToReturn= null;
        for(Annotation currentDepRelation: depRelationsSetOfSentence)
        {
            String theIds = currentDepRelation.getFeatures().get("args").toString().trim();
            String theIdsWithoutBrackets = theIds.substring(1,theIds.length()-1);
            String [] args  = theIdsWithoutBrackets.split("\\,");
            Integer theGovId = Integer.parseInt(args[0].trim());
            Integer tokenID = tokenToGetMoreDeps.getId();
            if(tokenID.equals(theGovId))
            {
                String theKind = currentDepRelation.getFeatures().get("kind").toString().trim();
                if(theKind.equals(type))
                {
                   myAnnotationToReturn = tokenAnnotationSet.get(Integer.parseInt(args[1].trim())); 
                   return myAnnotationToReturn;
                    
                }
                // new for collapsed option
                else if(type.equals("prep") && theKind.startsWith("prep"))
                {
                    myAnnotationToReturn = tokenAnnotationSet.get(Integer.parseInt(args[1].trim())); 
                    return myAnnotationToReturn; 
                }
                // new for collapsed option - changed March 18th
                else if(type.equals("conj") && theKind.startsWith("conj"))
                {
                    myAnnotationToReturn = tokenAnnotationSet.get(Integer.parseInt(args[1].trim()));
                    return myAnnotationToReturn;
                }

            }
            
        }
        return myAnnotationToReturn;
    }
    /***************************************************************************
     * method name: findMoreDepsForToken()** NEW JUNE 13th
     * returns: an annotation
     * function: this is a helper method that when required can be used to return
     * a dependency relation for a given token.
     *
     ****************************************************************************/
    protected static ArrayList<Annotation> findMoreDepsForTokenList(Annotation tokenToGetMoreDeps, String type, boolean exactMatch, AnnotationSet tokenAnnotationSet,AnnotationSet depRelationsSetOfSentence)
    {
        ArrayList<Annotation> myAnnotationsToReturn= new ArrayList<Annotation>();
        for(Annotation currentDepRelation: depRelationsSetOfSentence)
        {
            String theIds = currentDepRelation.getFeatures().get("args").toString().trim();
            String theIdsWithoutBrackets = theIds.substring(1,theIds.length()-1);
            String [] args  = theIdsWithoutBrackets.split("\\,");
            Integer theGovId = Integer.parseInt(args[0].trim());
            Integer tokenID = tokenToGetMoreDeps.getId();
            if(tokenID.equals(theGovId))
            {
                String theKind = currentDepRelation.getFeatures().get("kind").toString().trim();
                if(theKind.equals(type))
                {
                   // myAnnotationToReturn = tokenAnnotationSet.get(Integer.parseInt(args[1].trim()));
                    //return myAnnotationToReturn;
                    myAnnotationsToReturn.add(tokenAnnotationSet.get(Integer.parseInt(args[1].trim())));
                    
                }
                // new for collapsed option
                else if(type.equals("prep") && theKind.startsWith("prep"))
                {
                    //myAnnotationToReturn = tokenAnnotationSet.get(Integer.parseInt(args[1].trim()));
                    myAnnotationsToReturn.add(tokenAnnotationSet.get(Integer.parseInt(args[1].trim())));
                    //return myAnnotationToReturn;
                }
                // new for collapsed option - changed March 18th
                else if(type.equals("conj") && theKind.startsWith("conj"))
                {
                    //myAnnotationToReturn = tokenAnnotationSet.get(Integer.parseInt(args[1].trim()));
                    myAnnotationsToReturn.add(tokenAnnotationSet.get(Integer.parseInt(args[1].trim())));
                    //return myAnnotationToReturn;
                }
                
            }
            
        }
        return myAnnotationsToReturn;
    }
    /***************************************************************************
     * method name: findMoreDepsForTokenforADvModSpecial()** looking for a tyep of adverb
     * returns: an annotation
     * function: this is a helper method that when required can be used to return
     * a dependency relation for a given token.
     *
     ****************************************************************************/
    protected static Annotation findMoreDepsForTokenAdvModSpecial(Annotation tokenToGetMoreDeps, String type, boolean exactMatch, AnnotationSet tokenAnnotationSet,AnnotationSet depRelationsSetOfSentence, AnnotationSet syntaxTreeNodeAnnotationSet)
    {
        Annotation myAnnotationToReturn= null;
        for(Annotation currentDepRelation: depRelationsSetOfSentence)
        {
            String theIds = currentDepRelation.getFeatures().get("args").toString().trim();
            String theIdsWithoutBrackets = theIds.substring(1,theIds.length()-1);
            String [] args  = theIdsWithoutBrackets.split("\\,");
            Integer theGovId = Integer.parseInt(args[0].trim());
            Integer tokenID = tokenToGetMoreDeps.getId();
            if(tokenID.equals(theGovId))
            {
                String theKind = currentDepRelation.getFeatures().get("kind").toString().trim();
                if(theKind.equals(type))
                {
                    // check that dep is before the gov
                    // check the POS Tag
                    myAnnotationToReturn = tokenAnnotationSet.get(Integer.parseInt(args[1].trim())); 
                    String posTagOfDep = InvestigateConstituentsFromParseTree.findPosTag (myAnnotationToReturn,syntaxTreeNodeAnnotationSet);
                    if( myAnnotationToReturn.getStartNode().getOffset() < tokenToGetMoreDeps.getStartNode().getOffset() && posTagOfDep.startsWith("W"))
                    {
                    
                        return myAnnotationToReturn;
                    }
                    
                }
            }
            
        }
        return myAnnotationToReturn;
    }				   

    /***************************************************************************
     * method name: findDepOfGovenor()
     * returns: an annotation
     * function: this is a helper method that when required can be used to return
     * a dependency relation for a given token that is a govenor token in one relation 
     * and the dependency token in another dependency relation.
     *
     ****************************************************************************/
    protected static Annotation findDepOfGovenor(Annotation governorTokenOfPrevious, AnnotationSet tokenAnnotationSet,String type,AnnotationSet depRelationsSetOfSentence)
    {
        Annotation myAnnotationToReturn= null;
        // find if gov token is the dep of another dep relation
        for(Annotation currentDepRelation: depRelationsSetOfSentence)
        {
            String theIds = currentDepRelation.getFeatures().get("args").toString().trim();
            String theIdsWithoutBrackets = theIds.substring(1,theIds.length()-1);
            String [] args  = theIdsWithoutBrackets.split("\\,");
            Integer theDepId = Integer.parseInt(args[1].trim());
            Integer tokenID = governorTokenOfPrevious.getId();
            if(tokenID.equals(theDepId))
            {
                String theKind = currentDepRelation.getFeatures().get("kind").toString().trim();
                if(theKind.equals(type))
                {
                    myAnnotationToReturn = tokenAnnotationSet.get(Integer.parseInt(args[0].trim())); 
                    return myAnnotationToReturn;
                }
                // new for collapsed option
                else if(type.equals("prep") && theKind.startsWith("prep"))
                {
                    myAnnotationToReturn = tokenAnnotationSet.get(Integer.parseInt(args[0].trim())); 
                    return myAnnotationToReturn; 
                }
                // new for collapsed option - changed MArch 18th
                else if(type.equals("conj") && theKind.startsWith("conj"))
                {
                    myAnnotationToReturn = tokenAnnotationSet.get(Integer.parseInt(args[0].trim()));
                    return myAnnotationToReturn;
                }
        }
        
    }
    return myAnnotationToReturn;
}    
    /***************************************************************************
     * method name: findDepOfGovenorForAToken
     * returns: a boolean value.
     * function: this is a helper method that when required can be used to find 
     * potentially more dependencies for a given token where the given token is 
     * a govenor token in one relation and the dependency token in another 
     * dependency relation.
     *
     ****************************************************************************/
    protected static boolean findDepOfGovenorForAToken(Annotation governorTokenOfPrevious,String type,AnnotationSet depRelationsSetOfSentence)
    {
        boolean toReturn = false;
        // find if gov token is the dep of another dep relation
        for(Annotation currentDepRelation: depRelationsSetOfSentence)
        {
            String theIds = currentDepRelation.getFeatures().get("args").toString().trim();
            String theIdsWithoutBrackets = theIds.substring(1,theIds.length()-1);
            String [] args  = theIdsWithoutBrackets.split("\\,");
            Integer theDepId = Integer.parseInt(args[1].trim());
            Integer tokenID = governorTokenOfPrevious.getId();
            if(tokenID.equals(theDepId))
            {
                String theKind = currentDepRelation.getFeatures().get("kind").toString().trim();
                if(theKind.equals(type))
                {
                    toReturn = true;
                    return toReturn;
                }
                // new for collapsed option
                else if(type.equals("prep") && theKind.startsWith("prep"))
                {
                    toReturn = true;
                    return toReturn;
 
                }

            }
            
        }
        return toReturn;
    }    

/*********************************************************************************************/
}



