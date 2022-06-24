/* ModalityTrigger.java
 * Authors:
 * Date: April 2012
 * Purpose: This class implements the necessary methods for a MODALITY trigger
 *
 *
 *
 */

package clac.creole.extractGenericTriggersV3;


public class ModalityTrigger extends GenericTrigger implements Comparable<GenericTrigger>
{
    private String priorPolarityVal; 
    private String source;
    private String sourceType;
    private String posType;
	
    public ModalityTrigger(String triggerType, String trigger,String priorPolarityVal, String source, String sourceType,  String posType,String triggerSubType) 
    {
		super(triggerType,trigger,triggerSubType," ");
		this.priorPolarityVal = priorPolarityVal;
        this.sourceType = sourceType;
        this.source =source;
        this.posType = posType;
        
	}
    
    
    public String getPosType() 
    {
        return posType;
        
	}
    public void setPosType(String posType) 
    {
        this.posType= posType;
        
	}
    public String getSourceType() 
    {
        return sourceType;
        
	}
    public void setSourceType(String sourceType) 
    {
        this.sourceType= sourceType;
        
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
	
	
	

	@Override
	public int compareTo(GenericTrigger o) 
    {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
	
}
