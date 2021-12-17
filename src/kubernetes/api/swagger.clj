(ns kubernetes.api.swagger
  "Render client API functions from swagger doc using macros"
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [kubernetes.api.util :as util]))


(defn capital? [c] (let [i (int c)] (and (<= i 90) (>= i 65))))

(defn camel->dashed [nickname]
  (-> nickname
      (str/replace #"([a-z])([A-Z])" "$1-$2")
      (str/replace #"([A-Z]+)([A-Z][a-z])" "$1-$2")
      str/lower-case))

(defn renderable-param? [{:keys [name] :as param}]
  (not (#{"watch" "pretty" "timeoutSeconds" "resourceVersion"} name)))

(defn render-doc-param [{:keys [type name description
                                required allowMultiple]}]
  (str (camel->dashed name)
       ": " (if required "required" "optional")
       " " (if allowMultiple (str "[" type "]") type)
       " " description))

(defn body-param-type [params]
  (->> params
       (filter #(= (:paramType %) "body"))
       first
       :type))

(defn render-doc-str [method path summary ret-type params]
  (str "\nCalls " method " on " path
       "\n" summary
       "\nParameters:"
       "\n\t- ctx: required server context"
       (if (not= method "GET")
         (str "\n\t- body: required " (body-param-type params))
         "")
       (if (not-empty params)
         (str "\nOptions:\n\t- " (str/join "\n\t- " (map render-doc-param params))))
       "\nReturns " ret-type))

(defn renderable-op?
  "Return true if op is renderable"
  [{:keys [produces nickname] :as op}]
  (and (some #{"application/json"} produces)
       (not (.startsWith nickname "watch"))))

(defn param-by-f [params f]
  (->> params
       (filter f)
       (map :name)
       (map camel->dashed)
       (map keyword)
       vec))
(defn param-by-type [params type] (param-by-f params #(= (:paramType %) type)))

(defn render-op [{:keys [path] :as api}
                 {:keys [nickname summary method type]
                  params :parameters
                  [{ret-type :responseModel}] :responseMessages
                  :as op}]
  (let [params (filter renderable-param? params)
        fn-name (symbol (camel->dashed nickname))
        doc-str (render-doc-str method path summary ret-type params)
        method-kw (-> method str/lower-case keyword)
        body-param (first (param-by-type params "body"))]
    `(defn ~fn-name ~doc-str [ctx# & [body?# opts?#]]
       (let [required-body# ~(some? body-param)
             [body# opts#] (if required-body# [body?# opts?#] [nil body?#])
             path-params# ~(param-by-type params "path")
             req-params# ~(param-by-f params
                                      #(and (:required %)
                                            (not= (:paramType %) "body")))
             query-params# ~(param-by-type params "query")]
         (assert (if required-body# (some? body#) (nil? body#))
                 (if required-body# "Body is required" "Body is prohibited"))
         (assert (every? #(get opts# %) req-params#)
                 (str "Missing required options: "
                      (pr-str (filter #(not (get opts# %)) req-params#))))
         (util/request
          ctx#
          {:method ~method-kw
           :path ~path
           :params (select-keys opts# path-params#)
           :query (select-keys opts# query-params#)
           :patch-type (:patch-type opts#)
           :body body#})))))

(defn render-api [{:keys [operations] :as api}]
  `(do ~@(->> operations
              (filter renderable-op?)
              (map (partial render-op api)))))

(defn render-swagger [{:keys [apis]}]
  `(do ~@(map render-api apis)))

(defmacro render-full-api [version]
  (-> (str "swagger/" version ".json")
      io/resource
      slurp
      (json/read-str :key-fn keyword)
      render-swagger))
