/*
 * Main.java
 *
 * Created on April 1, 2007, 9:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import java.io.*;

public class Project3
{
    public final static void main(String[] argv)
    {
        NetworkSimulator simulator;
        
        int trace = -1;
        int seed = -1;
        String filename = "";
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String buffer = "";
                                   
        System.out.println("Network Simulator v1.0");

        if (argv.length == 3) {
          trace = Integer.parseInt(argv[0]);
          seed = Integer.parseInt(argv[1]);
          filename = argv[2];
        } 
        
        while (trace < 0)
        {
            System.out.print("Enter trace level (>= 0): [0] ");
            try
            {
                buffer = stdIn.readLine();
            }
            catch (IOException ioe)
            {
                System.out.println("IOError reading your input!");
                System.exit(1);
            }
            
            if (buffer.equals(""))
            {
                trace = 3;
            }
            else
            {            
                try
                {
                    trace = Integer.parseInt(buffer);
                }
                catch (NumberFormatException nfe)
                {
                    trace = -1;
                }
            }
        }
        
        while (seed < 0)
        {
            System.out.print("Enter seed (>= 0): [3322] ");
            try
            {
                buffer = stdIn.readLine();
            }
            catch (IOException ioe)
            {
                System.out.println("IOError reading your input!");
                System.exit(1);
            }
            
            if (buffer.equals(""))
            {
                seed = 999;
            }
            else
            {            
                try
                {
                    seed = Integer.parseInt(buffer);
                }
                catch (NumberFormatException nfe)
                {
                    seed = -1;
                }
            }
        }

        while (filename.equals(""))
        {
          System.out.print("Enter topology file (topology.txt): ['default_topology.txt'] ");
          try
          {
              buffer = stdIn.readLine();
          }
          catch (IOException ioe)
          {
              System.out.println("IOError reading your input!");
              System.exit(1);
          }

          if (buffer.equals(""))
          {
              filename = "default_topology.txt";
          }
          else
          {            
              filename = buffer;
          }
        }
         
        simulator = new NetworkSimulator(trace, seed, filename);                  
        simulator.runSimulator();
    }
}
