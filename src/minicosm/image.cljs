(ns minicosm.image)

(defn- to-image-data [arr width]
  (let [as32 (js/Uint32Array.from arr)
        buff (.-buffer as32)
        uint8 (js/Uint8ClampedArray. buff)]
    (js/ImageData. uint8 width)))

(defn- image-data->temp-canvas [img-data]
  (let [cvs (js/document.createElement "canvas")
        ctx (.getContext cvs "2d")]
    (.putImageData ctx img-data 0 0)
    cvs))

(defn- make-sprite [arr width]
  (-> (to-image-data arr width)
      (image-data->temp-canvas)))

(defn sprite8
  "Given an array of color values of length 64, returns an 8x8 sprite"
  [arr]
  (make-sprite arr 8))

(defn sprite16
  "Given an array of 256 color values, returns a 16x16 sprite"
  [arr]
  (make-sprite arr 16))
