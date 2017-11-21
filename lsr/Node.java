import java.io.*;
import java.util.*;
import java.lang.*;

/**
 * Kai Bernardini
 * Contribution
 * -Rinit
 * -Rupdate
 * -Dijkstra
 *
 * Rudhra Raveendran
 * Contribution
 * -Dijkstra
 * -LinkHandler
 * -Rupdate
 *
 * This is the class that students need to implement. The code skeleton is provided.
 * Students need to implement rtinit(), rtupdate() and linkhandler().
 * printdt() is provided to pretty print a table of the current costs for reaching
 * other nodes in the network.
 */





public class Node { 

	public static final int INFINITY = 9999;

	int[] lkcost;				/*The link cost between this node and other nodes*/
	int nodename;           	/*Name of this node*/
	int[][] costs;				/*forwarding table, where index is destination node, [i][0] is cost to destination node and
  	  							  [i][1] is the next hop towards the destination node */
	boolean DEBUG = true;
	int[][] graph;				/*Adjacency metric for the network, where (i,j) is cost to go from node i to j */
	
	/* Class constructor */
	public Node() { }
	int seqNum = 0;



	int computeMinDistance(int V, int output[][], Boolean finalized[])
	{
		// Initialize min value
		int min = INFINITY;
		int min_index=-1;

		for (int v = 0; v < V; v++) {
			if (finalized[v] == false && output[v][0] <= min) {
				min = output[v][0];
				// updated
				min_index = v;
			}
		}
		return min_index;
	}
	// Pseudocode source https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm
	 int[][] dijkstra(int graph[][], int src){
		// verticies
		int V= graph[0].length;

		// ouput is a 4x2 array
		// output[i][0] is the running min distance from source to i
		// output[i][1] is the node before destination i in the min cost path
		int[][] output = new int[4][2];

		// finalized[i] is true if shortest path from source --> i is finalized
		Boolean finalized[] = new Boolean[V];

		// Assign to every node a tentative distance value:
		// set it to zero for our initial node and to infinity for all other nodes.
		for(int i=0; i<V; i++){
			output[i][0] = INFINITY; // initialized to infinity
			output[1][1] = -1 ; // there is no previous node
			finalized[i] = false;
		}
		// set values for current node
		//finalized[src] = true;
		output[src][0] = 0; // there is no cost to stay still.
		output[src][1] = -1; // there is no previous node

		// Find the shortest path for all nodes in the graph other than the initial
		for(int c = 0; c < V-1; c++){

			int u = computeMinDistance(V, output, finalized);
			finalized[u] = true;


			for (int v = 0; v < V; v++){

				// Update dist[v] only if is not in sptSet, there is an
				// edge from u to v, and total weight of path from src to
				// v through u is smaller than current value of dist[v]
				if (!finalized[v] && graph[u][v]!=INFINITY &&
						output[u][0]+ graph[u][v] <= output[v][0]){
					// new distance is updated
					output[v][0] = output[u][0] + graph[u][v];
					//update previous node
					output[v][1] = u;
				}
			}
		}

		if(DEBUG){
			System.out.println("Output_"+ src + " " + Arrays.deepToString(output));
			//printdt();
		}
	return output;
	}


	/* students to write the following two routines, and maybe some others */
	void rtinit(int nodename, int[] initial_lkcost) {
		if(DEBUG){
			System.out.println(nodename + ": " + Arrays.toString(initial_lkcost));
		}
		this.nodename =  nodename;
		this.lkcost = initial_lkcost;
		this.costs = new int[4][2];
		int cur_node;
		int distance;
		//Build cost array.
		/**
		 * cost[i][0] is the minimum cost of getting to node i from node this.nodename
		 * cost[i][1] is the next hop on the path form this.nodename --> i
		 */

		for(int i=0 ;i<4; i++ ){
			distance = lkcost[i];
			costs[i][0] = distance;
			if(distance != INFINITY){
				costs[i][1] = i;
			}
			else{ // there is no initial path to node i
				costs[i][1] = -1;
			}
		}
	/*if(DEBUG){

			System.out.println(nodename + " costs:" +Arrays.toString(costs[0]));
		System.out.println(nodename + " next hop:" +Arrays.toString(costs[1]));
	}
	*/
	// initilaize empty graph
	this.graph =  new int[4][4];
	for(int i=0; i<4; i++){
		for(int j=0; j<4; j++){
			graph[i][j] = INFINITY;
		}
	}
	// Update graph with known distances \ hops
	// graph is symmetric, update with lkcosts
	for(int i=0; i<4; i++){
		graph[this.nodename][i] = lkcost[i];
		graph[i][this.nodename] = lkcost[i];
	}

	//broadcast changes to network
	for(int i=0; i<4; i++){
		// We don't broadcast to ourself or nodes we cannot reach/non neighbors
		if(i == this.nodename || lkcost[i] == INFINITY){
			continue;
		}
		//System.out.println("Node " + this.nodename + "broadcasted to " + i );
		Packet packet = new Packet(this.nodename, i, this.nodename, lkcost, seqNum );
		NetworkSimulator.tolayer2(packet);
	}

	//seqNum ++;
	}

