import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Bot implementation.
 */
public class MyBot extends Bot 
{
	private GameState gameState = new GameState();
	private List<Route> activeRoutes = new ArrayList<Route>();
	private Set<Tile> availableAnts;
	private boolean forceStrategy;
	private Strategy myStrategy = Strategy.DEFAULT;
	private Strategy enemyStrategy = Strategy.DEFAULT;
	private float defensiveThreshold = 1;
	private float offensiveThreshold = 1;
    
    /**
     * Functie die iedere ronde wordt uitgevoerd
     */
    @Override
    public void doTurn()
    {
    	Strategy strategy = myStrategy;
    	
    	/** - Update data - */
    	
        // Update game state
    	availableAnts = gameState.update(getAnts());
    	
    	// Update de vijandige strategie
    	updateEnemyStrategy();
    	
    	// Update de eigen strategie
    	updateMyStrategy();
    	
        /** - Geef alle mieren orders - */
    	
		// Laat steeds een aantal mieren de eigen mierhoop verdedigen
		updateHillDefenders();
    	
    	if (forceStrategy)
    	{
    		// Verwijder alle huidige routes als een nieuwe strategie geforceerd wordt
    		activeRoutes.clear();
    	}
    	else
    	{
    		// Voer actieve routes uit en verwijder inactieve routes
    		updateRoutes(activeRoutes);
    	}
    	
    	// Voer strategie uit
    	for (Action action : myStrategy.getActions())
    		searchAndOrder(action.getTarget(), (int)(availableAnts.size() * action.getAntRate()));
    	
	    // Deblokkeer eigen heuvels
	    unblockHills();
	    
	    /** - System outs - */
	    
	    // Vijandige strategie detectie
    	if (myStrategy != strategy)
    		System.out.println("Enemy Strategy: " + enemyStrategy.name());
    }
    
    /**
     * Functie die vanuit huidige situatie de eigen strategie bepaalt
     */
    private void updateMyStrategy()
    {
    	forceStrategy = false;
    	
    	if (enemyStrategy == Strategy.DEFENSIVE || enemyStrategy == Strategy.DEFAULT)
		{
    		if (myStrategy == Strategy.DEFENSIVE || myStrategy == Strategy.ALL_DEFENSIVE)
        	{
        		// Forceer nieuwe strategie als we momenteel aan het verdedigen zijn
        		forceStrategy = true;
        	}
    		
			// Als leger niet meer uitgebreid kan worden, laat dan alle mieren aanvallen
			if (gameState.getFoodTiles().size() == 0 && gameState.getUnseenTiles().size() == 0)
			{
				// Forceer nieuwe strategie als we nog niet allemaal aan het aanvallen waren
				if (myStrategy != Strategy.ALL_OFFENSIVE)
					forceStrategy = true;
				
				myStrategy = Strategy.ALL_OFFENSIVE;
			}
		}
    	
    	if (enemyStrategy == Strategy.OFFENSIVE)
		{
    		// Als leger niet meer uitgebreid kan worden, laat dan alle mieren verdedigen
			if (gameState.getFoodTiles().size() == 0 && gameState.getUnseenTiles().size() == 0)
			{
				// Forceer nieuwe strategie als we nog niet allemaal aan het verdedigen waren
				if (myStrategy != Strategy.ALL_DEFENSIVE)
					forceStrategy = true;
				
				myStrategy = Strategy.ALL_DEFENSIVE;
			}
			// Als leger wel uitgebreid kan worden, laat dan een deel van de mieren verdedigen
			else
			{
				// Forceer nieuwe strategie als we nog niet aan het verdedigen waren
				if (myStrategy != Strategy.DEFENSIVE)
					forceStrategy = true;
				
				myStrategy = Strategy.DEFENSIVE;
			}
		}
    }
    
