package box;
import java.util.*;

public class Node implements Comparable<Node> {
    public Node parent;
    public State state;
    public char move;
 
    public static int nodesExpanded = 0;

    Node(Node parent, char move, State state) {
	this.parent = parent;
	this.state = state;
	this.move = move;
    }


    LinkedList<Node> expand() {
		LinkedList<Node> childNodes = new LinkedList<Node>();
		nodesExpanded++;
	
		State newState;
		
		newState = state.actionRight();
		if (newState!=null) childNodes.addLast( new Node(this, 'L', newState ) );
		newState = state.actionLeft();
		if (newState!=null) childNodes.addLast( new Node(this, 'R', newState ) );	    
		newState = state.actionUp();
		if (newState!=null) childNodes.addLast( new Node(this, 'D', newState ) );
		newState = state.actionDown();
		if (newState!=null) childNodes.addLast( new Node(this, 'U', newState ) );
		newState = state.actionRightN();
		if (newState!=null) childNodes.addLast( new Node(this, 'L', newState ) );
		newState = state.actionLeftN();
		if (newState!=null) childNodes.addLast( new Node(this, 'R', newState ) );	    
		newState = state.actionUpN();
		if (newState!=null) childNodes.addLast( new Node(this, 'D', newState ) );
		newState = state.actionDownN();
		if (newState!=null) childNodes.addLast( new Node(this, 'U', newState ) );
		
		return(childNodes);
    }
    
    public String printSolution()
    {
    	String ret = printInnerSolution();
    	
    	while (Character.isLowerCase(ret.charAt(ret.length()-1)))
    		ret = ret.substring(0, ret.length()-1);
    	
    	return ret;
    }
    
    private String printInnerSolution(){
    	if( parent!= null ) {
    		if (state.boxMoved)
    			return move + parent.printInnerSolution();
    		else
    		{
    			String temp = new String();
    			temp += move;
    			return temp.toLowerCase() + parent.printInnerSolution();
    		}

    	}
    	return "";
    }
    
    @Override
    public String toString()
    {
    	String ret = new String();
    	
    	for (int i = 0; i < state.map.length; i++)
    		for (int j = 0; j < state.map[0].length; j++)
    		{
    			ret += String.valueOf(state.map[i][j]);
    		}
    	//System.out.println(ret);
    	return ret;
    }
    
    @Override
    public boolean equals(Object obj)
    {
    	Node temp = (Node)obj;
    	
    /*	temp.state.printMap(temp.state.map);
    	state.printMap(state.map);
    	System.out.println(state.equalToMap(temp.state.map));*/
    	return state.equalToMap(temp.state.map);
    }


	@Override
	public int compareTo(Node arg) {

		if (state.price > arg.state.price)
			return 1;
		else if (state.price < arg.state.price)
			return -1;

		return 0;	
	}

    
 
}