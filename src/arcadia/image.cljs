(ns braeburn.image)

(def _ [0 0 0 0])
(def O [255 255 255 255])
(def X [0 0 0 255])

(defn- to-image-data [arr width]
  (let [uint8 (js/Uint8ClampedArray. (flatten arr))]
    (js/ImageData. uint8 width)))

(defn- image-data->temp-canvas [img-data width]
  (let [cvs (js/document.createElement "canvas")
        ctx (.getContext cvs "2d")]
    (.putImageData ctx img-data 0 0)
    cvs))

(defn- make-sprite [arr width]
  (-> (to-image-data arr width)
      (image-data->temp-canvas width)))

(defn sprite8 [arr]
  (make-sprite arr 8))

(defn image [url]
  (let [img (js/Image.)]
    (set! (.-src img) url)
    img))