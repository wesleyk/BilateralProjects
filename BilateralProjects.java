/* 
README:
@author Wesley Kim
@date 7-21-2012
@Github https://github.com/wesleyk
@website http://www.contrib.andrew.cmu.edu/~wesleyk/profile/

Hi Spotify :).

Solution to Spotify developer challenge "Bilateral Projects" (http://www.spotify.com/se/jobs/tech/bilateral-projects/).

My thought process on the problem:
1) The problem essentially broke down to some sort of minimum set cover problem.
In particular, it's the minimum vertex cover problem,
where we're looking for the minimum set of vertices such that each edge of the graph is connected to at least one of those vertices.
The brute-force approach would be much too slow.
2) So, I first thought of using a Greedy approach towards solving the problem.
However, this would not guarantee the smallest number of people invited (it's approximated within a logarithmic factor).
3) Then, I noticed that there are only two locations, thus the input could be represented as a bipartite graph.
What's nice about bipartite graphs is that it is possible to find the solution to vertex cover
in O(e(sqrt(v)) where e is the number of edges and v is the number of vertices.
This is achieved with the Hopcroft-Karp algorithm (http://en.wikipedia.org/wiki/Hopcroft%E2%80%93Karp_algorithm).
While the Hopcroft-Karp algorithm solves maximum matching, by Koenig's theorem the solution also applies to minimum vertex cover.
This is a much more reasonable run time than brute force, and provides the correct solution unlike the greedy approach!
4) With regards to what kind of graph to use, I noticed that there are a
maximum of 10000 edges (10000 teams) and 2000 vertices (2000 employees).
Furthermore, the algorithm requires BFS and DFS.
"Find neighbors" is quicker with adjacency lists, so I decided to implement graphs with adjacency lists.
5) Because the vertices are represented by IDs (ints), we can use arrays versus hash tables in the matching,
with the employee ID working as the index into the array.
 */

import java.util.*;
import java.io.*;

/**
 * @Purpose Main class, takes input and runs Hopcroft-Karp algorithm.
 */
public class BilateralProjects {

	// max number of vertices
	public static final int MAX_VERTICES = 2000;

	// max ID + 1 (for indexing arrays)
	public static final int MAX_IDS = 3000;

	// ID of friend that we want to invite, if possible
	public static final int FRIEND_ID = 1009;

	// Essentially infinite value used to initiate distances with for BFS/DFS
	public static final int MAX_DISTANCE = Integer.MAX_VALUE;

	// used to represent "nil" Vertex, since there is no ID 0
	public static final int NIL_VERTEX = 0;

	// adjacency list graph
	public static AdjacencyListGraph graph;

	// vertices (employees) from Stockholm/London
	public static HashSet<Integer> employeesStockholm;
	public static HashSet<Integer> employeesLondon;

	// used to maintain which edges have been chosen
	public static int[] matchStockholm;
	public static int[] matchLondon;

	// used to map vertices to their distance from a given free vertex
	// for BFS and DFS
	public static int[] distances;

	// used as part of Konig's Theorem in Part 2, representing set T
	public static HashSet<Integer> T;
	
