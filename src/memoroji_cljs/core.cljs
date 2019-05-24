(ns memoroji-cljs.core
  (:require [reagent.core :as reagent :refer [atom]]
            [memoroji-cljs.header :as header]
            [memoroji-cljs.board :as board]
            [memoroji-cljs.victoryScreen :as vs]))

(enable-console-print!)

(def emojiEntities
  (map js/String.fromCodePoint
       (take 30
             (shuffle
              (range 0x1F601 0x1F64F)))))

(defn createCard [uuid, matchId, emoji]
  {:id uuid
   :emoji emoji
   :matchId matchId
   :isRevealed false
   :matched false})

(defn createCards [pairs]
  (shuffle
   (flatten
    (for [i (range 0 pairs)]
      [(createCard (random-uuid) i (nth emojiEntities i))
       (createCard (random-uuid) i (nth emojiEntities i))]))))

(defonce app-state (atom {:numOfPairs 1
                          :revealedCards []
                          :score 0
                          :turnsCount 0
                          :isDone false
                          :isInteractionAllowed true
                          :cardsData (createCards 1)}))






(defn isMatch [card1 card2]
  (= (:matchId card1) (:matchId card2)))

(defn selectedCards [cardsData]
  (filter #(and (:isRevealed %1) (not (:matched %1))) cardsData))

(defn resetRevealedCards [cardsData]
  (let [updatedCardsData
        (map (fn [card]
               (assoc card :isRevealed false))
             (:cardsData @app-state))]
    (swap! app-state assoc-in [:cardsData] updatedCardsData)))



(defn processRevealedCards [cardsData]
  (swap! app-state assoc-in [:isInteractionAllowed] false)
  (let [selectedCards (selectedCards cardsData)]
    (js/setTimeout (fn [] (if (isMatch (first selectedCards) (last selectedCards))
                            (let [updatedCardsData
                                  (map (fn [card]
                                         (if
                                          (or (= (:id card) (:id (first selectedCards))) (= (:id card) (:id (last selectedCards))))
                                           (assoc card :matched true)
                                           card))
                                       (:cardsData @app-state))]
                              (swap! app-state assoc-in [:cardsData] updatedCardsData)
                              (swap! app-state update :score + 1)
                              (if (= (:score @app-state) (:numOfPairs @app-state))
                                (swap! app-state assoc-in [:isDone] true)))
                            (resetRevealedCards (:cardsData @app-state)))
                     (swap! app-state assoc-in [:isInteractionAllowed] true)) 1000)))

(defn handleClick [id]
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
          (if (= (count (selectedCards updatedCardsData)) 2) (processRevealedCards updatedCardsData)))
        )
      )))






(defn memoroji []
  [:div
   (header/render
    (:turnsCount @app-state)
    (:score @app-state)
    (:numOfPairs @app-state))
   (board/render (:cardsData @app-state) handleClick)
   (vs/render (:isDone @app-state))])

(reagent/render-component [memoroji]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  ;;(swap! app-state assoc-in [:score] 0)
  )
