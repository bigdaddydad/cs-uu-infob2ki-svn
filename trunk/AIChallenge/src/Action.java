/**
 * Represents a subgoal of a strategy.
 */
public class Action {
	
	private final Target target;
	private final double antRate;
	
	public Action(Target target, double antRate)
	{
		this.target = target;
		this.antRate = antRate;
	}
	
	public Target getTarget()
	{
		return target;
	}
	
	public double getAntRate()
	{
		return antRate;
	}
}
