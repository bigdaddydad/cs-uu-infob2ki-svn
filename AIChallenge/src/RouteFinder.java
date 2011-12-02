import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * Represents a route navigator.
 */
public class RouteFinder {
	
	/**
     * Functie die A* algoritme gebruikt om kortste route te vinden tussen gegeven start- en eindlocatie 
     * @return kortste route als er een route is gevonden, <code>null</code> als er geen route is gevonden
     */
	public static Route getShortestRoute(Ants gameState, Target target, Tile startLoc, Tile targetLoc)
	{
		// Initialiseer een set waar locaties in komen te staan die afgerond zijn
		ArrayList<Tile> closedset = new ArrayList<Tile>();
		
		// Initialiseer een set waarbij de locatie met kleinste f score vooraan komt te staan
		PriorityQueue<Tile> openset = new PriorityQueue<Tile>(1, new Comparator<Tile>() {
			public int compare(Tile t1, Tile t2) {
				return t1.f_score - t2.f_score;
			}
		});
		
		// Bepaal parent en scores voor start locatie en voeg hem toe aan queue
		startLoc.parent = null;
		startLoc.g_score = 0;
		startLoc.h_score = estimateDistance(gameState, startLoc, targetLoc);
		startLoc.f_score = startLoc.g_score + startLoc.h_score;
		openset.add(startLoc);
		
		// Voer algoritme uit zolang de queue niet leeg is
	    while (!openset.isEmpty())
	    {
	    	// Haal het voorste element uit de queue
	    	Tile x = openset.remove();
	    	
	    	// Als dit element de eind locatie is, return dan de gevonden route
			if (x.equals(targetLoc))
				return reconstructRoute(target, startLoc, x);				
			
			// Voeg het element toe aan de set van tiles die afgerond zijn
			closedset.add(x);
			
			// Loop alle buurlocaties langs
			for (Tile y : getNeighbors(gameState, x))
			{
				// Sla buurlocatie over als hij al afgerond is
			    if (closedset.contains(y))
			    	continue;
			    
			    // Bepaal nieuwe g score
			    int g_score = x.g_score + 1;
			    
			    if (!openset.contains(y))	// Buur zit nog niet in queue
			    {
			    	// Bepaal parent en scores voor buur en voeg hem toe aan queue
			    	y.parent = x;
			        y.g_score = g_score;
			        y.h_score = estimateDistance(gameState, y, targetLoc);
			        y.f_score = y.g_score + y.h_score;
			        openset.add(y);
			    }
			    else if (g_score < y.g_score)	// nieuwe g score is beter dan bestaande van buur
			    {
			    	// Verwijder buur van queue, update parent en scores en voeg hem weer toe
			    	openset.remove(y);
			    	y.parent = x;
			        y.g_score = g_score;
			        y.h_score = estimateDistance(gameState, y, targetLoc);
			        y.f_score = y.g_score + y.h_score;
			        openset.add(y);
			    }
			}
	    }
	    
	    // Return null als er geen route is gevonden
	    return null;
	}
	
	/**
     * Functie die gegeven een start- en eindlocatie een route reconstrueert
     * @return de route van de startlocatie naar de eindlocatie
     */
	private static Route reconstructRoute(Target target, Tile startLoc, Tile targetLoc)
	{
		// Lijst met alle locaties die samen het pad vormen 
		LinkedList<Tile> path = new LinkedList<Tile>();
		
		// Beginnend bij de laatste locatie
		Tile current = targetLoc;
		
		// Loop alle locaties af door steeds de parent van een locatie te pakken
		while(current != null && !current.equals(startLoc))
		{
			path.addFirst(current);
			current = current.parent;
		}
		
		// Return het pad als route
		return new Route(target, startLoc, targetLoc, path);
	}
	
	/**
     * Functie die de afstand schat tussen twee locaties op de map
     * @return de geschatte afstand
     */
	private static int estimateDistance(Ants gameState, Tile t1, Tile t2)
	{
		// Bereken het verschil in rijen en kolommen
		int rowDelta = Math.abs(t1.getRow() - t2.getRow());
        int colDelta = Math.abs(t1.getCol() - t2.getCol());
        
        // Houd rekening met het feit dat de randen van de map doorlopend kunnen zijn
        rowDelta = Math.min(rowDelta, gameState.getRows() - rowDelta);
        colDelta = Math.min(colDelta, gameState.getCols() - colDelta);
        
        // Return als geschatte afstand de som van het aantal rijen en kolommen
        return rowDelta + colDelta;
	}
	
	/**
     * Functie die gegeven een locatie de buurlocaties geeft
     * @return lijst van buurlocaties
     */
	private static ArrayList<Tile> getNeighbors(Ants gameState, Tile t)
	{
		// Lijst met alle buurlocaties
		ArrayList<Tile> result = new ArrayList<Tile>();
		
		// Voeg buurlocaties toe aan de lijst als ze toegankelijk zijn
		if (gameState.getIlk(t, Aim.WEST).isPassable())
			result.add(gameState.getTile(t, Aim.WEST));
		if (gameState.getIlk(t, Aim.NORTH).isPassable())
			result.add(gameState.getTile(t, Aim.NORTH));
		if (gameState.getIlk(t, Aim.EAST).isPassable())
			result.add(gameState.getTile(t, Aim.EAST));
		if (gameState.getIlk(t, Aim.SOUTH).isPassable())
			result.add(gameState.getTile(t, Aim.SOUTH));
		
		// Return de lijst van buurlocaties
		return result;
	}
}
