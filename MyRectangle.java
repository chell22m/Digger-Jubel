
/*
 * CS341 - Direct Manipulation
 * Monica Chelliah
 */

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public class MyRectangle extends Rectangle2D.Float implements MyShape{ //Rectangle {
   
	//Global variables
   private static final Color GREEN = Color.GREEN;
   private static final Color ORANGE = Color.ORANGE;
   private static final Color DARKYELLOW = new Color(233, 184, 43);
   private static final Color LIGHTYELLOW = new Color(251, 218, 60);
   private static final Color WINDOW = new Color(249, 247, 221);

   private final static BasicStroke wideStroke = new BasicStroke(5.0f);
   private static final int COORD_LENGTH = 5;

   private Color color;
   public AffineTransform trans  = new AffineTransform();
   private MyShape parent;
   private ArrayList<MyShape> children;
   private boolean body = false;
   
   public RoundRectangle2D.Double rect = new RoundRectangle2D.Double(0, 0, 100, 20, 5, 5);
   public AffineTransform notch = new AffineTransform();
   
   private static boolean debug = false;
   public Point2D point = new Point2D.Double(0,0);
   public boolean showClick = false;

   public void setDebug(boolean b) {
      debug = b;
      System.out.println("debug called");
      Iterator<MyShape> it = children.iterator();
      while (it.hasNext()) {
         MyShape r = it.next();
         r.setDebug(b);
      }
   }

   /*
    * The constructor of MyRectangle assigns the following fields that
    * define the rectangle geometry and bare appearance. 
    * - An AffineTransform trans that changes the coordinate system
    * of the Graphics2D context prior to draw the shape
    * - A width, a height and a color and
    * - A parent (the root has parent==null)
    * Notice: without AffineTransform trans it is impossible to draw 
    * a rotated rectangle.
    *
    * The AffineTransform of trans of a MyRectangle object defines 
    *    its local coordinate system in relation to the one of its parent.
    * A traversal from parent to children encodes the hierarchical formation,
    * that permits the hierarchical modelling.
    * 
    * The rectangle shape is drawn from left top corner (0,0) AFTER 
    * transforming by all the AffineTransformations down to the leaf 
    * (i.e. the AffineTransform trans of parent at each level).
    * When a parent changes the orientation of its local coordinate system 
    * with a rotation
    * - its local AffineTransform is not the identity matrix, neither just 
    * a translation transformation,
    * - its children follow it since they use parent transformations to
    * proceed their rendering.
    * Notice: Differently of the starting code you will not always
    * want the coordinate system by which the transformation is applied
    * to be at the left corner of a rectangle.
    * Important reference for transformations :
    * http://java.sun.com/docs/books/tutorial/2d/display/transforming.html
    * Link to update
    */
   public MyRectangle(AffineTransform t, int w, int h, Color c, MyRectangle p, boolean b) {
      super(0, 0, w, h);
      trans = t;
      color = c;
      parent = p;
      body = b;
      children = new ArrayList<MyShape>();
   }
   
   @Override
   public void addChild(MyShape r) {
      children.add(r);
   }

   public void paint(Graphics2D g2) {
      
      AffineTransform saveAT = g2.getTransform();
      g2.transform(trans);

      if(!body && parent != null){
          AffineTransform saveAT2 = g2.getTransform();
          Iterator<MyShape> it = children.iterator();
          while (it.hasNext()) {
        	 g2.transform(notch);
             MyShape r = it.next();
             r.paint(g2);
          }
          g2.setTransform(saveAT2);
      }
      
      g2.setColor(color);
      g2.fill(this);
      
      if(parent == null){
    	  drawBody(g2);
      }
      else{
          if(body){
        	  drawCabin(g2);
          }
          else{
        	  drawExtender(g2);
          }
      }

     
      /*
       * For debugging purpose: 
       * to know where you are/what the transformation do, 
       * the anchor point at the crossing of local coord axis is drawn.
       * (Can be made different than the (x,y) left corner location of
       * the rectangle, if you wish.)
       */
      if (debug) {
         // left top corner of rectangle is (0, 0) (uncomment below to see)
         int CS_x = (int)getX();  
         int CS_y = (int)getY();

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
         if(parent == null || body){
             r.paint(g2);
         }
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
          g2.setColor(ORANGE);
          g2.drawLine(CS_x+COORD_LENGTH, CS_y, CS_x-COORD_LENGTH, CS_y);
       }

      g2.setTransform(saveAT);

   }


   private void drawBody(Graphics2D g2) {
	   AffineTransform tr = g2.getTransform();
	   g2.translate(10, 0);
	   g2.setColor(DARKYELLOW);
	   g2.fill(new RoundRectangle2D.Double(0, 0, 70, 30, 10, 10));
	   g2.translate(7, 7);
	   g2.setColor(Color.BLACK);
	   g2.fill(new RoundRectangle2D.Double(0, 0, 40, 4, 5, 5));
	   g2.translate(0, 7);
	   g2.fill(new RoundRectangle2D.Double(0, 0, 40, 4, 5, 5));
	   g2.setTransform(tr);
	   g2.translate(22, 27);
	   g2.setColor(Color.DARK_GRAY);
	   g2.fill(new RoundRectangle2D.Double(0, 0, 65, 6, 10, 10));
	   g2.translate(22, 6);
	   g2.setColor(Color.BLACK);
	   g2.fill(new RoundRectangle2D.Double(0, 0, 20, 4, 5, 5));
	   g2.setTransform(tr);
	   g2.translate(0, 37);
	   g2.setColor(Color.DARK_GRAY);
	   g2.fill(new RoundRectangle2D.Double(0, 0, 100, 16, 20, 20));
	   g2.translate(5, 2);
	   g2.setColor(Color.BLACK);
	   g2.fill(new Ellipse2D.Double(0, 0, 12, 12));
	   g2.translate(15, 0);
	   g2.fill(new Ellipse2D.Double(0, 0, 12, 12));
	   g2.translate(49, 0);
	   g2.fill(new Ellipse2D.Double(0, 0, 12, 12));
	   g2.translate(15, 0);
	   g2.fill(new Ellipse2D.Double(0, 0, 12, 12));
	   g2.setTransform(tr);
   }

   private void drawCabin(Graphics2D g2) {
	   AffineTransform tr = g2.getTransform();
	   g2.setColor(LIGHTYELLOW);
	   g2.fill(new RoundRectangle2D.Double(0, 0, 45, 70, 10, 10));
	   g2.translate(6, 5);
	   g2.setColor(WINDOW);
	   g2.fill(new RoundRectangle2D.Double(0, 0, 33, 50, 10, 10));
	   g2.setTransform(tr);
   }


   private void drawExtender(Graphics2D g2) {
	   AffineTransform tr = g2.getTransform();
	   g2.setColor(DARKYELLOW);
	   g2.draw(rect);
	   g2.setColor(LIGHTYELLOW);
	   g2.fill(rect);
	   g2.translate(0, 5);
	   g2.setColor(Color.GRAY);
	   g2.fill(new RoundRectangle2D.Double(0, 0, rect.width - 25, 4, 5, 5));
	   g2.translate(0, 0);
	   g2.setColor(Color.BLACK);
	   g2.fill(new Ellipse2D.Double(rect.width -19, 0, 10, 10));
	   
	   g2.setTransform(tr);
   }

}

   
