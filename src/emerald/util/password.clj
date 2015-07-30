(ns emerald.util.password
  (:require
   [clojure.string :as s]))

(defonce legacy-hashing-method "SHA-1")

(defn legacy-encrypt [password]
  (let [message (java.security.MessageDigest/getInstance legacy-hashing-method)]
    (.update message (.getBytes password))
    (.encodeToString (java.util.Base64/getEncoder) (.digest message))))

(defn equals? [encryptedPassword password]
  (= encryptedPassword (legacy-encrypt password)))
