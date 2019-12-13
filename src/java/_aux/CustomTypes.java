package _aux;

/*
  Custom types
    Static class. Used to define some useful data types
*/
public final class CustomTypes {

	// Round steps
	public static enum Round {
		ACT, STEAL, INFECT, WAIT
	}

	// Specifies program log level
	public static enum LogLevel {
		CRITICAL, WARN, INFO, DUMP, ALL
	}

	// Direction for adjacent moves
	public static enum Direction {
		UP, DOWN, LEFT, RIGHT
	}

	// Card types
	public static enum CardType {
		CITY, INFECTION, EPIDEMIC, SPECIAL
	}

	// Deck type
	public static enum DeckType {
		GAME, INFECTION
	}

	// Game action mode
	public static enum GameMode {
		TURN, TIMESTAMP
	}
}
