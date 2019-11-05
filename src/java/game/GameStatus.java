package game;

import player.Player;
import board.Board;
import _aux.CustomTypes;
import _aux.Options;

/*
  GameStatus class
    Holds relevant data about the game status
*/
public class GameStatus{
  public boolean won = false;
  public boolean lost = false;
  public boolean over = false;
  public int n_outbreaks = 0;
  private Board board;

  // Player data; actions left, and round status (resolving actions, drawing cards or infecting)
  public int p_actions_left = 0;  // Actions left for the current player
  private Player cp;  // Current player
  private CustomTypes.Round round;

  // Constructor
  public GameStatus(Board b){
    this.board = b;

    if(Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
      System.out.printf("[GameStatus] INFO: Board loaded:\n");

    if(Options.LOG.ordinal() >= CustomTypes.LogLevel.DUMP.ordinal())
      b.dump();
  }
}
