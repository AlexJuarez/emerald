{:profiles/dev {:env
                {:dbspec {:subprotocol "postgresql"
                          :subname "//localhost/"
                          :user "bowser"
                          :password "koopa"
                          :test-connection-query true}
                 :jdbc-uri "jdbc:postgresql://localhost/bowser?user=bowser&password=koopa"
                 :couchbase true
                 :auth true
                 :log-path "emerald.log"
                 }}
 :profiles/prod {:env
                 {:dbspec {:subprotocol "postgresql"
                          :subname "//localhost/"
                          :user "bowser"
                          :password "koopa"
                          :test-connection-query true}
                 :jdbc-uri "jdbc:postgresql://localhost/bowser?user=bowser&password=koopa"
                 :couchbase false
                 :auth false
                 :log-path "../../logs/emerald.log"
                 }}}
