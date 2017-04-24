(ns blog.views
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com]))

(defn editor []
  (fn []
    (let [focus @(re-frame/subscribe [:focused])]
      [:div
       [re-com/title
        :label (:title focus)
        :level :level1]
       [re-com/title
        :label (:body focus)
        :level :level2]])))

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
          focus   @(re-frame/subscribe [:focused])]
      [:ul
       (for [entry- entries] ^{:key entry-}
         [:li
          {
           :on-click #(re-frame/dispatch [:focus entry-])
           :style {:color (if (= entry- focus) :purple :gray)}
           }
          (:title entry-)])]
      )))

(defn panel []
  (fn []
    [re-com/h-box
     :size "none"
     :children [
                [re-com/box
                 :child [entries]
                 :size "auto"
                 :min-width "50px"
                 :max-width "100px"]
                [re-com/box
                 :child [entry]
                 :size "auto"
                 :min-width "100px"
                 :max-width "200px"]
                ]]))

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
                             :child [re-com/title
                                     :label "new entry"
                                     :level :level3
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
