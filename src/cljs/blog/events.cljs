(ns blog.events
    (:require [re-frame.core :as re-frame]
              [blog.db :as db]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :focus
 (fn  [db [_ entry]]
   (-> db
   (assoc-in [:focus] entry)
   (assoc-in [:mode] :read)
   )))

(re-frame/reg-event-db
 :new-entry
 (fn  [db _]
   (assoc-in db [:mode] :edit
   )))

(re-frame/reg-event-db
 :on-change-title
 (fn  [db [_ title]]
   (assoc-in db [:new-entry :title] title
   )))

(re-frame/reg-event-db
 :on-change-body
 (fn  [db [_ body]]
   (assoc-in db [:new-entry :body] body
   )))

(re-frame/reg-event-db
 :new-entry-save
 (fn [db _]
   (-> db
   (update-in [:mode] :read)
   (update-in [:entries] #(conj % (:new-entry db)))
   (update-in [:focus] (:new-entry db))
   (update-in [:new-entry] {:title nil :body nil})
   )))
