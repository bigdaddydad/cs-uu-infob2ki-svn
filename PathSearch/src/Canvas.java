import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

public class Canvas extends JPanel implements Runnable {

	private Map map;
	private Tile begin, end;
	private final int size = 50;
	private List<Tile> walkedPath = new ArrayList<Tile>();
	
	public Canvas(Map map, Tile begin, Tile end)
	{
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(map.getCols() * size, map.getRows() * size));
		
		this.map = map;
		this.begin = begin;
		this.end = end;
		
		new Thread(this).start();
	}
	
	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g);
		
		// draw grid
		g.setColor(Color.GRAY);
		for (int row = 1; row < map.getRows(); row++)
			g.drawLine(0, row * size, map.getCols() * size, row * size);
		for (int col = 1; col < map.getCols(); col++)
			g.drawLine(col * size, 0, col * size, map.getCols() * size);
		
		// draw walls
		g.setColor(Color.BLACK);
		for (Tile t : map.getWalls())
			g.fillRect(t.getCol() * size, t.getRow() * size, size, size);
		
		// draw begin and end point
		g.setColor(Color.RED);
		g.fillOval(end.getCol() * size, end.getRow() * size, size, size);
		g.setColor(Color.BLUE);
		g.fillOval(begin.getCol() * size, begin.getRow() * size, size, size);
		
		// draw walked path
		g.setColor(Color.YELLOW);
		for (Tile t : walkedPath)
			g.fillOval(t.getCol() * size + size/4, t.getRow() * size + size/4, size/2, size/2);
    }
	
	public void run()
	{
		LinkedList<Tile> shortestPath = PathFinder.shortestPath(begin, end, map);
		
		while (shortestPath.size() > 0)
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
			
			walkedPath.add(begin);
			begin = shortestPath.removeFirst();
			repaint();
		}
	}
}
