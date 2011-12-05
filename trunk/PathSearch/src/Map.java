import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Map {

	public final static int MAX_MAP_SIZE = 200;
	private final int rows, cols;
	private Set<Tile> walls = new HashSet<Tile>();
	
	public Map(int rows, int cols)
	{
		this.rows = rows;
		this.cols = cols;
	}
	
	public List<Tile> getNeighbors(Tile t)
	{
		List<Tile> result = new ArrayList<Tile>();
		
		for (int i = -1; i < 2; i += 2)
		{
			Tile neighbor = new Tile((t.getRow() + i + rows) % rows, t.getCol());
			
			if (isPassable(neighbor))
				result.add(neighbor);
		}
		
		for (int i = -1; i < 2; i += 2)
		{
			Tile neighbor = new Tile(t.getRow(), (t.getCol() + i + cols) % cols);
			
			if (isPassable(neighbor))
				result.add(neighbor);
		}
		
		return result;
	}
	
	public int getRows()
	{
		return rows;
	}
	
	public int getCols()
	{
		return cols;
	}
	
	public void addWall(int col, int row)
	{
		walls.add(new Tile(col, row));
	}
	
	public boolean isPassable(Tile tile)
	{
		return walls.contains(tile) == false;
	}
	
	public Set<Tile> getWalls()
	{
		return walls;
	}
}
