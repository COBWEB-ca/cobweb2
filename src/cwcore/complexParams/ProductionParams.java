package cwcore.complexParams;

import cobweb.params.AbstractReflectionParams;
import cobweb.params.ConfDisplayName;
import cobweb.params.ConfXMLTag;


public class ProductionParams extends AbstractReflectionParams {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7452975610085145539L;

	public ProductionParams() {
	}

	/**
	 * Agent type index.
	 */
	@ConfXMLTag("Index")
	public int type = -1;

	@ConfXMLTag("InitProdChance")
	@ConfDisplayName("Initial production percentage roll")	
	public float initProdChance = 0.8f;

	@ConfXMLTag("LowDemThresh")
	@ConfDisplayName("The maximum prodVal to consider \"low\"")
	public float lowDemandThreshold = 5f;

	@ConfXMLTag("LowDemProdChance")
	@ConfDisplayName("Percentage roll to produce in low demand area")
	public float lowDemandProdChance = 0.001f;

	@ConfXMLTag("SweetDemThresh")
	@ConfDisplayName("The maximum prodVal to consider \"sweet\"")
	public float sweetDemandThreshold = 6f;

	@ConfXMLTag("SweetDemStartChance")
	@ConfDisplayName("Minimum roll percentage in sweet zone")
	public float sweetDemandStartChance = 0.001f;

	@ConfXMLTag("HiDemCutoff")
	@ConfDisplayName("Maximal prodVal to produce on")
	public float highDemandCutoff = 20f;

	@ConfXMLTag("HiDemProdChance")
	@ConfDisplayName("Maximum roll percentage in high zone")
	public float highDemandProdChance = 0.001f;

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}	
}
