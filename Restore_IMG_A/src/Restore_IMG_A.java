import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.util.*;
public class Restore_IMG_A implements PlugInFilter 
{

	
	private final int[][] W = 
		{ 
			{1, 2, 1},
			{2, 3, 2},
			{1, 2, 1}
		};
	
	Vector<Integer> extendedPV;
	@Override
	public void run(ImageProcessor ip) 
	{
		// TODO Auto-generated method stub
		int pvSize = 0;
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				pvSize += W[i][j];
			}
		}
		ImagePlus Origin = new ImagePlus("Original Image", ip);
		ImagePlus tmpImage = Origin.duplicate();
		tmpImage.setTitle("Result");
		ImageProcessor tmpip = tmpImage.getProcessor();
		
		int w = ip.getWidth();
		int h = ip.getHeight();
		for (int y = 1; y < h-1; y++)
		{
			for (int x = 1; x < w-1; x++)
			{
				extendedPV = new Vector<Integer>();
				for (int i = -1; i < 2; i++)
				{
					for (int j = -1; j < 2; j++)
					{
						int p = tmpip.getPixel(x+j, y+i);
						for (int t = 0; t < W[i+1][j+1]; t++)
						{
							extendedPV.add(p);
						}
					}
				}
//				System.out.println(extendedPV);
				Collections.sort(extendedPV);
//				System.out.println(extendedPV);
				int q = extendedPV.get(extendedPV.size()/2);
				tmpip.putPixel(x, y, q);
			}
		}
		
		tmpImage.show();
	}

	@Override
	public int setup(String arg0, ImagePlus arg1) 
	{
		// TODO Auto-generated method stub
		return DOES_ALL;
	}
	
	
}
