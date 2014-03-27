import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.plugin.filter.Convolver;
import ij.process.Blitter;
import ij.process.ByteProcessor;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

class Corner implements Comparable {
	int u;
	int v;
	float q;
	
	Corner (int u, int v, float q)
	{
		this.u = u;
		this.v = v;
		this.q = q;
	}
	
	public int compareTo (Object obj) {
		// used for sorting corners by corner strength q
		Corner c2 = (Corner) obj;
		if (this.q > c2.q) return -1;
		if (this.q < c2.q) return 1;
		else return 0;
	}
	
	double dist2 (Corner c2) {
		// returns the squared distance between this corner and corner c2
		int dx = this.u - c2.u;
		int dy = this.v - c2.v;
		return (dx*dx)+(dy*dy);
	}
	
	void draw(ImageProcessor ip) {
		// draw this corner as a black cross in ip
		int paintvalue = 0; // black
		int size = 2;
		ip.setValue(paintvalue);
		ip.drawLine(u-size, v, u+size, v);
		ip.drawLine(u, v-size, u, v+size);
	}
}	

class HarrisCornerDetector {
	public static final float DEFAULT_ALPHA = 0.050f;
	public static final int DEFAULT_THRESHOLD = 20000;
	float alpha = DEFAULT_ALPHA;
	int threshold = DEFAULT_THRESHOLD;
	double dmin = 10;
	final int border = 20;
	// filter kernels (1D part of separable 2D filters)
	final float[] pfilt = {0.223755f, 0.552490f, 0.223755f};
	final float[] dfilt = {0.453014f, 0.0f, -0.453014f};
	final float[] bfilt = {0.01563f, 0.09375f, 0.234375f, 0.3125f,
			0.234375f, 0.09375f, 0.01563f};
					// = [1, 6, 15, 20, 15, 6, 1]/64
	ImageProcessor ipOrig;
	FloatProcessor A;
	FloatProcessor B;
	FloatProcessor C;
	FloatProcessor Q;
	List<Corner> corners;
	void SetThreshold(int t)
	{
		this.threshold = t;
	}
	HarrisCornerDetector (ImageProcessor ip) {
		this.ipOrig = ip;
	}
	
	public HarrisCornerDetector (ImageProcessor ip, float alpha, int threshold) {
		this.ipOrig = ip;
		this.alpha = alpha;
		this.threshold = threshold;
	}
	public void findCorners() {
		makeDerivatives();
		makeCrf(); // corner response function(CRF)
		corners = collectCorners(border);
		corners = cleanupCorners(corners);
	}
	
	void makeDerivatives() {
		FloatProcessor Ix = 
				(FloatProcessor) ipOrig.convertToFloat();
		FloatProcessor Iy = 
				(FloatProcessor) ipOrig.convertToFloat();
		Ix = convolve1h(convolve1h(Ix, pfilt), dfilt);
		Iy = convolve1v(convolve1v(Iy, pfilt), dfilt);
		
		A = sqr((FloatProcessor) Ix.duplicate());
		A = convolve2(A, bfilt);
		
		B = sqr((FloatProcessor) Iy.duplicate());
		B = convolve2(B, bfilt);
		
		C = mult((FloatProcessor) Ix.duplicate(), Iy);
		C = convolve2(C, bfilt);
	}
	
