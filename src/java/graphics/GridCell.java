package graphics;

import java.awt.image.BufferedImage;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;

import board.Cell;
import city.City;
import _aux.Datapaths;

public class GridCell extends Box {
	private int size;
	private Cell board_cell;
	private Color color_ldis; // Local disease color
	private JPanel cell_hud;
	private Renderer r;

	GridDis info_diseases;
	GridInfo info_city;
	GridPlayer info_players;

	public GridCell(int s, Cell bc, Renderer r) {
		super(s, s, new BorderLayout(0, 0));

		this.r = r;
		this.size = s;
		this.board_cell = bc;

		// Resolves local disease color
		City city = this.board_cell.getCity();
		if (city == null) {
			this.color_ldis = Color.gray;
		}

		else {
			this.color_ldis = this.r.color_diseases.get(city.local_disease.alias);
		}

		// Border color identifies local disease (if any)
		this.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(2.0f), this.color_ldis));
		// this.add(new JButton("rawr"));

		// Cell info block
		cell_hud = new JPanel();
		cell_hud.setPreferredSize(new Dimension(this.size, this.size));
		cell_hud.setMaximumSize(new Dimension(this.size, this.size));
		cell_hud.setLayout(new GridLayout(1, 3, 0, 0));

		this.info_diseases = new GridDis(r.g.n_diseases);
		this.info_city = new GridInfo(6);
		this.info_players = new GridPlayer(r.g.n_players);

		this.info_diseases.setBackground(Color.red);
		this.info_city.setBackground(Color.white);
		this.info_players.setBackground(Color.pink);

		cell_hud.add(this.info_diseases);
		cell_hud.add(this.info_city);
		cell_hud.add(this.info_players);
		cell_hud.setOpaque(false);

		this.add(cell_hud);
		// this.add(new JButton("r"));
	}

	public void refresh() {
		// Refreshes infections
		this.info_diseases.update(this.board_cell, this.r);

		// Refreshes players
		this.info_players.refresh(this.board_cell, this.r);

		// Updates city metainfo (iterates all inner grid cells to set the elements in
		// the appropriate order)
		this.info_city.removeAll();
		for (int i = 0; i < 6; i++) {
			// Adds empty cell
			if (i >= 0 && i < 4) {
				JPanel empty = new JPanel();
				empty.setOpaque(false);
				info_city.add(empty);
			}

			// Set research (if available)
			else if (i == 4) {
				if (this.board_cell.city.can_research) {
					JPanel research_ico = new JPanel();
					research_ico.setBackground(Color.white);
					research_ico.setOpaque(true);
					info_city.add(research_ico);
				}

				// Adds empty cell
				else {
					JPanel empty = new JPanel();
					empty.setOpaque(false);
					info_city.add(empty);
				}
			}

			// Set city name
			else if (i == 5) {
				JLabel c_alias = new JLabel(this.board_cell.city.alias, SwingConstants.CENTER);
				c_alias.setBackground(this.color_ldis);
				c_alias.setOpaque(true);
				info_city.add(c_alias);
			}

		}

		/*
		 * // Refresh research if(this.board_cell.city.can_research) {
		 * this.info_research.setOpaque(true); } else {
		 * this.info_research.setOpaque(false); }
		 */
	}

	/*
	 * Redefinition. Overrides default paintComponent method Sets cell-specific
	 * background image
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		BufferedImage sprite = null;
		String cell_spritePath;
		String cell_cityAlias = this.board_cell.getCityAlias();
		File imfile;

		// If blank cell, then a water sprite is used
		if (cell_cityAlias == null) {
			cell_cityAlias = "water";
		}

		// Tries to load a specific sprite from the folder. If fail, then sets a default
		// sprite
		imfile = new File(Datapaths.sprites_cellbg_folder.concat(cell_cityAlias).concat(".jpg"));
		if (!imfile.exists() || imfile.isDirectory()) {
			cell_cityAlias = "grass";
		}

		// Uses already loaded image
		if (this.r.sprites.containsKey(cell_cityAlias)) {
			sprite = this.r.sprites.get(cell_cityAlias);
		}

		// Loads new image
		else {
			// Tries to load image from path
			String ipath = Datapaths.sprites_cellbg_folder.concat(cell_cityAlias).concat(".jpg");
			imfile = new File(ipath);

			sprite = this.r.loadImg(imfile);
			this.r.sprites.put(cell_cityAlias, sprite);
		}

		g.drawImage(sprite, 0, 0, this.size, this.size, null);
	}
}
