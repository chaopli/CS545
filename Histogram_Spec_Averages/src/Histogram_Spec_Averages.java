import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.gui.NewImage;
import ij.io.*;

import java.io.File;


public class Histogram_Spec_Averages implements PlugInFilter {
	public static File folder = new File("D:\\Chao\\SkyDrive\\Study\\WPI\\CS545\\samples\\hw2\\p1");
	public final int NIMAGE = 4;
	@Override
	public void run(ImageProcessor ip) {
		// TODO Auto-generated method stub
		ImagePlus[] images = new ImagePlus[NIMAGE];
		Opener mOpener = new Opener();
		images = ReadAllImages(folder, mOpener);
		ImageProcessor[] ips = new ImageProcessor[NIMAGE];
		for (int i = 0; i < NIMAGE; i++)
		{
			images[i].show();
		}
		
		for (int i = 0; i < NIMAGE; i++)
		{
			ips[i] = images[i].getProcessor();
		}
		
		int[][] histograms = new int[NIMAGE][];
		for (int i = 0; i < NIMAGE; i++)
		{
			histograms[i] = ips[i].getHistogram();
		}
		ImagePlus origin  = new ImagePlus("Original Image", ip);
		ImagePlus result = origin.duplicate();
		result.setTitle("Result");
		ImageProcessor rip = result.getProcessor();
		int[] hA = rip.getHistogram();
		int[] hR = GetAveHistogram(histograms);
		int[] F = MatchHistograms(hA, hR);
		rip.applyTable(F);
		result.show();
	}

	@Override
	public int setup(String arg0, ImagePlus arg1) {
		// TODO Auto-generated method stub
		return DOES_ALL;
	}
	
	public ImagePlus[] ReadAllImages(final File folder, Opener mOpener)
	{
		int i = 0;
		ImagePlus[] images = new ImagePlus[NIMAGE];
		for (final File fileEntry : folder.listFiles())
		{
			if (fileEntry.isFile())
			{
				images[i++] = mOpener.openImage(folder.getAbsolutePath()+"/"+fileEntry.getName());
			}
		}
		return images;
	}
	public int[] GetAveHistogram(int[][] hists)
	{
		int histsize = hists[0].length;
		int[] aveHist = new int[hists[0].length];
		for (int i = 0; i < histsize; i++)
		{
			int v = 0;
			for (int j = 0; j < NIMAGE; j++)
			{
				v += hists[j][i];
			}
			v /= NIMAGE;
			aveHist[i] = v;
		}
		
		return aveHist;
	}
	public int[] MatchHistograms(int[] hA, int[] hR)
	{
		int K = hA.length;
		double[] PA = CDF(hA);
		double[] PR = CDF(hR);
		int[] F = new int[K];
		
		for (int a = 0; a < K; a++)
		{
			int j = K-1;
			do
			{
				F[a] = j;
				j--;
			} while (j >= 0 && PA[a] <= PR[j]);
		}
		return F;
	}
	
	public double[] CDF(int[] h)
	{
		int K = h.length;
		int n = 0;
		for (int i = 0; i < K; i++)
		{
			n += h[i];
		}
		
		double[] P = new double[K];
		int c = h[0];
		P[0] = (double) c/n;
		for (int i = 1; i < K; i++)
		{
			c += h[i];
			P[i] = (double) c/n;
		}
		return P;
		
	}
}
