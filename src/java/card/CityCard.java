package card;

import city.City;

public class CityCard extends Card {
	public CityCard(City city) {
		this.city = city;
		this.color = city.getColor();
	}
}
