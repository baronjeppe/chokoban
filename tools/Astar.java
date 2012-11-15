package tools;

import java.util.ArrayList;

public class Astar {
	
	public Astar() {
		// TODO Auto-generated constructor stub
	}
	
	public static ArrayList<Integer> findGoals(int[][] map){

		ArrayList<Integer> ret = new ArrayList<Integer>();

		for(int i = 0; i < map.length; i++)
			for(int j = 0; j < map[0].length; j++){
				if(map[i][j] >= 100 && map[i][j] < 999){
					ret.add(i);
					ret.add(j);
				}
			}
		
		return ret;
	}
	
	public static ArrayList<Integer> findBoxes(int[][] map){

		ArrayList<Integer> ret = new ArrayList<Integer>();

		for(int i = 0; i < map.length; i++)
			for(int j = 0; j < map[0].length; j++){
				if(map[i][j] >= 1000 && map[i][j] < 9999){
					ret.add(i);
					ret.add(j);
				}
			}
		
		return ret;
	}
	
	public static int[][] calcMoverHeuristicOld(int[][] map)
	{
		int[][] ret = new int[map.length][map[0].length];
		Square[][] squares = new Square[map.length][map[0].length];
		
		for (int i = 0; i<map.length; i++)
			for (int j = 0; j<map[0].length;j++)
				squares[i][j] = new Square(i,j,10000);
		
		ArrayList<Square> open_list = new ArrayList<Square>();
		ArrayList<Integer> goals = findBoxes(map);
		
		/*for(int i = 0; i < goals.size(); i++)
			System.out.println(goals.get(i));*/

		
		for(int i = 0; i < goals.size(); i += 2){
			squares[goals.get(i)][goals.get(i+1)].price = 2;
			open_list.add(squares[goals.get(i)][goals.get(i+1)]);
		}
		
		while(!open_list.isEmpty()){
			
			Square curSq = open_list.get(0);
			
			// up
			if (map[curSq.x][curSq.y-1] > 1 && map[curSq.x][curSq.y-1] < 9999)
				if(squares[curSq.x][curSq.y-1].price > curSq.price*2)
				{
					squares[curSq.x][curSq.y-1].price = curSq.price*2;
					open_list.add(squares[curSq.x][curSq.y-1]);
				}
			// down
			if (map[curSq.x][curSq.y+1] > 1 && map[curSq.x][curSq.y+1] < 9999)
				if(squares[curSq.x][curSq.y+1].price > curSq.price*2)
				{
					squares[curSq.x][curSq.y+1].price = curSq.price*2;
					open_list.add(squares[curSq.x][curSq.y+1]);
				}
			// left
			if (map[curSq.x-1][curSq.y] > 1 && map[curSq.x-1][curSq.y] < 9999)
				if(squares[curSq.x-1][curSq.y].price > curSq.price*2)
				{
					squares[curSq.x-1][curSq.y].price = curSq.price*2;
					open_list.add(squares[curSq.x-1][curSq.y]);
				}
			// right
			if (map[curSq.x+1][curSq.y] > 1 && map[curSq.x+1][curSq.y] < 9999)
				if(squares[curSq.x+1][curSq.y].price > curSq.price*2)
				{
					squares[curSq.x+1][curSq.y].price = curSq.price*2;
					open_list.add(squares[curSq.x+1][curSq.y]);
				}
			
			open_list.remove(0);
		}
		/*
		for (int i = 0; i < map[0].length; i++){
			for (int j = 0; j < map.length; j++){
				System.out.print(squares[j][i].price + "\t");
			}
			System.out.println("");
		}*/	
		
		
		for(int i = 0; i < map[0].length; i++)
			for(int j = 0; j < map.length; j++)
				ret[j][i] = squares[j][i].price;
		
		return ret;
		
	}
	
	public static int[][] calcMoverHeuristic(int[][] map)
	{
		int[][] ret = new int[map.length][map[0].length];
		Square[][] squares = new Square[map.length][map[0].length];
		
		for (int i = 0; i<map.length; i++)
			for (int j = 0; j<map[0].length;j++)
				squares[i][j] = new Square(i,j,10000);
		
		ArrayList<Square> open_list = new ArrayList<Square>();
		ArrayList<Integer> goals = findBoxes(map);
		
		/*for(int i = 0; i < goals.size(); i++)
			System.out.println(goals.get(i));*/

		
		for(int i = 0; i < goals.size(); i += 2){
			squares[goals.get(i)][goals.get(i+1)].price = 0;
			open_list.add(squares[goals.get(i)][goals.get(i+1)]);
		}
		
		while(!open_list.isEmpty()){
			
			Square curSq = open_list.get(0);
			
			// up
			if (map[curSq.x][curSq.y-1] > 1 && map[curSq.x][curSq.y-1] < 9999)
				if(squares[curSq.x][curSq.y-1].price > curSq.price+1)
				{
					squares[curSq.x][curSq.y-1].price = curSq.price+1;
					open_list.add(squares[curSq.x][curSq.y-1]);
				}
			// down
			if (map[curSq.x][curSq.y+1] > 1 && map[curSq.x][curSq.y+1] < 9999)
				if(squares[curSq.x][curSq.y+1].price > curSq.price+1)
				{
					squares[curSq.x][curSq.y+1].price = curSq.price+1;
					open_list.add(squares[curSq.x][curSq.y+1]);
				}
			// left
			if (map[curSq.x-1][curSq.y] > 1 && map[curSq.x-1][curSq.y] < 9999)
				if(squares[curSq.x-1][curSq.y].price > curSq.price+1)
				{
					squares[curSq.x-1][curSq.y].price = curSq.price+1;
					open_list.add(squares[curSq.x-1][curSq.y]);
				}
			// right
			if (map[curSq.x+1][curSq.y] > 1 && map[curSq.x+1][curSq.y] < 9999)
				if(squares[curSq.x+1][curSq.y].price > curSq.price+1)
				{
					squares[curSq.x+1][curSq.y].price = curSq.price+1;
					open_list.add(squares[curSq.x+1][curSq.y]);
				}
			
			open_list.remove(0);
		}
		/*
		for (int i = 0; i < map[0].length; i++){
			for (int j = 0; j < map.length; j++){
				System.out.print(squares[j][i].price + "\t");
			}
			System.out.println("");
		}*/	
		
		
		for(int i = 0; i < map[0].length; i++)
			for(int j = 0; j < map.length; j++)
				ret[j][i] = squares[j][i].price;
		
		return ret;
		
	}
	
