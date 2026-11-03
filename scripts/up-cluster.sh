#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
name=autorepairshop
context="kind-${name}"
config="${root}/infra/kind-config.yaml"

if command -v kind >/dev/null 2>&1; then
  kind_bin="$(command -v kind)"
elif [[ -x "${HOME}/.local/bin/kind.exe" ]]; then
  kind_bin="${HOME}/.local/bin/kind.exe"
elif [[ -x "${HOME}/.local/bin/kind" ]]; then
  kind_bin="${HOME}/.local/bin/kind"
else
  echo "kind is not installed. https://kind.sigs.k8s.io/" >&2
  exit 1
fi

wait_for_api() {
  local attempt
  for attempt in $(seq 1 30); do
    if kubectl --context "$context" --request-timeout=5s cluster-info >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done
  return 1
}

node="${name}-control-plane"
node_state="$(docker inspect -f '{{.State.Status}}' "$node" 2>/dev/null || true)"

if [[ "$node_state" == "exited" || "$node_state" == "created" || "$node_state" == "paused" ]]; then
  echo "Cluster ${name} is stopped. Starting the existing container."
  docker start "$node" >/dev/null
elif [[ -z "$node_state" ]]; then
  echo "Cluster ${name} does not exist. Creating it."
  node_image="$(awk '/^[[:space:]]*image:/ { print $2; exit }' "$config")"
  if [[ -n "$node_image" ]]; then
    docker pull "$node_image"
  fi
  "$kind_bin" create cluster --name "$name" --config "$config"
  bash "${root}/scripts/install-metrics-server.sh" "$context"
fi

if ! wait_for_api; then
  echo "Cluster ${name} did not respond." >&2
  exit 1
fi

k8s_version="$(kubectl --context "$context" version -o json | sed -n 's/.*"gitVersion": "\([^"]*\)".*/\1/p' | tail -n 1)"
if [[ -n "$k8s_version" ]]; then
  container_image="$(docker inspect -f '{{.Image}}' "$node")"
  container_image="${container_image#sha256:}"
  container_image="${container_image:0:12}"
  none_id="$(docker images kindest/node --format '{{.ID}} {{.Tag}}' | awk '$2 == "<none>" { print $1; exit }')"
  extra_ids="$(docker images kindest/node --format '{{.ID}} {{.Tag}}' | awk -v keep="$container_image" -v none="$none_id" '$1 != keep && $1 != none { print $1 }' | sort -u)"
  if [[ -n "$none_id" ]]; then
    docker tag "$none_id" "kindest/node:${k8s_version}"
  elif ! docker images kindest/node --format '{{.Tag}}' | grep -qx "$k8s_version"; then
    docker tag "$container_image" "kindest/node:${k8s_version}"
  fi
  if [[ -n "$extra_ids" ]]; then
    while read -r id; do
      [[ -z "$id" ]] && continue
      docker rmi "$id" >/dev/null 2>&1 || true
    done <<< "$extra_ids"
  fi
  echo "Kind node: kindest/node:${k8s_version}"
fi

kubectl apply -k "${root}/k8s" --context "$context"
kubectl --context "$context" -n auto-repair-shop rollout status deployment/api --timeout=300s
echo "Published API: http://localhost:30080"
echo "Mailpit: http://localhost:30025"
