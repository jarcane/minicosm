(ns arcadia.core
    (:require ))

(enable-console-print!)

(defn image [url]
  (let [img (js/Image.)]
    (set! (.-src img) url)
    img))

(defn draw! [ctx graphics]
  (.drawImage ctx (:background graphics) 0 0)
  (doseq [[spr x y] (:sprites graphics)]
    (.drawImage ctx spr x y)))

(defn game-loop! [t ctx key-evs state {:keys [on-key on-tick to-draw] :as handlers}]
  (let [new-state (-> state
                      (on-key @key-evs)
                      (on-tick t))]
    (.clearRect ctx 0 0 256 256)
    (draw! ctx (to-draw new-state))
    (js/requestAnimationFrame (fn [t] (game-loop! t ctx key-evs new-state handlers)))))

(defn start! [{:keys [init] :as handlers}]
  (let [canvas (js/document.getElementById "game")
        ctx (.getContext canvas "2d")
        key-evs (atom {})
        init-state (init)]
    (set! js/window.onkeyup (fn [e] (swap! key-evs assoc (.-code e) false)))
    (set! js/window.onkeydown (fn [e] (swap! key-evs assoc (.-code e) true)))
    (game-loop! 0 ctx key-evs init-state handlers)))

(def game-handlers
  {:init (fn [] [128 128])
   :on-key (fn [[x y] key-evs]
             (cond
               (get key-evs "ArrowUp") [x (- y 3)]
               (get key-evs "ArrowDown") [x (+ y 3)]
               (get key-evs "ArrowLeft") [(- x 3) y]
               (get key-evs "ArrowRight") [(+ x 3) y]
               :else [x y]))
   :on-tick (fn [state _] state)
   :to-draw (fn [[x y]]
              (let [img (image "https://media.giphy.com/media/NMr9UUZSqQbhS/giphy.gif")]
                {:background (image "https://i.pinimg.com/originals/e1/ff/53/e1ff53238b5263d0e6a963363e3a4ff0.jpg") 
                 :sprites [[img x y]]}))})

(start! game-handlers)
