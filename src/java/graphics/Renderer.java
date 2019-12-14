package graphics;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.awt.Color;
import javax.swing.*;
import java.util.*;

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

	// Modules
	private Grid grid;
	private JPanel content_area;
	private Footer footer;
	private Header header;
	private Control control;

	// Internal parameters
	private int headerHeight = 50;
	private int header_playerHeight = 15;
	private int header_diseaseHeight = 15;
	private int footer_playerHeight = 80;
	private int footerHeight = 320;
	private int cell_size = 80;
	private int control_height = 20;

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

		// Resolves color maps for diseases and players
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
		// Creates grid and draws cells

		this.grid = new Grid(board, this, this.cell_size);
		this.header = new Header(this.grid.width, this.headerHeight, this.header_playerHeight,
				this.header_diseaseHeight, this);
		this.control = new Control(this.grid.width, this.control_height, this.g);
		this.footer = new Footer(this.grid.width, this.footerHeight, this.g, this, this.footer_playerHeight);

		// Dummy bg
		/*
		 * h.setBackground(Color.red); this.grid.setBackground(Color.blue);
		 * f.setBackground(Color.cyan);
		 */

		// RAWR - Buggy when trying to add players to an unknown city!!
		// Adds inner panels to general frame
		// content_area.add(this.header);
		content_area.add(this.grid);
		content_area.add(this.control);
		content_area.add(this.footer);

		// Once the components are ready, builds the render
		getContentPane().add(content_area);
		this.pack();

		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Whenever a Renderer is instatiated, its whole grid is updated
		this.refresh(null, null);
		// this.updateGridCells(new ArrayList<String>(g.cities.keySet()));
	}

	/*
	 * Refresh the grid cells containing the specified city aliases
	 */
	/*
	 * public void updateGridCells(ArrayList<String> city_aliases){
	 * this.grid.updateCells(city_aliases); }
	 */

	// TODO
	/*
	 * Refresh renderer If a list of city aliases is specified, then only the cells
	 * containing those cities are updated. Else, if a null object is specified, the
	 * whole grid is updated Updates player info
	 */
	public void refresh(ArrayList<String> city_aliases, ArrayList<String> players) {
		if (city_aliases == null) {
			city_aliases = this.g.gs.board.used_cities;
		}

		if (players == null) {
			players = new ArrayList<String>(this.g.players.keySet());
		}

		this.grid.refresh(city_aliases);
		this.footer.refresh(players);
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
