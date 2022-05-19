terraform {
  required_providers {
    azurerm = {
      source  = "azurerm"
      version = "=2.40.0"
    }
  }

// TODO: Generate Azure Blob for state file
//  backend "azurerm" {
//    resource_group_name   = "tstate"
//    storage_account_name  = "tstate09762"
//    container_name        = "tstate"
//    key                   = "terraform.tfstate"
//  }
}

provider "azurerm" {
  # TODO: To be discovered from the environment (not clear yet how this will work in production)
  subscription_id = "xxxx"
  features {}
}

locals {
  resource_group_name = "xxxx"
  resource_group_location = "xxxx"
}

variable "vm_ssh_key" {
  default = "~/.ssh/azure"
}

resource "azurerm_resource_group" "main" {
  name     = local.resource_group_name
  location = local.resource_group_location
}

resource "azurerm_virtual_network" "main" {
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location

  name                = "${local.resource_group_name}-network"
  address_space       = ["10.0.0.0/16"]
}

resource "azurerm_subnet" "internal" {
  resource_group_name = azurerm_resource_group.main.name

  name                 = "internal"
  virtual_network_name = azurerm_virtual_network.main.name
  address_prefixes     = ["10.0.2.0/24"]
}

module "dataiku" {
  source = "./modules/dataiku"

  resource_group_name = local.resource_group_name
  resource_group_location = local.resource_group_location

  vm_ubuntu_version = "18.04-LTS"
  vm_root_user = "dataiku-root"
  vm_network_interface_subnet_id = azurerm_subnet.internal.id
  hostname = "dataiku"

  depends_on = [azurerm_resource_group.main]

  vm_size = "Standard_B2ms"

  vm_ssh_key = var.vm_ssh_key
}

output "dataiku_vm_public_ip" {
  value = module.dataiku.dataiku_vm_public_ip
}

output "dataiku_vm_ssh_hostname" {
  value = module.dataiku.dataiku_vm_ssh_hostname
}
