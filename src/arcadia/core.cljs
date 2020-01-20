(ns braeburn.core)

(enable-console-print!)

(defn- draw! [ctx graphics]
  (when-let [bkg (:background graphics)]
    (.drawImage ctx bkg 0 0))
  (doseq [[spr x y] (:sprites graphics)]
    (.drawImage ctx spr x y))
  (doseq [[str x y] (:text graphics)]
    (.fillText ctx str x y)))

(defn- game-loop! [t ctx key-evs state {:keys [on-key on-tick to-draw] :as handlers}]
  (let [new-state (-> state
                      (on-key @key-evs)
                      (on-tick t))]
    (.clearRect ctx 0 0 512 384)
    (draw! ctx (to-draw new-state))
    (js/requestAnimationFrame (fn [t] (game-loop! t ctx key-evs new-state handlers)))))

(defn start! [{:keys [init] :as handlers}]
  (let [canvas (js/document.getElementById "game")
        ctx (.getContext canvas "2d")
        key-evs (atom {})
        init-state (init)]
    (set! (.-fillStyle ctx) "white")
    (set! (.-font ctx) "9pt ChicagoFLFRegular")
    (set! js/window.onkeyup (fn [e] (swap! key-evs assoc (.-code e) false)))
    (set! js/window.onkeydown (fn [e] (swap! key-evs assoc (.-code e) true)))
    (game-loop! 0 ctx key-evs init-state handlers)))
