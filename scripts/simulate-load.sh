#!/usr/bin/env bash
set -euo pipefail

context="${1:-kind-autorepairshop}"
namespace="${2:-auto-repair-shop}"
restored=0

restore() {
  if [[ "$restored" -eq 1 ]]; then
    return
  fi
  restored=1

  echo
  echo "Restoring the CPU target to 70% and removing the load generator."
  kubectl --context "$context" -n "$namespace" delete pod loadgen --ignore-not-found --wait=false >/dev/null 2>&1 || true
  kubectl --context "$context" -n "$namespace" patch hpa api --type merge -p '{"spec":{"metrics":[{"type":"Resource","resource":{"name":"cpu","target":{"type":"Utilization","averageUtilization":70}}}]}}' || true
  echo "Target restored. The HPA keeps the higher count for 2 minutes, then removes one replica per minute until it is back at 2."
}

trap restore EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

echo "Current HPA:"
kubectl --context "$context" -n "$namespace" get hpa api

echo "Lowering the CPU target to 1% and sending traffic for 2 minutes."
echo "A short burst stays inside the 30-second scale-up window, so the replica count does not move."
kubectl --context "$context" -n "$namespace" patch hpa api --type merge -p '{"spec":{"metrics":[{"type":"Resource","resource":{"name":"cpu","target":{"type":"Utilization","averageUtilization":1}}}]}}'

kubectl --context "$context" -n "$namespace" delete pod loadgen --ignore-not-found
kubectl --context "$context" -n "$namespace" run loadgen --image=busybox:1.36 --restart=Never --command -- \
  sh -c 'end=$(($(date +%s)+120)); n=0; while [ "$n" -lt 4 ]; do (while [ "$(date +%s)" -lt "$end" ]; do wget -q -O /dev/null http://api:8080/actuator/health || true; done) & n=$((n+1)); done; wait'

echo "Watch replicas (Ctrl+C when the desired count grows). The script restores the 70% target on exit."
kubectl --context "$context" -n "$namespace" get hpa api -w
