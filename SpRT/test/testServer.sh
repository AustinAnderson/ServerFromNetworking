#!/bin/bash
###################################################################
# Filename:       testServer.sh
# Author:         Austin Anderson
# Class:          CSI 3336
# Date Modified:  2015-04-12
#   -File Created
# Description:   
# 
####################################################################

echo ""
echo "  Test 1"
echo ""
tst="0\rPoll\r0\rRUN\r0\r";
tst=$tst"0\rNameStep\r0\rRUN\r2\rBob\rSmith\r";
tst=$tst"0\rFoodStep\r2\rFName\rBob\rLName\rSmith\rRUN\r1\rItalian\r";
tst=$tst"\c sdfsdfs";
printf $tst|./runClient.sh


echo ""
echo "  Test 2"
echo ""
tst="0\rPoll\r2\rFName\rBob\rLName\rSmith\rRUN\r0\r";
tst=$tst"0\rFoodStep\r3\rFName\rBob\rLName\rSmith\rRepeat\r1\rRUN\r1\rMexican\r";
tst=$tst"\c asdfs";
printf $tst|./runClient.sh

echo ""
echo "  Test 3"
echo ""
tst="0\rPoll\r0\rRUN\r0\r";
tst=$tst"0\rFoodStep\r0\rRUN\r1\rChicken\r";
tst=$tst"\c asdfsadf";
printf $tst|./runClient.sh

echo ""
echo "  Test 4"
echo ""
tst="0\rpolling\r0\rRUN\r0\r";
tst=$tst"\c sadfasd";
printf $tst|./runClient.sh

echo ""
echo "  Test 5"
echo ""
tst="0\rPoll\r0\rRUN\r0\r";
tst=$tst"0\rNameStep\r0\rRUN\r1\rYurp\r";
tst=$tst"0\rNameStep\r0\rRUN\r2\rRobert\rSmith\r";
tst=$tst"\c asdf";
printf $tst|./runClient.sh


echo ""
echo "  Test 7 concurent"
echo ""
tst="0\rPoll\r2\rFName\rBob\rLName\rSmith\rRUN\r0\r";
tst=$tst"0\rFoodStep\r3\rFName\rBob\rLName\rSmith\rRepeat\r1\rRUN\r1\rMexican\r";
tst=$tst"\c asdf";
printf $tst|./runClient.sh&
printf $tst|./runClient.sh&
