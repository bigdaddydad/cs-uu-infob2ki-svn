
public class Action {
	
	private final Target target;
	private final int maxAnts;
	
	public Action(Target target, int maxAnts)
	{
		this.target = target;
		this.maxAnts = maxAnts;
	}
	
	public Target getTarget()
	{
		return target;
	}
	
	public int getMaxAnts()
	{
		return maxAnts;
	}
}
