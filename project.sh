#!/bin/bash

# This part is used to get name related to 'r' option in command
while getopts r: flag
do
    case "$flag" in
        r) command=${OPTARG};;
    esac
done

case "$command" in
    infra-up) script=./.tools/infra-up.sh;;
    infra-down) script=./.tools/infra-down.sh;;
    install-hooks) script=./.tools/install-hooks.sh;;
    install-libs) script=./.tools/install-libs.sh;;
esac

if [ -z "$script" ] 
then
    echo "Not exists any script related to command ${command}"
else
    sh $script $@
fi