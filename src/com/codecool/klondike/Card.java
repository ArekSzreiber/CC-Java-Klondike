package com.codecool.klondike;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.util.*;

public class Card extends ImageView {

    private int suit; // czyli kolorek
    private int rank; // czyli warość 1, 2, 3, 4, ... , 10, 11 (J), 12 (Q), 13 (K)
    private boolean faceDown;

    private Image backFace;
    private Image frontFace;
    private Pile containingPile; // w jakiej kupce jest ta karta
    private DropShadow dropShadow;

    static Image cardBackImage;
    private static final Map<String, Image> cardFaceImages = new HashMap<>();
    public static final int WIDTH = 150;
    public static final int HEIGHT = 215;

    public Card(int suit, int rank, boolean faceDown) {
        this.suit = suit;
        this.rank = rank;
        this.faceDown = faceDown;
        this.dropShadow = new DropShadow(2, Color.gray(0, 0.75));
        backFace = cardBackImage;
        frontFace = cardFaceImages.get(getShortName());
        setImage(faceDown ? backFace : frontFace);
        setEffect(dropShadow);
    }

    public int getSuit() {
        return suit;
    }

    public int getRank() {
        return rank;
    }

    public boolean isFaceDown() {
        return faceDown;
    }

    public String getShortName() {
        return "S" + suit + "R" + rank;
    }

    public DropShadow getDropShadow() {
        return dropShadow;
    }

    public Pile getContainingPile() {
        return containingPile;
    }

    public void setContainingPile(Pile containingPile) {
        this.containingPile = containingPile;
    }

    public void moveToPile(Pile destPile) {
        this.getContainingPile().getCards().remove(this);
        destPile.addCard(this);
    }

    public void flip() {
        faceDown = !faceDown;
        setImage(faceDown ? backFace : frontFace);
    }

    public void flipToFront() {
        faceDown = false;
        setImage(frontFace);
    }

    @Override
    public String toString() {
        return "The " + "Rank" + rank + " of " + "Suit" + suit;
    }

    public boolean isOppositeColor(Card card) {
        return isBlack(this) ^ isBlack(card);
    }


    public static boolean isBlack(Card card) {
        if (card.getSuit() == 3 || card.getSuit() == 4) {
            return true;
        } else return false;
    }

    public static boolean isSameSuit(Card card1, Card card2) {
        return card1.getSuit() == card2.getSuit();
    }

    public static List<Card> createNewDeck() {
        List<Card> deck = new ArrayList<>();
        for (int suit = 1; suit < 5; suit++) {
            for (int rank = 1; rank < 14; rank++) {
                deck.add(new Card(suit, rank, true));
            }
        }
        Collections.shuffle(deck);
        return deck;
    }

    public static void loadCardImages() {
        cardBackImage = new Image("card_images/card_back.png");
        String suitName;

        for (Suit suit : Suit.values()) {
            suitName = suit.getName();


            for (Rank rank : Rank.values()) {
                String cardName = suitName + rank.getRank();
                String cardId = "S" + suit.getNumber() + "R" + rank.getRank();
                String imageFileName = "card_images/" + cardName + ".png";
                cardFaceImages.put(cardId, new Image(imageFileName));
            }

        }

    }

    public enum Suit {
        HEARTS("hearts", 1),
        DIAMONDS("diamonds", 2),
        SPADES("spades", 3),
        CLUBS("clubs", 4);

        private String name;
        private int number;

        Suit(String name, int number) {
            this.name = name;
            this.number = number;
        }

        public String getName() {
            return this.name;
        }

        public int getNumber() {
            return this.number;
        }

    }

    public enum Rank {
        ACE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6),
        SEVEN(7),
        EIGHT(8),
        NINE(9),
        TEN(10),
        JACK(11),
        QUEEN(12),
        KING(13);

        private int rank;

        Rank(int rank) {
            this.rank = rank;
        }

        public int getRank() {
            return this.rank;
        }

    }

}
