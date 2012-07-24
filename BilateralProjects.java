/* 
README:
@author Wesley Kim
@date 7-21-2012

Hi Spotify :).

Solution to Spotify developer challenge "Bilateral Projects" (http://www.spotify.com/se/jobs/tech/bilateral-projects/).

My thought process on the problem:
1) The problem essentally broke down to some sort of minimum set cover problem.
In particular, it's the minimum vertex cover problem,
where we're looking for the minimum set of vertices such that each edge of the graph is connected to at least one of those vertices.
The brute-force approach would be much too slow.
2) So, I first thought of using a Greedy approach towards solving the problem.
However, this would not guarantee the smallest number of people invited (it's approximated within a logarithmic factor).
3) Then, I noticed that there are only two locations, thus the input could be represented as a bipartite graph.
What's nice about bipartite graphs is that it is possible to find the solution to vertex cover
in O(e(sqrt(v)) where e is the number of edges and v is the number of vertices.
This is achieved with the Hopcroft-Karp algorithm (http://en.wikipedia.org/wiki/Hopcroft%E2%80%93Karp_algorithm).
This is a much more reasonable run time than brute force, and provides the correct solution unlike the greedy approach!
4) With regards to what kind of graph to use, I noticed that there are a
maximum of 10000 edges (10000 teams) and 2000 vertices (2000 employees).
Furthermore, the algorithm requires BFS and DFS.
"Find neighbors" is quicker with adjacency lists, so I decided to implement graphs with adjacency lists.
*/

import java.util.*;
import java.io.*;

/**
 *@Purpose Main class
 */
public class BilateralProjects {
	
	//max number of vertices
	public static final int MAX_VERTICES = 2000;
	
	//ID of friend that we want to invite, if possible
	public static final int FRIEND_ID = 1009;
	
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		int teams = input.nextInt();
		
		AdjacencyListGraph graph = new AdjacencyListGraph(MAX_VERTICES);
		for(int i = 0; i < teams; i++) {
			Vertex a = new Vertex(input.nextInt());
			Vertex b = new Vertex(input.nextInt());
			graph.addVertex(a);
			graph.addVertex(b);
			graph.addEdge(a,b);
		}
			
	}
}

/**
 * @Purpose Vertex for graph. Each vertex has a name and its associated getters and setters.
 */
class Vertex {
	private int id;

	public Vertex(int id) {
		this.id = id;
	}
	
	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}
	
	/**
	 * if id of both the vertices is the same then the vertices are equal
	 */
	@Override
	public boolean equals(Object obj){
		if(obj instanceof Vertex)
		{
			Vertex v = (Vertex)obj;
			if(this.id == v.getID()){ 
				return true;
			}
			return false;
		}
		else
			return false;
	}
}

/**
 * @Purpose VertexList used to implement AdjacencyList
 */
class VertexList {
	private Vertex v;
	private VertexList next;
	
	public VertexList(Vertex v) {
		this.v = v;
	}
	
	public Vertex getValue() {
		return v;
	}
	
	public void setValue(Vertex set) {
		v = set;
	}
	
	public VertexList getNext() {
		return next;
	
	}
	
	public void setNext(VertexList set) {
		next = set;
	} 
}

/**
 *@Purpose: Implement graph using adjacency lists
 */
class AdjacencyListGraph {
	
	private Vertex[] vertices;
	private VertexList[] adjacencyList;
	private int count;
	private int size;
	
	/**
	 * Purpose: This is the constructor for AdjacencyListGraph.
	 * @param maxVertices maximum number of vertices, used here as initial capacity for the map.
	 */
	public AdjacencyListGraph(int maxVertices) {
		
		//check for negative max vertices
		if(maxVertices < 0) {
			throw new IllegalArgumentException(
					"Max vertices must be non-negative");
		}
		
		vertices = new Vertex[maxVertices];
		adjacencyList = new VertexList[maxVertices];
		count = 0;
		size = maxVertices;
	}

