/* XMLReader.java
 * Authors:
 * Date: January 2012
 * Purpose: This class implements the necessary methods for parsing the wordlist in XML - and generating the correct instances of the correct trigger classes
 *
 *
 *
 */


package clac.creole.extractGenericTriggersV3;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import java.net.*;
import java.io.*;


public class XMLReader 
{
	
 public static Map<String,List<GenericTrigger>> readTriggers(URL triggerFile)
 {
	 Map<String,List<GenericTrigger>> triggers = new HashMap<String,List<GenericTrigger>>();
	 try 
	 {
		 SAXBuilder builder = new SAXBuilder(false);
		 String path = new java.io.File(".").getCanonicalPath();
		 Document doc = builder.build(new FileInputStream(new File(triggerFile.toURI())));
		 String theAnnotationType = doc.getRootElement().getChild("triggerCat").getAttributeValue("annType");
		 List<Element> triggerElements = doc.getRootElement().getChildren("genericTrigger");
         // could be the MPQA
         if (triggerElements.size()==0)
         {
             triggerElements = doc.getRootElement().getChildren("MPQATrigger");
         }
		 for (Element cl: triggerElements) 
		 {
			List<GenericTrigger> triggerObjects = null;
			 if (triggers.containsKey(cl.getAttributeValue("string")))
			 {
					triggerObjects = triggers.get(cl.getAttributeValue("string"));
			 }
				else
				{
					triggerObjects = new ArrayList<GenericTrigger>();
				}
				
				List<Element> evClasses = cl.getChildren("subType");
                if(evClasses.size()>0)
                {
                    for (Element ec: evClasses) 
                    {
                        //what type of trigger are we looking at?
                        if(theAnnotationType.equals("NegElement"))
                        {
                            if(cl.getAttributeValue("Head_Cat")!=null)
                            {
                                triggerObjects.add(new NegTrigger("NegTrigger",cl.getAttributeValue("string"),
                                                                  cl.getAttributeValue("priorPolarity"),
                                                                  cl.getAttributeValue("source"),  
                                                                  cl.getAttributeValue("posType"),
                                                                  cl.getAttributeValue("Head_Cat"),
                                                                  ec.getAttributeValue("name")));
                            }
                            else if(cl.getAttributeValue("Seed_word")!=null)
                            {
                                
                                triggerObjects.add(new NegTrigger("NegTrigger",cl.getAttributeValue("string"),
                                                                  cl.getAttributeValue("priorPolarity"),
                                                                  cl.getAttributeValue("source"),  
                                                                  cl.getAttributeValue("posType"),
                                                                  cl.getAttributeValue("Seed_word"),
                                                                  ec.getAttributeValue("name")));
                                
                            }
                       
                            // see if the current entry is a prefixed neg...
                            // has the prefix attribute?
                            else if((cl.getAttributeValue("prefix")!=null)&& (cl.getAttributeValue("source")!=null))
                            {
                            
                                triggerObjects.add(new NegTrigger("NegTrigger",cl.getAttributeValue("string"),
												   cl.getAttributeValue("priorPolarity"),
												   cl.getAttributeValue("prefix"),
												   cl.getAttributeValue("root"),
                                                   cl.getAttributeValue("source"),  
                                                   cl.getAttributeValue("posType"),
												   ec.getAttributeValue("name")));
                            }
                            // New clause; - good housekeeping ... 
                            else if(cl.getAttributeValue("source")!=null)
                            {
                                
                                triggerObjects.add(new NegTrigger("NegTrigger",cl.getAttributeValue("string"),
                                                                  cl.getAttributeValue("priorPolarity"),
                                                                  cl.getAttributeValue("source"),
                                                                  cl.getAttributeValue("posType"),
                                                                  ec.getAttributeValue("name")));

                            }
                            
                            else
                            {
                                triggerObjects.add(new NegTrigger("NegTrigger",cl.getAttributeValue("string"),
												   cl.getAttributeValue("priorPolarity"),
                                                   cl.getAttributeValue("posType"),
												   ec.getAttributeValue("name")));
                            }
                    
                        }//if NegElement
                        
                        else if(theAnnotationType.equals("ValenceElement"))
                        {
                            triggerObjects.add(new ValenceTrigger("ValenceTrigger",cl.getAttributeValue("string"),
                                                          cl.getAttributeValue("description"),
                                                          cl.getAttributeValue("initDegree"),
                                                          ec.getAttributeValue("name")));
                        }//end if ValenceTrigger
                        // if modalityElement
                        else if(theAnnotationType.equals("ModalityElement"))
                        {
                            if(cl.getAttributeValue("posTag")!=null)
                            {
                                triggerObjects.add(new ModalityTrigger("ModalityTrigger", cl.getAttributeValue("string"),
                                                                   cl.getAttributeValue("priorPolarity"),
                                                                   cl.getAttributeValue("source"),  
                                                                   cl.getAttributeValue("sourceType"),
                                                                   cl.getAttributeValue("posTag"),
                                                                   ec.getAttributeValue("name"))); 
                            }
                            else
                            {
                                triggerObjects.add(new ModalityTrigger("ModalityTrigger", cl.getAttributeValue("string"),
                                                                       cl.getAttributeValue("priorPolarity"),
                                                                       cl.getAttributeValue("source"),  
                                                                       cl.getAttributeValue("sourceType"),"",
                                                                       ec.getAttributeValue("name")));  
                            }
                        }
					
                    // type attribute == the subType...
                    }//for
                }// if we have the subType attribute in the XML 
                // Case for no subtype ... 
                else
                {
                    if(theAnnotationType.equals("HedgeElement"))
                    {
                        // type attribute == the subType...
                        //System.out.println(cl.getAttributeValue("string"));
                        triggerObjects.add(new HedgeTrigger("HedgeTrigger", cl.getAttributeValue("string"),
                                                            cl.getAttributeValue("pos"),
                                                            cl.getAttributeValue("strength"),
                                                            cl.getAttributeValue("source"),
                                                            cl.getAttributeValue("type")));
                    }
                    // for Reported Speech Verbs
                    else if(theAnnotationType.equals("ReportedSpeechVerbElement"))
                    {
                        triggerObjects.add(new GenericTrigger("ReportedSpeechVerbTrigger", cl.getAttributeValue("string"),"null"));

                    }
                    // could be just generic ... ie for Reported Speech Verbs
                    else if(theAnnotationType.equals("GenericTriggerElement"))
                    {
                        triggerObjects.add(new GenericTrigger("GenericTrigger", cl.getAttributeValue("string"),"null"));
                        
                    }
                    

                    
                }// end else (no subType child...)
				Collections.sort(triggerObjects);
				if (triggerObjects.size() > 0)
					triggers.put(cl.getAttributeValue("string"), triggerObjects);
		}// for
		
			 
			 
	 }// try
	 catch(URISyntaxException e) {
		 e.printStackTrace();
	 }
	 catch (FileNotFoundException fnfe) 
	 {
		 System.err.println("Trigger file not found.");
	 } 
	 catch (JDOMException je) 
	 {
		 System.err.println("XML file cannot be parsed.");
	 } 
	 catch (IOException ioe) 
	 {
		 System.err.println("IO error with file .");
	 } 
	 return triggers;
	 
 }
}//class