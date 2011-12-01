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
 * Starter bot implementation.
 */
public class MyBot extends Bot 
{
	private Set<Tile> availableAnts = new HashSet<Tile>();
	private HashMap<Tile,Tile> reservedTiles = new HashMap<Tile,Tile>();
	private LinkedList<Route> activeRoutes = new LinkedList<Route>();
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
        
        // Update de te verkennen map
        updateMap(gameState);
        
        /** - Geef alle mieren orders */
        
        //System.out.println("ants "+gameState.getMyAnts());
        
        // Voer active routes uit, en haal routes die zijn afgelopen weg
        updateRoutes(gameState);
        
        // Zoek naar eten
	    searchAndOrder(gameState, gameState.getFoodTiles(), 2);  
	    
	    // Verken de map
	    searchAndOrder(gameState, unseenTiles, 2); 
	    
	    // Verdedig eigen mierenhopen
	    searchAndOrder(gameState, gameState.getMyHills(), 2);
	    
	    // Val vijandelijke mieren aan
	    searchAndOrder(gameState, gameState.getEnemyAnts(), 2);          
	    
	    // Val vijandelijke mierenhopen aan
	    searchAndOrder(gameState, gameState.getEnemyHills(), 2);
    }
    
	private void updateRoutes(Ants gameState) 
	{
		// Lijst van routes die klaar zijn
        LinkedList<Route> finishedRoutes = new LinkedList<Route>();
        
        // Voer alle actieve routes uit
        for (Route route : activeRoutes)
        {
        	if (!availableAnts.contains(route.getCurrentLocation()))
        	{
        		// Haal route uit actieve lijst als mier dood is
        		finishedRoutes.add(route);
        		continue;
        	}
        	
        	// Mier is bezig met route
        	availableAnts.remove(route.getCurrentLocation());
        	
        	//System.out.println("executing route "+route.toString());
        	
        	// Probeer een zet
        	route.doStep(gameState,this);
        	
        	if (route.finished)
        	{
        		// Route is afgelopen
        		finishedRoutes.add(route);
        		
        		// Route is al klaar, mier is weer beschikbaar
        		availableAnts.add(route.getCurrentLocation());
        	}
        	else
        		reservedTiles.put(route.getEnd(),route.getCurrentLocation());      	
        }
        
        // Haal de routes uit de actieve lijst als ze klaar zijn
        for (Route route : finishedRoutes)
        {
        	activeRoutes.remove(route);
        }
	}
	
	private void updateMap(Ants gameState)
    {
    	// Als we in de eerste ronde zitten, voeg dan alle locaties toe aan de te verkennen map
        if (unseenTiles == null) 
		{
            unseenTiles = new HashSet<Tile>();
			
            for (int row = 0; row < gameState.getRows(); row++)
                for (int col = 0; col < gameState.getCols(); col++)
                    unseenTiles.add(new Tile(row, col));
        }
        
        Iterator<Tile> locIter = unseenTiles.iterator();
		
        // Verwijder alle zichtbare locaties van de te verkennen map
        while (locIter.hasNext()) 
		{
            Tile next = locIter.next();
            
			if (gameState.isVisible(next))
                locIter.remove();
        }
    }
    
    /**
     * Functie die gegeven een lijst van Targets en van Ants routes zoekt en orders uitdeelt
     * @param gameState
     * @param availableAnts
     * @param orderedAnts
     * @param targets
     */
    private void searchAndOrder(Ants gameState, Set<Tile> targets, int maxOrders) 
    {
    	//System.out.println("ants "+availableAnts.size());
    	
    	// Targets die al als doel worden gebruikt
    	HashMap<Tile,Tile> targetTiles = new HashMap<Tile,Tile>();
    	
    	 // Lijst van gevonden routes
        List<Route> routes = new ArrayList<Route>();
        
        // Lijst van alle targets
        TreeSet<Tile> sortedTargets = new TreeSet<Tile>(targets);
        
        /** - Zoek routes - */ 
        
        // Vind alle routes tussen mieren en targets
        for (Tile target : sortedTargets) 
        {
        	if(!reservedTiles.containsKey(target))
        	{
                for (Tile ant : availableAnts) 
                {
                	// Vind de kortste route tussen mieren en target
                	Route route = RouteFinder.getShortestRoute(ant, target, gameState);
                	
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
        
        // Deel aan de hand van de gevonden routes orders uit
        for (Route route : routes) 
        {
        	if(orders >= maxOrders)
            	break;
        	
            if (   !targetTiles.containsKey(route.getEnd())         // Als target nog niet getarget is
                && !targetTiles.containsValue(route.getCurrentLocation()))    // Als de mier nog geen doel heeft
            {

            	// Voeg de route aan de lijst toe
            	activeRoutes.add(route);
            	
            	// Maak de connectie tussen mier en target
            	targetTiles.put(route.getEnd(), route.getCurrentLocation());
            	
            	//System.out.println("creating route "+route.toString());
            	
            	// Probeer de route uit te voeren
            	//route.doStep(gameState,this);
                
            	// Beschikbare mieren updaten
            	availableAnts.remove(route.getCurrentLocation());
            	
            	// Deze mier heeft nu een order
            	orders ++;
            }   
        } 		
	}
	
	/**
     * Functie die gegeven een Ant en een Location een zet probeert te doen
     * @param gameState
     * @param myAnt
     * @param location
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
     * @param gameState
     * @param myAnt
     * @param direction 
     */
	private boolean doMoveDirection(Ants gameState, Tile myAnt, Aim direction) 
	{
		if (gameState.getIlk(myAnt, direction).isPassable() && 
			!reservedTiles.containsKey(gameState.getTile(myAnt, direction))) 
        {
        	// Geef de order door aan het systeem
        	gameState.issueOrder(myAnt, direction);
        	
        	// Reserveer de nieuwe positie
        	reservedTiles.put(gameState.getTile(myAnt, direction), myAnt);
        	
        	// Maak vorige positie vrij
        	reservedTiles.remove(myAnt);
        	
        	return true;
        }
		
		return false;
	}
	
	/**
     * Main method executed by the game engine for starting the bot.
     * @param args command line arguments
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws IOException 
    {
        new MyBot().readSystemInput();
    }
}
