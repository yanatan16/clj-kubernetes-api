(defproject kubernetes-api "0.3.0-SNAPSHOT"
  :description "Kubernetes Client API Library"
  :url "https://github.com/yanatan16/clj-kubernetes-api"
  :license {:name "MIT"
            :url "https://github.com/yanatan16/clj-kubernetes-api/blob/master/LICENSE"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.2.374"]
                 [org.clojure/data.json "0.2.6"]
                 [http-kit "2.1.18"]]

  :repositories  [["central"  {:url "http://repo1.maven.org/maven2/" :snapshots false}]
                  ["clojars"  {:url "https://clojars.org/repo/"}]
                  ["nu-maven" {:url "s3p://nu-maven/releases/"
                  :username [:gpg :env/artifacts_aws_access_key_id]
                  :passphrase [:gpg :env/artifacts_aws_secret_access_key]}]]

  :codox {:namespaces [#"kubernetes\.api\.v\d.*"]}

  :profiles {:dev {:plugins [[lein-codox "0.9.0"]]}})
