package card;

import city.City;

public class InfectionCard extends Card {
	
	public Disease disease;
	
	public InfectionCard(City city) {
		this.city = city;
		this.color = city.getColor();
		this.disease = city.getDisease();
	}
}
