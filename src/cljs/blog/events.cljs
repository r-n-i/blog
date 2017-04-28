(ns blog.events
  (:require [ajax.core :as ajax]
            [re-frame.core :as re-frame]
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

;(re-frame/reg-event-db
;  :get-entries
;  (fn [db _]
;    (println "get-entries")
;    (GET
;      "/entries"
;      {:response-format :json
;       :keywords? true
;       :handler #(re-frame/dispatch [:process-entries %])
;       :error-handler #()
;       }
;      )))

(re-frame/reg-event-fx
  :get-entries
  (fn [{:keys [db]} [_ a]]
    {:http {:method          :get
            :uri             "/entries"
            :timeout         8000
            :response-format (ajax/json-response-format {:keywords? true})
            :on-success      [:process-entries]
            :on-failure      [:process-error]}
     }))

(re-frame/reg-event-db
  :process-entries
  (fn [db [_ entries]]
    (assoc-in db [:entries] [{:title "w" :body "ee"}])
    ))

(re-frame/reg-event-db
  :process-error
  (fn [db [_ entries]]
    (assoc-in db [:entries] [{:title "w" :body "ee"}])
    ))

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
