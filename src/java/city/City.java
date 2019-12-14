package city;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;

import dis.*;
import player.Player;
import board.Cell;
import _aux.Options;
import _aux.CustomTypes;
import game.GameStatus;

/*
  City class
    Represents a city object. Contains relevant information about its status
*/
public class City{

  // Static info. Once initialized, it should remain untouched
  public String alias;
  public String name;
  public int population;
  public Disease local_disease;
  
  public Cell cell;
  private ArrayList<City> neighbors;  // Adjacent cities. Should be initialized when instatiating the Board object

  // Dynamic data. Can be changed through gameplay
  public Hashtable<String, Player> players = new Hashtable<String, Player>();  // Players in the city
  public Hashtable<String, Infection> infections = new Hashtable<String, Infection>();  // Active diseases
  public boolean can_research = false;

  // Constructor
  public City(String name, String alias, Disease ldis, int pop){
    this.name = name;
    this.alias = alias;
    this.local_disease = ldis;
    this.population = pop;
  }

  // Setters/Getters
  public void setCell(Cell c){
    this.cell = c;
  }

  /*
    parseCities
      Initializes cities from a datafile.
      Expected format (csv):
        <City full name>;<alias>;<local disease alias>;

      Alias must be unique. It'll be used as identifier.

      **PARAMETERS:
        datafile: String; path to file.
        diseases: Disease list; list containing existing diseases. Will be used to maintain coherence between game objects

      **RETURNS a map of cities (key:alias, value:City)
  */
  public static Hashtable<String, City> parseCities(String datafile, Hashtable<String, Disease> diseases){
    Hashtable<String, City> cities = new Hashtable<String, City>();

    try{
      BufferedReader br = new BufferedReader(new FileReader(datafile));
      String line;
      String[] city_data;
      String city_name;
      String city_alias;
      String city_ldis_alias;
      //Disease city_ldis;
      
      // NEW
      int city_pop;
      
      //boolean disease_exists = false;

      // Reads city data from subsequent lines (Must follow the expected disease format)
      while((line = br.readLine()) != null){
        city_data = line.split(";");

        // Gets data from splitted line elements
        city_name = city_data[0];
        city_alias = city_data[1];
        city_ldis_alias = city_data[2];
        
        // NEW
        city_pop = Integer.parseInt(city_data[3]);

        // Tries to create City object
        if(cities.containsKey(city_alias)){
          if(Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
            System.out.printf("[City] WARN: City \"%s\" duplicated. Ignoring...\n", city_alias);

          continue;
        }
        else{

          // Checks that the specified local disease exists
          if(!diseases.containsKey(city_ldis_alias)){
            if(Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
              System.out.printf("[City] WARN: Specified unkonw disease \"%s\" for city \"%s\". Ignoring...\n", city_ldis_alias, city_alias);
            continue;
          }

          // Creates City object and adds it to the dictionary
          else{
            City city = new City(city_name, city_alias, diseases.get(city_ldis_alias), city_pop);
            cities.put(city_alias, city);

            if(Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
              System.out.printf("[City] INFO: New city generated\n");

            if(Options.LOG.ordinal() >= CustomTypes.LogLevel.DUMP.ordinal())
              city.dump();
          }
        }
      }
      
      br.close();
    }
    catch(Exception e){
      if(Options.LOG.ordinal() >= CustomTypes.LogLevel.CRITICAL.ordinal())
        System.out.printf("CRITICAL: Exception while parsing\n");

      System.err.println(e.getMessage());
      System.exit(0);
    }

    // Binds cities to game
    return cities;
  }
  
  // Infects a city
  public Infection infect(Infection i) {
	  String infecting_disease = i.dis.alias;
	  
	  // If city already is infected by the same infection, only updates the spread level, otherwise, adds the infection object as a whole
	  if(this.infections.contains(infecting_disease)) {
		  int infection_current_spread = this.infections.get(infecting_disease).spread_level;
		  this.infections.get(infecting_disease).spread_level += i.spread_level;
		  i = this.infections.get(infecting_disease);
		  
		  // Trick. Removes current active infection to update it and add it later checking for outbreaks
		  this.infections.remove(infecting_disease);
		  i.dis.spreads_left += infection_current_spread;
	  }
	  
	  int infecting_totalSpread = i.spread_level;
	  
	  // Spread disease to neighbor cities if spread level exceeds limit
	  if(infecting_totalSpread > _aux.Options.MAX_SPREADS_PER_CITY) {
		  i.spread_level = _aux.Options.MAX_SPREADS_PER_CITY;
		  i.outbreak();
	  }
	  
	  // Adds infection to city and updates disease spreads left
	  this.infections.put(infecting_disease, i);
	  i.dis.spreads_left -= i.spread_level;
	  
	  return i;
  }
  
  // Heals a disease of the city (reduces infection level by some amount)
  public void heal(Disease d, int amount) {
	  Infection i = this.infections.get(d.alias);
	  
	  // Healed amount cannot exceed infection spread in the city
	  if(amount > i.spread_level) {
		  amount = i.spread_level;
	  }
	  i.spread_level -= amount;
	  d.spreads_left += amount;
	  
	  
	  // If infection is totally healed, then, remove it from the city
	  if(i.spread_level == 0) {
		  this.infections.remove(d.alias);
	  }
	  
  }
  
  // Builds a research center
  public void buildResearch(GameStatus gs) {
	  if(gs.current_research_centers < Options.CITY_MAX_RESEARCH) {
		  this.can_research = true;
		  gs.current_research_centers++;
	  }
  }
  
  
  // Destroys a research center
  public void destroyReseach(GameStatus gs) {
	  this.can_research = false;
	  gs.current_research_centers--;
  }

  //Setters/getters

  // Returns local disease alias
  public String getLocalDisease(){
    return this.local_disease.alias;
  }

  // Retrieves list of active epidemics
  /*
  public ArrayList<Infection> getInfections(){
    return this.infections;
  }
  */

  // Sets adjacent cities
  public void setNeighbors(ArrayList<City> neighs){
    this.neighbors = neighs;
  }

  // Puts a player in the city
  public void putPlayer(Player p){
    this.players.put(p.alias, p);
  }

  // Dummy method to print city data
  public void dump(){
    System.out.printf(">>Printing city data (%s)\n", this.name);
    System.out.printf(".Alias: %s\n", this.alias);
    System.out.printf(".Local disease: %s\n", this.local_disease.name);

    if(this.neighbors != null){
      System.out.printf(".Adjacent cities:\n");

      for(City adj_city : this.neighbors){
        System.out.printf("..%d\n", adj_city.name);
      }
    }

    System.out.println();
  }
}
