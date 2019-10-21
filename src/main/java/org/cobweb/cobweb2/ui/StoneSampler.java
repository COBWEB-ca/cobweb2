package org.cobweb.cobweb2.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.core.Environment;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.io.CobwebXmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author fchoong
 *
 *          For saving and loading stones
 *
 */
public class StoneSampler
{
	private static String root_name = "StoneMap";
	protected static boolean fg_has_stonemap = false;
	protected static Collection<Stone> stonemap = null;
	protected static String stonemap_filename = "";

	public static boolean hasStoneMap()
	{
		return fg_has_stonemap;
	}

	public static String getStoneMapFilename()
	{
		return stonemap_filename;
	}

	public static Collection<Stone> getStoneMap()
	{
		return stonemap;
	}

	public static void saveStoneMap(Simulation sim, String filePath)
	{
		int map_width = sim.theEnvironment.topology.width;
		int map_height = sim.theEnvironment.topology.height;
		Map<Location, Stone> stoneTable = new Hashtable<Location, Stone>();

		for(int x = 0; x < map_width; x++)
		{
			for(int y = 0; y < map_width; y++)
			{
				Location loc = new Location(x, y);

				if(sim.theEnvironment.hasStone(loc))
				{
					Stone s = new Stone(loc);
					stoneTable.put(loc, s);
				}
			}
		}

		Collection<Stone> stones = stoneTable.values();

		try
		{
			OutputStream fileOut = new FileOutputStream(filePath);
			serializeStones(stones, fileOut);
			stonemap = stones;
			fg_has_stonemap = true;
			stonemap_filename = filePath;
		}
		catch(IOException ioe)
		{
			throw new RuntimeException("Could not save stone map.", ioe);
		}
	}

	public static void insertStoneMap(Simulation sim, String filePath, boolean fg_replace)
	{
		if(fg_replace)
		{
			sim.theEnvironment.clearStones();
		}

		try
		{
			InputStream fileIn = new FileInputStream(filePath);

			stonemap = loadStones(fileIn);

			loadStoneMap(sim.theEnvironment);

			stonemap_filename = filePath;

		}
		catch(IOException ioe)
		{
			throw new RuntimeException("Could not load stone map.", ioe);
		}
	}

	public static void loadStoneMap(Environment env)
	{
		if(!fg_has_stonemap)
			fg_has_stonemap = true;

		for(Stone stone: stonemap) // we load it anyway
		{
			env.addStone(stone.getPostion());
		}
	}

	public static void unloadStoneMap(Environment env)
	{
		if(fg_has_stonemap)
		{
			fg_has_stonemap = false;

			for(Stone stone: stonemap)
			{
				env.removeStone(stone.getPostion());
			}

			stonemap = null;
		}
	} // public static void unloadStoneMap(Simulation sim)

	protected static void serializeStones(Collection<Stone> stones, OutputStream fileOut)
	{
		Element root = CobwebXmlHelper.createDocument(root_name, "stonemap");
		Document d = root.getOwnerDocument();
		root.setAttribute("stonemap-version", "2019-08-03");

		for(Stone stone: stones)
		{
			Node node = saveStone(stone, d);
			root.appendChild(node);
		}

		CobwebXmlHelper.writeDocument(fileOut, d);
	}

	protected static Collection<Stone> loadStones(InputStream fileIn)
	{
		Element root = CobwebXmlHelper.openDocument(fileIn);
		if(!root.getNodeName().equals(root_name))
			throw new UserInputException("File does not appear to be a stone map");

		NodeList stoneList = root.getChildNodes();
		int stone_count = stoneList.getLength();
		Collection<Stone> result = new ArrayList<>(stone_count);

		for(int i = 0; i < stone_count; i++)
		{
			Element stoneNode = (Element) stoneList.item(i);
			Stone stone = loadStone(stoneNode);
			result.add(stone);
		}

		return result;
	}

	protected static Node saveStone(Stone simpleStone, Document d)
	{
		Stone s = simpleStone;

		Element stone = d.createElement("Stone");
		// No type

		// No params for simple stone

		Element locationElement = d.createElement("location");
		Location location = s.getPostion();
		locationElement.setAttribute("x", location.x + "");
		locationElement.setAttribute("y", location.y + "");

		stone.appendChild(locationElement);

		return stone;
	}

	protected static Stone loadStone(Element element)
	{
		Element location = (Element)element.getElementsByTagName("location").item(0);
		Location loc = new Location(Integer.parseInt(location.getAttribute("x")), Integer.parseInt(location.getAttribute("y")));
		return new Stone(loc);
	}
} // public class StoneSampler

class Stone {

	Location position = null;

	public Stone(Location l)
	{
		this.position = l;
	}

	Location getPostion()
	{
		return position;
	}
}