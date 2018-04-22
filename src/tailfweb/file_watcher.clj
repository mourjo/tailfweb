(ns tailfweb.file-watcher
  (:require [amalloy.ring-buffer :as arb]
            [clojure.core.async :as async]
            [clojure.string :as cs]
            [clojure.tools.logging :as ctl]))

(defonce last-ten-lines (atom (arb/ring-buffer 10)))


(def process-new-lines
  "A transducer that partitions read characters into a list of strings
  in the order in which they appear in the file."
  (comp (partition-by #{\newline})
        (remove #{[\newline]})
        (map reverse)
        (map cs/join)))


(defn locate-last-ten-lines
  "Scans the input file and locates the last ten lines by jumping to
  the end of the file and scanning backwards for ten new-line
  characters. `rdr` must already be opened."
  [^java.io.RandomAccessFile rdr]
  (let [original-length (.length rdr)]
    (.seek rdr original-length)

    (let [ten-lines (loop [data []
                           new-line-count 0]
                      (if (and (<= 2 (.getFilePointer rdr))
                               (< new-line-count 10))
                        (do (.seek rdr (- (.getFilePointer rdr) 2))
                            (let [c (char (.read rdr))]
                              (if (= c \newline)
                                (recur (conj data c) (inc new-line-count))
                                (recur (conj data c) new-line-count))))
                        (->> data
                             (sequence process-new-lines)
                             reverse
                             vec)))]
      ;; reset to original position because the file might have grown
      ;; since then
      (.seek rdr original-length)
      ten-lines)))


(defn watch-file-appends
  "Launches a new thread that updates the last-ten-files atom and
  polls the file for new lines. If there are new lines, it pushes the
  new line onto the output channel."
  [{:keys [file-name output-chan]}]
  (future
    (try
      (with-open [rdr (java.io.RandomAccessFile. file-name "r")]

        (swap! last-ten-lines into (locate-last-ten-lines rdr))

        (loop [size (.getFilePointer rdr)]
          (Thread/sleep 10) ;;; without this, the .length method returns 0
                            ;;; sometimes, not sure why
          (let [new-size (.length rdr)]
            (if (= size new-size)
              (recur new-size)
              (do (.seek rdr (dec size))
                  ;;; assuming lines only get appended, read the next \n
                  (.readLine rdr)

                  (while (< (.getFilePointer rdr) new-size)
                    (if-let [line (.readLine rdr)]
                      (do (swap! last-ten-lines conj line)
                          (async/>!! output-chan line))
                      (do (ctl/error "Encountered nil in file reading")
                          (throw (ex-info "Encountered nil in file reading"
                                          {:new-size new-size
                                           :size size})))))
                  (.seek rdr new-size)
                  (recur new-size))))))
      (catch Exception e
        (ctl/error (format "Error in file watcher\n%s" (str e)))))))
