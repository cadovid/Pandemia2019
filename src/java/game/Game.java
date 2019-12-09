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
import _aux.CustomTypes.Direction;
import _aux.CustomTypes.Round;

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

	private Logger logger = Logger.getLogger("pandemic.mas2j." + Game.class.getName());

	public static final Term MOVE_ADJACENT = Literal.parseLiteral("moveAdjacent(direction)");
	public static final Term DIRECT_FLIGHT = Literal.parseLiteral("directFlight(dest)");
	public static final Term CHARTER_FLIGHT = Literal.parseLiteral("charterFlight(dest)");
	public static final Term AIR_BRIDGE = Literal.parseLiteral("airBridge(dest)");
	public static final Term BUILD_CI = Literal.parseLiteral("buildCI");
	public static final Term TREAT_DISEASE = Literal.parseLiteral("treatDisease(disease)");
	public static final Term SHARE_INFO = Literal.parseLiteral("shareInfo(player, city, cp_giver)");
	public static final Term DISCOVER_CURE = Literal.parseLiteral("discoverCure(disease)");
	public static final Term DISCARD_CARD = Literal.parseLiteral("discardCard(card, player)");
	public static final Term PASS_TURN = Literal.parseLiteral("passTurn");

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
	// The agents must have the same alias than players
	public Hashtable<String, Player> players;
	public ArrayList<String> p_order; // Player order
	public ArrayList<CityCard> city_cards;

	// GRA
	private Renderer render;

	public ArrayList<CityCard> cards_player;
	public ArrayList<CityCard> discarded_pcards = new ArrayList<CityCard>(); // discarded cards public
	public ArrayList<CityCard> cards_infection;
	public ArrayList<CityCard> discarded_icards = new ArrayList<CityCard>();

	// Constructor. Loads data and initializes board.
	public Game() {
		if (Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
			System.out.printf("[Game] INFO - Initializing game environment...\n");

		// Parses game data from init files
		this.parseData();

		if (Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
			System.out.printf("[Game] INFO - Environment ready!\n");
	}

	// Called before the end of MAS execution
	@Override
	public void stop() {
		super.stop();
	}

	// Called before the MAS execution with the args informed in .mas2j
	@Override
	public void init(String[] args) {
		// Needs to communicate number of players to supplicant. When all players are
		// ready, supplicant should remove the init belief

		// Dummy
		// addPercept("supplicant", Literal.parseLiteral("nPlayers(" + this.n_players +
		// ")"));
		addPercept("supplicant", Literal.parseLiteral("nPlayers(" + this.n_players + ")"));

		// Adds initial percept to supplicant agent
		addPercept("supplicant", Literal.parseLiteral("init"));

		logger.info("Starting game\n");
		// Initializes game

		// Since class is extended from environment, is already initialized, hence
		// there's no need to create the object
		// Game g = new Game();
		Board board = new Board(Datapaths.map, this.cities);
		GameStatus gs = new GameStatus(board);

		// TODO
		/*
		 * Settle initial game configuration. Needs to implement: Initial diseases (draw
		 * infection cards as per the real game) Player hands (draw deck cards) Player
		 * order (hand-dependant, as per the real game)
		 */

		// Dummy. Method to resolve player order. Should be redefined.
		this.p_order = Player.resolvePlayerOrder(this.players);

		// GRA - Initializes renderer
		this.render = new Renderer(this, null, board);
		logger.info("Ready");
	}

	/*
	 * parseData Reads data from files and generates basic structures
	 */
	public void parseData() {
		this.roles = Role.parseRoles(Datapaths.role_list);

		this.diseases = Disease.parseDis(Datapaths.disease_list);
		this.cities = City.parseCities(Datapaths.city_list, this.diseases);
		this.players = Player.parsePlayers(Datapaths.player_list, this.roles, this.cities);

		this.n_roles = this.roles.size();
		this.n_diseases = this.diseases.size();
		this.n_cities = this.cities.size();
		this.n_players = this.players.size();
	}

	@Override
	public boolean executeAction(String ag, Structure action) {
		logger.info(ag + " doing: " + action);
		boolean consumed_action = false;
		try {
			if (gs.round == Round.ACT) {
				if (action.equals(MOVE_ADJACENT)) {
					if (moveAdjacent(gs.cp, Direction.values()[((int) ((NumberTerm) action.getTerm(0)).solve())])) {
						consumed_action = true;
						automaticDoctorDiseasesTreatment();
					} else {
						return false;
					}
				} else if (action.equals(DIRECT_FLIGHT)) {
					City dest = cities.get(((StringTerm) action.getTerm(0)).toString());
					if (directFlight(gs.cp, dest)) {
						consumed_action = true;
						automaticDoctorDiseasesTreatment();
					}
				} else if (action.equals(CHARTER_FLIGHT)) {
					City dest = cities.get(((StringTerm) action.getTerm(0)).toString());
					if (charterFlight(gs.cp, dest)) {
						consumed_action = true;
						automaticDoctorDiseasesTreatment();
					}
				} else if (action.equals(AIR_BRIDGE)) {
					City dest = cities.get(((StringTerm) action.getTerm(0)).toString());
					if (airBridge(gs.cp, dest)) {
						consumed_action = true;
						automaticDoctorDiseasesTreatment();
					}
				} else if (action.equals(BUILD_CI)) {
					if (putInvestigationCentre(gs.cp.getCity())) {
						consumed_action = true;
					}
				} else if (action.equals(TREAT_DISEASE)) {
					String dis_alias = ((StringTerm) action.getTerm(0)).toString();
					// TODO: treatDisease() with the Epidemic object or the final decision to handle
					// this
					// if (treatDisease() {
					// consumed_action = true;
					// } else {
					// consumed_action = false;
					// }
				} else if (action.equals(SHARE_INFO)) {
					String player_alias = ((StringTerm) action.getTerm(0)).toString();
					String city_name = ((StringTerm) action.getTerm(1)).toString();
					boolean cp_giver = Boolean.parseBoolean(((StringTerm) action.getTerm(1)).toString());
					shareInfo(player_alias, city_name, cp_giver);
					consumed_action = true;
				} else if (action.equals(DISCOVER_CURE)) {
					String dis_alias = ((StringTerm) action.getTerm(0)).toString();
					// TODO: discoverCure checks
					if (discoverCure(gs.cp, dis_alias)) {
						consumed_action = true;
					} else {
						return false;
					}
				} else if (action.equals(PASS_TURN)) {
					consumed_action = false;
					gs.p_actions_left = 0;
				} else {
					return false;
				}
			}
			if (action.equals(DISCARD_CARD)) {
				String city = ((StringTerm) action.getTerm(0)).toString();
				String player_alias = ((StringTerm) action.getTerm(1)).toString();
				players.get(player_alias).removeCard(city);
				// This action does never consume action, it happens in draw phase
				consumed_action = false;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (gs.round == Round.ACT) {
			if (consumed_action) {
				gs.p_actions_left--;
			}
			// It should never be lower than 0, but in case
			if (gs.p_actions_left <= 0) {
				// Set to 0 because the draw phase begins
				gs.p_actions_left = 0;
				gs.round = Round.STEAL;
			}
		}
		if (gs.round == Round.STEAL) {
			if (gs.cp.getHand().size() <= Options.PLAYER_MAX_CARDS) {
				while (gs.round == Round.STEAL) {
					drawPLayerCard(gs.cp);
					gs.drawnCards++;
					if (gs.cp.getHand().size() > Options.PLAYER_MAX_CARDS) {
						// One card must be discarded, so the actions are delayed
						// until the agent calls the discard action
						break;
					} else if (gs.drawnCards == Options.PLAYER_DRAW_CARDS) {
						gs.round = Round.INFECT;
						for (int i = 0; i < gs.infection_levels[gs.current_infection_level]; i++) {
							drawInfectCard();
						}
						gs.round = Round.ACT;
						gs.p_actions_left = Options.PLAYER_MAX_ACTIONS;
						gs.cp = nextPlayer(gs.cp);
						// For the next turn
						gs.drawnCards = 0;
					}
				}
			} else {
				System.out.printf("[Game] WARNING: Agent must discard card but it does not call the action!!\n");
			}
		}

		updatePercepts();

		try {
			Thread.sleep(200);
		} catch (Exception e) {
		}
		informAgsEnvironmentChanged();
		return true;
	}

	/** creates the agents perception */

	void updatePercepts() {
		clearPercepts();

		// All percepts are added to all agents except the remaining actions,
		// that depends on the agent
		for (Player p : players.values()) {
			if (gs.cp.getHand().size() > Options.PLAYER_MAX_CARDS) {
				addPercept(p.alias, Literal.parseLiteral("cardMustBeenDiscarded"));
			}
			if (gs.cp.equals(p)) {
				addPercept(p.alias, Literal.parseLiteral("left_actions(" + gs.p_actions_left + ")"));
			} else {
				addPercept(p.alias, Literal.parseLiteral("left_actions(" + 0 + ")"));
			}
		}

		// Percepts for cards and location of players
		for (Player player : this.players.values()) {
			addPercept(Literal.parseLiteral("at(" + player.alias + "," + player.alias + ")"));
			for (String new_card : player.getHand().keySet()) {
				addPercept(Literal.parseLiteral("hasCard(" + player.alias + "," + new_card + ")"));
			}
		}

		// Percepts for spread level and CI per city
		for (City city : this.cities.values()) {
			if (city.canResearch()) {
				addPercept(Literal.parseLiteral("hasCI(" + city.alias + ")"));
			}
			for (Epidemic epidemic : city.getEpidemics()) {
				addPercept(Literal.parseLiteral("spreadLevel(" + epidemic.city_host.alias + "," + epidemic.dis.alias
						+ "," + epidemic.spread_level + ")"));
			}
		}

		// Percepts for disease cures
		for (Disease dis : this.diseases.values()) {
			if (dis.cure) {
				addPercept(Literal.parseLiteral("isCure(" + dis.alias + ")"));
			}
		}

		// TODO: percepts for all agents
		addPercept(Literal.parseLiteral(""));
	}

	/**
	 * Returns the next player in function of the current player.
	 * 
	 * @param Player: cp, the current player.
	 * @return Player: the next player.
	 */
	public Player nextPlayer(Player cp) {
		boolean current_found = false;
		String next_alias = null;
		Player next_player = null;
		for (String alias : p_order) {
			if (current_found) {
				next_alias = alias;
			}
			if (alias.equals(cp.alias)) {
				current_found = true;
			}
		}
		// The next one is the first player
		if (next_alias == null && current_found) {
			next_alias = p_order.get(0);
		}
		next_player = players.get(next_alias);

		return next_player;
	}

	/*
	 * Generate and shuffle the players and infection decks.
	 */
	public void generateDecks(int cards_per_city, int epidemic_cards) {
		Collection<City> setOfCities = this.cities.values();
		for (City c : setOfCities) {
			for (int i = 0; i < cards_per_city; i++) {
				this.cards_player.add(new CityCard(c, false));
				this.cards_infection.add(new CityCard(c, false));
			}
		}

		for (int i = 0; i < epidemic_cards; i++) {
			this.cards_player.add(new CityCard(null, true));
		}

		Collections.shuffle(this.cards_player);
		Collections.shuffle(this.cards_infection);

	}

	public void shareInfo(String player, String city_name, boolean cp_giver) {
		Player giver, receiver;
		// Select who gives the card and who receives it
		if (cp_giver) {
			giver = this.gs.cp;
			receiver = this.players.get(player);
		} else {
			receiver = this.gs.cp;
			giver = this.players.get(player);
		}
		CityCard card = giver.removeCard(city_name);
		// The discard percept is added in updatePercepts() after doing the
		// action

		receiver.addCard(card);
	}

	public boolean drawPLayerCard(Player player) {
		// Si no quedan cartas que robar, devuelve false
		boolean enoughCards = false;
		if (!this.cards_player.isEmpty()) {
			enoughCards = true;
			CityCard new_card = this.cards_player.remove(0);
			this.discarded_pcards.add(new_card);
			if (new_card.isEpidemic()) {
				// TODO
				// propagate()
				// infect()
				increaseInfectionLevel();
			} else {
				player.addCard(new_card);
			}
		}
		return enoughCards;
	}

	public boolean drawInfectCard() {
		// Si no quedan cartas que robar, devuelve false
		boolean enoughCards = false;
		if (!this.cards_infection.isEmpty()) {
			enoughCards = true;
			CityCard new_card = this.cards_infection.remove(0);
			this.discarded_icards.add(new_card);
			infect(new_card.getCity(), new_card.getDisease());
		}

		return enoughCards;
	}

	/*
	 * discoverCure Remove to the player hands needed cards and set in the selected
	 * disease the attribute cure to True
	 */
	public boolean discoverCure(Player current_player, String diseaseAlias) {
		Disease disease = this.diseases.get(diseaseAlias);
		ArrayList<String> to_discard = new ArrayList<String>();
		for (Map.Entry<String, CityCard> entry : current_player.getHand().entrySet()) {
			City city = entry.getValue().getCity();
			if (city.local_disease.alias == diseaseAlias) {
				to_discard.add(entry.getKey());
				if (to_discard.size() == 5
						|| (to_discard.size() == 4 && current_player.getRole().alias.equals("genetist"))) {
					break;
				}
			}
			for (String city_alias : to_discard) {
				current_player.removeCard(city_alias);
			}
			// discarded_pcards.add(c);
		}
		disease.setCure(true);
		automaticDoctorDiseasesTreatment();
		return true;
	}

	public boolean moveAdjacent(Player current_player, Direction destination) {
		boolean moved = false;

		if (current_player.getCity().getNeighbors().get(destination) != null) {
			current_player.getCity().removePlayer(current_player);
			current_player.setCity(current_player.getCity().getNeighbors().get(destination));
			current_player.getCity().putPlayer(current_player);
			moved = true;
		}
		return moved;
	}

	/**
	 * Flies to the destination city discarding one card of the hand of that city.
	 * 
	 * @param destination
	 * @return true if moved and false otherwise.
	 */
	public boolean directFlight(Player current_player, City destination) {
		boolean moved = false;
		for (Card card : current_player.getHand().values()) {
			if (card.getCity() == destination) {
				current_player.getCity().removePlayer(current_player);
				current_player.setCity(destination);
				current_player.getCity().putPlayer(current_player);
				current_player.getHand().remove(card.getCity().alias);
				return true;
			}
		}

		return moved;
	}

	/**
	 * Flies to the destination city discarding one card of the hand of the current
	 * city.
	 * 
	 * @param destination
	 * @return true if moved and false otherwise.
	 */
	public boolean charterFlight(Player current_player, City destination) {
		boolean moved = false;
		for (Card card : current_player.getHand().values()) {
			if (card.getCity() == current_player.getCity()) {
				current_player.getCity().removePlayer(current_player);
				current_player.setCity(destination);
				current_player.getCity().putPlayer(current_player);
				current_player.getHand().remove(card.getCity().alias);
				return true;
			}
		}

		return moved;
	}

	/**
	 * Flies to the destination city if there is a investigation center in the
	 * current city and another one in the destination city.
	 * 
	 * @param destination
	 * @return true if moved and false otherwise.
	 */
	public boolean airBridge(Player current_player, City destination) {
		boolean moved = false;
		if (current_player.getCity().canResearch() && destination.canResearch()) {
			current_player.getCity().removePlayer(current_player);
			current_player.setCity(destination);
			current_player.getCity().putPlayer(current_player);
			moved = true;
		}

		return moved;
	}

	/**
	 * Puts a investigation centre in the city.
	 */
	public boolean putInvestigationCentre(City city) {
		if (gs.current_research_centers < Options.MAX_RESEARCH_CENTERS - 1) {
			city.can_research = true;
			gs.current_research_centers++;
			return true;
		} else {
			return false;
		}
	}

	void automaticDoctorDiseasesTreatment() {
		if (gs.cp.getRole().alias.equals("doctor")) {
			for (Epidemic e : gs.cp.getCity().getEpidemics()) {
				if (e.dis.cure) {
					e.dis.heal(e.spread_level);
					e.spread_level = 0;
				}
			}
		}
	}

	/*
	 * Infecta las ciudades de manera recursiva
	 */
	public void infect(City city, Disease dis) {

		Epidemic epidemic = city.getEpidemic(dis);

		// If max spread level exceeded, then an outbreak occurs.
		if (epidemic.spread_level + 1 > Options.MAX_SPREADS_PER_CITY) {

			// Updates disease total spreads
			dis.spread(Options.MAX_SPREADS_PER_CITY - epidemic.spread_level);
			epidemic.spread_level = Options.MAX_SPREADS_PER_CITY;

			// Expands to adjacent cities
			for (City neigh : city.getNeighbors().values()) {
				infect(neigh, dis);
			}
		} else {

			epidemic.spread_level = epidemic.spread_level + 1;
		}
	}

	/*
	 * Incrementa el nivel de epidemia con respecto al array de niveles
	 */
	public boolean increaseInfectionLevel() {
		boolean increased = false;

		if (gs.current_infection_level < gs.infection_levels.length) {
			gs.current_infection_level++;
			increased = true;
		}

		return increased;
	}

	// OLD INITIALIZATION; ONLY AS A REFERENCE!: main class is useless if using game
	// as environment. Initialization must be done in the init method...
	public static void OLDmain(String args[]) {

		// Initializes game
		Game g = new Game();
		Board board = new Board(Datapaths.map, g.cities);
		GameStatus gs = new GameStatus(board);

		// TODO
		/*
		 * Settle initial game configuration. Needs to implement: Initial diseases (draw
		 * infection cards as per the real game) Player hands (draw deck cards) Player
		 * order (hand-dependant, as per the real game)
		 */

		// Dummy. Method to resolve player order. Should be redefined.
		g.p_order = Player.resolvePlayerOrder(g.players);

		// GRA - Initializes renderer
		g.render = new Renderer(g, null, board);

		// Game cycle. Runs unitl the game is over
		while (!gs.over) {

			// Refresh graphics for updated cells. MUST KEEP RECORD OF THE UPDATED CITIES ON
			// EACH TURN!
			// GRA - List used to resolve which cities were updated on the current turn
			// (renderer will refresh only those cities)
			ArrayList<String> updated_cities = new ArrayList<String>();

			// Dummy chunk - (test purposes)
			City m = g.cities.get("mad");
			Disease b = g.diseases.get("ban");
			Disease b2 = g.diseases.get("ebo");
			b.spreadToCity(m, 1);
			b2.spreadToCity(m, 3);
			updated_cities.add("mad");
			gs.over = true;
			System.out.printf("[GAME] over!!\n");

			// Updates player number of available actions
			gs.p_actions_left = Options.PLAYER_MAX_ACTIONS;

			// On each round, iterates through all players
			for (String p_alias : g.p_order) {

				// Gets player according to the player order
				Player p = g.players.get(p_alias);

				// Action round
				while (gs.p_actions_left > 0) {

					// TODO - Player resolves action
					gs.p_actions_left--;
				}

				// TODO
				// Draw cards
				/*
				 * Define in Game a list of drawable cards from which the player should pick on
				 * each round p.draw(cards);
				 */

				// TODO
				// Resolve infection
				/*
				 * Define in Game a list of drawable infection cards from which the player
				 * should pick on each round (the cards are activated at game level) Number of
				 * cards to steal is manage through a variable in gs Cities gets automatically
				 * infected. Outbreaks occurs at ease. g.draw(cards);
				 */
			}

			// GRA - Once the player turn has ended, the renderer refreshes the updated
			// cells
			g.render.updateGridCells(updated_cities);
		}
	}
}
