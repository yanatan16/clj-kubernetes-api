(ns kubernetes.api
  (:require [clojure.core.async :refer [go <! >! chan]]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]))

(defn make-context [server]
  {:server server
   :version "v1"})

(defn- url [{:keys [server version]} path]
  (str server "/api/" version path))

(defn request [ctx {:keys [method path]}]
  (let [c (chan)]
    (http/request
     {:url (url ctx path)
      :method method
      :as :text}
     (fn [{:keys [status headers body error]}]
       (go (>! c (cond
                   error {:success false :error error}
                   (= status 200) {:success true
                                   :body (json/read-str body :key-fn keyword)}
                   :else {:success false :error body})))))
    c))

(defn list-nodes [ctx]
  (request ctx {:method :get :path "/nodes"}))
