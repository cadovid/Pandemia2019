package card;

import city.City;

public class InfectionCard extends Card {
	public InfectionCard(City city) {
		this.city = city;
		this.color = city.getColor();
	}
}
