import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.Convolver;
import ij.plugin.filter.PlugInFilter;
//import ij.process.Blitter;
//import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class testzero implements PlugInFilter{
	
	
	
	public int setup (String arg, ImagePlus im){
		return DOES_ALL;
		}
	
	final float[] dfilt = {1,-2,1};
	//ImageProcessor ipOrig;
	FloatProcessor A;
	FloatProcessor B;
	public void run(ImageProcessor ip){
		
		int w = ip.getWidth();
		int h = ip.getHeight();
		ImagePlus im = IJ.createImage("new pic", "", w+50, h+50, 1);

		ImageProcessor np = im.getProcessor();
		im.show();
		np.insert(ip.convertToByte(false), 25, 25);
		for (int v=0;v<25;v++){
			for(int u=25;u<w+25;u++){
				np.putPixelValue(u, v, np.getPixelValue(u,v+h));
			}
			}

		for(int u=25;u<w+25;u++){
		    for(int v=h+25;v<h+50;v++){
				np.putPixelValue(u, v, np.getPixelValue(u,v-h));
			}
			}

		for (int v=25;v<25+h;v++){
			for(int u=0;u<25;u++){
				np.putPixelValue(u, v, np.getPixelValue(u+w,v));
			}
			}

		for (int v=25;v<25+h;v++){
			for(int u=w+25;u<w+50;u++){
				np.putPixelValue(u, v, np.getPixelValue(u-w,v));
			}
			}

		for (int v=0;v<25;v++){
			for(int u=0;u<25;u++){
				np.putPixelValue(u, v, np.getPixelValue(u+w,v+h));
			}
			}

		for (int v=0;v<25;v++){
			for(int u=w+25;u<w+50;u++){
				np.putPixelValue(u, v, np.getPixelValue(u-w,v+h));
			}
			}

		for (int v=h+25;v<h+50;v++){
			for(int u=0;u<25;u++){
				np.putPixelValue(u, v, np.getPixelValue(u+w,v-h));
			}
			}

		for (int v=h+25;v<h+50;v++){
			for(int u=w+25;u<w+50;u++){
				np.putPixelValue(u, v, np.getPixelValue(u-w,v-h));
			}
			}
		
		//this.ipOrig = ip;
		
		
		//ipOrig = np.duplicate();
		aaa GaussKernel1d = new aaa();
		float[] H = GaussKernel1d.makeGaussKernel1d(1f); // see Prog. 6.4
		Convolver cv = new Convolver();
		cv.setNormalize(true);
		cv.convolve(np, H, 1, H.length);
		cv.convolve(np, H, H.length, 1);
		//ip.insert(ipOrig.convertToByte(false), 0, 0);
		im.updateAndDraw();
		
		FloatProcessor Ix =(FloatProcessor) np.convertToFloat();
		FloatProcessor Iy = (FloatProcessor) np.convertToFloat();
		Ix = convolve1h(Ix,dfilt);
		Iy = convolve1v(Iy,dfilt);
		
		A = ((FloatProcessor) Ix.duplicate());
		B = ((FloatProcessor) Iy.duplicate());
		
		int w1 = np.getWidth();
		int h1 = np.getHeight();
		
		float[] Apix = (float[]) A.getPixels();
		float[] Bpix = (float[]) B.getPixels();
		
		for (int v=0; v<h1; v++) {
			for (int u=0; u<w1; u++) {
			int i = v*w1+u;
			float a = Apix[i], b = Bpix[i];
			float mag1 = a+b;
			//float mag = (float) Math.sqrt(mag1);
			if(mag1==0)
			np.putPixelValue(u, v, 255);
			else
			np.putPixelValue(u, v, 0);
			
            
			}
			}
		
		
		
		for (int v=0;v<h;v++){
			for(int u=0;u<w;u++){
				ip.putPixelValue(u, v, np.getPixelValue(u+25,v+25));
			}
		}
		//im.updateAndDraw();
	}
	
	
	class aaa{
		public float[] makeGaussKernel1d(float sigma) {

			// create the kernel
			int center = (int) (3.0*sigma);
			float[] kernel = new float[2*center+1]; // odd size

			 // fill the kernel
			double sigma2 = sigma * sigma; // ¦Ò2
			for (int i=0; i<kernel.length; i++) {
			double r = center - i;
			kernel[i] = (float) Math.exp(-0.5 * (r*r) / sigma2);
			}

			return kernel;
			}
		}
	
	static FloatProcessor convolve1h(FloatProcessor p, float[] h) {
           Convolver conv = new Convolver();
           conv.setNormalize(false);
           conv.convolve(p, h, 1, h.length);
           return p;
}

    static FloatProcessor convolve1v(FloatProcessor p, float[] h) {
           Convolver conv = new Convolver();
           conv.setNormalize(false);
           conv.convolve(p, h, h.length, 1);
           return p;
}
    static FloatProcessor sqr (FloatProcessor fp1) {
    	fp1.sqr();
    	return fp1;
    	 }
}