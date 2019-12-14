package _aux;

/*
  Datapaths
    Static class. Used to define common datapaths to files used by the game.
*/
public final class Datapaths{
  // Project root
  public static String proot = System.getProperty("user.dir").concat("/src/java/");
	
  // Game configuration
  public static String role_list = proot + "_initdata/roles.csv";
  public static String disease_list = proot + "_initdata/dis.csv";
  public static String city_list = proot + "_initdata/cities.csv";
  public static String map = proot + "_initdata/map.csv";
  public static String player_list = proot + "_initdata/players.csv";

  // GRA
  public static String sprites_cellbg_folder = proot + "graphics/sprites/cellbg/";

  // TODO
  // Returns a string representing the path of the data file
  public static String getFolderPath(String filetype) {
	  return null;
  }
  
  // Private constructor. This class shouldn't be initialized
  private Datapaths(){

  }
}
