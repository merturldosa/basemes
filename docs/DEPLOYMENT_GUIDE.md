# SoIce MES Platform - Deployment Guide

> **Author**: Moon Myung-seop (문명섭)
> **Company**: SoftIce Co., Ltd. (주)소프트아이스
> **Version**: 0.1.0
> **Last Updated**: 2026-01-27

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Local Development Setup](#local-development-setup)
4. [Docker Deployment](#docker-deployment)
5. [Kubernetes Deployment](#kubernetes-deployment)
6. [Monitoring Setup](#monitoring-setup)
7. [CI/CD Pipeline](#cicd-pipeline)
8. [Environment Variables](#environment-variables)
9. [Troubleshooting](#troubleshooting)
10. [Rollback Procedures](#rollback-procedures)

---

## Overview

This guide covers deployment options for the SoIce MES Platform:

- **Local Development**: Running services directly on your machine
- **Docker**: Containerized deployment for testing and small-scale production
- **Kubernetes**: Scalable production deployment with high availability
- **Monitoring**: Prometheus + Grafana stack for observability

---

## Prerequisites

### Common Requirements

- Git (for cloning repository)
- Internet connection (for downloading dependencies)

### For Local Development

- **Java 11** (JDK)
- **Maven 3.8+**
- **Node.js 18+** and npm
- **PostgreSQL 15+**
- **Redis 7+**

### For Docker Deployment

- **Docker 20.10+**
- **Docker Compose 2.0+**

### For Kubernetes Deployment

- **Kubernetes 1.24+** cluster
- **kubectl** CLI tool
- **Helm 3+** (optional, for easier deployment)
- Container registry access (Docker Hub, GitHub Container Registry, etc.)

---

## Local Development Setup

### 1. Clone Repository

```bash
git clone https://github.com/your-org/SoIceMES.git
cd SoIceMES
```

### 2. Setup PostgreSQL

```bash
# Create database
createdb soice_mes

# Create user
psql -c "CREATE USER soice_admin WITH PASSWORD 'soice_password_2024';"
psql -c "GRANT ALL PRIVILEGES ON DATABASE soice_mes TO soice_admin;"

# Run migrations
psql -U soice_admin -d soice_mes -f database/migrations/V001__init_schema.sql
```

### 3. Setup Redis

```bash
# Start Redis with password
redis-server --requirepass soice_redis_2024
```

### 4. Backend Setup

```bash
cd backend

# Build the application
mvn clean install

# Run the application
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Backend will be available at: `http://localhost:8080`

### 5. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Run development server
npm run dev
```

Frontend will be available at: `http://localhost:3000`

---

## Docker Deployment

### Quick Start

```bash
# Build and start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f
```

Access the application:
- Frontend: `http://localhost`
- Backend API: `http://localhost/api`
- Swagger UI: `http://localhost/api/swagger-ui.html`

### Development Mode

For hot-reloading during development:

```bash
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
```

### With Monitoring

To include Prometheus and Grafana:

```bash
docker-compose -f docker-compose.yml -f monitoring/docker-compose.monitoring.yml up -d
```

Access monitoring:
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3001` (admin/admin123)

### Build Custom Images

```bash
# Build backend
cd backend
docker build -t soice-mes-backend:latest .

# Build frontend
cd frontend
docker build -t soice-mes-frontend:latest .
```

### Docker Commands Cheat Sheet

```bash
# Stop all services
docker-compose down

# Remove volumes (WARNING: deletes data!)
docker-compose down -v

# Rebuild images
docker-compose build --no-cache

# View resource usage
docker stats

# Clean up unused resources
docker system prune -a
```

---

## Kubernetes Deployment

### 1. Prepare Kubernetes Cluster

Ensure you have a running Kubernetes cluster and `kubectl` configured:

```bash
# Test cluster connection
kubectl cluster-info

# Create namespace
kubectl create namespace soice-mes
```

### 2. Configure Secrets

**IMPORTANT**: Never commit real secrets to Git!

```bash
# Create secrets
kubectl create secret generic soice-mes-secret \
  --from-literal=POSTGRES_USER=soice_admin \
  --from-literal=POSTGRES_PASSWORD='YOUR_STRONG_PASSWORD' \
  --from-literal=REDIS_PASSWORD='YOUR_REDIS_PASSWORD' \
  --from-literal=JWT_SECRET='YOUR_JWT_SECRET_KEY_MIN_256_BITS' \
  --from-literal=SPRING_DATASOURCE_URL='jdbc:postgresql://postgres-service:5432/soice_mes' \
  --from-literal=SPRING_DATASOURCE_USERNAME=soice_admin \
  --from-literal=SPRING_DATASOURCE_PASSWORD='YOUR_STRONG_PASSWORD' \
  --namespace=soice-mes
```

### 3. Deploy Infrastructure

```bash
# Apply configurations
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-configmap.yaml

# Deploy PostgreSQL
kubectl apply -f k8s/10-postgres-statefulset.yaml

# Deploy Redis
kubectl apply -f k8s/11-redis-deployment.yaml

# Wait for databases to be ready
kubectl wait --for=condition=ready pod -l app=postgres -n soice-mes --timeout=300s
kubectl wait --for=condition=ready pod -l app=redis -n soice-mes --timeout=300s
```

### 4. Deploy Application

```bash
# Deploy backend
kubectl apply -f k8s/20-backend-deployment.yaml

# Wait for backend to be ready
kubectl rollout status deployment/backend -n soice-mes

# Deploy frontend
kubectl apply -f k8s/21-frontend-deployment.yaml

# Wait for frontend to be ready
kubectl rollout status deployment/frontend -n soice-mes
```

### 5. Setup Ingress

**Important**: Update `k8s/30-ingress.yaml` with your domain name before applying!

```bash
# Edit ingress configuration
# Replace 'soice-mes.yourdomain.com' with your actual domain

# Apply ingress
kubectl apply -f k8s/30-ingress.yaml
```

### 6. Verify Deployment

```bash
# Check all resources
kubectl get all -n soice-mes

# Check pod logs
kubectl logs -f deployment/backend -n soice-mes
kubectl logs -f deployment/frontend -n soice-mes

# Get ingress details
kubectl get ingress -n soice-mes
```

### SSL/TLS with cert-manager

```bash
# Install cert-manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

# Create Let's Encrypt issuer
kubectl apply -f - <<EOF
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: your-email@example.com
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
EOF
```

---

## Monitoring Setup

### Docker Environment

```bash
# Start with monitoring
docker-compose -f docker-compose.yml -f monitoring/docker-compose.monitoring.yml up -d

# Access Grafana
open http://localhost:3001
# Login: admin / admin123
```

### Kubernetes Environment

```bash
# Install Prometheus Operator (recommended)
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Install kube-prometheus-stack
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --set grafana.adminPassword=admin123

# Access Grafana
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80
```

### Import Dashboards

1. Access Grafana at `http://localhost:3001`
2. Login with admin credentials
3. Go to Dashboards → Import
4. Import dashboard IDs:
   - Spring Boot: `12900`
   - PostgreSQL: `9628`
   - Redis: `11835`
   - Kubernetes Cluster: `7249`

---

## CI/CD Pipeline

### GitHub Actions

The project includes three workflows:

1. **CI (Build & Test)**: `.github/workflows/ci.yml`
   - Runs on every push/PR
   - Builds backend and frontend
   - Runs tests
   - Generates coverage reports

2. **Docker Build**: `.github/workflows/docker-build.yml`
   - Builds and pushes Docker images
   - Scans for vulnerabilities
   - Triggered on main branch or tags

3. **Deploy**: `.github/workflows/deploy.yml`
   - Deploys to Kubernetes
   - Manual trigger or tag-based
   - Includes rollback on failure

### Setup Required Secrets

In GitHub repository settings → Secrets and variables → Actions:

```
DOCKERHUB_USERNAME=your-dockerhub-username
DOCKERHUB_TOKEN=your-dockerhub-token
KUBE_CONFIG=base64-encoded-kubeconfig
POSTGRES_USER=soice_admin
POSTGRES_PASSWORD=your-strong-password
REDIS_PASSWORD=your-redis-password
JWT_SECRET=your-jwt-secret-key
```

### Trigger Deployment

```bash
# Via Git tag
git tag -a v0.1.0 -m "Release version 0.1.0"
git push origin v0.1.0

# Via GitHub UI
# Go to Actions → Deploy to Kubernetes → Run workflow
```

---

## Environment Variables

### Backend (Spring Boot)

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | Yes | `dev` | Active Spring profile |
| `SPRING_DATASOURCE_URL` | Yes | - | PostgreSQL connection URL |
| `SPRING_DATASOURCE_USERNAME` | Yes | - | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | - | Database password |
| `SPRING_REDIS_HOST` | Yes | `localhost` | Redis host |
| `SPRING_REDIS_PORT` | Yes | `6379` | Redis port |
| `SPRING_REDIS_PASSWORD` | Yes | - | Redis password |
| `JWT_SECRET` | Yes | - | JWT signing secret (min 256 bits) |
| `JWT_EXPIRATION` | No | `86400000` | JWT expiration (ms, default 24h) |
| `SERVER_PORT` | No | `8080` | Server port |
| `JAVA_OPTS` | No | `-Xms512m -Xmx1024m` | JVM options |

### Frontend (React)

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `VITE_API_BASE_URL` | No | `/api` | Backend API base URL |
| `VITE_WS_BASE_URL` | No | `/ws` | WebSocket base URL |

---

## Troubleshooting

### Backend Not Starting

**Symptom**: Backend fails to start with database connection error

**Solution**:
```bash
# Check database is running
docker-compose ps postgres

# Check database logs
docker-compose logs postgres

# Verify connection
psql -h localhost -U soice_admin -d soice_mes

# Reset database (WARNING: deletes data!)
docker-compose down -v
docker-compose up -d postgres
```

### Frontend Not Accessible

**Symptom**: 404 errors when accessing frontend

**Solution**:
```bash
# Check nginx configuration
docker-compose exec frontend cat /etc/nginx/conf.d/default.conf

# Check nginx logs
docker-compose logs frontend

# Rebuild frontend
docker-compose build frontend
docker-compose up -d frontend
```

### High Memory Usage

**Symptom**: Backend using too much memory

**Solution**:
```bash
# Adjust JVM heap size
export JAVA_OPTS="-Xms256m -Xmx512m"

# Or in docker-compose.yml:
environment:
  JAVA_OPTS: "-Xms256m -Xmx512m"
```

### Pod CrashLoopBackOff in Kubernetes

**Symptom**: Pods continuously restarting

**Solution**:
```bash
# Check pod logs
kubectl logs -f pod/backend-xxx -n soice-mes

# Describe pod for events
kubectl describe pod backend-xxx -n soice-mes

# Check resource limits
kubectl top pods -n soice-mes

# Increase resource limits in deployment YAML if needed
```

### Database Migration Failed

**Symptom**: Migration errors on startup

**Solution**:
```bash
# Run migrations manually
kubectl exec -it pod/postgres-0 -n soice-mes -- psql -U soice_admin -d soice_mes -f /migrations/V001__init_schema.sql

# Or rollback and reapply
kubectl delete -f k8s/10-postgres-statefulset.yaml
kubectl apply -f k8s/10-postgres-statefulset.yaml
```

---

## Rollback Procedures

### Docker Rollback

```bash
# Stop current version
docker-compose down

# Checkout previous version
git checkout v0.0.9  # or specific commit

# Rebuild and start
docker-compose build
docker-compose up -d
```

### Kubernetes Rollback

```bash
# Check rollout history
kubectl rollout history deployment/backend -n soice-mes

# Rollback to previous version
kubectl rollout undo deployment/backend -n soice-mes

# Rollback to specific revision
kubectl rollout undo deployment/backend --to-revision=2 -n soice-mes

# Verify rollback
kubectl rollout status deployment/backend -n soice-mes
```

### Database Rollback

**IMPORTANT**: Always backup before schema changes!

```bash
# Backup database
kubectl exec -it postgres-0 -n soice-mes -- pg_dump -U soice_admin soice_mes > backup.sql

# Restore from backup
kubectl exec -i postgres-0 -n soice-mes -- psql -U soice_admin soice_mes < backup.sql
```

---

## Security Best Practices

1. **Never commit secrets** to Git
2. **Use strong passwords** (min 16 characters, mixed case, numbers, symbols)
3. **Rotate credentials** regularly (every 90 days)
4. **Enable HTTPS** in production with valid certificates
5. **Restrict database access** to application network only
6. **Use secret management tools** (HashiCorp Vault, AWS Secrets Manager)
7. **Enable firewall rules** to restrict access
8. **Regular security updates** for all dependencies
9. **Monitor for vulnerabilities** using Trivy or similar tools
10. **Enable audit logging** for compliance

---

## Support

For issues or questions:

- **Email**: msmoon@softice.co.kr
- **Phone**: 010-4882-2035
- **GitHub Issues**: https://github.com/your-org/SoIceMES/issues

---

## License

Copyright © 2024 SoftIce Co., Ltd. All rights reserved.
