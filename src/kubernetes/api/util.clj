(ns kubernetes.api.util
  (:require [clojure.string :as str]
            [clojure.core.async :refer [go <! >! chan]]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [less.awful.ssl :as ssl]))

(defn make-context
  ([server] (make-context server {}))
  ([server opts] (merge {:server server} opts)))

(defn- parameterize-path [path params]
  (reduce-kv (fn [s k v]
               (str/replace s (re-pattern (str "\\{" (name k) "\\}")) v))
             path
             params))

(defn dashed->camel [s]
  (str/replace s #"-([a-z])" #(str/upper-case (second %))))

(defn- query-str [query]
  (->> query
       (map (fn [[k v]] (str (dashed->camel (name k)) "=" v)))
       (str/join "&")))

(defn- url [{:keys [server]} path params query]
  (str server
       (parameterize-path path params)
       (if (empty? query) "" "?")
       (query-str query)))

(defn- new-basic-auth-token [username password]
  (str username ":" password))

(defn- new-ssl-engine [ca-cert client-cert client-key]
  (-> (ssl/ssl-context client-key client-cert ca-cert)
      ssl/ssl-context->engine))

(defn- content-type [method]
  (if (= method :patch)
    "application/strategic-merge-patch+json"
    "application/json"))

(defn parse-response [{:keys [status headers body error]}]
  (cond
    error {:success false :error error}
    :else (try
            (json/read-str body :key-fn keyword)
            (catch Exception e
              body))))

(defn- basic-auth? [{:keys [username password]}]
  (every? some? [username password]))

(defn- client-cert? [{:keys [ca-cert client-cert client-key]}]
  (every? some? [ca-cert client-cert client-key]))

(defn- token? [{:keys [token]}]
  (some? token))

(defn- token-fn? [{:keys [token-fn]}]
  (some? token-fn))

(defn- default-request-opts [ctx {:keys [method path params query]}]
  {:url       (url ctx path params query)
   :method    method
   :insecure? (not (client-cert? ctx))
   :as        :text})

(defn- request-auth-opts [{:keys [username password ca-cert client-cert client-key token token-fn] :as ctx} opts]
  (cond
    (basic-auth? ctx)
    {:basic-auth (new-basic-auth-token username password)}

    (client-cert? ctx)
    {:sslengine (new-ssl-engine ca-cert client-cert client-key)}

    (token? ctx)
    {:oauth-token token}

    (token-fn? ctx)
    {:oauth-token (token-fn ctx opts)}))

(defn- request-body-opts [{:keys [method body]}]
  (when (some? body)
    {:body    (json/write-str body)
     :headers {"Content-Type" (content-type method)}}))

(defn- request-opts [ctx opts]
  (merge (default-request-opts ctx opts)
         (request-auth-opts ctx opts)
         (request-body-opts opts)))

(defn request [ctx opts]
  (let [c (chan)]
    (http/request (request-opts ctx opts) #(go (>! c (parse-response %))))
    c))
