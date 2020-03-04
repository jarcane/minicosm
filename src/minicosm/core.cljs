(ns minicosm.core
  (:require [minicosm.drawing :refer [draw-commands!]]
            [minicosm.ddn :refer [render!]]))

(defn- game-loop! [t ctx key-evs state {:keys [on-key on-tick to-draw] :as handlers}]
  (let [new-state (-> state
                      (on-key @key-evs)
                      (on-tick t))]
    (.clearRect ctx 0 0 (.. ctx -canvas -width) (.. ctx -canvas -height))
    (render! ctx (to-draw new-state))
    (js/requestAnimationFrame (fn [t] (game-loop! t ctx key-evs new-state handlers)))))

(defn start!
  "Initiates the main game loop. Expects a map of handler functions with the following keys:
  {:init (fn [] state) 
     A function that returns the initial game state, run before the loop starts
   :on-key (fn [state keys] state)
     A function that takes the current game state, and a set of current key codes pressed, and returns a new
     game state
   :on-tick (fn [state time] state)
     A function that takes the current game state, and a DOMHighResTimeStamp, indicating the number of ms since 
     time origin (https://developer.mozilla.org/en-US/docs/Web/API/DOMHighResTimeStamp#The_time_origin).
     Runs every frame, approx. 60fps. Returns a new game state.
   :to-draw (fn [state] graphics-state)
     A function that takes the current game state, and returns a map of the graphics state to be be drawn.
     The graphics-state map may contain any of the following keys:
     {:background A static image to serve as the background
      :sprites An array of tuples, each containing a sprite and its x,y coordinates
      :text An array of tuples, each containing a string and its x,y coordinates
      :draw An array of draw commands, each a vector containing the keyword for the command and its arguments}
     Note that the elements of the display will be drawn in the order listed here, first background, then sprites,
     and finally text.}"
  [{:keys [init] :as handlers}]
  (let [canvas (js/document.getElementById "game")
        ctx (.getContext canvas "2d")
        key-evs (atom #{})
        init-state (init)]
    (set! js/window.onkeyup (fn [e] (swap! key-evs disj (.-code e))))
    (set! js/window.onkeydown (fn [e] (swap! key-evs conj (.-code e))))
    (game-loop! 0 ctx key-evs init-state handlers)))
