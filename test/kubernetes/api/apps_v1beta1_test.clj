(ns kubernetes.api.apps-v1beta1-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.core.async :refer [<!!] :as async]
            [kubernetes.api.v1 :as v1]
            [kubernetes.api.apps-v1beta1 :as a-v1beta1]))

(defn random-name []
  (->> (repeatedly 10 #(rand-int 26))
       (map #(nth (char-array "abcdefghijklmnopqrstuvwxyz") %))
       (str/join "")))

(def ctx (a-v1beta1/make-context "http://localhost:8080"))
(def tns (random-name))
(def stateful-set-name (random-name))

(def nsopt {:namespace tns})
(def stateful-set {:apiVersion "apps/v1beta1"
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
    (let [{:keys[kind metadata]} (<!! (a-v1beta1/create-namespaced-stateful-set ctx stateful-set nsopt))]
      (is (= kind "StatefulSet"))
      (is (= (:name metadata) stateful-set-name))))

  (testing "listing stateful-sets"
    (let [stateful-sets (<!! (a-v1beta1/list-namespaced-stateful-set ctx nsopt))]
      (is (= stateful-set-name (-> stateful-sets :items first :metadata :name)))
      (is (= "StatefulSetList" (:kind stateful-sets)))))

  (testing "reading single stateful-set"
    (let [{:keys[kind metadata]} (<!! (a-v1beta1/read-namespaced-stateful-set ctx (assoc nsopt :name stateful-set-name)))]
      (is (= kind "StatefulSet"))
      (is (= (:name metadata) stateful-set-name)))))

; Removed deletion test due to resources getting orphan
