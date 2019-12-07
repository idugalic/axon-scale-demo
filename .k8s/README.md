# Kubernetes deployment

Deployment on Kubernetes (CaaS), with [kustomize](https://github.com/kubernetes-sigs/kustomize).

All Kustomize does is merge various YAML files into one. 

We will have one environment(s):

- `Base`: contains a set of YAML configuration on which other environments can be based on
- `Local`: extends base, will change the image of the base deployment and service type

## `Local`

####  Build application and Docker image to local Docker Daemon
[Jib](https://github.com/GoogleContainerTools/jib) builds optimized Docker and OCI images for your Java applications without a Docker daemon 
```bash
$ mvn clean verify jib:dockerBuild
```

#### Apply / Deploy to `Local` environment
[Kustomize](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#overview-of-kustomize) is a tool for customizing Kubernetes configurations. It is part of the `kubectl`.

You can run the following command to start your application(s) on `Local` environment:
```bash
kubectl apply -k .k8s/overlays/monolithics/local/
```

#### Delete `Local` environment
```bash
kubectl delete -k .k8s/overlays/monolithics/local/
```
