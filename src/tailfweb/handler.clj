(ns tailfweb.handler
  (:gen-class)
  (:require [clojure.core.async :as async]
            [clojure.java.io :as cji]
            [clojure.tools.logging :as ctl]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [org.httpkit.server :refer :all]
            [ring.util.response :refer :all]
            [tailfweb.file-watcher :as watcher]))

(def clients (atom #{}))
(defonce chan (async/chan))
(defonce server (atom nil))


(defn register-client!
  "Registers a client and sends the current last ten lines over the
  socket."
  [req]
  (with-channel req client
    (ctl/info "New client joined.")
    (doseq [line (seq @watcher/last-ten-lines)]
      (send! client line false))
    (swap! clients conj client)
    (on-close client
              (fn [status]
                (ctl/info "Client left.")
                (swap! clients disj client)))))


(defroutes app-routes
  (GET "/" [] (cji/resource "public/index.html"))
  (GET "/tailf/" [] register-client!)
  (route/not-found "Page not found"))


(defn tailf
  "Launches a thread to push updates in the file to the connected
  clients."
  [file-name]
  (watcher/watch-file-appends {:file-name file-name
                               :output-chan chan})
  (future (loop []
            (when-let [line (async/<!! chan)]
              (doseq [client @clients]
                (send! client line))
              (recur)))))


(def application
  (handler/site app-routes))


(defn stop-server []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)
    (ctl/info "Stopped server.")))


(defn -main
  [& args]
  (let [port (or (try (Integer/parseInt (first args))
                      (catch Exception _))
                 8901)]
    (stop-server)
    (tailf "myfile.txt")
    (reset! server (run-server #'application {:port port}))
    (ctl/info (format "Started server on %d." port))))
