(ns emerald.handler
  (:require [compojure.core :refer [defroutes routes wrap-routes]]
            [emerald.routes.home :refer [home-routes]]
            [emerald.routes.api.v1.core :refer [api-routes]]
            [emerald.layout :refer [error-page]]
            [emerald.middleware :as middleware]
            [emerald.cache :as cache]
            [emerald.models.enums :as enums]
            [emerald.db.core :as db]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.3rd-party.rotor :as rotor]
            [selmer.parser :as parser]
            [environ.core :refer [env]]
            [clojure.tools.nrepl.server :as nrepl]))

(defroutes base-routes
           (route/resources "/")
           (route/not-found "Not Found"))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []

  (timbre/merge-config!
    {:level     (if (env :dev) :trace :info)
     :appenders {:rotor (rotor/rotor-appender
                          {:path (env :log-path)
                           :max-size (* 512 1024)
                           :backlog 10})}})

  (if (env :dev) (parser/cache-off!))
  (timbre/info (str
                 "\n-=[emerald started successfully"
                 (when (env :dev) " using the development profile")
                 "]=-")))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "emerald is shutting down...")
  (cache/shutdown-connection)
  (timbre/info "shutdown complete!"))

(def app-base
  (routes
   (wrap-routes #'home-routes middleware/wrap-csrf)
   (var api-routes)
   #'base-routes
   (route/not-found
    (error-page {:code 404
                 :title "page not found"}))))

(def app (middleware/wrap-base #'app-base))
