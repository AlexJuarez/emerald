(ns emerald.models.division
  (:refer-clojure :exclude [update get])
  (:use
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defn get [id]
  (->
   (select divisions
           (where {:id id :deleted false}))
   first))

(defn exists? [id]
  (not (empty? (get id))))

(defn access? [id user-id]
  (-> (select user-division-permissions
              (where {:division_id id :user_id user-id}))
      empty?
      not))

(defn get-pin [division-id user-id]
  (first (select division-pins
                 (where {:division_id division-id :user_id user-id}))))

(defn pin! [division-id user-id]
  (when (nil? (get-pin division-id user-id))
    (insert division-pins
            (values {:division_id division-id :user_id user-id}))))

(defn unpin! [division-id user-id]
  (delete division-pins
          (where {:division_id division-id :user_id user-id})))

(defn all-pins [user-id]
  (select division-pins
          (fields :division_id)))

(defn prep [division]
  (assoc division :id (java.util.UUID/randomUUID)))

(defn add! [division]
  (insert divisions
          (values division)))

(defn update! [id division]
  (update divisions
          (set-fields division)
          (where {:id id}))
  {:success "updated the division"})

(defn all
  ([]
   (all 10 0))
  ([lim os]
   (select divisions
           (where {:deleted false})
           (limit lim)
           (offset os))))
