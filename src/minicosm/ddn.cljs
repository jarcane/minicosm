(ns minicosm.ddn)

(defmulti ddn-elem
  "This multimethod handles the rendering of individual DDN elements, dispatching by key"
  (fn [_ [k & _]] k))

(defmethod ddn-elem :default invalid-elem [_ [k & _]]
  (throw js/Error. (str "Unrecognized elem: " k)))

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

(defn render! 
  "Given a canvas context and a DDN :canvas element, render the specified graphics to the canvas"
  [ctx ddn]
  (ddn-elem ctx ddn))