(ns minicosm.demo
  (:require [minicosm.core :refer [start!]]
            [minicosm.ddn :refer [render-to-canvas]]))

(enable-console-print!)

(defn make-stars []
  (render-to-canvas 16 16
   [:canvas {}
    [:rect {:pos [0 0] :dim [16 16] :color "black" :style :fill}]
    (concat [:group {}]
            (for [_ (range 3)]
              [:point {:pos [(rand-int 16) (rand-int 16)] 
                       :color (rand-nth ["white" "white" "white" "yellow" "PaleTurquoise" "orange"])}]))]))

(def tilemap
  (for [_ (range 24)]
    (for [_ (range 32)]
      (make-stars))))

(defn draw [[x y] assets]
  [:canvas {}
   #_ [:image {:pos [0 0]} (:space assets)]
   [:map {:pos [0 0] :dim [32 24] :size 16} tilemap]
   [:sprite {:pos [x y]} (:ship assets)]
   [:text {:pos [32 32] :color "white" :font "16px serif"} "THIS IS A TEST"]
   [:group {:desc "lines"}
    [:rect {:style :fill :pos [300 200] :dim [64 32] :color "white"}]
    [:rect {:pos [200 150] :dim [32 32] :color "white"}]
    [:circ {:pos [400 50] :r [32 32] :color "white"}]
    [:line {:from [200 64] :to [350 150] :color "white"}]
    [:point {:pos [45 100] :color "purple"}]]])

(def game-handlers
  {:init (fn [] [128 128])
   :assets (fn [] {:space "/img/starfield.gif"
                   :ship "/img/shuttle.png"})
   :on-key (fn [[x y] key-evs]
             (cond
               (key-evs "ArrowUp") [x (- y 3)]
               (key-evs "ArrowDown") [x (+ y 3)]
               (key-evs "ArrowLeft") [(- x 3) y]
               (key-evs "ArrowRight") [(+ x 3) y]
               :else [x y]))
   :on-tick (fn [state _] state)
   :to-draw draw})

(start! game-handlers)