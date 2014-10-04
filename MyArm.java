/*
 * CS341 - Direct Manipulation
 * Monica Chelliah
 */

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public class MyArm extends Polygon implements MyShape{ //Rectangle {
   
   private static final Color GREEN = Color.GREEN;
   private static final Color ORANGE = Color.ORANGE;
   private static final Color ORANGE2 = new Color(239, 131, 38);
   
   private final static BasicStroke wideStroke = new BasicStroke(5.0f);
   private static final int COORD_LENGTH = 5;

   private Color color;
   private AffineTransform trans  = new AffineTransform();
   private MyShape parent;
   private ArrayList<MyShape> children;
   
   private static boolean debug = true;
   public Point2D point = new Point2D.Double(0,0);
   public boolean showClick = false;

   public void setDebug(boolean b) {
      debug = b;
      System.out.println("debug called");
   }

/**
 * Arm of the digger.
 * @param t Current transformation of window
 * @param xpoints current x points
 * @param ypoints current y points
 * @param p parent shape
 */
   public MyArm(AffineTransform t, int[] xpoints, int[] ypoints, MyShape p) {
      super(xpoints, ypoints, 6);
      trans = t;
      color = ORANGE2;
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


      g2.setColor(color);
      g2.fill(this);

      drawCircle(g2);
     
      /*
       * For debugging purpose: 
       * to know where you are/what the transformation do, 
       * the anchor point at the crossing of local coord axis is drawn.
       * (Can be made different than the (x,y) left corner location of
       * the rectangle, if you wish.)
       */
      if (debug) {
         // left top corner of arm is (0, 0) (uncomment below to see)
    	 Rectangle rect = getBounds();
         int CS_x = (int)rect.getX();  
         int CS_y = (int)rect.getY();

         g2.setStroke(wideStroke);
         // y-axis in green
         g2.setColor(GREEN);
         g2.drawLine(CS_x, CS_y+COORD_LENGTH, CS_x, CS_y-COORD_LENGTH);
         // x-axis in orange 
         g2.setColor(ORANGE);
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
          g2.setColor(GREEN);
          g2.drawLine(CS_x, CS_y+COORD_LENGTH, CS_x, CS_y-COORD_LENGTH);
          // x-axis in orange 
          g2.setColor(GREEN);
          g2.drawLine(CS_x+COORD_LENGTH, CS_y, CS_x-COORD_LENGTH, CS_y);
       }

      g2.setTransform(saveAT);
      
   }

   private void drawCircle(Graphics2D g2) {
	   AffineTransform tr = g2.getTransform();
	   g2.translate(23, 27);
	   g2.setColor(Color.BLACK);
	   g2.fill(new Ellipse2D.Double(0, 0, 8, 8));
	   
	   g2.setTransform(tr);
   }

}
