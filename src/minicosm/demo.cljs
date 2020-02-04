(ns minicosm.demo
  (:require [minicosm.core :refer [start!]]
            [minicosm.image :refer [sprite8 sprite16 generate-background]]
            [minicosm.drawing :refer [filled-rect stroke-rect set-stroke set-fill stroke-circ]]))

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

(def background (generate-background 512 385 16 make-stars))

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
   :to-draw (fn [[x y]]
              {:background background
               :sprites [[sprite x y]]
               :text [["THIS IS A TEST" "white" 16 16]]
               :draw [(set-fill O)
                      (filled-rect 300 200 64 64)
                      (set-stroke O)
                      (stroke-rect 200 150 32 32)
                      (stroke-circ 400 50 32)]})})

(start! game-handlers)