	/**
	 * Purpose: Add vertex function
	 * @param v Vertex added to graph
	 */
	public boolean addVertex(Vertex v) {
		//check if vertex is null
		if(v == null) {
			return false;
		}
		
		//graph is at max size
		if(count == size) {
			return false;
		}
		
		for(int i = 0; i < count; i++) {
			//can't insert vertex already in graph
			if(vertices[i].equals(v)) {
				return false;
			}
		}
			
		vertices[count] = v;
		count++;
		return true;
	}

	/**
	 * Purpose: Add edge function, without duplicate edges.
	 * Precondition: v1 and v2 should already be part of the graph.
	 * Postcondition: adjacent(v1, v2)
	 * @param v1 First vertex included in edge
	 * @param v2 Second vertex included in edge
	 */
	public boolean addEdge(Vertex v1, Vertex v2) {
		VertexList list1 = null;
		VertexList list2 = null;
		VertexList next1 = new VertexList(v2);
		VertexList next2 = new VertexList(v1);
		int index1 = -1;
		int index2 = -1;
		
		//retrieve adjacency list for first vertex
		for(int i = 0; i < count; i++) {
			if(vertices[i].equals(v1)) {
				list1 = adjacencyList[i];
				index1 = i;
			}
			
			if(vertices[i].equals(v2)) {
				list2 = adjacencyList[i];
				index2 = i;
			}
		}
		
		//violates the precondition
		if(index1 == -1 || index2 == -1) {
			return false;
		}
		
		//place vertices into adjacency lists
		if(list1 == null && index1 != -1) {
			adjacencyList[index1] = next1;
		}
		
		boolean newSelfLoop = false;
		
		while(list1 != null) {
			if(list1.getValue().equals(v2)) {
				break;
			}
			
			if(list1.getNext() == null) {
				list1.setNext(next1);
				if(v1.equals(v2)) {
					newSelfLoop = true;
				}
			}
			
			list1 = list1.getNext();
		}
		
		//case of self-loop
		if(newSelfLoop) {
			return true;
		}
		
		if(list2 == null && index2 != -1) {
			adjacencyList[index2] = next2;
			return true;
		}
		
		while(list2 != null) {
			if(list2.getValue().equals(v1)) {
				break;
			}
			
			if(list2.getNext() == null) {
				list2.setNext(next2);
				return true;
			}
			
			list2 = list2.getNext();
		}
		
		return false;
	}

	/**
	 * Purpose: check if v1 and v2 are adjacent
	 * @param v1 First vertex
	 * @param v2 Second vertex
	 */
	public boolean adjacent(Vertex v1, Vertex v2) {
		VertexList neighbors = null;
		
		//retrieve adjacency list for v1
		for(int i = 0; i < count; i++) {
			if(vertices[i].equals(v1)) {
				neighbors = adjacencyList[i];
				break;
			}
		}
		
		//go through adjacency list, looking for v2
		while(neighbors != null) {
			if(neighbors.getValue().equals(v2)) {
				return true;
			}
			
			neighbors = neighbors.getNext();
		}
		
		return false;
	}

	/**
	 * Purpose: return neighbors of vertex v
	 * @param v Vertex who's neighbors will be returned
	 */
	public Vertex[] getNeighbors(Vertex v) {
		if(v == null) {
			return null;
		}
		
		VertexList list;
		Vertex[] neighbors;
		int neighborCount = 0;
		
		//find adjacency list for v
		for(int i = 0; i < count; i++) {
			if(vertices[i].equals(v)) {
				list = adjacencyList[i];
				
				//retrieve number of neighbors
				while(list != null) {
					list = list.getNext();
					neighborCount++;
				}
				
				list = adjacencyList[i];
				
				neighbors = new Vertex[neighborCount];
				
				//place neighbors into array
				for(int j = 0; j < neighborCount; j++) {
					neighbors[j] = list.getValue();
					list = list.getNext();
				}
					
				return neighbors;
			}
		}
		
		return null;
	}

	/**
	 * Purpose: return array of vertices in the graph
	 */
	public Vertex[] getVertices() {
		//create new array as to avoid null elements
		Vertex[] ret = new Vertex[count];
		
		//place elements into ret array
		for(int i = 0; i < count; i++) {
			ret[i] = vertices[i];
		}
		
		return ret;
	}
}
