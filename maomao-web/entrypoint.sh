#!/bin/sh
# Set defaults if not provided
#: "${WEB_SERVER_PORT:=80}"
#: "${WEB_SERVER_NAME:=maomaoworld_web}"
#: "${AUTH_SERVER_HOST:=auth-service}"
#: "${AUTH_SERVER_PORT:=9000}"
#: "${MUSIC_SERVER_HOST:=music-service}"
#: "${MUSIC_SERVER_PORT:=6000}"
#
#export WEB_SERVER_PORT WEB_SERVER_NAME AUTH_SERVER_HOST AUTH_SERVER_PORT MUSIC_SERVER_HOST MUSIC_SERVER_PORT
#
#envsubst < /etc/nginx/templates/nginx.conf.template > /etc/nginx/conf.d/default.conf
cp /etc/nginx/templates/nginx.conf.template /etc/nginx/conf.d/default.conf
exec nginx -g 'daemon off;'