#!/bin/bash

echo
echo "Downloading and installing kubectl"
KUBERNETES_VERSION=$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)
curl -LO https://storage.googleapis.com/kubernetes-release/release/${KUBERNETES_VERSION}/bin/linux/amd64/kubectl
chmod +x kubectl
sudo mv kubectl /usr/local/bin/

echo
echo "Downloading and installing KinD"
curl -Lo ./kind https://github.com/kubernetes-sigs/kind/releases/download/v0.5.1/kind-$(uname)-amd64
chmod +x ./kind
sudo mv kind /usr/local/kind

echo
echo "Starting Kubernetes Cluster"
kind create cluster
export KUBECONFIG="$(kind get kubeconfig-path)"

echo
echo "Starting kubectl proxy"
kubectl proxy --port=8080 &

lein test

