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
	

	private static final int FIGURE_SIZES = 32;
	
	private final JFrame frame  = new JFrame();;
	
	public Viewer(int map_width, int map_height){

		frame.setSize((1+map_width)*FIGURE_SIZES-16,(1+map_height)*FIGURE_SIZES+6);
		
		Color c = new Color(0,0,0);
		frame.getContentPane().setBackground(c);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}
	
	public void drawMap(int[][] map){
		frame.getContentPane().removeAll();
		frame.revalidate();
		frame.repaint();
		drawMapPriv(map);
	}

	private void drawMapPriv(int[][] map){
		printMap(map);
		for(int i = 0; i < map.length; i++){
			for(int j = 0; j < map[0].length; j++){
				if(map[i][j] == 1)
					drawWall(i*FIGURE_SIZES, j*FIGURE_SIZES);
				
				else if(map[i][j] == 2)
					drawBackground(i*FIGURE_SIZES, j*FIGURE_SIZES);
				
				else if(map[i][j] >= 10 && map[i][j] <= 99)
					drawSolver(i*FIGURE_SIZES, j*FIGURE_SIZES);
				
				else if(map[i][j] >= 100 && map[i][j] <= 999)
					drawGoal(i*FIGURE_SIZES, j*FIGURE_SIZES);
				
				else if(map[i][j] >= 1000 && map[i][j] <= 9999 )
					drawBox(i*FIGURE_SIZES, j*FIGURE_SIZES);
				

				/*
				switch (map[i][j]) {
				case 1:
					drawWall(i*FIGURE_SIZES, j*FIGURE_SIZES);
					break;
					
				case 2:
					drawBackground(i*FIGURE_SIZES, j*FIGURE_SIZES);
					break;
					
				case :
					drawGoal(i*FIGURE_SIZES, j*FIGURE_SIZES);
					break;
					
				case 74:
					drawBox(i*FIGURE_SIZES, j*FIGURE_SIZES);
					
				case 77:
					drawSolver(i*FIGURE_SIZES, j*FIGURE_SIZES);
					break;
					

				default:
					break;
				}*/
			}
		}
	}
	
	private void printMap(int[][] map){
		for(int i = 0 ; i < map[0].length ; i++){
			for(int j = 0; j < map.length ; j++)
				System.out.print(map[j][i] + " ");
			System.out.println();
		}
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
