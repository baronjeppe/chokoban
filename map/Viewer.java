
package map;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Viewer implements MouseListener {
	
	JComponent[][] graphics;
	
	int[][] map_data;

	private static final int FIGURE_SIZES = 32;
	
	private JFrame frame  = new JFrame();
		
	public Viewer(int[][] map){
		graphics = new JComponent[map.length][map[0].length];
		
		map_data = new int[map.length][];
		for(int i = 0; i < map.length; i++)
		{
		  map_data[i] = new int[map[i].length];
		  for (int j = 0; j < map[i].length; j++)
		  {
		    map_data[i][j] = map[i][j];
		  }
		}

		frame.setSize((1+3+map.length)*FIGURE_SIZES-16,(1+map[0].length)*FIGURE_SIZES+6);
		
		Color c = new Color(0,0,0);
		frame.getContentPane().setBackground(c);		
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


	}
	
	public void updateMap(int[][] map)
	{
		for(int i = 0; i < map.length; i++){
			for(int j = 0; j < map[0].length; j++){
				if (map_data[i][j] != map[i][j])
				{
					//System.out.println("map_data [" + i + "][" + j + "]:" + map_data[i][j] + " map: " + map[i][j]);
					frame.getContentPane().remove(graphics[i][j]);
					if(map[i][j] == 1)
						drawWall(i, j);
					
					else if(map[i][j] == 2)
						drawBackground(i, j);
					
					else if(map[i][j] >= 10 && map[i][j] <= 99)
						drawSolver(i, j);
					
					else if(map[i][j] >= 100 && map[i][j] <= 999)
						drawGoal(i, j);
					
					else if(map[i][j] >= 1000 && map[i][j] <= 9999 )
						drawBox(i, j);
				}
			}
		}
		frame.validate();
		frame.repaint();
		
		map_data = new int[map.length][];
		for(int i = 0; i < map.length; i++)
		{
		  map_data[i] = new int[map[i].length];
		  for (int j = 0; j < map[i].length; j++)
		  {
		    map_data[i][j] = map[i][j];
		  }
		}
	}

	public void drawMap(int[][] map){
		frame.getContentPane().removeAll();
		frame.validate();
		frame.repaint();
		drawMapPriv(map);
	}

	private void drawMapPriv(int[][] map){
		printMap(map);
		for(int i = 0; i < map.length; i++){
			for(int j = 0; j < map[0].length; j++){
				if(map[i][j] == 1)
					drawWall(i, j);
				
				else if(map[i][j] == 2)
					drawBackground(i, j);
				
				else if(map[i][j] >= 10 && map[i][j] <= 99)
					drawSolver(i, j);
				
				else if(map[i][j] >= 100 && map[i][j] <= 999)
					drawGoal(i, j);
				
				else if(map[i][j] >= 1000 && map[i][j] <= 9999 )
					drawBox(i, j);
				
			}
		}
		drawBut(map.length,0);
	}
	
	private void printMap(int[][] map){
		for(int i = 0 ; i < map[0].length ; i++){
			for(int j = 0; j < map.length ; j++)
				System.out.print(map[j][i] + " ");
			System.out.println();
		}
	}
	

	private void drawBox(int x, int y){
		ShowBox panel = new ShowBox(x*FIGURE_SIZES,y*FIGURE_SIZES);
		graphics[x][y] = panel;
		frame.add(panel);
		frame.setVisible(true);
	}
	
	private void drawSolver(int x, int y){
		ShowSolver panel = new ShowSolver(x*FIGURE_SIZES,y*FIGURE_SIZES);
		graphics[x][y] = panel;
		frame.add(panel);
		frame.setVisible(true);
	}
	
	private void drawGoal(int x, int y){
		ShowGoal panel = new ShowGoal(x*FIGURE_SIZES,y*FIGURE_SIZES);
		graphics[x][y] = panel;
		frame.add(panel);
		frame.setVisible(true);
	}
	
	private void drawBackground(int x, int y){
		ShowBackground panel = new ShowBackground(x*FIGURE_SIZES,y*FIGURE_SIZES);
		graphics[x][y] = panel;
		frame.add(panel);
		frame.setVisible(true);
	}
	
	private void drawWall(int x, int y){
		ShowWall panel = new ShowWall(x*FIGURE_SIZES,y*FIGURE_SIZES);
		graphics[x][y] = panel;
		frame.add(panel);
		frame.setVisible(true);
	}

	private void drawBut(int x, int y){
		ShowBut panel = new ShowBut(x*FIGURE_SIZES,y*FIGURE_SIZES);
		panel.addMouseListener(this);
		frame.add(panel);
		frame.setVisible(true);
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		System.out.println("lol");
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
