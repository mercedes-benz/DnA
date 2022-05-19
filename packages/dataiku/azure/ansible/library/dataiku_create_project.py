#!/usr/bin/python

import dataikuapi
from ansible.module_utils.basic import *


def main():
    fields = {
        'key': {'required': True, 'type': 'str'},
        'name': {'required': True, 'type': 'str'},
        'description': {'required': True, 'type': 'str'},
        'api_host': {'required': True, 'type': 'str'},
        'api_key': {'required': True, 'type': 'str'}
    }

    module = AnsibleModule(argument_spec=fields)

    dss_client = dataikuapi.dssclient.DSSClient(module.params['api_host'],
                                                api_key=module.params['api_key'])

    if module.params['key'] in set(dss_client.list_project_keys()):
        module.exit_json(changed=False, meta={})
        return

    dss_client.create_project(module.params['key'], module.params['name'], 'admin',
                              description=module.params['description'])

    module.exit_json(changed=True, meta={})


if __name__ == '__main__':
    main()
