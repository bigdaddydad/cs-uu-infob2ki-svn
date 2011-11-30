import java.util.HashMap;
import java.util.LinkedList;

/**
 * Represents a route from one tile to another.
 */
public class Route implements Comparable<Route> {
    
	private Tile location;
    private final Tile end;
    private final int distance;
    private final LinkedList<Tile> path;
    public boolean finished = false;
    
    public Route(Tile start, Tile end, LinkedList<Tile> path) {
        this.location = start;
        this.end = end;
        this.path = path;
        this.distance = path.size();
    }
    
    public Tile getCurrentLocation() {
        return location;
    }

    public Tile getEnd() {
        return end;
    }

    public int getDistance() {
        return distance;
    }
    
    public LinkedList<Tile> getPath() {
        return path;
    }

    @Override
    public int compareTo(Route route) {
        return distance - route.distance;
    }

    @Override
    public int hashCode() {
        return location.hashCode() * Ants.MAX_MAP_SIZE * Ants.MAX_MAP_SIZE + end.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof Route) {
            Route route = (Route)o;
            result = location.equals(route.location) && end.equals(route.end);
        }
        return result;
    }
    @Override
    public String toString() {
        return "route " + location.toString() + " to " + end.toString();
    }

	public void doStep(Ants gameState,MyBot bot) 
	{
		if(path.isEmpty())
		{
			finished = true;
		}
		else if(bot.doMoveLocation(gameState, location, path.getFirst()))
		{
			location = path.getFirst();
			
			path.removeFirst();
		}
		
	}
}