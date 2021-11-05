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

(defonce nodes
  (r/atom (into {}
                (for [name (keys hedgehogs)]
                  {name {:x (rand-int 350) :y (rand-int 350)
                         :width 80}}))))



(defn label
  [name x y]
   [:text {:id name
           :x (+ 3 x) :y y :dy "0.9em"  :text-anchor "left"
           :font-size "10px"
           :fill "yellow"} name])

(defn rect
  [x y width height]
  [:rect {:x x :y y 
          :rx 5
          :width width :height height :fill "purple"}])

(defn width [name]
  (-> js/document
      (.getElementById name)
      .getBBox
      .-width))

(:x (get @nodes "Animalia"))
(:y (get @nodes "Animalia"))

(:x (get @nodes "Chordata"))
(:y (get @nodes "Chordata"))


(defn draw-edges [node]
  (into [:g]
        (let [edges (get hedgehogs node)]
          (for [edge edges]
            [:line {:x1 (+ (/ (width edge) 2) (:x (get @nodes node)))
                    :y1 (+ 6 (:y (get @nodes node)))
                    :x2 (+ (/ (width edge) 2)(:x (get @nodes edge)))
                    :y2 (+ 6 (:y (get @nodes edge)))
                    :stroke "magenta"}]))))


(defn app []
  [:div#app
   [:h1 "Force graph"]
   [:svg {:width "100%" :view-box "0 0 400 400"}
    (into [:g]
          (map draw-edges (keys hedgehogs)))
    (into [:g]
          (for [name (keys hedgehogs)]
            (rect (:x (get @nodes name))
                  (:y (get @nodes name))
                  (:width (get @nodes name)) 13)))
    (into [:g]
          (for [name (keys hedgehogs)]
            (label name (:x (get @nodes name))
                   (:y (get @nodes name)))))]])
            
(comment
  

(edges hedgehogs)
  @nodes
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
      (swap! nodes assoc-in [name :width] (+ 6 (width name))))
    
    (swap! counter inc)))

(js/setInterval update! 100)

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
