package game;

import player.Player;
import board.Board;
import _aux.CustomTypes;
import _aux.CustomTypes.Round;
import _aux.Options;

/*
  GameStatus class
    Holds relevant data about the game status
*/
public class GameStatus {
	public boolean won = false;
	public boolean lost = false;
	public boolean over = false;
	public int n_outbreaks = 0;
	public Board board;
	public int[] infection_levels = new int[] { 2, 2, 2, 3, 3, 4, 4 };
	public int current_infection_level = 0;
	public int current_research_centers = 0;

	// Player data; actions left, and round status (resolving actions, drawing cards
	// or infecting)
	public int p_actions_left = 0; // Actions left for the current player
	public Player cp; // Current player
	public CustomTypes.Round round;
	public int drawnCards = 0;

	// Constructor
	public GameStatus(Board b) {
		this.board = b;
		this.round = Round.ACT;

		if (Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
			System.out.printf("[GameStatus] INFO: Board loaded:\n");

		if (Options.LOG.ordinal() >= CustomTypes.LogLevel.DUMP.ordinal())
			b.dump();
	}
}
