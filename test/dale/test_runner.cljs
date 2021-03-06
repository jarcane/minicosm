;; This test runner is intended to be run from the command line
(ns dale.test-runner
  (:require
    ;; require all the namespaces that you want to test
    [dale.minicosm-port-test]
    [figwheel.main.testing :refer [run-tests-async]]))

(defn -main [& args]
  (run-tests-async 5000))
