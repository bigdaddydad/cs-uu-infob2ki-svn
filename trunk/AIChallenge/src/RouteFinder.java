import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class RouteFinder {
	
	public static Route getShortestRoute(Ants gameState, Target target, Tile startLoc, Tile targetLoc)
	{
		ArrayList<Tile> closedset = new ArrayList<Tile>();
		PriorityQueue<Tile> openset = new PriorityQueue<Tile>(1, new Comparator<Tile>() {
			public int compare(Tile t1, Tile t2) {
				return t1.f_score - t2.f_score;
			}
		});
		
		startLoc.parent = null;
		startLoc.g_score = 0;
		startLoc.h_score = estimateDistance(gameState, startLoc, targetLoc);
		startLoc.f_score = startLoc.g_score + startLoc.h_score;
		openset.add(startLoc);
		
	    while (!openset.isEmpty())
	    {
	    	Tile x = openset.remove();
	    	
			if (x.equals(targetLoc))
				return reconstructRoute(target, startLoc, x);				
			
			closedset.add(x);
			
			for (Tile y : getNeighbors(gameState, x))
			{
			    if (closedset.contains(y))
			    	continue;
			    
			    int g_score = x.g_score + 1;
			    
			    if (!openset.contains(y))
			    {
			    	y.parent = x;
			        y.g_score = g_score;
			        y.h_score = estimateDistance(gameState, y, targetLoc);
			        y.f_score = y.g_score + y.h_score;
			        openset.add(y);
			    }
			    else if (g_score < y.g_score)
			    {
			    	openset.remove(y);
			    	y.parent = x;
			        y.g_score = g_score;
			        y.h_score = estimateDistance(gameState, y, targetLoc);
			        y.f_score = y.g_score + y.h_score;
			        openset.add(y);
			    }
			}
	    }

	    return null;
	}
	
	private static Route reconstructRoute(Target target, Tile startLoc, Tile targetLoc)
	{
		LinkedList<Tile> path = new LinkedList<Tile>();
		Tile current = targetLoc;
		
		while(current != null && !current.equals(startLoc))
		{
			path.addFirst(current);
			current = current.parent;
		}
		
		return new Route(target, startLoc, targetLoc, path);
	}
	
	private static int estimateDistance(Ants gameState, Tile t1, Tile t2)
	{
		int rowDelta = Math.abs(t1.getRow() - t2.getRow());
        int colDelta = Math.abs(t1.getCol() - t2.getCol());
        
        rowDelta = Math.min(rowDelta, gameState.getRows() - rowDelta);
        colDelta = Math.min(colDelta, gameState.getCols() - colDelta);
        
        return rowDelta + colDelta;
	}
	
	private static ArrayList<Tile> getNeighbors(Ants gameState, Tile t)
	{
		ArrayList<Tile> result = new ArrayList<Tile>();
		
		if (gameState.getIlk(t, Aim.WEST).isPassable())
			result.add(gameState.getTile(t, Aim.WEST));
		
		if (gameState.getIlk(t, Aim.NORTH).isPassable())
			result.add(gameState.getTile(t, Aim.NORTH));
		
		if (gameState.getIlk(t, Aim.EAST).isPassable())
			result.add(gameState.getTile(t, Aim.EAST));
		
		if (gameState.getIlk(t, Aim.SOUTH).isPassable())
			result.add(gameState.getTile(t, Aim.SOUTH));
		
		return result;
	}
}
