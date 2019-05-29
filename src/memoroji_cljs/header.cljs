(ns memoroji-cljs.header
  (:require [reagent.core :as reagent :refer [atom]]))

(defn render [turns score numOfPairs]
  [:header
   [:h1 "Memor-oji"]
   [:div {:class "game-details"}
    [:div {:class "turs-container"}
     [:div {:class "turns-title"} "Turns"]
     [:div {:id "turns"} turns]]
    [:div {:class "score-container"}
     [:div {:class "score-title"} "Matches"]
     [:div {:class "score"}
      [:span {:id "matches"} score]
      "/"
      [:span {:id "pairs"} numOfPairs]]]]])