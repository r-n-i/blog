(ns blog.views
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com]
            [reagent.core :as reagent]
            [markdown.core :as markdown]))

(defn inputs []
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
                   ]]])))

(defn react-raw [raw-html-str]
  "A React component that renders raw html."
  (.div (.-DOM js/React)
        (clj->js {:key (str (hash raw-html-str))
                  :dangerouslySetInnerHTML {:__html raw-html-str}})))

(defn preview []
  (fn []
    (let [new-entry @(re-frame/subscribe [:new-entry])]
      [re-com/v-box
       :height   "auto"
       :gap      "10px"
       :children [
                  [re-com/title
                   :label (:title new-entry)
                   :level :level1]
                  (react-raw (markdown/md->html (:body new-entry)))
                  ]
       ])))

(defn editor []
  (fn []
    (let [new-entry   @(re-frame/subscribe [:new-entry])
          editor-mode (re-frame/subscribe [:editor-mode])
          tab-defs    (reagent/atom [{:id nil :label "edit"}
                                     {:id :preview :label "preview"}])]
      [re-com/v-box
       :height   "auto"
       :gap      "10px"
       :children [
                  [re-com/horizontal-tabs
                   :model     editor-mode
                   :tabs      tab-defs
                   :on-change #(re-frame/dispatch [:switch-editor-mode %])]
                  (if (nil? @editor-mode) [inputs] [preview])
                  [re-com/button
                   :label "save"
                   :on-click #(re-frame/dispatch [:post])
                   ]]])))

(defn entry []
  (fn []
    (let [focus @(re-frame/subscribe [:focused])]
      [:div
       [re-com/title
        :label (:title focus)
        :level :level1]
       (react-raw (markdown/md->html (:body focus)))
       ])))

(defn entries []
  (fn []
    (let [entries @(re-frame/subscribe [:entries])
          focus   @(re-frame/subscribe [:focused])
          mode    @(re-frame/subscribe [:mode])]
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

(defn new-entry-button []
  (fn []
    [re-com/button
     :label "new entry"
     :on-click #(re-frame/dispatch [:new-entry])]))

(defn login-button []
  (fn []
    [re-com/button
     :label "login"
     :on-click #(re-frame/dispatch [:login])]))

(defn main-panel []
  (fn []
    (let [error @(re-frame/subscribe [:error])
          auth  @(re-frame/subscribe [:auth])]
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
                                       :label "auth"
                                       :on-click #(re-frame/dispatch [:auth])
                                       ]
                               :size "auto"
                               :align-self :stretch
                               :align :end
                               :min-width "100px"
                               :max-width "100px"]
                              [re-com/box
                               :child [re-com/button
                                       :label "delete"
                                       :on-click #(re-frame/dispatch [:delete])
                                       ]
                               :size "auto"
                               :align-self :stretch
                               :align :end
                               :min-width "100px"
                               :max-width "100px"]
                              [re-com/box
                               :child [re-com/button
                                       :label "post"
                                       :on-click #(re-frame/dispatch [:post])
                                       ]
                               :size "auto"
                               :align-self :stretch
                               :align :end
                               :min-width "100px"
                               :max-width "100px"]
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
                               :child (if auth [new-entry-button] [login-button])
                               :size "auto"
                               :align-self :stretch
                               :align :end
                               :min-width "100px"
                               :max-width "100px"]]]
                  [re-com/line
                   :size  "3px"
                   :color "red"]
                  [re-com/alert-box
                   :heading error
                   :style {:display (if error :inherit :none)}]
                  [panel]]])))
