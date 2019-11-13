package city;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;

import dis.Disease;
import dis.Epidemic;
import player.Player;
import board.Cell;
import _aux.Options;
import _aux.CustomTypes;
import _aux.CustomTypes.Color;

/*
  City class
    Represents a city object. Contains relevant information about its status
*/
public class City{

  // Static info. Once initialized, it should remain untouched
  public String alias;
  public String name;
  private Disease local_disease;
  private Color color;
  private Cell cell;
  private ArrayList<City> neighbors;  // Adjacent cities. Should be initialized when instantiating the Board object

  // Dynamic data. Can be changed through gameplay
  private ArrayList<Player> players = new ArrayList<Player>();  // Players in the city
  private ArrayList<Epidemic> epidemics = new ArrayList<Epidemic>();  // Active diseases
  /**
   * true if there is a investigation centre and false otherwise.
   */
  private boolean can_research = false;

  // Constructor
  public City(String name, String alias, Disease ldis, Color color){
    this.name = name;
    this.alias = alias;
    this.local_disease = ldis;
    this.color = color;
  }

  // Setters/Getters
  public void setCell(Cell c){
    this.cell = c;
  }
  public Color getColor() {
	  return this.color;
  }  
  public Disease getDisease() {
	  return this.local_disease;
  }
  
  /**
   * Parse color from String to Color. The default value is Color.BLACK.
   * @param c: The color to be parsed.
   * @return The parsed color.
   */
  public static Color parseColor(String c) {
	  switch (c.toUpperCase()) {
	  	case "BLUE":
	  		return Color.BLUE;
	  	case "YELLOW":
	  		return Color.YELLOW;
	  	case "BLACK":
	  		return Color.BLACK;
	  	case "RED":
	  		return Color.RED;
	  	default:
	  		return Color.BLACK;
	  }
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

    try(BufferedReader br = new BufferedReader(new FileReader(datafile))){
      String line;
      String[] city_data;
      String city_name;
      String city_alias;
      String city_ldis_alias;
      Disease city_ldis;
      Color city_color;
      boolean disease_exists = false;

      // Reads city data from subsequent lines (Must follow the expected disease format)
      while((line = br.readLine()) != null){
        city_data = line.split(";");

        // Gets data from splitted line elements
        city_name = city_data[0];
        city_alias = city_data[1];
        city_ldis_alias = city_data[2];
        city_color = City.parseColor(city_data[3]);

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
            City city = new City(city_name, city_alias, diseases.get(city_ldis_alias), city_color);
            cities.put(city_alias, city);

            if(Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
              System.out.printf("[City] INFO: New city generated\n");

            if(Options.LOG.ordinal() >= CustomTypes.LogLevel.DUMP.ordinal())
              city.dump();
          }
        }
      }
    }
    catch(Exception e){
      if(Options.LOG.ordinal() >= CustomTypes.LogLevel.CRITICAL.ordinal())
        System.out.printf("CRITICAL: Exception while parsing\n");

      System.err.println(e.getMessage());
      System.exit(0);
    }

    return cities;
  }

  //Setters/getters

  // Returns local disease alias
  public String getLocalDisease(){
    return this.local_disease.alias;
  }

  // Retrieves list of active epidemics
  public ArrayList<Epidemic> getEpidemics(){
    return this.epidemics;
  }

  // Retrieves list of players in the city
  public ArrayList<Player> getPlayers(){
    return this.players;
  }

  // Sets adjacent cities
  public void setNeighbors(ArrayList<City> neighs){
    this.neighbors = neighs;
  }

  // Adds an epidemic
  public void infect(Epidemic e){
    this.epidemics.add(e);
  }

  // Puts a player in the city
  public void putPlayer(Player p){
    this.players.add(p);
  }
  
  /**
   * Removes a player from the city.
   * @param p
   */
  public void removePlayer(Player p) {
	  if (this.players.contains(p)) {
		  this.players.remove(p);
	  }
  }
  
  /**
   * Returns if there is a investigation centre
   * @return
   */
  public boolean canResearch() {
	  return this.can_research;
  }
    
  /**
   * Puts a investigation centre in the city.
   */
  public void putInvestigationCentre() {
	  this.can_research = true;
  }
  
  /**
   * Returns the epidemic of a given disease.
   */
  public Epidemic getEpidemic(Disease disease) {
	  	  
      for(Epidemic epidemic: this.epidemics){
		  if(epidemic.dis.equals(disease)){
			  return epidemic;
		  }
	  }	  
	  Epidemic epidemic = new Epidemic(disease, this, 0);
	  this.epidemics.add(epidemic);
	  disease.epidemics.add(epidemic);
	  
	  return epidemic;
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
