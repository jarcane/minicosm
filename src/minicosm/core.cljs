(ns minicosm.core
  (:require [minicosm.ddn :refer [render!]]
            [clojure.set :as set]))

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

(defn- url-to-audio [url counts]
  (let [audio (js/Audio. url)]
    (.addEventListener audio "canplaythrough" (make-callback url :loaded counts))
    (set! (.-onerror audio) (make-callback url :error counts))
    audio))

(defn- dispatch-load [[type val] counts]
  (case type 
    :image (url-to-img val counts)
    :audio (url-to-audio val counts)))

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
         to-images (into {} (map (fn [[k v]] [k (dispatch-load v counts)]) assets))]
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

(defn- handle-audio [state assets audio-state to-play]
  (let [curr-state (deref audio-state)
        new-state (to-play state assets curr-state)
        pruned-state (update new-state :effects (fn [effects] (set (filter #(.-ended %) effects))))
        {curr-music :music curr-effects :effects} curr-state
        {new-music :music new-effects :effects} new-state
        effects-to-stop (set/difference curr-effects new-effects)
        effects-to-start (set/difference new-effects curr-effects)
        music-to-stop (set/difference curr-music new-music)
        music-to-start(set/difference new-music curr-music)]
    (doseq [e effects-to-stop]
      (.pause e)
      (set! (.-currentTime e) 0))
    (doseq [e effects-to-start]
      (.play e))
    (doseq [m music-to-stop]
      (.pause m)
      (set! (.-currentTime m) 0))
    (doseq [m music-to-start]
      (set! (.-loop m) true)
      (.play m))
    (reset! audio-state pruned-state)))

(defn- game-loop! [t ctx key-evs state assets audio-state {:keys [on-key on-tick to-play to-draw] :as handlers}]
  (let [new-state (-> state
                      (on-key @key-evs)
                      (on-tick t))]
    (.clearRect ctx 0 0 (.. ctx -canvas -width) (.. ctx -canvas -height))
    (handle-audio state assets audio-state to-play)
    (render! ctx (to-draw new-state assets))
    (js/requestAnimationFrame (fn [t] (game-loop! t ctx key-evs new-state assets audio-state handlers)))))

(defn start!
  "Initiates the main game loop. Expects a map of handler functions with the following keys:
  ```clj
  {:init (fn [] state) 
     A function that returns the initial game state, run before the loop starts
   :assets (fn [] assets)
     A function that returns a map of keys to asset type/url pairs, to be loaded into memory.
   :on-key (fn [state keys] state)
     A function that takes the current game state, and a set of current key codes pressed, and returns a new
     game state
   :on-tick (fn [state time] state)
     A function that takes the current game state, and a DOMHighResTimeStamp, indicating the number of ms since 
     time origin (https://developer.mozilla.org/en-US/docs/Web/API/DOMHighResTimeStamp#The_time_origin).
     Runs every frame, approx. 60fps. Returns a new game state.
   :to-play (fn [state assets] sound-state)
     A function that taks the current state, assets, and a boolean indicating if music is currently playing
     and returns a map describing sounds to play, in the form:
     `{:music <sound asset to loop or :stop> :effects [<sound assets to play once>]}`. If the :music key is empty,
     any currently playing sound will continue.
   :to-draw (fn [state assets] graphics-state)
     A function that takes the current game state, and returns a DDN vector
  ```"
  [{:keys [init assets] :as handlers}]
  (let [canvas (js/document.getElementById "game")
        ctx (.getContext canvas "2d")
        key-evs (atom #{})
        init-state (init)
        audio-state (atom {:music #{} :effects #{}})
        done-fn (fn [assets-loaded] (game-loop! 0 ctx key-evs init-state assets-loaded audio-state handlers))]
    (set! js/window.onkeyup (fn [e] (.preventDefault e) (swap! key-evs disj (.-code e))))
    (set! js/window.onkeydown (fn [e] (.preventDefault e) (swap! key-evs conj (.-code e))))
    (asset-loader ctx done-fn (assets))))
