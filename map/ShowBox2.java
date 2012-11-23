package map;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

public class ShowBox2 extends JComponent {

	private static final long serialVersionUID = 1L;
	private int x,y;
		BufferedImage image;
		public ShowBox2(int x_, int y_) {
			x = x_;
			y = y_;
		try {
		
		String imageName= "map/box2.png";
		File input = new File(imageName);
		image = ImageIO.read(input);
		} catch (IOException ie) {
		System.out.println("Error:"+ie.getMessage());
		}
		}

		public void paint(Graphics g) {
		g.drawImage( image, x, y, null);
		}
	}