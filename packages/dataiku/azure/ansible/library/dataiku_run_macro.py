#!/usr/bin/python
import json

import dataikuapi
from ansible.module_utils.basic import *


def main():
    fields = {
        'project_key': {'required': True, 'type': 'str'},
        'as_user': {'required': True, 'type': 'str'},
        'macro_id': {'required': True, 'type': 'str'},
        'api_host': {'required': True, 'type': 'str'},
        'api_key': {'required': True, 'type': 'str'},
        'params_json': {'required': True, 'type': 'dict'}
    }

    module = AnsibleModule(argument_spec=fields)

    dss_client = dataikuapi.dssclient.DSSClient(module.params['api_host'],
                                                api_key=module.params['api_key'])

    user = dss_client.get_user(module.params['as_user'])
    client_as_user = user.get_client_as()

    project = client_as_user.get_project(module.params['project_key'])

    found_macro = list(filter(lambda x: x['ownerPluginId'] == module.params['macro_id'],
                              project.list_macros()))

    if not found_macro:
        module.fail_json(msg='Macro with ownerPluginId={} could not be found'.format(module.params['macro_id']))
        return

    macro = project.get_macro(found_macro[0]['runnableType'])
    macro.run(params=module.params['params_json'])

    module.exit_json(changed=True)


if __name__ == '__main__':
    main()
