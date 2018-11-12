(ns kubernetes.api.policy-v1beta1
  (:require [kubernetes.api.swagger :as swagger]
            [kubernetes.api.util :as util]))

(def make-context util/make-context)

(swagger/render-full-api "policy_v1beta1")
