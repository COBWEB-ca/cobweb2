/**
 *
 */
package cwcore.complexParams;

import cobweb.TickScheduler;
import cobweb.params.AbstractReflectionParams;
import cobweb.params.CobwebParam;
import cobweb.params.ConfDisplayName;
import cobweb.params.ConfDynamicInstance;
import cobweb.params.ConfXMLTag;
import cwcore.GeneticController;
import cwcore.GeneticControllerParams;

public class ComplexEnvironmentParams extends AbstractReflectionParams implements CobwebParam {
	/**
	 *
	 */
	private static final long serialVersionUID = -3308627358945982393L;

	@ConfDisplayName("Agent types")
	@ConfXMLTag("AgentTypeCount")
	public int agentTypeCount;

	@ConfDisplayName("Food types")
	@ConfXMLTag("FoodTypeCount")
	public int foodTypeCount;


	@ConfDisplayName("Scheduler type")
	@ConfXMLTag("scheduler")
	public String schedulerName;

	@ConfDisplayName("Controller type")
	@ConfXMLTag("ControllerName")
	public String controllerName;

	@ConfXMLTag("ControllerConfig")
	@ConfDynamicInstance(ControllerLoader.class)
	public CobwebParam controllerParams;


	@ConfDisplayName("Width")
	@ConfXMLTag("Width")
	public int width;

	@ConfDisplayName("Height")
	@ConfXMLTag("Height")
	public int height;

	@ConfDisplayName("Wrap edges")
	@ConfXMLTag("wrap")
	public boolean wrapMap;


	@ConfDisplayName("Random seed")
	@ConfXMLTag("randomSeed")
	public long randomSeed;


	@ConfDisplayName("Keep old array")
	@ConfXMLTag("keepOldArray")
	public boolean keepOldArray;

	@ConfDisplayName("Drop new food")
	@ConfXMLTag("dropNewFood")
	public boolean dropNewFood;

	@ConfDisplayName("Keep old waste")
	@ConfXMLTag("randomSeed")
	public boolean keepOldWaste;

	@ConfDisplayName("Keep old agents")
	@ConfXMLTag("keepOldAgents")
	public boolean keepOldAgents;

	@ConfDisplayName("Spawn new agents")
	@ConfXMLTag("spawnNewAgents")
	public boolean spawnNewAgents;

	@ConfDisplayName("Keep old packets")
	@ConfXMLTag("keepOldPackets")
	public boolean keepOldPackets;

	@ConfDisplayName("Random stones")
	@ConfXMLTag("randomStones")
	public int initialStones;


	@ConfDisplayName("Like food probability")
	@ConfXMLTag("likeFoodProb")
	public float likeFoodProb;


	@ConfDisplayName("Prisoner's dillema")
	@ConfXMLTag("PrisDilemma")
	public boolean prisDilemma;

	@ConfDisplayName("PD oponent memory")
	@ConfXMLTag("memorySize")
	public int pdMemorySize;

	@ConfXMLTag("pd")
	public PDParams pdParams;

	public ComplexEnvironmentParams() {
		agentTypeCount = 4;
		foodTypeCount = 4;

		schedulerName = TickScheduler.class.getName();
		controllerName = GeneticController.class.getName();
		controllerParams = new GeneticControllerParams();

		width = 80;
		height = 80;
		wrapMap = true;

		randomSeed = 42;

		keepOldArray = false;
		dropNewFood = true;
		keepOldWaste = false;
		keepOldAgents = false;
		spawnNewAgents = true;
		keepOldPackets = false;
		initialStones = 10;

		likeFoodProb = 0.0f;

		prisDilemma = false;
		pdMemorySize = 10;
		pdParams = new PDParams();
	}




}