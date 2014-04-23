import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class Chain_codes implements PlugInFilter {

	ImageProcessor ip;
	ImageProcessor src;
	int width;
	int height;
	int label = 0;
	Vector<Vector<Code>> codes;
	@Override
	public void run(ImageProcessor arg0) {
		// TODO Auto-generated method stub
		ip = arg0.duplicate();
		src = arg0.duplicate();
		codes = new Vector<Vector<Code> >();
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				if ((ip.getPixel(x, y) == 0) && (ip.getPixel(x-1, y) == 255))
				{
					encode(x, y, label);
					label += 10;
				}
			}
		}
	}

	@Override
	public int setup(String arg0, ImagePlus arg1) {
		// TODO Auto-generated method stub
		return DOES_ALL;
	}
	
	public void encode(int u, int v, int label)
	{
		Vector<Code> cr = new Vector<Code>();
		while(true)
		{
			Code c = new Code();
			if (ip.getPixel(u-1, v) == 0)
			{
				c.deltau = -1;
				c.deltav =  0;
			}
			else if (ip.getPixel(u-1, v) == 0)
			{
				
			}
		}
	}

}

class Code {
	int deltau;
	int deltav;
	
	public Code()
	{
		deltau = 0;
		deltav = 0;
	}
}

class Contour {
	static int INITIAL_SIZE = 50;
	int label;
	List<Point> points;
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
	            k=k+1;
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
	            i=i+1;
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
 
}// end of classContour


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

}// end of classBinaryRegion


