(ns braeburn.image)

(defn image [url]
  (let [img (js/Image.)]
    (set! (.-src img) url)
    img))