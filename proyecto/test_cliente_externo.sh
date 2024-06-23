#!/bin/bash

USERNAME="usuario1"
EMAIL="usuario1@gmail.es"
PASSWORD=$(echo -n "usuario1" | md5sum | awk '{print $1}')
HOST="http://localhost:8180/Service/u"
CONTENT_TYPE="Content-Type: application/json"
USER="User: $USERNAME"

# Función para generar tokens
generate_token() {
    local url=$1
    local date=$2
    local user_token=$(echo -n "$USERNAME$EMAIL$PASSWORD" | md5sum | awk '{print $1}')
    echo -n "$url$date$user_token" | md5sum | awk '{print $1}'
}

# Función para realizar peticiones
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3

    local url="$HOST/$endpoint"
    local date=$(date -u +'%Y-%m-%dT%H:%M:%S.%3NZ')
    local token=$(generate_token "$url" "$date")
    local auth_token="Auth-Token: $token"

    curl -vv -X $method "$url" \
        -H "$USER" \
        -H "Date: $date" \
        -H "$auth_token" \
        -H "$CONTENT_TYPE" \
        -d "$data"
}


# Crear usuario usuario1
curl -X POST http://localhost:8080/Service/u/register      -H "Content-Type: application/json"      -d '{"id": "usuario1", "name": "usuario1", "password": "usuario1", "email": "usuario1@gmail.es"}'

# Login usuario1
echo -e "\n\nLogin usuario1\n"
curl -X POST http://localhost:8180/Service/checkLogin      -H "Content-Type: application/json"      -d '{"email": "usuario1@gmail.es", "password": "usuario1"}'

# Consulta del usuario
echo -e "\n\nConsulta usuario1\n"
make_request "GET" "$USERNAME/" ""

# Creación de un diálogo
echo -e "\n\nCreación de dialogo test\n"
make_request "POST" "$USERNAME/dialogue" '{"dialogueId": "test"}'

# Consulta del diálogo
echo -e "\n\nConsulta el test\n"
make_request "GET" "$USERNAME/dialogue/test" ""

# Modificar el nombre del diálogo
echo -e "\n\nModificar test a dialogoClient\n"
make_request "PUT" "$USERNAME/dialogue/test" '{"dialogueId": "dialogoClient"}'

# Consulta del diálogo
echo -e "\n\nConsulta del dialogoCliente\n"
make_request "GET" "$USERNAME/dialogue/dialogoClient" ""

# Prompt en el diálogo
echo -e "\n\nPrompt\n"
diag="dialogoClient"
url="$HOST/$USERNAME/dialogue/$diag"
date=$(date -u +'%Y-%m-%dT%H:%M:%S.%3NZ')
token=$(generate_token "$url" "$date")
auth_token="Auth-Token: $token"
next_url=$(curl -s "$url" -H "$USER" -H "Date: $date" -H "$auth_token" | grep -oP 'nextUrl":"/dialogue/'$diag'/\K-?[0-9]+')
timestamp=$(date +'%Y-%m-%dT%H:%M:%S')
make_request "POST" "$USERNAME/dialogue/$diag/$next_url" '{"timestamp": "'$timestamp'", "prompt": "Hola!"}'

# Consulta del diálogo
echo -e "\n\nConsulta dialogoClient\n"
make_request "GET" "$USERNAME/dialogue/dialogoClient" ""

# Consulta del diálogo después de esperar 5 segundos
sleep 7
echo -e "\n\nConsulta dialogoClient\n"
make_request "GET" "$USERNAME/dialogue/dialogoClient" ""

# Eliminar el diálogo
echo -e "\n\nEliminar dialogoClient\n"
make_request "DELETE" "$USERNAME/dialogue/dialogoClient" ""

# Consulta del diálogo
echo -e "\n\nConsulta dialogoClient\n"
make_request "GET" "$USERNAME/dialogue/dialogoClient" ""

# Eliminar usuario usuario1
echo -e "\n\nEliminar usuario1\n"
make_request "DELETE" "$USERNAME/" ""

# Consulta del usuario
echo -e "\n\nConsulta usuario1\n"
make_request "GET" "$USERNAME/" ""
