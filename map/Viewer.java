/*************************************************************************************
# Copyright (c) 2012, Thomas Iversen
# All rights reserved.
# 
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
# 1. Redistributions of source code must retain the above copyright
#    notice, this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright
#    notice, this list of conditions and the following disclaimer in the
#    documentation and/or other materials provided with the distribution.
# 3. All advertising materials mentioning features or use of this software
#    must display the following acknowledgement:
#    This product includes software developed by the University of Southern Denmark.
# 4. Neither the name of the University of Southern Denmark nor the
#    names of its contributors may be used to endorse or promote products
#    derived from this software without specific prior written permission.
# 
# THIS SOFTWARE IS PROVIDED BY Anders Bøgild "AS IS" AND ANY
# EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL Anders Bøgild BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
**************************************************************************************
# File:     Viewer.java                                            
# Purpose:  Viewer for the Sokoban game (http://www.game-sokoban.com/), when the map is presented in
# 			an text file, or an array.
# Project:  Sokoban Solver, using agent oriented programming
# Author:   Thomas Iversen (thive09@student.sdu.dk)
# Created:  Sep 23, 2012 Thomas Iversen
*************************************************************************************/
package map;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import javax.swing.JFrame;

public class Viewer {
	
	public static final int PATH_SIZE = 12;
	public static final int FIGURE_SIZES = 32;
	public static final int MAP_HEIGHT = PATH_SIZE * FIGURE_SIZES;
	public static final int MAP_WIDTH =  PATH_SIZE * FIGURE_SIZES;
	private final JFrame frame  = new JFrame();;
	
	private int[][] map = new int[PATH_SIZE][PATH_SIZE];
	
		
	public Viewer(){
		map = loader();

		frame.setSize(MAP_WIDTH,MAP_HEIGHT);
		
		Color c = new Color(0,0,0);
		frame.getContentPane().setBackground(c);
	
		drawMap();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}
	
	public void drawMap(){
		frame.getContentPane().removeAll();
		frame.revalidate();
		frame.repaint();
		drawMapPriv();
	}
	
	private int[][] emptyMap(int[][] mapIn){
		for(int i = 0 ; i < PATH_SIZE; i++)
			for(int j = 0; j < PATH_SIZE; j++)
				mapIn[i][j] = 0;
		return mapIn;
	}
	
	private void drawMapPriv(){
		for(int i = 0; i < PATH_SIZE; i++){
			for(int j = 0; j < PATH_SIZE; j++){
				switch (map[i][j]) {
				case 88:
					drawWall(i*FIGURE_SIZES, j*FIGURE_SIZES);
					break;
					
				case 46:
					drawBackground(i*FIGURE_SIZES, j*FIGURE_SIZES);
					break;
					
				case 71:
					drawGoal(i*FIGURE_SIZES, j*FIGURE_SIZES);
					break;
					
				case 74:
					drawBox(i*FIGURE_SIZES, j*FIGURE_SIZES);
					
				case 77:
					drawSolver(i*FIGURE_SIZES, j*FIGURE_SIZES);
					break;
					

				default:
					break;
				}
			}
		}
	}
	
	private void printMap(int[][] map){
		for(int i = 0 ; i < PATH_SIZE; i++){
			for(int j = 0; j < PATH_SIZE; j++)
				System.out.print(map[j][i] + " ");
			System.out.println();
		}
	}
	
	private int[][] loader(){
		int[][] path = new int[PATH_SIZE][PATH_SIZE];
		path = emptyMap(path);
		
		try{
			  // Open the file that is the first 
			  // command line parameter
			  FileInputStream fstream = new FileInputStream("C:/Users/superthomz/Favorites/Documents/GitHub/chokoban/map/mymap.txt");
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  strLine = br.readLine();
			  
			    // Do something to the map specs
			  for(int i = 0; i < strLine.length(); i++){
				  
			  }
			  int j = 0;
			  //Read File Line By Line
			  while ((strLine = br.readLine()) != null)   {
				  // Print the content on the console
				 // System.out.println (strLine);
				  for( int i = 0 ; i < strLine.length()  ; i++){
					  path[i][j] = (int)strLine.charAt(i);
				  }
				  j++;
			  }
			  //Close the input stream
			  in.close();
			    }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
			  }
		printMap(path);
		return path;
	}

	private void drawBox(int x, int y){
		ShowBox panel = new ShowBox(x,y);
		frame.add(panel);
		frame.setVisible(true);
	}
	
	private void drawSolver(int x, int y){
		ShowSolver panel = new ShowSolver(x,y);
		frame.add(panel);
		frame.setVisible(true);
	}
	
	private void drawGoal(int x, int y){
		ShowGoal panel = new ShowGoal(x,y);
		frame.add(panel);
		frame.setVisible(true);
	}
	
	private void drawBackground(int x, int y){
		ShowBackground panel = new ShowBackground(x,y);
		frame.add(panel);
		frame.setVisible(true);
	}
	
	private void drawWall(int x, int y){
		ShowWall panel = new ShowWall(x,y);
		frame.add(panel);
		frame.setVisible(true);
	}

}
