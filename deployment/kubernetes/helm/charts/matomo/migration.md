# Matomo Migration Guide

This guide provides the steps to migrate Matomo from one instance to another, including the migration of the Persistent Volume Claim (PVC) which holds the plugin configuration.

## Prerequisites

- Access to the VM where the NFS file share is mounted.

## Migration Steps

### 1. Copy Matomo PVC

1. **Login to the VM:**
   - SSH into the VM where the NFS file share is mounted.

2. **Copy the PVC contents:**
   - Copy the contents from the source PVC to the destination PVC. Use the following command, replacing `<source_pvc_path>` and `<destination_pvc_path>` with the actual paths:

     ```sh
     cp -r <source_pvc_path>/* <destination_pvc_path>/
     ```

### 2. Set Correct Ownership

1. **Ensure the data owner is `1001`:**
   - Matomo runs with the user ID `1001`. Ensure that the copied data has the correct ownership by running:

     ```sh
     chown -R 1001:1001 <destination_pvc_path>
     ```

### 3. Update Configuration

1. **Check and update `config.ini.php`:**
   - The `config.ini.php` file holds important configuration information, including the MySQL database hostname. Open this file and update the necessary details to match the new instance:

     ```sh
     vi <destination_pvc_path>/config/config.ini.php
     ```

   - Update the MySQL database hostname and any other configuration settings as needed.

## Notes

- Ensure that the data owner is set to `1001` to avoid permission issues.
- Double-check the `config.ini.php` file to ensure all settings are correct for the new instance.