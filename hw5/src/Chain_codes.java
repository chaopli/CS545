import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

 
public class Chain_codes implements PlugInFilter{
	 
  ImagePlus origImage = null;
  String origTitle = null;
  static boolean verbose = true;
 
  public int setup(String arg, ImagePlus im){
	  
	  origImage = im;
	  origTitle = im.getTitle();
	  RegionLabeling.setVerbose(verbose);
	  return DOES_8G + NO_CHANGES;
  }

  int coder(Point old, Point now){
	  int direction = 0;
	  int[][] vector = {{ 1,0}, { 1, 1}, {0, 1}, {-1, 1}, {-1,0}, {-1,-1}, {0,-1}, { 1,-1}};
	  int x=now.x-old.x;
	  int y=now.y-old.y;
	  for(int i=1;i<8;i++){
		  if(x==vector[i][0] && y==vector[i][1]){
			   direction = i;
			   break;
		  }
	  }
	  return direction;
}
  Point decoder(Point now, int dir){
	   Point next=now;
	   int[][] vector = {{ 1,0}, { 1, 1}, {0, 1}, {-1, 1}, {-1,0}, {-1,-1}, {0,-1}, { 1,-1}};
	   next.x=now.x+vector[dir][0];
	   next.y=now.y+vector[dir][1];	   
	   return next; 
  }
  
public void run(ImageProcessor ip) {
	  ImageProcessor ip2 = ip.duplicate();
	  int w=ip.getWidth();
	  int h=ip.getHeight();
  	  ByteProcessor ip3=new ByteProcessor(w,h);
  	  ContourTracer tracer = new ContourTracer(ip2);
  	  List<Contour> outerContours = tracer.getOuterContours();
  	  List<Contour> innerContours = tracer.getInnerContours();
  	  List<List> Chain_code=new ArrayList<List>();
  	  List<Point> startpoints = new ArrayList<Point>();
  	  while(!outerContours.isEmpty()) {	
  		  List<Integer> Chain=new ArrayList<Integer>();
  		  Contour n =outerContours.remove(0);
  		  List<Point> p=n.points;
  		  Point outerstart=p.remove(0);
  		  Point old=outerstart;
  		  startpoints.add(outerstart);
  		  while(!p.isEmpty()){
  			  Point now=p.remove(0);
  			  int m=coder(old,now);
  			  Chain.add(m);
  			  old=now;
  		  }
  		  Chain_code.add(Chain);
  	  }
  	  while(!innerContours.isEmpty()){	
  		  List<Integer> Chain=new ArrayList<Integer>();
  		  Contour n =innerContours.remove(0);
  		  List<Point> p=n.points;
  		  Point innerstart=p.remove(0);
  		  Point old=innerstart;
  		  startpoints.add(innerstart);
  		  while(!p.isEmpty()){
  			  Point now=p.remove(0);
  			  int m=coder(old,now);
  			  Chain.add(m);
  			  old=now;
  		  }
  		  Chain_code.add(Chain);	 
  	  }
 
  	  for(int i=0; i<startpoints.size();i++){
  		  Point p=startpoints.get(i);
  		  ip3.putPixel(p.x, p.y, 255);
  		  List<Integer> Chain=Chain_code.get(i);
  		  for(int j=0;j<Chain.size();j++){
  			  int dir=Chain.get(j);
  			  Point next=decoder(p,dir);
  			  ip3.putPixel(next.x, next.y, 255);
  			  p=next;
  		  }
  	  }
  	  ImagePlus im3=new ImagePlus("contour", ip3);
  	  im3.show();   
  	} // end of class Contour_Tracing_Plugin
}
 
class Contour {
    static int INITIAL_SIZE = 50;
    int label;
    public List<Point> points;

 	Contour (int label, int size) {
 		this.label = label;
 		points = new ArrayList<Point>(size);
 	}
 	Contour (int label) {
 		this.label = label;
 		points = new ArrayList<Point>(INITIAL_SIZE);
 	}
 	void addPoint (Point n) {
 		points.add(n);
 	}
 	Shape makePolygon() {
 		int m = points.size();
 		if (m>1) {
 			int[] xPoints = new int[m];
 			int[] yPoints = new int[m];
 			int k = 0;
 			Iterator<Point> itr = points.iterator();
 			while (itr.hasNext() && k < m) {
 				Point cpt = itr.next();
 				xPoints[k] = cpt.x;
 				yPoints[k] = cpt.y;
 				k = k + 1;
 			}
 			return new Polygon(xPoints, yPoints, m);
 		}
 		else { // use circles for isolated pixels
 			Point cpt = points.get(0);
 			return new Ellipse2D.Double
 					(cpt.x-0.1, cpt.y-0.1, 0.2, 0.2);
 		}
 	}
 	static Shape[] makePolygons(List<Contour> contours) {
 		if (contours == null)
 			return null;
 		else {
 			Shape[] pa = new Shape[contours.size()];
 			int i = 0;
 			for (Contour c: contours) {
 				pa[i] = c.makePolygon();
 				i = i + 1;
 			}
 			return pa;
 		}
 	}	
 	void moveBy (int dx, int dy) {
 		for (Point pt: points) {
 			pt.translate(dx,dy);
 		}
 	}
 
