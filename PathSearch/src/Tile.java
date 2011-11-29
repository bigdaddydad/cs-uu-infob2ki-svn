
public class Tile {

	private int row;
	private int col;
	
	public Tile parent;
	public int g_score;
	public int h_score;
	public int f_score;
	
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
	
	public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof Tile) {
            Tile tile = (Tile)o;
            result = row == tile.row && col == tile.col;
        }
        return result;
    }
}
