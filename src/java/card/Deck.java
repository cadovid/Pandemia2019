package card;

import java.util.*;

import _aux.*;
import city.*;

/*
 * Represents a card deck
 * Must define methods to draw, shuffle & append cards to it
 */
public class Deck {
	public ArrayList<Card> cards;
	public CustomTypes.DeckType type;

	// Initializes empty deck
	public Deck(CustomTypes.DeckType t) {
		this.type = t;
		this.cards = new ArrayList<Card>();
	}

	// Initializes deck out of a list of cards
	public Deck(ArrayList<Card> cs, CustomTypes.DeckType t) {
		this.type = t;
		this.cards = cs;
	}

	// Shuffles deck
	public void shuffle() {
		Collections.shuffle(this.cards);
	}

	// Appends a set of cards to the top
	public void atop(ArrayList<Card> stack) {
		stack.addAll(this.cards);
		this.cards = stack;
	}

	// Draws a card from the top of the deck
	public Card draw() {
		Card draw;
		if (this.cards.size() > 0) {
			draw = this.cards.remove(0);
		}

		else {
			draw = null;
		}
		return draw;
	}

	// Draws n cards from the deck (returns a sorted list)
	public ArrayList<Card> draw(int n) {
		ArrayList<Card> cards = new ArrayList<Card>();
		Card draw;
		//System.out.println("cards BEFORE drawing "+this.cards.size()+" n"+n);
		while (n-- > 0) {
			draw = this.draw();

			if (draw != null) {
				cards.add(draw);
			}

			// If can't draw the specified number of cards from the deck, a null object is
			// returned (result must be handled properly elsewhere)
			else {
				cards = null;
				break;
			}

		}
		//System.out.println("cards AFTER drawing "+this.cards.size());

		return cards;
	}

	// Stacks a card to the top of the deck
	public void stack(Card c) {
		this.cards.add(0, c);
	}

	// Stacks a list of cards to the top of the deck
	public void stack(ArrayList<Card> cl) {
		for (Card c : cl) {
			this.stack(c);
		}
	}

	// Inserts a set of cards densely distributed in the deck
	public void shove(ArrayList<Card> cards) {
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
