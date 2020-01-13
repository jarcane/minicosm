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
    (game-loop! 0 ctx key-evs init-state handlers)))

(start!
 {:init (fn [] 0)
  :on-key (fn [state key-evs]
            (cond
              (get key-evs "ArrowUp") (inc state)
              (get key-evs "ArrowDown") (dec state)
              :else state))
  :on-tick (fn [state _] state)
  :to-draw (fn [state ctx]
             (.fillText ctx state 64 128))})
