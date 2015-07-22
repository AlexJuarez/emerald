(ns emerald.util.core
  (:require
   [clojure.string :as s]))

(defn camel-case [m]
  (into {}
        (for [[k v] m]
          [(let [t (s/split (name k) #"_")]
             (apply str (first t) (map s/capitalize (rest t)))) v])))

(defn to-dash [m]
  (into {}
        (for [[k v] m]
          [(let [t (s/split (name k) #"(?=\p{Upper})")]
             (symbol (s/join "_" (map s/lower-case t)))) v])))
