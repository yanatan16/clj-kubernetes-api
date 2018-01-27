(ns kubernetes.api.util-test
  (:require [clojure.test :refer :all]
            [kubernetes.api.util :as util]))

(deftest content-type-test
  (testing "when the request method is PATCH, the content type is strategic-merge-patch+json"
    (let [ct (#'util/content-type :patch)]
      (is (= "application/strategic-merge-patch+json" ct))))

  (testing "when the request method is anything else, the content type is application/json"
    (let [ct (#'util/content-type :post)]
      (is (= "application/json" ct)))))

(deftest basic-auth-test
  (testing "when the context has an username and password, it returns true"
    (let [ctx {:username "username"
               :password "password"}]
      (is (true? (#'util/basic-auth? ctx)))))

  (testing "when the context does not have either username or password, it returns false"
    (is (false? (#'util/basic-auth? {:username "only"})))
    (is (false? (#'util/basic-auth? {:password "only"})))
    (is (false? (#'util/basic-auth? {})))))

(deftest client-cert-test
  (testing "when the context has a ca-cert, client-cert and client-key, it returns true"
    (let [ctx {:ca-cert     "/path/to/ca.crt"
               :client-cert "/path/to/client.crt"
               :client-key  "/path/to/client.key"}]
      (is (true? (#'util/client-cert? ctx)))))

  (testing "when the context does not have either ca-cert, client-cert or client-key, it returns false"
    (is (false? (#'util/client-cert? {:ca-cert "only"})))
    (is (false? (#'util/client-cert? {:client-cert "only"})))
    (is (false? (#'util/client-cert? {:client-key "only"})))
    (is (false? (#'util/client-cert? {:ca-cert "ca" :client-cert "client"})))
    (is (false? (#'util/client-cert? {:ca-cert "ca" :client-key "key"})))
    (is (false? (#'util/client-cert? {:client-cert "client" :client-key "key"})))
    (is (false? (#'util/client-cert? {})))))

(deftest token-test
  (testing "when the context has a token, it returns true"
    (is (true? (#'util/token? {:token "token"}))))

  (testing "when the context does not have a token, it returns false"
    (is (false? (#'util/token? {})))))

(deftest token-fn-test
  (testing "when the context has a token-fn, it returns true"
    (is (true? (#'util/token-fn? {:token-fn (fn [ctx req] "token")}))))

  (testing "when the context does not have a token-fn, it returns false"
    (is (false? (#'util/token-fn? {})))))

(deftest default-request-opts-test
  (let [ctx {:server "http://kubernetes-api"}
        req {:method :get
             :path   "/path"}]
    (testing "it includes the request url"
      (with-redefs-fn {#'util/url (fn [ctx path params query] "expected-url")}
        #(is (= "expected-url" (:url (#'util/default-request-opts ctx req))))))

    (testing "it includes the request method"
      (is (= :get (:method (#'util/default-request-opts ctx req)))))

    (testing "it includes whether server's SSL certificates will use strict validation"
      (testing "when using client certificates, it enables strict SSL validation"
        (let [ctx {:ca-cert     "ca"
                   :client-cert "client-cert"
                   :client-key  "client-key"}]
          (is (false? (:insecure? (#'util/default-request-opts ctx req))))))

      (testing "when not using client certificates, it disables strict SSL validation"
        (let [ctx {:username "username"
                   :password "password"}]
          (is (true? (:insecure? (#'util/default-request-opts ctx req)))))))

    (testing "it includes the output coercion"
      (is (= :text (:as (#'util/default-request-opts ctx req)))))))

(deftest request-opts-test
  (testing "when using basic auth"
    (let [ctx  {:username "username"
                :password "password"}
          req  {:method :get
                :path   "/foo"}
          opts (#'util/request-opts ctx req)]
      (is (= "username:password" (:basic-auth opts)))))

  (testing "when using client certificates"
    (with-redefs [util/new-ssl-engine (constantly "fake ssl engine")]
      (let [ctx  {:ca-cert     "ca"
                  :client-cert "client-cert"
                  :client-key  "client-key"}
            req  {:method :get
                  :path   "/foo"}
            opts (#'util/request-opts ctx req)]
        (is (false? (:insecure? opts)))
        (is (= "fake ssl engine" (:sslengine opts))))))

  (testing "when using a bearer token"
    (let [ctx  {:token "token"}
          req  {:method :get
                :path   "/foo"}
          opts (#'util/request-opts ctx req)]
      (is (= "token" (:oauth-token opts)))))

  (testing "when using a function to dynamically generate the bearer token"
    (let [ctx  {:token-fn (fn [ctx req] "dynamic-token")}
          req  {:method :get
                :path   "/foo"}
          opts (#'util/request-opts ctx req)]
      (is (= "dynamic-token" (:oauth-token opts)))))

  (testing "when both token and token-fn are provided, the value returned by token fn is used"
    (let [ctx  {:token    "static-token"
                :token-fn (fn [ctx req] "dynamic-token")}
          req  {:method :get
                :path   "/foo"}
          opts (#'util/request-opts ctx req)]
      (is (= "dynamic-token" (:oauth-token opts)))))

  (testing "when the request has a body"
    (let [ctx {:token "token"}
          req {:method :post
               :path   "/foo"
               :body   {:json true}}
          opts (#'util/request-opts ctx req)]
      (is (= "{\"json\":true}" (:body opts))))))
