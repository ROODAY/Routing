import java.io.*;

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
	
	/* Class constructor */
	public Node() { }

	/* students to write the following two routines, and maybe some others */
	void rtinit(int nodename, int[] initial_lkcost) { }    

	void rtupdate(Packet rcvdpkt) { }

	/* called when cost from the node to linkid changes from current value to newcost*/
	void linkhandler(int linkid, int newcost) { }  

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
