import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.PriorityQueue;
import javax.swing.JPanel;

public class Canvas extends JPanel {

	private Tile[][] map;
	private Tile begin, end;
	private ArrayList<Tile> walls;
	
	private int rows = 8;
	private int cols = 8;
	private int size = 50;
	
	public Canvas()
	{
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(cols * size, rows * size));
		
		initializeMap();
		initializeWalls();
		initializeRoute();
	}
	
	private void initializeMap()
	{
		map = new Tile[rows][cols];
		
		for (int row = 0; row < rows; row++)
			for (int col = 0; col < cols; col++)
				map[col][row] = new Tile(row, col);
	}
	
	private void initializeWalls()
	{
		walls = new ArrayList<Tile>();
		
		walls.add(map[4][5]);
		walls.add(map[4][6]);
		walls.add(map[5][5]);
		walls.add(map[5][4]);
	}
	
	private void initializeRoute()
	{
		begin = map[0][0];
		end = map[6][5];
	}
	
	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g);
		
		g.setColor(Color.BLACK);
		
		// draw grid
		g.setColor(Color.BLACK);
		for (int row = 1; row < rows; row++)
			g.drawLine(0, row * size, cols * size, row * size);
		for (int col = 1; col < cols; col++)
			g.drawLine(col * size, 0, col * size, rows * size);
		
		// draw walls
		g.setColor(Color.BLACK);
		for (Tile wall : walls)
			g.fillRect(wall.getCol() * size, wall.getRow() * size, size, size);
		
		// draw begin and end point
		g.setColor(Color.BLUE);
		g.fillOval(begin.getCol() * size, begin.getRow() * size, size, size);
		g.setColor(Color.RED);
		g.fillOval(end.getCol() * size, end.getRow() * size, size, size);
		
		// draw shortest path info
		g.setColor(Color.BLACK);
		shortestPath(begin, end, g);
		
		// draw shortest path route
		g.setColor(Color.YELLOW);
		Tile t = end.parent;
		while (t != begin)
		{
			g.fillOval(t.getCol() * size + size/4, t.getRow() * size + size/4, size/2, size/2);
			t = t.parent;
		}
    }
	
	private void shortestPath(Tile start, Tile goal, Graphics g)
	{
		ArrayList<Tile> closedset = new ArrayList<Tile>();
		PriorityQueue<Tile> openset = new PriorityQueue<Tile>();
		
		start.g_score = 0;
		start.h_score = getEstimatedDistance(start, goal);
		start.f_score = start.g_score + start.h_score;
		
		openset.add(start);
		
	    while (!openset.isEmpty())
	    {
			Tile x = openset.peek();
			 
			if (x == goal)
				break;
			 
			openset.remove();
			closedset.add(x);
			 
			ArrayList<Tile> neighbors = getNeighbors(x);
			 
			for (Tile y : neighbors)
			{
			    if (closedset.contains(y))
			        continue;
			    
			    boolean tentative_is_better = false;
			    int tentative_g_score = x.g_score + 1;
			    
			    if (!openset.contains(y))
			    {
			    	openset.add(y);
			    	tentative_is_better = true;
			    } 
			    else if (tentative_g_score < y.g_score)
			    {
			    	tentative_is_better = true;
			    }
			     
			    if (tentative_is_better)
			    {
			    	y.parent = x;
			        y.g_score = tentative_g_score;
			        y.h_score = getEstimatedDistance(y, goal);
			        y.f_score = y.g_score + y.h_score;
			    }
			}
	    }
	    
	    // Draw f_score info
	    for (Tile t : openset)
       	 	g.drawString("" + t.f_score, t.getCol() * size, t.getRow() * size);
        for (Tile t : closedset)
       	 	g.drawString("" + t.f_score, t.getCol() * size, t.getRow() * size);
	}
	
	private int getEstimatedDistance(Tile begin, Tile end)
	{
		return Math.abs(begin.getCol() - end.getCol()) + Math.abs(begin.getRow() - end.getRow());
	}
	
	private ArrayList<Tile> getNeighbors(Tile t)
	{
		ArrayList<Tile> result = new ArrayList<Tile>();
		
		if (t.getRow() + 1 < rows)
		{
			Tile neighbor = map[t.getCol()][t.getRow()+1];
			
			if (!walls.contains(neighbor))
				result.add(neighbor);
		}
		
		if (t.getRow() - 1 >= 0)
		{
			Tile neighbor = map[t.getCol()][t.getRow()-1];
			
			if (!walls.contains(neighbor))
				result.add(neighbor);
		}
		
		if (t.getCol() + 1 < cols)
		{
			Tile neighbor = map[t.getCol()+1][t.getRow()];
			
			if (!walls.contains(neighbor))
				result.add(neighbor);
		}
		
		if (t.getCol() - 1 >= 0)
		{
			Tile neighbor = map[t.getCol()-1][t.getRow()];
			
			if (!walls.contains(neighbor))
				result.add(neighbor);
		}
		
		return result;
	}
}
