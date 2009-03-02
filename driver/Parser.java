package driver;

import ga.GAChartOutput;
import ga.GATracker;
import ga.GeneticCode;
import ga.GeneticCodeException;
import ga.PhenotypeMaster;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Parser {
	public String fileName;

	static Document document;

	public static final int GA_START_OF_FIRST_GENE = 0;

	public static final int GA_START_OF_SECOND_GENE = 8;

	public static final int GA_START_OF_THIRD_GENE = 16;

	public static final int GA_AGENT_1 = 0;

	public static final int GA_AGENT_2 = 1;

	public static final int GA_AGENT_3 = 2;

	public static final int GA_AGENT_4 = 3;

	public int foodType = 0;

	public int agentType = 0;

	public String TickScheduler = "cobweb.TickScheduler";

	public String ControllerName = "cwcore.GeneticController";

	public String ControllerConfig;

	public int[] ComplexEnvironment = new int[1];

	public int[] Width = new int[1];

	public int[] Height = new int[1];

	public boolean[] wrap = new boolean[1];

	public boolean[] keepOldAgents = new boolean[1];

	public boolean[] spawnNewAgents = new boolean[1];

	public boolean[] keepOldArray = new boolean[1];

	public boolean[] dropNewFood = new boolean[1];

	public boolean[] ColorCodedAgents = new boolean[1];

	public boolean[] newColorizer = new boolean[1];

	public boolean[] keepOldWaste = new boolean[1];

	public boolean[] keepOldPackets = new boolean[1];

	public boolean[] food_bias = new boolean[1];

	public boolean[] PrisDilemma = new boolean[1];

	public boolean[] FoodWeb = new boolean[1];

	public int[] numColor = new int[1];

	public int[] colorSelectSize = new int[1];

	public int[] reColorTimeStep = new int[1];

	public int[] colorizerMode = new int[1];

	public int[] RandomSeed = new int[1];

	public int[] randomStones = new int[1];

	public float[] maxFoodChance = new float[1];

	public int[] memory_size = new int[1];

	// constraint: 10 types [0]
	public int[][] agentIndex = new int[1][10];

	public int[][] foodIndex = new int[1][10];

	public float[][] foodRate = new float[1][10];

	public int[][] foodGrow = new int[1][10];

	public int[][] food = new int[1][10];

	public int[][] draught = new int[1][10];

	public int[][] mode = new int[1][10];

	public float[][] foodDeplete = new float[1][10];

	public int[][] depleteTimeSteps = new int[1][10];

	public int[][] agents = new int[1][10];

	public int[][] foodEnergy = new int[1][10];

	public int[][] otherFoodEnergy = new int[1][10];

	public int[][] breedEnergy = new int[1][10];

	public int[][] pregnancyPeriod = new int[1][10];

	public int[][] initEnergy = new int[1][10];

	public int[][] stepEnergy = new int[1][10];

	public int[][] stepRockEnergy = new int[1][10];

	public int[][] stepAgentEnergy = new int[1][10];

	public int[][] turnRightEnergy = new int[1][10];

	public int[][] turnLeftEnergy = new int[1][10];

	public float[][] mutationRate = new float[1][10];

	public int[][] memoryBits = new int[1][10];

	public int[][] commSimMin = new int[1][10];

	public int[][] communicationBits = new int[1][10];

	public float[][] sexualBreedChance = new float[1][10];

	public float[][] asexualBreedChance = new float[1][10];

	public float[][] breedSimMin = new float[1][10];

	public int[][] sexualPregnancyPeriod = new int[1][10];

	public int[][] agents2eat = new int[10][10];

	public int[][] plants2eat = new int[10][10];

	// Aging parameters
	public boolean[][] agingMode = new boolean[1][10];

	public int[][] agingLimit = new int[1][10];

	public float[][] agingRate = new float[1][10];

	// waste parameters
	public boolean[][] wasteMode = new boolean[1][10];

	public int[][] wastePen = new int[1][10];

	public int[][] wasteGain = new int[1][10];

	public int[][] wasteLoss = new int[1][10];

	public float[][] wasteRate = new float[1][10];

	public int[][] wasteInit = new int[1][10];

	public boolean[][] pdTitForTat = new boolean[1][10];

	public int[][] pdCoopProb = new int[1][10];

	public boolean[][] broadcastMode = new boolean[1][10];

	public boolean[][] broadcastEnergyBased = new boolean[1][10];

	public int[][] broadcastFixedRange = new int[1][10];

	public int[][] broadcastEnergyMin = new int[1][10];

	public int[][] broadcastEnergyCost = new int[1][10];

	public int[] reward = new int[1];

	public int[] sucker = new int[1];

	public int[] temptation = new int[1];

	public int[] punishment = new int[1];

	/**
	 * The genetic sequence. Initialize them to a certain sequence for the four
	 * agents.
	 */
	public String[][] genetic_sequence = { { "000111100001111000011110",
		"000111100001111000011110", "000111100001111000011110",
		"000111100001111000011110", "", "", "", "", "", "" } };

	java.util.Hashtable<String, Object> parseData;

	public Parser(String fileName) throws FileNotFoundException {
		this.fileName = fileName;
		loadFile(new FileInputStream(fileName));
	}

	public Parser(InputStream file) {
		loadFile(file);
	}

	private void loadFile(InputStream file) {
		parseData = new java.util.Hashtable<String, Object>();
		org.w3c.dom.Node domNode;

		// read these variables from the xml file

		// DOM initialization
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(file);
		} catch (SAXException sxe) {
			// Error generated during parsing)
			Exception x = sxe;
			if (sxe.getException() != null) {
				x = sxe.getException();
			}
			x.printStackTrace();
		} catch (ParserConfigurationException pce) {
			// Parser with specified options can't be built
			pce.printStackTrace();
		} catch (IOException ioe) {
			// I/O error
			ioe.printStackTrace();
		}

		domNode = document;
		domNode.normalize();

		// now start reading from the XML file
		String nodeValue = "";
		int countf = 0;
		int counta = 0;
		clearArrayHelper(plants2eat);
		clearArrayHelper(agents2eat);

		int foodType = 0;
		int agentType = 0;
		NodeList project = domNode.getChildNodes(); // the project inside the document
		String nodeName2;

		NodeList nodeChildren = project.item(0).getChildNodes();

		for (int j = 0; j < nodeChildren.getLength(); j++) {
			nodeName2 = nodeChildren.item(j).getNodeName();

			// Parsing GA info
			if (nodeName2.equals("ga")) {
				NodeList nodeChildren3 = nodeChildren.item(j).getChildNodes();
				for (int k = 0; k < nodeChildren3.getLength(); k++) {
					nodeName2 = nodeChildren3.item(k).getNodeName();
					NodeList value = nodeChildren3.item(k).getChildNodes();
					if (value.toString().equals("None")) {
						continue;
					} else if (nodeName2.equals("agent1gene1")) {
						nodeValue = getValue(value);
						GUI.genetic_table.setValueAt(nodeValue,
								GUI.GA_AGENT_1_ROW, GUI.GA_GENE_1_COL); // Set the GUI GA table.
						genetic_sequence[0][GA_AGENT_1] = nodeValue
						+ genetic_sequence[0][GA_AGENT_1].substring(GA_START_OF_SECOND_GENE);
					} else if (nodeName2.equals("agent1gene2")) {
						nodeValue = getValue(value);
						GUI.genetic_table.setValueAt(nodeValue,
								GUI.GA_AGENT_1_ROW, GUI.GA_GENE_2_COL); // Set the GUI GA table.
						genetic_sequence[0][GA_AGENT_1] =
							genetic_sequence[0][GA_AGENT_1]
							                    .substring(GA_START_OF_FIRST_GENE,
							                    		GA_START_OF_SECOND_GENE)
							                    		+ nodeValue
							                    		+ genetic_sequence[0][GA_AGENT_1]
							                    		                      .substring(GA_START_OF_THIRD_GENE);
					} else if (nodeName2.equals("agent1gene3")) {
						nodeValue = getValue(value);
						GUI.genetic_table.setValueAt(nodeValue,
								GUI.GA_AGENT_1_ROW, GUI.GA_GENE_3_COL); // Set the GUI GA table.
						genetic_sequence[0][GA_AGENT_1] = genetic_sequence[0][GA_AGENT_1]
						                                                      .substring(GA_START_OF_FIRST_GENE,
						                                                    		  GA_START_OF_THIRD_GENE)
						                                                    		  + nodeValue;
					} else if (nodeName2.equals("agent2gene1")) {
						nodeValue = getValue(value);
						GUI.genetic_table.setValueAt(nodeValue,
								GUI.GA_AGENT_2_ROW, GUI.GA_GENE_1_COL); // Set the GUI GA table.
						genetic_sequence[0][GA_AGENT_2] = nodeValue
						+ genetic_sequence[0][GA_AGENT_2]
						                      .substring(GA_START_OF_SECOND_GENE);
					} else if (nodeName2.equals("agent2gene2")) {
						nodeValue = getValue(value);
						GUI.genetic_table.setValueAt(nodeValue,
								GUI.GA_AGENT_2_ROW, GUI.GA_GENE_2_COL); // Set the GUI GA table.
						genetic_sequence[0][GA_AGENT_2] = genetic_sequence[0][GA_AGENT_2]
						                                                      .substring(GA_START_OF_FIRST_GENE,
						                                                    		  GA_START_OF_SECOND_GENE)
						                                                    		  + nodeValue
						                                                    		  + genetic_sequence[0][GA_AGENT_2]
						                                                    		                        .substring(GA_START_OF_THIRD_GENE);
					} else if (nodeName2.equals("agent2gene3")) {
						nodeValue = getValue(value);
						GUI.genetic_table.setValueAt(nodeValue,
								GUI.GA_AGENT_2_ROW, GUI.GA_GENE_3_COL); // Set the GUI GA table.
						genetic_sequence[0][GA_AGENT_2] = genetic_sequence[0][GA_AGENT_2]
						                                                      .substring(GA_START_OF_FIRST_GENE,
						                                                    		  GA_START_OF_THIRD_GENE)
						                                                    		  + nodeValue;
					} else if (nodeName2.equals("agent3gene1")) {
						nodeValue = getValue(value);
						GUI.genetic_table.setValueAt(nodeValue,
								GUI.GA_AGENT_3_ROW, GUI.GA_GENE_1_COL); // Set the GUI GA table.
						genetic_sequence[0][GA_AGENT_3] = nodeValue
						+ genetic_sequence[0][GA_AGENT_3]
						                      .substring(GA_START_OF_SECOND_GENE);
					} else if (nodeName2.equals("agent3gene2")) {
						nodeValue = getValue(value);
						GUI.genetic_table.setValueAt(nodeValue,
								GUI.GA_AGENT_3_ROW, GUI.GA_GENE_2_COL); // Set the GUI GA table.
						genetic_sequence[0][GA_AGENT_3] =
							genetic_sequence[0][GA_AGENT_3]
							                    .substring(GA_START_OF_FIRST_GENE,
							                    		GA_START_OF_SECOND_GENE)
							                    		+ nodeValue
							                    		+ genetic_sequence[0][GA_AGENT_3]
							                    		                      .substring(GA_START_OF_THIRD_GENE);
					} else if (nodeName2.equals("agent3gene3")) {
						nodeValue = getValue(value);
						GUI.genetic_table.setValueAt(nodeValue,
								GUI.GA_AGENT_3_ROW, GUI.GA_GENE_3_COL); // Set the GUI GA table.
						genetic_sequence[0][GA_AGENT_3] =
							genetic_sequence[0][GA_AGENT_3]
							                    .substring(GA_START_OF_FIRST_GENE,
							                    		GA_START_OF_THIRD_GENE)
							                    		+ nodeValue;
					} else if (nodeName2.equals("agent4gene1")) {
						nodeValue = getValue(value);
						GUI.genetic_table.setValueAt(nodeValue,
								GUI.GA_AGENT_4_ROW, GUI.GA_GENE_1_COL); // Set the GUI GA table.
						genetic_sequence[0][GA_AGENT_4] = nodeValue
						+ genetic_sequence[0][GA_AGENT_4]
						                      .substring(GA_START_OF_SECOND_GENE);
					} else if (nodeName2.equals("agent4gene2")) {
						nodeValue = getValue(value);
						GUI.genetic_table.setValueAt(nodeValue,
								GUI.GA_AGENT_4_ROW, GUI.GA_GENE_2_COL); // Set the GUI GA table.
						genetic_sequence[0][GA_AGENT_4] = genetic_sequence[0][GA_AGENT_4]
						                                                      .substring(GA_START_OF_FIRST_GENE,
						                                                    		  GA_START_OF_SECOND_GENE)
						                                                    		  + nodeValue
						                                                    		  + genetic_sequence[0][GA_AGENT_4]
						                                                    		                        .substring(GA_START_OF_THIRD_GENE);
					} else if (nodeName2.equals("agent4gene3")) {
						nodeValue = getValue(value);
						GUI.genetic_table.setValueAt(nodeValue,
								GUI.GA_AGENT_4_ROW, GUI.GA_GENE_3_COL); // Set the GUI GA table.
						genetic_sequence[0][GA_AGENT_4] =
							genetic_sequence[0][GA_AGENT_4].substring(GA_START_OF_FIRST_GENE, GA_START_OF_THIRD_GENE)
                        		  + nodeValue;
					} else if (nodeName2.equals("linkedphenotype1")) {
						nodeValue = getValue(value);
						GUI.genetic_table.setValueAt(nodeValue,
								GUI.GA_LINKED_PHENOTYPE_ROW, GUI.GA_GENE_1_COL); // Set the GUI GA table.
						try {
							PhenotypeMaster.setLinkedAttributes(nodeValue,
									PhenotypeMaster.RED_PHENOTYPE);
						} catch (GeneticCodeException e) {
							System.err.println(e);
						}
					} else if (nodeName2.equals("linkedphenotype2")) {
						nodeValue = getValue(value);
						GUI.genetic_table.setValueAt(nodeValue,
								GUI.GA_LINKED_PHENOTYPE_ROW, GUI.GA_GENE_2_COL); // Set the GUI GA table.
						try {
							PhenotypeMaster.setLinkedAttributes(nodeValue,
									PhenotypeMaster.GREEN_PHENOTYPE);
						} catch (GeneticCodeException e) {
							System.err.println(e);
						}
					} else if (nodeName2.equals("linkedphenotype3")) {
						nodeValue = getValue(value);
						GUI.genetic_table.setValueAt(nodeValue,
								GUI.GA_LINKED_PHENOTYPE_ROW, GUI.GA_GENE_3_COL); // Set the GUI GA table.
						try {
							PhenotypeMaster.setLinkedAttributes(nodeValue,
									PhenotypeMaster.BLUE_PHENOTYPE);
						} catch (GeneticCodeException e) {
							System.err.println(e);
						}
					} else if (nodeName2.equals("meiosismode")) {
						nodeValue = getValue(value);
						GeneticCode.meiosis_mode = nodeValue;
						if (nodeValue.equals("Colour Averaging")) {
							GUI.meiosis_mode.setSelectedIndex(0);
							GeneticCode.meiosis_mode_index = 0;
						} else if (nodeValue.equals("Random Recombination")) {
							GUI.meiosis_mode.setSelectedIndex(1);
							GeneticCode.meiosis_mode_index = 1;
						} else if (nodeValue.equals("Gene Swapping")) {
							GUI.meiosis_mode.setSelectedIndex(2);
							GeneticCode.meiosis_mode_index = 2;
						}
					} else if (nodeName2.equals("trackgenestatusdistribution")) {
						boolean state = false;
						if (getValue(value).equals("true")) {
							state = true;
							GATracker.setTrackGeneStatusDistribution(state);

						}
						if (GUI.track_gene_status_distribution != null) {
							GUI.track_gene_status_distribution.setSelected(state);
						}
					} else if (nodeName2.equals("trackgenevaluedistribution")) {
						boolean state = false;
						if (getValue(value).equals("true")) {
							state = true;
							GATracker.setTrackGeneValueDistribution(state);
						}
						if (GUI.track_gene_value_distribution != null) {
							GUI.track_gene_value_distribution.setSelected(state);
						}
					} else if (nodeName2.equals("chartupdatefrequency")) {
						nodeValue = getValue(value);
						try {
							int freq = Integer.parseInt(nodeValue);
							if (freq <= 0) {
								// Do nothing
							} else {
								GAChartOutput.update_frequency = freq;
							}
						} catch (NumberFormatException e) {
							// Do nothing
						}
					}
				}
			}

			if (nodeName2.equals("pd")) {
				NodeList nodeChildren3 = nodeChildren.item(j).getChildNodes();
				for (int k = 0; k < nodeChildren3.getLength(); k++) {
					nodeName2 = nodeChildren3.item(k).getNodeName();

					if (nodeName2.equals("reward")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						reward[0] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("sucker")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						sucker[0] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("temptation")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						temptation[0] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("punishment")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						punishment[0] = Integer.parseInt(nodeValue);
					}
				}
			}

			if (nodeName2.equals("scheduler")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				TickScheduler = nodeValue;
			}
			if (nodeName2.equals("ControllerName")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				ControllerName = nodeValue;
			}
			if (nodeName2.equals("ControllerConfig")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				ControllerConfig = nodeValue;
			}
			if (nodeName2.equals("Width")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				Width[0] = Integer.parseInt(nodeValue);
				GAChartOutput.grid_width = Width[0];
			}
			if (nodeName2.equals("Height")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				Height[0] = Integer.parseInt(nodeValue);
				GAChartOutput.grid_height = Height[0];
			}
			if (nodeName2.equals("wrap")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				if (nodeValue.equals("true")) {
					wrap[0] = true;
				} else {
					wrap[0] = false;
				}
			}
			if (nodeName2.equals("PrisDilemma")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				if (nodeValue.equals("true")) {
					PrisDilemma[0] = true;
				} else {
					PrisDilemma[0] = false;
					// System.out.print("in nodename set: Value of PrisDilemma
					// is....");
					// System.out.println("PrisDilemma: "+PrisDilemma[0]);
				}
			}
			if (nodeName2.equals("FoodWeb")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				if (nodeValue.equals("true")) {
					FoodWeb[0] = true;
				} else {
					FoodWeb[0] = false;
				}
			}
			if (nodeName2.equals("randomStones")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				randomStones[0] = Integer.parseInt(nodeValue);
			}
			if (nodeName2.equals("maxFoodChance")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				maxFoodChance[0] = Float.valueOf(nodeValue).floatValue();
			}
			if (nodeName2.equals("keepOldAgents")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				if (nodeValue.equals("true")) {
					keepOldAgents[0] = true;
				} else {
					keepOldAgents[0] = false;
				}
			}
			if (nodeName2.equals("spawnNewAgents")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				if (nodeValue.equals("true")) {
					spawnNewAgents[0] = true;
				} else {
					spawnNewAgents[0] = false;
				}
			}
			if (nodeName2.equals("keepOldArray")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				if (nodeValue.equals("true")) {
					keepOldArray[0] = true;
				} else {
					keepOldArray[0] = false;
				}
			}
			if (nodeName2.equals("dropNewFood")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				if (nodeValue.equals("true")) {
					dropNewFood[0] = true;
				} else {
					dropNewFood[0] = false;
				}
			}
			if (nodeName2.equals("randomSeed")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				RandomSeed[0] = Integer.parseInt(nodeValue);
				GATracker.seed = RandomSeed[0];
			}
			if (nodeName2.equals("newColorizer")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				if (nodeValue.equals("true")) {
					newColorizer[0] = true;
				} else {
					newColorizer[0] = false;
				}
			}
			if (nodeName2.equals("keepOldWaste")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				if (nodeValue.equals("true")) {
					keepOldWaste[0] = true;
				} else {
					keepOldWaste[0] = false;
				}
			}
			if (nodeName2.equals("keepOldPackets")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				if (nodeValue.equals("true")) {
					keepOldPackets[0] = true;
				} else {
					keepOldPackets[0] = false;
				}
			}
			if (nodeName2.equals("numColor")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				numColor[0] = Integer.parseInt(nodeValue);
			}
			if (nodeName2.equals("colorSelectSize")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				colorSelectSize[0] = Integer.parseInt(nodeValue);
			}
			if (nodeName2.equals("reColorTimeStep")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				reColorTimeStep[0] = Integer.parseInt(nodeValue);
			}
			if (nodeName2.equals("colorizerMode")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				colorizerMode[0] = Integer.parseInt(nodeValue);
			}
			if (nodeName2.equals("ColorCodedAgents")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				if (nodeValue.equals("true")) {
					ColorCodedAgents[0] = true;
				} else {
					ColorCodedAgents[0] = false;
				}
			}
			if (nodeName2.equals("memorySize")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				memory_size[0] = Integer.parseInt(nodeValue);
			}
			if (nodeName2.equals("food_bias")) {
				NodeList value = nodeChildren.item(j).getChildNodes();
				nodeValue = getValue(value);
				if (nodeValue.equals("true")) {
					food_bias[0] = true;
				} else {
					food_bias[0] = false;
				}
			}

			if (nodeName2.equals("food")) {

				NodeList nodeChildren2 = nodeChildren.item(j).getChildNodes();

				// read all the node parameters
				for (int k = 0; k < nodeChildren2.getLength(); k++) {
					nodeName2 = nodeChildren2.item(k).getNodeName();
					if (nodeName2.equals("Index")) {
						NodeList value = nodeChildren2.item(k).getChildNodes();
						nodeValue = getValue(value);
						new Integer(Integer.parseInt(nodeValue));
						foodIndex[0][foodType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("Food")) {
						NodeList value = nodeChildren2.item(k).getChildNodes();
						nodeValue = getValue(value);
						food[0][foodType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("FoodRate")) {
						NodeList value = nodeChildren2.item(k).getChildNodes();
						nodeValue = getValue(value);
						foodRate[0][foodType] = Float.valueOf(nodeValue)
						.floatValue();
					}
					if (nodeName2.equals("FoodGrow")) {
						NodeList value = nodeChildren2.item(k).getChildNodes();
						nodeValue = getValue(value);
						foodGrow[0][foodType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("FoodDeplete")) {
						NodeList value = nodeChildren2.item(k).getChildNodes();
						nodeValue = getValue(value);
						foodDeplete[0][foodType] = Float.valueOf(nodeValue)
						.floatValue();
					}
					if (nodeName2.equals("DepleteTimeSteps")) {
						NodeList value = nodeChildren2.item(k).getChildNodes();
						nodeValue = getValue(value);
						depleteTimeSteps[0][foodType] = Integer
						.parseInt(nodeValue);
					}
					if (nodeName2.equals("DraughtPeriod")) {
						NodeList value = nodeChildren2.item(k).getChildNodes();
						nodeValue = getValue(value);
						draught[0][foodType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("FoodMode")) {
						NodeList value = nodeChildren2.item(k).getChildNodes();
						nodeValue = getValue(value);
						mode[0][foodType] = Integer.parseInt(nodeValue);
					}
				}
				foodType++;
			}
			if (nodeName2.equals("agent")) {
				NodeList nodeChildren3 = nodeChildren.item(j).getChildNodes();
				for (int k = 0; k < nodeChildren3.getLength(); k++) {
					nodeName2 = nodeChildren3.item(k).getNodeName();

					if (nodeName2.equals("Index")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						agentIndex[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("Agents")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						agents[0][agentType] = Integer.parseInt(nodeValue);
					}

					if (nodeName2.equals("MutationRate")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						mutationRate[0][agentType] = Float.valueOf(nodeValue).floatValue();
					}
					if (nodeName2.equals("InitEnergy")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						initEnergy[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("FoodEnergy")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						foodEnergy[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("OtherFoodEnergy")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						otherFoodEnergy[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("BreedEnergy")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						breedEnergy[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("pregnancyPeriod")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						pregnancyPeriod[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("StepEnergy")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						stepEnergy[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("StepRockEnergy")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						stepRockEnergy[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("TurnRightEnergy")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						turnRightEnergy[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("TurnLeftEnergy")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						turnLeftEnergy[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("MemoryBits")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						memoryBits[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("commSimMin")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						commSimMin[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("StepAgentEnergy")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						stepAgentEnergy[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("communicationBits")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						communicationBits[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("sexualPregnancyPeriod")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						sexualPregnancyPeriod[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("breedSimMin")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						breedSimMin[0][agentType] = Float.valueOf(nodeValue).floatValue();
					}
					if (nodeName2.equals("sexualBreedChance")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						sexualBreedChance[0][agentType] = Float.valueOf(nodeValue).floatValue();
					}
					if (nodeName2.equals("asexualBreedChance")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						asexualBreedChance[0][agentType] = Float.valueOf(nodeValue).floatValue();
					}
					// AGING

					if (nodeName2.equals("agingMode")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						agingMode[0][agentType] = (nodeValue.equals("true") ? true : false);
					}
					if (nodeName2.equals("agingLimit")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						agingLimit[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("agingRate")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						agingRate[0][agentType] = Float.valueOf(nodeValue).floatValue();
					}
					// END OF AGING
					// WASTE
					if (nodeName2.equals("wasteMode")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						wasteMode[0][agentType] = (nodeValue.equals("true") ? true
								: false);
					}
					if (nodeName2.equals("wastePen")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						wastePen[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("wasteGain")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						wasteGain[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("wasteLoss")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						wasteLoss[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("wasteRate")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						wasteRate[0][agentType] = Float.valueOf(nodeValue).floatValue();
					}
					if (nodeName2.equals("wasteInit")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						wasteInit[0][agentType] = Integer.parseInt(nodeValue);
					}

					// END OF WASTE

					// PD PARAMS
					if (nodeName2.equals("pdTitForTat")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						pdTitForTat[0][agentType] = (nodeValue.equals("true") ? true
								: false);
					}
					if (nodeName2.equals("pdCoopProb")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						pdCoopProb[0][agentType] = Integer.parseInt(nodeValue);
					}

					// END OF PD PARAMS

					// BROADCASTING PARAMS
					if (nodeName2.equals("broadcastMode")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						broadcastMode[0][agentType] = (nodeValue.equals("true") ? true
								: false);
					}
					if (nodeName2.equals("broadcastEnergyBased")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						broadcastEnergyBased[0][agentType] = (nodeValue
								.equals("true") ? true : false);
					}
					if (nodeName2.equals("broadcastFixedRange")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						broadcastFixedRange[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("broadcastEnergyMin")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						broadcastEnergyMin[0][agentType] = Integer.parseInt(nodeValue);
					}
					if (nodeName2.equals("broadcastEnergyCost")) {
						NodeList value = nodeChildren3.item(k).getChildNodes();
						nodeValue = getValue(value);
						broadcastEnergyCost[0][agentType] = Integer.parseInt(nodeValue);
					}
					// END OF BROADCASTING PARAMS

					if (nodeName2.equals("foodweb")) {

						NodeList nodeChildren4 = nodeChildren3.item(k)
						.getChildNodes();
						counta = 0;
						countf = 0;

						for (int p = 0; p < nodeChildren4.getLength(); p++) {
							nodeName2 = nodeChildren4.item(p).getNodeName();
							for (int i = 0; i < nodeChildren4.getLength(); i++) {

								if ((nodeName2).equals("agent" + i)) {
									NodeList value = nodeChildren4.item(p).getChildNodes();
									nodeValue = getValue(value);
									if (nodeValue.equals("true")) {

										agents2eat[agentType][counta++] = (i - 1);

									}
								}
								if (nodeName2.equals("food" + i)) {
									NodeList value = nodeChildren4.item(p).getChildNodes();
									nodeValue = getValue(value);

									if (nodeValue.equals("true")) {
										plants2eat[agentType][countf++] = (i - 1);

									}
								}
							}
						}

					}

				}
				agentType++;
			}
		}// for
		ComplexEnvironment[0] = agentType;
		fillHashTable();
	} // parser

	private void clearArrayHelper(int array[][]) {
		for (int k = 0; k < array.length; k++) {
			for (int i = 0; i < array[k].length; i++) {
				array[k][i] = -1;
			}
		}
	}

	private String getValue(NodeList value) {
		String nodeValue = "";
		int c = 0;
		if (value.item(c) == null) {
			return null;
		}
		while (value.item(c).getNodeType() != Node.TEXT_NODE
				|| (value.item(c).getNodeType() == Node.TEXT_NODE && value.item(c)
						.getNodeValue().trim().length() == 0)) {
			c++;
		}
		nodeValue = value.item(c).getNodeValue().trim();
		return nodeValue;
	}

	public void fillHashTable() {
		parseData.put("TickScheduler", TickScheduler);
		parseData.put("ControllerName", ControllerName);
		parseData.put("ComplexEnvironment", ComplexEnvironment);
		parseData.put("width", Width);
		parseData.put("height", Height);
		parseData.put("wrap", wrap);
		parseData.put("keepoldagents", keepOldAgents);
		parseData.put("spawnnewagents", spawnNewAgents);
		parseData.put("keepoldarray", keepOldArray);
		parseData.put("dropnewfood", dropNewFood);
		parseData.put("colorcodedagents", ColorCodedAgents);
		parseData.put("newcolorizer", newColorizer);
		parseData.put("keepoldwaste", keepOldWaste);
		parseData.put("keepoldpackets", keepOldPackets);
		parseData.put("foodBias", food_bias);
		parseData.put("PrisDilemma", PrisDilemma);
		parseData.put("numcolor", numColor);
		parseData.put("colorselectsize", colorSelectSize);
		parseData.put("recolortimestep", reColorTimeStep);
		parseData.put("colorizermode", colorizerMode);
		parseData.put("randomseed", RandomSeed);
		parseData.put("randomstones", randomStones);
		parseData.put("maxfoodchance", maxFoodChance);
		parseData.put("memorysize", memory_size);

		parseData.put("food", food);
		parseData.put("foodrate", foodRate);
		parseData.put("foodgrow", foodGrow);
		parseData.put("fooddeplete", foodDeplete);
		parseData.put("depletetimesteps", depleteTimeSteps);
		parseData.put("DraughtPeriod", draught);
		parseData.put("foodmode", mode);

		parseData.put("agents", agents);
		parseData.put("foodenergy", foodEnergy);
		parseData.put("otherfoodenergy", otherFoodEnergy);
		parseData.put("breedenergy", breedEnergy);
		parseData.put("pregnancyperiod", pregnancyPeriod);
		parseData.put("initenergy", initEnergy);
		parseData.put("stepenergy", stepEnergy);
		parseData.put("steprockenergy", stepRockEnergy);
		parseData.put("stepagentenergy", stepAgentEnergy);
		parseData.put("turnrightenergy", turnRightEnergy);
		parseData.put("turnleftenergy", turnLeftEnergy);
		parseData.put("mutationrate", mutationRate);
		parseData.put("memorybits", memoryBits);
		parseData.put("commsimmin", commSimMin);
		parseData.put("communicationbits", communicationBits);
		parseData.put("sexualbreedchance", sexualBreedChance);
		parseData.put("asexualbreedchance", asexualBreedChance);
		parseData.put("sexualpregnancyperiod", sexualPregnancyPeriod);
		parseData.put("agingMode", agingMode);
		parseData.put("agingLimit", agingLimit);
		parseData.put("agingRate", agingRate);
		parseData.put("wasteMode", wasteMode);
		parseData.put("wastePen", wastePen);
		parseData.put("wasteGain", wasteGain);
		parseData.put("wasteLoss", wasteLoss);
		parseData.put("wasteRate", wasteRate);
		parseData.put("wasteInit", wasteInit);
		parseData.put("pdTitForTat", pdTitForTat);
		parseData.put("pdCoopProb", pdCoopProb);
		parseData.put("broadcastMode", broadcastMode);
		parseData.put("broadcastEnergyBased", broadcastEnergyBased);
		parseData.put("broadcastFixedRange", broadcastFixedRange);
		parseData.put("broadcastEnergyMin", broadcastEnergyMin);
		parseData.put("broadcastEnergyCost", broadcastEnergyCost);
		parseData.put("breedsimmin", breedSimMin);
		parseData.put("agents2eat", agents2eat);
		parseData.put("plants2eat", plants2eat);

		parseData.put("reward", reward);
		parseData.put("sucker", sucker);
		parseData.put("temptation", temptation);
		parseData.put("punishment", punishment);

	}

	public Object getfromHashTable(String param) {
		return parseData.get(param);
	}

	public String getFilename() {
		return fileName;
	}

} // Parser
