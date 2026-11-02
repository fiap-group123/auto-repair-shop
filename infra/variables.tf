variable "cluster_name" {
  description = "Name of the local kind cluster."
  type        = string
  default     = "autorepairshop"
}

variable "api_host_port" {
  description = "Host port published to the API NodePort (30080)."
  type        = number
  default     = 8080
}

variable "mailpit_host_port" {
  description = "Host port published to the Mailpit UI NodePort (30025)."
  type        = number
  default     = 8025
}
