(ns emerald.middleware
  (:require [emerald.session :as session]
            [emerald.cache :as cache]
            [emerald.layout :refer [*servlet-context* error-page]]
            [taoensso.timbre :as timbre]
            [emerald.env :refer [env]]
            [clojure.java.io :as io]
            [emerald.models.user :as user]
            [emerald.util.session :as sess :refer [wrap-session]]
            [selmer.middleware :refer [wrap-error-page]]
            [prone.middleware :refer [wrap-exceptions]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.auth.accessrules :refer [restrict]]
            [ring.util.http-response :refer [internal-server-error]]
            [ring.util.response :refer [redirect]]
            [ring.middleware.reload :as reload]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring-ttl-session.core :refer [ttl-memory-store]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.format :refer [wrap-restful-format]]))

(defn wrap-servlet-context [handler]
  (fn [request]
    (binding [*servlet-context*
              (if-let [context (:servlet-context request)]
                ;; If we're not inside a servlet environment
                ;; (for example when using mock requests), then
                ;; .getContextPath might not exist
                (try (.getContextPath context)
                     (catch IllegalArgumentException _ context)))]
      (handler request))))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (timbre/error t)
        (internal-server-error
          (error-page {:code 500
                       :title "Something very bad has happened!"
                       :message "We've dispatched a team of highly trained gnomes to take care of the problem."}))))))

(defn wrap-dev [handler]
  (if (env :dev)
    (-> handler
        reload/wrap-reload
        wrap-error-page
        wrap-exceptions)
    handler))

(defn wrap-csrf [handler]
  (wrap-anti-forgery handler))

(defn wrap-formats [handler]
  (wrap-restful-format handler :formats [:json-kw :transit-json :transit-msgpack]))

(defn authenticated? [request]
  (let [t (or (get (:headers request) "authorization")
              (get (:query-params request) "api_key")
              (get-in (:cookies request) ["access_token" :value]))
        token (cache/get (str "oauth:" t))
        user-id (or (get token "user_id") (get token :user_id))]
    (sess/put! :user_id user-id)
    (when (empty? (sess/get :user))
      (sess/put! :user (user/get user-id)))
    (or (not (env :auth)) (not (nil? token)));;todo remove this flag, this disables auth
    ))

(defn on-error [request response]
  {:status  403
   :headers {"Content-Type" "text/plain"}
   :body    (str "Access to " (:uri request) " is not authorized")})

(defn wrap-api-restricted [handler]
  (restrict handler {:handler authenticated?
                     :on-error on-error}))

(defn wrap-base [handler]
  (-> handler
      wrap-dev
      wrap-formats
      wrap-webjars
      wrap-session
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (assoc-in  [:session :store] (if (env :couchbase)
                                           (cache/create-couchbase-session-store)
                                           (ttl-memory-store (* 60 30))))))
      wrap-servlet-context
      wrap-internal-error))