 	static void moveContoursBy
 			(List<Contour> contours, int dx, int dy) {
 		for (Contour c: contours) {
 			c.moveBy(dx, dy);
 		}
 	}

 } // end of class Contour
 
 class ContourOverlay extends ImageCanvas {
	 private static final long serialVersionUID = 1L;
	 static float strokeWidth = 0.5f;
	 static int capsstyle = BasicStroke.CAP_ROUND;
	 static int joinstyle = BasicStroke.JOIN_ROUND;
	 static Color outerColor = Color.black;
	 static Color innerColor = Color.white;
	 static float[] outerDashing = {strokeWidth * 2.0f,
		 strokeWidth * 2.5f};
	 static float[] innerDashing = {strokeWidth * 0.5f,
		 strokeWidth * 2.5f};
	 static boolean DRAW_CONTOURS = true;

	 Shape[] outerContourShapes = null;
	 Shape[] innerContourShapes = null;

	 public ContourOverlay(ImagePlus im,
			 List<Contour> outerCs, List<Contour> innerCs)
	 {
		 super(im);
		 if (outerCs != null)
			 outerContourShapes = Contour.makePolygons(outerCs);
		 if (innerCs != null)
			 innerContourShapes = Contour.makePolygons(innerCs);
	 }

	 public void paint(Graphics g) {
		 super.paint(g);
		 drawContours(g);
	 }
	  private void drawContours(Graphics g) {
		  Graphics2D g2d = (Graphics2D) g;
		  g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				  RenderingHints.VALUE_ANTIALIAS_ON);
		 
		  // scale and move overlay to the pixel centers
		 double mag = this.getMagnification();
		 g2d.scale(mag, mag);
		 g2d.translate(0.5-this.srcRect.x, 0.5-this.srcRect.y);
		 
		  if (DRAW_CONTOURS) {
			  Stroke solidStroke = new BasicStroke
					  (strokeWidth, capsstyle, joinstyle);
			  Stroke dashedStrokeOuter = new BasicStroke
					  (strokeWidth, capsstyle, joinstyle, 1.0f,
							  outerDashing, 0.0f);
			  Stroke dashedStrokeInner = new BasicStroke
					  (strokeWidth, capsstyle, joinstyle, 1.0f,
							  innerDashing, 0.0f);
		 
			  if (outerContourShapes != null)
				  drawShapes(outerContourShapes, g2d, solidStroke,
						  dashedStrokeOuter, outerColor);
			  if (innerContourShapes != null)
				  drawShapes(innerContourShapes, g2d, solidStroke,
						  dashedStrokeInner, innerColor);
		  }
		}
		 
		  void drawShapes(Shape[] shapes, Graphics2D g2d,
				  Stroke solidStrk, Stroke dashedStrk, Color col) {
			  g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					  RenderingHints.VALUE_ANTIALIAS_ON);
		  g2d.setColor(col);
		  for (int i = 0; i < shapes.length; i++) {
			  Shape s = shapes[i];
			  if (s instanceof Polygon)
				  g2d.setStroke(dashedStrk);
			  else
				  g2d.setStroke(solidStrk);
			  g2d.draw(s);
		  }
		 }
		 
	} // end of class ContourOverlay
 
 class ContourTracer {
	 static final byte FOREGROUND = 1;
	 static final byte BACKGROUND = 0;
	 static boolean beVerbose = true;
	 
	 List<Contour> outerContours = null;
	 List<Contour> innerContours = null;
	 List<BinaryRegion> allRegions = null;
	 int regionId = 0;
	 
	 ImageProcessor ip = null;
	 int width;
	 int height;
	 byte[][] pixelArray;
	 int[][] labelArray;
	 

	 public ContourTracer (ImageProcessor ip) {
		 this.ip = ip;
		 this.width = ip.getWidth();
		 this.height = ip.getHeight();
		 makeAuxArrays();
		 findAllContours();
		 collectRegions();
	 }
	 
	 public static void setVerbose(boolean verbose) {
		 beVerbose = verbose;
	 }
	 
	 public List<Contour> getOuterContours() {
		 return outerContours;
	 }
	 
	 public List<Contour> getInnerContours() {
		 return innerContours;
	 }
	 
	 public List<BinaryRegion> getRegions() {
		 return allRegions;
	 }
	 
	  // nonpublic methods
	 
	  void makeAuxArrays() {
		  int h = ip.getHeight();
		  int w = ip.getWidth();
		  pixelArray = new byte[h+2][w+2];
		  labelArray = new int[h+2][w+2];
	   // initialize auxiliary arrays
		  for (int v = 0; v < h+2; v++) {
			  for (int u = 0; u < w+2; u++) {
				  if (ip.getPixel(u-1,v-1) == 0)
					  pixelArray[v][u] = BACKGROUND;
				  else
					  pixelArray[v][u] = FOREGROUND;
			  }
		  }
	   	}
	  
	   Contour traceOuterContour (int cx, int cy, int label) {
		   Contour cont = new Contour(label);
		   traceContour(cx, cy, label, 0, cont);
		   return cont;
	   }
	  
	   Contour traceInnerContour(int cx, int cy, int label) {
		   Contour cont = new Contour(label);
		   traceContour(cx, cy, label, 1, cont);
		   return cont;
	   }
	  
	   // trace one contour starting at ( xS,yS) in direction dS
	  Contour traceContour (int xS, int yS, int label, int dS,
			  Contour cont) {
		  int xT, yT; // T = successor of starting point ( xS,yS)
		  int xP, yP; // P = previous contour point
		  int xC, yC; // C = current contour point
		  Point pt = new Point(xS, yS);
		  int dNext = findNextPoint(pt, dS);
		  cont.addPoint(pt);
		  xP = xS; yP = yS;
		  xC = xT = pt.x;
		  yC = yT = pt.y;
	  
		  boolean done = (xS==xT && yS==yT); // true if isolated pixel
	  
		  while (!done) {
			  labelArray[yC][xC] = label;
			  pt = new Point(xC, yC);
			  int dSearch = (dNext + 6) % 8;
			  dNext = findNextPoint(pt, dSearch);
			  xP = xC; yP = yC;
			  xC = pt.x; yC = pt.y;
	   // are we back at the starting position?
			  done = (xP==xS && yP==yS && xC==xT && yC==yT);
			  if (!done) {
				  cont.addPoint(pt);
			  		}
		    	}
		    	return cont;
		    }
		   
		    int findNextPoint (Point pt, int dir) {
		    // starts at Point pt in direction dir, returns the
		    // final tracing direction, and modifies pt
		    	final int[][] vector = {
		    			{ 1,0}, { 1, 1}, {0, 1}, {-1, 1},
		    			{-1,0}, {-1,-1}, {0,-1}, { 1,-1}};
		    	for (int i = 0; i < 7; i++) {
		    		int x = pt.x + vector[dir][0];
		    		int y = pt.y + vector[dir][1];
		    		if (pixelArray[y][x] == BACKGROUND) {
		    // mark surrounding background pixels
		    			labelArray[y][x] = -1;
		    			dir = (dir + 1) % 8;
		    		}
		    else { // found a nonbackground pixel
		    	pt.x = x; pt.y = y;
		    	break;
		    }
		   }
		    return dir;
		  }
		   
		  void findAllContours() {
		    outerContours = new ArrayList<Contour>(50);
		    innerContours = new ArrayList<Contour>(50);
		    int label = 0; // current label
		   
		    // scan top to bottom, left to right
		    for (int v = 1; v < pixelArray.length-1; v++) {
		    	label = 0; // no label
		    	for (int u = 1; u < pixelArray[v].length-1; u++) {
		   
		    		if (pixelArray[v][u] == FOREGROUND) {
		    			if (label != 0) { // keep using the same label
		    				labelArray[v][u] = label;
		    			}
		    			else {
		    				label = labelArray[v][u];
		    				if (label == 0) {
		    // unlabeled—new outer contour
		    					regionId = regionId + 1;
		    					label = regionId;
		    					Contour oc = traceOuterContour(u, v, label);
		    					outerContours.add(oc);
		    					labelArray[v][u] = label;
		    				}
		    			}
		    		}
		    		else { // background pixel
		    			if (label != 0) {
		    				if (labelArray[v][u] == 0) {
		    					// unlabeled—new inner contour
		    					Contour ic = traceInnerContour(u-1, v, label);
		    					innerContours.add(ic);
		    				}
		    				label = 0;
		    			}
		    		}
		    	}
		    }
		    // shift back to original coordinates
		     Contour.moveContoursBy (outerContours, -1, -1);
		     Contour.moveContoursBy (innerContours, -1, -1);
		   }
		    
		   
		    // creates a container of BinaryRegion objects
		    // collects the region pixels from the label image
		    // and computes the statistics for each region
		    void collectRegions() {
		    	int maxLabel = this.regionId;
		    	int startLabel = 1;
		    	BinaryRegion[] regionArray =
		    			new BinaryRegion[maxLabel + 1];
		    	for (int i = startLabel; i <= maxLabel; i++) {
		    		regionArray[i] = new BinaryRegion(i);
		    	}
		    	for (int v = 0; v < height; v++) {
		    		for (int u = 0; u < width; u++) {
		    			int lb = labelArray[v][u];
		    			if (lb >= startLabel && lb <= maxLabel
		    					&& regionArray[lb]!=null) {
		    				regionArray[lb].addPixel(u, v);
		    			}
		    		}
		    	}
		    
		    	// create a list of regions to return, collect nonempty regions
		    	List<BinaryRegion> regionList =
		    			new LinkedList<BinaryRegion>();
		    	for (BinaryRegion r: regionArray) {
		    		if (r != null && r.getSize()>0) {
		    			r.update(); // compute the statistics for this region
		    			regionList.add(r);
		    		}
		    	}
		    	allRegions = regionList;
		    }
		     
 	}// end of class ContourTracer
 
 	class BinaryRegion {
 		int label;
 		int numberOfPixels = 0;
 		double xc = Double.NaN;
 		double yc = Double.NaN;
 		int left = Integer.MAX_VALUE;
 		int right = -1;
 		int top = Integer.MAX_VALUE;
 		int bottom = -1;

 		int x_sum = 0;
 		int y_sum = 0;
 		int x2_sum = 0;
 		int y2_sum = 0;

 		public BinaryRegion(int id){
 			this.label = id;
 		}

 		public int getSize() {
 			return this.numberOfPixels;
 		}
	 
 		public Rectangle getBoundingBox() {
 			if (left == Integer.MAX_VALUE)
 				return null;
 			else
 				return new Rectangle
 						(left, top, right-left+1, bottom-top+1);
 		}
	 
	  public Point2D.Double getCenter(){
		  if (Double.isNaN(xc))
			  return null;
		  else
			  return new Point2D.Double(xc, yc);
	  }
	 
	  public void addPixel(int x, int y){
		  numberOfPixels = numberOfPixels + 1;
		  x_sum = x_sum + x;
		  y_sum = y_sum + y;
		  x2_sum = x2_sum + x*x;
		  y2_sum = y2_sum + y*y;
		  if (x<left) left = x;
		  if (y<top) top = y;
		  if (x>right) right = x;
		  if (y>bottom) bottom = y;
	  	}
	 
	  	public void update(){
	  		if (numberOfPixels > 0){
	  			xc = x_sum / numberOfPixels;
	  			yc = y_sum / numberOfPixels;
	  		}
	  	}
	 
	  } // end of class BinaryRegion
 
 
 abstract class RegionLabeling {
     
     static boolean doDisplay = false;
     static boolean beVerbose = false;

     final int BACKGROUND = 0;
     final int FOREGROUND = 1;
     final int START_LABEL = 2;
     int[] labels = null;
     int width;
     int height;
     private int currentLabel;
     private int maxLabel;   // the maximum label in the labels array
     List<BinaryRegion> regions = null;

     RegionLabeling(ImageProcessor ip) {
             width  = ip.getWidth();
             height = ip.getHeight();
             makeLabelArray(ip);
             //showLabelArray();
             if (labels == null)
                     IJ.error("RegionLabeling: could not create labels array");
             applyLabeling();
             collectRegions();
     }
     
     void makeLabelArray(ImageProcessor ip) {
             if (beVerbose)
                     IJ.log("makeLabelArray()");
             // set all pixels to FOREGROUND or BACKGROUND (thresholding)
             labels = new int[width * height];
             for (int v = 0; v < height; v++) {
                     for (int u = 0; u < width; u++) {
                             int p = ip.getPixel(u, v);
                             if (p > 0)
                                     labels[v * width + u] = FOREGROUND;
                             else
                                     labels[v * width + u] = BACKGROUND;
                     }
             }
     }
     
     /* Must be implemented by the sub-classes:
      */
     abstract void applyLabeling();
     
     /* Creates a container of BinaryRegion objects, then collects the regions'
      * coordinates by scanning the "labels" array.
      */
     void collectRegions() {
             if (beVerbose) IJ.log("makeRegions()");
             // create an array of BinaryRegion objects
             BinaryRegion[] regionArray = new BinaryRegion[maxLabel + 1];
             for (int i = START_LABEL; i <= maxLabel; i++) {
                     regionArray[i] = new BinaryRegion(i);
             }
             // scan the labels array and collect the coordinates for each region
             for (int v = 0; v < height; v++) {
                     for (int u = 0; u < width; u++) {
                             int lb = labels[v * width + u];
                             if (lb >= START_LABEL && lb <= maxLabel && regionArray[lb]!=null) {
                                     regionArray[lb].addPixel(u,v);
                             }
                     }
             }
             
             // collect all nonempty regions and create the final list of regions
             List<BinaryRegion> regionList = new ArrayList<BinaryRegion>();
             for (BinaryRegion r: regionArray) {
                     if (r != null && r.getSize() > 0) {
                             r.update();     // calculate the statistics for this region
                             regionList.add(r);
                     }
             }
             regions = regionList;
     }
     
     boolean isForeground(int u, int v) {
             return (labels[v * width + u] == FOREGROUND);
     }
     
     void resetLabel() {
             currentLabel = -1;
             maxLabel = -1;
     }
     
     int getNextLabel() {
             if (currentLabel < 1)
                     currentLabel = START_LABEL;
             else
                     currentLabel = currentLabel + 1;
             maxLabel = currentLabel;
             if (beVerbose) 
                     IJ.log("  new label: "+ currentLabel + " / max label: " + maxLabel);
             return currentLabel;
     }
     
     void setLabel(int u, int v, int label) {
             if (u >= 0 && u < width && v >= 0 && v < height)
                     labels[v * width + u] = label;
     }
     
     // public methods ------------------------------------

     public static void setDisplay(boolean display) {
             doDisplay = display;
     }
     
     public static void setVerbose(boolean verbose) {
             beVerbose = verbose;
     }
     
     public List<BinaryRegion> getRegions() {
             return regions;
     }
     
     public int getMaxLabel() {
             return maxLabel;
     }
     
     public int getLabel(int u, int v) {
             if (u >= 0 && u < width && v >= 0 && v < height)
                     return labels[v * width + u];
             else
                     return BACKGROUND;
     }
     
     public boolean isLabel(int u, int v) {
             return (labels[v * width + u] >= START_LABEL);
     }
     
     public ImageProcessor makeLabelImage(boolean color) {
             if (color)
                     return makeRandomColorImage();
             else
                     return makeGrayImage();
     }
     
     public FloatProcessor makeGrayImage() {
             FloatProcessor ip = new FloatProcessor(width, height, labels);
             ip.resetMinAndMax();
             return ip;
     }


     public ColorProcessor makeRandomColorImage() {
             int[] colorLUT = new int[maxLabel+1];

             for (int i = START_LABEL; i <= maxLabel; i++) {
                     colorLUT[i] = makeRandomColor();
             }

             ColorProcessor cp = new ColorProcessor(width, height);
             int[] colPix = (int[]) cp.getPixels();

             for (int i = 0; i < labels.length; i++) {
                     if (labels[i]>=0 && labels[i]<colorLUT.length)
                             colPix[i] = colorLUT[labels[i]];
                     else
                             throw new Error("illegal label = "+labels[i]);
             }
             return cp;
     }
     
     public void printSummary() {
             if (regions != null && regions.size() > 0) {
                     IJ.log("Summary: number of regions = " + regions.size());
                     for (BinaryRegion r : regions) {
                             IJ.log(r.toString());
                     }
             } else
                     IJ.log("Summary: no regions found.");
     }
     
     // local utility methods -----------------------------------
     
     void showLabelArray() {
             ImageProcessor ip = new FloatProcessor(width, height, labels);
             ip.resetMinAndMax();
             ImagePlus im = new ImagePlus("Label Array",ip);
             im.show();
     }

     int makeRandomColor() {
             double saturation = 0.2;
             double brightness = 0.2;
             float h = (float) Math.random();
             float s = (float) (saturation * Math.random() + 1 - saturation);
             float b = (float) (brightness * Math.random() + 1 - brightness);
             return Color.HSBtoRGB(h, s, b);
     }
     
     void snooze(int time) {
             if (time > 0) {
                     try {
                             Thread.sleep(time);
                     } catch (InterruptedException e) {
                             e.printStackTrace();
                     }
             }
     }

}
 
  