(ns kubernetes.api.v1-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.core.async :refer (<!!) :as async]
            [kubernetes.api.v1 :as v1]))

(def ctx (v1/make-context "http://localhost:8080"))
(def tns (->> (repeatedly 10 #(rand-int 26))
              (map #(nth (char-array "abcdefghijklmnopqrstuvwxyz") %))
              (str/join "")))
(def nsopt {:namespace tns})
(def pod {:kind "Pod"
          :metadata {:name "test" :labels {:test "yes"}}
          :spec {:containers [{:name "nginx"
                               :image "nginx"}]}})

(use-fixtures :once
  (fn [f]
    (<!! (v1/create-namespaced-namespace ctx {:metadata {:name tns}}))
    (<!! (async/timeout 2000))
    (f)
    (<!! (v1/delete-namespaced-namespace ctx {} {:name tns}))))

(deftest node-test
  (testing "get all nodes"
    (let [{:keys [kind items]} (<!! (v1/list-namespaced-node ctx))]
      (is (= kind "NodeList"))
      (is (= (count items) 1))))
  (testing "get nodes with options"
    (let [{:keys [kind]} (<!! (v1/list-namespaced-node ctx nsopt))]
      (is (= kind "NodeList")))))

(deftest pod-test
  (let [name (atom nil)]
    (testing "create-namespaced-pod"
      (let [{:keys [kind status metadata] :as pod}
            (<!! (v1/create-namespaced-pod ctx pod nsopt))]
        (is (= kind "Pod") (str "Failed to create pod: " (pr-str pod)))
        (is (= (:phase status) "Pending"))
        (reset! name (:name metadata))))
    (testing "read-namespaced-pod"
      (let [{:keys [kind metadata]}
            (<!! (v1/read-namespaced-pod ctx (assoc nsopt :name @name)))]
        (is (= kind "Pod"))
        (is (= @name (:name metadata)))))
    (testing "list-pod"
      (let [{:keys [kind items]} (<!! (v1/list-pod ctx))]
        (is (= kind "PodList"))
        (is (some #(= @name (get-in % [:metadata :name])) items))))
    (testing "list-namespaced-pod"
      (let [{:keys [kind items]} (<!! (v1/list-namespaced-pod ctx nsopt))]
        (is (= kind "PodList"))
        (is (= @name (get-in items [0 :metadata :name])))))
    (testing "list-namespaced-pod with label-selector"
      (let [{:keys [kind items]} (<!! (v1/list-namespaced-pod ctx (assoc  nsopt :label-selector "test=yes")))]
        (is (= kind "PodList"))
        (is (= @name (get-in items [0 :metadata :name])))))
    (testing "list-namespaced-pod with bad label-selector"
      (let [{:keys [kind items]} (<!! (v1/list-namespaced-pod ctx (assoc nsopt :label-selector "dasfads=no")))]
        (is (= kind "PodList"))
        (is (= (count items) 0))))
    (testing "delete-namespaced-pod"
      (is (some? (<!! (v1/delete-namespaced-pod ctx {} (assoc nsopt :name @name))))))))
