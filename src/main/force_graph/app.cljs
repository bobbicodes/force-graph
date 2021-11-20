(ns force-graph.app
  (:require 
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [force-graph.svg :as svg]))

;; attempting to translate https://github.com/jackrusher/jssvggraph/blob/master/graph.js

(defn square-root
  [x]
  (.sqrt js/Math x))

(defonce nodes
  (r/atom (into {}
                (for [name (keys svg/building-supplies)]
                  {name {:x (rand-int 700) :y (rand-int 700)
                         :width 50}}))))

(def repulsion 10000) ; adjust for wider/narrower spacing
(def spring-length 50) ; base resting length of springs
(def step-size 0.0005)

(defn edge? 
  "Returns true if n1 is connected to n2."
  [n1 n2]
  (contains? (set (get svg/building-supplies n1)) n2))

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
          (remove #(js/isNaN (first %)) (map #(node-forces node %) (keys svg/building-supplies)))))

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

(defn draw-edges [cx cy]
  (into [:g]
        (let [edges (keys svg/building-supplies)]
          (for [edge edges]
            [:line {:x1 cx
                    :y1 cy
                    :x2 (+ 30 (:x (get @nodes edge)))
                    :y2 (+ 6 (:y (get @nodes edge)))
                    :stroke "magenta" :stroke-width 3}]))))

(defonce counter (r/atom 0))

(defn svg-paths
  ([paths]
   (svg-paths nil paths 0 0 1))
  ([attrs paths]
   (svg-paths attrs paths 0 0 1))
  ([paths x y]
   (svg-paths nil paths x y 1))
  ([paths x y scale]
   (svg-paths nil paths x y scale))
  ([attrs paths x y scale]
   (into [:g (merge attrs
                    {:transform (str "scale(" scale ") translate(" x "," y ")")})]
         (for [[color path] paths]
           [:path {:stroke color :d path
                   :shape-rendering "crispEdges"}]))))

(defn app []
  [:div#app
   [:h1 "Force graph"]
   [:svg {:width "100%" :view-box "0 0 700 700"
          :shape-rendering "crispEdges"}
    (draw-edges 310 350)
    (svg-paths (get svg/stores "Building Supplies") 250 310)
    (into [:g]  
          (for [node (keys @nodes)]
            (svg-paths (node svg/building-supplies) (:x (get @nodes node)) (:y (get @nodes node)))))]])
            
(defn update! []
  (doseq [name (keys svg/building-supplies)]
    (if (< @counter 20)
      (swap! nodes assoc-in [name :width] (+ 6 (:width name)))
      (when (< @counter 5000) (do (swap! nodes update-in [name :x] #(+ % (* step-size (first (node-force name)))))
                                 (swap! nodes update-in [name :y] #(+ % (* step-size (last (node-force name))))))))
    (swap! counter inc)))

(js/setInterval update! 1)

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
