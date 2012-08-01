# Bilateral Projects

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

Enjoy!