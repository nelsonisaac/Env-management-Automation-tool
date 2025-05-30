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
  env = var.env_vars
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

variable "management_port" {
  type = number
}

ports {
  internal = 9990
  external = var.management_port
}