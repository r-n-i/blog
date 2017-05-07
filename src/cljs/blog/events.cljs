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
  (fn [{:keys [db]} [_ entries]]
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
  :on-change-email
  (fn  [db [_ email]]
    (assoc-in db [:user-form :email] email)))

(re-frame/reg-event-db
  :on-change-password
  (fn  [db [_ password]]
    (assoc-in db [:user-form :password] password)))

(re-frame/reg-event-fx
  :new-entry-save
  (fn [{:keys [db]} _]
    {:db (-> db
             (update-in [:entries] #(vec (conj (lazy-seq %) (:new-entry db))))
             (assoc-in  [:mode] :read)
             (assoc-in  [:editor-mode] :input)
             (assoc-in  [:focus] (:new-entry db))
             (assoc-in  [:new-entry] {:title nil :body nil}))
     :dispatch [:get-entries]}))

(re-frame/reg-event-db
  :auth-success
  (fn [db [_ res]]
    (-> db
        (assoc-in [:auth] true)
        (assoc-in [:show-login-modal] false))))

(re-frame/reg-event-db
  :auth-error
  (fn [db [_ res]]
    (assoc-in db [:auth] false)))

(re-frame/reg-event-db
  :sign-error
  (fn [db [_ res]]
    (assoc-in db [:sign-error] (:message (:response res)))))

(re-frame/reg-event-db
  :switch-editor-mode
  (fn [db [_ mode]]
    (assoc-in db [:editor-mode] mode)))

(re-frame/reg-event-db
  :show-login-modal
  (fn [db [_ mode]]
    (assoc-in db [:show-login-modal] true)))

(re-frame/reg-event-db
  :hide-login-modal
  (fn [db [_ mode]]
    (assoc-in db [:show-login-modal] false)))

(re-frame/reg-cofx
  :token
  (fn [coeffects _]
    (assoc coeffects :token (.getItem (.-localStorage js/window) :token))))

(re-frame/reg-event-fx
  :login-success
  (fn [_ [_ {:keys [token]}]]
  {:store-token-localstrage token
   :dispatch [:auth]}))

(re-frame/reg-fx
  :store-token-localstrage
  (fn [token]
    (.setItem (.-localStorage js/window) :token token)))

(defn wrap-default-http [info]
  (merge info {:format          (ajax/json-request-format)
               :response-format (ajax/json-response-format {:keywords? true})}))

(defn wrap-token-http [info token]
  (merge info {:headers {:Authorization (str "Bearer " token)}}))

(re-frame/reg-event-fx
  :get-entries
  [(re-frame/inject-cofx :token)]
  (fn [_ _]
    {:http-xhrio (-> {:method          :get
                      :uri             "/entries"
                      :on-success      [:process-entries]
                      :on-failure      [:process-error]}
                     wrap-default-http
                     )}))

(re-frame/reg-event-fx
  :auth
  [(re-frame/inject-cofx :token)]
  (fn [{:keys [token]} _]
    {:http-xhrio (-> {:method          :get
                      :uri             "/auth"
                      :on-success      [:auth-success]
                      :on-failure      [:auth-error]}
                     wrap-default-http
                     (wrap-token-http token))}))

(re-frame/reg-event-fx
  :login
  (fn [{:keys [db]} _]
    (let [{email :email password :password} (:user-form db)]
      {:http-xhrio {:method          :post
                    :uri             "/login"
                    :params          {:username email :password password}
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:login-success]
                    :on-failure      [:sign-error]}})))

(re-frame/reg-event-fx
  :post
  (fn [{:keys [db]} _]
    {:http-xhrio {:method          :post
                  :uri             "/entries"
                  :params          (:new-entry db)
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:new-entry-save]
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
