import java.util.Stack;
import java.util.Vector;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class Region_labeling implements PlugInFilter {
	int m = 1;
	int tmpm = 1;
	ImageProcessor ip;
	ImageProcessor sc;
	ImageProcessor tmp;
	int width;
	int height;
	Vector<Node> centroid;
	@Override
	public void run(ImageProcessor arg) {
		// TODO Auto-generated method stub
		ip = arg.duplicate();
		sc = arg.duplicate();
		width = ip.getWidth();
		height = ip.getHeight();
		tmp = sc.duplicate();
		int n = 0;
		centroid = new Vector<Node>();
		for (int y = 0; y < height; y++)
		{
			for (int x = 0;  x< width; x++)
			{
				int v = ip.getPixel(x, y);
				if (v == 0)
				{
					FloodFill(x, y, m);
//					CalcOrientationVector(x, y, m);
					m += 10;
					n++;
				}

			}
		}
		System.out.print(Integer.toString(n)+"\n");
		n = 0;
		for (int y = 0; y < height; y++)
		{
			for (int x = 0;  x< width; x++)
			{
				int v = tmp.getPixel(x, y);
				if (v == 0)
				{
					CalcOrientationVector(x, y, tmpm, n);
					tmpm += 10;
					n++;
				}
			}
		}
		System.out.print(Integer.toString(n)+"\n");
		ImagePlus result = new ImagePlus("Result", ip);
		result.show();
	}
	@Override
	public int setup(String arg0, ImagePlus arg1) {
		// TODO Auto-generated method stub
		return DOES_ALL;
	}
	
	public void FloodFill(int u, int v, int lable)
	{
		 Stack<Node> s = new Stack<Node>();// stack
		 s.push(new Node(u, v));
		 int sumx = 0;
		 int sumy = 0;
		 int centroidx;
		 int centroidy;
		 int num = 0;
		 while (!s.isEmpty())
		 {
			 Node n = s.pop();
			 if ((n.x>=0) && (n.x<width) && (n.y>=0) && (n.y<height)
				 && ip.getPixel(n.x,n.y) == 0) 
			 {
				 num++;
				 sumx += n.x;
				 sumy += n.y;
				 ip.putPixel(n.x, n.y, lable);
				 s.push(new Node(n.x+1,n.y));
				 s.push(new Node(n.x,n.y+1));
				 s.push(new Node(n.x,n.y-1));
				 s.push(new Node(n.x-1,n.y));
			 }
		 }
		 
//		 new ImagePlus("222", ip).show();
		 if (num != 0)
		 {
			 centroidx = sumx/num;
			 centroidy = sumy/num;
			 Node n = new Node(centroidx, centroidy);
			 centroid.addElement(n);
		 }
	}
	
	public void CalcOrientationVector(int u, int v, int lable, int index)
	{
		 Stack<Node> s = new Stack<Node>();// stack
		 s.push(new Node(u, v));
		 double u11 = 0.0;
		 double u20 = 0.0;
		 double u02 = 0.0;
		 double A;
		 double B;
		 double xd;
		 double yd;
		 int num = 0;
		 double normu11;
		 double normu20;
		 double normu02;
		 double norm;
		 
		 while (!s.isEmpty())
		 {
			 Node n = s.pop();
			 if ((n.x>=0) && (n.x<width) && (n.y>=0) && (n.y<height)
				 && tmp.getPixel(n.x,n.y) == 0) 
			 {
				 num++;
				 
				 u11 += ((double)n.x-(double)centroid.get(index).x)*((double)n.y-(double)centroid.get(index).y);
				 u20 += Math.pow(((double)n.x-(double)centroid.get(index).x), 2);
				 u02 += Math.pow(((double)n.y-(double)centroid.get(index).y), 2);
				 tmp.putPixel(n.x, n.y, lable);
				 s.push(new Node(n.x+1,n.y));
				 s.push(new Node(n.x,n.y+1));
				 s.push(new Node(n.x,n.y-1));
				 s.push(new Node(n.x-1,n.y));
			 }
		 }
		 
		 
//		 new ImagePlus("111", tmp).show();
		 norm = num*num;
		 normu11 = u11/norm;
		 normu20 = u20/norm;
		 normu02 = u02/norm;
		 A = 2*u11;
		 B = u20 - u02;
		 
		 if (A == 0 && B == 0)
		 {
			 xd = 0;
			 yd = 0;
		 }
		 else if (A >= 0)
		 {
			 xd = Math.sqrt(0.5*(1+B/Math.sqrt(A*A+B*B)));
			 yd = Math.sqrt(0.5*(1-B/Math.sqrt(A*A+B*B)));
		 }
		 
		 else
		 {
			 xd =  Math.sqrt(0.5*(1+B/Math.sqrt(A*A+B*B)));
			 yd = -Math.sqrt(0.5*(1-B/Math.sqrt(A*A+B*B)));
		 }
		 
		 double lambda1 = (u20+u02+Math.sqrt((u20-u02)*(u20-u02)+4*u11*u11))/2;
		 double lambda2 = (u20+u02-Math.sqrt((u20-u02)*(u20-u02)+4*u11*u11))/2;
		 double Ecc = lambda1/lambda2;
		 double ra = 2*Math.sqrt(lambda1/num);
		 double rb = 2*Math.sqrt(lambda2/num);
		 double theta = 0.0;
		 if (A >= 0)
		 {
			 theta = -Math.acos(xd);
		 }
		 else
		 {
			 theta = Math.acos(xd);
		 }
		 
		 
		 
//		 Line line = new Line(centroidx, centroidy, xd*Ecc, yd*Ecc);
//		 line.draw(ip.);
		 System.out.print("B = "+Double.toString(A)+"\n");
		 System.out.print("A = "+Double.toString(B)+"\n");
		 System.out.print("Xd = "+Double.toString(xd)+"\n");
		 System.out.print("yd = "+Double.toString(yd)+"\n");
		 System.out.print("Ecc = "+Double.toString(Ecc)+"\n");
		 System.out.print("Theta = "+Double.toString(theta)+"\n");

		 DrawEllipse(centroid.get(index).x, centroid.get(index).y, ra, rb, Math.toDegrees(theta));
	}
	
	public void DrawEllipse(int x, int y, double a, double b, double angle) 
	{

	      double beta = -angle * (Math.PI/180);
	      double ax1 = 0.0, bx1 = 0.0;
	      double ay1 = 0.0, by1 = 0.0;
	      double ax2 = 0.0, bx2 = 0.0;
	      double ay2 = 0.0, by2 = 0.0;
	      double X0 = 0.0;
	      double Y0 = 0.0;
	      for (int i = 0; i <= 360; i+=2) {
	          double alpha = i*(Math.PI/180) ;
	          double X = x + a*Math.cos(alpha)*Math.cos(beta) - b*Math.sin(alpha)*Math.sin(beta);
	          double Y = y + a*Math.cos(alpha)*Math.sin(beta) + b*Math.sin(alpha)*Math.cos(beta);
	          if (i==0) {
	        	  ip.drawLine((int)X, (int)Y, (int)X, (int)Y);
	        	  X0 = X;
	        	  Y0 = Y;
	          }
	          else {
	        	  ip.drawLine((int)X0, (int)Y0, (int)X, (int)Y);
	        	  X0 = X;
	        	  Y0 = Y;	        	  
	          }
	          if (i==0) {ax1=X; ay1=Y;}
	          if (i==90) {bx1=X; by1=Y;}
	          if (i==180) {ax2=X; ay2=Y;}
	          if (i==270) {bx2=X; by2=Y;}
	      }
	      ip.drawLine(x, y, (int)ax1, (int)ay1);
	}
}
class Node 
{
	int x, y;
	
	Node(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
}
