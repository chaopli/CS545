import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.plugin.filter.PlugInFilter;
import ij.process.Blitter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class Laplacian_Edge_Detection implements PlugInFilter {

	LEDWindow m_window;
	@Override
	public void run(ImageProcessor ip) {
		// TODO Auto-generated method stub
		ImagePlus originImage = new ImagePlus("Original Image", ip);
		ImagePlus resizedImage = originImage.duplicate();
		int w = originImage.getWidth();
		int h = originImage.getHeight();
		ImageProcessor resizedImageProcessor = resizedImage.getProcessor();
		if (originImage.getHeight() > 800)
		{
			originImage.getProcessor().setInterpolate(true);
			resizedImageProcessor = resizedImageProcessor.resize((int)(w*0.75), (int)(h*0.75));
		}
		resizedImageProcessor = resizedImageProcessor.duplicate();
		resizedImage.setProcessor(resizedImageProcessor);
		m_window = new LEDWindow(resizedImage);
	}

	@Override
	public int setup(String arg0, ImagePlus arg1) {
		// TODO Auto-generated method stub
		return DOES_ALL;
	}

}


class LEDWindow extends ImageWindow implements AdjustmentListener
{
	double sigma_x;
	double sigma_y;
	double w;
	double magnification;
	private Scrollbar scrollBarX;
	private Scrollbar scrollBarY;
	private Scrollbar scrollBarW;
	private ImagePlus processingImg;
	private ImagePlus originImg;
	private double scale;

