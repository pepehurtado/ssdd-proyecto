#!/bin/bash

# Mostrar mensajes de progreso
echo "==============================="
echo "Iniciando el proceso de despliegue..."
echo "==============================="

docker-compose -f docker-compose-devel-mongo.yml build


# Lista de imágenes a procesar
IMAGES=(
  "proyecto-backend-rest:latest"
  "proyecto-backend-rest-aux:latest"
  "proyecto-backend-grpc:latest"
  "proyecto-ssdd-frontend:latest"
  "proyecto-db-mongo:latest"
  "dsevilla/ssdd-llamachat-dummy:1.0"
)

for IMAGE in "${IMAGES[@]}"; do
  echo "Procesando imagen: $IMAGE"
  docker tag $IMAGE localhost:5000/$IMAGE
  docker push localhost:5000/$IMAGE
done

docker-compose -f docker-compose-devel-mongo.yml up -d

echo "==============================="
echo "¡Despliegue completado con éxito!"
echo "==============================="
