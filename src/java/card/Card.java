package card;

import city.City;
import dis.Disease;

/*
  TODO Card class
    Defines a game card
*/
public abstract class Card {

	protected City city;

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
