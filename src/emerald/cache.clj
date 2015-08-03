(ns emerald.cache
  (:refer-clojure :exclude [set get namespace])
  (:require [clojurewerkz.spyglass.client :as c]
            [ring.middleware.session.store :as session-store]
            [taoensso.timbre :as timbre]))

(def ^:private address "127.0.0.1:11211")

(defonce ce (atom nil))

(defn init-connection []
  (if (nil? @ce)
    (reset! ce (c/text-connection address))))

(defn get-connection []
  (cast net.spy.memcached.MemcachedClient @ce))

(defrecord CouchBaseSessionStore [namespace ttl-secs]
  session-store/SessionStore
  (read-session [_ key] (or (when key (c/get (get-connection) (str namespace key))) {}))
  (delete-session [_ key] (c/delete (get-connection) (str namespace key)) nil)
  (write-session [_ key data]
    (let [key (or key (str (java.util.UUID/randomUUID)))]
      (c/set (get-connection) (str namespace key) ttl-secs data)
      key)))

(defn create-couchbase-session-store
  ([]
   (create-couchbase-session-store "session:"))
  ([namespace]
   (->CouchBaseSessionStore namespace (* 60 60 10))))

(defn set [key value & ttl]
  (c/set (get-connection) key (or (first ttl) (+ (* 60 10) (rand-int 600))) value));;Prevent stampede

(defn get [key]
  (c/get (get-connection) key))

(defn delete [key]
  (c/delete (get-connection) key))

(defmacro cache! [key & forms]
  (let [value# (get ~key)]
    (if (nil? value#)
      (let [v# (do ~@forms)]
        (set ~key v#)
        v#)
      value#)))
