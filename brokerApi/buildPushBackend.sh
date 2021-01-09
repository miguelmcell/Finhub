sudo docker build -t cloud.canister.io:5000/miguelmcell/finhub-broker-api:latest .
sudo docker push cloud.canister.io:5000/miguelmcell/finhub-broker-api:latest
kubectl delete deployment broker-api
kubectl create -f broker-deployment.yaml
