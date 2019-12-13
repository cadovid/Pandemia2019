package graphics;

import java.awt.*;

// Table object of dynamic height (fixed row height)
public class Table extends Box {
	public Table(int w, int rows, int row_h) {
		super(w, row_h * rows, null);
		this.setLayout(new GridLayout(rows, 1, 0, 0));
	}
}
