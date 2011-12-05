import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bot implementation.
 */
public class MyBot extends Bot 
{
	private List<Route> activeRoutes = new ArrayList<Route>();
	private Set<Tile> availableAnts = new HashSet<Tile>();
	private Set<Tile> reservedTiles = new HashSet<Tile>();
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
            reservedTiles.add(myHill);
        }
        
        // Reserveer eigen mierlocaties als niet-toegangbare locaties
        for (Tile myAnt : gameState.getMyAnts()) 
        {
        	availableAnts.add(myAnt);
            reservedTiles.add(myAnt);
        }
        
        // Update de te verkennen map en de objecten op de map
        updateMap(gameState);
        
        /** - Geef alle mieren orders - */
        
        // Voer active routes uit, en haal routes die zijn afgelopen weg
        updateRoutes(gameState, activeRoutes);
        
        // Zoek naar eten
        searchAndOrder(gameState, Target.FOOD, gameState.getFoodTiles(), 0);          
	    
	    // Val vijandelijke mierenhopen aan
	    searchAndOrder(gameState, Target.ENEMY_HILL, enemyHills, 0);
	    
	    // Kies een paar vakjes om te verkennen
	    Set<Tile> unseenTilesSelection = pickTiles(unseenTiles, 5);
	    
	    // Verken de map
	    searchAndOrder(gameState, Target.LAND, unseenTilesSelection, 5);
	    
	    // Deblokkeer eigen heuvels
	    unblockHills(gameState);
    }
    
    private void unblockHills(Ants gameState) 
    {
    	List<Route> newRoutes = new ArrayList<Route>();
    	
    	 for (Tile myHill : gameState.getMyHills()) 
         {
             if(gameState.getIlk(myHill) == Ilk.MY_ANT)
             {
            	 Tile randomTile = new Tile((int)((float)gameState.getCols()*Math.random()),(int)((float)gameState.getRows()*Math.random()));
                	 
                // Vind de kortste route tussen mieren en target
            	Route route = RouteFinder.getShortestRoute(gameState, Target.LAND, myHill, randomTile);

            	if(route == null)
            	{
            		for(Aim direction : Aim.values())
            		{
            			if(doMoveDirection(gameState,myHill,direction));
            				break;
            		}
            		
            	}
            	else
            	{
            		// Voeg toe aan de lijsten
                 	activeRoutes.add(route);
                 	newRoutes.add(route);
            	}
             	
             }
         }

		updateRoutes(gameState,newRoutes);
	}

	/**
     * Functie die een aantal random Tiles pakt uit de meegegeven lijst van Tiles
     * @return lijst met <code>i</code> aantal random geselecteerde Tiles
     */
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
     */
	private void updateRoutes(Ants gameState, List<Route> routes) 
	{
        // Voer alle meegegeven routes uit
        for (Iterator<Route> i = routes.iterator(); i.hasNext();) 
        {
        	Route route = i.next();
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
        		i.remove();
        	}
        	else
        	{
	        	// Mier is bezig met route
	        	availableAnts.remove(route.getCurrentLocation());
	        	
	        	// Probeer een zet
	        	route.doStep(gameState, this);
	        	
	        	if (route.isFinished())
	        	{
	        		// Route is afgelopen
	        		i.remove();
	        		
	        		// Route is al klaar, mier is weer beschikbaar
	        		availableAnts.add(route.getCurrentLocation());
	        	}
	        	else
	        	{
	        		// Reserveer de positie van de target
	        		//reservedTiles.add(route.getTargetLocation());      	
	        	}
        	}
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
		
        // Verwijder alle zichtbare en water locaties van de te verkennen map
        for (Iterator<Tile> i = unseenTiles.iterator(); i.hasNext();) 
		{
            Tile next = i.next();
            
			if (gameState.isVisible(next) || !gameState.getIlk(next).isPassable())
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
    	Map<Tile,Tile> targetTiles = new HashMap<Tile,Tile>();
    	
    	 // Lijst van gevonden routes
        List<Route> routes = new ArrayList<Route>();
        
        /** - Zoek routes - */ 
        
        // Limit aan het aantal routes dat er gemaakt mag worden
        int limit = 10;
        
        // Vind alle routes tussen mieren en targets
        for (Tile targetLoc : targetLocs) 
        {
        	if(limit < 1)
        		break;
        	
        	if (!reservedTiles.contains(targetLoc))
        	{
                for (Tile ant : availableAnts)
                {
                	if(limit < 1)
                		break;
                	
                	// Verlaag de limit
                	limit--;
                	
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
        List<Route> newOrders = new ArrayList<Route>();
        
        // Deel aan de hand van de gevonden routes orders uit
        for (Route route : routes) 
        {
        	if(maxOrders != 0 && orders >= maxOrders)
            	break;
        	
            if (   !targetTiles.containsKey(route.getTargetLocation())      // Als target nog niet getarget is
                && !targetTiles.containsValue(route.getCurrentLocation()))	// Als de mier nog geen doel heeft
            {
            	// Voeg de route aan de lijsten toe
            	activeRoutes.add(route);
            	newOrders.add(route);
            	
            	// Maak de connectie tussen mier en target
            	// Als de target eten is, willen er er niet meer dan 1 mier op afsturen
            	if(target == Target.FOOD)
            	{
            		targetTiles.put(route.getTargetLocation(), route.getCurrentLocation());
            	}
            	else
            	{
            		targetTiles.put(null, route.getCurrentLocation());
            	}
            		
            	
            	// Deze mier heeft nu een order
            	orders++;
            }   
        } 
        
        // Voer de nieuwe orders uit
        updateRoutes(gameState, newOrders);
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
	public boolean doMoveDirection(Ants gameState, Tile myAnt, Aim direction) 
	{
		if (gameState.getIlk(myAnt, direction).isPassable() && 
			!reservedTiles.contains(gameState.getTile(myAnt, direction))) 
        {
        	// Geef de order door aan het systeem
        	gameState.issueOrder(myAnt, direction);
        	
        	// Reserveer de nieuwe positie van de mier
        	reservedTiles.add(gameState.getTile(myAnt, direction));
        	
        	// Maak de vorige positie van de mier vrij
        	reservedTiles.remove(myAnt);
        	
        	return true;
        }
		else if(gameState.getIlk(myAnt, direction).isPassable())
		{
			// Switch de 2 mieren om
			if(availableAnts.contains(gameState.getTile(myAnt, direction)))
			{
				// Geef de order door aan het systeem
	        	gameState.issueOrder(myAnt, direction);
	        	
	        	// Geef de order door aan het systeem
	        	gameState.issueOrder(gameState.getTile(myAnt, direction), switchDirection(direction));
	        	
	        	// Mier is niet meer beschikbaar
	        	availableAnts.remove(gameState.getTile(myAnt, direction));	   
	        	
	        	return true;
			}
		}
		
		return false;
	}
	
	private Aim switchDirection(Aim direction) 
	{
		if(direction == Aim.NORTH)
			return Aim.SOUTH;
		else if(direction == Aim.SOUTH)
			return Aim.NORTH;
		else if(direction == Aim.EAST)
			return Aim.WEST;
		else
			return Aim.EAST;
	}

	/**
     * Main methode die uitgevoerd wordt door de engine om de bot te starten
     */
    public static void main(String[] args) throws IOException 
    {
        new MyBot().readSystemInput();
    }
}
