(ns kubernetes.api.autoscaling-v2beta1
  (:require [kubernetes.api.swagger :as swagger]
            [kubernetes.api.util :as util]))

(def make-context util/make-context)

(swagger/render-full-api "autoscaling_v2beta1")
