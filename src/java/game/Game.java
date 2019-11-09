package game;

import java.util.*;

// Local imports
import player.*;
import dis.*;
import city.*;
import board.*;
import graphics.Renderer;
import _aux.Datapaths;
import _aux.Options;
import _aux.CustomTypes;

/*
  Game class
    Contains all relevant game data and structures
    Initializes game objects (board and players), and executes game
*/
public class Game{
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
  public ArrayList<String> p_order;  // Player order

  // GRA
  private Renderer render;

  // TODO - Game cards
  /*
  public ArrayList<Card> cards_player;  // drawable cards (only those that are available)
  public ArrayList<Card> discarded_pcards;  // discarded cards
  public ArrayList<Card> cards_infection;
  public ArrayList<Card> discarded_icards;
  */

  // Constructor. Loads data and initializes board.
  public Game(){
    if(Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
      System.out.printf("[Game] INFO - Initializing game environment...\n");

    // Parses game data from init files
    this.parseData();

    if(Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
      System.out.printf("[Game] INFO - Environment ready!\n");
  }

  /*
    parseData
      Reads data from files and generates basic structures
  */
  public void parseData(){
    this.roles = Role.parseRoles(Datapaths.role_list);
    this.diseases = Disease.parseDis(Datapaths.disease_list);
    this.cities = City.parseCities(Datapaths.city_list, this.diseases);
    this.players = Player.parsePlayers(Datapaths.player_list, this.roles, this.cities);

    this.n_roles = this.roles.size();
    this.n_diseases = this.diseases.size();
    this.n_cities = this.cities.size();
    this.n_players = this.players.size();
  }

  public static void main(String args[]){

    // Initializes game
    Game g = new Game();
    Board board = new Board(Datapaths.map, g.cities);
    GameStatus gs = new GameStatus(board);

    // TODO
    /*
      Settle initial game configuration. Needs to implement:
        * Initial diseases (draw infection cards as per the real game)
        * Player hands (draw deck cards)
        * Player order (hand-dependant, as per the real game)
    */

    // Dummy. Method to resolve player order. Should be redefined.
    g.p_order = Player.resolvePlayerOrder(g.players);

    // GRA - Initializes renderer
    g.render = new Renderer(g, null, board);

    // Game cycle. Runs unitl the game is over
    // TODO: Jason does not work with a infinite loop. The last action in each
    // must be a turn end function that must do the turn change calls.
    // This class must extends Environment and define the possible actions that
    // agents can do.
    while(!gs.over){

      // Refresh graphics for updated cells. MUST KEEP RECORD OF THE UPDATED CITIES ON EACH TURN!
      // GRA - List used to resolve which cities were updated on the current turn (renderer will refresh only those cities)
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
      for(String p_alias : g.p_order){

        // Gets player according to the player order
        Player p = g.players.get(p_alias);

        // Action round
        while(gs.p_actions_left > 0){

          // TODO - Player resolves action
          gs.p_actions_left--;
        }

        // TODO
        // Draw cards
        /*
          Define in Game a list of drawable cards from which the player should pick on each round
          p.draw(cards);
        */

        // TODO
        // Resolve infection
        /*
          Define in Game a list of drawable infection cards from which the player should pick on each round (the cards are activated at game level)
          Number of cards to steal is manage through a variable in gs
          Cities gets automatically infected. Outbreaks occurs at ease.
          g.draw(cards);
        */
      }

      // GRA - Once the player turn has ended, the renderer refreshes the updated cells
      g.render.updateGridCells(updated_cities);
    }
  }
}
