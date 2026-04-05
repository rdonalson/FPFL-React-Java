# -----------------------------
# 1. Build Stage
# -----------------------------
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files separately so dependency changes bust cache
COPY package.json package-lock.json ./

# Always reinstall dependencies (forces fresh build)
RUN npm ci

# Copy the rest of the source code
COPY . .

# Build Vite production bundle
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

EXPOSE 9500

CMD ["npm", "run", "preview", "--", "--port", "9500", "--host"]