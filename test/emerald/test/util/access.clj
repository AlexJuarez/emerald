(ns emerald.test.util.access
  (:require [clojure.test :refer :all]
            [emerald.util.session :as sess]
            [emerald.util.access :refer :all]))

(deftest test-access?
  (testing "user is employee"
    (is (= true
           (binding [sess/*session* (atom {:user {:employee true}})]
             (access? (fn [id] false) 1)
             ))))
  (testing "user is not employee but has access"
    (is (= true
           (binding [sess/*session* (atom {:user {:employee false}})]
             (access? (fn [id] true) 1)
             ))))
  (testing "user is not employee and does not have access"
    (is (= false
           (binding [sess/*session* (atom {:user {:employee false}})]
             (access? (fn [id] false) 1)
             )))))
