(ns emerald.db.core
  (:refer-clojure :exclude [update])
  (:use
   [korma.core])
  (:require
    [clojure.java.jdbc :as jdbc]
    [korma.db :refer [defdb]]
    [korma.core :refer :all]
    [cheshire.core :refer [generate-string parse-string]]
    [taoensso.timbre :as timbre]
    [clojure.xml :as xml]
    [environ.core :refer [env]])
  (:import org.postgresql.util.PGobject
           org.postgresql.jdbc4.Jdbc4Array
           clojure.lang.IPersistentMap
           clojure.lang.IPersistentVector
           [java.sql BatchUpdateException
            Date
            Timestamp
            PreparedStatement]))

(defdb db (env :db-spec))

(declare
 placements
 publishers
 creatives)

(defentity placements
  (belongs-to publishers {:fk :publisher_id})
  (many-to-many creatives :narwhal.targets {:lfk :placement_id :rfk :creative_id})
  (table :narwhal.placements))

(defentity publishers
  (table :narwhal.publishers))

(defentity creatives
  (table :narwhal.creatives))

(defn to-date [sql-date]
  (-> sql-date (.getTime) (java.util.Date.)))

(extend-protocol jdbc/IResultSetReadColumn
  Date
  (result-set-read-column [v _ _] (to-date v))

  Timestamp
  (result-set-read-column [v _ _] (to-date v))

  Jdbc4Array
  (result-set-read-column [v _ _] (vec (.getArray v)))

  java.sql.SQLXML
  (result-set-read-column [v _ _] (xml/parse (.getBinaryStream v)))

  PGobject
  (result-set-read-column [pgobj _metadata _index]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "json" (parse-string value true)
        "jsonb" (parse-string value true)
        "citext" (str value)
        value))))

(extend-type java.util.Date
  jdbc/ISQLParameter
  (set-parameter [v ^PreparedStatement stmt idx]
    (.setTimestamp stmt idx (Timestamp. (.getTime v)))))

(defn to-pg-json [value]
  (doto (PGobject.)
    (.setType "jsonb")
    (.setValue (generate-string value))))

(extend-protocol jdbc/ISQLValue
  IPersistentMap
  (sql-value [value] (to-pg-json value))
  IPersistentVector
  (sql-value [value] (to-pg-json value)))
