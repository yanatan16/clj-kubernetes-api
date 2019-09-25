(defproject nubank/kubernetes-api "1.6.0"
  :description "Kubernetes Client API Library"
  :url "https://github.com/yanatan16/clj-kubernetes-api"
  :license {:name "MIT"
            :url "https://github.com/yanatan16/clj-kubernetes-api/blob/master/LICENSE"}

  :plugins [[s3-wagon-private "1.3.1"]
            [lein-ancient "0.6.14"]]

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/data.json "0.2.6"]
                 [http-kit "2.2.0"]
                 [less-awful-ssl "1.0.2"]]

  :repositories  [["central"  {:url "https://repo1.maven.org/maven2/" :snapshots false}]
                  ["clojars"  {:url "https://clojars.org/repo/"}]
                  ["nu-maven" {:url "s3p://nu-maven/releases/" :snapshots false :sign-releases false}]]

  :codox {:namespaces [#"kubernetes\.api\.v\d.*"]}

  :profiles {:dev {:plugins [[lein-codox "0.9.0"]]}})
