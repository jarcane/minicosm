(ns minicosm.drawing)

(defn draw-commands! [ctx cmds]
  (doseq [[mtd & args] cmds]
    (case mtd
      :fillRect (let [[x y w h] args] (.fillRect ctx x y w h))
      :strokeRect (let [[x y w h] args] (.strokeRect ctx x y w h))
      :setFill (set! (.-fillStyle ctx) (first args))
      :setStroke (set! (.-strokeStyle ctx) (first args))
      :strokeCirc (let [[x y r] args
                        pi js/Math.PI]
                    (.beginPath ctx)
                    (.ellipse ctx x y r r (/ pi 4) 0 (* 2 pi))
                    (.stroke ctx)))))

(defn- pad-hex [num]
  (let [as-hex (.toString num 16)]
    (if (= 2 (count as-hex))
      as-hex
      (str "0" as-hex))))

(defn- color->rgba-string [color]
  (let [r (bit-and (bit-shift-right color 16) 0xff)
        g (bit-and (bit-shift-right color 8) 0xff)
        b (bit-and color 0xff)
        a (bit-and (bit-shift-right color 24) 0xff)
        to-str (str "#" (pad-hex r) (pad-hex g) (pad-hex b) (pad-hex a))]
    to-str))

(defn set-fill
  "Returns a command to set the fill color"
  [color]
  [:setFill (color->rgba-string color)])

(defn set-stroke
  "Returns a command to set the stroke color"
  [color]
  [:setStroke (color->rgba-string color)])

(defn filled-rect 
  "Returns a command to draw a filled rectangle at the given point and size"
  [x y w h]
  [:fillRect (+ 0.5 x) (+ 0.5 y) w h])

(defn stroke-rect
  "Returns a command to draw a stroked (outline only) rectangle at the given point and size"
  [x y w h]
  [:strokeRect (+ 0.5 x) (+ 0.5 y) w h])

(defn stroke-circ
  [x y r]
  [:strokeCirc (+ 0.5 x) (+ 0.5 y) r])