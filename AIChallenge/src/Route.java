import java.util.LinkedList;

/**
 * Represents a route from one tile to another.
 */
public class Route implements Comparable<Route> {
    
	private final Team start;
    private final Tile end;
    private final int distance;
    private final LinkedList<Tile> path;
    
    public Route(Team start, Tile end, LinkedList<Tile> path) {
        this.start = start;
        this.end = end;
        this.path = path;
        this.distance = path.size();
    }
    
    public Team getStart() {
        return start;
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
        return start.hashCode() * Ants.MAX_MAP_SIZE * Ants.MAX_MAP_SIZE + end.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof Route) {
            Route route = (Route)o;
            result = start.equals(route.start) && end.equals(route.end);
        }
        return result;
    }
    @Override
    public String toString() {
        return "route " + start.getTile().toString() + " to " + end.toString();
    }
}