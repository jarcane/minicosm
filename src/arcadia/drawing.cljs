(ns braeburn.drawing)

(defn draw-commands! [ctx cmds]
  (doseq [[mtd & args] cmds]
    (case mtd
      :fillRect (let [[x y w h] args] (.fillRect ctx x y w h))
      :strokeRect (let [[x y w h] args] (.strokeRect ctx x y w h))
      )))

(defn filled-rect 
  "Returns a command to draw a filled rectangle at the given point and size"
  [x y w h]
  [:fillRect x y w h])

(defn stroke-rect
  "Returns a command to draw a stroked (outline only) rectangle at the given point and size"
  [x y w h]
  [:strokeRect x y w h])