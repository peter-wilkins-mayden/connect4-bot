(ns bot.ai-minimax
  (:require [bot.board       :as board]
            [bot.check-board :as check]))
  ;(:use     clojure.tools.trace))

;;; HEURISTIC FUNCTION ;;;

; define score points for 4, 3 and 2 connected coins
;(def INF (Integer/MAX_VALUE))
(def CC4  1048576)
(def CC3  32)
(def CC2  4)

(defn bit-count
  "Counts how many bits are set on given bitboard."
  [board]
  (count (filter true? (map #(bit-test board %) board/board-bits))))

(defn get-score
  "Determines score of a bitboard manipulated by check-board-*."
  [check score]
  (* score (apply + (map bit-count check))))

(defn get-old-y [board x]
  (let [y (board/get-y board x)]
    (if (nil? y) 5 (dec y))))

(defn heuristic
  "Calculates the heuristic value of given bitboards for a player."
  [boards player-num x]
  (cond
   (> (check/check-board (boards (- 3 player-num))) 0)
     (- CC4)
   (> (check/check-board (boards player-num)) 0)
     CC4
   :else
   (let [alt-board (board/bit-insert (boards (- 3 player-num))
                                     (get-old-y (boards 0) x) x)]
     (apply + (map
               (fn [[check score]] (get-score check score))
              ;; what new connections do we get?
               [[(check/check-board-3 (boards player-num)) CC3]
                ;[(check/check-board-2 (boards player-num)) CC2]
              ;; would opponent connect coins if he set here?
                [(check/check-board-4 alt-board) (/ CC4 4)]
                [(check/check-board-3 alt-board) (/ CC3 2)]])))))
                ;[(check/check-board-2

;;; MINIMAX ALGORITHM ;;;

(defn get-max [coll]
  (let [filtered (filter (comp not nil?) coll)]
    (if (empty? filtered)
    0
    (apply max filtered))))

(defn minimax
  "Minimax algorithm using only main heuristic."
  [boards player-num x depth]
  (cond
   (nil? boards) nil
   (zero? depth) (heuristic boards player-num x)
   :else
   (+ (- (get-max (map #(minimax
                         (board/insert boards % (- 3 player-num))
                         (- 3 player-num) % (dec depth))
                       [0 1 2 3 4 5 6])))
      (heuristic boards player-num x))))

(defn get-highest-index [coll]
  (apply max-key second
         (filter (comp not nil? second) coll)))

(defn make-move
  "Generate next move using given algorithm.
  Given function should take 4 args in this order:
  boards, player-num, x, depth"
  [algorithm boards player-num depth]
  (first (get-highest-index
          (map-indexed
           vector
           (map #(algorithm (board/insert boards % player-num)
                            player-num % depth)
                [0 1 2 3 4 5 6])))))