(ns kubernetes.api.security-istio-io-v1
   (:require [kubernetes.api.openapiv2 :as openapiv2]
            [kubernetes.api.util :as util]))

(def make-context util/make-context)

(openapiv2/render-api-group "v1.23.17" "security.istio.io" "v1")
