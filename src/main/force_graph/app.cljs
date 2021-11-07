(ns force-graph.app
  (:require 
   [reagent.core :as r]
   [reagent.dom :as rdom]))

(defn square-root
  [x]
  (.sqrt js/Math x))

(def hedgehogs
  {"Animalia" ["Chordata"]
   "Chordata" ["Mammalia"]
   "Mammalia" ["Erinaceomorpha"]
   "Erinaceomorpha" ["Erinaceidae"]
"Erinaceidae" ["Erinaceinae"]
   "Erinaceinae" [ "Atelerix" "Erinaceus" "Hemiechinus" "Mesechinus"
                  "Paraechinus"]
   
   "Atelerix" []
   "Erinaceus" []
   "Hemiechinus" []
   "Mesechinus" []
   "Paraechinus" []})

(defonce nodes
  (r/atom (into {}
                (for [name (keys hedgehogs)]
                  {name {:x (rand-int 350) :y (rand-int 350)
                         :width 80}}))))

(def repulsion 20000) ; adjust for wider/narrower spacing
(def spring-length 20) ; base resting length of springs
(def step-size 0.0005)

(defn edge? 
  "Returns true if n1 is connected to n2."
  [n1 n2]
  (contains? (set (get hedgehogs n1)) n2))

(defn node-forces 
  "Takes the names of 2 nodes as strings, and outputs
   the force between them in the format of [forcex forcey]."
  [n1 n2]
  (let [i (get @nodes n1)
        j (get @nodes n2)
        deltax (- (:x j) (:x i))
        deltay (- (:y j) (:y i))
        d2 (+ (* deltax deltax) (* deltay deltay))
      ;; Coulomb's law -- repulsion varies inversely with square of distance
        forcex (* (/ repulsion d2) deltax)
        forcey (* (/ repulsion d2) deltay)
        distance (.sqrt js/Math d2)]
  ;; spring force along edges, follows Hooke's law
    [(+ forcex (when (edge? n1 n2) (* (- distance spring-length) deltax)))
     (+ forcey (when (edge? n1 n2) (* (- distance spring-length) deltay)))]))

(node-forces "Animalia" "Chordata")
(node-forces "Mesechinus" "Erinaceinae")

(defn node-force 
  "Calculates the aggregated [x y] force values for a node."
  [node]
  (reduce (fn [[x y] [x' y']] [(+ x x') (+ y y')])
          (remove #(js/isNaN (first %)) (map #(node-forces node %) (keys hedgehogs)))))

(node-force "Animalia")

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

(defonce counter (r/atom 0))


(defn app []
  [:div#app
   [:h1 "Force graph"]
   [:svg {:width "100%" :view-box "0 0 400 400"}
    (when (< 2 @counter)
      (into [:g]
            (map draw-edges (keys hedgehogs))))
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
  
  @nodes
  (first (keys hedgehogs))

  (-> js/document
      (.getElementById "Erinaceidae")
      .getBBox
      .-x)
  )

(defn update! []
  (doseq [name (keys hedgehogs)]
    (if (< @counter 20)
      (swap! nodes assoc-in [name :width] (+ 6 (width name)))
      (do (swap! nodes update-in [name :x] #(+ % (* step-size (first (node-force name)))))
          (swap! nodes update-in [name :y] #(+ % (* step-size (last (node-force name))))))
      )
    
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
