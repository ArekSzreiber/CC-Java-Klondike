package com.codecool.klondike;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            card = card.getContainingPile().getTopCard();
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        if (activePile.getPileType() == Pile.PileType.STOCK) {
            return;
        }
        if (card.isFaceDown()) {
            return;
        }

        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        draggedCards.clear();
        int cardIndex = activePile.getCards().indexOf(card);

        for (Card element : activePile.getCards()) {
            if (activePile.getCards().indexOf(element) >= cardIndex) {
                draggedCards.add(element);
                element.getDropShadow().setRadius(20);
                element.getDropShadow().setOffsetX(10);
                element.getDropShadow().setOffsetY(10);

                element.toFront();
                element.setTranslateX(offsetX);
                element.setTranslateY(offsetY);
            }
        }
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();

        List<Pile> tableauAndFoundationPiles = createJoinedFoundationAndTableauPiles();

        Pile pile = getValidIntersectingPile(card, tableauAndFoundationPiles);

        if (isGameWon()) {
            showWinPopup();
        }

        if (pile != null) {
            for (Card element : draggedCards) {
                handleValidMove(element, pile);
            }
            //handleValidMove(card, pile);
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards = FXCollections.observableArrayList();
        }

    };

    private List<Pile> createJoinedFoundationAndTableauPiles() {
        List<Pile> tableauAndFoundationPiles = FXCollections.observableArrayList();

        for (Pile pile : tableauPiles) {
            tableauAndFoundationPiles.add(pile);
        }

        for (Pile pile : foundationPiles) {
            tableauAndFoundationPiles.add(pile);
        }

        return tableauAndFoundationPiles;
    }

    public boolean isGameWon() {
        int cardsOnFoundationPiles = 0;
        for (Pile pile : foundationPiles) {
            cardsOnFoundationPiles += pile.getCards().size();
        }

        if (cardsOnFoundationPiles == 52) {
            return true;
        } else return false;
    }

    private void showWinPopup() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Application information");
        alert.setHeaderText("Congratulations, you won!");
        alert.setContentText("Do you want to replay?");
        alert.show();

//        Optional<ButtonType> result = alert.showAndWait();
//        if (result.get() == ButtonType.OK) {
//            restartGame();
//        }
//        else {
//            Platform.exit();
//        }
    }

    public Game() {
        deck = Card.createNewDeck();
        initPiles();
        dealCards();
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        while (!discardPile.isEmpty()) {
            Card card = discardPile.getTopCard();
            card.moveToPile(stockPile);
            card.flip();
        }
        System.out.println("Stock refilled from discard pile.");
    }

    public boolean isMoveValid(Card card, Pile destPile) {

        if (destPile.getPileType() == Pile.PileType.TABLEAU) {

            if (destPile.isEmpty()) {
                return card.getRank() == Card.Rank.KING.getRank();
            } else { // if tableau is not empty
                Card topCard = destPile.getTopCard();
                boolean oppositeColorOk = topCard.isOppositeColor(card);
                boolean previousRankOk = topCard.getRank() - 1 == card.getRank();
                return oppositeColorOk && previousRankOk;
            }

        } else if (destPile.getPileType() == Pile.PileType.FOUNDATION) {

            if (destPile.isEmpty()) {
                return card.getRank() == Card.Rank.ACE.getRank();
            } else {
                Card topCard = destPile.getTopCard();
                boolean SuitSameOk = topCard.getSuit() == card.getSuit();
                boolean NextRankOk = topCard.getRank() == card.getRank() - 1;
                return SuitSameOk && NextRankOk;
            }

        }

        return false;
    }

    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();

    }

    private void setStockPilePosition() {
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
    }

    private void setDiscardPilePosition() {
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
    }

    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        setStockPilePosition();
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);


        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        setDiscardPilePosition();
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();

        int cardAdder = 1;

        for (Pile pile : tableauPiles) {
            for (int i = 0; i < cardAdder; i++) {
                Card nextCard = deckIterator.next();
                pile.addCard(nextCard);
                addMouseEventHandlers(nextCard);
                getChildren().add(nextCard);

                if (i == cardAdder - 1) {
                    nextCard.flip();
                }
            }


            cardAdder++;
        }

        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });

    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}
