data "azurerm_resource_group" "resource_group" {
  name = var.resource_group_name
}

resource "azurerm_public_ip" "vm_public_ip" {
  resource_group_name = data.azurerm_resource_group.resource_group.name
  location = data.azurerm_resource_group.resource_group.location

  name = "${var.resource_group_name}-publicIP"
  allocation_method = "Static"

  domain_name_label = data.azurerm_resource_group.resource_group.name
}

resource "azurerm_network_interface" "main" {
  resource_group_name = data.azurerm_resource_group.resource_group.name
  location = data.azurerm_resource_group.resource_group.location

  name                = "${data.azurerm_resource_group.resource_group.name}-nic"

  ip_configuration {
    name                          = "NetworkInterface"
    subnet_id                     = var.vm_network_interface_subnet_id
    private_ip_address_allocation = "Dynamic"

    public_ip_address_id = azurerm_public_ip.vm_public_ip.id
  }
}

resource "azurerm_virtual_machine" "main" {
  resource_group_name = data.azurerm_resource_group.resource_group.name
  location = data.azurerm_resource_group.resource_group.location

  name                = "${data.azurerm_resource_group.resource_group.name}-vm"

  network_interface_ids = [azurerm_network_interface.main.id]
  vm_size               = var.vm_size

  delete_os_disk_on_termination = true
  delete_data_disks_on_termination = true

  storage_image_reference {
    publisher = "Canonical"
    offer     = "UbuntuServer"
    sku       = var.vm_ubuntu_version
    version   = "latest"
  }
  storage_os_disk {
    name              = "${data.azurerm_resource_group.resource_group.name}-os"
    caching           = "ReadWrite"
    create_option     = "FromImage"
    managed_disk_type = "Standard_LRS"
  }
  os_profile {
    computer_name  = var.hostname
    admin_username = var.vm_root_user
  }

  os_profile_linux_config {
    disable_password_authentication = true
    ssh_keys {
      path = "/home/${var.vm_root_user}/.ssh/authorized_keys"
      key_data = file("${var.vm_ssh_key}.pub")
    }
  }
  tags = {
    environment = data.azurerm_resource_group.resource_group.name
  }
}

//resource "azurerm_network_security_group" "ssh" {
//  resource_group_name = data.azurerm_resource_group.resource_group.name
//  location            = data.azurerm_resource_group.resource_group.location
//
//  name                = "SSHAccessFromAnywhere"
//
//  security_rule {
//    name                       = "SSH"
//    priority                   = 100
//    direction                  = "Inbound"
//    access                     = "Allow"
//    protocol                   = "Tcp"
//    source_port_range          = "22"
//    destination_port_range     = "22"
//    source_address_prefix      = "*"
//    destination_address_prefix = "*"
//  }
//}

# Regular provisioner would be launched only once during the instance creation
# Updates to the instance via Ansible playbook are expected, so that's a workaround
resource "null_resource" "dataiku_provisioning" {
  provisioner "local-exec" {
    command = "ansible-playbook ansible/playbook.yml -i ${var.vm_root_user}@${azurerm_public_ip.vm_public_ip.ip_address}, --private-key ${var.vm_ssh_key}"
  }

  triggers = {
    always_run = timestamp()
  }

  depends_on = [azurerm_virtual_machine.main]
}