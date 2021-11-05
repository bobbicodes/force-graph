(ns force-graph.app
  (:require 
   [reagent.core :as r]
   [reagent.dom :as rdom]))

(defn square-root
  [x]
  (.sqrt js/Math x))

(def hedgehogs
  {"Animalia" ["Chordata"]
   "Chordata" ["Animalia" "Mammalia"]
   "Mammalia" ["Chordata" "Erinaceomorpha"]
   "Erinaceomorpha" ["Mammalia" "Erinaceidae"]
   "Erinaceinae" ["Erinaceidae" "Atelerix" "Erinaceus" "Hemiechinus" "Mesechinus" "Paraechinus"]
   "Atelerix" ["Erinaceinae"]
   "Erinaceus" ["Erinaceinae"]
   "Hemiechinus" ["Erinaceinae"]
   "Mesechinus" ["Erinaceinae"]
   "Paraechinus" ["Erinaceinae"]
   "Erinaceidae" ["Erinaceomorpha" "Erinaceinae"]})

(defn label
  [name x y]
   [:text {:id name
           :x (+ 3 x) :y y :dy "1em"  :text-anchor "left"
           :font-size "12px"
           :fill "white"} name])

(defn rect
  [x y width height]
  [:rect {:x x :y y 
          :rx 5
          :width width :height height :fill "gray"}])

(defn x [name]
  (-> js/document
      (.getElementById name)
      .getBBox
      .-x))

(defn y [name]
  (-> js/document
      (.getElementById name)
      .getBBox
      .-y))

(defn width [name]
  (-> js/document
      (.getElementById name)
      .getBBox
      .-width))

(defonce positions 
  (r/atom (into {}
                (for [name (keys hedgehogs)]
                  {name [(rand-int 350) (rand-int 350)]}))))

(defonce widths
  (r/atom (into {}
                (for [name (keys hedgehogs)]
                  {name 80}))))

(get @positions "Erinaceidae")

(defn app []
  [:div#app
   [:h1 "Force graph"]
   [:svg {:width "100%" :view-box "0 0 400 400"}
    (into [:g]
          (for [name (keys hedgehogs)]
            (rect (first (get @positions name))
                  (last (get @positions name)) (get @widths name) 17)))
    (into [:g]
          (for [name (keys hedgehogs)]
            (label name (first (get @positions name)) 
                   (last (get @positions name)))))
    ]])
            
(comment
  (first (keys hedgehogs))

  (-> js/document
      (.getElementById "Erinaceidae")
      .getBBox
      .-x)
  )

(defonce counter (r/atom 0))

(defn update! []
  (doseq [name (keys hedgehogs)]
    (when (< @counter 20)
      (swap! widths assoc name (+ 6 (width name))))
    (swap! counter inc)))

(js/setInterval update! 100)

@widths

@counter

(defn render []
  (rdom/render [app]
            (.getElementById js/document "root")))

(defn ^:dev/after-load start []
  (render)
  (js/console.log "start"))

(defn ^:export init []
  (js/console.log "init")
  (start))

(defn ^:dev/before-load stop []
  (js/console.log "stop"))
