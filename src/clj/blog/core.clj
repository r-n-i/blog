(ns blog.core
 (:require [korma.db :refer [defdb mysql]]
           [korma.core :refer [defentity values where order insert select delete]]))

(defdb db (mysql {:db "blog"
                 :user "root"
                 :password ""
                 }))

(defentity entries)

(defn select-entries []
  (select entries (order :id :desc)))

(defn create-entry [{:keys [title body]}]
  (insert entries (values {:title title :body body})))

(defn delete-entry [id]
  (delete entries (where {:id id})))
