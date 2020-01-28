(ns braeburn.drawing)

(defn filled-rect 
  "Returns a command to draw a filled rectangle at the given point"
  [x y w h]
  [:fillRect x y w h])