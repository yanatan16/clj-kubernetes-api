(ns kubernetes.api.v1beta1
  "Kubernetes v1 API. Auto-generated via macros from swagger documentation.
   Swagger doc is available from kubernetes.io/swagger-spec/api/v1"
  (:require [kubernetes.api.swagger :as swagger]
            [kubernetes.api.util :as util]))

(def make-context util/make-context)

(swagger/render-full-api "v1beta1")
