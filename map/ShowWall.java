package map;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

public class ShowWall extends JComponent {
	private static final long serialVersionUID = 1L;
	private int x,y;
		BufferedImage image;
		public ShowWall(int x_, int y_) {
			x = x_;
			y = y_;
		try {
		
		String imageName= "map/wall.jpg";
		System.out.println(System.getProperty("user.dir"));
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