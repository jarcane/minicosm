(ns minicosm.demo
  (:require [minicosm.core :refer [start!]]
            [minicosm.image :refer [sprite8 sprite16]]))

(enable-console-print!)

(def _ 0x00000000)
(def O 0xFFFFFFFF)
(def X 0xFF000000)

(def sprite
  (sprite8 [_ _ _ O O _ _ _
            _ _ O X X O _ _
            _ _ O X X O _ _
            _ O X O O X O _
            _ O X X X X O _
            _ O X X X X O _
            O X X O O X X O
            O X O _ _ O X O]))

(defn make-stars []
  (sprite16 
   (for [_ (range 256)]
     (if (< (rand) 0.005)
       O
       X))))

(def tilemap 
  (for [y (range 24)]
    (for [x (range 32)]
      (make-stars))))

(defn draw [[x y]]
  [:canvas {}
   [:map {:pos [0 0] :dim [32 24] :size 16} tilemap]
   [:sprite {:pos [x y]} sprite]
   [:text {:pos [32 32] :color "white" :font "16px serif"} "THIS IS A TEST"]
   [:group {:desc "lines"}
    [:rect {:style :fill :pos [300 200] :dim [64 32] :color "white"}]
    [:rect {:pos [200 150] :dim [32 32] :color "white"}]
    [:circ {:pos [400 50] :r [32 32] :color "white"}]
    [:line {:from [200 64] :to [350 150] :color "white"}]]])

(def game-handlers
  {:init (fn [] [128 128])
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