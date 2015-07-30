(ns emerald.cache
  (:refer-clojure :exclude [set get namespace])
  (:require [clojurewerkz.spyglass.client :as c]
            [ring.middleware.session.store :as session-store]
            [clauth.store :refer [Store]]
            [taoensso.timbre :as timbre]))

(def ^:private address "127.0.0.1:11211")

(defonce ce (atom nil))

(defn init []
  (try
    (reset! ce (c/text-connection address))
    (catch Exception e
      (timbre/error e))))

(defrecord CouchBaseSessionStore [namespace conn ttl-secs]
  session-store/SessionStore
  (read-session [_ key] (or (when key (c/get conn (str namespace key))) {}))
  (delete-session [_ key] (c/delete conn (str namespace key)) nil)
  (write-session [_ key data]
    (let [key (or key (str (java.util.UUID/randomUUID)))]
      (c/set conn (str namespace key) (+ ttl-secs (rand-int ttl-secs)) data)
      key)))

(defn create-couchbase-session-store
  ([]
   (create-couchbase-session-store "session:"))
  ([namespace]
   (create-couchbase-session-store namespace @ce))
  ([namespace connection]
   (->CouchBaseSessionStore namespace connection (* 60 60 10))))

(defn set [key value & ttl]
  (c/set @ce key (or (first ttl) (+ (* 60 10) (rand-int 600))) value));;Prevent stampede

(defn get [key]
  (c/get @ce key))

(defn delete [key]
  (c/delete @ce key))

(defmacro cache! [key & forms]
  (let [value# (get ~key)]
    (if (nil? value#)
      (let [v# (do ~@forms)]
        (set ~key v#)
        v#)
      value#)))
