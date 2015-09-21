(ns emerald.models.division
  (:refer-clojure :exclude [update get])
  (:use
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defn access? [id user-id]
  (-> (select user-division-permissions
              (where {:division_id id :user_id user-id}))
      empty?
      not))

(defn get-pin [division-id user-id]
  (first (select division-pins
                 (where {:division_id division-id :user_id user-id}))))

(defn pinned? [id user-id]
  (not (nil? (get-pin id user-id))))

(defn get
  ([id] (get id nil))
  ([id user-id]
   (->
    (select divisions
            (where {:id id :deleted false}))
    first
    (assoc :pinned (pinned? id user-id)))))

(defn exists? [id]
  (not (empty? (get id))))

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

(defn prep-for-update [division]
  (dissoc division :pinned))

(defn add! [division]
  (insert divisions
          (values (-> division prep prep-for-update))))

(defn update! [id division user-id]
  (transaction
   (let [division (prep-for-update division)]
     (when (not (empty? division))
       (update divisions
               (set-fields division)
               (where {:id id}))))
   (if (:pinned division) (pin! id user-id))
   (if (= false (:pinned division)) (unpin! id user-id)))
  (get id user-id))

(defn all
  ([]
   (all 10 0))
  ([lim os]
   (select divisions
           (where {:deleted false})
           (limit lim)
           (offset os))))
