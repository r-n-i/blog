(ns blog.views
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com]
            [reagent.core :as reagent]
            [markdown.core :as markdown]))

(defn react-raw [raw-html-str]
  "A React component that renders raw html."
  (.div (.-DOM js/React)
        (clj->js {:key (str (hash raw-html-str))
                  :dangerouslySetInnerHTML {:__html raw-html-str}})))

(defn modal-dialog-panel [children]
  [re-com/modal-panel
   :backdrop-color   "grey"
   :backdrop-opacity 0.4
   :style            {:font-family "Consolas"}
   :child
   [re-com/border
    :border "1px solid #eee"
    :child  [re-com/v-box
             :padding  "10px"
             :style    {:background-color "cornsilk"}
             :children children]]])

(defn modal-dialog []
  (fn []
    (let [user-form (re-frame/subscribe [:user-form])
          auth       @(re-frame/subscribe [:auth])
          sign-error (re-frame/subscribe [:sign-error])]
      (modal-dialog-panel [
                           [re-com/title :label "Profile" :level :level2]
                           [re-com/title :label @sign-error :level :level3 :style {:color :red}]
                           [re-com/v-box
                            :class    "form-group"
                            :children [[:label {:for "pf-email"} "Email address"]
                                       [re-com/input-text
                                        :model       (or (:email @user-form) "")
                                        :on-change   #(re-frame/dispatch [:on-change-email %])
                                        :placeholder "Enter email"
                                        :class       "form-control"
                                        :attr        {:id "pf-email"}]]]
                           [re-com/v-box
                            :class    "form-group"
                            :children [[:label {:for "pf-password"} "Password"]
                                       [re-com/input-text
                                        :model       (or (:password @user-form) "")
                                        :on-change   #(re-frame/dispatch [:on-change-password %])
                                        :placeholder "Enter password"
                                        :class       "form-control"
                                        :attr        {:id "pf-password" :type "password"}]]]
                           [re-com/line :color "#ddd" :style {:margin "10px 0 10px"}]
                           [re-com/h-box
                            :gap      "12px"
                            :children [(if-not auth [re-com/button
                                                     :label    "Sign in"
                                                     :class    "btn-primary"
                                                     :on-click #(re-frame/dispatch [:login])])
                                       (if auth [re-com/button
                                                 :label    "Create user"
                                                 :class    "btn-primary"
                                                 :on-click #(re-frame/dispatch [:create-user])])
                                       [re-com/button
                                        :label    "Cancel"
                                        :on-click #(re-frame/dispatch [:hide-login-modal])]]]]))))

(defn inputs []
  (fn []
    (let [new-entry @(re-frame/subscribe [:new-entry])]
      [re-com/v-box
       :height   "auto"
       :gap      "10px"
       :children [
                  [re-com/input-text
                   :width "500px"
                   :model (or (:title new-entry) "")
                   :on-change #(re-frame/dispatch [:on-change-title %])
                   :change-on-blur? false
                   :placeholder "new entry"
                   ]
                  [re-com/input-textarea
                   :width "500px"
                   :model (or (:body new-entry) "")
                   :on-change #(re-frame/dispatch [:on-change-body %])
                   :change-on-blur? false
                   :placeholder "body"
                   ]]])))

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

(defn input-preview []
  (fn []
    [re-com/h-box
     :gap "10px"
     :children[[inputs][preview]]]))

(defn editor []
  (fn []
    (let [new-entry   @(re-frame/subscribe [:new-entry])
          editor-mode (re-frame/subscribe [:editor-mode])
          tab-defs    (reagent/atom [{:id :input :label "edit"}
                                     {:id :preview :label "preview"}
                                     {:id :both :label "both"}])]
      [re-com/v-box
       :height   "auto"
       :width    "500px"
       :gap      "10px"
       :children [
                  [re-com/horizontal-tabs
                   :model     editor-mode
                   :tabs      tab-defs
                   :on-change #(re-frame/dispatch [:switch-editor-mode %])]
                  (case @editor-mode
                    :input   [inputs]
                    :preview [preview]
                    :both    [input-preview])
                  [re-com/button
                   :label "save"
                   :on-click #(re-frame/dispatch [:post])
                   ]]])))

(defn entry []
  (fn []
    (let [focus @(re-frame/subscribe [:focused])
          auth  @(re-frame/subscribe [:auth])]
      [:div
       [re-com/title
        :label (:title focus)
        :level :level1]
       (react-raw (markdown/md->html (:body focus)))
       [re-com/h-box
        :size "none"
        :gap "10px"
        :children [
                   [re-com/label
                    :label (:updated_at focus)
                    ]
                   (if (and auth (:title focus)) [re-com/md-icon-button
                                                  :md-icon-name "zmdi-delete"
                                                  :size :smaller
                                                  :on-click #(re-frame/dispatch [:delete])
                                                  ] "")
                   ]]])))

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
           :style {:color (if (and (= mode :read) (= entry- focus)) "#bf4a8e" :gray)}
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
                   :max-width "600px"]
                  ]])))

(defn new-entry-button []
  (fn []
    [re-com/hyperlink
     :label "new entry"
     :on-click #(re-frame/dispatch [:new-entry])]))

(defn login-button []
  (fn []
    [re-com/hyperlink
     :label "login"
     :on-click #(re-frame/dispatch [:show-login-modal])]))

(defn setting-button []
  (fn []
    [re-com/hyperlink
     :label "setting"
     :on-click #(re-frame/dispatch [:show-login-modal])]))

(defn main-panel []
  (fn []
    (let [error            @(re-frame/subscribe [:error])
          auth             @(re-frame/subscribe [:auth])
          show-login-modal @(re-frame/subscribe [:show-login-modal])]
      [re-com/v-box
       :gap      "10px"
       :children [
                  [re-com/h-box
                   :align :baseline
                   :children [
                              [re-com/box
                               :child [re-com/title
                                       :label "Blog"
                                       :level :level1]
                               :min-width "100px"
                               :max-width "200px"]
                              [re-com/box
                               :child ""
                               :size "auto"
                               ]
                              [re-com/box
                               :child (if auth [setting-button] "")
                               :align-self :stretch
                               :align :end
                               :min-width "100px"
                               :max-width "100px"]
                              [re-com/box
                               :child (if auth [new-entry-button] [login-button])
                               :align-self :stretch
                               :align :end
                               :min-width "100px"
                               :max-width "100px"]]]
                  [re-com/line
                   :class "line"
                   :size  "3px"
                   :color "#bf4a8e"
                   ]
                  [re-com/alert-box
                   :heading error
                   :style {:display (if error :inherit :none)}]
                  [panel]
                  (when show-login-modal [modal-dialog])]])))