	public static void main(String[] args) {
		//PART 0: receive input and instantiate variables
		
		Scanner input = new Scanner(System.in);
		int teams = input.nextInt();
		
		// instantiate graph with up to MAX_VERTICES vertices
		graph = new AdjacencyListGraph(MAX_VERTICES);

		// instantiate hash sets that contain relevant employees
		// in other words, the vertices of the two sides of the bipartite graph
		employeesStockholm = new HashSet<Integer>();
		employeesLondon = new HashSet<Integer>();

		// instantiate arrays
		matchStockholm = new int[MAX_IDS];
		matchLondon = new int[MAX_IDS];
		distances = new int[MAX_IDS];

		//instantiate HashSet for T
		T = new HashSet<Integer>();
		
		// retrieve team pairings from input
		for (int i = 0; i < teams; i++) {
			// add vertices/edge to the adjacency list
			// a vertex is an employee, while an edge is a team
			int a = input.nextInt();
			int b = input.nextInt();
			graph.addVertex(a);
			graph.addVertex(b);
			graph.addEdge(a, b);

			// add vertices to respective vertex sets
			employeesStockholm.add(a);
			employeesLondon.add(b);
		}

		// set matchings as null initially
		for (int v : graph.getVertices()) {
			matchStockholm[v] = NIL_VERTEX;
			matchLondon[v] = NIL_VERTEX;
			distances[v] = MAX_DISTANCE;
		}

		// PART 1: run Hopcroft-Karp algorithm
		while (BFS()) {
			for (int v : employeesStockholm) {
				if (matchStockholm[v] == NIL_VERTEX) {
					DFS(v);
				}
			}
		}

		// PART 2: using Konig's theorem, figure out which vertices make up the minimum vertex cover
		
		// we first find a set of vertices T as such:
		// 1: start with each unmatched employee from Stockholm
		// 2: for each unmatched employee, follow the path of unmatched edge/matched edge,
		//		adding employees along the way
		for(int v : employeesStockholm) {
			// if v is unmatched, add it to T
			if(matchStockholm[v] == NIL_VERTEX) {
				T.add(v);

				Stack<Integer> stack = new Stack<Integer>();
				
				// go left (Stockholm) to right (London) along unmatched edges,
				// and then right to left along matched edges, adding appropriate employees along the way
				// completed iteratively to avoid stack overflow issues
				stack.push(v);
				
				while(!stack.isEmpty()) {
					int next = stack.pop();
					int[] neighbors = graph.getNeighbors(next);
					for(int u : neighbors) {
						//to avoid infinite loops, continue if the element has already been explored
						if(T.contains(u)) {
							continue;
						}
						
						int match = matchLondon[u];

						// only look at London employees that are not matched with v
						// in order to avoid infinite loops
						if(match == next) {
							continue;
						}
						
						// add the London employee
						T.add(u);
						
						if(match != NIL_VERTEX) {				
							// add the Stockholm employee
							T.add(match);
							
							// recurse with the Stockholm employee
							stack.push(match);	
						}
					}
				}
				
			}
		}
		
		// the solution is made up of the union of the following two sets:
		// 1) The difference of employeesStockholm and T
		// 2) The intersection of employeesLondon and T
		HashSet<Integer> solution = new HashSet<Integer>();
		employeesStockholm.removeAll(T); //difference
		employeesLondon.retainAll(T); // intersection
		solution.addAll(employeesStockholm);
		solution.addAll(employeesLondon);
		
		//print solution
		System.out.println(solution.size());
		for(int v : solution) {
			System.out.println(v);
		}
	}

	//breadth first search
	public static boolean BFS() {
		Queue<Integer> queue = new LinkedList<Integer>();

		// start by adding free vertices to queue
		for (int v : employeesStockholm) {
			if (matchStockholm[v] == NIL_VERTEX) {
				distances[v] = 0;
				queue.add(v);
			}
			else {
				distances[v] = MAX_DISTANCE;
			}
		}

		distances[NIL_VERTEX] = MAX_DISTANCE;

		// then, run bfs with free vertices are starting points
		while (!queue.isEmpty()) {
			int v = queue.remove();
			int[] neighbors = graph.getNeighbors(v);
			if (neighbors != null) {
				for (int u : neighbors) {
					int u2 = matchLondon[u];
					// if the vertex hasn't been checked yet, enqueue and add the distance
					if (distances[u2] == MAX_DISTANCE) {
						distances[u2] = (distances[v] + 1);
						queue.add(u2);
					}
				}
			}
		}
		return (distances[NIL_VERTEX] != MAX_DISTANCE);
	}

	//depth first search
	public static boolean DFS(int v) {
		if (v != NIL_VERTEX) {
			int[] neighbors = graph.getNeighbors(v);
			// first time through the neighbors, look for FRIEND_ID
			for(int u : neighbors) {
				if(u == FRIEND_ID) {
					if (distances[matchLondon[u]] == (distances[v] + 1)) {
						if (DFS(matchLondon[u])) {
							matchLondon[u] = v;
							matchStockholm[v] = u;
							return true;
						}
					}
				}
			}
			// run through again, this time examining non-FRIEND_ID ids
			for (int u : neighbors) {
				// skip if FRIEND_ID
				if(u == FRIEND_ID) continue;
				
				if (distances[matchLondon[u]] == (distances[v] + 1)) {
					if (DFS(matchLondon[u])) {
						matchLondon[u] = v;
						matchStockholm[v] = u;
						return true;
					}
				}
			}
			distances[v] = MAX_DISTANCE;
			return false;
		}
		return true;
	}
}

/**
 * @Purpose List used to implement AdjacencyList
 */
class List {
	private int v;
	private List next;

	public List(int v) {
		this.v = v;
	}

	public int getValue() {
		return v;
	}

	public void setValue(int set) {
		v = set;
	}

	public List getNext() {
		return next;

	}

