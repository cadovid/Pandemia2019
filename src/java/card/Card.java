package card;
import java.util.*;

import _aux.*;
import city.*;

/*
 * Defines a game card
 * Type must be specified
 * City must be specified only if card is of type "city" or "infection"
 */
public class Card{
	public CustomTypes.CardType type;
	public City city;
	
	public Card(CustomTypes.CardType t) {
		this.type = t;
	}
	
	public Card(CustomTypes.CardType t, City c) {
		this(t);	// Calls alternative constructor
		this.city = c;
	}
	
	// Creates a list of cards of type "t" out of a set of City objects
	public static ArrayList<Card> parseCities(ArrayList<City> cities, CustomTypes.CardType t){
		ArrayList<Card> cards = new ArrayList<Card>();
		
		for(City c : cities) {
			Card card = new Card(t, c);
			cards.add(card);
		}
		
		return cards;
	}
}
