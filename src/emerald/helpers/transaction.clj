(ns emerald.helpers.transaction
  (:require
   [emerald.models.client :as client]
   [emerald.models.division :as division]
   [emerald.models.account :as account]
   [emerald.models.campaign :as campaign]
   [emerald.models.placement :as placement]
   [emerald.models.creative :as creative]))

(defn create-ids-vec [ids]
  (if (sequential? ids)
    (apply conj [] ids)
    (conj [] ids)))

(defmacro children-by [id pk key childrenfn childbyfn]
  `(let [ids# (create-ids-vec ~id)
         children# (~childrenfn ids#)]
     (merge {(keyword ~pk) ids#} children# (~childbyfn (get children# ~key)))))

(defn children-by-placement [placement-id]
  (let [placement-ids (create-ids-vec placement-id)]
    (creative/children placement-ids)))

(defn children-by-campaign [campaign-id]
  (children-by campaign-id :campaign_ids :placement_ids placement/children children-by-placement))

(defn children-by-account [account-id]
  (children-by account-id :account_ids :campaign_ids campaign/children children-by-campaign))

(defn children-by-division [division-id]
  (children-by division-id :division_ids :account_ids account/children children-by-account))

(defn children-by-client [client-id]
  (let [client-id (:id (client/get client-id))]
    (children-by client-id :client_ids :division_ids division/children children-by-division)))