	public void setNext(List next) {
		this.next = next;
	}
}

/**
 * @Purpose: Implement graph using adjacency lists
 */
class AdjacencyListGraph {

	private int[] vertices;
	private List[] adjacencyList;
	private int count;
	private int size;

	/**
	 * Purpose: This is the constructor for AdjacencyListGraph.
	 * 
	 * @param maxVertices
	 *            maximum number of vertices, used here as initial capacity for
	 *            the map.
	 */
	public AdjacencyListGraph(int maxVertices) {

		// check for negative max vertices
		if (maxVertices < 0) {
			throw new IllegalArgumentException(
					"Max vertices must be non-negative");
		}

		vertices = new int[maxVertices];
		adjacencyList = new List[maxVertices];
		count = 0;
		size = maxVertices;
	}

	/**
	 * Purpose: Add vertex function
	 * 
	 * @param v
	 *            Vertex added to graph
	 */
	public boolean addVertex(int v) {
		// graph is at max size
		if (count == size) {
			return false;
		}

		for (int i = 0; i < count; i++) {
			// can't insert vertex already in graph
			if (vertices[i] == v) {
				return false;
			}
		}

		vertices[count] = v;
		count++;
		return true;
	}

	/**
	 * Purpose: Add edge function, without duplicate edges. Precondition: v1 and
	 * v2 should already be part of the graph. Postcondition: adjacent(v1, v2)
	 * 
	 * @param v1
	 *            First vertex included in edge
	 * @param v2
	 *            Second vertex included in edge
	 */
	public boolean addEdge(int v1, int v2) {
		List list1 = null;
		List list2 = null;
		List next1 = new List(v2);
		List next2 = new List(v1);
		int index1 = -1;
		int index2 = -1;

		// retrieve adjacency list for first vertex
		for (int i = 0; i < count; i++) {
			if (vertices[i] == v1) {
				list1 = adjacencyList[i];
				index1 = i;
			}

			if (vertices[i] == v2) {
				list2 = adjacencyList[i];
				index2 = i;
			}
		}

		// violates the precondition
		if (index1 == -1 || index2 == -1) {
			return false;
		}

		// place vertices into adjacency lists
		if (list1 == null && index1 != -1) {
			adjacencyList[index1] = next1;
		}

		boolean newSelfLoop = false;

		while (list1 != null) {
			if (list1.getValue() == v2) {
				break;
			}

			if (list1.getNext() == null) {
				list1.setNext(next1);
				if (v1 == v2) {
					newSelfLoop = true;
				}
			}

			list1 = list1.getNext();
		}

		// case of self-loop
		if (newSelfLoop) {
			return true;
		}

		if (list2 == null && index2 != -1) {
			adjacencyList[index2] = next2;
			return true;
		}

		while (list2 != null) {
			if (list2.getValue() == v1) {
				break;
			}

			if (list2.getNext() == null) {
				list2.setNext(next2);
				return true;
			}

			list2 = list2.getNext();
		}

		return false;
	}

	/**
	 * Purpose: check if v1 and v2 are adjacent
	 * 
	 * @param v1
	 *            First vertex
	 * @param v2
	 *            Second vertex
	 */
	public boolean adjacent(int v1, int v2) {
		List neighbors = null;

		// retrieve adjacency list for v1
		for (int i = 0; i < count; i++) {
			if (vertices[i] == v1) {
				neighbors = adjacencyList[i];
				break;
			}
		}

		// go through adjacency list, looking for v2
		while (neighbors != null) {
			if (neighbors.getValue() == v2) {
				return true;
			}

			neighbors = neighbors.getNext();
		}

		return false;
	}

	/**
	 * Purpose: return neighbors of vertex v
	 * 
	 * @param v
	 *            Vertex who's neighbors will be returned
	 */
	public int[] getNeighbors(int v) {
		List list;
		int[] neighbors;
		int neighborCount = 0;

		// find adjacency list for v
		for (int i = 0; i < count; i++) {
			if (vertices[i] == v) {
				list = adjacencyList[i];

				// retrieve number of neighbors
				while (list != null) {
					list = list.getNext();
					neighborCount++;
				}

				list = adjacencyList[i];

				neighbors = new int[neighborCount];

				// place neighbors into array
				for (int j = 0; j < neighborCount; j++) {
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
	public int[] getVertices() {
		// create new array as to avoid null elements
		int[] ret = new int[count];

		// place elements into ret array
		for (int i = 0; i < count; i++) {
			ret[i] = vertices[i];
		}

		return ret;
	}
}
