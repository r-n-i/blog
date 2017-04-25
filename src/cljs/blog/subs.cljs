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
 :new-entry-title
 (fn [db]
   (:title (:new-entry db))))
