#!/bin/bash
###################################################################
# Filename:       runClient.sh
# Author:         Austin Anderson
# Class:          CSI 3336
# Date Modified:  2015-04-12
#   -File Created
# Description:   
# 
####################################################################


./java -jar SpRTTestClient.jar "129.62.129.195" 12345 2>output >/dev/null;
cat output|
sed 's/Exception.*.$//g'|sed 's/\t.*.$//g'|grep "^[a-Z]"|
sed 's/Ap..*run$//g'
