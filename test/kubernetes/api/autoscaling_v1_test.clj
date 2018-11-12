(ns kubernetes.api.autoscaling-v1-test
  (:require [clojure.core.async :refer [<!!] :as async]
            [clojure.test :refer :all]
            [kubernetes.api.apps-v1 :as apps-v1]
            [kubernetes.api.autoscaling-v1 :as as-v1]
            [kubernetes.api.common :as common]
            [kubernetes.api.v1 :as v1]))

(def ctx (as-v1/make-context "http://localhost:8080"))
(def tns (common/random-name))

(def deployment-name (common/random-name))
(def deployment {:apiVersion "apps/v1"
                 :kind       "Deployment"
                 :metadata   {:name deployment-name}
                 :spec       {:selector {:matchLabels {:service "service"}}
                              :template {:metadata {:labels {:service "service"}}
                                         :spec     {:containers [{:name  "nginx"
                                                                  :image "nginx"}]}}}})

(def nsopt {:namespace "default"})
(def hpa-name (common/random-name))
(def hpa {:apiVersion "autoscaling/v1"
          :kind       "HorizontalPodAutoscaler"
          :metadata   {:name hpa-name}
          :spec       {:maxReplicas 4
                       :minReplicas 1
                       :scaleTargetRef {:kind "Deployment"
                                        :name deployment-name
                                        :apiVersion "apps/v1"}
                       :targetCPUUtilizationPercentage 50}})

(use-fixtures :once
              (fn [f]
                (<!! (v1/create-namespace ctx {:metadata {:name tns}}))
                (<!! (async/timeout 2000))
                (f)
                (<!! (v1/delete-namespace ctx {} {:name tns}))))

(deftest hpa-test
  (testing "creation of deployments"
    (let [{:keys[kind metadata]} (<!! (apps-v1/create-namespaced-deployment ctx deployment nsopt))]
      (is (= kind "Deployment"))
      (is (= (:name metadata) deployment-name))))

  (testing "creation of hpa"
    (let [{:keys[kind metadata]} (<!! (as-v1/create-namespaced-horizontal-pod-autoscaler ctx hpa nsopt))]
      (is (= kind "HorizontalPodAutoscaler"))
      (is (= (:name metadata) hpa-name))))

  (testing "listing hpas"
    (let [hpas (<!! (as-v1/list-namespaced-horizontal-pod-autoscaler ctx nsopt))]
      (is (= hpa-name (-> hpas :items first :metadata :name)))
      (is (= "HorizontalPodAutoscalerList" (:kind hpas)))))

  (testing "reading single hpa"
    (let [{:keys[kind metadata]} (<!! (as-v1/read-namespaced-horizontal-pod-autoscaler ctx (assoc nsopt :name hpa-name)))]
      (is (= kind "HorizontalPodAutoscaler"))
      (is (= (:name metadata) hpa-name))))

  (testing "deleting hpa"
    (let [_ (<!! (as-v1/delete-namespaced-horizontal-pod-autoscaler ctx {} (assoc nsopt :name hpa-name)))
          {:keys [reason]} (<!! (as-v1/read-namespaced-horizontal-pod-autoscaler ctx (assoc nsopt :name hpa-name)))]
      (is (= "NotFound" reason)))))

