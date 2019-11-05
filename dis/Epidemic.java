package dis;

import city.City;
import dis.Disease;
import _aux.Options;

public class Epidemic{
  public City city_host;
  public Disease dis;
  public int spread_level = 1;

  // Constructor
  public Epidemic(Disease d, City c, int spread){
    this.dis = d;
    this.city_host = c;
    this.spread_level = spread;
  }

  // Increases the strength of the epidemic in the city by "increment". Spreads to neighbor cities if neccesary
  public void strengthen(int increment){

    // If max spread level exceeded, then an outbreak occurs.
    if(this.spread_level + increment > Options.MAX_SPREADS_PER_CITY){

      // Updates disease total spreads
      dis.spread(Options.MAX_SPREADS_PER_CITY - this.spread_level);
      this.spread_level = Options.MAX_SPREADS_PER_CITY;

      // Expands to adjacent cities
      outbreak();
    }

    // Local spread. Only increases strength of the disease in the city
    else{
      this.spread_level += increment;
      dis.spread(increment);
    }
  }

  // TO-DO
  /*
    Spreads a disease to a neighbor city using its City object
    // Should call the "spreadToCity" method of the associated Disease object
  */
  public void outbreak(){
    return;
  }
}
