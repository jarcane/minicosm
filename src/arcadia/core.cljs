(ns arcadia.core
    (:require ))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom 1))

(defn game-loop! [ctx keys]
  (.clearRect ctx 0 0 256 256)
  (.fillText ctx @app-state 64 128)
  (cond
    (get @keys "ArrowUp") (swap! app-state inc)
    (get @keys "ArrowDown") (swap! app-state dec))
  (js/requestAnimationFrame (fn [] (game-loop! ctx keys))))

(defn start! []
  (let [canvas (js/document.getElementById "game")
        ctx (.getContext canvas "2d")
        keys (atom {})]
    (set! js/window.onkeyup (fn [e] (swap! keys assoc (.-code e) false)))
    (set! js/window.onkeydown (fn [e] (swap! keys assoc (.-code e) true)))
    (game-loop! ctx keys)))


(start!)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (reset! app-state 0))
