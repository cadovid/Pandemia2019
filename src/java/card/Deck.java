package card;

import java.util.*;

import _aux.*;
import city.*;

/*
 * Represents a card deck
 * Must define methods to draw, shuffle & append cards to it
 */
public class Deck {
	public ArrayList<CityCard> cards;
	public CustomTypes.DeckType type;

	// Initializes empty deck
	public Deck(CustomTypes.DeckType t) {
		this.type = t;
		this.cards = new ArrayList<CityCard>();
	}

	// Initializes deck out of a list of cards
	public Deck(ArrayList<CityCard> cs, CustomTypes.DeckType t) {
		this.type = t;
		this.cards = cs;
	}

	// Shuffles deck
	public void shuffle() {
		Collections.shuffle(this.cards);
	}

	// Appends a set of cards to the top
	public void atop(ArrayList<CityCard> stack) {
		stack.addAll(this.cards);
		this.cards = stack;
	}

	// Draws a card from the top of the deck
	public CityCard draw() {
		return this.cards.remove(0);
	}

	public CityCard bottomDraw() {
		return this.cards.remove(this.cards.size() - 1);
	}

	// Draws n cards from the deck
	public HashMap<String, CityCard> draw(int n) {
		HashMap<String, CityCard> cards = new HashMap<String, CityCard>();

		while (n-- > 0) {
			CityCard card = this.draw();
			cards.put(card.city.alias, card);
		}

		return cards;
	}

	// Stacks a card to the top of the deck
	public void stack(CityCard c) {
		this.cards.add(0, c);
	}

	// Stacks a list of cards to the top of the deck
	public void stack(ArrayList<CityCard> cl) {
		for (CityCard c : cl) {
			this.stack(c);
		}
	}

	// Inserts a set of cards densely distributed in the deck
	public void shove(ArrayList<CityCard> cards) {
		int size_deck = this.cards.size();
		int size_shove = cards.size();

		// Splits deck into even regions
		int size_region = (int) (size_deck / size_shove);

		// Inserts a card in a random position of each region
		for (int i = size_shove; i > 0; i--) {
			Random rand = new Random();
			int pos_rand = rand.nextInt(i * size_region - (i - 1) * size_region + 1) + (i - 1) * size_region;

			this.cards.add(pos_rand, cards.get(i - 1));
		}
	}
}
