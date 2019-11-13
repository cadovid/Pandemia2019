package card;

import city.City;
import _aux.CustomTypes.Color;

public abstract class Card {

	Color color;
	City city;
	public Color getColor() {
		return color;
	}
	public City getCity() {
		return city;
	}
}
