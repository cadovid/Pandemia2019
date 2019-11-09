package player;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;

import city.City;
import card.*;
import game.*;
import _aux.Options;
import _aux.CustomTypes;
/*
  Player class
    Represents a player object. Contains relevant information about its status
*/
public class Player{
  public String alias;
  private City city;
  private Role prole;
  private ArrayList<Card> hand = new ArrayList<Card>(); // Player card hand. Hashtable? (might be useful to map the cards by a unique id)

  // Constructors
  public Player(String alias){
    this.alias = alias;
  }
  public Player(String alias, City c, Role r){
    this.alias = alias;
    this.city = c;
    this.prole = r;

    // Cross-reference. Role has a reference to the assigned player, so do the city in which is in
    this.prole.bindPlayer(this);
    this.city.putPlayer(this);
  }

  /*
    parsePlayers
      Initializes players from a datafile.
      Expected format (csv):
        <Alias>;<role>;<city>;

      Alias must be unique. It'll be used as identifier.

      **PARAMETERS:
        datafile: String; path to file.
        roles: Role map; map containing existing roles. Will be used to maintain coherence with the game objects
        cities: City map; map containing existing cities. Will be used to maintain coherence with the game objects

  */
  public static Hashtable<String, Player> parsePlayers(String datafile, Hashtable<String, Role> roles, Hashtable<String, City> cities){
    Hashtable<String, Player> players = new Hashtable<String, Player>();

    try(BufferedReader br = new BufferedReader(new FileReader(datafile))){
      String line;
      String[] player_data;
      String player_alias;
      String player_role_alias;
      String player_city_alias;
      Role player_role;
      City player_city;

      // Reads player data from subsequent lines (Must follow the expected disease format)
      while((line = br.readLine()) != null){
        player_data = line.split(";");

        // Gets data from splitted line elements
        player_alias = player_data[0];
        player_role_alias = player_data[1];
        player_city_alias = player_data[2];

        // Checks game object coherence
        if(players.containsKey(player_alias)){
          if(Options.LOG.ordinal() >= CustomTypes.LogLevel.WARN.ordinal())
            System.out.printf("[Player] WARN: Player alias \"%s\" duplicated. Ignoring...\n", player_alias);

          continue;
        }
        if(!roles.containsKey(player_role_alias)){
          if(Options.LOG.ordinal() >= CustomTypes.LogLevel.WARN.ordinal())
            System.out.printf("[Player] WARN: Invalid role \"%s\" specified for player \"%s\". Ignoring...\n", player_role_alias, player_alias);

          continue;
        }

        if(!cities.containsKey(player_city_alias)){
          if(Options.LOG.ordinal() >= CustomTypes.LogLevel.WARN.ordinal())
            System.out.printf("[Player] WARN: Invalid location \"%s\" specified for player \"%s\". Ignoring...\n", player_city_alias, player_alias);

          continue;
        }

        // Resolves fully quaalified Role and City objects by the alias int he file
        player_role = roles.get(player_role_alias);
        player_city = cities.get(player_city_alias);

        // Creates Player object and adds it to the dictionary
        Player player = new Player(player_alias, player_city, player_role);
        players.put(player_alias, player);

        if(Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
          System.out.printf("[Player] INFO: New player added\n");

        if(Options.LOG.ordinal() >= CustomTypes.LogLevel.DUMP.ordinal())
          player.dump();
      }

    }
    catch(Exception e){
      System.out.printf("CRITICAL: Exception while parsing\n");
      System.err.println(e.getMessage());
      System.exit(0);
    }

    return players;
  }

  // TODO
  /*
    Resolves player order based on initial player hand (as per the original game)
  */
  public static ArrayList<String> resolvePlayerOrder(Hashtable<String, Player> players){
    ArrayList<String> p_order = new ArrayList<String>();

    // Dummy order
    Set<String> p_alias = players.keySet();
    for(String alias : p_alias){
      p_order.add(alias);
    }

    return p_order;
  }

  /**
   * Flies to the destination city discarding one card of the hand of that city.
   * @param destination
   * @return true if moved and false otherwise.
   */
  public boolean directFlight(City destination) {
	  boolean moved = false;
	  for (Card card : this.hand) {
		  if (card.getCity() == destination) {
			  this.city.removePlayer(this);
			  this.city = destination;
			  this.city.putPlayer(this);
			  this.hand.remove(card);
			  return true;
		  }
	  }
	  
	  return moved;
  }
  
  /**
   * Flies to the destination city discarding one card of the hand of the
   * current city.
   * @param destination
   * @return true if moved and false otherwise.
   */
  public boolean charterFlight(City destination) {
	  boolean moved = false;
	  for (Card card : this.hand) {
		  if (card.getCity() == this.city) {
			  this.city.removePlayer(this);
			  this.city = destination;
			  this.city.putPlayer(this);
			  this.hand.remove(card);			  
			  return true;
		  }
	  }
	  
	  return moved;
  }
  
  /**
   * Flies to the destination city if there is a investigation center in the
   * current city and another one in the destination city.
   * @param destination
   * @return true if moved and false otherwise.
   */
  public boolean airBridge(City destination) {
	  boolean moved = false;
	  if (this.city.canResearch() && destination.canResearch()) {
		  this.city.removePlayer(this);
		  this.city = destination;
		  this.city.putPlayer(this);
		  moved = true;
	  }
	  
	  return moved;
  }


  // Setters/Getters
  public void setCity(City c){
    this.city = c;
  }

  public void setRole(Role r){
    this.prole = r;

    // Cross-reference. Role has a reference to the assigned player
    prole.bindPlayer(this);
  }

  public Role getRole(){
    return this.prole;
  }


  // Dummy method to print disease data
  public void dump(){
    System.out.printf(">>Printing player data (%s)\n", this.alias);
    System.out.printf(".Current location: %s\n", this.city.name);
    System.out.printf(".Role: %s\n", this.prole.name);

    System.out.println();
  }
}
