repository=registry.cn-shanghai.aliyuncs.com/helphands
version=${1:-latest}

image1=helphands-impl:$version
image2=helphands-stream-impl:$version
docker tag $image1 $repository/$image1
docker tag $image2 $repository/$image2

docker push $repository/$image1
docker push $repository/$image2