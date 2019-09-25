(ns kubernetes.api.batch-v1beta1-test
  (:require [clojure.core.async :refer [<!!] :as async]
            [clojure.test :refer :all]
            [kubernetes.api.batch-v1beta1 :as batch.v1beta1]
            [kubernetes.api.common :as common]
            [kubernetes.api.v1 :as core.v1]))

(def ctx (batch.v1beta1/make-context "http://localhost:8080"))
(def tns (common/random-name))
(def cron-job-name (common/random-name))

(def nsopt {:namespace tns})
(def cron-job {:apiVersion "batch/v1beta1"
               :kind       "CronJob"
               :metadata   {:name cron-job-name}
               :spec       {:schedule "*/1 * * * *"
                            :jobTemplate {:spec {:template {:spec {:containers [{:name  "hello"
                                                                                 :image "busybox"
                                                                                 :args ["/bin/sh" "-c" "date"]}]
                                                                   :restartPolicy "OnFailure"}}}}}})

(use-fixtures :once
              (fn [f]
                (<!! (core.v1/create-namespace ctx {:metadata {:name tns}}))
                (<!! (async/timeout 2000))
                (f)
                (<!! (core.v1/delete-namespace ctx {} {:name tns}))))

(deftest cron-job-test
  (testing "creation of cron-jobs"
    (let [{:keys[kind metadata] :as xpto} (<!! (batch.v1beta1/create-namespaced-cron-job ctx cron-job nsopt))]
         #nu/tap cron-job
#nu/tap xpto
         (is (= kind "CronJob"))
      (is (= (:name metadata) cron-job-name))))

  (testing "listing cron-jobs"
    (let [cron-jobs (<!! (batch.v1beta1/list-namespaced-cron-job ctx nsopt))]
      (is (= cron-job-name (-> cron-jobs :items first :metadata :name)))
      (is (= "CronJobList" (:kind cron-jobs)))))

  (testing "reading single cron-job"
    (let [{:keys[kind metadata]} (<!! (batch.v1beta1/read-namespaced-cron-job ctx (assoc nsopt :name cron-job-name)))]
      (is (= kind "CronJob"))
      (is (= (:name metadata) cron-job-name)))))