	public LEDWindow(ImagePlus imp) {
		super(imp);
		magnification = super.ic.getMagnification();
		originImg = imp.duplicate();
		processingImg = originImg.duplicate();
		scale = 1;
		sigma_x = 1;
		sigma_y = 1;
		w = -1;
		GridBagConstraints constraints = new GridBagConstraints();
        GridBagLayout gridbag = new GridBagLayout();
        setLayout(gridbag);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.CENTER;
        gridbag.setConstraints(super.ic, constraints);
        
        scrollBarY = new Scrollbar(Scrollbar.VERTICAL, 1, 0, 0, 51);
        scrollBarY.setBlockIncrement(1);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.VERTICAL;
        gridbag.setConstraints(scrollBarY, constraints);
        scrollBarY.addAdjustmentListener(this);
        scrollBarY.setVisible(true);
        add(scrollBarY);
        scrollBarX = new Scrollbar(Scrollbar.HORIZONTAL, 1, 0, 0, 51);
        scrollBarX.setBlockIncrement(1);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(scrollBarX, constraints);
        scrollBarX.addAdjustmentListener(this);
        scrollBarX.setVisible(true);
        add(scrollBarX);
        
        scrollBarW = new Scrollbar(Scrollbar.HORIZONTAL, 0, 0, -10, 10+1);
        scrollBarW.setBlockIncrement(1);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(scrollBarW, constraints);
        scrollBarW.addAdjustmentListener(this);
        scrollBarW.setVisible(false);
        add(scrollBarW);
        pack();
	}

	
	public ImagePlus ProcessImage()
	{
		processingImg = originImg.duplicate();
		ImageProcessor processingProcessor = processingImg.getProcessor().convertToFloat();

		float[] H_x = MakeGaussKernel1d(sigma_x);
		float[] H_y = MakeGaussKernel1d(sigma_y);
		ImageProcessor tmpProcessor = processingProcessor.duplicate();
		
		MyConvolve(tmpProcessor, H_y, 1, H_y.length);	// In here involves the Wrap Method
		MyConvolve(tmpProcessor, H_x, H_x.length, 1);
		
		float[] H_xd1 = {-0.5f, 0, 0.5f};// MakeGaussKernel1dd2(sigma_x);
		float[] H_yd1 = {-0.5f, 0, 0.5f}; // MakeGaussKernel1dd2(sigma_y);
		FloatProcessor Ix = (FloatProcessor)tmpProcessor.duplicate();
		FloatProcessor Iy = (FloatProcessor)tmpProcessor.duplicate();
		MyConvolve(Iy, H_yd1, 1, H_yd1.length);
		MyConvolve(Ix, H_xd1, H_xd1.length, 1);
		float[] Apix = (float[])Ix.getPixels();
		float[] Bpix = (float[])Iy.getPixels();
		for (int y = 0; y < Ix.getHeight(); y++)
		{
			for (int x = 0; x < Iy.getWidth(); x++)
			{
				float v = Apix[y*Ix.getWidth()+x]+Bpix[y*Ix.getWidth()+x];
				float v0 = (float) Math.sqrt(v);
				v0 *= 10;
				if (v0 >= 10)
					v0 = 255;
				else
					v0 = 0;
				tmpProcessor.putPixelValue(x, y, v0);
			}
		}
		
		processingImg = originImg.duplicate();
		FloatProcessor processingProcessor1 = (FloatProcessor) processingImg.getProcessor().convertToFloat();

		FloatProcessor tmpProcessor1 = (FloatProcessor) processingProcessor1.duplicate();
		
		MyConvolve(tmpProcessor1, H_y, 1, H_y.length);	// In here involves the Wrap Method
		MyConvolve(tmpProcessor1, H_x, H_x.length, 1);
		
		
		float[] H_xd2 = {1, -2, 1};// MakeGaussKernel1dd2(sigma_x);
		float[] H_yd2 = {1, -2, 1}; // MakeGaussKernel1dd2(sigma_y);
		Ix = (FloatProcessor) tmpProcessor1.duplicate();
		Iy = (FloatProcessor) tmpProcessor1.duplicate();
		MyConvolve(Iy, H_yd2, 1, H_yd2.length);
		MyConvolve(Ix, H_xd2, H_xd2.length, 1);
		
		Apix = (float[])Ix.getPixels(); 
		Bpix = (float[])Iy.getPixels();
		int mw = Ix.getWidth();
		int mh = Ix.getHeight();
		for (int y = 0; y < mh; y++)
		{
			for (int x = 0; x < mw; x++)
			{
				int i = y*mw+x;
				float a = Apix[i], b = Bpix[i];
				
				float v = a+b;
				
				if (v < 10 && v > -10) 
				{
					tmpProcessor1.putPixelValue(x, y, 0);
				}
				else 
				{
					tmpProcessor1.putPixelValue(x, y, 255);
				}
			}
		}
		
		ImagePlus i1 = new ImagePlus("i1", tmpProcessor);
		ImagePlus i2 = new ImagePlus("i2", tmpProcessor1);

		ImageProcessor tmpp = originImg.getProcessor().duplicate();
		for (int y = 0; y < Ix.getHeight(); y++)
		{
			for (int x = 0; x < Ix.getWidth(); x++)
			{
				float v1 = tmpProcessor1.getPixelValue(x, y);
				float v2 = tmpProcessor.getPixelValue(x, y);
				if (v1 == 255.0f && v2 == 255.0f)
					tmpp.putPixelValue(x, y, v1);
				else
					tmpp.putPixel(x, y, 0);
			}
		}

		processingImg.setProcessor(tmpp.duplicate());
		return processingImg;
	}
	
	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) 
	{
		// TODO Auto-generated method stub
		
		if (e.getSource() == scrollBarX)
		{
			this.setImage(processingImg);
			sigma_x = (double)e.getValue()/scale;
			String title = "sigma= "+Double.toString(sigma_x)+", sigma y= "+Double.toString(sigma_y);
			ProcessImage();
			super.ic.repaint();
			setTitle(title);
			pack();
		}
		else if (e.getSource() == scrollBarY)
		{
			this.setImage(processingImg);
			sigma_y = (double)e.getValue()/scale;
			String title = "sigma= "+Double.toString(sigma_x)+", sigma y= "+Double.toString(sigma_y);
			ProcessImage();
			super.ic.repaint();
			setTitle(title);
			pack();
		}
		else 
		{
			this.setImage(processingImg);
			w = (double)e.getValue()/scale;
			String title = "sigma= "+Double.toString(sigma_x)+", sigma y= "+Double.toString(sigma_y);
			ProcessImage();
			super.ic.repaint();
			setTitle(title);
			pack();
		}
		
	}
	
	float[] MakeGaussKernel1d (double sigma)
	{
		int center = (int) (3.0*sigma);
		float[] kernel = new float[2*center+1];
		double sigma2 = sigma*sigma;
		for (int i = 0; i < kernel.length; i++)
		{
			double r = center - i;
			kernel[i] = (float) Math.exp(-0.5 * (r*r)/sigma2);
		}
		float sum = 0;
		
		// Normalize the kernel
		for (int i = 0; i < kernel.length; i++)
		{
			sum += kernel[i];
		}

		for (int i = 0; i < kernel.length; i++)
		{
			kernel[i] /= sum;
		}
		return kernel;
	}
	
	float[] MakeGaussKernel1dd1 (double sigma)
	{
		int center = (int) (3.0*sigma);
		float[] kernel = new float[2*center+1];
		double sigma2 = sigma*sigma;
		double sigma4 = sigma2*sigma2;
		for (int i = 0; i < kernel.length; i++)
		{
			double r = center - i;
			kernel[i] = (float) ((-r)*Math.exp(r*r*(-1)/(2*sigma2))/(2*3.14*sigma4));
		}
		float sum = 0;
		
		// Normalize the kernel
		for (int i = 0; i < kernel.length; i++)
		{
			sum += kernel[i];
		}

		for (int i = 0; i < kernel.length; i++)
		{
			kernel[i] /= sum;
		}
		return kernel;
	}
	
	float[] MakeGaussKernel1dd2 (double sigma)
	{
		int center = (int) (3.0*sigma);
		float[] kernel = new float[2*center+1];
		double sigma2 = sigma*sigma;
		double sigma4 = sigma2*sigma2;
		for (int i = 0; i < kernel.length; i++)
		{
			double r = center - i;
			kernel[i] = (float) ((-1)*Math.exp(r*r*(-1)/(2*sigma2))*(1-r*r/sigma2)/(2*3.14*sigma4));
		}
		float sum = 0;
		
		// Normalize the kernel
		for (int i = 0; i < kernel.length; i++)
		{
			sum += kernel[i];
		}

		for (int i = 0; i < kernel.length; i++)
		{
			kernel[i] /= sum;
		}
		return kernel;
	}
	
	// Convolve the Image using kernel and wrap method
	void MyConvolve(ImageProcessor ip, float[] kernel, int kw, int kh)
	{
		int width = ip.getWidth();
		int height = ip.getHeight();
		int x1 = 0;
		int y1 = 0;
		int x2 = x1 + ip.getWidth();
		int y2 = y1 + ip.getHeight();
		int uc = kw/2;    
		int vc = kh/2;
		float[] pixels = (float[])ip.getPixels();
		float[] pixels2 = (float[])ip.getSnapshotPixels();
		if (pixels2==null)
			pixels2 = (float[])ip.getPixelsCopy();
		double sum;
		int offset, i;
		boolean edgePixel;
		int xedge = width-uc;
		int yedge = height-vc;
		for (int y=y1; y<y2; y++) {
			
			for (int x=x1; x<x2; x++) {
				sum = 0.0;
				i = 0;
				edgePixel = y<vc || y>=yedge || x<uc || x>=xedge;
				for (int v=-vc; v <= vc; v++) {
					offset = x+(y+v)*width;
					for(int u = -uc; u <= uc; u++) {
						if (edgePixel) {	// here is where wrap method happens
							sum += getPixel(x+u, y+v, pixels2, width, height)*kernel[i++];
						} else
							sum += pixels2[offset+u]*kernel[i++];
					}
		    	}
				pixels[x+y*width] = (float)(sum);
			}
    	}
	}
	private float getPixel(int x, int y, float[] pixels, int width, int height) {
		// this method just make the pixels out of bounds using the pixel periodically of the original image
		if (x<=0) x += width;
		if (x>=width) x -= width;
		if (y<=0) y += height;
		if (y>=height) y -= height;
		return pixels[x+y*width];
	}

}