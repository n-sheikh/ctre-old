/* GenericTrigger.java
 * Authors:
 * Date: January 2012
 * Purpose: This class implements the necessary methods for a GENERIC trigger
 *
 *
 *
 */

package clac.creole.extractGenericTriggersV3;


public class GenericTrigger implements Comparable<GenericTrigger>
{
    private String trigger;
    private String triggerType;
    private String triggerSubType;
    private Class className;
    private String posType;
	
	// constructor 1
	public GenericTrigger(String triggerType,String trigger,String triggerSubType, String posType) 
    {
		super();
        this.triggerSubType = triggerSubType;
		this.trigger = trigger;
		this.triggerType = triggerType;
        this.posType =posType;
	}
    
    // constructor 2
	public GenericTrigger(String triggerType,String trigger,String triggerSubType) 
    {
		super();
        this.triggerSubType = triggerSubType;
		this.trigger = trigger;
		this.triggerType = triggerType;
        this.posType = null;
	}
	
    public String getPosType() 
    {
        return posType;
	}
    
    public void setPosType(String posType) 
    {
        this.posType= posType;
        
	}
    
	public String getTrigger() 
    {
		return trigger;
	}
	public void setTrigger(String trigger) 
    {
		this.trigger = trigger;
	}
	
	public String getTriggerType() 
    {
		return triggerType;
	}
		
	public void setTriggerType(String triggerType) 
    {
		this.triggerType = triggerType;
	}
    
    public String getTriggerSubType() 
    {
		return triggerSubType;
	}
    
	public void setTriggerSubType(String triggerSubType) 
    {
		this.triggerSubType = triggerSubType;
	}
		
    public String toString()
    {
		return trigger + "|" + "|"+ triggerType + "\n";
	}

	public Class getClassName() 
    {
		return className;
	}

	public void setClassName(Class className) 
    {
		this.className = className;
	}

	@Override
	public int compareTo(GenericTrigger o) 
    {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
	
}
