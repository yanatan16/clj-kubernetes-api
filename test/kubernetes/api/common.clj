(ns kubernetes.api.common
  (:require [clojure.string :as str]))

(defn random-name []
  (->> (repeatedly 10 #(rand-int 26))
       (map #(nth (char-array "abcdefghijklmnopqrstuvwxyz") %))
       (str/join "")))