    /**
     * Functie die vanuit huidige situatie de vijandige strategie bepaalt
     */
    private void updateEnemyStrategy()
    {
    	enemyStrategy = Strategy.DEFAULT;
    	
    	// Vraag eigen en vijandige hill op
		Tile myHill = gameState.getMyHill();
		Tile enemyHill = gameState.getEnemyHill();
		
		// Bereken gevaar bij eigen hill en vijandige hill
		int infMyHill = gameState.getInfluenceValue(myHill);
		int infEnemyHill = gameState.getInfluenceValue(enemyHill);
		
    	if (infEnemyHill >= 20 * defensiveThreshold		// Veel gevaar bij vijandige mierhoop
    		&& infMyHill <  5  * defensiveThreshold)	// Weinig gevaar bij eigen mierhoop
		{
    		// Verhoog de grens dat vijand offensive speelt
    		offensiveThreshold *= 0.5;
    		
			// Zet vijandige strategie op defensive
			enemyStrategy = Strategy.DEFENSIVE;
		}
    	
    	if (infMyHill >= 5 * offensiveThreshold)		// Gevaar bij eigen mierhoop	
		{
    		// Verhoog de grens dat vijand defensive speelt
    		defensiveThreshold *= 0.5;
    		
    		// Zet vijandige strategie op offensive
			enemyStrategy = Strategy.OFFENSIVE;
		}
    }
    
    /**
     * Functie die de hill defender update
     */
    private void updateHillDefenders()
    {
    	if (gameState.getHillDefenders().size() < 3 && availableAnts.size() > 3)
    	{
	    	// Vraag de eigen hill op
	    	Tile myHill = gameState.getMyHill();
	    	
	    	// Bepaal nieuwe hill defender als er een mier beschikbaar is
	    	if (myHill != null && availableAnts.contains(myHill))
	    	{
	    		// Zet nieuwe hill defender naast een van de kanten van de hill
	    		for (Aim direction : Aim.values())
	    		{
	    			Tile defendLoc = gameState.getTile(myHill, direction);
	    			
	    			if (!gameState.isWater(defendLoc) && !gameState.isReserved(defendLoc))
	    			{
	    				doMoveDirection(myHill, direction);
	    				gameState.setHillDefender(defendLoc);
	    				availableAnts.remove(myHill);
	    				break;
	    			}
	    		}
	    	}
    	}
    }
    
