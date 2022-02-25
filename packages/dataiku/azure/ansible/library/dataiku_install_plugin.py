#!/usr/bin/python

import dataikuapi
from ansible.module_utils.basic import *


def main():
    fields = {
        'name': {'required': True, 'type': 'str'},
        'archive_path': {'required': True, 'type': 'str'},
        'api_host': {'required': True, 'type': 'str'},
        'api_key': {'required': True, 'type': 'str'},
        'settings': {'required': False, 'type': 'list'},
        'current_version': {'required': False, 'type': 'str'},
        'latest_version': {'required': False, 'type': 'str'},
        'force_update': {'required': False, 'type': 'bool'}

    }

    module = AnsibleModule(argument_spec=fields)

    dss_client = dataikuapi.dssclient.DSSClient(module.params['api_host'],
                                                api_key=module.params['api_key'])

    plugin = dss_client.get_plugin(module.params['name'])

    current_version = module.params.get('current_version')
    latest_version = module.params.get('latest_version')
    force_update = module.params.get('force_update')

    plugins = [p['id'] for p in dss_client.list_plugins()]
    is_plugin_installed = module.params['name'] in plugins

    is_plugin_updated = current_version and latest_version and current_version != latest_version
    if (is_plugin_updated or force_update) and is_plugin_installed:
        future = plugin.delete(force=True)
        future.wait_for_result()

    try:
        # Plugin already installed
        settings = plugin.get_settings()
    except dataikuapi.utils.DataikuException:
        with open(module.params['archive_path'], 'rb') as f:
            dss_client.install_plugin_from_archive(f)

            future = plugin.create_code_env()
            result = future.wait_for_result()

            settings = plugin.get_settings()
            settings.set_code_env(result["envName"])
            settings.save()

    raw_settings = settings.get_raw()
    for setting in (module.params.get('settings', []) or []):
        raw_settings[setting['key']] = setting['value']
    settings.save()

    module.exit_json(changed=is_plugin_updated, meta={})


if __name__ == '__main__':
    main()
