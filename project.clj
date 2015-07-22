(defproject emerald "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [selmer "0.8.5"] ;;templating language
                 [com.taoensso/timbre "4.0.2"] ;;logging
                 [com.taoensso/tower "3.0.2"] ;;localization
                 [markdown-clj "0.9.67"] ;;markdown parser
                 [environ "1.0.0"] ;;env variables
                 [compojure "1.4.0"] ;;Status dsl, POST, GET ect
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-session-timeout "0.1.0"]
                 [ring "1.4.0"
                  :exclusions [ring/ring-jetty-adapter]]
                 [prismatic/schema "0.4.3"] ;;api validation
                 [metosin/ring-middleware-format "0.6.0"]
                 [metosin/ring-http-response "0.6.3"]
                 [bouncer "0.3.3"] ;;validation dsl
                 [prone "0.8.2"] ;;better error reporting
                 [org.clojure/tools.nrepl "0.2.10"]
                 [metosin/compojure-api "0.22.0"]
                 [metosin/ring-swagger-ui "2.1.1-M2"]
                 [migratus "0.8.2"] ;;table migration
                 [org.clojure/java.jdbc "0.3.7"]
                 [com.mchange/c3p0 "0.9.5.1"] ;;connection pooling
                 [korma "0.4.2" :exclusions [c3p0/c3p0]] ;;sql dsl
                 [org.postgresql/postgresql "9.3-1102-jdbc41"] ;;postgres adapter
                 [http-kit "2.1.19"]] ;;server

  :min-lein-version "2.0.0"
  :uberjar-name "emerald.jar"
  :jvm-opts ["-server"]

  :main emerald.core
  :migratus {:store :database}

  :plugins [[lein-environ "1.0.0"]
            [lein-ancient "0.6.5"]
            [migratus-lein "0.1.5"]]
  :profiles
  {:uberjar {:omit-source true
             :env {:production true}
             :aot :all}
   :dev           [:project/dev :profiles/dev]
   :test          [:project/test :profiles/test]
   :project/dev  {:dependencies [[ring/ring-mock "0.2.0"]
                                 [ring/ring-devel "1.4.0"]
                                 [pjstadig/humane-test-output "0.7.0"]
                                 [mvxcvi/puget "0.8.1"]]


                  :repl-options {:init-ns emerald.core}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]
                  ;;when :nrepl-port is set the application starts the nREPL server on load
                  :env {:dev        true
                        :port       3000
                        :nrepl-port 7000}}
   :project/test {:env {:test       true
                        :port       3001
                        :nrepl-port 7001}}
   :profiles/dev {}
   :profiles/test {}})
