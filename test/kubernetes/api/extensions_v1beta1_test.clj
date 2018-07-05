(ns kubernetes.api.extensions-v1beta1-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.core.async :refer [<!!] :as async]
            [kubernetes.api.v1 :as v1]
            [kubernetes.api.extensions-v1beta1 :as e-v1beta1]))

(defn random-name []
  (->> (repeatedly 10 #(rand-int 26))
       (map #(nth (char-array "abcdefghijklmnopqrstuvwxyz") %))
       (str/join "")))

(def ctx (e-v1beta1/make-context "http://localhost:8080"))
(def tns (random-name))
(def deployment-name (random-name))

(def nsopt {:namespace tns})
(def deployment {:apiVersion "extensions/v1beta1"
                 :kind       "Deployment"
                 :metadata   {:name deployment-name}
                 :spec       {:template {:metadata {:labels {:service "service"}}
                                         :spec     {:containers [{:name  "nginx"
                                                                  :image "nginx"}]}}}})


(use-fixtures :once
              (fn [f]
                (<!! (v1/create-namespace ctx {:metadata {:name tns}}))
                (<!! (async/timeout 2000))
                (f)
                (<!! (v1/delete-namespace ctx {} {:name tns}))))

(deftest deployment-test
  (testing "creation of deployments"
    (let [{:keys[kind metadata]} (<!! (e-v1beta1/create-namespaced-deployment ctx deployment nsopt))]
      (is (= kind "Deployment"))
      (is (= (:name metadata) deployment-name))))

  (testing "updating deployments"
    (let [new-image "nginx:1.13.3"
          patch-param {:spec {:template {:spec {:containers [{:name "nginx"
                                                              :image new-image}]}}}}
          updated-image (->> (assoc nsopt :name deployment-name)
                             (e-v1beta1/patch-namespaced-deployment ctx patch-param)
                             (<!!) :spec :template :spec :containers first :image)]
      (is (= updated-image new-image))))

  (testing "listing deployments"
    (let [deployments (<!! (e-v1beta1/list-namespaced-deployment ctx nsopt))]
      (is (= deployment-name (-> deployments :items first :metadata :name)))
      (is (= "DeploymentList" (:kind deployments)))))

  (testing "reading single deployment"
    (let [{:keys[kind metadata]} (<!! (e-v1beta1/read-namespaced-deployment ctx (assoc nsopt :name deployment-name)))]
      (is (= kind "Deployment"))
      (is (= (:name metadata) deployment-name)))))

; Removed deletion test due to resources getting orphan
