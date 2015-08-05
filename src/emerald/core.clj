(ns emerald.core
  (:require [emerald.handler :refer [app init destroy parse-port]]
            [org.httpkit.server :as http-kit]
            [emerald.db.migrations :as migrations]
            [taoensso.timbre :as timbre]
            [emerald.cache :as cache]
            [emerald.db.migrations :as migrations]
            [environ.core :refer [env]])
  (:gen-class))

(defn http-port [port]
  (parse-port (or port (env :port) 3000)))

(defonce server (atom nil))

(defn start-server [port]
  (init)
  (reset! server
          (http-kit/run-server
            app
            {:port port})))

(defn stop-server []
  (when @server
    (destroy)
    (@server :timeout 100)
    (reset! server nil)))

(defn start-app [[port]]
  (let [port (http-port port)]
    (.addShutdownHook (Runtime/getRuntime) (Thread. stop-server))
    (timbre/info "server is starting on port " port)
    (start-server port)))

(defn -main [& args]
  (cond
   (some #{"migrate" "rollback"} args) (migrations/migrate args)
   :else (do
           (if (env :couchbase) (cache/init-connection))
           (migrations/init)
           (start-app args))))

