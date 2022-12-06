(ns kubernetes.api.openapiv2
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [kubernetes.api.swagger :as swagger]))

(defn operation-parameters [path-level-parameters parameters]
  (mapv
    (fn [param]
      {:type          (:type param)
       :paramType     (:in param)
       :name          (:name param)
       :description   (:description param)
       :required      (:required param)
       :allowMultiple false})
    (concat path-level-parameters parameters)))

(defn paths->apis [paths]
  (mapv (fn [[path {:keys [parameters] :as operations}]]
         {:path (subs (str path) 1)
          :operations (mapv (fn [[method operation]]
                             {:type ""
                              :consumes (:consumes operation)
                              :produces (:produces operation)
                              :summary (:description operation)
                              :nickname (:operationId operation)
                              :method (str/upper-case (name method))
                              :x-kubernetes-group-version-kind (:x-kubernetes-group-version-kind operation)
                              :parameters (operation-parameters parameters (:parameters operation))})
                            (select-keys operations [:get :patch :delete :put :post :head :options]))})
        paths))

(defn openapiv2->swagger [openapiv2]
  {:swaggerVersion "1.2"
   :apis (paths->apis (:paths openapiv2))})


(defn filter-operations [api-group api-version api]
  (update api :operations
          (fn [operations]
            (vec (filter (fn [{{:keys [group version]} :x-kubernetes-group-version-kind}]
                        (and (= api-group group) (= api-version version)))
                         operations)))))

(defn filter-api-group [swagger-definition api-group version]
  (update swagger-definition :apis
          (fn [apis]
            (->> apis
                 (map (partial filter-operations api-group version))
                 (filter (comp not-empty :operations))))))

(defmacro render-api-group [k8s-version api-group version]
  (-> (str "openapiv2/" k8s-version ".json")
      io/resource
      slurp
      (json/read-str :key-fn keyword)
      openapiv2->swagger
      (filter-api-group api-group version)
      swagger/render-swagger))

(defmacro render-full-api [k8s-version]
  (-> (str "openapiv2/" k8s-version ".json")
      io/resource
      slurp
      (json/read-str :key-fn keyword)
      openapiv2->swagger
      swagger/render-swagger))
