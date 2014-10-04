
/*
 * CS341 - Direct Manipulation
 * Monica Chelliah
 */

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;


public interface MyShape {

	   public MyShape parent = null;
	   public ArrayList<MyShape> children = null;   
	   public AffineTransform trans  = null;
	   
	   public static boolean debug = false;
	   public Point2D point = new Point2D.Double(0,0);


	   public void setDebug(boolean b);
	   
	   public void addChild(MyShape r);

	   public void paint(Graphics2D g2);

}
