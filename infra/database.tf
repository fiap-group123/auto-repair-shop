resource "null_resource" "metrics_server" {
  depends_on = [kind_cluster.auto_repair_shop]

  triggers = {
    cluster = kind_cluster.auto_repair_shop.endpoint
  }

  provisioner "local-exec" {
    interpreter = ["bash", "-c"]
    working_dir = path.module
    command     = "bash ../scripts/install-metrics-server.sh ${kind_cluster.auto_repair_shop.context_name}"
  }
}

resource "null_resource" "database" {
  depends_on = [kind_cluster.auto_repair_shop]

  triggers = {
    namespace = filesha256("${path.module}/../k8s/base/namespace.yaml")
    config    = filesha256("${path.module}/../k8s/base/configmap.yaml")
    secret    = filesha256("${path.module}/../k8s/base/secret.yaml")
    postgres  = filesha256("${path.module}/../k8s/base/postgres.yaml")
    kustom    = filesha256("${path.module}/../k8s/base/kustomization.yaml")
  }

  provisioner "local-exec" {
    interpreter = ["bash", "-c"]
    working_dir = path.module
    command     = <<-EOT
      set -euo pipefail
      context="${kind_cluster.auto_repair_shop.context_name}"
      for attempt in 1 2 3 4 5 6 7 8 9 10; do
        if kubectl apply -k ../k8s/base --context "$context"; then
          exit 0
        fi
        sleep 5
      done
      exit 1
    EOT
  }
}
