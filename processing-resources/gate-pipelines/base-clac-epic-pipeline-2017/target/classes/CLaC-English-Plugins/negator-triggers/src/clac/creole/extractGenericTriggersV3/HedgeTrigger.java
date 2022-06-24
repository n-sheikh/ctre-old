/* HedgeTrigger.java
 * Authors:
 * Date: January 2012
 * Purpose: This class implements the necessary methods for a HEDGE trigger
 *
 *
 *
 */


package clac.creole.extractGenericTriggersV3;


public class HedgeTrigger extends GenericTrigger implements Comparable<GenericTrigger>
{
    private String strength; 
    private String source;
    private String posCat;
  
	

    public HedgeTrigger(String triggerType, String trigger,String posCat,String strength, String source, String subType) 
    {
		super(triggerType,trigger,subType);
		this.strength = strength;
		this.source = source;
        this.posCat = posCat;
        
	}
	
	public String getStrength() 
    {
        return strength;
	}
	
	public void setStrength(String strength) 
    {
        this.strength = strength;
    }
	
    public void setSource(String source) 
    {
        this.source = source;
	}
	
    public String getSource() 
    {
		return source;
	}
    
    public void setPosCat(String posCat) 
    {
        this.posCat = posCat;
	}
	
    public String getPosCat() 
    {
		return posCat;
	}
   
    @Override
	public int compareTo(GenericTrigger o) 
    {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
	
}
