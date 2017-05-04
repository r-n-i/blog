(ns blog.handler
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.util.response :refer [resource-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-body wrap-json-response]]
            [clojure.data.json :as json]
            [blog.core :as blog]))

(defroutes routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/entries" [] (json/write-str (blog/select-entries)))
  (POST "/entries" req (json/write-str (blog/create-entry (:params req))))
  (DELETE "/entries" [id] (json/write-str (blog/delete-entry id)))
  (resources "/"))

(def dev-handler
  (->
    #'routes
    wrap-keyword-params
    wrap-json-params
    wrap-json-response
    wrap-reload))

(def handler routes)
