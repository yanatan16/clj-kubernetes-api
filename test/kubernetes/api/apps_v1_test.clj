(ns kubernetes.api.apps-v1-test
  (:require [clojure.core.async :refer [<!!] :as async]
            [clojure.test :refer :all]
            [kubernetes.api.apps-v1 :as a-v1]
            [kubernetes.api.common :as common]
            [kubernetes.api.v1 :as v1]))

(def ctx (a-v1/make-context "http://localhost:8080"))
(def tns (common/random-name))
(def stateful-set-name (common/random-name))

(def nsopt {:namespace tns})
(def stateful-set {:apiVersion "apps/v1"
                   :kind       "StatefulSet"
                   :metadata   {:name stateful-set-name}
                   :spec       {:selector {:matchLabels {:service "service"}}
                                :template {:metadata {:labels {:service "service"}}
                                           :spec     {:containers [{:name  "kafka"
                                                                    :image "kafka"}]}}}})


(use-fixtures :once
              (fn [f]
                (<!! (v1/create-namespace ctx {:metadata {:name tns}}))
                (<!! (async/timeout 2000))
                (f)
                (<!! (v1/delete-namespace ctx {} {:name tns}))))

(deftest stateful-set-test
  (testing "creation of stateful-sets"
    (let [{:keys[kind metadata]} (<!! (a-v1/create-namespaced-stateful-set ctx stateful-set nsopt))]
      (is (= kind "StatefulSet"))
      (is (= (:name metadata) stateful-set-name))))

  (testing "listing stateful-sets"
    (let [stateful-sets (<!! (a-v1/list-namespaced-stateful-set ctx nsopt))]
      (is (= stateful-set-name (-> stateful-sets :items first :metadata :name)))
      (is (= "StatefulSetList" (:kind stateful-sets)))))

  (testing "reading single stateful-set"
    (let [{:keys[kind metadata]} (<!! (a-v1/read-namespaced-stateful-set ctx (assoc nsopt :name stateful-set-name)))]
      (is (= kind "StatefulSet"))
      (is (= (:name metadata) stateful-set-name))))

  (testing "deleting stateful-set"
    (let [_ (<!! (a-v1/delete-namespaced-stateful-set ctx {} (assoc nsopt :name stateful-set-name)))
          {:keys [reason]} (<!! (a-v1/read-namespaced-stateful-set ctx (assoc nsopt :name stateful-set-name)))]
      (is (= "NotFound" reason)))))
