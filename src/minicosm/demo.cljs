(ns minicosm.demo
  (:require [minicosm.core :refer [start!]]
            [minicosm.ddn :refer [render-to-canvas]]))

(enable-console-print!)

(defn make-stars []
  (render-to-canvas 32 32
   [:group {}
    [:rect {:pos [0 0] :dim [32 32] :color "black" :style :fill}]
    [:group {}
     (for [_ (range 4)]
       [:point {:pos [(rand-int 32) (rand-int 32)]
                :color (rand-nth ["white" "white" "white" "yellow" "PaleTurquoise" "orange"])}])]]))

(def tilemap
  (for [_ (range 12)]
    (for [_ (range 16)]
      (make-stars))))

(defn draw [[x y] assets]
  [:group {:desc "base"}
   [:map {:pos [0 0] :dim [16 12] :size 32} tilemap]
   [:sprite {:pos [x y]} (:ship assets)]
   [:text {:pos [32 32] :color "white" :font "16px serif"} "THIS IS A TEST"]
   [:group {:desc "shapes"}
    [:rect {:style :fill :pos [300 200] :dim [64 32] :color "white"}]
    [:rect {:pos [200 150] :dim [32 32] :color "white"}]
    [:circ {:pos [400 50] :r [32 32] :color "white"}]
    [:line {:from [200 64] :to [350 150] :color "white"}]
    [:point {:pos [45 100] :color "purple"}]
    [:path {:style :fill :color "green"}
     [[200 200]
      [250 200]
      [225 250]
      [175 250]
      [200 200]]]]])

(def game-handlers
  {:init (fn [] [128 128])
   :assets (fn [] {:ship [:image "/img/shuttle.png"]
                   :zap [:audio "/audio/zap.wav"]
                   :background [:audio "/audio/igluifohn.wav"]})
   :on-key (fn [[x y] key-evs]
             (cond
               (key-evs "ArrowUp") [x (- y 3)]
               (key-evs "ArrowDown") [x (+ y 3)]
               (key-evs "ArrowLeft") [(- x 3) y]
               (key-evs "ArrowRight") [(+ x 3) y]
               :else [x y]))
   :on-tick (fn [state _] state)
   :to-play (fn [_ assets is-playing] (if is-playing
                                        {}
                                        {:music (:background assets)}))
   :to-draw draw})

(start! game-handlers)