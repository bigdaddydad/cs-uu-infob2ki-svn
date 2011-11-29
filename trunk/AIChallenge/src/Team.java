import java.util.HashSet;
import java.util.Set;

public class Team 
{
	public Set<Tile> teamMembers = new HashSet<Tile>();
	
	public Team(){}
	
	public void addMember(Tile ant)
	{
		teamMembers.add(ant);
	}
	public void removeMember(Tile ant)
	{
		teamMembers.remove(ant);
	}
	public Tile getTile()
	{
		float x = 0;
		float y = 0;
		int counter = 0;
		
		for(Tile tile : teamMembers)
		{
			x += tile.getRow();
			y += tile.getCol();
			
			counter++;
		}
		
		x /= counter;
		y /= counter;
		
		return new Tile((int)x,(int)y);
	}
}
