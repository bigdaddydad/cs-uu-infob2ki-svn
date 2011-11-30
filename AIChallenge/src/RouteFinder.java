import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class RouteFinder {
	
	public static Route getShortestRoute(Tile ant, Tile goal, Ants gameState)
	{
		Tile start = ant;
		
		ArrayList<Tile> closedset = new ArrayList<Tile>();
		PriorityQueue<Tile> openset = new PriorityQueue<Tile>(1, new Comparator<Tile>() {
			public int compare(Tile t1, Tile t2) {
				return t1.f_score - t2.f_score;
			}
		});
		
		start.parent = null;
		start.g_score = 0;
		start.h_score = estimateDistance(start, goal, gameState);
		start.f_score = start.g_score + start.h_score;
		openset.add(start);
		
	    while (!openset.isEmpty())
	    {
	    	Tile x = openset.remove();
	    	
			if (x.equals(goal))
				return reconstructRoute(ant, x);				
			
			closedset.add(x);
			
			for (Tile y : getNeighbors(x, gameState))
			{
			    if (closedset.contains(y))
			    	continue;
			    
			    int g_score = x.g_score + 1;
			    
			    if (!openset.contains(y))
			    {
			    	y.parent = x;
			        y.g_score = g_score;
			        y.h_score = estimateDistance(y, goal, gameState);
			        y.f_score = y.g_score + y.h_score;
			        openset.add(y);
			    }
			    else if (g_score < y.g_score)
			    {
			    	openset.remove(y);
			    	y.parent = x;
			        y.g_score = g_score;
			        y.h_score = estimateDistance(y, goal, gameState);
			        y.f_score = y.g_score + y.h_score;
			        openset.add(y);
			    }
			}
	    }

	    return null;
	}
	
	private static Route reconstructRoute(Tile ant, Tile current)
	{
		LinkedList<Tile> path = new LinkedList<Tile>();
		
		Tile start = ant;
		Tile tile = current;
		
		while(tile != null && !tile.equals(start))
		{
			path.addFirst(tile);
			tile = tile.parent;
		}
		
		return new Route(ant, current, path);
	}
	
	private static int estimateDistance(Tile t1, Tile t2, Ants gameState)
	{
		int rowDelta = Math.abs(t1.getRow() - t2.getRow());
        int colDelta = Math.abs(t1.getCol() - t2.getCol());
        
        rowDelta = Math.min(rowDelta, gameState.getRows() - rowDelta);
        colDelta = Math.min(colDelta, gameState.getCols() - colDelta);
        
        return rowDelta + colDelta;
	}
	
	private static ArrayList<Tile> getNeighbors(Tile tile, Ants gameState)
	{
		ArrayList<Tile> result = new ArrayList<Tile>();
		
		if (gameState.getIlk(tile, Aim.WEST).isPassable())
			result.add(gameState.getTile(tile, Aim.WEST));
		
		if (gameState.getIlk(tile, Aim.NORTH).isPassable())
			result.add(gameState.getTile(tile, Aim.NORTH));
		
		if (gameState.getIlk(tile, Aim.EAST).isPassable())
			result.add(gameState.getTile(tile, Aim.EAST));
		
		if (gameState.getIlk(tile, Aim.SOUTH).isPassable())
			result.add(gameState.getTile(tile, Aim.SOUTH));
		
		return result;
	}
}
