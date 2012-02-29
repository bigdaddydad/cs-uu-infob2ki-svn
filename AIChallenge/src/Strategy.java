import java.util.Arrays;
import java.util.List;

/**
 * Represents a strategy.
 */
public enum Strategy {

	DEFAULT(Arrays.asList(
		new Action(Target.FOOD, 1.0), 
		new Action(Target.LAND, 1.0)
	)),
	
	OFFENSIVE(Arrays.asList(
		new Action(Target.ENEMY_HILL, 0.5),
		new Action(Target.FOOD, 1.0), 
		new Action(Target.LAND, 1.0)
	)),
	
	DEFENSIVE(Arrays.asList(
		new Action(Target.MY_HILL, 0.5),
		new Action(Target.FOOD, 1.0), 
		new Action(Target.LAND, 1.0)
	)),
	
	ALL_OFFENSIVE(Arrays.asList(
		new Action(Target.ENEMY_HILL, 1.0),
		new Action(Target.FOOD, 1.0), 
		new Action(Target.LAND, 1.0)
	)),
	
	ALL_DEFENSIVE(Arrays.asList(
		new Action(Target.MY_HILL, 1.0)
	));
	
	private final List<Action> actions;
	
	Strategy(List<Action> actions)
	{
		this.actions = actions;
	}
	
	public List<Action> getActions()
	{
		return actions;
	}
}
