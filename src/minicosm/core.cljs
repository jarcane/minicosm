(ns minicosm.core
  (:require [minicosm.ddn :refer [render!]]))

(defn- url-to-img [url on-load on-error]
  (let [img (js/Image.)]
    (set! (.-onload img) on-load)
    (set! (.-onerror img) on-error)
    (set! (.-src img) url)
    img))

(defn- draw-loading [ctx]
  (let [w (.. ctx -canvas -width)
        h (.. ctx -canvas -height)
        old-ta (.-textAlign ctx)]
    (.clearRect 0 0 w h)
    (set! (.-textAlign ctx) "center")
    (.fillText ctx "Loading..." (/ w 2) (/ h 2))
    (set! (.-textAlign ctx) old-ta)))

(defn- asset-loader
  ([ctx assets]
   (let [counts (atom {:loaded 0
                       :error 0
                       :total (count assets)})
         on-load (fn [] (swap! counts update :loaded inc))
         on-error (fn [] (swap! counts update :error inc))
         to-images (into {} (map (fn [[k v]] [k (url-to-img v on-load on-error)]) assets))]
     (draw-loading ctx)
     (js/requestAnimationFrame (fn [] (asset-loader ctx to-images counts)))))
  ([ctx assets counts]
   (let [{:keys [loaded error total]} @counts]
     (if (= (+ loaded error)
            total)
       assets
       (do 
         (draw-loading ctx)
         (js/requestAnimationFrame (fn [] (asset-loader ctx assets counts))))))))

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
