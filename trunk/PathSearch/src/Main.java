import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    
    private static void createAndShowGUI() 
    {   
    	Map map = new Map(8, 8);
		map.addWall(4, 1);
		map.addWall(4, 2);
		map.addWall(4, 3);
		map.addWall(4, 4);
		
		Tile begin = map.getData(7, 7);
		Tile end = map.getData(2, 0);
    	
        JFrame frame = new JFrame("Path Search");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Canvas(map, begin, end));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    public static void main(String[] args) 
	{
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
