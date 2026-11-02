#!/usr/bin/env bash
set -euo pipefail

context="${1:-kind-autorepairshop}"

kubectl --context "$context" apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

if ! kubectl --context "$context" -n kube-system get deployment metrics-server -o jsonpath='{.spec.template.spec.containers[0].args}' | grep -q kubelet-insecure-tls; then
  kubectl --context "$context" -n kube-system patch deployment metrics-server --type json \
    -p '[{"op":"add","path":"/spec/template/spec/containers/0/args/-","value":"--kubelet-insecure-tls"}]'
fi

kubectl --context "$context" -n kube-system rollout status deployment/metrics-server --timeout=180s
