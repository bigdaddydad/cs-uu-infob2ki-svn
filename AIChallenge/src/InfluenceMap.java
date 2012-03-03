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
		// Initialiseer de map
		map = new int[gameState.getRows()][gameState.getCols()];
		
		// Bereken voor elk object de influence
		for (Tile object : objects)
			calculateInfluence(gameState, object, radius);
	}
	
	/**
     * Functie die gegeven een object en een radius via een BFS methode de influence berekent 
     */
	private void calculateInfluence(GameState gameState, Tile object, int radius)
	{	
		// Initialiseer een set waar locaties in komen te staan die bezocht zijn
		Set<Tile> visited = new HashSet<Tile>();
		
		// Initialiseer een queue waar locaties in komen te staan waarvan buren bezocht moeten worden
		LinkedList<Tile> queue = new LinkedList<Tile>();
		
		// Bepaal influence voor start locatie en markeer hem als bezocht
		object.depth = 0;
		map[object.getRow()][object.getCol()] += radius;
		visited.add(object);
		
		// Voeg start locatie toe aan de queue
		queue.add(object);
		
		// Voer algoritme uit zolang de queue niet leeg is
		while (!queue.isEmpty())
		{
			// Haal het voorste element uit de queue
			Tile x = queue.remove();
			
			// Loop alle buurlocaties langs
			for (Tile y : getNeighbors(gameState, x))
			{
				// Ga alleen verder als locatie nog niet bezocht is
				if (!visited.contains(y))
				{
					// Bepaal influence voor locatie en markeer hem als bezocht
					y.depth = x.depth + 1;
					map[y.getRow()][y.getCol()] += (radius - y.depth);
					visited.add(y);
					
					// Voeg locatie toe aan de queue als buren nog binnen de radius vallen
					if (y.depth < radius)
						queue.add(y);
				}
			}
		}
	}
	
	/**
     * Functie die gegeven een locatie de buurlocaties geeft
     * @return lijst van buurlocaties
     */
    private Set<Tile> getNeighbors(GameState gameState, Tile t)
	{
		Set<Tile> result = new HashSet<Tile>();
		
		// Loop alle directies langs en voeg toegankelijke buren toe aan het resultaat
		for (Aim direction : Aim.values())
		{
			Tile neighbor = gameState.getTile(t, direction);
			
			if (!gameState.isWater(neighbor))
				result.add(neighbor);
		}
		
		return result;
	}
	
	/**
     * Functie die gegeven een locatie de influence waarde geeft
     * @return influence waarde
     */
	public int getValue(Tile t)
	{
		return map[t.getRow()][t.getCol()];
	}
	
	@Override
    public String toString() 
	{
		String result = "";
		
		for (int[] row : map)
		{
			for (int value : row)
			{
				result += "| " + value + " ";
				
				if (value < 10)
					result += " ";
			}
			
			result += "|\n";
		}
		
        return result;
    }
}
