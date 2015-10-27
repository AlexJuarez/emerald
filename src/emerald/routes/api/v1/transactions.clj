(ns emerald.routes.api.v1.transactions
  (:use
   [emerald.util.core]
   [emerald.util.access])
  (:require
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]
   [emerald.models.transaction :as transaction]))

(defn restore [id]
  (transaction/restore! id))

(defroutes* transaction-routes
  (context* "/transactions/:id" []
            :tags ["transactions"]
            :middlewares [wrap-employee-access]
            :path-params [id :- (s/both java.util.UUID (s/pred transaction/exists? 'transaction/exists?))]
            (DELETE* "/" []
                     :summary "rollback a transaction, and remove the transaction entry"
                     (ok (restore id)))))
