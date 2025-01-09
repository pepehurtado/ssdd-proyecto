#!/bin/bash

docker service create --name registry --publish published=5000,target=5000 registry:2

sleep 5

docker-compose -f docker-compose-devel-mongo.yml build

IMAGES=(
  "proyecto-backend-rest:latest"
  "proyecto-backend-rest-aux:latest"
  "proyecto-backend-grpc:latest"
  "proyecto-ssdd-frontend:latest"
  "proyecto-db-mongo:latest"
  "dsevilla/ssdd-llamachat-dummy:1.0"
)

for IMAGE in "${IMAGES[@]}"; do
  docker tag $IMAGE localhost:5000/$IMAGE
  docker push localhost:5000/$IMAGE
done

docker-compose -f docker-compose-devel-mongoSwarm.yml up -d
