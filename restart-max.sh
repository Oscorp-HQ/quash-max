#!/bin/bash

BACKEND_ENV="application.properties"
FRONTEND_ENV=".env.local"

echo "Are you running this script on a hosted VM instance? (yes/no)"
read -r IS_HOSTED_VM

if [[ "$IS_HOSTED_VM" == "yes" ]]; then
    NEW_EXTERNAL_IP=$(curl -s https://ipinfo.io/ip)
    if [[ -z "$NEW_EXTERNAL_IP" ]]; then
        echo "Failed to retrieve external IP"
        exit 1
    fi
# If running locally, no need to run this script
elif [[ "$IS_HOSTED_VM" == "no" ]]; then
    NEW_EXTERNAL_IP="localhost"
else
    echo "Invalid input. Please enter 'yes' or 'no'."
    exit 1
fi
echo "EXTERNAL IP: $NEW_EXTERNAL_IP"

sed -i "s|http://[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+:3000/|http://${NEW_EXTERNAL_IP}:3000/|g" $BACKEND_ENV
sed -i "s|http://[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+:8080|http://${NEW_EXTERNAL_IP}:8080|g" $BACKEND_ENV

# Update frontend configuration
sed -i "s|NEXTAUTH_URL=http://[^:]*:3000/|NEXTAUTH_URL=http://${NEW_EXTERNAL_IP}:3000/|g" $FRONTEND_ENV
sed -i "s|NEXT_PUBLIC_REFRESH_BASE_URL=http://[^:]*:8080|NEXT_PUBLIC_REFRESH_BASE_URL=http://${NEW_EXTERNAL_IP}:8080|g" $FRONTEND_ENV
sed -i "s|NEXT_PUBLIC_BACKEND_URL=http://[^:]*:8080|NEXT_PUBLIC_BACKEND_URL=http://${NEW_EXTERNAL_IP}:8080|g" $FRONTEND_ENV

docker cp ./$BACKEND_ENV max-backend:/app/backend/$BACKEND_ENV
docker cp ./$FRONTEND_ENV max-frontend:/app/frontend/$FRONTEND_ENV

docker start max-frontend max-backend
echo "Containers started successfully"
