import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Bot implementation.
 */
public class MyBot extends Bot 
{
	private Set<Tile> availableAnts = new HashSet<Tile>();
	private HashMap<Tile,Tile> reservedTiles = new HashMap<Tile,Tile>();
	private LinkedList<Route> activeRoutes = new LinkedList<Route>();
	private Set<Tile> enemyHills = new HashSet<Tile>();
	private Set<Tile> unseenTiles;
	
    /**
     * Functie die iedere ronde wordt uitgevoerd
     */
    @Override
    public void doTurn()
    {
    	/** - Initialisatie - */
    	
    	// Haal de gamestatus op
        Ants gameState = getAnts();	
        
        // Wis lijst van beschikbare mieren
        availableAnts.clear();
        
        // Wis lijst van gereserveerde locaties
        reservedTiles.clear();
        
        // Reserveer de eigen mierenhopen als niet-toegangbare locaties
        for (Tile myHill : gameState.getMyHills()) 
        {
            reservedTiles.put(myHill, null);
        }
        
        // Reserveer eigen mierlocaties als niet-toegangbare locaties
        for (Tile myAnt : gameState.getMyAnts()) 
        {
        	availableAnts.add(myAnt);
            reservedTiles.put(myAnt, null);
        }
        
        // Update de te verkennen map en de objecten op de map
        updateMap(gameState);
        
        /** - Geef alle mieren orders */
        
        // Voer active routes uit, en haal routes die zijn afgelopen weg
        updateRoutes(activeRoutes,gameState);
        
        // Zoek naar eten
        searchAndOrder(gameState, Target.FOOD, gameState.getFoodTiles(), 5);          
	    
	    // Val vijandelijke mierenhopen aan
	    searchAndOrder(gameState, Target.ENEMY_HILL, enemyHills, 10);
	    
	    // Kies een paar vakjes om te verkennen
	    Set<Tile> unseenTilesSelection = pickTiles(unseenTiles,20);
	    
	    // Verken de map
	    searchAndOrder(gameState, Target.LAND, unseenTilesSelection, 20);

    }
    
    private Set<Tile> pickTiles(Set<Tile> tiles, int i) 
    {
    	if(tiles.isEmpty())
    		return tiles;
    	
    	// Gekozen vakjes
    	Set<Tile> pickedTiles = new HashSet<Tile>();
    	
    	// Beschikbare vakjes
		Tile[] tilesArray = tiles.toArray(new Tile[tiles.size()]);
		
		// Interval voor het kiezen van vakjes
		int interval = (int)((float)tilesArray.length/(float)i);
		
		// Kies met een interval steeds een vakje
		for(int x = 0; x < i; x++)
		{
			pickedTiles.add(tilesArray[x*interval]);
		}
		
		return pickedTiles;
	}

	/**
     * Functie die alle actieve routes update
     * @param routes 
     */
	private void updateRoutes(LinkedList<Route> routes, Ants gameState) 
	{
		// Lijst van routes die inactief zijn
        LinkedList<Route> inactiveRoutes = new LinkedList<Route>();
        
        // Voer alle actieve routes uit
        for (Route route : routes)
        {
        	boolean inactive = false;
        	
        	if (!availableAnts.contains(route.getCurrentLocation()))
        	{
        		// Maak route inactief als mier dood is
        		inactive = true;
        	}
        	else
        	{
        		Tile targetLoc = route.getTargetLocation();
        		
        		// Maak route inactief als target niet meer bestaat
	        	switch (route.getTarget()) 
	        	{
		            case FOOD: inactive = !gameState.getFoodTiles().contains(targetLoc); break;
		            case ENEMY_HILL: inactive = !enemyHills.contains(targetLoc); break;
		        }
        	}
        	
        	if (inactive)
        	{
        		// Route is inactief geworden
        		inactiveRoutes.add(route);
        	}
        	else
        	{
	        	// Mier is bezig met route
	        	availableAnts.remove(route.getCurrentLocation());
	        	
	        	//System.out.println("executing route "+route.toString());
	        	
	        	// Probeer een zet
	        	route.doStep(gameState, this);
	        	
	        	if (route.finished)
	        	{
	        		// Route is afgelopen
	        		inactiveRoutes.add(route);
	        		
	        		// Route is al klaar, mier is weer beschikbaar
	        		availableAnts.add(route.getCurrentLocation());
	        	}
	        	else
	        	{
	        		// Reserveer de positie van de target
	        		reservedTiles.put(route.getTargetLocation(), route.getCurrentLocation());      	
	        	}
        	}
        }
        
        // Haal de routes uit de actieve lijst als ze inactief zijn
        for (Route route : inactiveRoutes)
        {
        	routes.remove(route);
        }
	}
	
