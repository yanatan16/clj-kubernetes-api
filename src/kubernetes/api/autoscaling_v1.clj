(ns kubernetes.api.autoscaling-v1
  (:require [kubernetes.api.swagger :as swagger]
            [kubernetes.api.util :as util]))

(def make-context util/make-context)

(swagger/render-full-api "autoscaling_v1")
