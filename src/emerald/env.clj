(ns emerald.env
  (:require [clojure.string :as strlib]
            [environ.core :as environ]
            [clojure.java.io :as io]
            [taoensso.timbre :as timbre]))

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
      (timbre/debug "enviroment key" k "has been corrected to" s))
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
    (timbre/debug "looking for file at" mixpo-identity)
    (if
      (.exists mixpo-identity)
      (read-from-identity-file (slurp mixpo-identity))
      (timbre/info "could not find mixpo_server.identity at" path))))

(defn- read-tomcat-file []
  (let [emerald-config (io/file tomcat-config-path)]
    (timbre/debug "looking for file at" tomcat-config-path)
    (if (.exists emerald-config)
      (do (timbre/info "loaded tomcat config file for emerald")
      (into {} (for [[k v] (read-string (slurp emerald-config))]
                 [(sanitize k) v]))))))

(defonce ^{:doc "A map of enviroment variables including external configuration"}
  env
  (merge
   (read-tomcat-file)
   (read-identity-file mixpo-identity-default-path)
   (read-identity-file mixpo-identity-system-path)
   environ/env))
