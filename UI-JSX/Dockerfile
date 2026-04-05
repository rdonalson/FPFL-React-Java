# -----------------------------
# 1. Build Stage
# -----------------------------
FROM node:20-alpine AS builder

WORKDIR /app

# Install dependencies first (better caching)
COPY package.json package-lock.json* ./
RUN npm ci

# Copy source
COPY . .

# Build Vite production bundle
RUN npm run build


# -----------------------------
# 2. Runtime Stage
# -----------------------------
FROM node:20-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production

# Copy only what we need
COPY --from=builder /app/package.json ./
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/dist ./dist

# Expose the port your compose file maps
EXPOSE 9500

# Run Vite preview server on the correct port
CMD ["npm", "run", "preview", "--", "--port", "9500", "--host"]