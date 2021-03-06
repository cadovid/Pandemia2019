package graphics;

import java.awt.*;
import javax.swing.*;

import board.Cell;
import city.City;
import dis.*;

public class GridDis extends GridInfo {

	public GridDis(int n_items) {
		super(n_items);
	}

	/*
	 * Redefinition. Refreshes disease data in the cell
	 */
	public void update(Cell cell, Renderer r) {
		// Removes old contents
		this.removeAll();

		// A valid city is required to be in the cell to properly update disease data
		City c = cell.city;
		if (c == null) {
			System.out.printf("[Renderer] WARN: Trying to update an empty cell. Ignoring...\n");
			return;
		}

		// Iterates through active infections
		for (Infection e : c.infections) {

			// Retrieves Infection view data
			Color e_color = r.color_diseases.get(e.dis.alias);
			String e_spread = String.valueOf(e.spread_level);

			// Creates graphic object
			JLabel e_info = new JLabel(e_spread, SwingConstants.CENTER);
			e_info.setFont(e_info.getFont().deriveFont(9.0f));
			e_info.setOpaque(true);
			e_info.setBackground(e_color);
			e_info.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(1.0f), Color.black));
			this.add(e_info);
		}
	}
}
