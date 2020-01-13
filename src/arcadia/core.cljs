(ns arcadia.core
    (:require ))

(enable-console-print!)

(defn game-loop! [t ctx key-evs state {:keys [on-key on-tick to-draw] :as handlers}]
  (let [new-state (-> state
                      (on-key @key-evs)
                      (on-tick t))]
    (.clearRect ctx 0 0 256 256)
    (to-draw new-state ctx)
    (js/requestAnimationFrame (fn [t] (game-loop! t ctx key-evs new-state handlers)))))

(defn start! [{:keys [init] :as handlers}]
  (let [canvas (js/document.getElementById "game")
        ctx (.getContext canvas "2d")
        key-evs (atom {})
        init-state (init)]
    (set! js/window.onkeyup (fn [e] (swap! key-evs assoc (.-code e) false)))
    (set! js/window.onkeydown (fn [e] (swap! key-evs assoc (.-code e) true)))
    (.scale ctx 2 2)
    (game-loop! 0 ctx key-evs init-state handlers)))

(start!
 {:init (fn [] [128 128])
  :on-key (fn [[x y] key-evs]
            (cond
              (get key-evs "ArrowUp") [x (dec y)]
              (get key-evs "ArrowDown") [x (inc y)]
              (get key-evs "ArrowLeft") [(dec x) y]
              (get key-evs "ArrowRight") [(inc x) y]
              :else [x y]))
  :on-tick (fn [state _] state)
  :to-draw (fn [[x y] ctx]
             (def img (js/Image.))
             (set! (.-src img) "https://media.giphy.com/media/NMr9UUZSqQbhS/giphy.gif")
             (.drawImage ctx img x y))})
