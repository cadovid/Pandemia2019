package game;

import java.util.*;

// Local imports
import player.*;
import dis.*;
import city.*;
import board.*;
import card.*;
import graphics.Renderer;
import _aux.Datapaths;
import _aux.Options;
import _aux.CustomTypes;

// Jason
import jason.asSyntax.*;
import jason.environment.*;
import java.util.logging.*;

/*
  Game class
    Contains all relevant game data and structures
    Initializes game objects (board and players), and executes game
*/
public class Game extends jason.environment.Environment {
	public static final Literal TURN = Literal.parseLiteral("turn");
	public static final Literal ACTIONS_LEFT = Literal
			.parseLiteral("actions_left(" + _aux.Options.PLAYER_MAX_ACTIONS + ")");

	private Logger logger = Logger.getLogger("pandemic.mas2j." + Game.class.getName());

	// Game board
	public Board board;

	// GameStatus object. Contains current game relevant data
	public GameStatus gs;

	// Counters
	public int n_roles;
	public int n_diseases;
	public int n_cities;
	public int n_players;

	// Dictionaries (key, value) - each value is retrieved as: dictionary.get(key)
	public Hashtable<String, Role> roles;
	public Hashtable<String, Disease> diseases;
	public Hashtable<String, City> cities;
	public Hashtable<String, Player> players;

	// Helpful lists
	public ArrayList<String> p_order;

	// List of active infections in the game
	public ArrayList<Infection> infections;

	// Cards
	public ArrayList<Card> c_cities;
	public ArrayList<Card> c_infection;

	// Decks
	public Deck d_game;
	public Deck d_infection;
	public Deck d_game_discards;
	public Deck d_infection_discards;

	// GRA & gameplay
	private Renderer render;
	public CustomTypes.GameMode gm = CustomTypes.GameMode.TURN;
	public boolean runTurn = false;

