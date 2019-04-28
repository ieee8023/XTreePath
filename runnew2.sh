#!/bin/bash

java -Xmx4g -cp `sh getclasspath.sh`:classes experiments.Experiment $@

echo java finished