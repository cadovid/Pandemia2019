package _aux;

/*
  Options
    Static class. Used to define game and compile options.
*/
public final class Options {

	// Game options
	public static final int MAX_EPIDEMICS_SPREADS = 20;
	public static final int MAX_SPREADS_PER_CITY = 3;
	public static final int PLAYER_MAX_CARDS = 7;
	public static final int PLAYER_MAX_ACTIONS = 4;
	public static final int PLAYER_DRAW_CARDS = 2;
	public static final int MAX_RESEARCH_CENTERS = 7;

	// Program options
	public static final CustomTypes.LogLevel LOG = CustomTypes.LogLevel.CRITICAL;

	// Private constructor. This class shouldn't be initialized
	private Options() {

	}
}
