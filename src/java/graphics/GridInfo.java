package graphics;

import java.awt.*;
import javax.swing.*;

import board.Cell;

public class GridInfo extends JPanel {

	public GridInfo(int n_items) {
		super();

		this.setLayout(new GridLayout(n_items, 1));
		this.setOpaque(false);
	}
}
