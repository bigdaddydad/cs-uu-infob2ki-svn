import java.util.Comparator;

public class TilePathComparator implements Comparator<Tile> {
	
	public int compare(Tile t1, Tile t2) 
	{
		return t1.f_score - t2.f_score;
	}
}
