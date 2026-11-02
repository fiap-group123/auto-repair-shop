output "context_name" {
  description = "kubectl context created by kind."
  value       = kind_cluster.auto_repair_shop.context_name
}

output "api_url" {
  description = "API published on the host through the kind port mapping."
  value       = "http://localhost:${var.api_host_port}"
}

output "mailpit_url" {
  description = "Mailpit UI published on the host."
  value       = "http://localhost:${var.mailpit_host_port}"
}

output "kubeconfig" {
  description = "Kubeconfig of the kind cluster."
  value       = kind_cluster.auto_repair_shop.kubeconfig
  sensitive   = true
}
