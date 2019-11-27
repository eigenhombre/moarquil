(ns moarquil.merging)



;; WIP for face simplification


;; 0D
(def points0 (repeatedly 5 (partial rand-int 5)))
points0
;;=>
'(2 3 1 3 2)

(defn merge0 [x] (distinct x))
(merge0 points0)
;;=>
'(2 3 1)

(def points1 [[0 1] [2 4] [4 5] [7 9]])

(defn is-adjacent1 [[aa bb] [a b]]
  (or (= aa b)
      (= bb a)))

(defn attach1 [[a1 a2] [b1 b2]]
  (cond (= b1 a2) [a1 b2]
        (= b2 a1) [b1 a2]
        :else (throw (Exception. "error"))))

(defn find-adjacent [adjacent-fn p el-set]
  (first (filter (partial adjacent-fn p) el-set)))

;; FIXME: O(n**2). Use kd-tree?
(defn simplify-items [join-fn adjacent-fn points]
  (loop [outer (set points)
         ret #{}]
    (if-not (seq outer)
      ret
      (let [x (first outer)
            xs (disj outer x)]
        (if-let [neighbor (find-adjacent adjacent-fn
                                         x
                                         xs)]
          (recur (disj xs neighbor)
                 (conj ret (join-fn x neighbor)))
          (recur xs (conj ret x)))))))

(simplify-items attach1 is-adjacent1 points1)
;;=>
'#{[2 5] [7 9] [0 1]}

(def points2 [[[0 0] [1 0]]
              [[2 0] [4 0]]
              [[4 0] [5 0]]
              [[7 0] [9 0]]])

(defn is-adjacent2 [[[a _] [b _]]
                    [[aa _] [bb _]]]
  (or (= aa b)
      (= bb a)))

(defn attach2 [[[a1 z1] [a2 z2]] [[b1 y1] [b2 y2]]]
  (cond (= b1 a2) [[a1 z1] [b2 z2]]
        (= b2 a1) [[b1 y1] [a2 z2]]
        :else (throw (Exception. "error"))))

(simplify-items attach2 is-adjacent2 points2)
;;=>
'#{[[7 0] [9 0]] [[2 0] [5 0]] [[0 0] [1 0]]}

(def points3 #{#{[0 0] [0 1]
                 [1 0] [1 1]}
               #{[2 0] [2 1]
                 [4 0] [4 1]}
               #{[4 0] [4 1]
                 [5 0] [5 1]}
               #{[7 0] [7 1]
                 [9 0] [9 1]}})

(defn faces-touch [points1 points2]
  (->> points1
       (clojure.set/intersection points2)
       count
       (< 1)))

(faces-touch #{[0 0] [0 1]
               [1 0] [1 1]}
             #{[2 0] [2 1]
               [4 0] [4 1]})
;;=>
'false

(faces-touch #{[2 0] [2 1]
               [4 0] [4 1]}
             #{[4 0] [4 1]
               [5 0] [5 1]})
;;=>
'true

(defn attach-faces [points1 points2]
  (let [intersection
        (->> points1
             set
             (clojure.set/intersection (set points2)))
        union
        (->> points1
             set
             (clojure.set/union (set points2)))]
    (clojure.set/difference union intersection)))

(simplify-items attach-faces faces-touch points3)
;;=>
'#{#{[0 0] [1 0] [1 1] [0 1]}
   #{[5 1] [2 0] [2 1] [5 0]}
   #{[7 1] [9 0] [9 1] [7 0]}}

;; Next: 3d faces; simplify if:
;; 1. faces share two points (within tolerance?)
;; 2. faces have the same "direction" (within tolerance?)
