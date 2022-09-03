This directory contains the helm chart that is used to deploy this app.

```
minikube image build -t hangar-frontend:2 . -f chart/dockerfiles/frontend/Dockerfile
```
```
helm upgrade --install hangar chart/ -f chart/local-values.yaml --set backend.image.repository=hangar-backend --set backend.image.tag=4 --set frontend.image.repository=hangar-frontend --set frontend.image.tag=2
```
