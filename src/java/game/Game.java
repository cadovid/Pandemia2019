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

	private Logger logger = Logger.getLogger("pandemic.mas2j." + Game.class.getName());
	
	public static final Term MOVE_ADJACENT = Literal.parseLiteral("moveAdjacent(direction)");
    public static final Term DIRECT_FLIGHT = Literal.parseLiteral("directFlight(dest)");
    public static final Term CHARTER_FLIGHT = Literal.parseLiteral("charterFlight(dest)");
    public static final Term AIR_BRIDGE = Literal.parseLiteral("airBridge(dest)");
    public static final Term BUILD_CI = Literal.parseLiteral("buildCI()");
    public static final Term TREAT_DISEASE = Literal.parseLiteral("treatDisease(disease)");
    public static final Term SHARE_INFO = Literal.parseLiteral("shareInfo(player)");
    public static final Term DISCOVER_CURE = Literal.parseLiteral("discoverCure(disease)");

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

	// TODO - Game cards
	/*
	 * public ArrayList<Card> cards_player; // drawable cards (only those that are
	 * available) public ArrayList<Card> discarded_pcards; // discarded cards public
	 * ArrayList<Card> cards_infection; public ArrayList<Card> discarded_icards;
	 */

	// Constructor. Loads data and initializes board.
	public Game() {
		if (Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
			System.out.printf("[Game] INFO - Initializing game environment...\n");

		// Parses game data from init files
		this.parseData();

		if (Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
			System.out.printf("[Game] INFO - Environment ready!\n");
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
        logger.info(ag+" doing: "+ action);
        boolean consumed_action = false;
        try {
            if (action.equals(MOVE_ADJACENT)) {
            	// TODO: moveAdjacent
                // if (moveAdjacent((Direction)((NumberTerm)action.getTerm(0)).solve())) {
            	// 		consumed_action = true;
            	// } else {
            	// 		return false;
            	// }
            } else if (action.equals(DIRECT_FLIGHT)) {
                City dest = cities.get(((StringTerm) action.getTerm(0)).toString());
                if (gs.cp.directFlight(dest)) {
                	consumed_action = true;
                }
            } else if (action.equals(CHARTER_FLIGHT)) {
                City dest = cities.get(((StringTerm) action.getTerm(0)).toString());
                if (gs.cp.charterFlight(dest)) {
                	consumed_action = true;
                }                
            } else if (action.equals(AIR_BRIDGE)) {
                City dest = cities.get(((StringTerm) action.getTerm(0)).toString());
                if (gs.cp.airBridge(dest)) {
                	consumed_action = true;
                }
            } else if (action.equals(BUILD_CI)) {
                gs.cp.getCity().putInvestigationCentre();
                consumed_action = true;
            } else if (action.equals(TREAT_DISEASE)) {
            	String dis_alias = ((StringTerm) action.getTerm(0)).toString();
                // TODO: treatDisease() with the Epidemic object or the final decision to handle this
                consumed_action = true;
            } else if (action.equals(SHARE_INFO)) {
            	String player_alias = ((StringTerm) action.getTerm(0)).toString();
                // TODO: shareInfo(gs.cp, player_alias);
                consumed_action = true;
            } else if (action.equals(DISCOVER_CURE)) {
            	String dis_alias = ((StringTerm) action.getTerm(0)).toString();
                // TODO: discoverCure checks
                if (discoverCure(dis_alias)) {
                	consumed_action = true;
                } else {
                	return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (consumed_action) {
        	gs.p_actions_left--;
        }
        // It should never be lower than 0, but in case
        if (gs.p_actions_left <= 0) {
        	gs.p_actions_left = Options.PLAYER_MAX_ACTIONS;
        	gs.cp = nextPlayer(gs.cp);
        }

        updatePercepts();

        try {
            Thread.sleep(200);
        } catch (Exception e) {}
        informAgsEnvironmentChanged();
        return true;
    }
	
	/** creates the agents perception*/
    void updatePercepts() {
        clearPercepts();
        
        // All percepts are added to all agents except the remaining actions,
        // that depends on the agent
        for (Player p : players.values()) {
        	if (gs.cp.equals(p)) {
        		addPercept(p.alias, Literal.parseLiteral("left_actions(" + gs.p_actions_left + ")"));
        	} else {
        		addPercept(p.alias, Literal.parseLiteral("left_actions(" + 0 + ")"));
        	}
        }
        
        // TODO: percepts for all agents
        addPercept(Literal.parseLiteral(""));
    }
    
    /**
     * Returns the next player in function of the current player.
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

	// TODO
	/*
	 * ¿Any security check? As number of cards, disease color,.... Change current
	 * player left actions. The boolean return value is to check if the call is valid
	 * or there are missing conditions.
	 */

	/*
	 * discoverCure Remove to the player hands needed cards and set in the selected
	 * disease the attribute cure to True
	 */
	public boolean discoverCure(String diseaseAlias) {
		Disease disease = this.diseases.get(diseaseAlias);
		for (int i = 0; i < city_cards.size(); i++) {
			gs.cp.removeCard(city_cards.get(i));
			// discarded_pcards.add(c);
		}
		disease.setCure(true);
		return true;
	}

	// Called before the end of MAS execution
	@Override
	public void stop() {
		super.stop();
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
			for (City neigh : city.getNeighbors()) {
				infect(neigh, dis);
			}
		} else {

			epidemic.spread_level = epidemic.spread_level + 1;
		}
	}

	// TODO
	/*
	 * When cards_infection will be implemented, uncomment the function
	 */

	/*
	 * DrawInfectionCards Infect the city indicated by the infection card Return
	 * true if are enough cards, false otherwise
	 */
	/*
	 * public boolean drawInfectionCards(Player player, int cardsToDraw) { if
	 * (cards_infection.size() >= cardsToDraw) { for(int i = 0; i < cardsToDraw;
	 * i++) { InfectionCard card = cards_infection.remove(0); infect(card.getCity(),
	 * card.getDisease()); } return true; } else{ return false; } }
	 */

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
