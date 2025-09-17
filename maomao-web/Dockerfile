FROM node:20-alpine AS builder
LABEL authors="Maximilian Wey"

WORKDIR /app

# Accept build-time variables
ARG VITE_AUTH_API_BASE
ARG VITE_DISCORD_BOT_API_BASE

# Inject them as env vars so Vite can use them
ENV VITE_AUTH_API_BASE=$VITE_AUTH_API_BASE
ENV VITE_DISCORD_BOT_API_BASE=$VITE_DISCORD_BOT_API_BASE

COPY . .

RUN echo "VITE_AUTH_API_BASE=$VITE_AUTH_API_BASE" && \
    echo "VITE_DISCORD_BOT_API_BASE=$VITE_DISCORD_BOT_API_BASE" && \
    npm install && npm run build

RUN npm install && npm run build

FROM nginx:alpine
LABEL authors="Maximilian Wey"

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf.template /etc/nginx/templates/nginx.conf.template
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

EXPOSE 80
