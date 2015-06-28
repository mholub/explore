(ns process.core
  (:gen-class))
(require '[me.raynes.fs :refer [glob]]
         '[clojure.java.io :as io]
         '[cheshire.core :as json]
         '[clojure.data.csv :as csv])

(defn exit [message] (throw (Exception. message)))

(defn csv [filename data]
  (with-open [f (io/writer filename)]
    (csv/write-csv f data)))

(defn check-field [value]
  (cond #(%1 %2) value
    nil? (exit "Field is missing")
    (complement string?) (exit "Field is not a string")
    :else value))

(defn process [field folder]
  (def files (glob (io/file folder) "*.json"))
  (def contents (map #(json/parse-string (slurp %)) files))
  (def values (map (comp check-field #(get % field)) contents))
  (def stats (sort-by val > (frequencies (remove empty? values))))
  (csv "output.csv" stats))

(defn -main [& args]
  (if (= 2 (count args))
    (process (first args) (second args))
    (println "Args are: <field name> <folder>")))