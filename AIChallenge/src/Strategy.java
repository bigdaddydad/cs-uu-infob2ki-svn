import java.util.Arrays;
import java.util.List;

public enum Strategy {

	DEFAULT(Arrays.asList(
		new Action(Target.FOOD, 0), 
		new Action(Target.ENEMY_HILL, 0), 
		new Action(Target.LAND, 5)
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
