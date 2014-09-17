(ns skipjack.core
  (:gen-class)
  (:use org.httpkit.server))


(def state (atom 0))
(defn incr-value []
  (swap! state inc))

(defn decr-value []
  (swap! state dec))

(defn ok [body]
  { :status 200
    :headers {"Content-Type" "text/plain"}
    :body (str body)})

(defn increment-handler [channel]
  (send! channel (ok (incr-value))))

(defn decrement-handler [channel]
  (send! channel (ok (decr-value))))


(defn async-handler [ring-request]
  (println ring-request)
  (println (:uri ring-request))
  ;; unified API for WebSocket and HTTP long polling/streaming
  (with-channel ring-request channel    ; get the channel
    (if (websocket? channel)            ; if you want to distinguish them
      (on-receive channel (fn [data]     ; two way communication
                            (send! channel data)))
      (condp = (:uri ring-request)
        "/up" (increment-handler channel)
        "/down" (decrement-handler channel)
        (println "No handler")
        ))))


(defn -main
  []
  (run-server async-handler {:port 8080}))
