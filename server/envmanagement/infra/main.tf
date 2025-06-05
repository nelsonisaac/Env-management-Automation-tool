terraform {
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "~> 3.0"
    }
  }
}

provider "docker" {}

resource "docker_image" "wildfly" {
  name         = "jboss/wildfly:latest"
  keep_locally = true
}

resource "docker_container" "wildfly" {
  image = docker_image.wildfly.name
  name  = var.container_name
  ports {
    internal = 8080
    external = var.port
  }
  ports {
    internal = 9990
    external = 9990
  }
  # Bind interfaces for HTTP and management
  command = ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]

  env = var.env_vars
  healthcheck {
    test     = ["CMD-SHELL", "curl -f http://localhost:8080 || exit 1"]
    interval = "30s"
    timeout  = "10s"
    retries  = 3
    start_period = "10s"
  }
}

variable "container_name" {
  type = string
}

variable "port" {
  type = number
}

variable "env_vars" {
  type = list(string)
}