	void rtupdate(Packet rcvdpkt) {
		// A link state packet has been recieved
		// We use this packet to update our view of the global graph
		// the graph is then used to update the mindistance to other nodes
		int[][] old_costs = this.costs.clone();
		// update the graph matrix
		for(int i=0; i<4; i++){
			// This updates the graph
			// Check to make sure distance is not infinity (if it is, it is already initialized to infty)
			if(rcvdpkt.mincost[i] != INFINITY) {
				graph[rcvdpkt.sourceid][i] = rcvdpkt.mincost[i];
				graph[i][rcvdpkt.sourceid] = rcvdpkt.mincost[i];
			}
		}

		// Compute Dijkstras on Graph
		int[][] output = dijkstra(graph, this.nodename);
		for(int i=0; i< costs.length; i++){
			this.costs[i][0] =  output[i][0];
			int nextHop = i;
			while (output[nextHop][1] != this.nodename && output[nextHop][1] !=-1){
				nextHop = output[nextHop][1];
			}
			this.costs[i][1] = nextHop;
		}

		if( !Arrays.deepEquals(costs, old_costs)){
			// update lkcosts
			for(int j=0; j< lkcost.length; j++){
				lkcost[j] = costs[j][0];
			}

			for(int i=0; i<4; i++){
				// We don't broadcast to ourself or nodes we cannot reach/non neighbors
				if(i == this.nodename || lkcost[i] == INFINITY ){
					continue;
				}
				//System.out.println("Node " + this.nodename + "broadcasted to " + i );
				Packet packet = new Packet(this.nodename, i, this.nodename, lkcost, seqNum );
				NetworkSimulator.tolayer2(packet);
			}

		}
	if(DEBUG){printdt();}
	}

	/* called when cost from the node to linkid changes from current value to newcost*/
	void linkhandler(int linkid, int newcost) {
		System.out.println("*********************************************************");
		System.out.println("Change made to "+ this.nodename + ": " + lkcost[linkid] + " --> "+ newcost);
		System.out.println("*********************************************************");

		this.costs[linkid][0] = this.lkcost[linkid] = newcost;
		this.costs[linkid][1] = (this.costs[linkid][0] != INFINITY) ? linkid : -1;

		this.graph[linkid][this.nodename] = this.graph[this.nodename][linkid] = this.lkcost[linkid];

		for (int i = 0; i < this.lkcost.length; i++) {
			if (i != this.nodename && this.costs[i][0] != INFINITY) {
				Packet packet = new Packet(this.nodename, i, this.nodename, this.lkcost, this.seqNum);
				NetworkSimulator.tolayer2(packet);
			}
		}


	}

	/* Prints the current costs to reaching other nodes in the network */
	void printdt() {

		System.out.printf("                    \n");
		System.out.printf("   D%d |   cost  next-hop \n", nodename);
		System.out.printf("  ----|-----------------------\n");
		System.out.printf("     0|  %3d   %3d\n",costs[0][0],costs[0][1]);
		System.out.printf("dest 1|  %3d   %3d\n",costs[1][0],costs[1][1]);
		System.out.printf("     2|  %3d   %3d\n",costs[2][0],costs[2][1]);
		System.out.printf("     3|  %3d   %3d\n",costs[3][0],costs[3][1]);
		System.out.printf("                    \n");
	}

}
