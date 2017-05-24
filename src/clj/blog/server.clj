(ns blog.server
  (:require [blog.handler :refer [handler]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [migratus.core :as migratus])
  (:gen-class))

(def config {:store            :database
             :migration-dir    "migrations"
             :db {:classname   "com.mysql.cj.jdbc.Driver"
                  :subprotocol "mysql"
                  :subname     "//localhost/blog"
                  :user        "blog"
                  :password    ""}})

(defn -main [& args]
  (migratus/migrate config)
  (run-jetty handler {:port (Integer/parseInt (or (env :port) "3000"))
                      :join? false}))
