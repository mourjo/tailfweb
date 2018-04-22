(ns tailfweb.observer
  (:gen-class)
  (:require [clojure.java.io :as cji]
            [clojure.string :as cs]
            [clojure.tools.logging :as ctl]
            [tailfweb.handler :as main]))

(defn -main
  "A function that writes to the file and prints it to the stdout to
  observe tailf behaviour."
  [& args]
  (apply main/-main args)
  (ctl/info "To help the observation, logging will be suspended...\n\n\n")
  (with-redefs [ctl/log* (constantly nil)]
    (let [words (cs/split (slurp (cji/resource "words.txt")) #"\n")]
      (dotimes [i 1000]
        (with-open [wr (cji/writer "myfile.txt" :append true)]
          (let [txt (cs/join " "
                             (map (fn [_] (rand-nth words))
                                  (range (inc (rand-int 10)))))]
            (.write wr txt)
            (.write wr "\n")

            (println txt)))
        (Thread/sleep (inc (rand-int 2000)))))))
