package card;

import java.util.ArrayList;

import _aux.CustomTypes;
import city.City;
import dis.Disease;

/*
  TODO Card class
    Defines a game card
*/
public abstract class Card {

	public City city;
	public boolean isEpidemic;

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public Disease getDisease() {
		return this.city.getLocalDisease();
	}

}
