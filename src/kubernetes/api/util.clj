(ns kubernetes.api.util
  (:require [clojure.string :as str]
            [clojure.core.async :refer [go <! >! chan]]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]))

(defn make-context [server]
  {:server server})

(defn- parameterize-path [path params]
  (reduce-kv (fn [s k v]
               (str/replace s (re-pattern (str "\\{" (name k) "\\}")) v))
             path
             params))

(defn- query-str [query]
  (->> query
       (map (fn [[k v]] (str (name k) "=" v)))
       (str/join "&")))

(defn- url [{:keys [server]} path params query]
  (str server
       (parameterize-path path params)
       (if (empty? query) "" "?")
       (query-str query)))

(defn parse-response [{:keys [status headers body error]}]
  (cond
    error {:success false :error error}
    :else (json/read-str body :key-fn keyword)))

(defn request [ctx {:keys [method path params query body]}]
  (let [c (chan)]
    (http/request
     (cond-> {:url (url ctx path params query)
              :method method
              :as :text}
       body (assoc :body (json/write-str body)
                   :content-type :json))
     #(go (let [resp (parse-response %)]
            #_(println "Request" method path query body resp)
            (>! c resp))))
    c))
