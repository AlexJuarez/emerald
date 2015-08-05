(ns emerald.session
  (:refer-clojure :exclude [get set]))

(defonce mem (atom {}))

(def half-hour 1800000)

(defn- current-time []
  (quot (System/currentTimeMillis) 1000))

(defn- expired? [[id session]]
  (pos? (- (:ring.middleware.session-timeout/idle-timeout session) (current-time))))

(defn clear-expired-sessions []
  (clojure.core/swap! mem #(->> % (filter expired?) (into {}))))

(defn get [key]
  (clojure.core/get @mem key))

(defn set [key value]
  (swap! mem assoc key value))

(defn delete [key]
  (swap! mem dissoc key))

(defn start-cleanup-job! []
  (future
    (loop []
      (clear-expired-sessions)
      (Thread/sleep half-hour)
      (recur))))
