(defproject emerald "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [selmer "0.8.8"] ;;templating language
                 [com.taoensso/timbre "4.0.2"] ;;logging
                 [com.taoensso/tower "3.0.2"] ;;localization
                 [markdown-clj "0.9.67"] ;;markdown parser
                 [environ "1.0.0"] ;;env variables
                 [compojure "1.4.0"] ;;Status dsl, POST, GET ect
                 [ring-webjars "0.1.1"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-ttl-session "0.1.1"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [ring "1.4.0"
                  :exclusions [ring/ring-jetty-adapter]]
                 [metosin/ring-middleware-format "0.6.0"]
                 [metosin/ring-http-response "0.6.3"]
                 [bouncer "0.3.3"] ;;validation dsl
                 [prone "0.8.2"] ;;better error reporting
                 [org.clojure/tools.nrepl "0.2.10"]
                 [metosin/compojure-api "0.23.1"]
                 [metosin/ring-swagger-ui "2.1.1"]
                 [metosin/ring-swagger "0.21.0"]
                 [prismatic/schema "0.4.4"]
                 [migratus "0.8.2"] ;;table migration
                 [to-jdbc-uri "0.2.0"] ;;jdbc uri parser
                 [org.clojure/java.jdbc "0.3.7"]
                 [com.mchange/c3p0 "0.9.5.1"] ;;connection pooling
                 [korma "0.4.2" :exclusions [c3p0/c3p0]] ;;sql dsl
                 [org.postgresql/postgresql "9.3-1102-jdbc41"] ;;postgres adapter
                 [org.immutant/web "2.0.2"];;server
                 [crypto-random "1.2.0"] ;;crypto lib
                 [clojurewerkz/spyglass "1.1.0"];;couchbase interface
                 [buddy "0.6.1" :exclusions [org.clojure/tools.reader clj-time]]] ;;ring based authentication

  :min-lein-version "2.0.0"
  :uberjar-name "emerald.jar"
  :jvm-opts ["-server"]

  :main emerald.core
  :migratus {:store :database}

  :plugins [[lein-environ "1.0.0"]
            [lein-ancient "0.6.5"]
            [lein-ring "0.9.6"]
            [migratus-lein "0.1.5"]]
  :ring
  {:handler emerald.handler/app
   :init emerald.handler/init
   :destroy emerald.handler/destroy
   :uberwar-name "emerald.war"}
  :profiles
  {:uberjar       [:project/uberjar :profiles/prod]
   :dev           [:project/dev :profiles/dev]
   :test          [:project/test :profiles/test]
   :prod          [:project/prod :profiles/prod]
   :project/dev  {:dependencies [[ring/ring-mock "0.2.0"]
                                 [ring/ring-devel "1.4.0" :exclusions [org.clojure/tools.reader hiccup]]
                                 [pjstadig/humane-test-output "0.7.0"]
                                 [mvxcvi/puget "0.8.1"]]


                  :repl-options {:init-ns emerald.core}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]
                  ;;when :nrepl-port is set the application starts the nREPL server on load
                  :env {:dev        true
                        :port       4000
                        :nrepl-port 7000}}
   :project/test {:env {:test       true
                        :port       3001
                        :nrepl-port 7001}}
   :project/prod {}
   :project/uberjar {:omit-source true
                     :env {:production true}
                     :aot :all}})
