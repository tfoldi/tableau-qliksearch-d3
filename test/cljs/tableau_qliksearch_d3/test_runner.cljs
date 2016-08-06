(ns tableau-qliksearch-d3.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [tableau-qliksearch-d3.core-test]
   [tableau-qliksearch-d3.common-test]))

(enable-console-print!)

(doo-tests 'tableau-qliksearch-d3.core-test
           'tableau-qliksearch-d3.common-test)
