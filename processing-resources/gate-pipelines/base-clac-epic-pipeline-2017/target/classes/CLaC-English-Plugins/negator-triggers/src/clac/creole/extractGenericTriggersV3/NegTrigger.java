/* NegTrigger.java
 * Authors:
 * Date: January 2012
 * Purpose: This class implements the necessary methods for a NEGATION trigger
 *
 *
 *
 */

package clac.creole.extractGenericTriggersV3;


public class NegTrigger extends GenericTrigger implements Comparable<GenericTrigger>
{
    private String priorPolarityVal; 
    private String prefix;
    private String localScope;
    private String source;
    private String additionalCat;
	
    public NegTrigger(String triggerType, String trigger,String priorPolarityVal,String posType, String triggerSubType) 
    {
		super(triggerType,trigger,triggerSubType,posType);
		this.priorPolarityVal = priorPolarityVal;
        this.prefix = null;
        this.localScope =null;
        this.source =null;
        this.additionalCat=null;
	}
	// for negTriggers with cat - ** new for Extended List
	public NegTrigger(String triggerType, String trigger, String priorPolarityVal, String source,String posType,String additionalCat,String triggerSubType) 
	{
			super(triggerType,trigger,triggerSubType,posType);
			this.priorPolarityVal = priorPolarityVal;
			this.prefix = null;
			this.localScope = null;
            this.additionalCat = additionalCat;
            this.source =source;
        
    }
	
    // for negTriggers with prefix	*** AND source. 
	public NegTrigger(String triggerType, String trigger, String priorPolarityVal,String prefix, String localScope, String source, String posType,String triggerSubType) 
	{
        super(triggerType,trigger,triggerSubType,posType);
        this.priorPolarityVal = priorPolarityVal;
        this.prefix = prefix;
        this.localScope = localScope;
        this.source =source;
        this.additionalCat=null;
    }
    // for negTriggers with source
	public NegTrigger(String triggerType, String trigger, String priorPolarityVal,String source,String posType, String triggerSubType) 
	{
        super(triggerType,trigger,triggerSubType,posType);
        this.priorPolarityVal = priorPolarityVal;
        this.source = source;
        this.localScope = null;
        this.prefix =null;
        this.additionalCat=null;
    }
    
    public String getAdditionalCat() 
    {
        return additionalCat;
	}
    
    public void setAdditionalCat(String additionalCat) 
    {
        this.additionalCat= additionalCat;
        
	}
    public String getSource() 
    {
        return source;
	}
    
    public void setSource(String source) 
    {
        this.source= source;
        
	}
    
	public String getPriorPolarityVal() 
    {
        return priorPolarityVal;
	}
	
	public void setPriorPolarityVal(String priorPolarityVal) 
    {
        this.priorPolarityVal = priorPolarityVal;
    }
	
	
	public void setPrefix(String prefix) 
    {
        this.prefix = prefix;
	}
	public String getPrefix() 
    {
		return prefix;
	}
	public void setLocalScope(String localScope) 
    {
        this.localScope = localScope;
	}
	public String getLocalScope() 
    {
		return localScope;
	}

	@Override
	public int compareTo(GenericTrigger o) 
    {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
	
}
