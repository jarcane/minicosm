(ns minicosm.core
  (:require [minicosm.ddn :refer [render!]]))

(defn- make-callback
  [url key counts]
  (fn [_]
    (println (name key) url)
    (swap! counts update key inc)))

(defn- url-to-img [url counts]
  (let [img (js/Image.)]
    (set! (.-onload img) (make-callback url :loaded counts))
    (set! (.-onerror img) (make-callback url :error counts))
    (set! (.-src img) url)
    img))

(defn- draw-loading [ctx]
  (let [w (.. ctx -canvas -width)
        h (.. ctx -canvas -height)
        old-ta (.-textAlign ctx)]
    (.clearRect ctx 0 0 w h)
    (set! (.-textAlign ctx) "center")
    (.fillText ctx "Loading..." (/ w 2) (/ h 2))
    (set! (.-textAlign ctx) old-ta)))

(defn- asset-loader
  ([ctx done-fn assets]
   (let [counts (atom {:loaded 0
                       :error 0
                       :total (count assets)})
         to-images (into {} (map (fn [[k v]] [k (url-to-img v counts)]) assets))]
     (draw-loading ctx)
     (js/requestAnimationFrame (fn [_] (asset-loader ctx done-fn to-images counts)))))
  ([ctx done-fn assets counts]
   (let [{:keys [loaded error total]} @counts]
     (if (= (+ loaded error)
            total)
       (done-fn assets)
       (do 
         (draw-loading ctx)
         (js/requestAnimationFrame (fn [_] (asset-loader ctx done-fn assets counts))))))))

(defn- game-loop! [t ctx key-evs state assets {:keys [on-key on-tick to-draw] :as handlers}]
  (let [new-state (-> state
                      (on-key @key-evs)
                      (on-tick t))]
    (.clearRect ctx 0 0 (.. ctx -canvas -width) (.. ctx -canvas -height))
    (render! ctx (to-draw new-state assets))
    (js/requestAnimationFrame (fn [t] (game-loop! t ctx key-evs new-state assets handlers)))))

(defn start!
  "Initiates the main game loop. Expects a map of handler functions with the following keys:
  {:init (fn [] state) 
     A function that returns the initial game state, run before the loop starts
   :assets (fn [] assets)
     A function that returns a map of keys to asset urls, to be loaded into memory.
   :on-key (fn [state keys] state)
     A function that takes the current game state, and a set of current key codes pressed, and returns a new
     game state
   :on-tick (fn [state time] state)
     A function that takes the current game state, and a DOMHighResTimeStamp, indicating the number of ms since 
     time origin (https://developer.mozilla.org/en-US/docs/Web/API/DOMHighResTimeStamp#The_time_origin).
     Runs every frame, approx. 60fps. Returns a new game state.
   :to-draw (fn [state] graphics-state)
     A function that takes the current game state, and returns a DDN vector"
  [{:keys [init assets] :as handlers}]
  (let [canvas (js/document.getElementById "game")
        ctx (.getContext canvas "2d")
        key-evs (atom #{})
        init-state (init)
        done-fn (fn [assets-loaded] (game-loop! 0 ctx key-evs init-state assets-loaded handlers))]
    (set! js/window.onkeyup (fn [e] (swap! key-evs disj (.-code e))))
    (set! js/window.onkeydown (fn [e] (swap! key-evs conj (.-code e))))
    (asset-loader ctx done-fn (assets))))