	/**
     * Functie die alle actieve routes update 
     */
	private void updateRoutes(List<Route> routes) 
	{
        for (Iterator<Route> i = routes.iterator(); i.hasNext();) 
        {
        	Route route = i.next();
        	
        	if (!availableAnts.contains(route.getCurrentLocation()))
        	{
        		// Mier is dood
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
	        		
	        		// Mier is weer beschikbaar
	        		availableAnts.add(route.getCurrentLocation());
	        	}
        	}
        }
	}
    
    /**
     * Functie die gegeven een type target routes zoekt en orders uitdeelt aan mieren
     */
    private void searchAndOrder(Target target, int maxOrders) 
    {
    	// Lijst van target locaties
    	Set<Tile> targetLocs = new HashSet<Tile>();
    	
    	// Bepaal target locaties aan de hand van meegegeven target
    	switch (target)
		{
			case FOOD: targetLocs = gameState.getFoodTiles(); break;
			case LAND: targetLocs = gameState.getUnseenTiles(maxOrders); break; 
			case ENEMY_HILL: targetLocs = gameState.getEnemyHills(); break;
			case MY_HILL: targetLocs = gameState.getMyHills(); break;
		}
    	
    	// Lijst van gevonden routes
        List<Route> routes = RouteFinder.findRoutes(gameState, target, availableAnts, targetLocs); 
        
        // Sets van targets die al als doel worden gebruikt en mieren die al een order hebben
    	Set<Tile> targetedTiles = new HashSet<Tile>();
    	Set<Tile> orderedAnts = new HashSet<Tile>();
        
        // Lijst van nieuwe routes/orders
        List<Route> newRoutes = new ArrayList<Route>();
        
        // Deel aan de hand van de gevonden routes orders uit
        for (Route route : routes) 
        {
        	// Stop met orders uitdelen als limiet is bereikt
        	if (orderedAnts.size() >= maxOrders)
        		break;
        	
            if (!targetedTiles.contains(route.getTargetLocation())	   // Als target nog niet getarget is
                && !orderedAnts.contains(route.getCurrentLocation()))  // Als de mier nog geen doel heeft
            {
            	// Voeg de route aan de lijsten toe
            	activeRoutes.add(route);
            	newRoutes.add(route);
            	
            	// Als target food is, willen we er maar 1 mier op afsturen
            	if (target == Target.FOOD)
            		targetedTiles.add(route.getTargetLocation());
            	
            	// Deze mier heeft nu een order
            	orderedAnts.add(route.getCurrentLocation());
            }   
        }
        
        // Voer de nieuwe orders uit
        updateRoutes(newRoutes);
	}
    
    /**
     * Functie die ervoor zorgt dat de mieren de eigen mierenhopen niet blokkeren
     */
    private void unblockHills() 
    {
    	// Lijst van nieuwe routes/orders
    	List<Route> newRoutes = new ArrayList<Route>();
    	
    	for (Tile myHill : gameState.getMyHills()) 
        {
    		if (availableAnts.contains(myHill))
            {
    			// Vraag een random locatie op
    			Tile randomTile = gameState.getRandomTile();
    			
                // Vind de kortste route tussen mier en de random locatie
            	Route route = RouteFinder.findShortestRoute(gameState, Target.LAND, myHill, randomTile);
            	
            	if (route == null)
            	{
            		// Stuur de mier in een richting
            		for (Aim direction : Aim.values())
            		{
            			if (doMoveDirection(myHill, direction));
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
    	
    	// Voer de nieuwe orders uit
		updateRoutes(newRoutes);
	}
	
	/**
	 * Functie die gegeven een Ant en een Location een zet probeert te doen
	 * @return <code>true</code> als zet gelukt is, <code>false</code> als zet niet gelukt is
	 */
	public boolean doMoveLocation(Tile myAnt, Tile targetLocation) 
    {
    	// Haal de mogelijk directies op naar het doel
    	List<Aim> directions = gameState.getDirections(myAnt, targetLocation);
    	
    	// Check voor elke richting of ze erheen kunnen, zoja, ga er dan heen
        for (Aim direction : directions) 
        {
            if (doMoveDirection(myAnt, direction))
            	return true;
        }
    	
		return false;
	}
	
	/**
     * Functie die gegeven een mier en een richting een zet probeert te doen
     * @return <code>true</code> als zet gelukt is, <code>false</code> als zet niet gelukt is
     */
	public boolean doMoveDirection(Tile myAnt, Aim direction) 
	{
		// Bepaal de nieuwe locatie
		Tile newLoc = gameState.getTile(myAnt, direction);
		
		if (!gameState.isWater(newLoc) && !gameState.isReserved(newLoc)) 
        {
        	// Geef de order door aan het systeem
			gameState.issueOrder(myAnt, direction);
        	
        	// Reserveer de nieuwe positie van de mier
        	gameState.setReserved(newLoc, true);
        	
        	// Maak de vorige positie van de mier vrij
        	gameState.setReserved(myAnt, false);
        	
        	return true;
        }
		else if (!gameState.isWater(newLoc) && availableAnts.contains(newLoc))
		{
			// Geef de order door aan het systeem
			gameState.issueOrder(myAnt, direction);
        	
        	// Geef de order door aan het systeem
			gameState.issueOrder(newLoc, direction.getOpposite());
        	
        	// Mier is niet meer beschikbaar
        	availableAnts.remove(newLoc);	   
        	
        	return true;
		}
		
		return false;
	}
	
    public static void main(String[] args) throws IOException 
    {
        new MyBot().readSystemInput();
    }
}
