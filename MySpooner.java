/*
 * CS341 - Direct Manipulation
 * Monica Chelliah
 */

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public class MySpooner extends Arc2D.Float implements MyShape{
   
	//Global variables
   private static final Color GREEN = Color.GREEN;
   private static final Color ORANGE = Color.ORANGE;
   private static final Color LIGHTYELLOW = new Color(251, 218, 60);
   
   private final static BasicStroke wideStroke = new BasicStroke(5.0f);
   private static final int COORD_LENGTH = 5;

   private Color color;
   private AffineTransform trans  = new AffineTransform();
   private MyShape parent;
   private ArrayList<MyShape> children;
   
   private static boolean debug = false;
   public Point2D point = new Point2D.Double(0,0);
   public boolean showClick = false;
      
   public boolean filled = false;

   public void setDebug(boolean b) {
      debug = b;
      System.out.println("debug called");
   }

/**
 * The spoon of the digger.
 * @param t Current transformation
 * @param w width
 * @param h height
 * @param p parent shape
 */
   public MySpooner(AffineTransform t, int w, int h, MyShape p) {
      super(0, 0, w, h, 0, 180, Arc2D.PIE);
      trans = t;
      color = LIGHTYELLOW;
      parent = p;
      children = new ArrayList<MyShape>();
   }
   
   @Override
   public void addChild(MyShape r) {
      children.add(r);
   }

   public void paint(Graphics2D g2) {
      
      AffineTransform saveAT = g2.getTransform();
      g2.transform(trans);

      if(filled){
    	  filledSpoon(g2);
      }
      g2.setColor(color);
      g2.fill(this);
     
      /*
       * For debugging purpose: 
       * to know where you are/what the transformation do, 
       * the anchor point at the crossing of local coord axis is drawn.
       * (Can be made different than the (x,y) left corner location of
       * the rectangle, if you wish.)
       */
      if (debug) {
         // left top corner of spooner is (0, 0) (uncomment below to see)
         int CS_x = (int) getX();  
         int CS_y = (int) getY();

         g2.setStroke(wideStroke);
         // y-axis in green
         g2.setColor(Color.GRAY);
         g2.drawLine(CS_x, CS_y+COORD_LENGTH, CS_x, CS_y-COORD_LENGTH);
         // x-axis in orange 
         g2.setColor(Color.GRAY);
         g2.drawLine(CS_x+COORD_LENGTH, CS_y, CS_x-COORD_LENGTH, CS_y);
      }

      /*
       * Draw the children linked to that shape
       */ 
      
      Iterator<MyShape> it = children.iterator();
      while (it.hasNext()) {
         MyShape r = it.next();
         r.paint(g2);
      }
      
      if (showClick) {
          // left top corner of rectangle is (0, 0) (uncomment below to see)
          int CS_x = (int)point.getX();  
          int CS_y = (int)point.getY();

          g2.setStroke(wideStroke);
          // y-axis in green
          g2.setColor(Color.GRAY);
          g2.drawLine(CS_x, CS_y+COORD_LENGTH, CS_x, CS_y-COORD_LENGTH);
          // x-axis in orange 
          g2.setColor(Color.BLUE);
          g2.drawLine(CS_x+COORD_LENGTH, CS_y, CS_x-COORD_LENGTH, CS_y);
       }

      g2.setTransform(saveAT);
      
   }

   private void filledSpoon(Graphics2D g2) {
	   AffineTransform tr = g2.getTransform();
	   g2.translate(2, 10);
	   g2.setColor(new Color(170, 120, 100));
	   g2.fill(new Ellipse2D.Double(0, 0, 26, 20));
	   
	   g2.setTransform(tr);
   }

}
