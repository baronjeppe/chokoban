/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
 *****************************************************************/

package map;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import jade.core.Agent;



public class MapAgent extends Agent {
	
	private static final int PATH_SIZE = 12;
	private static final int FIGURE_SIZES = 32;
	private int MAP_HEIGHT = 0;
	private int MAP_WIDTH = 0;
	private int NO_OF_GOALS = 0;
	private int[][] map;

	// Put agent initializations here
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hallo! BoxAgent " + getAID().getName() + " is running.");

		
		// Loading arguments
		Object[] args = getArguments();
		if (args != null && args.length > 0) {

		}
		else {
			// Make the agent terminate
			System.out.println("No arguments was passed");
			//doDelete();
		}
		
		
		map = loader();
		
		Viewer viewer = new Viewer(MAP_WIDTH, MAP_HEIGHT);
		
		viewer.drawMap(map);
	}
	
	private int[][] emptyMap(int[][] mapIn){
		for(int i = 0 ; i < mapIn.length; i++)
			for(int j = 0; j < mapIn[0].length; j++)
				mapIn[i][j] = 0;
		return mapIn;
	}
	
	private int[][] loader(){
		
		int[][] path = new int[0][0];
		
		try{
			  // Open the file that is the first 
			  // command line parameter
			  FileInputStream fstream = new FileInputStream("map/mymap.txt");
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  strLine = br.readLine();
			  
			  // Do something to the map specs
			  String delimiter = " ";
			  String[] temp = strLine.split(delimiter);
			 
			  if (temp.length == 3)
			  {
				  MAP_WIDTH = Integer.parseInt(temp[0]);
				  MAP_HEIGHT = Integer.parseInt(temp[1]);
				  NO_OF_GOALS = Integer.parseInt(temp[2]);				  
			  }
			  else
				  System.out.println("ERROR: Map specs has wrong format");


			  
			  path = new int[MAP_WIDTH][MAP_HEIGHT];
			  path = emptyMap(path);
			  
			  int j = 0;
			  //Read File Line By Line
			  int solvers = 10;
			  int boxes = 1000;
			  int goals = 100;
			  
			  while ((strLine = br.readLine()) != null)   {
				  // Print the content on the console
				 // System.out.println (strLine);
				  for( int i = 0 ; i < strLine.length()  ; i++){
					  switch (strLine.charAt(i)) {
					case 'X':
					case 'x':
						path[i][j] = 1;
						break;
						  
					case '.':
						path[i][j] = 2;
						break;
						
					case 'M':
					case 'm':
						path[i][j] = solvers++;
						break;
						
					case 'G':
					case 'g':
						path[i][j] = goals++;
						break;
						
					case 'J':
					case 'j':
						path[i][j] = boxes++;
						break;
						
					default:
						break;
					}
				  }
				  j++;
			  }
			  //Close the input stream
			  in.close();
			    }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
			  }
		return path;
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("BoxAgent " + getAID().getName() + " terminating.");
	}
}
