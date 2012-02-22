import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Represents a route navigator.
 */
public class RouteFinder {
	
	private final static int TARGET_LIMIT = 1000;
	private final static int PATH_LIMIT = 10000;
	private final static int PATH_SAFETY = 2;
	
	/**
     * Functie die gegeven start- en eindlocaties alle korste routes zoekt en sorteert op lengte van de routes
     * @return lijst van alle korste routes tussen start- en eindlocaties gesorteerd op lengte van de routes
     */
	public static List<Route> findRoutes(GameState gameState, Target target, Set<Tile> startLocs, Set<Tile> targetLocs)
	{
		// Lijst van gevonden routes
        List<Route> routes = new ArrayList<Route>();
        
        // Lijst van veilige beginlocaties
        Set<Tile> safeLocs = getSafestLocations(gameState, targetLocs);
        
        // Vind alle routes tussen start- en eindlocaties
        for (Tile targetLoc : safeLocs) 
        {
            for (Tile startLoc : startLocs)
            {
            	// Vind de kortste route tussen mieren en target
            	Route route = findShortestRoute(gameState, target, startLoc, targetLoc);
            	
            	// Als er een route gevonden is, voeg deze dan toe aan lijst van routes
            	if (route != null)
            		routes.add(route);
            }
        }
        
        // Sorteer de gevonden routes van kort naar lang
        Collections.sort(routes);
        
        return routes;
	}
	
	/**
     * Functie die gegeven een lijst van target locaties de veiligste locaties uitkiest binnen het target limiet
     * @return lijst van veiligste locaties binnen het target limiet
     */
	private static Set<Tile> getSafestLocations(GameState gameState, Set<Tile> targetLocs) 
	{
		// De map met de veiligste locaties
		Map<Tile,Integer> safeTargets = new HashMap<Tile,Integer>();
		
		// Locatie met de meeste influence
		Tile maxTarget = null;
		
		for (Tile tile : targetLocs)
		{
			int danger = gameState.getInfluenceValue(tile);
			
			// Als er nog plek is voor meer locaties
			if (safeTargets.size() < TARGET_LIMIT)
			{
				safeTargets.put(tile, danger);
				
				// Update de maxTarget
				if (maxTarget == null || safeTargets.get(maxTarget) < danger)
					maxTarget = tile;
			}
			else
			{
				// Als de verzameling vol is maar we hebben een tile met kleinere 
				// influence dan de grootste in de verzameling
				if (danger < safeTargets.get(maxTarget))
				{
					// Haal de grootse eruit en doe de kleinere erin
					safeTargets.remove(maxTarget);
					safeTargets.put(tile, danger);
					
					// Reset de maxTarget
					maxTarget = null;
					
					// Update de maxTarget
					for (Tile t : safeTargets.keySet())
					{
						if (maxTarget == null || safeTargets.get(maxTarget) < safeTargets.get(t))
							maxTarget = t;
					}
				}
			}
		}
		
		return safeTargets.keySet();
	}
	
	/**
     * Functie die A* algoritme gebruikt om kortste route te vinden tussen gegeven start- en eindlocatie 
     * @return kortste route als er een route is gevonden, <code>null</code> als er geen route is gevonden
     */
	public static Route findShortestRoute(GameState gameState, Target target, Tile startLoc, Tile targetLoc)
	{
		if (!startLoc.equals(targetLoc))
		{
			// Bepaal parent en scores voor start locatie
			startLoc.parent = null;
			startLoc.g_score = 0;
			startLoc.h_score = estimateDistance(gameState, startLoc, targetLoc);
			startLoc.f_score = startLoc.g_score + startLoc.h_score;
			
			// Initialiseer een set waar locaties in komen te staan die afgerond zijn
			Set<Tile> closedset = new HashSet<Tile>();
			
			// Initialiseer een set waarbij de locatie met kleinste f score vooraan komt te staan
			PriorityQueue<Tile> openset = new PriorityQueue<Tile>(startLoc.f_score, new Comparator<Tile>() {
				public int compare(Tile t1, Tile t2) {
					return t1.f_score - t2.f_score;
				}
			});
			
			// Voeg start locatie toe aan de queue
			openset.add(startLoc);
			
			// Voer algoritme uit zolang de queue niet leeg is en het limiet niet bereikt is
		    for (int i = 0; i <= PATH_LIMIT && !openset.isEmpty(); i++)
		    {
		    	// Haal het voorste element uit de queue
		    	Tile x = openset.remove();
		    	
		    	// Return gevonden route als eind locatie is bereikt
		    	if (x.equals(targetLoc))
		    		return reconstructRoute(target, startLoc, x);
		    	
		    	// Return gevonden route als limiet is bereikt
		    	if (i == PATH_LIMIT)
		    		return reconstructRoute(Target.LAND, startLoc, x);
				
				// Voeg het element toe aan de set van tiles die afgerond zijn
				closedset.add(x);
				
				// Loop alle buurlocaties langs
				for (Tile y : gameState.getNeighbors(x))
				{
					// Sla locatie over als hij al afgerond is
				    if (closedset.contains(y))
				    	continue;
				    
				    // Bepaal nieuwe g score waarbij we rekening houden met de influence
				    int g_score = x.g_score + 1 + (gameState.getInfluenceValue(y) * PATH_SAFETY);
				    
				    // Bepaal parent en scores voor buur en voeg hem toe aan queue als hij
				    // nog niet in de queue zit of als nieuwe g score beter is dan zijn bestaande
				    if (!openset.contains(y) || g_score < y.g_score)
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
	public static int estimateDistance(GameState gameState, Tile t1, Tile t2)
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
}
