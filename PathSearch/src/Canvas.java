import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JPanel;

public class Canvas extends JPanel {

	private Map map;
	private PathFinder pathFinder;
	private Tile begin, end;
	private final int size = 50;
	
	public Canvas(Map map, Tile begin, Tile end)
	{
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(map.getCols() * size, map.getRows() * size));
		
		this.map = map;
		this.begin = begin;
		this.end = end;
		pathFinder = new PathFinder(map);
	}
	
	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g);
		
		// draw grid
		g.setColor(Color.BLACK);
		for (int row = 1; row < map.getRows(); row++)
			g.drawLine(0, row * size, map.getCols() * size, row * size);
		for (int col = 1; col < map.getCols(); col++)
			g.drawLine(col * size, 0, col * size, map.getCols() * size);
		
		// draw walls
		g.setColor(Color.BLACK);
		for (Tile t : map.getWalls())
			g.fillRect(t.getCol() * size, t.getRow() * size, size, size);
		
		// draw begin and end point
		g.setColor(Color.BLUE);
		g.fillOval(begin.getCol() * size, begin.getRow() * size, size, size);
		g.setColor(Color.RED);
		g.fillOval(end.getCol() * size, end.getRow() * size, size, size);
		
		// get shortest path
		ArrayList<Tile> shortestPath = pathFinder.shortestPath(begin, end);
		
		// draw shortest path
		if (shortestPath != null)
		{
			g.setColor(Color.YELLOW);
			for (Tile t : shortestPath)
				g.fillOval(t.getCol() * size + size/4, t.getRow() * size + size/4, size/2, size/2);
		}
    }
}
