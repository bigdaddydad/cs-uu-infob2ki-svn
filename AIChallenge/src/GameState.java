import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Represents current game state.
 */
public class GameState {

	private Ants ants;
	private Set<Tile> reservedTiles = new HashSet<Tile>();
	private Set<Tile> enemyHills = new HashSet<Tile>();
	private Set<Tile> waterTiles = new HashSet<Tile>();
	private Set<Tile> unseenTiles;
	private InfluenceMap imap;
	
	/**
     * Functie die map locaties en map objecten update
     * @return set met alle beschikbare mieren
     */
	public Set<Tile> update(Ants ants)
    {
		this.ants = ants;
		
		// Lijst van alle beschikbare mieren
		Set<Tile> availableAnts = new HashSet<Tile>();
		
		// Wis lijst van gereserveerde locaties
        reservedTiles.clear();
        
        // Voeg nieuw ontdekte vijandige mierenhopen toe
        for (Tile hill : ants.getEnemyHills())
        	enemyHills.add(hill);
        
        // Reserveer de eigen mierenhopen als niet-toegangbare locaties
        for (Tile myHill : ants.getMyHills()) 
        	reservedTiles.add(myHill);
        
        // Voeg mierlocaties toe aan lijst en reserveer ze als niet-toegangbare locaties
        for (Tile myAnt : ants.getMyAnts()) 
        {
        	availableAnts.add(myAnt);
        	reservedTiles.add(myAnt);
        }
        
        // Als dit de eerste update is, voeg dan alle locaties toe als te verkennen locaties
        if (unseenTiles == null) 
		{
            unseenTiles = new HashSet<Tile>();
            
            for (int row = 0; row < ants.getRows(); row++)
            	for (int col = 0; col < ants.getCols(); col++)
            		unseenTiles.add(new Tile(row, col));
		}
		
        // Verwijder alle zichtbare locaties van de te verkennen map en check op water
        for (Iterator<Tile> i = unseenTiles.iterator(); i.hasNext();) 
		{
            Tile t = i.next();
            
			if (ants.isVisible(t))
				i.remove();
			
			if (!ants.getIlk(t).isPassable())
				waterTiles.add(t);
        }
        
        // Update de influence map
        imap = new InfluenceMap(this, getEnemyAnts(), 10);
        
//        for(int x = 0; x < gameState.getRows(); x++)
//        {
//        	for(int y = 0; y < gameState.getCols(); y++)
//            {
//            	System.out.print(imap.map[x][y] + "");
//            }
//        	
//        	System.out.println(" ");
//        }
        
        return availableAnts;
    }
	
	/**
     * Functie die een aantal random Tiles pakt uit de meegegeven lijst van Tiles
     * @return lijst met <code>i</code> aantal random geselecteerde Tiles
     */
    private Set<Tile> pickTiles(Set<Tile> tiles, int i) 
    {
    	if (tiles.isEmpty())
    		return tiles;
    	
    	// Gekozen vakjes
    	Set<Tile> pickedTiles = new HashSet<Tile>();
    	
    	// Beschikbare vakjes
		Tile[] tilesArray = tiles.toArray(new Tile[tiles.size()]);
		
		// Interval voor het kiezen van vakjes
		int interval = (int)((float)tilesArray.length/(float)i);
		
		// Kies met een interval steeds een vakje
		for (int x = 0; x < i; x++)
			pickedTiles.add(tilesArray[x*interval]);
		
		return pickedTiles;
	}
    
    /**
     * Functie die kijkt of het type target zich bevindt op de meegegeven locatie
     * @return <code>true</code> als target bestaat, <code>false</code> in het andere geval
     */
    public boolean checkTarget(Target target, Tile targetLoc)
	{
		switch (target)
		{
			case FOOD: return ants.getFoodTiles().contains(targetLoc);
			case MY_ANT: return ants.getMyAnts().contains(targetLoc);
			case ENEMY_ANT: return ants.getEnemyAnts().contains(targetLoc);
			case MY_HILL: return ants.getMyHills().contains(targetLoc);
			case ENEMY_HILL: return enemyHills.contains(targetLoc);
			case LAND: return !waterTiles.contains(targetLoc);
		    default: return false;
		}
	}
    
    public int getRows() 
    {
		return ants.getRows();
	}
	
	public int getCols() 
	{
		return ants.getCols();
	}
	
	public Tile getTile(Tile t, Aim direction) 
	{
		return ants.getTile(t, direction);
	}
	
	public Tile getRandomTile() 
	{
		return new Tile(
			(int)(ants.getCols()*Math.random()), 
			(int)(ants.getRows()*Math.random())
		);
	}
	
	public List<Aim> getDirections(Tile t1, Tile t2) 
	{
		return ants.getDirections(t1, t2);
	}
	
	public Set<Tile> getMyAnts() 
	{
		return ants.getMyAnts();
	}
	
	public Set<Tile> getEnemyAnts() 
	{
		return ants.getEnemyAnts();
	}
	
	public Set<Tile> getMyHills() 
	{
		return ants.getMyHills();
	}
	
	public Set<Tile> getEnemyHills() 
	{
		return enemyHills;
	}
	
	public Set<Tile> getFoodTiles() 
	{
		return ants.getFoodTiles();
	}
	
	public Set<Tile> getUnseenTiles() 
	{
		return unseenTiles;
	}
	
	public Set<Tile> getUnseenTiles(int amount) 
	{
		return pickTiles(unseenTiles, amount);
	}
	
	public boolean isMyAnt(Tile t) 
	{
		return ants.getMyAnts().contains(t);
	}
	
	public boolean isEnemyAnt(Tile t) 
	{
		return ants.getEnemyAnts().contains(t);
	}
	
	public boolean isMyHill(Tile t) 
	{
		return ants.getMyHills().contains(t);
	}
	
	public boolean isEnemyHill(Tile t) 
	{
		return enemyHills.contains(t);
	}
	
	public boolean isFood(Tile t) 
	{
		return ants.getFoodTiles().contains(t);
	}
	
	public boolean isWater(Tile t) 
	{
		return waterTiles.contains(t);
	}
	
	public boolean isReserved(Tile t) 
	{
		return reservedTiles.contains(t);
	}
	
	public void setReserved(Tile t, boolean reserved) 
	{
		if (reserved)
			reservedTiles.add(t);
		else
			reservedTiles.remove(t);
	}
	
	public void issueOrder(Tile t, Aim direction) 
	{
		ants.issueOrder(t, direction);
	}
	
	public int getInfluenceValue(Tile t)
	{
		return imap.getValue(t);
	}
}
