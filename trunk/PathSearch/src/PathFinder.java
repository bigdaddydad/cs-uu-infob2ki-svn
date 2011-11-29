import java.util.ArrayList;
import java.util.PriorityQueue;

public class PathFinder {

	private Map map;
	
	public PathFinder(Map map)
	{
		this.map = map;
	}
	
	public ArrayList<Tile> shortestPath(Tile start, Tile goal)
	{
		if (start != goal)
		{
			start.g_score = 0;
			start.h_score = getEstimatedDistance(start, goal);
			start.f_score = start.g_score + start.h_score;
			
			ArrayList<Tile> closedset = new ArrayList<Tile>();
			PriorityQueue<Tile> openset = new PriorityQueue<Tile>(start.f_score, new TilePathComparator());
			
			openset.add(start);
			
		    while (!openset.isEmpty())
		    {
		    	Tile x = openset.remove();
		    	
				if (x == goal)
					return reconstructPath(start, goal);
				
				closedset.add(x);
				
				for (Tile y : getNeighbors(x))
				{
				    if (closedset.contains(y))
				    	continue;
				    
				    int g_score = x.g_score + 1;
				    
				    if (!openset.contains(y))
				    {
				    	y.parent = x;
				        y.g_score = g_score;
				        y.h_score = getEstimatedDistance(y, goal);
				        y.f_score = y.g_score + y.h_score;
				        openset.add(y);
				    }
				    else if (g_score < y.g_score)
				    {
				    	openset.remove(y);
				    	y.parent = x;
				        y.g_score = g_score;
				        y.h_score = getEstimatedDistance(y, goal);
				        y.f_score = y.g_score + y.h_score;
				        openset.add(y);
				    }
				}
		    }
		}
	    
	    return new ArrayList<Tile>();
	}
	
	private ArrayList<Tile> reconstructPath(Tile start, Tile current)
	{
		ArrayList<Tile> result = new ArrayList<Tile>();
		Tile parent = current;
		
		while(parent != null && parent != start)
		{
			result.add(parent);
			parent = parent.parent;
		}
		
		return result;
	}
	
	private ArrayList<Tile> getNeighbors(Tile t)
	{
		ArrayList<Tile> result = new ArrayList<Tile>();
		Tile neighbor;
		
		for (int i = -1; i < 2; i += 2)
		{
			neighbor = map.getData((t.getCol() + i + map.getCols()) % map.getCols(), t.getRow());
			
			if (map.isAccessible(neighbor))
				result.add(neighbor);
			
			neighbor = map.getData(t.getCol(), (t.getRow() + i + map.getRows()) % map.getRows());
			
			if (map.isAccessible(neighbor))
				result.add(neighbor);
		}	
		
		return result;
	}
	
	private int getEstimatedDistance(Tile begin, Tile end)
	{
		return Math.abs(begin.getCol() - end.getCol()) + Math.abs(begin.getRow() - end.getRow());
	}
}
