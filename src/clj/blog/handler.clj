(ns blog.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [resources]]
            [ring.util.response :refer [resource-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [clojure.data.json :as json]
            )
  )
(use 'korma.db)
  (use 'korma.core)

(defdb db (mysql {:db "blog"
                 :user "root"
                 :password ""
                 }))

(defentity entries)
(defroutes routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/entries" [] (json/write-str (select entries)))
  (resources "/"))

(def dev-handler (-> #'routes wrap-reload))

(def handler routes)

