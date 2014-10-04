/*
 * CS341 - Direct Manipulation
 * Monica Chelliah
 */

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

import java.io.*;

 class DrawingPanel extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener {
   
   //recorded events for playback
   private static final String RECORD_FILE = "events";

   //colors
   private final static Color BG = Color.WHITE;
   private final static Color BLUE = Color.BLUE;
   private final static Color RED = Color.RED;
   private final static Color GREEN = Color.GREEN;
   private final static Color TRANSPARENT = new Color(255, 255, 255, 0);
   
   // The root of the model (the only shape in the hierarchy without parent)
   // MyRectangle constructor sets the parent of the shape (null for root)
   // MyRectangle addChild() method is used to connect a parent to its children 
   // (one in our case but could be extended to many)
   public MyRectangle displayRoot = null;

   // Handle transformation on the entire object, i.e. the digger
   private AffineTransform objectTransform = new AffineTransform();

   // Needed to reposition before replay
   // (Not possible across program execution as not stored in text file:
   // do your recording from the initial digger location.)
   private AffineTransform oldObjectTransform;
   
   private ArrayList<AffineTransform> transform = new ArrayList<AffineTransform>();
   
   //digger parts
   private MyRectangle _B = null;
   private MyRectangle _C = null;
   private MyArm arm = null;
   private MySpooner spoon = null;

   
   // Used for simple selection
   private static final int NONE = -2;
   private static final int ALL_OBJECT = -1;
   private int selected = NONE;
   private int lastX, lastY;
   
   //keeps track of angles of all digger parts
   private float angleC = 0;
   private float angleArm = 0;
   private float angleSpoon = (float) Math.PI/2;
   private float angleSpoonCont = 0;
   
   //keeps track of inversion
   private boolean invert = false;
   private double lengthC = 85;
   
   //keeps track of which digger part is selected
   private boolean diggerSelected = false;
   private boolean bSelected = false;
   private boolean cSelected = false;
   private boolean armSelected = false;
   private boolean spoonSelected = false;
   
   //keeps track of where dirt has been dug and dropped, as well as stars
   private ArrayList<MouseEvent> dirtDropXY = new ArrayList<MouseEvent>();
   private ArrayList<MouseEvent> dirtDigXY = new ArrayList<MouseEvent>();
   private ArrayList<Point2D.Double> stars = new ArrayList<Point2D.Double>();
   
   //current frame number
   private int frameNumber;
   //shape for hills
   private Path2D hills; 
//   private Path2D bottles;
   
   //keeps track of night time
   private boolean night = false;
   
   public Point2D point = new Point2D.Double(0,0);

   // Used for recording and replay
   private ObjectOutputStream out;
   private boolean isRecording = false;
   // Record some mouse events, may want to also record more according to
   // user interaction choices/definitions
   private ArrayList<MouseEvent> events;
   
   

   private static RenderingHints rh = new RenderingHints(
		RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

   /**
    * Sets up window.
    */
   public DrawingPanel() {
      super();
      setBackground(BG);
      addMouseMotionListener(this);
      addMouseListener(this);
      addMouseWheelListener(this);
      
      //initialize
      init();
      objectTransform.translate(1, 1);
      transform.get(0).translate(100, 320);
      
      //draw hills
      hills = new Path2D.Float();
      hills.moveTo(0,200);
      hills.lineTo(50,170);
      hills.lineTo(130,270);
      hills.lineTo(260,150);
      hills.lineTo(430,220);
      hills.lineTo(550,120);
      hills.lineTo(700,190);
      hills.lineTo(700,300);
      hills.lineTo(0, 300);
      hills.closePath();
      
      //randomly generate star positions
      makeStars();
      
      //TODO DRAW BOTTLES FOR A LATER DATE
//      bottles = new Path2D.Float();
//      double a= 300*Math.random();
//      double b= 300+ 300*Math.random();
//      bottles.moveTo(a, b);
//      bottles.lineTo(a+10, b);
//      bottles.lineTo(330, 330);
//      bottles.lineTo(360, 360);
//      bottles.lineTo(360, 390);
//      bottles.lineTo(330, 410);
//      bottles.lineTo(300, 410);
//      bottles.lineTo(270, 390);
//      bottles.lineTo(270, 360);
//      bottles.lineTo(300, 330);
//      bottles.closePath();
      
      //increase frame number and repainting for a moving visual
      new Timer(30,new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
             frameNumber++;
             repaint();
          }
       }).start();
   }

   /**
    * Randomly generate stars
    */
   private void makeStars() {
	   for(int i=0; i<=15; i++){
		   stars.add(new Point2D.Double(Math.random()*700, Math.random()*300));
	   }
   }