	/**
     * Functie die map locaties en map objecten update
     */
	private void updateMap(Ants gameState)
    {
		// Als dit de eerste ronde is, voeg dan alle locaties toe als te verkennen locaties
        if (unseenTiles == null) 
		{
            unseenTiles = new HashSet<Tile>();
			
            for (int row = 0; row < gameState.getRows(); row++)
                for (int col = 0; col < gameState.getCols(); col++)
                    unseenTiles.add(new Tile(row, col));
        }
		
        // Verwijder alle zichtbare locaties van de te verkennen map
        for (Iterator<Tile> i = unseenTiles.iterator(); i.hasNext();) 
		{
            Tile next = i.next();
            
			if (gameState.isVisible(next))
                i.remove();
        }
        
        // Voeg nieuw ontdekte vijandige mierenhopen toe
        for (Tile hill : gameState.getEnemyHills())
        {
        	if (!enemyHills.contains(hill))
        		enemyHills.add(hill);
        }		
    }
    
    /**
     * Functie die gegeven een lijst van Targets routes zoekt en orders uitdeelt aan Ants
     */
    private void searchAndOrder(Ants gameState, Target target, Set<Tile> targetLocs, int maxOrders) 
    {
    	// Targets die al als doel worden gebruikt
    	HashMap<Tile,Tile> targetTiles = new HashMap<Tile,Tile>();
    	
    	 // Lijst van gevonden routes
        List<Route> routes = new ArrayList<Route>();
        
        // Lijst van alle targets
        TreeSet<Tile> sortedTargets = new TreeSet<Tile>(targetLocs);
        
        /** - Zoek routes - */ 
        
        // Vind alle routes tussen mieren en targets
        for (Tile targetLoc : sortedTargets) 
        {
        	if(!reservedTiles.containsKey(targetLoc))
        	{
                for (Tile ant : availableAnts)
                {
                	// Vind de kortste route tussen mieren en target
                	Route route = RouteFinder.getShortestRoute(gameState, target, ant, targetLoc);
                	
                	// Als er een route gevonden is, voeg deze dan toe aan lijst van routes
                	if (route != null)
                		routes.add(route);
                }
        	}
        }
        
        // Sorteer de gevonden routes van kort naar lang
        Collections.sort(routes);
        
        /** - Deel orders uit - */  
        
        int orders = 0;
        
        // Nieuw gevonden routes/orders
        LinkedList<Route> newOrders = new LinkedList<Route>();
        
        // Deel aan de hand van de gevonden routes orders uit
        for (Route route : routes) 
        {
        	if(orders >= maxOrders)
            	break;
        	
            if (   !targetTiles.containsKey(route.getTargetLocation())      // Als target nog niet getarget is
                && !targetTiles.containsValue(route.getCurrentLocation()))	// Als de mier nog geen doel heeft
            {
            	
            	// Voeg de route aan de lijsten toe
            	activeRoutes.add(route);
            	newOrders.add(route);

            	// Maak de connectie tussen mier en target
            	targetTiles.put(route.getTargetLocation(), route.getCurrentLocation());
    
            	// Deze mier heeft nu een order
            	orders ++;
            }   
        } 
        
        // Voer de nieuwe orders uit
        updateRoutes(newOrders, gameState);
	}
	
	/**
	 * Functie die gegeven een Ant en een Location een zet probeert te doen
	 * @return <code>true</code> als zet gelukt is, <code>false</code> als zet niet gelukt is
	 */
	public boolean doMoveLocation(Ants gameState, Tile myAnt, Tile targetLocation) 
    {
    	// Haal de mogelijk directies op naar het doel
    	List<Aim> directions = gameState.getDirections(myAnt, targetLocation);
    	
    	// Check voor elke richting of ze erheen kunnen, zoja, ga er dan heen
        for (Aim direction : directions) 
        {
        	// Probeer de zet
            if(doMoveDirection(gameState,myAnt,direction))
            {
            	return true;
            }
        }
    	
		return false;
	}
	
	/**
     * Functie die gegeven een Ant en een Direction een zet probeert te doen
     * @return <code>true</code> als zet gelukt is, <code>false</code> als zet niet gelukt is
     */
	private boolean doMoveDirection(Ants gameState, Tile myAnt, Aim direction) 
	{
		if (gameState.getIlk(myAnt, direction).isPassable() && 
			!reservedTiles.containsKey(gameState.getTile(myAnt, direction))) 
        {
        	// Geef de order door aan het systeem
        	gameState.issueOrder(myAnt, direction);
        	
        	// Reserveer de nieuwe positie van de mier
        	reservedTiles.put(gameState.getTile(myAnt, direction), myAnt);
        	
        	// Maak de vorige positie van de mier vrij
        	reservedTiles.remove(myAnt);
        	
        	return true;
        }
		
		return false;
	}
	
	/**
     * Main methode die uitgevoerd wordt door de engine om de bot te starten
     */
    public static void main(String[] args) throws IOException 
    {
        new MyBot().readSystemInput();
    }
}
