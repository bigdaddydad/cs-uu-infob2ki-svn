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
}
