# kubernetes-api

Kubernetes client API library for Clojure. Functions are generated using macros derived from offical [swagger spec](http://kubernetes.io/swagger-spec/api/v1).

[![cljdoc documentation](https://cljdoc.org/badge/nubank/kubernetes-api)](https://cljdoc.org/d/nubank/kubernetes-api/)

## Installation

Add the dependency to your `project.clj`.

[![Clojars Project](https://img.shields.io/clojars/v/nubank/kubernetes-api.svg)](https://clojars.org/nubank/kubernetes-api)

## Usage

First, run a kubernetes proxy with `kubectl proxy --port=8080`.

Each endpoint function returns a [core.async](https://github.com/clojure/core.async) channel. Under the covers, the [http-kit](https://www.http-kit.org) http client is used.

```clojure
(require '[kubernetes.api.v1 :as k8s]
         '[clojure.core.async :refer [<!!]])

;; Make a context for querying k8s
(def ctx (k8s/make-context "http://localhost:8080"))

;; List all nodes
(<!! (k8s/list-namespaced-node ctx))

;; Pass an optional parameter
(<!! (k8s/list-namespaced-node ctx {:namespace "default"}))

;; Create a pod
(<!! (k8s/create-namespaced-pod ctx
       {:kind "Pod" :metadata {:name "test"}
        :spec {:containers [{:name "nginx" :image "nginx"}]}}
       {:namespace "default"}))

;; Use a label selector when listing pods
(<!! (k8s/list-pod ctx {:label-selector "kube-system=true"}))
```

## Testing

You need to have `minikube` running locally (`minikube --bootstrapper=kubeadm --kubernetes-version=v1.10.2 start`)

Start the kubernetes proxy to a running k8s cluster (`kubectl proxy --port=8080`)

```bash
lein test
```

## License

See [LICENSE](LICENSE) file.
