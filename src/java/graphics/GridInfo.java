package graphics;

import java.awt.*;
import javax.swing.*;

import board.Cell;

public class GridInfo extends JPanel {

	public GridInfo(int n_items) {
		super();

		this.setLayout(new GridLayout(n_items, 1, 0, 0));
		this.setOpaque(false);
	}

	/*
	 * Refresh all items on the panel Every specialized class extended from GridInfo
	 * must override this method with its own
	 */
	public void update(Cell c, Renderer r) {
		// Remove outdated components
		this.removeAll();
	}
}
