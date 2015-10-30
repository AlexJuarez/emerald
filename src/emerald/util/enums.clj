(ns emerald.util.enums
  (:require
   [korma.core :as korma])
  (:use emerald.models.enums
        emerald.db.helpers))

(defn string->enum
  ([s]
   (let [v (keyword s)
         type (get-enum-type v)]
     (emerald.db.protocols.KormaEnum. v type)))
  ([s type]
     (let [v (keyword s)
           type (keyword type)]
       (emerald.db.protocols.KormaEnum. v type))))

(defn string->enum-sql [s]
  (korma/raw (str "'" s "'")))
