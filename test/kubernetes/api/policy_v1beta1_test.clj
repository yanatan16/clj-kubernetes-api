(ns kubernetes.api.policy-v1beta1-test
  (:require [clojure.core.async :refer [<!!] :as async]
            [clojure.test :refer :all]
            [kubernetes.api.apps-v1 :as apps-v1]
            [kubernetes.api.common :as common]
            [kubernetes.api.policy-v1beta1 :as policy-v1beta1]
            [kubernetes.api.v1 :as v1]))

(def ctx (policy-v1beta1/make-context "http://localhost:8080"))

(def tns (common/random-name))

(def deployment-name (common/random-name))
(def deployment {:apiVersion "apps/v1"
                 :kind       "Deployment"
                 :metadata   {:name deployment-name}
                 :spec       {:selector {:matchLabels {:what "nginx-rocks"}}
                              :template {:metadata {:labels {:what "nginx-rocks"}}
                                         :spec     {:containers [{:name  "nginx"
                                                                  :image "nginx"}]}}}})

(def pod-disruption-budget-name (common/random-name))
(def pod-disruption-budget {:apiVersion "policy/v1beta1"
                            :kind       "PodDisruptionBudget"
                            :metadata   {:name pod-disruption-budget-name}
                            :spec       {:minAvailable 5
                                         :selector {:matchLabels {:what "nginx-rocks"}}}})

(def nsopt {:namespace tns})

(use-fixtures :once
              (fn [f]
                (<!! (v1/create-namespace ctx {:metadata {:name tns}}))
                (<!! (async/timeout 2000))
                (f)
                (<!! (v1/delete-namespace ctx {} {:name tns}))))

(deftest pod-disruption-budget-test
  (testing "creation of a deployment (basic requirement)"
    (let [{:keys [kind metadata]} (<!! (apps-v1/create-namespaced-deployment ctx deployment nsopt))]
      (is (= kind "Deployment"))
      (is (= (:name metadata) deployment-name))))

  (testing "creating a single pod disruption budget"
    (let [{:keys [kind spec]} (<!! (policy-v1beta1/create-namespaced-pod-disruption-budget ctx pod-disruption-budget nsopt))]
      (is (= kind "PodDisruptionBudget"))
      (is (= spec {:minAvailable 5 :selector {:matchLabels {:what "nginx-rocks"}}}))))

  (testing "listing pod disruption budgets"
    (let [{:keys [items kind]} (<!! (policy-v1beta1/list-namespaced-pod-disruption-budget ctx nsopt))]
      (is (= 1 (count items)))
      (is (= (->> items first :spec) {:minAvailable 5 :selector {:matchLabels {:what "nginx-rocks"}}}))
      (is (= kind "PodDisruptionBudgetList"))))

  (testing "reading a single pod disruption budget"
    (let [{:keys [kind spec]} (<!! (policy-v1beta1/read-namespaced-pod-disruption-budget ctx (assoc nsopt :name pod-disruption-budget-name)))]
      (is (= kind "PodDisruptionBudget"))
      (is (= spec {:minAvailable 5 :selector {:matchLabels {:what "nginx-rocks"}}}))))

  (testing "deleting a single pod disruption budget"
    (let [_ (<!! (policy-v1beta1/delete-namespaced-pod-disruption-budget ctx {} (assoc nsopt :name pod-disruption-budget-name)))
          {:keys [code details kind message metadata reason]} (<!! (policy-v1beta1/read-namespaced-pod-disruption-budget ctx (assoc nsopt :name pod-disruption-budget-name)))]
      (is (= code 404))
      (is (= details {:group "policy" :kind "poddisruptionbudgets" :name pod-disruption-budget-name}))
      (is (= kind "Status"))
      (is (= message (str "poddisruptionbudgets.policy \"" pod-disruption-budget-name "\" not found")))
      (is (= metadata {}))
      (is (= reason "NotFound")))))
