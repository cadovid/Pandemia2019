package game;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

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

	public static final Literal TURN = Literal.parseLiteral("turn");
	public static final Literal ACTIONS_LEFT = Literal
			.parseLiteral("actions_left(" + _aux.Options.PLAYER_MAX_ACTIONS + ")");

	public static final Term MOVE_ADJACENT = Literal.parseLiteral("moveAdjacent(direction)");
	public static final Term MOVE_ADJACENT_CITY = Literal.parseLiteral("moveAdjacentCity(dest)");
	public static final Term MOVE_ADJACENT_RANDOM = Literal.parseLiteral("moveAdjacentRandom");
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
	public int n_outbreaks = 0;

	// Dictionaries (key, value) - each value is retrieved as: dictionary.get(key)
	public Hashtable<String, Role> roles;
	public Hashtable<String, Disease> diseases;
	public Hashtable<String, City> cities;
	// The agents must have the same alias than players
	public Hashtable<String, Player> players;
	public ArrayList<String> p_order; // Player order
	public ArrayList<String> used_cities;
	// List of active infections in the game
	public ArrayList<Infection> infections;
	public ArrayList<CityCard> c_cities;
	public ArrayList<CityCard> c_infection;

	// GRA
	private Renderer render;
	public CustomTypes.GameMode gm = CustomTypes.GameMode.TURN;
	public boolean runTurn = false;
	public boolean waitingManualChange = false;

	// Decks
	public Deck d_game;
	public Deck d_infection;
	public Deck d_game_discards;
	public Deck d_infection_discards;

	// DISTANCE
	// --------------------------------------------------------------------------

	public ArrayList<City> solicitedCards = new ArrayList<City>();

	// DISTANCE
	// --------------------------------------------------------------------------

	// Constructor. Loads data and initializes board.
	public Game() {
		if (Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
			System.out.printf("[Game] INFO - Initializing game environment...\n");

		parseData();
		Board board = new Board(Datapaths.map, this.cities);
		GameStatus gs = new GameStatus(board);
		this.gs = gs;

		// GRA - Initializes renderer
		this.render = new Renderer(this, null, board);

		if (Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
			System.out.printf("[Game] INFO - Environment ready!\n");
	}

	// Called before the end of MAS execution
	@Override
	public void stop() {
		super.stop();

		addPercept(Literal.parseLiteral("gameover"));
	}

	public void win() {
		super.stop();
		addPercept(Literal.parseLiteral("win"));
	}

	// Called before the MAS execution with the args informed in .mas2j
	@Override
	public void init(String[] args) {
		c_cities = CityCard.parseCities(new ArrayList<City>(cities.values()), CustomTypes.CardType.CITY);
		c_infection = CityCard.parseCities(new ArrayList<City>(cities.values()), CustomTypes.CardType.INFECTION);

		d_game = new Deck(c_cities, CustomTypes.DeckType.GAME);
		d_infection = new Deck(c_infection, CustomTypes.DeckType.INFECTION);
		d_game_discards = new Deck(CustomTypes.DeckType.GAME);
		d_infection_discards = new Deck(CustomTypes.DeckType.INFECTION);

		// set neighbors to each city
		Board.resolveAdjacentCities(gs.board, cities);

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

		this.p_order = Player.resolvePlayerOrder(this.players);
		logger.info("Players order: " + p_order.toString());

		/*
		 * Builds research center and puts players in initial city (assumes a valid set
		 * of cities has already been loaded with the board)
		 */
		City starting_city = this.cities.get(this.gs.board.used_cities.get(0));
		putInvestigationCentre(starting_city, true);
		for (Player p : players.values()) {
			p.setCity(starting_city);
		}

		/*
		 * Spread initial infections across cities as per the game rules (drawing from
		 * infection deck)
		 */

		// Infects 3 cities with an infection level of 3 (3 cubes)
		for (CityCard c_3 : d_infection.draw(3).values()) {
			City infected_city = c_3.city;

			// Creates an Infection object and adds it to the city
			Infection infection = new Infection(infected_city.local_disease, infected_city, 3);
			infected_city.infections.add(infection);

			// Adds infection to list
			this.infections.add(infection);

			// Adds card to discard deck
			d_infection_discards.stack(c_3);
		}

		// Infects 3 cities with an infection level of 2 (2 cubes)
		for (CityCard c_2 : d_infection.draw(3).values()) {
			City infected_city = c_2.city;

			// Creates an Infection object and adds it to the city
			Infection infection = new Infection(infected_city.local_disease, infected_city, 2);
			infected_city.infections.add(infection);

			// Adds infection to list
			this.infections.add(infection);

			// Adds card to discard deck
			d_infection_discards.stack(c_2);
		}

		// Infects 3 cities with an infection level of 1 (1 cubes)
		for (CityCard c_1 : d_infection.draw(3).values()) {
			City infected_city = c_1.city;

			// Creates an Infection object and adds it to the city
			Infection infection = new Infection(infected_city.local_disease, infected_city, 1);
			infected_city.infections.add(infection);

			// Adds infection to list
			this.infections.add(infection);

			// Adds card to discard deck
			d_infection_discards.stack(c_1);
		}

		/*
		 * Generates epidemic cards (as many as specified in Options)
		 */
		ArrayList<CityCard> epidemics = new ArrayList<CityCard>();
		for (int i = 0; i < Options.CARD_TOTAL_EPIDEMICS; i++) {
			CityCard epidemic = new CityCard(null, true);
			epidemics.add(epidemic);
		}

		/*
		 * Puts epidemics cards into the game deck (evenly distributed, as per the game
		 * rules)
		 */
		d_game.shove(epidemics);

		gs.cp = players.get(this.p_order.get(0));
		gs.p_actions_left = Options.PLAYER_MAX_ACTIONS;

		// GRA - Refresh graphics
		this.render.refresh(null, null);
		if (Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal()) {
			System.out.printf("[Game] INFO - Environment ready!\n");
		}

		controlFeedback(this.gm);

		updatePercepts();
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

		// Sets counters
		this.n_roles = this.roles.size();
		this.n_diseases = this.diseases.size();
		this.n_cities = this.cities.size();
		this.n_players = this.players.size();

		// Initializes the rest of aux lists
		this.p_order = new ArrayList<String>();
		this.used_cities = new ArrayList<String>();
		this.infections = new ArrayList<Infection>();
	}

	@Override
	public boolean executeAction(String ag, Structure action) {
		// Resolves action name and player (agName must be equal to the role alias)
		String aname = action.getFunctor();
		Player p = this.roles.get(ag).player;

		logger.info(ag + " doing: " + action);

		if (aname.equals("init")) {
			logger.info("Adding percept.");
			addPercept(Literal.parseLiteral("init"));
			return true;
		}

		boolean consumed_action = false;
		try {
			if (aname.equals("discardCard")) {
				String city = action.getTerm(0).toString();
				String player_alias = action.getTerm(1).toString();
				players.get(player_alias).removeCard(city);
				// This action does never consume action, it happens in draw phase
				// or when sharing info with another player
				consumed_action = false;
				updatePercepts();
			} else {
				if (gs.round == Round.ACT) {
					if (aname.equals("moveAdjacent")) {
						consumed_action = true;
						if (moveAdjacent(gs.cp, Direction.values()[((int) ((NumberTerm) action.getTerm(0)).solve())])) {
							consumed_action = true;
							automaticDoctorDiseasesTreatment();
						} else {
							return false;
						}
					} else if (aname.equals("moveAdjacentCity")) {
						City dest = cities.get(action.getTerm(0).toString());
						if (moveAdjacentCity(gs.cp, dest)) {
							consumed_action = true;
							automaticDoctorDiseasesTreatment();
						}
					} else if (aname.equals("moveAdjacentRandom")) {
						if (moveAdjacentRandom(gs.cp)) {
							consumed_action = true;
							automaticDoctorDiseasesTreatment();
						}
					} else if (aname.equals("directFlight")) {
						City dest = cities.get(action.getTerm(0).toString());
						if (directFlight(gs.cp, dest)) {
							consumed_action = true;
							automaticDoctorDiseasesTreatment();
						}
					} else if (aname.equals("charterFlight")) {
						City dest = cities.get(action.getTerm(0).toString());
						if (charterFlight(gs.cp, dest)) {
							consumed_action = true;
							automaticDoctorDiseasesTreatment();
						}
					} else if (aname.equals("airBridge")) {
						City dest = cities.get(action.getTerm(0).toString());
						if (airBridge(gs.cp, dest)) {
							consumed_action = true;
							automaticDoctorDiseasesTreatment();
						}
					} else if (aname.equals("buildCI")) {
						if (putInvestigationCentre(gs.cp.getCity(), false)) {
							consumed_action = true;
						}
					} else if (aname.equals("treatDisease")) {
						String dis_alias = action.getTerm(0).toString();
						Disease dis = diseases.get(dis_alias);
						if (dis != null) {
							City city = gs.cp.getCity();
							/*
							 * if (dis.treatDisease(city.getInfection(dis), gs.cp)) { consumed_action =
							 * true; }
							 */

							if (dis.treatDisease(city, dis, gs.cp)) {
								consumed_action = true;
							} else {
								consumed_action = false;
							}
						} else {
							consumed_action = false;
						}
					} else if (aname.equals("shareInfo")) {
						String player_alias = action.getTerm(0).toString();
						String city_name = action.getTerm(1).toString();
						boolean cp_giver = Boolean.parseBoolean(action.getTerm(2).toString());
						shareInfo(player_alias, city_name, cp_giver);
						consumed_action = true;
					} else if (aname.equals("discoverCure")) {
						String dis_alias = action.getTerm(0).toString();
						if (discoverCure(gs.cp, dis_alias)) {
							consumed_action = true;
						} else {
							return false;
						}
					} else if (aname.equals("isCIreachable")) {
						consumed_action = false;
						isCIreachable(gs.cp.city, Integer.parseInt(action.getTerm(0).toString()));
					}
					// DISTANCE
					// --------------------------------------------------------------------------
					else if (aname.equals("passTurn")) {
						consumed_action = true;
						gs.p_actions_left = 1;
					} else if (aname.equals("findPlayerToAsk")) {
						findPlayerToAsk(action.getTerm(0).toString());
						consumed_action = false;
					} else if (aname.equals("findCIToReach")) {
						findCIToReach(action.getTerm(0).toString());
						consumed_action = false;
					}
					/*
					 * else if (aname.equals("smartDiscard")) {
					 * smartDiscard(action.getTerm(0).toString()); consumed_action = false; }
					 */
					else if (aname.equals("moveToNearObjective")) {
						String nombreCiudad = action.getTerm(0).toString();
						int[] coordenadasCiudad = null;
						for (City city : this.cities.values()) {
							if (city.alias.equals(nombreCiudad)) {
								coordenadasCiudad = city.getCell().getCoordinates();
							}
						}
						moveToNearObjective(coordenadasCiudad);
						consumed_action = true;
					} else if (aname.equals("moveToFarObjective")) {
						String nombreCiudad = action.getTerm(0).toString();
						int[] coordenadasCiudad = null;
						for (City city : this.cities.values()) {
							if (city.alias.equals(nombreCiudad)) {
								coordenadasCiudad = city.getCell().getCoordinates();
							}
						}
						moveToFarObjective(coordenadasCiudad, action.getTerm(1).toString());
						consumed_action = true;
					} else if (aname.equals("findNearestCube")) {
						findNearestCube();
						consumed_action = false;
					} else if(aname.equals("discard")) {
						int toDiscard = this.gs.cp.hand.size() - 7;
						for(City _ci: this.cities.values()) {
							if (this.gs.cp.hand.containsKey(_ci.alias)) {
								this.gs.cp.hand.remove(_ci.alias);
								toDiscard--;
							}
							if (toDiscard == 0) {
								break;
							}
						}
						consumed_action = false;
					}
					// DISTANCE
					// --------------------------------------------------------------------------
					else {
						logger.info("Unrecognized action! " + aname);
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.render.refresh(null, null);

		if (consumed_action && gs.round == Round.ACT) {
			logger.info("Checking left actions");
			if (consumed_action) {
				gs.p_actions_left--;
			}
			// It should never be lower than 0, but in case
			if (gs.p_actions_left <= 0) {
				// Set to 0 because the draw phase begins
				gs.p_actions_left = 0;
				// DISTANCE
				// --------------------------------------------------------------------------

				this.solicitedCards.clear();

				// DISTANCE
				// --------------------------------------------------------------------------
				gs.round = Round.STEAL;
				logger.info("STEAL round");
			}
		}
		if (gs.round == Round.STEAL) {
			if (gs.cp.getHand().size() <= Options.PLAYER_MAX_CARDS) {
				while (gs.round == Round.STEAL) {
					if (gs.drawnCards < Options.PLAYER_DRAW_CARDS) {
						logger.info("Stealing, hand size: " + gs.cp.getHand().size());
						if (!drawPLayerCard(gs.cp)) {
							updatePercepts();
							this.stop();
							return true;
						}
						gs.drawnCards++;
						logger.info("Drawn cards: " + gs.drawnCards);
					}
					this.render.refresh(null, null);
					if (gs.cp.getHand().size() > Options.PLAYER_MAX_CARDS) {
						// One card must be discarded, so the actions are delayed
						// until the agent calls the discard action
						break;
					} else if (gs.drawnCards == Options.PLAYER_DRAW_CARDS) {
						gs.round = Round.INFECT;
						logger.info("Infect round, to drawn: " + gs.infection_levels[gs.current_infection_level]);
						for (int i = 0; i < gs.infection_levels[gs.current_infection_level]; i++) {
							drawInfectCard();
							this.render.refresh(null, null);
						}

						gs.p_actions_left = Options.PLAYER_MAX_ACTIONS;
						gs.cp = nextPlayer(gs.cp);
						// For the next turn
						gs.drawnCards = 0;
						logger.info("ACT round, actions_left: " + gs.p_actions_left);
						logger.info("Current player: " + gs.cp.alias);
						logger.info("timeout mode: " + (gm == _aux.CustomTypes.GameMode.TIMESTAMP));
						this.render.refresh(null, null);

						if (gm == _aux.CustomTypes.GameMode.TIMESTAMP) {
							try {
								Thread.sleep(_aux.Options.GP_TIMEOUT_SLEEP);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} else {
							waitingManualChange = true;
							while (waitingManualChange) {
								try {
									Thread.sleep(200);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			} else {
				logger.info("[Game] WARNING: Agent must discard card but it does not call the action!!\n");
			}
		}
		if (consumed_action || gs.round != Round.ACT) {
			if (gs.round == Round.INFECT) {
				gs.round = Round.ACT;
			}

			logger.info("updating percepts");
			updatePercepts();
			logger.info("percepts updated");

			try {
				Thread.sleep(200);
			} catch (Exception e) {
			}
			informAgsEnvironmentChanged();
		}
		return true;
	}

	/** creates the agents perception */

	void updatePercepts() {
		clearAllPercepts();
		logger.info(gs.cp.alias);

		// All percepts are added to all agents except the remaining actions,
		// that depends on the agent
		for (Player p : players.values()) {
			addPercept(p.alias, Literal.parseLiteral("myHandSize(" + p.hand.size() +")"));
			if (p.getHand().size() > Options.PLAYER_MAX_CARDS) {
				addPercept(p.alias, Literal.parseLiteral("cardMustBeenDiscarded"));
			}
			if (gs.cp.equals(p)) {
				addPercept(p.alias, Literal.parseLiteral("left_actions(" + gs.p_actions_left + ")"));
				addPercept(p.alias, TURN);
			} else {
				addPercept(p.alias, Literal.parseLiteral("left_actions(" + 0 + ")"));
			}
		}

		// Percepts for cards and location of players
		for (Player player : this.players.values()) {
			addPercept(Literal.parseLiteral("atCity(" + player.alias + "," + player.city.alias + ")"));
			addPercept(player.alias, Literal.parseLiteral("myCity(" + player.city.alias + ")"));
			for (String new_card : player.getHand().keySet()) {
				addPercept(Literal.parseLiteral("hasCard(" + player.alias + "," + new_card + ")"));
				addPercept(player.alias, Literal.parseLiteral("myCard(" + new_card + ")"));
			}
		}

		// Percepts for spread level and CI per city
		for (City city : this.cities.values()) {
			String closest = getClosestCI(city).alias;
			addPercept(Literal.parseLiteral("closestCI(" + city.alias + "," + closest + ")"));

			for (City city_aux : this.cities.values()) {
				int distance = manhattanDistanceTorus(city.cell.getCoordinates(), city_aux.cell.getCoordinates(),
						this.gs.board.n_rows, this.gs.board.n_cols)[0];
				addPercept(
						Literal.parseLiteral("distance(" + city.alias + "," + city_aux.alias + "," + distance + ")"));
			}

			if (city.canResearch()) {
				addPercept(Literal.parseLiteral("hasCI(" + city.alias + ")"));
			}
			// Position of cities: at(city,alias,x,y)
			addPercept(Literal.parseLiteral(
					"at(" + city.alias + "," + city.local_disease.alias + "," + city.cell.x + "," + city.cell.y + ")"));

			// Sum of diseases viruses
			int ilevel = 0;
			for (Infection i : city.infections) {
				ilevel += i.spread_level;
			}
			addPercept(Literal.parseLiteral("infectionLVL(" + city.alias + "," + (ilevel) + ")"));
		}

		// Adjacent cities: adjacent(city,adjacent_city)
		// Iterates every key in the adjacent_cities dictionary
		for (String c_alias : this.gs.board.adjacent_cities.keySet()) {
			// Iterates every adjacent city assigned to c_alias city and parses the percept
			for (String adjacent : this.gs.board.adjacent_cities.get(c_alias)) {
				addPercept(Literal.parseLiteral("adjacent(" + c_alias + ", " + adjacent + ")"));
			}
		}

		// All (not only active) infections
		updateGameGeneralInfectionsList();
		for (Infection i : this.infections) {
			addPercept(Literal
					.parseLiteral("infected(" + i.city_host.alias + "," + i.dis.alias + "," + i.spread_level + ")"));
		}

		// Percepts for disease cures
		for (Disease dis : this.diseases.values()) {
			if (dis.cure) {
				addPercept(Literal.parseLiteral("isCured(" + dis.alias + ")"));
			}
			addPercept(Literal.parseLiteral("disease(" + dis.alias + "," + dis.spreads_left + ")"));
		}

		// Game mode
		if (gm == _aux.CustomTypes.GameMode.TIMESTAMP) {
			addPercept(Literal.parseLiteral("control_timeout(" + _aux.Options.GP_TIMEOUT_SLEEP + ")"));
		} else {
			addPercept(Literal.parseLiteral("control_manual)"));
		}

		// Number of cards of each virus
		for (Player p : players.values()) {
			for (Disease dis : diseases.values()) {
				int n_cards = 0;
				for (CityCard c : p.hand.values()) {
					if (c.city.local_disease == dis) {
						n_cards++;
					}
				}
				addPercept(p.alias, Literal.parseLiteral("myCardsNumber(" + dis.alias + "," + n_cards + ")"));
				addPercept(Literal.parseLiteral("cardsNumber(" + p.alias + "," + dis.alias + "," + n_cards + ")"));
			}
		}

		// This function is called when buttons are pressed
		// controlFeedback(this.gm);
	}

	// Buttons behaviour
	public void controlFeedback(_aux.CustomTypes.GameMode gm) {
		removePercept(Literal.parseLiteral("control_manual"));
		removePerceptsByUnif(Literal.parseLiteral("control_timeout(_)"));

		if (gm == _aux.CustomTypes.GameMode.TIMESTAMP) {
			addPercept(Literal.parseLiteral("control_timeout(" + _aux.Options.GP_TIMEOUT_SLEEP + ")"));
			addPercept(Literal.parseLiteral("control_run"));
			waitingManualChange = false;
		}

		// Manual control
		else {
			// control_run is removed from agent when its turn finishes
			addPercept(Literal.parseLiteral("control_manual"));
			addPercept(Literal.parseLiteral("control_run"));
			waitingManualChange = false;
		}
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
				break;
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
		if (!this.d_game.cards.isEmpty()) {
			CityCard new_card = this.d_game.draw();
			if (new_card.city != null) {
				logger.info("Drawn player card: " + new_card.city.alias);
			} else {
				logger.info("Drawn epidemic card");
			}
			this.d_game_discards.stack(new_card);
			if (new_card.isEpidemic()) {
				// propagate
				increaseInfectionLevel();
				// infect and intensify
				drawInfectCard();
			} else {
				player.addCard(new_card);
			}
		} else {
			logger.info("Game deck out of cards... Game Over!");
			return false;
		}
		return true;
	}

	public boolean drawInfectCard() {
		// Si no quedan cartas que robar, devuelve false
		if (!this.d_infection.cards.isEmpty()) {
			// The last card is drawn from the bottom
			CityCard new_card = this.d_infection.bottomDraw();
			logger.info("Drawn infection card: " + new_card.city.alias);
			this.d_infection_discards.stack(new_card);
			infect(new_card.getCity(), new_card.getDisease());
			// intensify: the discarded icards are shuffled and put on top
			// (last items)
			d_infection_discards.shuffle();
			d_infection.atop(d_infection_discards.cards);
			this.d_infection_discards.cards = new ArrayList<CityCard>();
		} else {
			logger.info("Infection deck out of cards... Game Over!");
			return false;
		}

		return true;
	}

	/*
	 * discoverCure Remove to the player hands needed cards and set in the selected
	 * disease the attribute cure to True
	 */
	public boolean discoverCure(Player current_player, String diseaseAlias) {
		Disease disease = this.diseases.get(diseaseAlias);
		ArrayList<String> to_discard = new ArrayList<String>();
		boolean satisfied = false;
		for (Map.Entry<String, CityCard> entry : current_player.getHand().entrySet()) {
			City city = entry.getValue().getCity();
			if (city.local_disease.alias.equals(diseaseAlias)) {
				to_discard.add(entry.getKey());
				if (to_discard.size() == 5
						|| (to_discard.size() == 4 && current_player.getRole().alias.equals("genetist"))) {
					satisfied = true;
					break;
				}
			}
		}
		if (satisfied) {
			for (String city_alias : to_discard) {
				current_player.removeCard(city_alias);
			}
			disease.setCure(true);
			// If all cured win
			for (Disease dis : diseases.values()) {
				if (dis.getCure()) {
					this.win();
				}
			}
			automaticDoctorDiseasesTreatment();

			return true;
		} else {
			return false;
		}
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
	 * Moves to the destination city (only if it is an adjacent city).
	 * 
	 * @param destination
	 * @return true if moved and false otherwise.
	 */
	public boolean moveAdjacentCity(Player current_player, City destination) {
		boolean moved = false;
		for (String adjacent : this.gs.board.adjacent_cities.get(current_player.city.alias)) {
			if (adjacent == destination.alias) {
				current_player.getCity().removePlayer(current_player);
				current_player.setCity(destination);
				current_player.getCity().putPlayer(current_player);
				return true;
			}
		}

		return moved;
	}

	/**
	 * Moves to a random adjacent city.
	 * 
	 * @param destination
	 * @return true if moved and false otherwise.
	 */
	public boolean moveAdjacentRandom(Player current_player) {
		int chosen = ThreadLocalRandom.current().nextInt(0, 4);
		String adjacent = this.gs.board.adjacent_cities.get(current_player.city.alias).get(chosen);
		City destination = this.cities.get(adjacent);
		current_player.getCity().removePlayer(current_player);
		current_player.setCity(destination);
		current_player.getCity().putPlayer(current_player);
		return true;
	}

	/**
	 * Flies to the destination city discarding one card of the hand of that city.
	 * 
	 * @param destination
	 * @return true if moved and false otherwise.
	 */
	public boolean directFlight(Player current_player, City destination) {
		boolean moved = false;
		// The operation expert can fly to everywhere from a CI
		// (relaxed, instead of discarding a card of any city, no discard is needed)
		if (current_player.getRole().alias.equals("op_expert")) {
			if (current_player.city.canResearch()) {
				current_player.getCity().removePlayer(current_player);
				current_player.setCity(destination);
				current_player.getCity().putPlayer(current_player);
				return true;
			}
		}
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
	 * Puts a investigation centre in the city. In the real game there are 6
	 * investigation centers. Infinite possible research centres seems a reasonable
	 * relaxation to the problem.
	 */
	public boolean putInvestigationCentre(City city, boolean init) {
		if (gs.current_research_centers < Options.MAX_RESEARCH_CENTERS - 1) {
			if (init || gs.cp.alias.equals("op_expert")) {
				city.can_research = true;
				gs.current_research_centers++;
				return true;
			} else {
				// The card of the city must been discarded
				for (Card card : gs.cp.getHand().values()) {
					if (city.equals(card.city)) {
						city.can_research = true;
						gs.current_research_centers++;
						gs.cp.getHand().remove(card.getCity().alias);
						return true;
					}
				}
				return false;
			}
		} else {
			return false;
		}
	}

	public void isCIreachable(City current_city, int left_moves) {
		boolean isReachable = isCIreachableAUX(current_city, left_moves);
		if (isReachable) {
			addPercept(gs.cp.alias, Literal.parseLiteral("aaciIsRecheableE(" + current_city.alias + ")"));
		} else {
			addPercept(gs.cp.alias, Literal.parseLiteral("aaciIsNotRecheableE(" + current_city.alias + ")"));
			addPercept(gs.cp.alias, Literal.parseLiteral("needCI(" + current_city.alias + ")"));
		}
	}

	public boolean isCIreachableAUX(City current_city, int left_moves) {
		if (current_city.canResearch()) {
			return true;
		} else if (left_moves == 0) {
			return false;
		} else {
			boolean isReachable = false;
			Collection<City> neighbors = current_city.getNeighbors().values();
			for (City c : neighbors) {
				isReachable = isReachable || isCIreachableAUX(c, left_moves - 1);
			}
			return isReachable;
		}
	}

	// DISTANCE
	// --------------------------------------------------------------------------

	public void findPlayerToAsk(String desiredDisease) {
		Player desiredPlayer = null;
		CityCard desiredCard = null;
		int minimumDistanceToCard = Integer.MAX_VALUE;
		for (Player player : this.players.values()) {

			for (String cardAlias : player.getHand().keySet()) {

				boolean ignorada = false;
				for (City cardYaPreguntada : this.solicitedCards) {
					if (cardYaPreguntada.alias.equals(cardAlias)) {
						ignorada = true;
					}
				}
				if (!ignorada & !player.alias.equals(gs.cp.alias)) {
					Disease diseaseCard = player.getHand().get(cardAlias).getDisease();
					String diseaseAlias = diseaseCard.alias;
					if (diseaseAlias.equals(desiredDisease)) {
						City cityCard = player.getHand().get(cardAlias).getCity();
						Cell cityCell = cityCard.getCell();
						int[] cardPosition = cityCell.getCoordinates();
						String[] calculusDistanceToCard = routeChoice(cardPosition, desiredDisease);
						int distanceToCard = Integer.parseInt(calculusDistanceToCard[0]);
						Player possibleDesiredPlayer = player;
						if (distanceToCard < minimumDistanceToCard) {
							desiredPlayer = possibleDesiredPlayer;
							desiredCard = player.getHand().get(cardAlias);
							minimumDistanceToCard = distanceToCard;
						}
					}
				}
			}
		}
		if (desiredPlayer != null) {
			this.solicitedCards.add(desiredCard.city);
			addPercept(gs.cp.alias,
					Literal.parseLiteral("soliciteCardE(" + desiredPlayer.alias + "," + desiredCard.city.alias + ")"));
			// addPercept(gs.cp.alias, Literal.parseLiteral("soliciteCardE(NADA)"));
		} else {
			addPercept(gs.cp.alias, Literal.parseLiteral("nobodySharesE"));
		}

	}

	public String[] routeChoice(int[] finalPosition, String desiredDisease) {
		int[] initialPosition = gs.cp.getCity().getCell().getCoordinates();
		// Calculo el coste de caminar entre los puntos
		int[] walkDistance = manhattanDistanceTorus(initialPosition, finalPosition, gs.board.n_rows, gs.board.n_cols);
		int minimumCostRoute = walkDistance[0];
		String nextAction = "" + walkDistance[1];
		boolean usoCarta = false;

		City ciudadDestino = null;
		for (City ciudad : cities.values()) {
			int coordAux[] = ciudad.getCell().getCoordinates();
			if (coordAux[0] == finalPosition[0] & coordAux[1] == finalPosition[1]) {
				ciudadDestino = ciudad;
			}
		}

		// Calculo el coste de utilizar un vuelo directo(Puedo volar hasta esa ciudad)
		for (String card : gs.cp.getHand().keySet()) {
			Disease diseaseCard = gs.cp.getHand().get(card).getDisease();
			if (!desiredDisease.equals(diseaseCard.alias)) {
				City city = gs.cp.getHand().get(card).getCity();
				Cell cell = city.getCell();
				int[] position = cell.getCoordinates();
				int[] totalDistanceDirectFlight = manhattanDistanceTorus(position, finalPosition, gs.board.n_rows,
						gs.board.n_cols);
				int aux = totalDistanceDirectFlight[0] + 1;
				if (aux < minimumCostRoute) {
					minimumCostRoute = aux;
					nextAction = 4 + "----" + city.alias;
					usoCarta = true;
				}
			}
		}

		// Calculo el coste de utilizar un vuelo charter(Puedo volar a cualquier parte
		// desde esa ciudad)
		for (String card : gs.cp.getHand().keySet()) {
			Disease diseaseCard = gs.cp.getHand().get(card).getDisease();
			if (!desiredDisease.equals(diseaseCard.alias)) {
				City city = gs.cp.getHand().get(card).getCity();
				Cell cell = city.getCell();
				int[] position = cell.getCoordinates();
				int[] totalDistanceDirectFlight = manhattanDistanceTorus(initialPosition, position, gs.board.n_rows,
						gs.board.n_cols);
				int aux = totalDistanceDirectFlight[0] + 1;
				if (aux < minimumCostRoute) {
					minimumCostRoute = aux;
					if (aux == 1) {
						for (City ciudad : cities.values()) {
							Cell cellCiudad = ciudad.getCell();
							int[] posicionesCoordenadas = cellCiudad.getCoordinates();
							if (posicionesCoordenadas[0] == position[0] && posicionesCoordenadas[1] == position[1]) {
								nextAction = 5 + "----" + ciudadDestino.alias;
							}
						}

					} else {
						nextAction = "" + totalDistanceDirectFlight[1];
					}
					usoCarta = true;
				}
			}
		}

		// Calculo el coste de utilizar un air bridge(Puedo volar entre centros de
		// investigacion)
		ArrayList<City> cityList = new ArrayList<City>();
		for (City city : cities.values()) {
			if (city.canResearch()) {
				cityList.add(city);
			}
		}

		for (City cityF : cityList) {

			boolean usoCartaCIF = false;
			Cell cellF = cityF.getCell();
			int[] positionF = cellF.getCoordinates();
			int[] totalDistanceToResearchCentreF = manhattanDistanceTorus(positionF, finalPosition, gs.board.n_rows,
					gs.board.n_cols);
			int distanceToResearchCentreF = totalDistanceToResearchCentreF[0];
			for (String card : gs.cp.getHand().keySet()) {
				Disease diseaseCard = gs.cp.getHand().get(card).getDisease();
				if (!diseaseCard.alias.equals(desiredDisease)) {
					City city = gs.cp.getHand().get(card).getCity();
					Cell cell = city.getCell();
					int[] position = cell.getCoordinates();
					int[] totalDistanceDirectFlight = manhattanDistanceTorus(positionF, position, gs.board.n_rows,
							gs.board.n_cols);
					int aux = totalDistanceDirectFlight[0] + 1;
					if (aux < distanceToResearchCentreF) {
						distanceToResearchCentreF = aux;
						usoCartaCIF = true;
					}
				}
			}

			for (City cityI : cityList) {
				boolean usoCartaCII = false;

				if (!cityI.alias.equals(cityF.alias)) {

					Cell cellI = cityI.getCell();
					int[] positionI = cellI.getCoordinates();
					int[] totalDistanceToResearchCentreI = manhattanDistanceTorus(initialPosition, positionI,
							gs.board.n_rows, gs.board.n_cols);
					String auxNextActionToResearchCentreI = "" + totalDistanceToResearchCentreI[1];
					int distanceToResearchCentreI = totalDistanceToResearchCentreI[0];
					if (distanceToResearchCentreI == 0) {
						auxNextActionToResearchCentreI = 6 + "----" + cityF.alias;
						// auxNextActionToResearchCentreI = ""+6;
						// break;
					}

					for (String card : gs.cp.getHand().keySet()) {
						Disease diseaseCard = gs.cp.getHand().get(card).getDisease();
						if (!diseaseCard.alias.equals(desiredDisease)) {
							City city = gs.cp.getHand().get(card).getCity();
							Cell cell = city.getCell();
							int[] position = cell.getCoordinates();
							int[] totalDistanceDirectFlight = manhattanDistanceTorus(positionI, position,
									gs.board.n_rows, gs.board.n_cols);
							int aux = totalDistanceDirectFlight[0] + 1;
							if (aux < distanceToResearchCentreI) {
								distanceToResearchCentreI = aux;
								auxNextActionToResearchCentreI = 4 + "----" + city.alias;
								// totalDistanceToResearchCentreI[1] = 4;
								usoCartaCII = true;
							}
						}
					}
					int costeTotalVuelosEntreCI = distanceToResearchCentreI + 1 + distanceToResearchCentreF;
					if (costeTotalVuelosEntreCI < minimumCostRoute) {
						minimumCostRoute = costeTotalVuelosEntreCI;
						nextAction = "" + auxNextActionToResearchCentreI;
					} else if (costeTotalVuelosEntreCI == minimumCostRoute && usoCarta && !usoCartaCII
							&& !usoCartaCIF) {
						minimumCostRoute = costeTotalVuelosEntreCI;
						nextAction = "" + auxNextActionToResearchCentreI;
						usoCarta = false;
					}
				}
			}
		}
		return new String[] { "" + minimumCostRoute, nextAction };
	}

	public void moveToFarObjective(int[] finalPosition, String desiredDisease) {
		String[] route = routeChoice(finalPosition, desiredDisease);
		String nextAction = route[1];
		String[] partsAction = nextAction.split("----");
		System.out.println("TIENE QUE: " + route[0] + " " + route[1]);
		System.out.println("PARTS ACTION: " + nextAction + "  " + partsAction[0]);
		switch (partsAction[0]) {
		case "0":
			moveAdjacent(gs.cp, CustomTypes.Direction.UP);
			break;
		case "1":
			moveAdjacent(gs.cp, CustomTypes.Direction.RIGHT);
			break;
		case "2":
			moveAdjacent(gs.cp, CustomTypes.Direction.DOWN);
			break;
		case "3":
			moveAdjacent(gs.cp, CustomTypes.Direction.LEFT);
			break;
		case "4":
			for (City city : this.cities.values()) {
				if (city.alias.equals(partsAction[1])) {
					directFlight(gs.cp, city);
					break;
				}
			}
			break;
		case "5":
			for (City city : this.cities.values()) {
				if (city.alias.equals(partsAction[1])) {
					charterFlight(gs.cp, city);
					break;
				}
			}
			break;
		case "6":
			for (City city : this.cities.values()) {
				if (city.alias.equals(partsAction[1])) {
					airBridge(gs.cp, city);
					break;
				}
			}
			break;
		}
	}

	public int[] manhattanDistanceTorus(int[] initialPosition, int[] finalPosition, int nRows, int nCols) {
		int movH, movV;
		// Calculo numero de casillas movimiento horizontal
		int hor = initialPosition[1] - finalPosition[1];
		if (hor < 0) {
			movH = 2;
			hor = hor * (-1);
		} else
			movH = 0;
		int finalHor = Math.min(hor, nRows - hor);
		// Calculo numero de casillas movimiento vertical
		int ver = initialPosition[0] - finalPosition[0];
		if (ver < 0) {
			movV = 1;
			ver = ver * (-1);
		} else
			movV = 3;
		int finalVer = Math.min(ver, nCols - ver);
		// Calculo direccion inicial del movimiento
		int mov = movH;
		if (finalHor == 0) {
			if (ver != finalVer) {
				movV = (movV + 2) % 4;
			}
			mov = movV;
		} else if (hor != finalHor) {
			mov = (movH + 2) % 4;
		}
		return new int[] { finalHor + finalVer, mov };
	}

	public City getClosestCI(City start) {
		City closestCI = null;
		int minDistance = 10000;
		for (City c : this.cities.values()) {
			if (c.can_research) {
				int distance = manhattanDistanceTorus(start.cell.getCoordinates(), c.cell.getCoordinates(),
						this.gs.board.n_rows, this.gs.board.n_cols)[0];
				if (distance < minDistance) {
					closestCI = c;
					minDistance = distance;
				}
			}
		}
		return closestCI;
	}

	public void findNearestCube() {
		int nearestCubeDistance = Integer.MAX_VALUE;
		City cityNearestCube = null;
		for (City city : cities.values()) {
			int sumValores = 0;
			for (Infection inf : city.getInfections()) {
				sumValores += inf.spread_level;
			}
			if (sumValores > 0) {
				Cell cell = city.getCell();
				int[] finalPosition = cell.getCoordinates();
				String[] aux = shortRouteChoice(finalPosition);
				if (Integer.parseInt(aux[0]) < nearestCubeDistance) {
					nearestCubeDistance = Integer.parseInt(aux[0]);
					cityNearestCube = city;
				}
			}
		}
		if (cityNearestCube != null) {
			addPercept(gs.cp.alias, Literal.parseLiteral("cityNearestCubeE(" + cityNearestCube.alias + ")"));
		} else {
			addPercept(gs.cp.alias, Literal.parseLiteral("notNearestCubeE"));
		}
	}

	public String[] shortRouteChoice(int[] finalPosition) {
		int[] initialPosition = gs.cp.getCity().getCell().getCoordinates();
		// Calculo el coste de caminar entre los puntos
		int[] walkDistance = manhattanDistanceTorus(initialPosition, finalPosition, gs.board.n_rows, gs.board.n_cols);
		int minimumCostRoute = walkDistance[0];
		String nextAction = "" + walkDistance[1];
		return new String[] { "" + minimumCostRoute, nextAction };
	}

	public void moveToNearObjective(int[] finalPosition) {
		String[] nextAction = shortRouteChoice(finalPosition);
		switch (nextAction[1]) {
		case "0":
			moveAdjacent(gs.cp, CustomTypes.Direction.UP);
			break;
		case "1":
			moveAdjacent(gs.cp, CustomTypes.Direction.RIGHT);
			break;
		case "2":
			moveAdjacent(gs.cp, CustomTypes.Direction.DOWN);
			break;
		case "3":
			moveAdjacent(gs.cp, CustomTypes.Direction.LEFT);
			break;
		}
	}

	public void findCIToReach(String desiredDisease) {
		int costNearestCI = Integer.MAX_VALUE;
		City nearestCI = null;
		for (City city : cities.values()) {
			if (city.canResearch()) {
				Cell cell = city.getCell();
				int[] positionCI = cell.getCoordinates();
				String[] aux = routeChoice(positionCI, desiredDisease);
				if (costNearestCI > Integer.parseInt(aux[0])) {
					costNearestCI = Integer.parseInt(aux[0]);
					nearestCI = city;
				}
			}
		}
		addPercept(gs.cp.alias, Literal.parseLiteral("irACIE(" + nearestCI.alias + ")"));
	}

	// DISTANCE
	// --------------------------------------------------------------------------

	void automaticDoctorDiseasesTreatment() {
		if (gs.cp.getRole().alias.equals("doctor")) {
			for (Infection e : gs.cp.getCity().getInfections()) {
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

		Infection infection = city.getInfection(dis);

		// if the disease is cured nothing happens
		if (!dis.getCure()) {
			// If max spread level exceeded, then an outbreak occurs.
			if (infection.spread_level + 1 > Options.MAX_SPREADS_PER_CITY) {

				// Updates disease total spreads
				dis.spread(Options.MAX_SPREADS_PER_CITY - infection.spread_level);
				infection.spread_level = Options.MAX_SPREADS_PER_CITY;

				logger.info("outbreak");
				this.n_outbreaks++;
				if (n_outbreaks >= 8) {
					updatePercepts();
					this.stop();
				}

				// Expands to adjacent cities
				for (City neigh : city.getNeighbors().values()) {
					infect(neigh, dis);
				}
			} else {
				infection.spread_level = infection.spread_level + 1;
				logger.info(city.alias + " infected of " + dis.alias + ", level: " + infection.spread_level);
			}
		}

		updateGameGeneralInfectionsList();
	}

	public void updateGameGeneralInfectionsList() {
		this.infections = new ArrayList<Infection>();
		for (City city : cities.values()) {
			for (Infection inf : city.infections) {
				if (/* inf.spread_level > 0 && */!this.infections.contains(inf)) {
					infections.add(inf);
				}
			}
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
			if (gs.current_infection_level == gs.infection_levels.length - 1) {
				updatePercepts();
				this.stop();
			}
		}

		return increased;
	}

}
