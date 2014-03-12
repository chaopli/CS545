import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class GrayLevel_Modification implements PlugInFilter {
	public int setup (String arg, ImagePlus im) {
		return DOES_8G;
	}
	
	public void run (ImageProcessor ip) {
		int w = ip.getWidth();
		int h = ip.getHeight();
	
		// iterate over all image coordinates
		for (int u = 0; u < w; u++) {
			for (int v = 0; v < h; v++) {
				int p = ip.getPixel(u, v);
				int s = 16 * (int)Math.sqrt(p);
				ip.putPixel(u, v, s);
			}
		}
	}
	
}
