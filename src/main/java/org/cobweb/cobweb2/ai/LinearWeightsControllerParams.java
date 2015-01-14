package org.cobweb.cobweb2.ai;

import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.core.params.AgentFoodCountable;
import org.cobweb.cobweb2.core.params.SimulationParams;
import org.cobweb.io.ParameterCustomSerializable;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LinearWeightsControllerParams implements ControllerParams, ParameterCustomSerializable {

	private static final long serialVersionUID = 8856565519749448009L;

	public double[][] data;

	private SimulationParams simParam;

	public LinearWeightsControllerParams(SimulationParams simParam) {
		this.simParam = simParam;
		data = new double[INPUT_COUNT + this.simParam.getPluginParameters().size()][OUTPUT_COUNT];
	}

	public LinearWeightsControllerParams copy() {
		LinearWeightsControllerParams p = new LinearWeightsControllerParams(simParam);
		p.data = new double[data.length][data[0].length];
		for (int i = 0; i < p.data.length; i++)
			for (int j = 0; j < p.data[i].length; j++)
				p.data[i][j] = data[i][j];
		return p;
	}

	@Override
	public void loadConfig(Node root) throws IllegalArgumentException {
		try {
			NodeList inps = root.getChildNodes();
			for (int o = 0; o < inps.getLength(); o++) {
				Node inp = inps.item(o);
				int inpid = Integer.parseInt(inp.getAttributes().getNamedItem("id").getNodeValue());
				NodeList outps = inp.getChildNodes();
				for (int i = 0; i < outps.getLength(); i++) {
					Node outp = outps.item(i);
					int outpid = Integer.parseInt(outp.getAttributes().getNamedItem("id").getNodeValue());
					data[inpid][outpid] = Double.parseDouble(outp.getFirstChild().getNodeValue());
				}
			}
		} catch (DOMException ex) {
			loadOldConf(root);
		}
	}

	private void loadOldConf(Node node) {
		String conf = node.getFirstChild().getNodeValue();
		int i = 0;
		int j = 0;
		for (String e : conf.split(",")) {
			data[i][j] = Double.parseDouble(e);
			if (++j >= data[i].length) {
				j = 0;
				if (++i >= data.length)
					return;
			}
		}
	}

	@Override
	public void saveConfig(Node root, Document document) {
		for (int o = 0; o < data.length; o++) {
			Element inp = document.createElement("inp");
			inp.setAttribute("id", Integer.toString(o));
			for (int i = 0; i < data[o].length; i++) {
				Element outp = document.createElement("outp");
				outp.setAttribute("id", Integer.toString(i));
				outp.setTextContent(Double.toString(data[o][i]));
				inp.appendChild(outp);
			}
			root.appendChild(inp);
		}
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		// Doesn't do anything so far
	}

	@Override
	public Controller createController(SimulationInternals sim, int memoryBits, int communicationBits, int type) {
		LinearWeightsController controller = new LinearWeightsController(sim, this, memoryBits, communicationBits, type);
		return controller;
	}

	public final int INPUT_COUNT = 10;
	public final int OUTPUT_COUNT = 6;

	public final String[] inputNames = { "Constant", "Energy", "Distance to agent", "Distance to food",
			"Distance to obstacle", "Direction", "Memory", "Communication", "Age", "Random" };

	public final String[] outputNames = { "Memory", "Communication", "Left", "Right", "Forward", "Asexual Breed" };

	private final double UPDATE_RATE = 0.001;

	private transient double[] runningOutputMean = new double[OUTPUT_COUNT];

	public void updateStats(int output, double value) {
		runningOutputMean[output] *= (1 - UPDATE_RATE);
		runningOutputMean[output] += UPDATE_RATE * value;
	}

	public double[] getRunningOutputMean() {
		return runningOutputMean;
	}

}
