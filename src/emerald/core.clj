(ns emerald.core
  (:require [emerald.handler :refer [app init destroy]]
            [immutant.web :as immutant]
            [emerald.db.migrations :as migrations]
            [taoensso.timbre :as log]
            [emerald.models.enums :as enums]
            [emerald.cache :as cache]
            [emerald.db.migrations :as migrations]
            [emerald.env :refer [env]]
            [clojure.tools.nrepl.server :as nrepl])
  (:gen-class))

(defonce nrepl-server (atom nil))

(defn parse-port [port]
  (when port
    (cond
      (string? port) (Integer/parseInt port)
      (number? port) port
      :else          (throw (Exception. (str "invalid port value: " port))))))

(defn stop-nrepl []
  (when-let [server @nrepl-server]
    (nrepl/stop-server server)))

(defn start-nrepl
  "Start a network repl for debugging when the :nrepl-port is set in the environment."
  []
  (if @nrepl-server
    (log/error "nREPL is already running!")
    (when-let [port (env :nrepl-port)]
      (try
        (->> port
             (parse-port)
             (nrepl/start-server :port)
             (reset! nrepl-server))
        (log/info "nREPL server started on port" port)
        (catch Throwable t
          (log/error "failed to start nREPL" t))))))

(defn http-port [port]
  (parse-port (or port (env :port) 3000)))

(defonce server (atom nil))

(defn start-http-server [port]
  (init)
  (reset! server
          (immutant/run app :port port)))

(defn stop-http-server []
  (when @server
    (destroy)
    (immutant/stop @server)
    (reset! server nil)))

(defn stop-app []
  (stop-nrepl)
  (stop-http-server))

(defn start-app [[port]]
  (let [port (http-port port)]
    (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app))
    (start-nrepl)
    (start-http-server (http-port port))
    (log/info "server is starting on port " (:port @server))))

(defn -main [& args]
  (cond
   (some #{"migrate" "rollback"} args) (migrations/migrate args)
   :else (do
           (if (env :dev) (migrations/init))
           (start-app args))))

