(ns kubernetes.api.util-test
  (:require [clojure.test :refer :all]
            [kubernetes.api.util :as util]))

(deftest request-opts-test
  (testing "when using basic auth"
    (let [ctx {:username "username"
               :password "password"}
          opts {:method :get
                :path "/foo"}
          request-opts (#'util/request-opts ctx opts)]
      (is (= "username:password" (:basic-auth request-opts)))))

  (testing "when using client certificates"
    (with-redefs [util/new-ssl-engine (constantly "fake ssl engine")]
      (let [ctx {:ca-cert "ca"
                 :client-cert "client-cert"
                 :client-key "client-key"}
            opts {:method :get
                  :path "/foo"}
            request-opts (#'util/request-opts ctx opts)]
        (is (false? (:insecure? request-opts)))
        (is (= "fake ssl engine" (:sslengine request-opts))))))

  (testing "when the request method is PATCH, the content type is set to strategic-merge-patch+json"
    (let [ctx {:username "username"
               :password "password"}
          opts {:method :patch
                :path "/foo"}
          request-opts (#'util/request-opts ctx opts)]
      (is (= "application/strategic-merge-patch+json" (get-in request-opts [:headers "Content-Type"]))))))
