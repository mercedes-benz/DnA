import importlib
from unittest import mock

# Workaround for directory names with "-"
from unittest.mock import call

factory = importlib.import_module('dss-plugin-azure-ad-project.python-lib.azureadproject.factory')

CREATED_CONNECTION_DEFINITION_MOCK = {
    'params': {
        'forcePathsOnDatasetsImport': True,
        'dkuProperties': [], 'namingRule': {}
    },
    'name': 'CUSTOM_CONNECTION',
    'type': 'Filesystem',
    'creationTag': {
        'versionNumber': 0,
        'lastModifiedBy': {'login': 'api:6VL1KT1PTLWLQ0C8'},
        'lastModifiedOn': 1611051593756
    },
    'allowWrite': True,
    'allowManagedDatasets': True,
    'allowManagedFolders': True,
    'useGlobalProxy': True,
    'maxActivities': 0,
    'customFields': {},
    'credentialsMode': 'GLOBAL',
    'customBasicConnectionCredentialProviderParams': [],
    'usableBy': 'ALLOWED',
    'allowedGroups': [],
    'detailsReadability': {
        'readableBy': 'NONE',
        'allowedGroups': []
    },
    'indexingSettings': {
        'indexIndices': False,
        'indexForeignKeys': False,
        'indexSystemTables': False
    }
}


def assert_permissions(all_permissions, group_name, permissions):
    expected_permissions = {
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

    expected_permissions.update(permissions)
    expected_permissions.update({'group': group_name})

    group_permissions = list(filter(lambda x: x['group'] == group_name, all_permissions))
    assert group_permissions, f"Group '{group_name}' should be added to permissions on project creation"
    assert group_permissions[0] == expected_permissions, f"Group '{group_name}' permissions invalid"


def test_create_project():
    # Given
    project_mock = mock.Mock()
    project_mock.get_permissions.return_value = {
        'owner': 'some_user',
        'permissions': [],
        'additionalDashboardUsers': {
            'users': []
        },
        'dashboardAuthorizations': {
            'allAuthorized': False,
            'authorizations': []
        }
    }

    client_mock = mock.Mock()
    client_mock.list_connections.return_value = {}
    client_mock.create_project.return_value = project_mock

    # One of the groups got synced from AD before creating a project
    client_mock.list_groups.return_value = [{'name': 'DATAIKU_INSTANCE_ID__PROJECT_KEY__ADMINISTRATOR'}]

    # When
    generator = factory.ADFactory('DATAIKU_INSTANCE_ID', client_mock, 'some_user')
    generator.create_project('PROJECT_KEY', 'Some project')

    # Then
    client_mock.create_project.assert_called_with('PROJECT_KEY', 'Some project', 'some_user')

    client_mock.create_group.assert_has_calls([
        call(name='DATAIKU_INSTANCE_ID__PROJECT_KEY__CONTRIBUTOR',
             description='Added by Azure AD Project', source_type='LOCAL'),
        call(name='DATAIKU_INSTANCE_ID__PROJECT_KEY__READ_ONLY',
             description='Added by Azure AD Project', source_type='LOCAL')
    ])

    permissions_payload = project_mock.set_permissions.call_args_list[0][0][0]['permissions']
    assert_permissions(permissions_payload,
                       'DATAIKU_INSTANCE_ID__PROJECT_KEY__ADMINISTRATOR',
                       {'admin': True})

    assert_permissions(permissions_payload,
                       'DATAIKU_INSTANCE_ID__PROJECT_KEY__CONTRIBUTOR',
                       {'writeProjectContent': True, 'exportDatasetsData': True})

    assert_permissions(permissions_payload,
                       'DATAIKU_INSTANCE_ID__PROJECT_KEY__READ_ONLY',
                       {'readProjectContent': True})


def test_create_datasets_project_connection():
    # Given
    connection_mock = mock.Mock()
    connection_mock.get_definition.return_value = CREATED_CONNECTION_DEFINITION_MOCK

    client_mock = mock.Mock()
    client_mock.list_connections.return_value = {}
    client_mock.create_connection.return_value = connection_mock

    # When
    generator = factory.ADFactory('DATAIKU_INSTANCE_ID', client_mock, 'some_user')
    generator.create_datasets_project_connection('PROJECT_KEY')

    # Then
    client_mock.create_connection.assert_called_with(
        'PROJECT_KEY__DATASETS',
        type='Filesystem',
        usable_by='ALLOWED'
    )

    provided_payload = connection_mock.set_definition.call_args[0][0]
    assert provided_payload['params']['root'] == '/data/datasets/PROJECT_KEY'
    assert provided_payload['allowManagedDatasets'] is True
    assert provided_payload['allowManagedFolders'] is False
    assert provided_payload['allowedGroups'] == ['DATAIKU_INSTANCE_ID__PROJECT_KEY__ADMINISTRATOR',
                                                 'DATAIKU_INSTANCE_ID__PROJECT_KEY__CONTRIBUTOR']
    assert provided_payload['detailsReadability']['readableBy'] == 'ALLOWED'
    assert provided_payload['detailsReadability']['allowedGroups'] == ['DATAIKU_INSTANCE_ID__PROJECT_KEY__READ_ONLY']

def test_create_folders_project_connection():
    # Given
    connection_mock = mock.Mock()
    connection_mock.get_definition.return_value = CREATED_CONNECTION_DEFINITION_MOCK

    client_mock = mock.Mock()
    client_mock.list_connections.return_value = {}
    client_mock.create_connection.return_value = connection_mock

    # When
    generator = factory.ADFactory('DATAIKU_INSTANCE_ID', client_mock, 'some_user')
    generator.create_folders_project_connection('PROJECT_KEY')

    # Then
    client_mock.create_connection.assert_called_with(
        'PROJECT_KEY__FOLDERS',
        type='Filesystem',
        usable_by='ALLOWED'
    )

    provided_payload = connection_mock.set_definition.call_args[0][0]
    assert provided_payload['params']['root'] == '/data/folders/PROJECT_KEY'
    assert provided_payload['allowManagedDatasets'] is False
    assert provided_payload['allowManagedFolders'] is True
    assert provided_payload['allowedGroups'] == ['DATAIKU_INSTANCE_ID__PROJECT_KEY__ADMINISTRATOR',
                                                 'DATAIKU_INSTANCE_ID__PROJECT_KEY__CONTRIBUTOR']
    assert provided_payload['detailsReadability']['readableBy'] == 'ALLOWED'
    assert provided_payload['detailsReadability']['allowedGroups'] == ['DATAIKU_INSTANCE_ID__PROJECT_KEY__READ_ONLY']
