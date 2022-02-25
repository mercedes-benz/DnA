#!/usr/bin/python

import logging

import dataikuapi
from ansible.module_utils.basic import *

logging.basicConfig(format='%(levelname)s:%(message)s', level=logging.INFO)

INSTANCE_GROUP_MATRIX = {'ADMINISTRATOR': {'admin': True},
    'POWER-USER': {"admin": False,
                    "mayCreateProjects": True,
                    "mayCreateProjectsFromMacros": True,
                    "mayCreateProjectsFromTemplates": True,
                    "mayCreateProjectsFromDataikuApps": True,
                    "mayWriteUnsafeCode": True,
                    "mayCreateAuthenticatedConnections": True,
                    "mayCreateCodeEnvs": True,
                    "mayDevelopPlugins": True,
                    "mayEditLibFolders": True}}


NO_PERMISSIONS_TEMPLATE = {
    'description': 'Added by Azure AD Sync',
    'sourceType': 'LOCAL',
    'admin': False,
    'mayManageUDM': False,
    'mayCreateProjects': False,
    'mayCreateProjectsFromMacros': False,
    'mayCreateProjectsFromTemplates': False,
    'mayCreateProjectsFromDataikuApps': False,
    'mayWriteUnsafeCode': False,
    'mayWriteSafeCode': False,
    'mayCreateAuthenticatedConnections': False,
    'mayCreateCodeEnvs': False,
    'mayCreateClusters': False,
    'mayDevelopPlugins': False,
    'mayEditLibFolders': False,
    'mayManageCodeEnvs': False,
    'mayManageClusters': False,
    'mayViewIndexedHiveConnections': False,
    'mayCreatePublishedAPIServices': False,
    'mayWriteInRootProjectFolder': False,
    'mayCreateActiveWebContent': False,
    'canObtainAPITicketFromCookiesForGroupsRegex': False
}

GROUP_NAME_PATTERN = '{}__{}__{}'


def main():
    fields = {
        'api_host': {'required': True, 'type': 'str'},
        'api_key': {'required': True, 'type': 'str'},
        'group_name': {'required': True, 'type': 'str'},
        'description': {'required': True, 'type': 'str'},
        'dss_instance_id': {'required': True, 'type': 'str'}
    }

    module = AnsibleModule(argument_spec=fields)

    dss_client = dataikuapi.dssclient.DSSClient(module.params['api_host'],
                                                api_key=module.params['api_key'])
    ## Check existing groups
    existing_groups = set(group['name'] for group in dss_client.list_groups())

    ### Check permissions diff for Power user and admin

    # Check and grant correct permissions for Admin and Power User groups only:
    for group_name in existing_groups:
            dss_group = dss_client.get_group(group_name)
            group_permissions = dss_group.get_definition()

            if "ADMINISTRATOR" in group_name:
                group_permissions.update(INSTANCE_GROUP_MATRIX['ADMINISTRATOR'])

            if "POWER-USER" in group_name:
                group_permissions.update(INSTANCE_GROUP_MATRIX['POWER-USER'])

            dss_group.set_definition(group_permissions)

    ### Create additional instance level groups if required
    if module.params['group_name'] in existing_groups:
        logging.info("Group {} already exists on the instance.".format(module.params['group_name']))

    else:
        instance_level_group = dss_client.create_group(module.params['group_name'])
        instance_level_group_permissions = instance_level_group.get_definition()
        instance_level_group_permissions.update({"admin": False,
                                                 "mayCreateProjects": True,
                                                 "mayCreateProjectsFromMacros": True,
                                                 "mayCreateProjectsFromTemplates": True,
                                                 "mayCreateProjectsFromDataikuApps": True,
                                                 "mayWriteUnsafeCode": True,
                                                 "mayCreateAuthenticatedConnections": True,
                                                 "mayCreateCodeEnvs": True,
                                                 "mayDevelopPlugins": True,
                                                 "mayEditLibFolders": True}
                                                )
        instance_level_group.set_definition(instance_level_group_permissions)

    module.exit_json(changed=True, meta={})


if __name__ == '__main__':
    main()
