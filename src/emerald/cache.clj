(ns emerald.cache
  (:refer-clojure :exclude [set get namespace])
  (:use [taoensso.timbre :only [trace debug info warn error fatal]])
  (:require [clojurewerkz.spyglass.client :as c]
            [emerald.env :refer [env]]
            [emerald.session :as mem]
            [taoensso.timbre :as timbre]
            [ring.middleware.session.store :as session-store]))

(def ^:private address (env :couchbase-server-uri))

(defonce ce (atom nil))

(defn create-connection [address]
  (if-not (empty? address)
    (try
      (c/text-connection address)
      (catch Exception e
        (timbre/error "Error creating couchbase connection" e)
        nil))))

(defn init-connection []
  (when (nil? @ce)
    (info "Starting couchbase connection")
    (reset! ce (create-connection address))))

(defn shutdown-connection []
  (when-not (nil? @ce)
    (info "Shutting down couchbase connection")
    (c/shutdown @ce)))

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
  (if-not (nil? @ce)
    (c/set (get-connection) key (or (first ttl) (+ (* 60 10) (rand-int 600))) value)
    (mem/set key value)
    ));;Prevent stampede

(defn get [key]
  (if-not (nil? @ce)
    (c/get (get-connection) key)
    (mem/get key)))

(defn delete [key]
  (if-not (nil? @ce)
    (c/delete (get-connection) key)
    (mem/delete key)))

(defmacro cache! [key & forms]
  (let [value# (get ~key)]
    (if (nil? value#)
      (let [v# (do ~@forms)]
        (set ~key v#)
        v#)
      value#)))
