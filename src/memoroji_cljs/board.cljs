(ns memoroji-cljs.board
  (:require [reagent.core :as reagent :refer [atom]]))

(defn render [cards handleClick]
  [:div {:id "board"}
   (for [card cards]
     ^{:key (:id card)}
     [:div {:class ["card"
                    (when (:isRevealed card) "revealed")
                    (when (:matched card) "matched")]
            :onClick #(handleClick (:id card))}
      [:div {:class "card-outer"}
       [:div {:class "card-inner"}
        [:div {:class "card-back"}]
        [:div {:class "card-face"}
         [:div {:class "card-icon"} (:emoji card)]]]]])])