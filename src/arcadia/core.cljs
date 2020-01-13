(ns arcadia.core
    (:require ))

(enable-console-print!)

(println "This text is printed from src/arcadia/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom 1))

(defn start []
  (let [canvas (js/document.getElementById "game")
        ctx (.getContext canvas "2d")]
    (.clearRect ctx 0 0 256 256)
    (.fillText ctx @app-state 64 128)
    (swap! app-state inc)
    (js/requestAnimationFrame start)))

(start)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
