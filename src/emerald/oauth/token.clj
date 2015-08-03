(ns emerald.oauth.token
  (:require [crypto.random :as random]))

(defn generate-token []
  "generates a unique token"
  (random/base32 20))

