import java.util.LinkedList;

/**
 * Represents a route from one tile to another.
 */
public class Route implements Comparable<Route> {
	
	private final Target target;
	private Tile currentLoc;
	private final Tile startLoc;
    private final Tile targetLoc;
    private final LinkedList<Tile> path;
    private final int distance;
    private boolean finished = false;
    
    public Route(Target target, Tile startLoc, Tile targetLoc, LinkedList<Tile> path) {
        this.target = target;
    	this.currentLoc = startLoc;
    	this.startLoc = startLoc;
        this.targetLoc = targetLoc;
        this.path = path;
        this.distance = path.size();
    }
    
    public Target getTarget() {
    	return target;
    }
    
    public Tile getCurrentLocation() {
        return currentLoc;
    }
    
    public Tile getStartLocation() {
        return startLoc;
    }
    
    public Tile getTargetLocation() {
        return targetLoc;
    }
    
    public int getDistance() {
        return distance;
    }
    
    public int getCurrentDistance() {
        return path.size();
    }
    
    public boolean isFinished() {
    	return finished;
    }
    
    @Override
    public int compareTo(Route route) {
        return distance - route.distance;
    }
    
    @Override
    public int hashCode() {
        return startLoc.hashCode() * Ants.MAX_MAP_SIZE * Ants.MAX_MAP_SIZE + targetLoc.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof Route) {
            Route route = (Route)o;
            result = startLoc.equals(route.startLoc) && targetLoc.equals(route.targetLoc);
        }
        return result;
    }
    
    @Override
    public String toString() {
        return "route " + startLoc.toString() + " to " + targetLoc.toString();
    }
    
    /**
     * Functie die de volgende stap in de route probeert te doen 
     */
	public void doStep(Ants gameState, MyBot bot) 
	{
		if (path.isEmpty())
		{
			finished = true;
		}
		else if (bot.doMoveLocation(gameState, currentLoc, path.getFirst()))
		{
			currentLoc = path.removeFirst();
		}
	}
}