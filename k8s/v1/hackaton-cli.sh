#!/bin/bash

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "🚀 Video Processing Setup"

# Verificar se minikube está rodando
if ! kubectl get nodes &> /dev/null; then
    echo "❌ Minikube não está rodando. Execute: minikube start"
    exit 1
fi

# Criar imagem docker
echo "🔨 Criando imagem docker..."
cd ../../  # Volta para a raiz onde está o Dockerfile
docker build --no-cache -t hackaton-v2i-web:v3 .

# Copiar imagem para o minikube
echo "📦 Copiando imagem para o minikube..."
minikube image load hackaton-v2i-web:v3

# Volta para a pasta dos manifestos
cd k8s/v1

# Verificar/ativar metrics-server
echo "📊 Verificando metrics-server..."
minikube addons enable metrics-server > /dev/null 2>&1

# Descobrir IP local
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    LOCAL_IP=$(hostname -I | awk '{print $1}')
elif [[ "$OSTYPE" == "darwin"* ]]; then
    LOCAL_IP=$(ifconfig | grep "inet " | grep -v 127.0.0.1 | awk '{print $2}' | head -n1)
else
    read -p "Digite o IP local da sua máquina: " LOCAL_IP
fi

echo "📍 IP local: $LOCAL_IP"

export HOST_IP=$LOCAL_IP

# Aplicar manifestos
echo "⚙️ Aplicando recursos..."
envsubst < hackaton-configmap-v1.yaml | kubectl apply -f -
kubectl apply -f hackaton-db-secret-v1.yaml
kubectl apply -f hackaton-service-v1.yaml
kubectl apply -f hackaton-hpa-v1.yaml
kubectl apply -f hackaton-deployment-v1.yaml

echo -e "\n${YELLOW}⏳ Aguardando pods...${NC}"
kubectl rollout status deployment/v2i-web-deployment-v1 --timeout=180s

MINIKUBE_IP=$(minikube ip)

echo -e "\n${GREEN}🎉 Setup concluído!${NC}"
echo -e "\n${BLUE}🌐 Acesse sua aplicação:${NC}"
echo -e "  URL: http://$MINIKUBE_IP:30000/swagger-ui/index.html"
echo -e "  Health: http://$MINIKUBE_IP:30000/actuator/health"

echo -e "\n${BLUE}📝 Comandos úteis:${NC}"
echo -e "  kubectl get pods"
echo -e "  kubectl logs -f deployment/hackaton-deployment-v1"
echo -e "  kubectl get hpa"