	void makeCrf() { // corner response function (CRF)
		int w = ipOrig.getWidth();
		int h = ipOrig.getHeight();
		Q = new FloatProcessor(w, h);
		float[] Apix = (float[]) A.getPixels();
		float[] Bpix = (float[]) B.getPixels();
		float[] Cpix = (float[]) C.getPixels();
		float[] Qpix = (float[]) Q.getPixels();
		for (int v = 0; v < h; v++) {
			for (int u = 0; u < w; u++) {
				int i = v*w+u;
				float a = Apix[i], b = Bpix[i], c = Cpix[i];
				float det = a*b-c*c;
				float trace = a+b;
				Qpix[i] = det - alpha * (trace * trace);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	List<Corner> collectCorners(int border) {
		List<Corner> cornerList = new Vector<Corner>(1000);
		int w = Q.getWidth();
		int h = Q.getHeight();
		float[] Qpix = (float[]) Q.getPixels();
		for (int v = border; v < h-border; v++) {
			for (int u = border; u < w - border; u++) {
				float q = Qpix[v*w+u];
				if (q>threshold && isLocalMax(Q, u, v)) {
					Corner c = new Corner(u, v, q);
					cornerList.add(c);
					
				}
			}
		}
		Collections.sort(cornerList);
		return cornerList;
	}
	
	List<Corner> cleanupCorners(List<Corner> corners) {
		double dmin2 = dmin*dmin;
		Corner[] cornerArray = new Corner[corners.size()];
		cornerArray = corners.toArray(cornerArray);
		List<Corner> goodCorners = 
				new Vector<Corner>(corners.size());
		for (int i = 0; i < cornerArray.length; i++) {
			if (cornerArray[i] != null) {
				Corner c1 = cornerArray[i];
				goodCorners.add(c1);
				// delete all remaining corners close to c
				for (int j = i+1; j < cornerArray.length; j++) {
					if (cornerArray[j] != null) {
						Corner c2 = cornerArray[j];
						if (c1.dist2(c2) < dmin2)
							cornerArray[j] = null; // delete corner
					}
				}
			}
		}
		return goodCorners;
	}
	
	@SuppressWarnings("deprecation")
	void printCornerPoints(List<Corner> crf) {
		int i = 0;
		for (Corner ipt: crf) {
			IJ.write((i++) + ": " + (int)ipt.q + " " + ipt.u + " " +
					ipt.v);
			
				}
			}
	public ImageProcessor showCornerPoints(ImageProcessor ip) {
		ByteProcessor ipResult = (ByteProcessor)ip.duplicate();
		// change background image contrast and brightness
		int[] lookupTable = new int[256];
		for (int i = 0; i < 256; i++) {
			lookupTable[i] = 128 + (i/2);
		}
		ipResult.applyTable(lookupTable);
		// draw corners:
		for (Corner c: corners) {
			c.draw(ipResult);
		}
		return ipResult;
	}
	
	void showProcessor (ImageProcessor ip, String title) {
		ImagePlus win = new ImagePlus(title, ip);
		win.show();
	}
	
	// utility methods for float processors --
	
	static FloatProcessor convolve1h
					(FloatProcessor p, float[] h) {
		Convolver conv = new Convolver();
		conv.setNormalize(false);
		conv.convolve(p, h, 1, h.length);
		return p;
	}
	
	static FloatProcessor convolve1v
					(FloatProcessor p, float[] h) {
		Convolver conv = new Convolver();
		conv.setNormalize(false);
		conv.convolve(p, h, h.length, 1);
		return p;
	}
	
	static FloatProcessor convolve2
					(FloatProcessor p, float[] h) {
		convolve1h(p, h);
		convolve1v(p, h);
		return p;
	}
	
	static FloatProcessor sqr (FloatProcessor fp1) {
		fp1.sqr();
		return fp1;
	}
	
	static FloatProcessor mult (FloatProcessor fp1, FloatProcessor fp2) {
		int mode = Blitter.MULTIPLY;
		fp1.copyBits(fp2, 0, 0, mode);
		return fp1;
	}
	
	static boolean isLocalMax (FloatProcessor fp, int u, int v) {
		int w = fp.getWidth();
		int h = fp.getHeight();
		
		if (u <= 0 || u >= w-1 || v <= 0 || v >= h-1)
			return false;
		else {
			float[] pix = (float[]) fp.getPixels();
			int i0 = (v-1)*w+u, i1 = v*w+u, i2 = (v+1)*w+u;
			float cp = pix[i1];
			return
					cp > pix[i0-1] && cp > pix[i0] && cp > pix[i0+1] &&
					cp > pix[i1-1] && cp > pix[i1+1] && cp > pix[i2-1] &&
					cp > pix[i2] && cp > pix[i2+1];
		}
	}
	
	float GetContrast(ImageProcessor ip){
		float contrast = 0;
		float c = 0;
		float sum = 0;
		int w = ip.getWidth();
		int h = ip.getHeight();
		
		
		
		for(int y = 0 ; y < h; y++)
			for(int x =0; x < w; x++){
				sum += ip.getPixelValue(x, y);
			}
		int num = w*h;
		float ave = sum /(float) num;
		sum = 0;
		for (int y = 0; y < h; y++)
		{
			for (int x = 0; x < w; x++)
			{
				float deltaI2 = (ip.getPixelValue(x, y)-ave)*(ip.getPixelValue(x, y)-ave);
				sum += deltaI2;
			}
		}
		
		contrast = (float)Math.sqrt(sum/num);
		return contrast;
	}
} // end of class HarrisCornerDetector


public class modified_harris_detector implements PlugInFilter {

	
	@Override
	public void run(ImageProcessor ip) {
		// TODO Auto-generated method stub
		HarrisCornerDetector hcd = new HarrisCornerDetector(ip);
		float contrast = hcd.GetContrast(ip);
		if(contrast > 60)
			hcd.SetThreshold(50000);
		else if (contrast < 45 )
			hcd.SetThreshold(10000);
		hcd.findCorners();
		ImageProcessor result = hcd.showCornerPoints(ip);
		ImagePlus win = new ImagePlus("Corners", result);
		win.show();
	}

	@Override
	public int setup(String arg0, ImagePlus arg1) {
		// TODO Auto-generated method stub
		return DOES_8G;
	}

}
