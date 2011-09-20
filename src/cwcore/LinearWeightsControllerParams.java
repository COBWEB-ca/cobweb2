package cwcore;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cobweb.params.CobwebParam;

public class LinearWeightsControllerParams implements CobwebParam {

	private static final long serialVersionUID = 8856565519749448009L;

	public double[][] data = new double[LinearWeightsController.INPUT_COUNT][LinearWeightsController.OUTPUT_COUNT];

	public float prodHuntChance = 0.6f;

	public LinearWeightsControllerParams copy() {
		LinearWeightsControllerParams p = new LinearWeightsControllerParams();
		p.data = new double[data.length][data[0].length];
		for (int i = 0; i < p.data.length; i++)
			for (int j = 0; j < p.data[i].length; j++)
				p.data[i][j] = data[i][j];
		return p;
	}

	public void loadConfig(Node root) throws IllegalArgumentException {
		try {
			NodeList inps = root.getChildNodes();
			int o;
			for (o = 0; o < inps.getLength(); o++) {
				Node inp = inps.item(o);
				int inpid = Integer.parseInt(inp.getAttributes().getNamedItem("id").getNodeValue());
				NodeList outps = inp.getChildNodes();
				for (int i = 0; i < outps.getLength(); i++) {
					Node outp = outps.item(i);
					int outpid = Integer.parseInt(outp.getAttributes().getNamedItem("id").getNodeValue());
					data[inpid][outpid] = Double.parseDouble(outp.getFirstChild().getNodeValue());
				}
			}
			//System.out.println("HERE!");
			//Node prodChance = inps.item(++o);
			//prodHuntChance = Float.parseFloat(prodChance.getFirstChild().getNodeValue());
			//System.out.println(prodHuntChance);
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

			//Element prodChance = document.createElement("prodHuntChance");
			//System.out.println("SAVED");
			//prodChance.setAttribute("chance", ".");
			//prodChance.setTextContent(Float.toString(prodHuntChance));

			root.appendChild(inp);
			//root.appendChild(prodChance);
		}
	}

}
