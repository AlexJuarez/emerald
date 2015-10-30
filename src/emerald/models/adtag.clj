(ns emerald.models.adtag
  (:refer-clojure :exclude [update get])
  (:use
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defn all []
  (select adtags
          (fields [:ad_tags.name :ad_tag_templates.template])
          (join adtag-templates)))
