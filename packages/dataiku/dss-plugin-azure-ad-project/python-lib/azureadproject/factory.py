from dataikuapi.utils import DataikuException

class ADFactory:
    PROJECT_GROUP_MATRIX = (
        ('ADMINISTRATOR', {'admin': True}),
        ('CONTRIBUTOR', {'writeProjectContent': True, 'exportDatasetsData': True}),
        ('READ-ONLY', {'readProjectContent': True})
    )

    NO_PERMISSIONS_TEMPLATE = {
        'admin': False,
        'readProjectContent': False,
        'writeProjectContent': False,
        'exportDatasetsData': False,
        'readDashboards': False,
        'writeDashboards': False,
        'moderateDashboards': False,
        'runScenarios': False,
        'manageDashboardAuthorizations': False,
        'manageExposedElements': False,
        'manageAdditionalDashboardUsers': False,
        'executeApp': False
    }

    INSTANCE_PERMISSIONS_MATRIX = dict((
        ('CONTRIBUTOR', {
                    "admin": False,
                    "mayCreateProjects": False,
                    "mayCreateProjectsFromMacros": False,
                    "mayCreateProjectsFromTemplates": False,
                    "mayCreateProjectsFromDataikuApps": False,
                    "mayWriteUnsafeCode": False,
                    "mayCreateAuthenticatedConnections": False,
                    "mayManageCodeEnvs": True,
                    "mayCreateCodeEnvs": True,
                    "mayWriteSafeCode": True,
                    "mayDevelopPlugins": False,
                    "mayEditLibFolders": False,
                    "mayWriteInRootProjectFolder": False,
                    "mayCreatePublishedProjects": False
                }),
        ('ADMINISTRATOR', {
                    "admin": False,
                    "mayCreateProjects": False,
                    "mayCreateProjectsFromMacros": False,
                    "mayCreateProjectsFromTemplates": False,
                    "mayCreateProjectsFromDataikuApps": False,
                    "mayWriteUnsafeCode": False,
                    "mayCreateAuthenticatedConnections": False,
                    "mayManageCodeEnvs": True,
                    "mayCreateCodeEnvs": True,
                    "mayWriteSafeCode": True,
                    "mayDevelopPlugins": False,
                    "mayEditLibFolders": False,
                    "mayWriteInRootProjectFolder": False,
                    "mayCreatePublishedProjects": True
                }),
        ('READ-ONLY', {
                    "admin": False,
                    "mayCreateProjects": False,
                    "mayCreateProjectsFromMacros": False,
                    "mayCreateProjectsFromTemplates": False,
                    "mayCreateProjectsFromDataikuApps": False,
                    "mayWriteUnsafeCode": False,
                    "mayCreateAuthenticatedConnections": False,
                    "mayCreateCodeEnvs": False,
                    "mayWriteSafeCode": False,
                    "mayDevelopPlugins": False,
                    "mayEditLibFolders": False,
                    "mayWriteInRootProjectFolder": False,
                    "mayCreatePublishedProjects": False
                })
    ))

    GROUP_NAME_PATTERN = '{}--{}--{}'

    DATASETS_CONNECTION_NAME_PATTERN = '{}__DATASETS'
    FOLDERS_CONNECTION_NAME_PATTERN = '{}__FOLDERS'
    MAX_LEN_GROUP_NAME = 64

    def __init__(self, instance_id, client, user_name):
        self.instance_id = instance_id
        self.client = client
        self.user_name = user_name

        self.existing_connections = set(self.client.list_connections().keys())

    def _get_no_permissions_template(self, group_name):
        permissions = {'group': group_name}
        permissions.update(self.NO_PERMISSIONS_TEMPLATE)
        return permissions

    def _create_group_if_missing(self, group_name, existing_groups):
        if group_name not in existing_groups:
            self.client.create_group(name=group_name,
                                     description='Added by Azure AD Project',
                                     source_type='LOCAL')

    def create_project(self, project_key, project_name, is_automation_node=False, template_project_key=None):
        if is_automation_node:
            project = self.client.get_project(project_key)

        elif template_project_key:
            target_datasets_connection = self.DATASETS_CONNECTION_NAME_PATTERN.format(project_key)
            target_folders_connection = self.FOLDERS_CONNECTION_NAME_PATTERN.format(project_key)

            connection_remapping = {
                "connections": [
                    {
                        "source": self.DATASETS_CONNECTION_NAME_PATTERN.format(template_project_key),
                        "target": target_datasets_connection
                    },
                    {
                        "source": self.FOLDERS_CONNECTION_NAME_PATTERN.format(template_project_key),
                        "target": target_folders_connection
                    }
                ]
            }

            template_project = self.client.get_project(template_project_key)
            template_project.duplicate(target_project_key=project_key, target_project_name=project_name,
                                       remapping=connection_remapping)

            project = self.client.get_project(project_key)
        else:
            project = self.client.create_project(project_key, project_name, self.user_name)

        existing_groups = set(group['name'] for group in self.client.list_groups())
        permissions = project.get_permissions()
        permissions['permissions'] = []
        for group_suffix, permissions_diff in self.PROJECT_GROUP_MATRIX:

            # DWGM group names cant contain "_" thats why we need to replace with "-"
            group_name = self.GROUP_NAME_PATTERN.format(self.instance_id, project_key.replace("_", "-"), group_suffix)
            if len(group_name) > self.MAX_LEN_GROUP_NAME:
                raise Exception("group_name must not exceed {} characters.".format(self.MAX_LEN_GROUP_NAME))

            self._create_group_if_missing(group_name, existing_groups)

            dss_group = self.client.get_group(group_name)

            if not dss_group:
                raise Exception("Expected to get a group")
            
            dss_group_permissions = dss_group.get_definition()
            dss_group_permissions.update(self.INSTANCE_PERMISSIONS_MATRIX[group_suffix])
            dss_group.set_definition(dss_group_permissions)

            project_group_permissions = self._get_no_permissions_template(group_name)
            project_group_permissions.update(permissions_diff)

            permissions['permissions'].append(project_group_permissions)

        project.set_permissions(permissions)

    def check_if_project_already_exists(self, project_key):
        try:
            self.client.get_project(project_key).get_permissions()
            raise Exception("The project with project key: {} already exist.".format(project_key))
        except DataikuException:
            pass

    def create_datasets_project_connection(self, project_key):
        connection_name = self.DATASETS_CONNECTION_NAME_PATTERN.format(project_key)
        if connection_name not in self.existing_connections:
            connection = self.client.create_connection(connection_name,
                                                       type='Filesystem',
                                                       usable_by='ALLOWED')
        else:
            connection = self.client.get_connection(connection_name)

        definition = connection.get_definition()
        definition['params']['root'] = '/data/datasets/{}'.format(project_key)
        definition['allowManagedDatasets'] = True
        definition['allowManagedFolders'] = False
        self._use_project_groups_in_connection_definition(definition, project_key)

        connection.set_definition(definition)

    def create_folders_project_connection(self, project_key):
        connection_name = self.FOLDERS_CONNECTION_NAME_PATTERN.format(project_key)
        if connection_name not in self.existing_connections:
            connection = self.client.create_connection(connection_name,
                                                       type='Filesystem',
                                                       usable_by='ALLOWED')
        else:
            connection = self.client.get_connection(connection_name)

        definition = connection.get_definition()
        definition['params']['root'] = '/data/folders/{}'.format(project_key)
        definition['allowManagedDatasets'] = False
        definition['allowManagedFolders'] = True
        self._use_project_groups_in_connection_definition(definition, project_key)

        connection.set_definition(definition)

    def _use_project_groups_in_connection_definition(self, definition, project_key):

        mapped_project_key = project_key.replace("_", "-")

        admin_group = self.GROUP_NAME_PATTERN.format(self.instance_id, mapped_project_key, 'ADMINISTRATOR')
        contributor_group = self.GROUP_NAME_PATTERN.format(self.instance_id, mapped_project_key, 'CONTRIBUTOR')
        read_only_group = self.GROUP_NAME_PATTERN.format(self.instance_id, mapped_project_key, 'READ-ONLY')

        definition['allowedGroups'] = [admin_group, contributor_group, read_only_group]
        definition['detailsReadability'] = {
            'readableBy': 'ALLOWED',
            'allowedGroups': [admin_group, contributor_group, read_only_group]
        }
