import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Represents an influence map.
 */
public class InfluenceMap {

	private int[][] map;
	
	public InfluenceMap(GameState gameState, Set<Tile> objects, int radius)
	{
		map = new int[gameState.getRows()][gameState.getCols()];
		
		for (Tile object : objects)
			calculateInfluence(gameState, object, radius);
	}
	
	private void calculateInfluence(GameState gameState, Tile object, int radius)
	{	
		Set<Tile> visited = new HashSet<Tile>();
		LinkedList<Tile> queue = new LinkedList<Tile>();
		
		object.depth = 0;
		map[object.getRow()][object.getCol()] += radius;
		visited.add(object);
		
		queue.add(object);
		
		while (!queue.isEmpty())
		{
			Tile x = queue.remove();
			
			for (Tile y : getNeighbors(gameState, x))
			{
				if (!visited.contains(y))
				{
					y.depth = x.depth + 1;
					map[y.getRow()][y.getCol()] += (radius - y.depth);
					visited.add(y);
					
					if (y.depth < radius)
						queue.add(y);
				}
			}
		}
	}
	
	private static Set<Tile> getNeighbors(GameState gameState, Tile t)
	{
		// Lijst met alle buurlocaties
		Set<Tile> neighbors = new HashSet<Tile>();
		
		// Loop alle directies langs en voeg toegankelijke buren toe aan de lijst
		for (Aim direction : Aim.values())
		{
			Tile neighbor = gameState.getTile(t, direction);
			
			if (!gameState.isWater(neighbor))
				neighbors.add(neighbor);
		}
		
		return neighbors;
	}
}
