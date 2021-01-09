kubectl delete deployment backend
sudo docker build -t cloud.canister.io:5000/miguelmcell/finhub-backend:latest .
sudo docker push cloud.canister.io:5000/miguelmcell/finhub-backend:latest
kubectl create -f backend-deployment.yaml
