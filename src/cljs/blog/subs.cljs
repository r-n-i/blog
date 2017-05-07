(ns blog.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
  :name
  (fn [db]
    (:name db)))

(re-frame/reg-sub
  :entries
  (fn [db]
    (:entries db)))

(re-frame/reg-sub
  :focused
  (fn [db]
    (:focus db)))

(re-frame/reg-sub
  :new-entry
  (fn [db]
    (:new-entry db)))

(re-frame/reg-sub
  :mode
  (fn [db]
    (:mode db)))

(re-frame/reg-sub
  :error
  (fn [db]
    (:error db)))

(re-frame/reg-sub
  :auth
  (fn [db]
    (:auth db)))

(re-frame/reg-sub
  :new-entry-title
  (fn [db]
    (:title (:new-entry db))))

(re-frame/reg-sub
  :editor-mode
  (fn [db]
    (:editor-mode db)))

(re-frame/reg-sub
  :user-form
  (fn [db]
    (:user-form db)))

(re-frame/reg-sub
  :sign-error
  (fn [db]
    (:sign-error db)))

(re-frame/reg-sub
  :show-login-modal
  (fn [db]
    (:show-login-modal db)))
