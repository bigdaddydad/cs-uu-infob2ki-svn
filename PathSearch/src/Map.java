import java.util.ArrayList;

public class Map {

	private final int cols, rows;
	private Tile[][] data;
	private ArrayList<Tile> walls;
	
	public Map(int cols, int rows)
	{
		this.rows = rows;
		this.cols = cols;
		
		walls = new ArrayList<Tile>();
		data = new Tile[rows][cols];
		
		for (int row = 0; row < rows; row++)
			for (int col = 0; col < cols; col++)
				data[col][row] = new Tile(row, col);
	}
	
	public Tile getData(int col, int row)
	{
		return data[col][row];
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
		walls.add(data[col][row]);
	}
	
	public boolean isAccessible(Tile tile)
	{
		return walls.contains(tile) == false;
	}
	
	public ArrayList<Tile> getWalls()
	{
		return walls;
	}
}
