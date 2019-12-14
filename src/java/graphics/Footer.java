package graphics;

import java.awt.*;

import javax.swing.*;

// Header data
import java.util.*;
import game.Game;
import player.Player;
import card.Card;
import card.CityCard;

public class Footer extends graphics.Box {
	int p_flag_width = 5;
	int p_info_width;
	int p_highlight = 2;
	int p_cont_gap = 2 + p_highlight;
	Hashtable<String, Box> player_tabs; // Useful to identify specific player tabs in the footer so they can be updated
										// individually
	Game g;
	Renderer r;

	int p_tab_height;
	int p_n_lines = 3;
	int p_line_height;
	Font tab_font_name;
	Font tab_font_data;

	// Constructors
	public Footer(int width, int height, Game g, Renderer r, int p_tab_height) {
		super(width, height, null);
		player_tabs = new Hashtable<String, Box>();
		this.p_tab_height = p_tab_height;
		this.g = g;
		this.r = r;

		// RAWR - rethink gaps
		this.p_info_width = width - this.p_flag_width - 2 * this.p_cont_gap;
		this.p_line_height = (int) ((float) p_tab_height / this.p_n_lines) - 2 * this.p_cont_gap;

		this.tab_font_name = new Font("TimesRoman", Font.BOLD, this.p_line_height);
		this.tab_font_data = new Font("TimesRoman", Font.PLAIN, this.p_line_height - 2);

		// Creates table contents of footers
		Collection<Player> players = g.players.values();
		Table player_hud = new Table(width, players.size(), p_tab_height);
		Box container = new Box(width, players.size() * p_tab_height, null);

		// Creates scrollable container
		JScrollPane scrollable = new JScrollPane();
		scrollable.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollable.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollable.setViewportView(container);

		// Creates an info object per player containing its relevant info
		for (Player p : players) {
			Box p_tab = new Box(width, p_tab_height, new FlowLayout(FlowLayout.LEFT, p_cont_gap, p_cont_gap));

			parsePlayerInfo(p_tab, p);

			// Appends main data container to general player list
			player_hud.add(p_tab);

			// Adds resulting object to a hashtable, so it can be easily refreshed later
			player_tabs.put(p.alias, p_tab);
		}

		// Appends list to main container and binds scrollable object to footer
		container.add(player_hud);
		this.add(scrollable);

		// this.add(new JLabel("[Footer] - Should show current turn data..."));
		// this.add(new JLabel("[Footer] - Second line..."));
	}

	// Refresh a set of player info blocks
	public void refresh(ArrayList<String> players) {

		// Refreshes blocks
		for (String p : players) {
			Box container = player_tabs.get(p);

			// Removes all its contents
			container.removeAll();

			// Generates player info contents
			parsePlayerInfo(container, this.g.players.get(p));

			// Refresh interface
			container.revalidate();
			container.repaint();
		}
	}

	// Given a Box object and a player, builds the dataobject
	public void parsePlayerInfo(Box container, Player p) {

		// Player info main containers
		Box p_flag = new Box(this.p_flag_width, this.p_tab_height, null);
		Box p_info = new Box(this.p_info_width, this.p_tab_height, null);

		// Highlights current playing player
		if (p.turn) {
			p_info.setBorder(BorderFactory.createLineBorder(Color.blue, p_highlight));
		}

		// Sets flag
		p_flag.setBackground(this.r.color_players.get(p.alias));

		// Builds info
		Table p_data = new Table(this.p_info_width, p_n_lines, this.p_line_height);

		JLabel p_name = new JLabel(p.alias);

		Box p_city = new Box(this.p_info_width, this.p_line_height, null);
		JLabel p_city_l1 = new JLabel("In: ");
		JLabel p_city_l2 = new JLabel(p.city.name + "(" + p.city.local_disease.name + ")");
		p_city_l2.setOpaque(true);
		p_city_l2.setBackground(this.r.color_diseases.get(p.city.local_disease.alias));
		p_city_l1.setFont(this.tab_font_data);
		p_city_l2.setFont(this.tab_font_data);

		p_city.add(p_city_l1);
		p_city.add(p_city_l2);

		// Player hand
		Box p_hand = new Box(this.p_info_width, this.p_line_height, null);
		JLabel p_hand_l1 = new JLabel("Hand: ");
		p_hand_l1.setFont(this.tab_font_data);
		p_hand.add(p_hand_l1);

		for (CityCard c : p.hand.values()) {
			JLabel p_hand_l = new JLabel(c.city.alias);
			p_hand_l.setOpaque(true);
			p_hand_l.setBackground(this.r.color_diseases.get(c.city.local_disease.alias));
			p_hand_l.setFont(this.tab_font_data);

			// Border
			p_hand_l.setBorder(BorderFactory.createLineBorder(Color.black));

			p_hand.add(p_hand_l);
		}

		// Sets appropriate font
		p_name.setFont(this.tab_font_name);
		p_hand.setFont(this.tab_font_data);

		// Appends tab contents to table
		p_data.add(p_name);
		p_data.add(p_city);
		p_data.add(p_hand);

		// Appends table to info container
		p_info.add(p_data);

		// Appends flag & tab to main player data container
		container.add(p_flag);
		container.add(p_info);
	}
}
