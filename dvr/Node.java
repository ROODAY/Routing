import java.io.*;
import java.util.*;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Author Kai Bernardini
 * Contribution
 * Rupdate
 * Poison Reverse
 * LinkHandler
 *
 * Rudhra Raveendran
 * Rinit
 * Rupdate
 *
 * This is the class that students need to implement. The code skeleton is provided.
 * Students need to implement rtinit(), rtupdate() and linkhandler().
 * printdt() is provided to pretty print a table of the current costs for reaching
 * other nodes in the network.
 */
public class Node {

    public static final int INFINITY = 9999;

    int[] lkcost;		/*The link cost between this node and other nodes*/
    int[][] costs;  		/*Define distance table*/
    int nodename;               /*Name of this node*/

    // Maintain list of neighbords
    Map<Integer, Integer> neighborMap = new HashMap<Integer, Integer>();
    // lkcost acts as the routing table, but it is important to know the initial ingle hop route
    // this is readable from costs but is more convinient to just maintain
    int[] initial_lkcost;

    // Debug
    boolean DEBUG = true;
    /* Class constructor */
    public Node() { }



    /*
    Helper functions
    */

    // Finds the smallest cost in a cost row
    // for a given destination via i, finds the i
    // that minimizes the cost
    // Returns an index
    int minCost(int[] costRow) {
        int runningMin = INFINITY;
        for (int i = 0; i < costRow.length; i++) {
            if (costRow[i] < runningMin) {
                runningMin = costRow[i];
            }
        }
        return runningMin;
    }

    // Given a row, finds the next hop with smallest cost
    // Similar to Min cost but returns the index instead of the min cost
    int nextHop(int[] costRow) {
        int runningMin = INFINITY;
        // return -1 means the destination is unreachable
        // IE, no path currently
        int index = -1;
        for (int i = 0; i < costRow.length; i++) {
            if (costRow[i] < runningMin) {
                runningMin = costRow[i];
                index = i;
            }
        }
        return index;
    }

    // Checks whether this node is neighbours with node id
    boolean isNeighbour(int id) {
        return this.neighborMap.containsKey(id);
    }

    // Main Functions for Routing
    /* students to write the following two routines, and maybe some others */
    void rtinit(int nodename, int[] initial_lkcost) {
        if(DEBUG){
            System.out.println( this.nodename + ": " + Arrays.toString(initial_lkcost));
        }
        this.nodename = nodename;
        this.costs = new int[4][4];

        this.lkcost = initial_lkcost;
    	for(int[] row: this.costs){
    	    Arrays.fill(row, INFINITY );
        }

        for(int i =0; i< initial_lkcost.length; i++){
    	    // reaching node i via node i has cost costs[i]
            // All i such that node is a neighbord (999 otherwise)
           costs[i][i] = lkcost[i];
           if( lkcost[i] != INFINITY){
               // maintain list of neighbors in path
               // May or may not be useful
               this.neighborMap.put(i, new Integer(1));
           }
        }
        // Broadcast position to network
        for(int i=0; i< initial_lkcost.length; i++){
            // we do not broadcast to ourself or nodes we cannot reach!
            if(i == this.nodename || lkcost[i] == INFINITY){
                continue;
            }
            Packet packet = new Packet(this.nodename, i,  lkcost );
            if(DEBUG){
               System.out.println("Initial broadcast for "+ this.nodename);
                printdt();
            }
            NetworkSimulator.tolayer2(packet);
        }
	}

    void rtupdate(Packet rcvdpkt) {
        // make a copy to check if there has been any update
        int[][] oldCosts = this.costs.clone();
        // payload form packet
        int sender = rcvdpkt.sourceid;
        int receiver = rcvdpkt.destid;
        int[] mincost = rcvdpkt.mincost;
        boolean flag = false;
        // update the costs table
        for (int dest = 0; dest < mincost.length; dest++) {
                // Bellman Ford equation
                // cost for the hop
                // reaching destination via sender

                int pot_new = this.lkcost[sender] + mincost[dest];
                if(pot_new != costs[dest][sender] && pot_new<= INFINITY  ){
                    flag = true;
                }
                // will need to change for split horizon/ poisson
                if(pot_new >= INFINITY){
                    // reach the destination via this sender
                    this.costs[dest][sender] = INFINITY;
                }
                else{
                    this.costs[dest][sender] =  pot_new;
                }

        }

        // calculate new mincost and update routing table accordingly
        int[] newMinCost = new int[mincost.length];
        for (int dest = 0; dest < lkcost.length; dest++) {
            newMinCost[dest] = minCost(this.costs[dest]);
        }

        if(flag){
           // sending packets
            for (int i = 0; i < this.lkcost.length; i++) {

                if (isNeighbour(i) && i != this.nodename ) {
                    int[] poison_reverse_mincost = newMinCost.clone();
                    for(int dest = 0; dest < this.lkcost.length; dest ++){
                        if(nextHop(costs[dest]) == i){
                            poison_reverse_mincost[dest] = INFINITY;
                        }

                    }



                    Packet distVector = new Packet(this.nodename, i, poison_reverse_mincost);
                    NetworkSimulator.tolayer2(distVector);
                    // Notice that we only send our paths to neighbors. We will not advertise paths longer than 1


                }
            }

        }
        else {
            // No changes
            System.out.println();
            printdt();
                   }
    }


