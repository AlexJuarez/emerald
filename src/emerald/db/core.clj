(ns emerald.db.core
  (:refer-clojure :exclude [update])
  (:use [korma.core]
        [emerald.util.core])
  (:require [clojure.java.jdbc :as jdbc]
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

(defonce dbspec {:subprotocol "postgresql"
                 :subname "//localhost/"
                 :user "bowser"
                 :password "koopa"
                 :test-connection-query true})

(defdb db dbspec)

(declare
  adtags
  placements
  publishers
  creatives
  clients
  channels
  accounts
  industries
  campaigns
  creatives
  divisions
  accounts
  geo-profiles
  applications
  users
  client-pins
  division-pins
  account-pins
  campaign-pins)

(defentity users
  (belongs-to clients {:fk :client_id})
  (table :mixpo.users :users))

(defentity clients
  (prepare to-dash)
  (transform camel-case)
  (belongs-to channels {:fk :channel_id})
  (has-many geo-profiles {:fk :client_id})
  (has-many publishers {:fk :client_id})
  (table :mixpo.clients :client))

(defentity accounts
  (prepare to-dash)
  (transform camel-case)
  (belongs-to divisions {:fk :division_id})
  (belongs-to industries {:fk :industry_id})
  (table :mixpo.accounts :account))

(defentity industries
  (prepare to-dash)
  (transform camel-case)
  (table :mixpo.industries :industries))

(defentity campaigns
  (prepare to-dash)
  (transform camel-case)
  (belongs-to accounts {:fk :account_id})
  (table :mixpo.campaigns :campaigns))

(defentity divisions
  (prepare to-dash)
  (transform camel-case)
  (table :mixpo.divisions :divisions))

(defentity channels
  (prepare to-dash)
  (transform camel-case)
  (table :mixpo.channels :channel))

(defentity placements
  (prepare to-dash)
  (transform camel-case)
  (belongs-to publishers {:fk :publisher_id})
  (many-to-many creatives :mixpo.targets {:lfk :placement_id :rfk :creative_id})
  (table :mixpo.placements :placement))

(defentity publishers
  (prepare to-dash)
  (transform camel-case)
  (belongs-to clients {:fk :client_id})
  (table :mixpo.publishers :publisher))

(defentity creatives
  (prepare to-dash)
  (transform camel-case)
  (table :mixpo.creatives :creative))

(defentity geo-profiles
  (prepare to-dash)
  (transform camel-case)
  (belongs-to clients {:fk :client_id})
  (table :mixpo.geo_profiles :geo_profiles))

(defentity applications
  (prepare to-dash)
  (transform camel-case)
  (belongs-to users {:fk :user_id})
  (table :mixpo.applications :applications))

(defentity client-pins
  (transform camel-case)
  (table :mixpo.user_client_pins))

(defentity division-pins
  (transform camel-case)
  (table :mixpo.user_division_pins))

(defentity account-pins
  (transform camel-case)
  (table :mixpo.user_account_pins))

(defentity campaign-pins
  (transform camel-case)
  (table :mixpo.user_campaign_pins))

(defentity adtags
  (transform camel-case)
  (table :mixpo.adtags))

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
    (let [type (.getType pgobj)
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

(extend-protocol jdbc/ISQLParameter
  (Class/forName "[Ljava.lang.String;")
  (set-parameter [v ^PreparedStatement stmt ^long i]
    (let [conn (.getConnection stmt)
          meta (.getParameterMetaData stmt)
          type-name (.getParameterTypeName meta i)]
      (if-let [elem-type (when (= (first type-name) \_) (apply str (rest type-name)))]
        (.setObject stmt i (.createArrayOf conn elem-type v))
        (.setObject stmt i v)))))
