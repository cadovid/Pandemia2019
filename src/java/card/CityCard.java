package card;

import city.City;

public class CityCard extends Card {

	public CityCard(City city, boolean isEpidemic) {
		super();
		this.city = city;
		this.isEpidemic = isEpidemic;
	}

	public boolean isEpidemic() {
		return this.isEpidemic;
	}
}
