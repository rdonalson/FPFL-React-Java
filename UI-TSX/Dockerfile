# -----------------------------
# 1. Build Stage
# -----------------------------
FROM node:20-alpine AS builder

WORKDIR /app

ARG VITE_API_URL
ARG VITE_NODE_ENV=docker
ARG VITE_API_TIMEOUT_MS=30000
ARG VITE_SESSION_WARNING_MS=60000
ARG VITE_SESSION_DEBUG=false

COPY package.json ./
RUN npm install
COPY . .

RUN printf "VITE_NODE_ENV=%s\nVITE_API_URL=%s\nVITE_API_TIMEOUT_MS=%s\nVITE_SESSION_WARNING_MS=%s\nVITE_SESSION_DEBUG=%s\n" \
    "$VITE_NODE_ENV" "$VITE_API_URL" "$VITE_API_TIMEOUT_MS" "$VITE_SESSION_WARNING_MS" "$VITE_SESSION_DEBUG" > .env.production

RUN npm run build


# -----------------------------
# 2. Runtime Stage
# -----------------------------
FROM node:20-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production

COPY --from=builder /app/package.json ./
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/vite.config.ts ./

EXPOSE 80

CMD ["npm", "run", "preview", "--", "--port", "80", "--host"]
