package graphics;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.awt.Color;
import javax.swing.*;
import java.util.*;
import java.util.Random;

import board.Board;
import player.Player;
import dis.Disease;
import game.Game;
import _aux.Options;
import _aux.CustomTypes;

public class Renderer extends JFrame {

	// General options
	public String title = "Parachis";
	public Hashtable<String, BufferedImage> sprites; // Stores laoded images
	public Game g;

	// Internal parameters
	private Grid grid;
	private JPanel content_area;
	private int headerHeight = 50;
	private int header_playerHeight = 15;
	private int header_diseaseHeight = 15;
	private int footerHeight = 30;

	// Color maps
	public Hashtable<String, Color> color_diseases = new Hashtable<String, Color>();
	public Hashtable<String, Color> color_players = new Hashtable<String, Color>();

	// Helpful structures
	public ArrayList<Player> players;
	public ArrayList<Disease> diseases;

	public Renderer(String title) {
		if (title == null) {
			title = this.title;
		}
		this.setTitle(title);

		this.sprites = new Hashtable<String, BufferedImage>();
	}

	public Renderer(Game g, String title, Board board) {
		this(title);
		this.g = g;

		this.players = new ArrayList<Player>(g.players.values());
		this.diseases = new ArrayList<Disease>(g.diseases.values());

		Set<String> alias_diseases = g.diseases.keySet();
		Set<String> alias_players = g.players.keySet();

		// Resolves color maps fro diseases and players
		for (String dis : alias_diseases) {
			Random rand = new Random();
			float color_r = rand.nextFloat(), color_g = rand.nextFloat(), color_b = rand.nextFloat();

			// RAWR - To avoid duplicate colors, it'll be useful to generate all color codes
			// separately,
			// then reshuffle duplicates before update hashmap
			// Realistically, this is not needed...

			Color color = new Color(color_r, color_g, color_b);
			this.color_diseases.put(dis, color);
		}

		for (String player : alias_players) {
			Random rand = new Random();
			float color_r = rand.nextFloat(), color_g = rand.nextFloat(), color_b = rand.nextFloat();

			Color color = new Color(color_r, color_g, color_b);
			this.color_players.put(player, color);
		}

		// Resolves game layout
		content_area = new JPanel();
		content_area.setLayout(new BoxLayout(content_area, BoxLayout.Y_AXIS)); // By default, elements are displayed
																				// vertically
		content_area.setBackground(Color.gray);

		// Adds complementary panels
		// Global width is ruled by the grid size
		this.grid = new Grid(board);

		// Grid needs to access the renderer to draw images
		this.grid.setRenderer(this);
		this.grid.drawCells();

		Header h = new Header(this.grid.width, this.headerHeight, this.header_playerHeight, this.header_diseaseHeight,
				this);
		Footer f = new Footer(this.grid.width, this.footerHeight);

		// Dummy bg
		/*
		 * h.setBackground(Color.red); this.grid.setBackground(Color.blue);
		 * f.setBackground(Color.cyan);
		 */

		// Adds inner panels to general frame
		content_area.add(h);
		content_area.add(this.grid);
		content_area.add(f);

		// Once the components are ready, builds the render
		getContentPane().add(content_area);
		this.pack();

		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Whenever a Renderer is instatiated, its whole grid is updated
		this.updateGridCells(new ArrayList<String>(g.cities.keySet()));
	}

	/*
	 * Refresh the grid cells containing the specified city aliases
	 */
	public void updateGridCells(ArrayList<String> city_aliases) {
		this.grid.updateCells(city_aliases);
	}

	// Loads image from file
	public static BufferedImage loadImg(File file) {
		BufferedImage img = null;

		if (Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
			System.out.printf("[Renderer] INFO: Loading image file: %s...\n", file.getPath());
		try {
			img = ImageIO.read(file);
		} catch (IOException e) {
			if (Options.LOG.ordinal() >= CustomTypes.LogLevel.CRITICAL.ordinal())
				System.out.printf("[Renderer] CRITICAL: Exception while loading image\n");

			System.exit(0);
		}
		return img;
	}
}
