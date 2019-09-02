(ns firemore.authentication
  (:require
   [cljs.core.async :as async]
   [firemore.firebase :as firebase]
   [firemore.config :as config])
  (:require-macros
   [cljs.core.async.macros :refer [go-loop go]]))

(enable-console-print!)

(def FB firebase/FB)

(def user-atom (atom nil))

(defn user-change-handler [js-user]
  (reset!
   user-atom
   (when js-user
     {:anonymous? (.-isAnonymous js-user)
      :uid        (.-uid js-user)})))

(-> FB firebase/auth (.onAuthStateChanged user-change-handler))

(defn login-anonymously!
  ([] (login-anonymously! FB))
  ([fb] (login-anonymously! FB false))
  ([fb force?]
   (when (or force? (nil? @user-atom))
     (.signInAnonymously (firebase/auth fb)))))
