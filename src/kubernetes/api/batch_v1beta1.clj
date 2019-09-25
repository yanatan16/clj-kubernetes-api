(ns kubernetes.api.batch-v1beta1
  (:require [kubernetes.api.swagger :as swagger]
            [kubernetes.api.util :as util]))

(def make-context util/make-context)

(swagger/render-full-api "batch_v1beta1")
