(ns emerald.util.model
  (:require [clojure.string :as s]))

(defn update-fields [item m]
  (if (contains? m (key item))
    [(key item) (into-array String (map s/trim (s/split (val item) #",")))]
    [(key item) (val item)]))