/*
    * Create a hierarchical object.
    * Provided code uses 2 simple rectangles, which are connected at
    * an anchor point (75, 25) on the parent rectangle, i.e. displayRoot.
    * The axis of the local coordinate system of the child rectangle, 
    * i.e. rect2, are rotated by -45 degrees
    * Try the Option menu, Debug On to see how the local coordinates systems
    * of the 2 rectangles relate
    */ 
   public void init() {
	   //Add current transformation
	  transform.add(objectTransform);
      AffineTransform trans = new AffineTransform();
      transform.add(trans);
      
      //Digger root
      displayRoot =  new MyRectangle(trans, 100, 50, TRANSPARENT, null, false);

      AffineTransform trans2 = AffineTransform.getTranslateInstance(50.0, -45.0);
      transform.add(trans2);
      
      // From API AffineTransform public void rotate method: 
      // "Rotating with a positive angle theta rotates points on the positive x axis 
      // (x+) toward the positive y axis (y+)". 
      // Since y+ is pointing down on the JComponent, a negative rotation has the 
      // effect of rotating upward
      // (reverse of trigonometry circle you may be used to since y+ is pointing down on screen) 
      //      trans2.rotate(-Math.PI/2.0);
      
      //add next digger part B
      _B = new MyRectangle(trans2, 45, 70, TRANSPARENT, displayRoot, true);
      displayRoot.addChild(_B);
      
      //transform window and add next digger part C
      AffineTransform trans3 = AffineTransform.getTranslateInstance(25, 20);
      transform.add(trans3);
      _C = new MyRectangle(trans3, 100, 20, TRANSPARENT, _B, false);
      _B.addChild(_C);
      
      //transform window and add next digger part Arm
      AffineTransform trans4 = AffineTransform.getTranslateInstance(lengthC, 10);
      transform.add(trans4);
      int[] xpoints = new int[6];
      int[] ypoints = new int[6];
      initializePoint(xpoints, ypoints);
      arm = new MyArm(trans4, xpoints, ypoints, _C);
      _C.addChild(arm);
      
      //transform window and add next digger part Spoon
      AffineTransform trans5 = AffineTransform.getTranslateInstance(32, 53);
      transform.add(trans5);
      trans5.rotate(Math.PI/2);
      spoon = new MySpooner(trans5, 30, 40, arm);
      arm.addChild(spoon);
      spoon.setDebug(true);
   }

   private static void initializePoint(int[] xpoints, int[] ypoints) {
	   // 0,0
	   xpoints[0] = 0;
	   ypoints[0] = 0;
	   //25,0
	   xpoints[1] = 15;
	   ypoints[1] = 0;
	   //50,50
	   xpoints[2] = 35;
	   ypoints[2] = 30;
	   //50,100
	   xpoints[3] = 35;
	   ypoints[3] = 60;
	   //25, 100
	   xpoints[4] = 20;
	   ypoints[4] = 60;
	   //25,50
	   xpoints[5] = 20;
	   ypoints[5] = 30;

   }

   public void paintComponent(Graphics g) {
	   super.paintComponent(g);
	   // From API The Graphics object should have the same state when 
	   // you're finished painting as it had when you started. Therefore
	   // either restore state (translate(x,y) and end with translate(-x,-y)
	   //     or make a copy (easier)
	   // Example of copying the Graphics object
	   Graphics2D g2 = (Graphics2D)g.create(); //copy g      

	   AffineTransform saveTr = g2.getTransform();

	   g2.setRenderingHints(rh);

	   g2.clearRect(0, 0, getWidth(), getHeight());

	   //start and stop night time
	   if((700 - (frameNumber%2500)*0.3) == -44.0){
		   //    	  System.out.println((700 - (frameNumber%2500)*0.3));
		   night = !night;
	   }


	   /* Draw the sky, ground, and hills directly (untransformed). */
	   if(night){
		   g2.setColor( new Color(4, 16, 29) ); //sky at night
	   }else{
		   g2.setColor( new Color(151, 209, 227) ); //sky
	   }
	   g2.fillRect(0,0,700,300); //sky

	   //draw stars
	   if(night){
		   drawStars(g2);
	   }

	   //draw Sun and Clouds
	   g2.translate(700 - (frameNumber%2500)*0.3, 30);
	   drawSun(g2);
	   g2.setTransform(saveTr);

	   g2.translate(700 - (frameNumber%1000)*0.8, 20);
	   drawClouds(g2);
	   g2.setTransform(saveTr);

	   g2.translate(800 - (frameNumber%2000)*0.5, 65);
	   g2.scale(2, 2);
	   drawClouds(g2);
	   g2.setTransform(saveTr);

	   g2.translate(700 - (frameNumber%800)*1.3, 95);
	   g2.scale(0.7, 0.7);
	   drawClouds(g2);
	   g2.setTransform(saveTr);

	   g2.translate(700 - (frameNumber%1300), 150);
	   g2.scale(1.5, 1.5);
	   drawClouds(g2);
	   g2.setTransform(saveTr);

	   g2.translate(700 - (frameNumber%1400)*0.7, 200);
	   g2.scale(0.7, 0.7);
	   drawClouds(g2);
	   g2.setTransform(saveTr);

	   g2.translate(50, 175);
	   g2.scale(0.4, 0.4);
	   drawClouds(g2);
	   g2.setTransform(saveTr);

	   g2.translate(100, 75);
	   g2.scale(0.4, 0.4);
	   drawClouds(g2);
	   g2.setTransform(saveTr);

	   g2.translate(300, 45);
	   g2.scale(0.4, 0.4);
	   drawClouds(g2);
	   g2.setTransform(saveTr);

	   g2.translate(450, 55);
	   g2.scale(0.4, 0.4);
	   drawClouds(g2);
	   g2.setTransform(saveTr);

	   g2.translate(635, 115);
	   g2.scale(0.4, 0.4);
	   drawClouds(g2);
	   g2.setTransform(saveTr);

	   if(night){
		   g2.setColor( new Color(16, 31, 52) ); //hills at night
	   }else{
		   g2.setColor( new Color(231, 147, 20) ); //hills
	   }
	   g2.fill(hills); 

	   if(night){
		   g2.setColor(new Color(58, 78, 102)); //ground at night
	   }else{
		   g2.setColor(new Color(253, 195, 104)); //ground
	   }
	   g2.fillRect(0,300,700,400);  // the ground

	   //dirt
	   drawDirtDig(g2);
	   drawDirtDrop(g2);

	   g2.transform(objectTransform);

	   // Start painting with displayRoot, which inside its paint
	   // method paints its own children (single child in our case)
	   if (displayRoot != null) {
		   displayRoot.paint(g2);
	   }

	   g2.setTransform(saveTr);

	   g2.dispose(); //release the copy's resources
   }

  /**
   * Draw all dug out dirt
   * @param g2 graphics object
   */
   private void drawDirtDig(Graphics2D g2) {
	   AffineTransform saveTr = g2.getTransform();
	   if(night){
		   g2.setColor(new Color(17, 31, 53));
	   }else{
		   g2.setColor(new Color(173, 99, 49));
	   }
	   for(MouseEvent dirt : dirtDigXY){
		   g2.translate(dirt.getX(), dirt.getY() + 20);
		   g2.fillArc(0, 0, 20, 30, 0, -180);
		   g2.setTransform(saveTr);
	   }
   }

   /**
    * Draw all dropped dirt
    * @param g2 graphics object
    */
   private void drawDirtDrop(Graphics2D g2) {
	   AffineTransform saveTr = g2.getTransform();
	   if(night){
		   g2.setColor(new Color(17, 31, 53));
	   }else{
		   g2.setColor(new Color(173, 99, 49));
	   }
	   for(MouseEvent drop : dirtDropXY){
		   g2.translate(drop.getX(), drop.getY() + 20);
		   g2.fillArc(0, 0, 20, 30, 0, 180);
		   g2.setTransform(saveTr);
	   }
   }

   /**
    * Draw starts
    * @param g2 graphics object
    */
   private void drawStars(Graphics2D g2) {
	   AffineTransform saveTr = g2.getTransform();
	   g2.setColor(Color.WHITE);
	   for(Point2D.Double star : stars){
		   g2.translate(star.x, star.y);
		   g2.fillOval(0, 0, 1, 1);
		   g2.setTransform(saveTr);
	   }
   }

   /**
    * Draw Sun
    * @param g2 graphics object
    */
   private void drawSun(Graphics2D g2) {
	   if(night){
		   g2.setColor(new Color(254, 238, 171));
	   }else{
		   g2.setColor(Color.YELLOW);
	   }
	   g2.fill(new Ellipse2D.Double(0, 0, 40, 40));
   }

   /**
    * Draw Clouds
    * @param g2 graphics object
    */
   private void drawClouds(Graphics2D g2) {
	   if(night){
		   g2.setColor(new Color(171, 172, 176));
	   }else{
		   g2.setColor(Color.WHITE);
	   }
	   g2.fill(new RoundRectangle2D.Double(0, 0, 100, 15, 20, 20));
	   g2.translate(17, -15);
	   g2.fill(new Ellipse2D.Double(0, 0, 40, 30));
	   g2.translate(20, 5);
	   g2.fill(new Ellipse2D.Double(0, 0, 30, 15));
	   g2.translate(20, 5);
	   g2.fill(new Ellipse2D.Double(0, 0, 30, 20));
   }

	/*
    * Create new list of events when recording is started
    */
   public void record() { 
      try {
         oldObjectTransform = new AffineTransform(objectTransform); 
         out = new ObjectOutputStream(new FileOutputStream(RECORD_FILE)); 
         isRecording = true;
         events = new ArrayList<MouseEvent>();
      } catch (IOException e) {
         System.out.println("Unable to open output stream for " + RECORD_FILE); 
      }  
   }
   
   /*
    * Before to stop recording write arraylist of recorded events in file 
    */
   public void stop() {
      if (isRecording) {
         try {  
            out.writeObject(events);
            objectTransform = new AffineTransform(oldObjectTransform); 
            isRecording = false; 
            out.close();
         } catch (IOException e) {
            System.out.println("Unable to write record file "); 
         }  
      }
   }

   /*
    * Stop recording before playing back, read in and process events by
    * calling back appropriate methods
    */ 
   public void play() { 
      stop();

      // Starting a new Thread so replay can be interrupted
      // However the replay is not done on the EDT (see below println)
      // (Enclosing replay() call in another Thread remove the interruption capabilities)
      new Thread( new Runnable() { 
	public void run() {

          try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(RECORD_FILE)); 
            ArrayList<MouseEvent> ev = (ArrayList<MouseEvent>) in.readObject();
            in.close(); 
            System.out.println("Is EDT? " + SwingUtilities.isEventDispatchThread());
   	    replay(ev);

          } catch (IOException exp) {
            System.out.println("Unable to open file: " + RECORD_FILE); 
          } catch (ClassNotFoundException exp2) {
            System.out.println(exp2); 
          }

	}
      }).start(); 

   }

   
    private void replay(ArrayList<MouseEvent> events) {

       for (MouseEvent e: events) {
          
          switch(e.getID()) {
             case MouseEvent.MOUSE_PRESSED:
                  mousePressed(e);
                  break;
             case MouseEvent.MOUSE_DRAGGED:
                  mouseDragged(e);
                  break;
            }

            // While a call to repaint() exists in mouseDragged(e), it is
            // essential to force a synchronous paint by calling paintImmediately
            // (with repaint() only paint requests are coalesced: teleportation effect)
             
            paintImmediately(this.getBounds());

            // To slow down by a constant time the re-execution of events.
            // Would be better to use event timestamps to sleep appropriately but not required. 
            try { 
	      Thread.sleep(40);  
	    } catch(InterruptedException ex) {
	      Thread.currentThread().interrupt();
	    }  
      } 
   }
   

   /*
    * Checks which button is pressed and if any objects have been selected.
    */
   public void mousePressed(MouseEvent e) {
      
      if (isRecording)
         events.add(e);
      
//	   System.out.println("mouse pressed");

      if (e.getButton() == MouseEvent.BUTTON1) {
         lastX = e.getX();
         lastY = e.getY();
//         System.out.println("mouse " + e.getX() + " " +  e.getY());
         pickUpdate(e.getX(), e.getY());
   	  angleSpoonCont = 0;
	      if(spoonSelected){
	    	  if(spoon.filled && angleSpoon < 0.55){
	    		  spoon.filled = false;
	    		  dirtDropXY.add(e);
	    	  }
	      }
//         System.out.println("Left mouse");
      }
      else if(e.getButton() == MouseEvent.BUTTON3 ){
    	  
//		   System.out.println("Right mouse");
		   
		   transform.get(1).scale(-1.00, 1.00);
		   invert = !invert;
	   }
      repaint();  
      
   }
   
   /*
    * If mouse wheel is scrolled then extend the arm.
    */
   @Override
   public void mouseWheelMoved(MouseWheelEvent e) {
	  int notches = e.getWheelRotation();
//	  System.out.println("notches: " + e.getWheelRotation());
	  if((_C.rect.width < 50 && notches < 0) || (_C.rect.width > 200 && notches >0)){
		  notches = 0;
	  }
	  _C.rect.width += notches;
	  _C.notch.translate(notches, 0);
      repaint();
   }

   /*
    * When the digger is selected and dragged, then the selected 
    * digger part performs the appropriate action.
    */
   
   public void mouseDragged(MouseEvent e){
      if (isRecording)
         events.add(e);

      AffineTransform trans = new AffineTransform();
      if(diggerSelected || bSelected){
        trans = AffineTransform.getTranslateInstance(e.getX() - lastX,e.getY() - lastY);
        if(e.getY()>300 && e.getY()<620 &&e.getX()>0 && e.getX()<670){
        transform.get(0).concatenate(trans);
        }
      }
      else if(cSelected){
    	  //get current x & y points and convert to shape C's coordinate system
    	  Point2D pInvCurrent = new Point2D.Double(e.getX(), e.getY());
    	  Point2D pInvLast = new Point2D.Double(lastX, lastY);
    	  AffineTransform inv = null;
    	  
    	  for(int i=0; i<=3; i++){
        	  try {
        		  inv = transform.get(i).createInverse();
        	  } catch (NoninvertibleTransformException eX) {
        		  return;
        	  }    	  
        	  pInvCurrent = inv.transform(pInvCurrent, null);
        	  pInvLast = inv.transform(pInvLast, null);
    	  }
		  
//		  System.out.println("point: " + pInvCurrent.toString() + " " + pInvLast.toString());
    	  
    	  //Use points to get angle between them to check if digger part C can rotate
    	  double currentAngle = Math.atan2(pInvCurrent.getY(), pInvCurrent.getX());
    	  double lastAngle = Math.atan2(pInvLast.getY(), pInvLast.getX());
//    	  System.out.println("rotate:" + currentAngle);
    	  trans = AffineTransform.getRotateInstance(currentAngle - lastAngle);
 
    	  float temp = angleC;
    	  temp += currentAngle - lastAngle;
    	  if(temp > -Math.PI/4 && temp < Math.PI/12 && _C.rect.contains(pInvCurrent.getX(),pInvCurrent.getY())){
    		  angleC += currentAngle - lastAngle;
//        	  System.out.println(" angleC: " +angleC);
        	  transform.get(3).concatenate(trans);
    	  }else{
    		  System.out.println("OVER THE REQUIRED ANGLE");
    	  }
    	  
    	 
      }
      else if(armSelected){
    	  AffineTransform inv = null;
    	  Point2D p2 = new Point2D.Double(0,0);
    	  //check if digger inverted
    	  try {
    		  if(invert){
    			  inv = _C.notch;
    		  }
    		  else{
    			  inv = _C.notch.createInverse();
    		  }
    	  } catch (NoninvertibleTransformException e2) {
    		  return;
    	  }
    	  p2 = inv.transform(p2, null);
    	  double tempangleC;
    	  if(invert){
    		  tempangleC = angleC*-1;
    	  }else{
    		  tempangleC = angleC;
    	  }
    	  double y = p2.getX()*Math.sin(tempangleC);
    	  double x = p2.getX()*Math.cos(tempangleC);

    	  //get current x & y points and convert to Arm's coordinate system
    	  Point2D pInvCurrent = new Point2D.Double(e.getX() + x, e.getY() + y);
    	  Point2D pInvLast = new Point2D.Double(lastX + x, lastY + y);
    	  
    	  for(int i=0; i<=4; i++){
        	  try {
        		  inv = transform.get(i).createInverse();
        	  } catch (NoninvertibleTransformException eX) {
        		  return;
        	  }    	  
        	  pInvCurrent = inv.transform(pInvCurrent, null);
        	  pInvLast = inv.transform(pInvLast, null);
    	  }
    	  
    	  //Use points to get angle between them to check if digger Arm can rotate
    	  double currentAngle = Math.atan2(pInvCurrent.getY(), pInvCurrent.getX());
    	  double lastAngle = Math.atan2(pInvLast.getY(), pInvLast.getX());
//    	  System.out.println("rotate:" + (currentAngle - lastAngle) + " angleArm: " + angleArm);
    	  trans = AffineTransform.getRotateInstance(currentAngle - lastAngle);
    	  
    	  float temp = angleArm;
    	  temp += currentAngle - lastAngle;
    	  if(temp > -Math.PI/2 && temp < 0 && arm.contains(pInvCurrent.getX(),pInvCurrent.getY())){
    		  angleArm += currentAngle - lastAngle;
//        	  System.out.println(" angleArm: " +angleArm);
        	  transform.get(4).concatenate(trans);
    	  }else{
    		  System.out.println("OVER THE REQUIRED ANGLE");
    		  
    	  }
    	  
      }
      else if(spoonSelected){
    	  AffineTransform inv = null;

    	  Point2D p2 = new Point2D.Double(0,0);
    	  //check if digger inverted
    	  try {
    		  if(invert){
    			  inv = _C.notch;
    		  }
    		  else{
    			  inv = _C.notch.createInverse();
    		  }
    	  } catch (NoninvertibleTransformException e2) {
    		  return;
    	  }
    	  p2 = inv.transform(p2, null);
    	  double tempangleC;
    	  if(invert){
    		  tempangleC = angleC*-1;
    	  }else{
    		  tempangleC = angleC;
    	  }
    	  double y = p2.getX()*Math.sin(tempangleC);
    	  double x = p2.getX()*Math.cos(tempangleC);

    	  //get current x & y points and convert to shape Spoon's coordinate system
    	  Point2D pInvCurrent = new Point2D.Double(e.getX() + x, e.getY() + y);
    	  Point2D pInvLast = new Point2D.Double(lastX + x, lastY + y);
    	  
    	  for(int i=0; i<=5; i++){
        	  try {
        		  inv = transform.get(i).createInverse();
        	  } catch (NoninvertibleTransformException eX) {
        		  return;
        	  }    	  
        	  pInvCurrent = inv.transform(pInvCurrent, null);
        	  pInvLast = inv.transform(pInvLast, null);
    	  }

//		  System.out.println("point: " + pInvCurrent.toString() + " " + pInvLast.toString());
    	  
    	  //Use points to get angle between them to check if digger Arm can rotate
    	  double currentAngle = Math.atan2(pInvCurrent.getY(), pInvCurrent.getX());
    	  double lastAngle = Math.atan2(pInvLast.getY(), pInvLast.getX());
//    	  System.out.println("rotate:" + currentAngle);
    	  trans = AffineTransform.getRotateInstance(currentAngle - lastAngle);
    	  
    	  float temp = angleSpoon;
    	  temp += currentAngle - lastAngle;
    	  if(temp > Math.PI/6 && temp < Math.PI && spoon.contains(pInvCurrent.getX(),pInvCurrent.getY())){
    		  angleSpoon += currentAngle - lastAngle;
    		  angleSpoonCont += angleSpoon;
//        	  System.out.println(" angleSpoon: " +angleSpoon);
        	  transform.get(5).concatenate(trans);
    	  }else{
    		  System.out.println("OVER THE REQUIRED ANGLE");
    		  
    	  }
      }
      repaint();
      lastX = e.getX();
      lastY = e.getY();
   }
   
   private void deselectAll() {
	      diggerSelected = false;
	      bSelected = false;
	      cSelected = false;
	      armSelected = false;
	      spoonSelected = false;
   }

   /**
    * Check if click was inside of any of the digger's parts
    * @param eX mouse click event's x coordinate
    * @param eY mouse click event's y coordinate
    * @param level digger's shape level
    * @param shape digger part
    * @return
    */
	  private boolean insideObject(int eX, int eY, int level, Shape shape) {
	    AffineTransform inv = null;
	    Point2D p;
	    double x = 0;
	    double y = 0;
	    if(level == 4 || level == 5){
//	    	System.out.println("angleSpoon: " + angleSpoon + " angleSoonCont: " + angleSpoonCont);
	    	Point2D p2 = new Point2D.Double(0,0);
	    	try {
	    		if(invert){
	    			inv = _C.notch;
	    		}
	    		else{
		    		inv = _C.notch.createInverse();
	    		}
	    	} catch (NoninvertibleTransformException e) {
	    		return false;
	    	}
	    	p2 = inv.transform(p2, null);
	    	double tempangleC;
	    	if(invert){
	    		tempangleC = angleC*-1;
	    	}else{
	    		tempangleC = angleC;
	    	}
	    	y = p2.getX()*Math.sin(tempangleC);
	    	x = p2.getX()*Math.cos(tempangleC);
//	    	System.out.println("x " + x + " y: " + y);
	    }
	    p = new Point2D.Double(eX + x, eY + y);

	    try {
		  inv = objectTransform.createInverse();
		} catch (NoninvertibleTransformException e) {
		  return false;
		}
	    Point2D pInv = inv.transform(p, null);

	    for(int i=1; i<=level; i++){
		    try {
			      inv = transform.get(i).createInverse();
			    } catch (NoninvertibleTransformException e) {
			      return false;
			    }
			    pInv = inv.transform(pInv, null);
	    }
	    if(level == 3){
	    	return _C.rect.contains(pInv);
	    }
	    point = pInv;
	    return shape.contains(pInv);
	  }
	  
	  /**
	   * Helper function to check if point x & y is inside digger's parts
	   * @param eX mouse click event X coordinate
	   * @param eY mouse click event Y coordinate
	   */
	  private void pickUpdate(int eX, int eY) {
	      deselectAll();
	      diggerSelected = insideObject(eX, eY, 1, displayRoot);
	      displayRoot.point = point;
	      bSelected = insideObject(eX, eY, 2, _B);
	      _B.point = point;
	      cSelected = insideObject(eX, eY, 3, _C);
	      _C.point = point;
	      armSelected = insideObject(eX, eY, 4, arm);
	      arm.point = point;
	      spoonSelected = insideObject(eX, eY, 5, spoon);
	      spoon.point = point;

//	      System.out.println("digger: " + diggerSelected);
//	      System.out.println("b: " + bSelected);
//	      System.out.println("c: " + cSelected);
//	      System.out.println("arm: " + armSelected);
//	      System.out.println("spoon: " + spoonSelected);
	  }

/*
    * When mouse is released (any button, so also for button1), nothing
    * becomes selected
    */
   public void mouseReleased(MouseEvent e) {
//      selected = NONE;
//	   System.out.println("ANGLESPOONCOUNT: " + angleSpoonCont + " angleSpoon: " + angleSpoon);
	   if(spoonSelected && angleSpoonCont >= 75 && !spoon.filled){
		   spoon.filled = true;
		   dirtDigXY.add(e);
	   }
	   deselectAll();
   }

public void mouseMoved(MouseEvent e){}
   public void mouseClicked(MouseEvent e){}
   public void mouseExited(MouseEvent e){}
   public void mouseEntered(MouseEvent e){}
   
}


