sudo docker build -t cloud.canister.io:5000/miguelmcell/finhub-frontend:latest .
sudo docker push cloud.canister.io:5000/miguelmcell/finhub-frontend:latest
kubectl delete deployment frontend
kubectl create -f frontend-deployment.yaml
