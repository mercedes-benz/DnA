#!/usr/bin/python
import json

import dataikuapi
from ansible.module_utils.basic import *


def try_rule_removal(rules_list, raw_rule):
    try:
        rules_list.remove(raw_rule)
    except:
        return


def main():
    fields = {
        'api_host': {'required': True, 'type': 'str'},
        'api_key': {'required': True, 'type': 'str'},
        'datasets_path': {'required': True, 'type': 'str'},
        'folders_path': {'required': True, 'type': 'str'},
    }

    module = AnsibleModule(argument_spec=fields)

    dss_client = dataikuapi.dssclient.DSSClient(module.params['api_host'],
                                                api_key=module.params['api_key'])

    settings = dss_client.get_general_settings()
    raw_settings = settings.get_raw()

    # Do not allow to use default data dir for storing datasets
    raw_settings['defaultDatasetCreationSettings']['allowUploadsWithoutConnection'] = False

    # Default impersonation rules (search for system user with the same name as dss user)
    # takes precedence over project-based rules, so they have to be removed
    impersonation_rules = settings.get_impersonation_rules(scope='GLOBAL')
    for impersonation_rule in impersonation_rules:
        try_rule_removal(raw_settings['impersonation']['userRules'], impersonation_rule.raw)
        try_rule_removal(raw_settings['impersonation']['groupRules'], impersonation_rule.raw)

    settings.save()

    # Clear default connections not pointing to dataset/folder directory (modified from default)
    default_connections = [
        'filesystem_managed',
        'filesystem_folders',
        'filesystem_root'
    ]

    existing_connections = dss_client.list_connections()
    for connection_key in default_connections:
        if connection_key in existing_connections:
            dss_client.get_connection(connection_key).delete()

    module.exit_json(changed=True)


if __name__ == '__main__':
    main()
