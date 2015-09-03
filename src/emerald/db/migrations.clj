(ns emerald.db.migrations
  (:refer-clojure :exclude [update])
  (:use
   [korma.core]
   [emerald.db.core])
  (:require
   [emerald.db.core :refer [db]]
   [emerald.db.helpers :refer [to-pg-json]]
   [migratus.core :as migratus]
   [cheshire.core :as jr]
   [emerald.env :refer [env]]
   [to-jdbc-uri.core :refer [to-jdbc-uri]]))

(def migration-uri (env :jdbc-uri))

(defn map-keywords [m]
  (let [attributes (to-pg-json (get m "attributes"))]
    (assoc (into {} (map #(vector (keyword (key %)) (val %)) m))
    :attributes attributes
    )))

(defn migrate-adtags []
  (if
    (empty? (select adtags))
    (insert adtags
            (values (into [] (map map-keywords (jr/parse-string (slurp "resources/fixtures/adtags.json"))))))))

(defn init []
  (migrate-adtags))

(defn parse-ids [args]
  (map #(Long/parseLong %) (rest args)))

(defn migrate [args]
  (let [config {:store :database
                :db {:connection-uri (to-jdbc-uri migration-uri)}}]
    (case (first args)
      "migrate"
      (if (> (count args) 1)
        (apply migratus/up config (parse-ids args))
        (migratus/migrate config))
      "rollback"
      (if (> (count args) 1)
        (apply migratus/down config (parse-ids args))
        (migratus/rollback config)))))
