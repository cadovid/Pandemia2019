package board;

import java.util.ArrayList;

import city.*;

/*
  Cell class
    Represents a board cell. Holds relevant cell data
*/
public class Cell{

  // Cell coordinates in the board (as a grid)
  public int x = -1;
  public int y = -1;

  // Threat level. The more diseases sourrounding the cell, the higher
  private int threat = 0;

  // A cell without city is a null cell
  public City city = null;

  // Constructors
  public Cell(int x, int y){
    this.x = x;
    this.y = y;
  }

  public Cell(int x, int y, City c){
    this(x, y);

    this.city = c;
  }

  // Setter / getters
  public City getCity(){
    return this.city;
  }

  public void setCity(City c){
    this.city = c;
  }

  public String getCityAlias(){
    String city_alias = null;
    if(this.city != null){
      city_alias = this.city.alias;
    }

    return city_alias;
  }

  // Dummy method to check whether a cell is empty
  public boolean isEmpty(){
    if(this.city == null){
      return true;
    }

    else{
      return false;
    }
  }
}
