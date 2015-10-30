(ns emerald.env
  (:require [clojure.string :as strlib]
            [environ.core :as environ]
            [clojure.java.io :as io]
            [taoensso.timbre :as log]))

;;user.home is supposed to be equal to ~ for the path, change if needed
(defonce tomcat-config-path (str (System/getProperty "user.home") "/tomcat/conf/emerald.config"))
(defonce mixpo-identity-default-path (str (System/getProperty "user.home") "/mixpo_server.identity"))
(defonce mixpo-identity-system-path (str "" (System/getenv "MIXPO_IDENTITY")))

(defn- keywordize [s]
  (-> (strlib/lower-case s)
      (strlib/replace "_" "-")
      (strlib/replace "." "-")
      (keyword)))

(defn- sanitize [k]
  (let [s (keywordize (name k))]
    (if-not
      (= k s)
      (log/debug "enviroment key" k "has been corrected to" s))
    s))

(defn- convert-line [line delimiter]
  (when (re-find delimiter line)
    (let [[k v] (strlib/split line delimiter)]
      [(sanitize k) v])))

;;if the line has a leading # ignore that line, also only include lines with =
(defn- read-from-identity-file [s]
  (let [lines (->> (strlib/split-lines s)
                   (filter #(not (= "#" (first (strlib/trim %))))))]
    (into {} (map #(convert-line (strlib/trim %) #"=") lines))))

(defn- read-identity-file [path]
  (let [mixpo-identity (io/file path)]
    (log/debug "looking for file at" mixpo-identity)
    (if
      (.exists mixpo-identity)
      (do
        (log/info "loaded .identity at" path)
        (read-from-identity-file (slurp mixpo-identity)))
      (log/debug "could not find mixpo_server.identity at" path))))

(defn- read-tomcat-file []
  (let [emerald-config (io/file tomcat-config-path)]
    (log/debug "looking for file at" tomcat-config-path)
    (if (.exists emerald-config)
      (do (log/info "loaded tomcat config file for emerald")
      (into {} (for [[k v] (read-string (slurp emerald-config))]
                 [(sanitize k) v]))))))

(defonce ^{:doc "A map of enviroment variables including external configuration"}
  env
  (merge
   environ/env
   (read-tomcat-file)
   (read-identity-file mixpo-identity-default-path)
   (read-identity-file mixpo-identity-system-path)))
