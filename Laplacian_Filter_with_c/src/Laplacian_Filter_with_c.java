import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.GifWriter;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class Laplacian_Filter_with_c implements PlugInFilter 
{

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
		return DOES_ALL;
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
        scrollBar = new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, -100, 100);
        scrollBar.setBlockIncrement(1);
        gridbag.setConstraints(scrollBar, constraints);
		scrollBar.addAdjustmentListener(this);
		add(scrollBar);
		pack();
	}

	
	public ImagePlus ProcessImage()
	{
		double[][] filter = 
			{ 
				{0, c/4, 0},
				{c/4, 1-c, c/4},
				{0, c/4, 0} 
		  	};
		int w = originImg.getProcessor().getWidth();
		int h = originImg.getProcessor().getHeight();
		processingImg = originImg.duplicate();

		for (int y = 1; y < h-1; y++)
		{
			for (int x = 1; x < w-1; x++)
			{
				double sum = 0;
				for (int i = -1; i < 2; i++)
				{
					for (int j = -1; j < 2; j++)
					{
						int v = originImg.getProcessor().getPixel(x+j, y+i);
						// Get the corresponding filter coefficient
						double a = filter[i+1][j+1];
						sum += a*v;
					}
				}
				int q = (int) Math.round(sum);
				processingImg.getProcessor().putPixel(x, y, q);
			}
		}
		
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
		processingImg.updateAndDraw();
	}

}