	// Constructor. Loads data and initializes board.
	public Game() {
		if (Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
			System.out.printf("[Game] INFO - Initializing game environment...\n");

		// Parses game data from init files
		this.parseData();

		// Game status variable contains relevant info
		this.gs = new GameStatus();

		// GRA - Initializes renderer
		this.render = new Renderer(this, null, board);

		if (Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
			System.out.printf("[Game] INFO - Environment ready!\n");
	}

	/*
	 * parseData Reads data from files and generates basic structures
	 */
	public void parseData() {
		// Parses data from input files
		this.roles = Role.parseRoles(Datapaths.role_list);
		this.diseases = Disease.parseDis(Datapaths.disease_list);
		this.cities = City.parseCities(Datapaths.city_list, this.diseases);
		this.players = Player.parsePlayers(Datapaths.player_list, this.roles, this.cities);

		// When generating the board, a list of used cities (out of the available ones)
		// is created as well
		this.board = new Board(Datapaths.map, this.cities);

		// Sets counters
		this.n_roles = this.roles.size();
		this.n_diseases = this.diseases.size();
		this.n_cities = this.cities.size();
		this.n_players = this.players.size();

		// Initializes the rest of aux lists
		this.p_order = new ArrayList<String>();
		// this.infections = new ArrayList<Infection>();
	}

	/*
	 * Resolves initial configuration (decks, player order, player hands)
	 */
	public void setGame() {
		/*
		 * Initializes cards and decks from City objects
		 */
		c_cities = Card.parseCities(new ArrayList<City>(cities.values()), CustomTypes.CardType.CITY);
		c_infection = Card.parseCities(new ArrayList<City>(cities.values()), CustomTypes.CardType.INFECTION);

		d_game = new Deck(c_cities, CustomTypes.DeckType.GAME);
		d_infection = new Deck(c_infection, CustomTypes.DeckType.INFECTION);
		d_game_discards = new Deck(CustomTypes.DeckType.GAME);
		d_infection_discards = new Deck(CustomTypes.DeckType.INFECTION);

		/*
		 * Shuffles decks
		 */
		d_game.shuffle();
		d_infection.shuffle();

		/*
		 * Players steal from deck its initial hands (random order; hand size depends on
		 * the number of players)
		 */
		int initHandSize = Options.initialHandSize(players.size());
		for (Player p : players.values()) {
			p.hand = d_game.draw(initHandSize);
		}

		/*
		 * Defines player order from cards
		 */
		p_order = Player.resolvePlayerOrder(players);

		/*
		 * Builds research center and puts players in initial city (assumes a valid set
		 * of cities has already been loaded with the board)
		 */
		City starting_city = this.cities.get(this.board.used_cities.get(0));
		starting_city.buildResearch(this.gs);

		for (Player p : players.values()) {
			p.move(starting_city);
		}

		/*
		 * Spread initial infections across cities as per the game rules (drawing from
		 * infection deck)
		 */

		// Infects 3 cities with an infection level of 3 (3 cubes)
		for (Card c_3 : d_infection.draw(3)) {
			City infected_city = c_3.city;

			// Creates an Infection object and adds it to the city
			Infection infection = new Infection(infected_city.local_disease, infected_city, 3);
			infected_city.infect(infection);

			// Adds card to discard deck
			d_infection_discards.stack(c_3);
		}

		// Infects 3 cities with an infection level of 2 (2 cubes)
		for (Card c_2 : d_infection.draw(3)) {
			City infected_city = c_2.city;

			// Creates an Infection object and adds it to the city
			Infection infection = new Infection(infected_city.local_disease, infected_city, 2);
			infected_city.infect(infection);

			// Adds card to discard deck
			d_infection_discards.stack(c_2);
		}

		// Infects 3 cities with an infection level of 1 (1 cubes)
		for (Card c_1 : d_infection.draw(3)) {
			City infected_city = c_1.city;

			// Creates an Infection object and adds it to the city
			Infection infection = new Infection(infected_city.local_disease, infected_city, 1);
			infected_city.infect(infection);

			// Adds card to discard deck
			d_infection_discards.stack(c_1);
		}

		/*
		 * Generates epidemic cards (as many as specified in Options)
		 */
		ArrayList<Card> epidemics = new ArrayList<Card>();
		for (int i = 0; i < Options.CARD_TOTAL_EPIDEMICS; i++) {
			Card epidemic = new Card(CustomTypes.CardType.EPIDEMIC);
			epidemics.add(epidemic);
		}

		/*
		 * Puts epidemics cards into the game deck (evenly distributed, as per the game
		 * rules)
		 */
		d_game.shove(epidemics);

		this.initialBeliefs();

		// GRA - Refresh graphics
		this.render.refresh(null, null);
	}

	// Sets initial beliefs from environment
	public void initialBeliefs() {
		// Position of cities: at(city,alias,x,y)
		for (City c : this.cities.values()) {
			addPercept(Literal.parseLiteral("at(" + c.alias + "," + c.cell.x + "," + c.cell.y + ")"));
		}

		// Adjacent cities: adjacent(city,adjacent_city)
		// Iterates every key in the adjacent_cities dictionary
		for (String c_alias : this.board.adjacent_cities.keySet()) {

			// Iterates every adjacent city assigned to c_alias city and parses the percept
			for (String adjacent : this.board.adjacent_cities.get(c_alias)) {
				addPercept(Literal.parseLiteral("adjacent(" + c_alias + ", " + adjacent + ")"));
			}
		}

		// Infection level of every city (sum of all infection levels):
		// infectionLVL(city,level)
		this.refreshPercepts_infections();

		// Active diseases info: disease(disease_alias, n_spreads_left)
		for (Disease dis : this.diseases.values()) {
			addPercept(Literal.parseLiteral("disease(" + dis.alias + "," + dis.spreads_left + ")"));
		}

		// TODO: Infected cities: infected(city_alias,disease_alias,spread_level)

		// Position of players: at(city_alias)
		// Specifically added to each player
		for (Player p : this.players.values()) {
			addPercept(p.prole.alias, Literal.parseLiteral("at(" + p.city.alias + ")"));
		}

		// TODO Position of research centers: research(city_alias)

		// Player cards: card(player_alias,card_city_alias)
		for (Player p : this.players.values()) {
			this.refreshPercepts_playerHand(p);
		}

		// Tells initial player to start its turn
		this.refreshPercepts_playerTurn(this.players.get(this.p_order.get(0)).prole.alias);
		
		// Game mode
		if(_aux.Options.GP_TIMEOUT) {
			addPercept(Literal.parseLiteral("control_timeout("+ _aux.Options.GP_TIMEOUT_SLEEP +")"));
		}
		else {
			addPercept(Literal.parseLiteral("control_manual)"));
		}
	}
	
	// Sets initial player turn beliefs
	public void refreshPercepts_playerTurn(String p) {		
		
		// Sets new percepts
		addPercept(p, TURN);
		addPercept(p, ACTIONS_LEFT);
		
		this.render.refresh(null, null);
	}
	
	// Sets player turn beliefs (replaces another turn)
	public void refreshPercepts_playerTurn(String p_old, String p_new) {
		
		// Removes old percepts
		removePercept(p_old, TURN);
		removePercept(p_old, ACTIONS_LEFT);
		
		
		// Sets new percepts
		this.refreshPercepts_playerTurn(p_new);
	}
	
	// Refreshes percepts regarding a player's hand
	public void refreshPercepts_playerHand(Player p) {
		removePerceptsByUnif(Literal.parseLiteral("card(" + p.prole.alias + ",_)"));
		
		ArrayList<Card> phand = p.hand;
		for (Card c : phand) {
			addPercept(this.players.get(this.p_order.get(0)).prole.alias,
					Literal.parseLiteral("card(" + p.prole.alias + "," + c.city.alias + ")"));
		}
	}

	// Refreshes percepts regarding active infections and infection level of each
	// city
	public void refreshPercepts_infections() {
		removePerceptsByUnif(Literal.parseLiteral("infectionLVL(_,_)"));
		
		for (String c : this.board.used_cities) {
			int ilevel = 0;

			// Iterates infection in every city
			for (Infection i : this.cities.get(c).infections.values()) {
				ilevel += i.spread_level;
			}

			// Adds percept
			addPercept(Literal.parseLiteral("infectionLVL(" + c + "," + ilevel + ")"));
		}
	}
	
	// Buttons behaviour
	public void control_feedback(_aux.CustomTypes.GameMode gm) {
		removePercept(Literal.parseLiteral("control_manual"));
		removePerceptsByUnif(Literal.parseLiteral("control_timeout(_)"));
		
		
		if(gm == _aux.CustomTypes.GameMode.TIMESTAMP) {
			addPercept(Literal.parseLiteral("control_timeout("+ _aux.Options.GP_TIMEOUT_SLEEP +")"));
			addPercept(Literal.parseLiteral("control_run"));
		}
		
		// Manual control
		else {
			// control_run is removed from agent when its turn finishes
			
			addPercept(Literal.parseLiteral("control_manual"));
			addPercept(Literal.parseLiteral("control_run"));
		}
	}

	// TODO: Implement the rest of methods regarding percepts update (each unique
	// set of percepts must be handled properly)

	// TODO: Resolve an epidemic. This method should be called whenever an epidemic
	// card is drawn
	public void epidemic() {
		return;
	}

	@Override
	// Needs to define the agent individually
	public boolean executeAction(String agName, Structure action) {
		
		//this.render.refresh(null, null);
		
		// Resolves action name and player (agName must be equal to the role alias)
		String aname = action.getFunctor();
		Player p = this.roles.get(agName).player;

		logger.info("Trying to execute action...");
		if (aname.equals("init")) {
			logger.info("Adding percept.");
			addPercept(Literal.parseLiteral("init"));
			return true;
		}
		
		// Removes a percept from the caller agent
		else if (aname.equals("removeAgPercept")) {
			if (action.getArity() != 1) {
				logger.info("[executeAction] - Wrong number of params for action \"removeAgPercept\"");
				return false;
			}
			
			String perc = action.getTerm(0).toString();
			removePercept(agName, Literal.parseLiteral(perc));
			return true;
			
		}
		
		// Removes a percept from the environment
		else if (aname.equals("removePercept")) {
			if (action.getArity() != 1) {
				logger.info("[executeAction] - Wrong number of params for action \"removePercept\"");
				return false;
			}
			
			String perc = action.getTerm(0).toString();
			
			removePercept(Literal.parseLiteral(perc));
			return true;
			
		}

		// Ends agent turn and triggers next player
		// New player draws cards from deck
		else if (aname.equals("turnover")) {
			// Game control feedback
			removePercept(Literal.parseLiteral("control_run"));
			
			// Last player alias & order
			String old_player = this.roles.get(agName).player.alias;
			int old_player_order = this.p_order.indexOf(old_player);

			// New player alias (player role is used to identify the agent)
			// Uses modulo operation to iterate players as a circular buffer (once the last
			// player has ended its turn, the first player starts again)
			String new_player = this.p_order.get(Math.floorMod(old_player_order + 1, this.n_players));

			// Draw player cards
			ArrayList<Card> old_player_draw = this.d_game.draw(_aux.Options.PLAYER_DRAW_CARDS);

			// Game deck depleted. Game over
			if (old_player_draw == null) {
				logger.info("Game deck out of cards... Game Over!");
				this.stop();
				
				return true;
			}

			// Extends player hand
			else {
				// Checks whether an EPIDEMIC card has been drawn
				// Iterates list backwards, so elements can be removed without having to alter the moving index
				for(int i = old_player_draw.size(); i > 0; i--) {
					if(old_player_draw.get(i-1).type == _aux.CustomTypes.CardType.EPIDEMIC) {
						
						// Discards card from player hand
						this.d_game_discards.stack(old_player_draw.get(i-1));
						old_player_draw.remove(i-1);
						this.epidemic();
						
					}
				}
				
				// Adds drawn cards to player hand
				ArrayList<Card> old_player_hand = this.players.get(old_player).hand;
				old_player_hand.addAll(old_player_draw);

				// If player hand exceeds size limit, some cards must be discarded
				int n_cards_over = old_player_hand.size() - _aux.Options.PLAYER_MAX_CARDS;

				// TODO: Discard must follow some strategy, the actual implementation just
				// removes cards from the beginning of the hand. This heavily depends on the agent implementation...
				if (n_cards_over > 0) {
					for (; n_cards_over > 0; n_cards_over--) {
						old_player_hand.remove(0);
					}
				}

				// Updates global percepts (flushes old percepts regarding player hand and add
				// new ones)
				this.refreshPercepts_playerHand(this.players.get(old_player));

				// Draw infection cards (as per the current infection rate)
				for (int i = 0; i < this.gs.infection_levels[this.gs.current_infection_level]; i++) {
					Card draw = this.d_infection.draw();

					// Infection game depleted. Game Over
					if (draw == null) {
						logger.info("Infection deck out of cards... Game Over!");
						this.stop();
						
						return true;
					}
					City infected_city = draw.city;

					// Creates an Infection object and adds it to the city
					Infection infection = new Infection(infected_city.local_disease, infected_city, 1);
					infected_city.infect(infection);

					// Adds card to discard deck
					d_infection_discards.stack(draw);

					// Updates global percepts (flushes old percepts regarding infections and add
					// new ones)
					this.refreshPercepts_infections();
				}
			}

			// Updates turns
			this.refreshPercepts_playerTurn(agName, this.players.get(new_player).prole.alias);

			logger.info("Turn changed from " + agName + " to " + this.players.get(new_player).prole.alias);

			return true;

		}

		// Moves agent to a city
		else if (aname.equals("moveto")) {
			if (action.getArity() != 1) {
				logger.info("[executeAction] - Wrong number of params for action \"moveto\"");
				return false;
			}

			String starting_city = p.city.alias;
			String target_city = action.getTerm(0).toString();

			// Updates objects
			p.move(this.cities.get(target_city));

			// Updates beliefs
			removePercept(agName, Literal.parseLiteral("at(" + starting_city + ")"));
			addPercept(agName, Literal.parseLiteral("at(" + target_city + ")"));

			logger.info("Moved to " + target_city);

			return true;
		}

		// Reduces the infection level of a city
		// TODO: This must be redefined: heal action should take 2 params: city &
		// disease, so the agent can heal a specific disease
		else if (aname.equals("heal")) {
			if (action.getArity() != 1) {
				logger.info("[executeAction] - Wrong number of params for action \"moveto\"");
				return false;
			}

			String city_alias = action.getTerm(0).toString();
			City city = this.cities.get(city_alias);

			// TODO: (Dummy selection: takes the first disease in the list and reduces its
			// spread level by 1)
			List<String> keysAsArray = new ArrayList<String>(city.infections.keySet());
			Disease dis = city.infections.get(keysAsArray.get(0)).dis;
			city.heal(dis, 1);

			// Recomputes infection level
			int ilevel = 0;

			// Iterates infection in every city
			for (Infection i : city.infections.values()) {
				ilevel += i.spread_level;
			}

			// Updates beliefs
			//removePercept(agName, Literal.parseLiteral("infectionLVL(" + city_alias + ",_)"));
			//addPercept(agName, Literal.parseLiteral("infectionLVL(" + city_alias + "," + (ilevel) + ")"));
			this.refreshPercepts_infections();

			logger.info("Healed");
			logger.info("Infection LVL of " + city_alias + ": " + ilevel);

			return true;

		}
		
		// Moves agent to a random nearby city
		else if (aname.equals("moveto_adjacentRandom")) {
			//TODO: Random movement to adjacent cells
		}
		
		else {
			logger.info("executing: " + action + ", but not implemented!");
		}

		return false;
	}

	// Called before the end of MAS execution
	@Override
	public void stop() {
		super.stop();
		
		addPercept(Literal.parseLiteral("gameover"));
	}

	// Called before the MAS execution with the args informed in .mas2j
	@Override
	public void init(String[] args) {
		// Must settle initial game status (both, beliefs and configuration)
		this.setGame(); // RAWR - could be redundant; remove wrapper and define functionality here
	}
}
