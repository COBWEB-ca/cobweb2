package cobweb;

import java.util.List;


public class ArrayEnvironment {

	private int width;

	private int height;

	private List<CellObject>[][] locations;

	public ArrayEnvironment(int w, int h) {
		width = w;
		height = h;
		locations = new List[w][h];
	}

	public ArrayEnvironment(int w, int h, ArrayEnvironment a) {
		width = w;
		height = h;
		locations = cobweb.ArrayUtilities.resizeArray(a.getLocations(), w, h);
	}

	public int getAxisCount() {
		return 2;
	}

	public List<CellObject>[][] getLocations() {
		return locations;
	}

	public List<CellObject> getLocationBits(cobweb.Location l) {
		return locations[l.v[0]][l.v[1]];
	}

	public int getSize(int axis) {
		switch (axis) {
			case cobweb.Environment.AXIS_X:
				return width;
			case cobweb.Environment.AXIS_Y:
				return height;
			default:
				return 0;
		}
	}

}
