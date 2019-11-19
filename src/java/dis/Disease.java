package dis;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;

import city.*;
import _aux.Options;
import _aux.CustomTypes;

/*
  Disease class
    Defines the global information of a given disease
*/
public class Disease{
  public String alias;
  public String name;
  public int spreads_left;  // Once this value reaches zero, then the disease is completely spreaded (game should end)
  private ArrayList<Epidemic> epidemics;  // Each epidemic represents an occurrence of the disease in a given city

  // Disease status flags
  public boolean erradicated = false;
  public boolean cure = false;
  public boolean overtook_world = false;  // Completely spreaded


  // Constructor
  public Disease(String name, String alias, int max_spreads){
    this.name = name;
    this.alias = alias;
    this.spreads_left = max_spreads;
    this.epidemics = new ArrayList<Epidemic>();
  }
  
  public boolean getCure() {
	  return this.cure;
  }
  
  public void setCure(boolean hasCure) {
	  this.cure = hasCure;
  }

  /*
    parseDis
      Initializes diseases from a datafile.
      Expected format (csv):
        <Disease full name>;<alias>;[max epidemics];

      Alias must be unique. It'll be used as identifier.
      max_epidemics specifies the max number of epidemcs that specific disease will allow.

      **PARAMETERS:
        datafile: String; path to file.

      **RETURNS a map of diseases (key:alias, value:Disease)

  */
  public static Hashtable<String, Disease> parseDis(String datafile){

    Hashtable<String, Disease> diseases = new Hashtable<String, Disease>();
    try{
      BufferedReader br = new BufferedReader(new FileReader(datafile));
      String line;
      String[] dis_data;
      String dis_name;
      String dis_alias;
      int dis_max_spreads = Options.MAX_EPIDEMICS_SPREADS;

      // Reads disease data from subsequent lines (Must follow the expected disease format)
      while((line = br.readLine()) != null){
        dis_data = line.split(";");

        // Gets data from splitted line elements
        dis_name = dis_data[0];
        dis_alias = dis_data[1];

        // Tries to read max_epidemics value from file if specified
        if(dis_data.length > 2){
          try{
            dis_max_spreads = Integer.parseInt(dis_data[2]);
          }
          catch(NumberFormatException e)
          {
            if(Options.LOG.ordinal() >= CustomTypes.LogLevel.CRITICAL.ordinal())
              System.out.printf("[Disease] CRITICAL: Unexpected value read from CSV. Terminating...\n");

            System.exit(0);
          }
        }
        else{
          dis_max_spreads = Options.MAX_EPIDEMICS_SPREADS;
        }

        // Tries to create Disease object
        if(diseases.containsKey(dis_alias)){
          if(Options.LOG.ordinal() >= CustomTypes.LogLevel.WARN.ordinal())
            System.out.printf("[Disease] WARN: Disease \"%s\" duplicated. Ignoring...\n");
          continue;
        }

        // Creates Disease object and adds it to the dictionary
        else{
          Disease dis = new Disease(dis_name, dis_alias, dis_max_spreads);
          diseases.put(dis_alias, dis);

          if(Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
            System.out.printf("[Disease] INFO: New disease generated\n");

          if(Options.LOG.ordinal() >= CustomTypes.LogLevel.DUMP.ordinal())
          dis.dump();
        }
      }
      
      br.close();
    }
    catch(Exception e){
      if(Options.LOG.ordinal() >= CustomTypes.LogLevel.CRITICAL.ordinal())
        System.out.printf("[Disease] CRITICAL: Exception while parsing\n");

      System.err.println(e.getMessage());
      System.exit(0);
    }

    return diseases;
  }

  // Weakens disease by increasing spreads_left counter by "n_heals"
  public void heal(int n_heals){
    this.spreads_left += n_heals;
  }

  // Strengthens disease by decreasing spreads_left counter by "n_spreads"
  public void spread(int n_spreads){
    this.spreads_left -= n_spreads;
  }

  // Decreases spreads_left counter by "n_spreads" while infecting a new city
  public void spreadToCity(City c, int n_spreads){
    Epidemic e = new Epidemic(this, c, n_spreads);
    c.infect(e);

    this.spreads_left -= n_spreads;

    // If the number of spreads left gets depleated, then the disease overtakes the world (game ends)
    // overtook_world must be checked by the game whenever a disease spread occurs
    if(this.spreads_left < 0){
      this.overtook_world = true;
    }
  }

  // Dummy method to print disease data
  public void dump(){
    System.out.printf(">>Printing disease data (%s, alias: %s)\n", this.name, this.alias);
    System.out.printf(".Epidemics left: %d\n", this.spreads_left);
    System.out.printf(".Erradicated: %b\n", this.erradicated);
    System.out.printf(".Cure found: %b\n", this.cure);

    System.out.println();
  }
}
