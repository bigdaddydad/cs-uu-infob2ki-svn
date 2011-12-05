
public class Tile implements Comparable<Tile> {

	private final int row, col;
	public Tile parent;
	public int g_score, h_score, f_score;
	
	public Tile(int row, int col)
	{
		this.row = row;
		this.col = col;
	}
	
	public int getRow()
	{
		return row;
	}
	
	public int getCol()
	{
		return col;
	}
	
	@Override
	public int hashCode()
	{
		return row * Map.MAX_MAP_SIZE + col;
	}
	
	@Override
    public int compareTo(Tile t) 
	{
        return hashCode() - t.hashCode();
    }
	
	@Override
    public boolean equals(Object o) 
	{
        if (o instanceof Tile) 
        {
            Tile tile = (Tile)o;
            return row == tile.row && col == tile.col;
        }
        
        return false;
    }
}