	public static int[][] calcBoxHeuristic(int[][] map)
	{
		int[][] ret = new int[map.length][map[0].length];
		Square[][] squares = new Square[map.length][map[0].length];
		
		for (int i = 0; i<map.length; i++)
			for (int j = 0; j<map[0].length;j++)
				squares[i][j] = new Square(i,j,10000);
		
		ArrayList<Square> open_list = new ArrayList<Square>();
		ArrayList<Integer> goals = findGoals(map);
		
		/*for(int i = 0; i < goals.size(); i++)
			System.out.println(goals.get(i));*/

		
		for(int i = 0; i < goals.size(); i += 2){
			squares[goals.get(i)][goals.get(i+1)].price = 0;
			open_list.add(squares[goals.get(i)][goals.get(i+1)]);
		}
		
		while(!open_list.isEmpty()){
			
			Square curSq = open_list.get(0);
			
			// up
			if (map[curSq.x][curSq.y-1] == 2)
				if(squares[curSq.x][curSq.y-1].price > curSq.price+1)
				{
					squares[curSq.x][curSq.y-1].price = curSq.price+1;
					open_list.add(squares[curSq.x][curSq.y-1]);
				}
			// down
			if (map[curSq.x][curSq.y+1] == 2)
				if(squares[curSq.x][curSq.y+1].price > curSq.price+1)
				{
					squares[curSq.x][curSq.y+1].price = curSq.price+1;
					open_list.add(squares[curSq.x][curSq.y+1]);
				}
			// left
			if (map[curSq.x-1][curSq.y] == 2)
				if(squares[curSq.x-1][curSq.y].price > curSq.price+1)
				{
					squares[curSq.x-1][curSq.y].price = curSq.price+1;
					open_list.add(squares[curSq.x-1][curSq.y]);
				}
			// right
			if (map[curSq.x+1][curSq.y] == 2)
				if(squares[curSq.x+1][curSq.y].price > curSq.price+1)
				{
					squares[curSq.x+1][curSq.y].price = curSq.price+1;
					open_list.add(squares[curSq.x+1][curSq.y]);
				}
			
			open_list.remove(0);
		}
		/*
		for (int i = 0; i < map[0].length; i++){
			for (int j = 0; j < map.length; j++){
				System.out.print(squares[j][i].price + "\t");
			}
			System.out.println("");
		}*/	
		
		
		for(int i = 0; i < map[0].length; i++)
			for(int j = 0; j < map.length; j++)
				ret[j][i] = squares[j][i].price;
		
		return ret;
		
	}
	
	public static int calcPrice(int[][] map, int[][] heuristic)
	{
		int ret = 0;
		for(int i = 0; i < map.length; i++)
			for(int j = 0; j < map[0].length; j++)
				if (map[i][j] == 1000)
					ret += heuristic[i][j];
		return ret;
	
		
	}
	
	private String RouteToString(Square[][] squares, int[] start)
	{
		Square curSq = squares[start[0]][start[1]];
		String ret = "";
		boolean goal_found = false;
		while(!goal_found)
		{
			// up
			if(squares[curSq.x][curSq.y-1].price < curSq.price)
			{
				ret += "u";
				curSq = squares[curSq.x][curSq.y-1];
			}
			// down
			else if(squares[curSq.x][curSq.y+1].price < curSq.price)
			{
				ret += "d";
				curSq = squares[curSq.x][curSq.y+1];
			}
			// left
			else if(squares[curSq.x-1][curSq.y].price < curSq.price)
			{
				ret += "l";
				curSq = squares[curSq.x-1][curSq.y];
			}
			// right
			else if(squares[curSq.x+1][curSq.y].price < curSq.price)
			{
				ret += "r";
				curSq = squares[curSq.x+1][curSq.y];
			}
			else
			{
				System.out.println("Route not found..!");
				ret = "-";
				break;
			}
			if (curSq.price == 0)
				goal_found = true;		
		}
		return ret;
	}

}
