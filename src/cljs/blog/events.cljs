(ns blog.events
  (:require [ajax.core :as ajax]
            [re-frame.core :as re-frame]
            [day8.re-frame.http-fx]
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
        (assoc-in [:mode] :read))))

(re-frame/reg-event-fx
  :process-entries
  (fn [db [_ entries]]
    {:db (assoc-in db [:entries] entries)
     :dispatch [:focus (first entries)]}))

(re-frame/reg-event-db
  :process-error
  (fn [db [_ error]]
    (assoc-in db [:error] error)))

(re-frame/reg-event-db
  :new-entry
  (fn  [db _]
    (assoc-in db [:mode] :edit)))

(re-frame/reg-event-db
  :on-change-title
  (fn  [db [_ title]]
    (assoc-in db [:new-entry :title] title)))

(re-frame/reg-event-db
  :on-change-body
  (fn  [db [_ body]]
    (assoc-in db [:new-entry :body] body)))

(re-frame/reg-event-db
  :new-entry-save
  (fn [db _]
    (-> db
        (update-in [:entries] #(conj % (:new-entry db)))
        (assoc-in  [:mode] :read)
        (assoc-in  [:focus] (:new-entry db))
        (assoc-in  [:new-entry] {:title nil :body nil}))))

(re-frame/reg-event-fx
  :get-entries
  (fn [{:keys [db]} _]
    {:http-xhrio {:method          :get
                  :uri             "/entries"
                  :timeout         8000
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:process-entries]
                  :on-failure      [:process-error]}}))

(re-frame/reg-event-fx
  :post
  (fn [{:keys [db]} _]
    {:http-xhrio {:method          :post
                  :uri             "/entries"
                  :timeout         8000
                  :params          (:new-entry db)
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:get-entries]
                  :on-failure      [:process-error]}}))

(re-frame/reg-event-fx
  :delete
  (fn [{:keys [db]} [_ id]]
    {:http-xhrio {:method          :delete
                  :uri             "/entries"
                  :timeout         8000
                  :params          (select-keys (:focus db) [:id])
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:get-entries]
                  :on-failure      [:process-error]}}))
