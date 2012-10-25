package tools;

import java.util.ArrayList;

public class Astar {
	
	public Astar() {
		// TODO Auto-generated constructor stub
	}
	
	public Route calcMoverRoute(int[][] map, int[] start, int[] goal)
	{
		Square[][] squares = new Square[map.length][map[0].length];
		
		for (int i = 0; i<map.length; i++)
			for (int j = 0; j<map[0].length;j++)
				squares[i][j] = new Square(i,j,10000);

		
		ArrayList<Square> open_list = new ArrayList<Square>();
		
		squares[goal[0]][goal[1]].price = 0;
		open_list.add(squares[goal[0]][goal[1]]);
		
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
		
		for (int i = 0; i < map[0].length; i++)
		{
			for (int j = 0; j < map.length; j++)
				System.out.print(squares[j][i].price + "\t");
			System.out.println("");
		}		
		
		String ret = RouteToString(squares, start);
		System.out.println("Route: " + ret);
		
		if (!ret.equals("-"))
			return new Route(squares[start[0]][start[1]].price,ret,true);
		else
			return null;
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
	
	private class Square
	{
		public int price;
		public int x;
		public int y;
		
		public boolean isClosed;
		public boolean isOpen;
		
		public Square(int X, int Y, int Price) {
			x = X;
			y = Y;
			price = Price;
			isClosed = false;
			isOpen = false;
		}
	}

}
