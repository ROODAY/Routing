import java.io.*;
import java.util.Arrays;

/**
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

	int[][] graph;				/*Adjacency metric for the network, where (i,j) is cost to go from node i to j */

  int curSeqNo = 0;
	
	/* Class constructor */
	public Node() { }

	/* students to write the following two routines, and maybe some others */
	void rtinit(int nodename, int[] initial_lkcost) {
    this.nodename = nodename;
    this.lkcost = initial_lkcost;

    this.costs = new int[4][2];
    for (int i = 0; i < this.costs.length; i++) {
      this.costs[i][0] = this.lkcost[i];
      this.costs[i][1] = (this.costs[i][0] != INFINITY) ? i : -1;
    }

    this.graph = new int[4][4];
    for (int[] row : this.graph) {
      Arrays.fill(row, INFINITY);
    }
    for (int i = 0; i < this.lkcost.length; i++) {
      this.graph[i][this.nodename] = this.graph[this.nodename][i] = this.lkcost[i];
    }

    for (int i = 0; i < this.lkcost.length; i++) {
      if (i != this.nodename && this.costs[i][0] != INFINITY) {
        Packet packet = new Packet(this.nodename, i, this.nodename, this.lkcost, this.curSeqNo);
        NetworkSimulator.tolayer2(packet);
      }
    }
  }    

	void rtupdate(Packet rcvdpkt) {
    for (int i = 0; i < 4; i++) {
      if (rcvdpkt.mincost[i] != INFINITY) {
        this.graph[i][rcvdpkt.sourceid] = this.graph[rcvdpkt.sourceid][i] = rcvdpkt.mincost[i];
      }
    }

    int[][] oldCosts = this.costs.clone();
    int[][] output = dijkstra(this.graph, this.nodename);
    for (int i = 0; i < output.length; i++) {
      this.costs[i][0] = output[i][0];
      int nextHop = i;
      while(output[nextHop][1] != this.nodename && output[nextHop][1] != -1) {
        nextHop = output[nextHop][1];
      }
      this.costs[i][1] = nextHop;
    }

    if (!Arrays.deepEquals(this.costs, oldCosts)) {
      rcvdpkt.nodename = this.nodename;
      for (int i = 0; i < this.lkcost.length; i++) {
        if (i != this.nodename && this.costs[i][0] != INFINITY) {
          NetworkSimulator.tolayer2(rcvdpkt);
        }
      }
    }
  }

	/* called when cost from the node to linkid changes from current value to newcost */
	void linkhandler(int linkid, int newcost) {

  }

  int closestNeighbor(int[][] costs, boolean[] visited) {
    int min = INFINITY;
    int u = -1;

    for (int j = 0; j < graph.length; j++) {
      if (visited[j] == false && costs[j][0] <= min) {
        min = costs[j][0];
        u = j;
      }
    }

    return u;
  }

  /* returns cost table where min cost of src to i is [i][0] via node [i][1] */
  int[][] dijkstra(int graph[][], int src) {
    int[][] output = new int[4][2];
    boolean[] visited = new boolean[4];

    for (int i = 0; i < graph.length; i++) {
        output[i][0] = INFINITY;
        output[i][1] = -1;
        visited[i] = false;
    }

    // outputance of source vertex from itself is always 0
    output[src][0] = 0;
    output[src][1] = -1;

    // Find shortest path for all vertices
    for (int i = 0; i < graph.length - 1; i++) {
      int u = closestNeighbor(output, visited);

      // Mark the picked vertex as processed
      visited[u] = true;
      //output[u][0] = min;

      // Update output value of the adjacent vertices of the
      // picked vertex.
      for (int v = 0; v < graph.length; v++) {
        if (!visited[v] && graph[u][v] != 0 && output[u][0] != INFINITY && output[u][0] + graph[u][v] < output[v][0]) {
          output[v][0] = output[u][0] + graph[u][v];
          output[v][1] = u;
        }
      }
    }

    return output;
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
