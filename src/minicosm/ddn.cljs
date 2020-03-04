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

(defn render! 
  "Given a canvas context and a DDN :canvas element, render the specified graphics to the canvas"
  [ctx ddn]
  (ddn-elem ctx ddn))