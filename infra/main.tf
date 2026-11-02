terraform {
  required_version = ">= 1.6.0"

  required_providers {
    kind = {
      source  = "tehcyx/kind"
      version = "0.13.0"
    }
    null = {
      source  = "hashicorp/null"
      version = "3.2.4"
    }
  }

  backend "s3" {
    bucket  = "fiap-group123-auto-repair-shop-tfstate"
    key     = "auto-repair-shop/terraform.tfstate"
    region  = "us-east-1"
    encrypt = true
  }
}

provider "kind" {}

resource "kind_cluster" "auto_repair_shop" {
  name           = var.cluster_name
  wait_for_ready = true

  kind_config {
    kind        = "Cluster"
    api_version = "kind.x-k8s.io/v1alpha4"

    node {
      role = "control-plane"

      extra_port_mappings {
        container_port = 30080
        host_port      = var.api_host_port
        protocol       = "TCP"
      }

      extra_port_mappings {
        container_port = 30025
        host_port      = var.mailpit_host_port
        protocol       = "TCP"
      }
    }
  }
}