    /* called when cost from the node to linkid changes from current value to newcost*/
    void linkhandler(int linkid, int newcost) {
        System.out.println("*********************************************************");
        System.out.println("Change made to "+ this.nodename + ": " + lkcost[linkid] + " --> "+ newcost);
        System.out.println("*********************************************************");
        for(int dest = 0; dest < costs[0].length; dest++){
            for(int via =0; via < costs[0].length; via ++){
                // check to make sure it is reachable
                if(via == linkid && costs[dest][via] != INFINITY && lkcost[via] != INFINITY){
                    // change in link cost
                    this.costs[dest][via] -= lkcost[via];
                    // update with new cost
                    this.costs[dest][via] += newcost;
                }
            }
        }
        // update
        lkcost[linkid]= newcost;
        //System.out.println("New cost updated")
        // Broadcast position to network
        for(int i=0; i< lkcost.length; i++){
            // we do not broadcast to ourself or nodes we cannot reach!
            if(i == this.nodename || lkcost[i] == INFINITY){
                continue;
            }

            int [] updatedMinCost = new int[4];
            for(int j =0; j< lkcost.length; j++){
                updatedMinCost[j] = minCost(costs[j]);
            }
            int[] poison_reverse_mincost = updatedMinCost.clone();
            for(int dest = 0; dest < this.lkcost.length; dest ++){
                //if(dest == i){continue;}
                //if(dest == this.nodename || dest == sender){continue;}
                if(nextHop(costs[dest]) == i){
                    poison_reverse_mincost[dest] = INFINITY;
                }

            }

            Packet distVector = new Packet(this.nodename, i, poison_reverse_mincost);
            NetworkSimulator.tolayer2(distVector);

        }
            //

            if(DEBUG){
                System.out.println("Updated Re-broadcast for "+ this.nodename);
                printdt();
                //System.out.println("Initial broadcast sent from "+ packet.sourceid + " to " + i);
            }
           // NetworkSimulator.tolayer2(packet);
        }




    /* Prints the current costs to reaching other nodes in the network */
    void printdt() {
        switch(nodename) {

	case 0:
	    System.out.printf("                via     \n");
	    System.out.printf("   D0 |    1     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     1|  %3d   %3d \n",costs[1][1], costs[1][2]);
	    System.out.printf("dest 2|  %3d   %3d \n",costs[2][1], costs[2][2]);
	    System.out.printf("     3|  %3d   %3d \n",costs[3][1], costs[3][2]);
	    break;
	case 1:
	    System.out.printf("                via     \n");
	    System.out.printf("   D1 |    0     2    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n",costs[0][0], costs[0][2],costs[0][3]);
	    System.out.printf("dest 2|  %3d   %3d   %3d\n",costs[2][0], costs[2][2],costs[2][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n",costs[3][0], costs[3][2],costs[3][3]);
	    break;
	case 2:
	    System.out.printf("                via     \n");
	    System.out.printf("   D2 |    0     1    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n",costs[0][0], costs[0][1],costs[0][3]);
	    System.out.printf("dest 1|  %3d   %3d   %3d\n",costs[1][0], costs[1][1],costs[1][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n",costs[3][0], costs[3][1],costs[3][3]);
	    break;
	case 3:
	    System.out.printf("                via     \n");
	    System.out.printf("   D3 |    1     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d\n",costs[0][1],costs[0][2]);
	    System.out.printf("dest 1|  %3d   %3d\n",costs[1][1],costs[1][2]);
	    System.out.printf("     2|  %3d   %3d\n",costs[2][1],costs[2][2]);
	    break;
        }
    }

}
