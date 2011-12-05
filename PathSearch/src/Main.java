import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    
    private static void createAndRunApp() 
    {
    	Map map = new Map(8, 7);
		
    	map.addWall(7, 5);
    	map.addWall(6, 2);
    	map.addWall(4, 1);
		map.addWall(4, 2);
		map.addWall(4, 3);
		map.addWall(3, 0);
		map.addWall(3, 4);
		map.addWall(2, 0);
		map.addWall(2, 5);
		map.addWall(0, 0);
		map.addWall(0, 3);
		map.addWall(0, 4);
		
		Tile begin = new Tile(5, 5);
		Tile end = new Tile(3, 2);
    	
        JFrame frame = new JFrame("Path Search");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Canvas(map, begin, end));
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    public static void main(String[] args) 
	{
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndRunApp();
            }
        });
    }
}
