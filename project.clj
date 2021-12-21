(defproject nubank/kubernetes-api "1.6.1"
  :description "Kubernetes Client API Library"
  :url "https://github.com/nubank/clj-kubernetes-api"
  :license {:name "MIT"
            :url  "https://github.com/nubank/clj-kubernetes-api/blob/master/LICENSE"}

  :plugins [[lein-ancient "0.7.0"]]

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "1.5.648"]
                 [org.clojure/data.json "2.4.0"]
                 [http-kit "2.5.3"]
                 [less-awful-ssl "1.0.6"]]

  :repositories  [["central"  {:url "https://repo1.maven.org/maven2/" :snapshots false}]
                  ["clojars"  {:url "https://clojars.org/repo/"}]]

  :codox {:namespaces [#"kubernetes\.api\.v\d.*"]}

  :profiles {:dev {:plugins [[lein-codox "0.10.8"]]}})
