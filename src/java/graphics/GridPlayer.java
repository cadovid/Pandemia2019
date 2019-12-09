package graphics;

import java.awt.*;
import javax.swing.*;

import board.Cell;
import city.City;
import player.Player;

public class GridPlayer extends GridInfo {

	public GridPlayer(int n_items) {
		super(n_items);
	}

	/*
	 * Redefinition. Refreshes player data in the cell
	 */
	public void update(Cell cell, Renderer r) {
		// Calls superclass update method
		super.update(cell, r);

		// A valid city is required to be in the cell to properly update disease data
		City c = cell.getCity();
		if (c == null) {
			System.out.printf("[Renderer] WARN: Trying to update an empty cell. Ignoring...\n");
			return;
		}

		// Iterates through active epidemics
		for (Player p : c.getPlayers()) {
			// Retrieves epidemic representation data
			Color p_color = r.color_players.get(p.alias);

			// Creates graphic object
			JLabel p_info = new JLabel(p.alias.substring(0, 2), SwingConstants.CENTER); // Shows first character of the
																						// player's alias
			p_info.setFont(p_info.getFont().deriveFont(8.0f));
			p_info.setOpaque(true);
			p_info.setBackground(p_color);
			p_info.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(1.0f), Color.black));
			this.add(p_info);

			// Refresh interface
			this.revalidate();
			this.repaint();
		}
	}
}
