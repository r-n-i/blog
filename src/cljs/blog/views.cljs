(ns blog.views
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com]))

(defn editor []
  (fn []
    (let [new-entry @(re-frame/subscribe [:new-entry])]
      [re-com/v-box
       :height   "auto"
       :gap      "10px"
       :children [
                  [re-com/input-text
                   :model (or (:title new-entry) "")
                   :on-change #(re-frame/dispatch [:on-change-title %])
                   :placeholder "new entry"
                   ]
                  [re-com/input-textarea
                   :model (or (:body new-entry) "")
                   :on-change #(re-frame/dispatch [:on-change-body %])
                   :placeholder "body"
                   ]
                  [re-com/button
                   :label "save"
                   :on-click #(re-frame/dispatch [:new-entry-save])
                   ]
                  ]])))

(defn entry []
  (fn []
    (let [focus @(re-frame/subscribe [:focused])]
      [:div
       [re-com/title
        :label (:title focus)
        :level :level1]
       [re-com/title
        :label (:body focus)
        :level :level2]])))

(defn entries []
  (fn []
    (let [entries @(re-frame/subscribe [:entries])
          focus   @(re-frame/subscribe [:focused])
          mode   @(re-frame/subscribe [:mode])]
      (println entries)
      [:ul
       (for [entry- entries] ^{:key entry-}
         [:li
          {
           :on-click #(re-frame/dispatch [:focus entry-])
           :style {:color (if (and (= mode :read) (= entry- focus)) :purple :gray)}
           }
          (:title entry-)])]
      )))

(defn panel []
  (fn []
    (let [mode @(re-frame/subscribe [:mode])]
      [re-com/h-box
       :size "none"
       :children [
                  [re-com/box
                   :child [entries]
                   :size "auto"
                   :min-width "100px"
                   :max-width "200px"]
                  [re-com/box
                   :child [(if (= mode :read) entry editor)]
                   :size "auto"
                   :min-width "100px"
                   :max-width "200px"]
                  ]])))

(defn main-panel []
  (fn []
    [re-com/v-box
     :height   "auto"
     :gap      "10px"
     :children [
                [re-com/h-box
                 :align :baseline
                 :children [
                            [re-com/box
                             :child [re-com/title
                                     :label "blog"
                                     :level :level1]
                             :size "auto"
                             :align-self :stretch
                             :align :end
                             :min-width "100px"
                             :max-width "200px"]
                            [re-com/box
                             :child ""
                             :size "auto"
                             :align-self :stretch
                             :min-width "100px"
                             ]
                            [re-com/box
                             :child [re-com/button
                                     :label "get"
                                     :on-click #(re-frame/dispatch [:get-entries])
                                     ]
                             :size "auto"
                             :align-self :stretch
                             :align :end
                             :min-width "100px"
                             :max-width "100px"]
                            [re-com/box
                             :child [re-com/button
                                     :label "new entry"
                                     :on-click #(re-frame/dispatch [:new-entry])
                                     ]
                             :size "auto"
                             :align-self :stretch
                             :align :end
                             :min-width "100px"
                             :max-width "100px"]

                            ]]
                [re-com/line
                 :size  "3px"
                 :color "red"]
                [panel]
                ]]))
