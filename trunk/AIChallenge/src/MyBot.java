import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Starter bot implementation.
 */
public class MyBot extends Bot 
{
	// Gereserveerde Locaties
	private HashMap<Tile,Tile> reservedTiles = new HashMap<Tile,Tile>();
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
        
        // Update de te verkennen map
        updateMap(gameState);
        
        // Wis alle orders/gereserveerde locaties
        reservedTiles.clear();
        
        // Reserveer de eigen mierenhopen als niet-toegangbare locaties
        for (Tile myHill : gameState.getMyHills()) 
        {
            reservedTiles.put(myHill, null);
        }
        
        /** - Geef alle mieren orders */
        
        // Maak teams
        Set<Team> teams = createTeams(gameState.getMyAnts(),1,gameState);
        
        // Zoek naar eten
	    searchAndOrder(gameState, gameState.getFoodTiles(), teams,2);  
	    
	    // Verken de map
	    //searchAndOrder(gameState, unseenTiles, teams,2); 
	    
	    // Verdedig eigen mierenhopen
	    //searchAndOrder(gameState, gameState.getMyHills(), teams,2);
	    
	    // Val vijandelijke mieren aan
	    //searchAndOrder(gameState, gameState.getEnemyAnts(), teams,2);          
	    
	    // Val vijandelijke mierenhopen aan
	    //searchAndOrder(gameState, gameState.getEnemyHills(), teams,2);
    }
    
    private Set<Team> createTeams(Set<Tile> myAnts, int teamSize, Ants gameState) 
    {
		Set<Team> teams = new HashSet<Team>();
		
		int counter = 1;
		Team currentTeam = new Team();

		Set<Tile> teamedAnts = new HashSet<Tile>();
		
		for(Tile ant : myAnts)
		{
			if(!teamedAnts.contains(ant))
			{
				currentTeam = new Team();
			}		
			
			for(Tile ant2 : myAnts)
			{
				if(teamedAnts.contains(ant))
					break;
				
				if(counter == teamSize)
				{
					currentTeam.addMember(ant);
					teamedAnts.add(ant);
					teams.add(currentTeam);
					break;
				}
				
				if(ant != ant2 && (gameState.getDistance(ant, ant2)) < 4)
				{
					currentTeam.addMember(ant2);
					teamedAnts.add(ant2);
					counter++;
				}
			}
			
			if(!teamedAnts.contains(ant))
			{
				currentTeam.addMember(ant);
				teamedAnts.add(ant);
				teams.add(currentTeam);
			}		
		}
		
		return teams;
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
    private void searchAndOrder(Ants gameState, Set<Tile> targets, Set<Team> teams, int maxOrders) 
    {
    	// Targets die al als doel worden gebruikt
    	HashMap<Tile,Team> targetTiles = new HashMap<Tile,Team>();
    	
    	 // Lijst van gevonden routes
        List<Route> routes = new ArrayList<Route>();
        
        // Lijst van alle targets
        TreeSet<Tile> sortedTargets = new TreeSet<Tile>(targets);
        
        /** - Zoek routes - */ 
        
        // Vind alle routes tussen mieren en targets
        for (Tile target : sortedTargets) 
        {
            for (Team team : teams) 
            {
               // int distance = gameState.getDistance(team.getTile(), target);
               // Route route = new Route(team, target, distance);
               // routes.add(route);
            	
            	// Vind de kortste route tussen mieren en target
            	Route route = RouteFinder.getShortestRoute(team, target, gameState);
            	
            	// Als er een route gevonden is, voeg deze dan toe aan lijst van routes
            	if (route != null)
            		routes.add(route);
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
                && !targetTiles.containsValue(route.getStart()))    // Als de mier nog geen doel heeft
            {
            	
            	boolean moveSucces = false;
            	
            	for(Tile ant : new TreeSet<Tile>(route.getStart().teamMembers))
            	{
            		// Als de zet mogelijk is
                    if(doMoveLocation(gameState, ant, route.getPath().getFirst()))
                    {
                    	// Maak de connectie tussen de mier en de target
                    	targetTiles.put(route.getEnd(), route.getStart());
                    	
                    	// Deze mier heeft nu een order
                    	orders++;
                    	
                    	// Er is een zet gelukt
                    	moveSucces = true;
                    }       
            	}   
            	
            	if(moveSucces)
            	{
            		teams.remove(route.getStart());
            	}
            }   
        } 		
	}
	
	/**
     * Functie die gegeven een Ant en een Location een zet probeert te doen
     * @param gameState
     * @param myAnt
     * @param location
     */
	private boolean doMoveLocation(Ants gameState, Tile myAnt, Tile targetLocation) 
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
        	
        	// Sla de order op
        	reservedTiles.put(gameState.getTile(myAnt, direction), myAnt);
        	
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
