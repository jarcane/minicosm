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

(defn render! 
  "Given a canvas context and a DDN element, render the specified element to the canvas"
  [ctx ddn]
  (ddn-elem ctx ddn))