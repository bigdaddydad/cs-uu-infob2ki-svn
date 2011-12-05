import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class PathFinder {
	
	public static LinkedList<Tile> shortestPath(Tile start, Tile end, Map map)
	{
		ArrayList<Tile> closedset = new ArrayList<Tile>();
		PriorityQueue<Tile> openset = new PriorityQueue<Tile>(1, new Comparator<Tile>() {
			public int compare(Tile t1, Tile t2) {
				return t1.f_score - t2.f_score;
			}
		});
		
		start.parent = null;
		start.g_score = 0;
		start.h_score = estimateDistance(start, end, map);
		start.f_score = start.g_score + start.h_score;
		openset.add(start);
		
	    while (!openset.isEmpty())
	    {
	    	Tile x = openset.remove();
	    	
			if (x.equals(end))
				return reconstructPath(start, x);
			
			closedset.add(x);
			
			for (Tile y : map.getNeighbors(x))
			{
			    if (closedset.contains(y))
			    	continue;
			    
			    int g_score = x.g_score + 1;
			    
			    if (!openset.contains(y))
			    {
			    	y.parent = x;
			        y.g_score = g_score;
			        y.h_score = estimateDistance(y, end, map);
			        y.f_score = y.g_score + y.h_score;
			        openset.add(y);
			    }
			    else if (g_score < y.g_score)
			    {
			    	openset.remove(y);
			    	y.parent = x;
			        y.g_score = g_score;
			        y.h_score = estimateDistance(y, end, map);
			        y.f_score = y.g_score + y.h_score;
			        openset.add(y);
			    }
			}
	    }
	    
	    return null;
	}
	
	private static LinkedList<Tile> reconstructPath(Tile start, Tile end)
	{
		LinkedList<Tile> result = new LinkedList<Tile>();
		Tile current = end;
		
		while(current != null && !current.equals(start))
		{
			result.addFirst(current);
			current = current.parent;
		}
		
		return result;
	}
	
	private static int estimateDistance(Tile t1, Tile t2, Map map)
	{
		int rowDelta = Math.abs(t1.getRow() - t2.getRow());
        int colDelta = Math.abs(t1.getCol() - t2.getCol());
        
        rowDelta = Math.min(rowDelta, map.getRows() - rowDelta);
        colDelta = Math.min(colDelta, map.getCols() - colDelta);
        
        return rowDelta + colDelta;
	}
}
