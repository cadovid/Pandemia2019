package card;

import java.util.ArrayList;

import _aux.CustomTypes;
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

	// Creates a list of cards of type "t" out of a set of City objects
	public static ArrayList<CityCard> parseCities(ArrayList<City> cities, CustomTypes.CardType t) {
		ArrayList<CityCard> cards = new ArrayList<CityCard>();

		for (City c : cities) {
			CityCard card = new CityCard(c, false);
			cards.add(card);
		}

		return cards;
	}
}
