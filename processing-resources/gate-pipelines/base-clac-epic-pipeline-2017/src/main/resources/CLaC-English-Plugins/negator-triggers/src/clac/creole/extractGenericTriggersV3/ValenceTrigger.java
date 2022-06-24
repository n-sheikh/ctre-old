/* ValenceTrigger.java
 * Authors:
 * Date: January 2012
 * Purpose: This class implements the necessary methods for a VALENCE trigger
 *
 *
 *
 */

package clac.creole.extractGenericTriggersV3;


public class ValenceTrigger extends GenericTrigger implements Comparable<GenericTrigger>
{
    private String description; 
    private String initDegree;
	

    public ValenceTrigger(String triggerType, String trigger,String description,String initDegree,String triggerSubType) 
    {
		super(triggerType,trigger,triggerSubType);
		this.description = description;
		this.initDegree = initDegree;
	}
	
	public String getInitDegree() 
    {
        return initDegree;
	}
	
	public void setInitDegree(String initDegree) 
    {
        this.initDegree = initDegree;
    }
	
	
	public void setDescription(String description) 
    {
        this.description = description;
	}
	public String getDescription() 
    {
		return description;
	}
    @Override
	public int compareTo(GenericTrigger o) 
    {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
	
}
