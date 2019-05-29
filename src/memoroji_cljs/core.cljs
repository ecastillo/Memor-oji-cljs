(ns memoroji-cljs.core
  (:require [reagent.core :as reagent :refer [atom]]
            [memoroji-cljs.header :as header]
            [memoroji-cljs.board :as board]
            [memoroji-cljs.victoryScreen :as vs]))

(enable-console-print!)

; List of possible emojis to use as symbols on the cards
(def emojiEntities
  (map js/String.fromCodePoint
       (take 30
             (shuffle
              (range 0x1F601 0x1F64F)))))

; Create a card
(defn createCard [uuid, matchId, emoji]
  {:id uuid
   :emoji emoji
   :matchId matchId
   :isRevealed false
   :isMatched false})

; Create a set of cards for the game
(defn createCards [pairs]
  (shuffle
   (flatten
    (for [i (range 0 pairs)]
      [(createCard (random-uuid) i (nth emojiEntities i))
       (createCard (random-uuid) i (nth emojiEntities i))]))))

; State
(defonce app-state (atom {:numOfPairs 12
                          :score 0
                          :turns 0
                          :isGameOver false
                          :isInteractionAllowed true
                          :cardsData (createCards 12)}))

; Are two cards a match?
(defn isMatch [card1 card2]
  (= (:matchId card1) (:matchId card2)))

; Cards that have been flipped and waiting to be matched
(defn selectedCards [cardsData]
  (filter #(and (:isRevealed %1) (not (:isMatched %1))) cardsData))

; Flip selected cards face down
(defn resetSelectedCards [cardsData]
  (let [updatedCardsData
        (map (fn [card]
               (assoc card :isRevealed false))
             (:cardsData @app-state))]
    (swap! app-state assoc-in [:cardsData] updatedCardsData)))

; Process selected pair of cards and update game accordingly
(defn processSelectedCards [cardsData]
  (swap! app-state assoc-in [:isInteractionAllowed] false)
  (let [selectedCards (selectedCards cardsData)]
    (js/setTimeout (fn []
                     (if (isMatch (first selectedCards) (last selectedCards))
                       (let [updatedCardsData
                             (map (fn [card]
                                    (if
                                     (or (= (:id card) (:id (first selectedCards))) (= (:id card) (:id (last selectedCards))))
                                      (assoc card :isMatched true)
                                      card))
                                  (:cardsData @app-state))]
                         (swap! app-state assoc-in [:cardsData] updatedCardsData)
                         (swap! app-state update :score + 1)
                         (if (= (:score @app-state) (:numOfPairs @app-state))
                           (swap! app-state assoc-in [:isGameOver] true)))
                       (resetSelectedCards (:cardsData @app-state)))
                     (swap! app-state update :turns + 1)
                     (swap! app-state assoc-in [:isInteractionAllowed] true)) 1000)))

; Handle when a card is selected
(defn handleSelectCard [id]
  (let [selectedCard (filter (:cardsData #(= id (:id %1)) @app-state))]
    (if (and (not (:isRevealed selectedCard)) (:isInteractionAllowed @app-state))
      (do
        (let [updatedCardsData
              (map (fn [card]
                     (if
                      (= id (:id card))
                       (assoc card :isRevealed true)
                       card))
                   (:cardsData @app-state))]
          (swap! app-state assoc-in [:cardsData] updatedCardsData)
          (if (= (count (selectedCards updatedCardsData)) 2) (processSelectedCards updatedCardsData)))))))

; Main component
(defn memoroji []
  [:div
   (header/render
    (:turns @app-state)
    (:score @app-state)
    (:numOfPairs @app-state))
   (board/render (:cardsData @app-state) handleSelectCard)
   (vs/render (:isGameOver @app-state))])

; Render components to screen
(reagent/render-component [memoroji]
                          (. js/document (getElementById "app")))
