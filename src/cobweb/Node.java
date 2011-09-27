package cobweb;


public class Node {

	private Environment.Location coords;

	public int dist;

	public boolean visited;

	public Node(int dist, Environment.Location pos) {
		this.dist = dist;
		this.coords = pos;
		this.visited = false;
	}

	public Environment.Location getPosition() {
		return this.coords;
	}
}
