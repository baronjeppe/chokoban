package tools;

public class Square
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