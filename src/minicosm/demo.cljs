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

(defn draw [state assets]
  [:group {:desc "base"}
   [:map {:pos [0 0] :dim [16 12] :size 32} tilemap]
   [:text {:pos [32 32] :color "white" :font "16px serif"} "THIS IS A TEST"]
   [:group {:desc "shapes"}
    (when false [:text {:pos [32 64] :color "white"} "NULL SAFE"])
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
      [200 200]]]]
   [:sprite {:pos [(:x state) (:y state)]} (:ship assets)]])

(def game-handlers
  {:init (fn [] {:x 128 :y 128 :moving false})
   :assets (fn [] {:ship [:image "/img/shuttle.png"]
                   :zap [:audio "/audio/zap.wav"]
                   :hover [:audio "/audio/hover.wav"]
                   :background [:audio "/audio/igluifohn.wav"]})
   :on-key (fn [state key-evs]
             (cond-> state
               true (assoc :moving
                           (pos? (count
                               (clojure.set/intersection
                                 #{"ArrowUp" "ArrowDown" "ArrowLeft" "ArrowRight"}
                                 key-evs))))
               (key-evs "ArrowUp") (update :y dec)
               (key-evs "ArrowDown") (update :y inc)
               (key-evs "ArrowLeft") (update :x dec)
               (key-evs "ArrowRight") (update :x inc)
               :else identity))
   :on-tick (fn [state _] state)
   :to-play (fn [state assets sounds]
              {:music #{(:background assets)}
               :effects (if
                          (:moving state)
                          #{(:hover assets)} 
                          #{})})
   :to-draw draw})

(start! game-handlers)
