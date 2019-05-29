(ns memoroji-cljs.victoryScreen
  (:require [reagent.core :as reagent :refer [atom]]))

(defn render [isGameOver]
  [:div {:id "victory-screen"
         :class (when (true? isGameOver) "active")}
   [:div {:class "victory-emoji"}
    (js/String.fromCodePoint "0x1F601")]
   [:div {:class "victory-message"} "Good job!"]
   [:button
    {:onClick #(js/window.location.reload ())}
    "Play again"]])