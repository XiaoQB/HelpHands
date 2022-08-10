#!/bin/sh

# shellcheck disable=SC2034
repository=75560
version=1.0-SNAPSHOT

# shellcheck disable=SC2039
declare -a arr=("consumer" "order" "provider" "service" "lookup")

# shellcheck disable=SC2039
#for i in "${arr[@]}"
#do
#   echo "building image of ""$i"
#   cd "$i"-api && mvn package install
#   cd ../"$i"-impl && mvn package docker:build
#   cd ..
#done

for i in "${arr[@]}"
do
   image=$i"-impl":$version
   echo "push image $image"
   docker tag "$image" $repository/"$image"
   docker push $repository/"$image"
done
