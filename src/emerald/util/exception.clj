(ns emerald.util.exception
  (:require
   [ring.util.http-response :refer [bad-request internal-server-error]]
   [clojure.walk :refer [postwalk]]
   [clojure.core.match :refer [match]]
   [taoensso.timbre :as log]
   [schema.utils :as su])
  (:import [schema.utils ValidationError NamedError]))

(defn humanize [x]
  (match
   x
   ['not ['instance? expected value]]
   (str "the value could not be coerced to " expected)
   ['not ['client/exists? value]]
   "this client does not exist"
   ['not ['industry/exists? value]]
   "this industry does not exist"
   ['not ['division/exists? value]]
   "this division does not exist"
   ['not ['division-access? value]]
   "you do not have access to this division"
   ['not ['account/exists? value]]
   "this account does not exist"
   ['not ['account-access? value]]
   "you do not have access to this account"
   ['not ['channel/exists? value]]
   "this channel does not exist"
   ['not ['client/unique-name? value]]
   "a client with this name already exists"
   ['not ['campaign/exists? value]]
   "this campaign does not exists"
   ['not ['campaign-access? value]]
   "you do not have access to this campaign"
   ['not ['client-access? value]]
   "you do not have access to this client"
   ['not ['publisher/exists? value]]
   "this publisher does not exist"
   :else
   (str x)))

(defn vectorize
  [m]
  (postwalk
   (fn [x]
     (cond
      (seq? x) (vec x)
      :else x))
   m))

(defn transform [message]
  (cond
    (= (symbol "missing-required-key") message) (symbol "is required")
    :else message))

(defn stringify-error
  "Stringifies symbols and validation errors in Schema error, keeping the structure intact."
  [error]
  (postwalk
    (fn [x]
      (cond
        (instance? ValidationError x) (humanize (vectorize (su/validation-error-explain x)))
        (instance? NamedError x) (humanize (vectorize (su/named-error-explain x)))
        :else (transform x)))
    error))

(defn request-validation-handler
  "Creates error response based on Schema error."
  [_ data _]
  (bad-request {:errors (stringify-error (su/error-val data))}))

(defn default-handler [^Exception e _ _]
  (let [message (.getMessage e)
        error-code (if (string? message) (.hashCode message) "unknown")]
    (log/error error-code e)
    (log/trace e)
    (internal-server-error {:type "Server Error"
                            :error_code error-code
                            :message "Our highly trained operatives are working on it"})))
