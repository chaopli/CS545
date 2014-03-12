import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Vector;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageWindow;
import ij.plugin.ChannelSplitter;
import ij.plugin.RGBStackMerge;
import ij.plugin.filter.Convolver;
import ij.plugin.filter.PlugInFilter;
import ij.process.Blitter;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;


public class Restore_Img_D implements PlugInFilter {
	private MyImageWindow mwindow;
	@Override
	public void run(ImageProcessor ip) 
	{
		// TODO Auto-generated method stub
		ImagePlus init = new ImagePlus("Initial Image", ip);
		ImagePlus origin = init.duplicate();
		origin.setTitle("Origin Image");
		mwindow = new MyImageWindow(origin);
	}

	@Override
	public int setup(String arg0, ImagePlus arg1) 
	{
		// TODO Auto-generated method stub
		return DOES_RGB;
	}
	

}

class MyImageWindow extends ImageWindow implements AdjustmentListener
{
	private double c;
	private Scrollbar scrollBar;
	private ImagePlus processingImg;
	private ImagePlus originImg;
	private double scale;
	public MyImageWindow(ImagePlus imp) {
		super(imp);
		originImg = imp.duplicate();
		processingImg = originImg.duplicate();
		scale = 10;
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
        GridBagLayout gridbag = new GridBagLayout();
        setLayout(gridbag);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(super.ic, constraints);
        scrollBar = new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, -100, 110);
        scrollBar.setMinimum(-100);
        scrollBar.setMaximum(110);
        scrollBar.setBlockIncrement(1);
        gridbag.setConstraints(scrollBar, constraints);
		scrollBar.addAdjustmentListener(this);
		add(scrollBar);
		pack();
	}

	
	public ImagePlus ProcessImage()
	{
		processingImg = originImg.duplicate();
		int w = originImg.getWidth();
		int h = originImg.getHeight();
		
		ImagePlus[] rgb = new ImagePlus[3];
		ImageStack[] stack = new ImageStack[3];
		stack = ChannelSplitter.splitRGB(processingImg.getStack(), true);
		rgb[0] = new ImagePlus("red", stack[0]);
		rgb[1] = new ImagePlus("green", stack[1]);
		rgb[2] = new ImagePlus("blue", stack[2]);
		ImageProcessor[] processingProcessor = new ImageProcessor[3];
		double[][] filter = 
			{ 
				{0, c/4, 0},
				{c/4, 1-c, c/4},
				{0, c/4, 0} 
		  	};
		for (int n = 0; n < 3; n++)
		{
			processingProcessor[n] = rgb[n].getProcessor().duplicate();
			ImageProcessor tmp = processingProcessor[n].duplicate();
			for (int y = 1; y < h-1; y++)
			{
				for (int x = 1; x < w-1; x++)
				{
					double sum = 0;
					for (int i = -1; i < 2; i++)
					{
						for (int j = -1; j < 2; j++)
						{
							int v = tmp.getPixel(x+j, y+i);
							// Get the corresponding filter coefficient
							double a = filter[i+1][j+1];
							sum += a*v;
						}
					}
					int q = (int) Math.round(sum);
					if (q < 0)
					{
						q = 0;
					}
					if (q > 255)
					{
						q = 255;
					}
					processingProcessor[n].putPixel(x, y, q);
				}
			}
		}
		
		for (int i = 0; i < 3; i++)
		{
			rgb[i].setProcessor(processingProcessor[i]);
		}
		ImageStack outstack = RGBStackMerge.mergeStacks(rgb[0].getStack(), rgb[1].getStack(), rgb[2].getStack(), true);
		ImagePlus output = new ImagePlus("output", outstack);
		processingImg = output.duplicate();
		return processingImg;
	}
	
	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) 
	{
		// TODO Auto-generated method stub
		this.setImage(processingImg);
		c = (double)e.getValue()/scale;
		ProcessImage();
		setTitle("c= "+Double.toString(c));
		super.ic.repaint();
		pack();
	}
}

