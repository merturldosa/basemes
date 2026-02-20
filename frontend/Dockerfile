# ═══════════════════════════════════════════════════════════════
# Multi-stage Dockerfile for SDS MES Frontend (React + Vite)
# @author Moon Myung-seop
# ═══════════════════════════════════════════════════════════════

# ───────────────────────────────────────────────────────────────
# Stage 1: Build
# ───────────────────────────────────────────────────────────────
FROM node:18-alpine AS builder

# Set working directory
WORKDIR /build

# Copy package files first for better layer caching
COPY package*.json ./

# Install dependencies
RUN npm ci --only=production=false

# Copy source code
COPY . .

# Build the application
RUN npm run build

# ───────────────────────────────────────────────────────────────
# Stage 2: Runtime (Nginx)
# ───────────────────────────────────────────────────────────────
FROM nginx:1.25-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/

# Copy built files from builder stage
COPY --from=builder /build/dist /usr/share/nginx/html

# Create script for runtime environment variable injection
RUN echo '#!/bin/sh' > /docker-entrypoint.d/40-inject-env.sh && \
    echo 'set -e' >> /docker-entrypoint.d/40-inject-env.sh && \
    echo 'echo "Injecting runtime environment variables..."' >> /docker-entrypoint.d/40-inject-env.sh && \
    echo 'cat > /usr/share/nginx/html/env-config.js << EOF' >> /docker-entrypoint.d/40-inject-env.sh && \
    echo 'window.ENV = {' >> /docker-entrypoint.d/40-inject-env.sh && \
    echo '  VITE_API_BASE_URL: "${VITE_API_BASE_URL:-/api}",' >> /docker-entrypoint.d/40-inject-env.sh && \
    echo '  VITE_WS_BASE_URL: "${VITE_WS_BASE_URL:-/ws}"' >> /docker-entrypoint.d/40-inject-env.sh && \
    echo '};' >> /docker-entrypoint.d/40-inject-env.sh && \
    echo 'EOF' >> /docker-entrypoint.d/40-inject-env.sh && \
    chmod +x /docker-entrypoint.d/40-inject-env.sh

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost/health || exit 1

# Default command (nginx will run)
CMD ["nginx", "-g", "daemon off;"]
