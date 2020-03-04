(ns minicosm.ddn)

(defmulti ddn-elem
  "This multimethod handles the rendering of individual DDN elements, dispatching by key"
  (fn [_ [k & _]] k))

(defmethod ddn-elem :default invalid-elem [_ [k & _]]
  (throw (js/Error. (str "Unrecognized elem: " k))))

(defmethod ddn-elem :canvas canvas [ctx [_ opts & elems]]
  (doseq [e elems]
    (ddn-elem ctx e)))

(defmethod ddn-elem :group group [ctx [_ opts & elems]]
  (doseq [e elems]
    (ddn-elem ctx e)))

(defmethod ddn-elem :image image [ctx [_ {:keys [pos view] :or {pos [0 0]}} img]]
  (let [[x y] pos]
    (cond
      view (let [[[x1 y1] [x2 y2]] view
                 sw (- x2 x1)
                 sh (- y2 y1)]
             (.drawImage ctx img x1 y1 sw sh x y))
      :else (.drawImage ctx img x y))))

(defmethod ddn-elem :map map [ctx [_ {:keys [pos dim size view] :or {pos [0 0]}} map]]
  (let [[x y] pos
        [tw th] dim
        w (* tw size)
        h (* th size)
        cvs (js/document.createElement "canvas")
        tctx (.getContext cvs "2d")
        tiles (to-array-2d map)]
    (set! (.. tctx -canvas -width) w)
    (set! (.. tctx -canvas -height) h)
    (doseq [x (range 0 tw)
            y (range 0 th)]
      (let [tile (aget tiles y x)]
        (.drawImage tctx tile (* x size) (* y size))))
    (cond
      view (let [[[x1 y1] [x2 y2]] view
                 sw (- x2 x1)
                 sh (- y2 y1)]
             (.drawImage ctx cvs x1 y1 sw sh x y))
      :else (.drawImage ctx cvs x y))))

(defmethod ddn-elem :sprite sprite [ctx [_ {[x y] :pos} sprite]]
  (.drawImage ctx sprite x y))

(defmethod ddn-elem :text text [ctx [_ {:keys [pos color font]} & strings]]
  (let [[x y] pos
        old-color (.-fillStyle ctx)
        old-font (.-font ctx)]
    (set! (.-textBaseline ctx) "top")
    (when color (set! (.-fillStyle ctx) color))
    (when font (set! (.-font ctx) font))
    (.fillText ctx (apply str strings) x y)
    (when color (set! (.-fillStyle ctx) old-color))
    (when font (set! (.-font ctx) old-font))))

(defmethod ddn-elem :rect rect [ctx [_ {:keys [pos dim style color] :or {style :stroke}}]]
  (let [[x y] pos
        [w h] dim]
    (case style
      :fill (let [old-color (.-fillStyle ctx)]
              (when color (set! (.-fillStyle ctx) color))
              (.fillRect ctx (+ 0.5 x) (+ 0.5 y) w h)
              (when color (set! (.-fillStyle ctx) old-color)))
      :stroke (let [old-color (.-strokeStyle ctx)]
                (when color (set! (.-strokeStyle ctx) color))
                (.strokeRect ctx (+ 0.5 x) (+ 0.5 y) w h)
                (when color (set! (.-strokeStyle ctx) old-color))))))

(defmethod ddn-elem :circ circ [ctx [_ {:keys [pos r style color] :or {style :stroke}}]]
  (let [[x y] pos
        [rx ry] r
        pi js/Math.PI]
    (.beginPath ctx)
    (case style 
      :stroke (let [old-color (.-strokeStyle ctx)]
                (when color (set! (.-strokeStyle ctx) color))
                (.ellipse ctx x y rx ry (/ pi 4) 0 (* 2 pi))
                (.stroke ctx)
                (when color (set! (.-strokeStyle ctx) old-color)))
      :fill (let [old-color (.-fillStyle ctx)]
              (when color (set! (.-fillStyle ctx) color))
              (.ellipse ctx x y rx ry (/ pi 4) 0 (* 2 pi))
              (.fill ctx)
              (when color (set! (.-fillStyle ctx) old-color))))
    (.closePath ctx)))

(defn render! 
  "Given a canvas context and a DDN element, render the specified element to the canvas"
  [ctx ddn]
  (ddn-elem ctx ddn))