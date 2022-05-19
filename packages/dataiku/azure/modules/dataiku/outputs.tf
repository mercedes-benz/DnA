output "dataiku_vm_public_ip" {
  value = azurerm_public_ip.vm_public_ip.ip_address
}

output "dataiku_vm_ssh_hostname" {
  value = "${var.vm_root_user}@${azurerm_public_ip.vm_public_ip.ip_address}"
}
