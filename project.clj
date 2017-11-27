(defproject kubernetes-api "0.6.0"
  :description "Kubernetes Client API Library"
  :url "https://github.com/yanatan16/clj-kubernetes-api"
  :license {:name "MIT"
            :url "https://github.com/yanatan16/clj-kubernetes-api/blob/master/LICENSE"}

  :plugins [[s3-wagon-private "1.3.1"]]

  :dependencies [[org.clojure/clojure "1.9.0-RC1"]
                 [org.clojure/core.async "0.3.442"]
                 [org.clojure/data.json "0.2.6"]
                 [http-kit "2.1.18"]
                 [less-awful-ssl "1.0.1"]]

  :repositories  [["central"  {:url "http://repo1.maven.org/maven2/" :snapshots false}]
                  ["clojars"  {:url "https://clojars.org/repo/"}]
                  ["nu-maven" {:url "s3p://nu-maven/releases/" :snapshots false :sign-releases false}]]

  :codox {:namespaces [#"kubernetes\.api\.v\d.*"]}

  :profiles {:dev {:plugins [[lein-codox "0.9.0"]]}})
