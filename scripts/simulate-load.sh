#!/usr/bin/env bash
set -euo pipefail

context="${1:-kind-autorepairshop}"
namespace="${2:-auto-repair-shop}"

echo "Current HPA:"
kubectl --context "$context" -n "$namespace" get hpa api

echo "Lowering the CPU target to 1% so a short burst scales the API."
kubectl --context "$context" -n "$namespace" patch hpa api --type merge -p '{"spec":{"metrics":[{"type":"Resource","resource":{"name":"cpu","target":{"type":"Utilization","averageUtilization":1}}},{"type":"Resource","resource":{"name":"memory","target":{"type":"Utilization","averageUtilization":80}}}]}}'

kubectl --context "$context" -n "$namespace" delete pod loadgen --ignore-not-found
kubectl --context "$context" -n "$namespace" run loadgen --image=busybox:1.36 --restart=Never --command -- \
  sh -c 'i=0; while [ "$i" -lt 400 ]; do wget -q -O /dev/null http://api:8080/actuator/health || true; i=$((i+1)); done'

echo "Watch replicas (Ctrl+C when the desired count grows). Then restore the target with:"
echo "  kubectl --context $context -n $namespace patch hpa api --type merge -p '{\"spec\":{\"metrics\":[{\"type\":\"Resource\",\"resource\":{\"name\":\"cpu\",\"target\":{\"type\":\"Utilization\",\"averageUtilization\":70}}},{\"type\":\"Resource\",\"resource\":{\"name\":\"memory\",\"target\":{\"type\":\"Utilization\",\"averageUtilization\":80}}}]}}'"
kubectl --context "$context" -n "$namespace" get hpa api -w
