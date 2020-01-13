(ns arcadia.core
    (:require ))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom 1))

(defn game-loop! [ctx key-evs state {:keys [on-key on-tick to-draw] :as handlers}]
  (let [new-state (-> state
                      (on-key @key-evs)
                      (on-tick))]
    (.clearRect ctx 0 0 256 256)
    (to-draw new-state ctx)
    (js/requestAnimationFrame (fn [] (game-loop! ctx key-evs new-state handlers)))))

(defn start! [{:keys [init] :as handlers}]
  (let [canvas (js/document.getElementById "game")
        ctx (.getContext canvas "2d")
        key-evs (atom {})
        init-state (init)]
    (set! js/window.onkeyup (fn [e] (swap! key-evs assoc (.-code e) false)))
    (set! js/window.onkeydown (fn [e] (swap! key-evs assoc (.-code e) true)))
    (game-loop! ctx key-evs init-state handlers)))

(start!
 {:init (fn [] 0)
  :on-key (fn [state key-evs]
            (cond
              (get key-evs "ArrowUp") (inc state)
              (get key-evs "ArrowDown") (dec state)
              :else state))
  :on-tick (fn [x] x)
  :to-draw (fn [state ctx]
             (.fillText ctx state 64 128))})

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (reset! app-state 0))
