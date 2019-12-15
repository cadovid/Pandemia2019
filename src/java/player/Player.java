package player;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;

import city.City;
import card.*;
import _aux.Options;
import _aux.CustomTypes;

/*
  Player class
    Represents a player object. Contains relevant information about its status
*/
public class Player {
	public String alias;
	public City city;
	private Role prole;
	public HashMap<String, CityCard> hand = new HashMap<String, CityCard>();
	public boolean turn = false;

	// Constructors
	public Player(String alias) {
		this.alias = alias;
	}

	public Player(String alias, City c, Role r) {
		this.alias = alias;
		this.city = c;
		this.prole = r;

		// Cross-reference. Role has a reference to the assigned player, so do the city
		// in which is in
		this.prole.bindPlayer(this);
		this.city.putPlayer(this);
	}

	/*
	 * parsePlayers Initializes players from a datafile. Expected format (csv):
	 * <Alias>;<role>;<city>;
	 * 
	 * Alias must be unique. It'll be used as identifier.
	 ** 
	 * PARAMETERS: datafile: String; path to file. roles: Role map; map containing
	 * existing roles. Will be used to maintain coherence with the game objects
	 * cities: City map; map containing existing cities. Will be used to maintain
	 * coherence with the game objects
	 * 
	 */
	public static Hashtable<String, Player> parsePlayers(String datafile, Hashtable<String, Role> roles,
			Hashtable<String, City> cities) {
		Hashtable<String, Player> players = new Hashtable<String, Player>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(datafile));
			String line;
			String[] player_data;
			String player_alias;
			String player_role_alias;
			String player_city_alias;
			Role player_role;
			City player_city;

			// Reads player data from subsequent lines (Must follow the expected disease
			// format)
			while ((line = br.readLine()) != null) {
				player_data = line.split(";");

				// Gets data from splitted line elements
				player_alias = player_data[0];
				player_role_alias = player_data[1];
				player_city_alias = player_data[2];

				// Checks game object coherence
				if (players.containsKey(player_alias)) {
					if (Options.LOG.ordinal() >= CustomTypes.LogLevel.WARN.ordinal())
						System.out.printf("[Player] WARN: Player alias \"%s\" duplicated. Ignoring...\n", player_alias);

					continue;
				}
				if (!roles.containsKey(player_role_alias)) {
					if (Options.LOG.ordinal() >= CustomTypes.LogLevel.WARN.ordinal())
						System.out.printf(
								"[Player] WARN: Invalid role \"%s\" specified for player \"%s\". Ignoring...\n",
								player_role_alias, player_alias);

					continue;
				}

				if (!cities.containsKey(player_city_alias)) {
					if (Options.LOG.ordinal() >= CustomTypes.LogLevel.WARN.ordinal())
						System.out.printf(
								"[Player] WARN: Invalid location \"%s\" specified for player \"%s\". Ignoring...\n",
								player_city_alias, player_alias);

					continue;
				}

				// Resolves fully quaalified Role and City objects by the alias int he file
				player_role = roles.get(player_role_alias);
				player_city = cities.get(player_city_alias);

				// Creates Player object and adds it to the dictionary
				Player player = new Player(player_alias, player_city, player_role);
				players.put(player_alias, player);

				if (Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
					System.out.printf("[Player] INFO: New player added\n");

				if (Options.LOG.ordinal() >= CustomTypes.LogLevel.DUMP.ordinal())
					player.dump();
			}

			br.close();
		} catch (Exception e) {
			System.out.printf("CRITICAL: Exception while parsing\n");
			System.err.println(e.getMessage());
			System.exit(0);
		}

		// Binds players to game
		return players;
	}

	/*
	 * Resolves player order based on initial player hand (as per the original game)
	 */
	public static ArrayList<String> resolvePlayerOrder(Hashtable<String, Player> players) {

		// Target player order & aux hashtable used to identify the highest card of each
		// player
		ArrayList<String> p_order = new ArrayList<String>();
		Hashtable<String, Integer> p_bestCard = new Hashtable<String, Integer>();

		// Evaluates every card in the player hand
		for (Player p : players.values()) {
			if (p.hand == null) {
				if (Options.LOG.ordinal() >= CustomTypes.LogLevel.CRITICAL.ordinal())
					System.out
							.printf("[Player] CRITICAL: Cannot resolve player order. At least a player have no hand\n");

				return null;
			}

			// Iterates every card in the player hand to find the city with the highest
			// population
			int highest_population = 0;
			for (CityCard c : p.hand.values()) {

				if (c.city != null) {
					int c_population = c.city.population;

					// Evaluates city population against the highest found value
					if (c_population > highest_population) {
						highest_population = c_population;
					}
				}
			}

			// Resolves player highest population
			p_bestCard.put(p.alias, highest_population);

			// Evaluates ordered list and checks the highest city population of every sorted
			// player
			int sorted_index = 0;
			for (String sorted_p : p_order) {
				if (p_bestCard.get(sorted_p) < highest_population) {
					break;
				}

				sorted_index++;
			}

			// Appends player in the right order
			p_order.add(sorted_index, p.alias);

		}
		/*
		 * // Dummy order Set<String> p_alias = players.keySet(); for (String alias :
		 * p_alias) { p_order.add(alias); }
		 */

		return p_order;
	}

	// Setters/Getters
	public City getCity() {
		return this.city;
	}

	public void setCity(City c) {
		// Keeps conherence. A player can only be in a single city
		if (this.city != null) {
			this.city.players.remove(this.alias);
		}
		this.city = c;
		c.players.put(this.alias, this);
	}

	public HashMap<String, CityCard> getHand() {
		return this.hand;
	}

	public void setRole(Role r) {
		this.prole = r;

		// Cross-reference. Role has a reference to the assigned player
		prole.bindPlayer(this);
	}

	public Role getRole() {
		return this.prole;
	}

	public CityCard removeCard(String c) {
		return this.hand.remove(c);
	}

	public void addCard(CityCard c) {
		if (c.getCity() != null) {
			this.hand.put(c.getCity().alias, c);
		}
	}

	// Dummy method to print disease data
	public void dump() {
		System.out.printf(">>Printing player data (%s)\n", this.alias);
		System.out.printf(".Current location: %s\n", this.city.name);
		System.out.printf(".Role: %s\n", this.prole.name);

		System.out.println();
	}
}
