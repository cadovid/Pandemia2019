package graphics;

import java.awt.*;
import javax.swing.*;

public class Box extends JPanel {
	public Renderer r;
	public int width;
	public int height;

	public Box() {

	}

	public Box(int width, int height, LayoutManager lyt) {
		// this(top_border, left_border);
		if (lyt == null) {
			lyt = new BoxLayout(this, BoxLayout.X_AXIS);
		}

		this.width = width;
		this.height = height;
		this.updateSize();
		this.setLayout(lyt);
	}

	/*
	 * public Box(int width, int height, Renderer r){ this(width, height); this.r =
	 * r; this.setLayout(new BorderLayout(0, 0)); }
	 */

	// Setters/getters

	public void updateSize() {
		this.setPreferredSize(new Dimension(this.width, this.height));
		this.setMaximumSize(new Dimension(this.width, this.height));
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public void setRenderer(Renderer r) {
		this.r = r;
	}
}
