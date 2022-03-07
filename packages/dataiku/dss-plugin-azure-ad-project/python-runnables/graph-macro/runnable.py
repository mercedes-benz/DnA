import dataiku
from dataiku.runnables import Runnable
from dataiku.runnables import utils
import json
from azureadproject.factory import ADFactory
import subprocess


class MyRunnable(Runnable):

    TECHNICAL_USER_PATTERN = 'dataiku__{}'

    def __init__(self, unused, config, plugin_config):
        # Note that, as all macros, it receives a first argument
        # which is normally the project key, but which is irrelevant for project creation macros
        self.config = config
        self.plugin_config = plugin_config

    def get_progress_target(self):
        return 3, "NONE"

    def _create_technical_user(self, project_key):
        user_name = self.TECHNICAL_USER_PATTERN.format(project_key.lower())
        process = subprocess.Popen(['sudo',
                                    self.plugin_config['create_technical_user_script_path'],
                                    user_name],
                                   stdout=subprocess.PIPE,
                                   stderr=subprocess.PIPE)

        stdout, stderr = process.communicate()
        if stderr:
            raise Exception('Creating technical user {} has failed: {}'.format(user_name, stderr))

        client = dataiku.api_client()
        general_settings = client.get_general_settings()
        general_settings.add_impersonation_rule({
            'type': 'REGEXP_RULE',
            'ruleFrom': '(.*)',
            'targetUnix': user_name,
            'scope': 'PROJECT',
            'projectKey': project_key
        })
        general_settings.save()

    def run(self, progress_callback):
        # Get the identity of the end DSS user
        user_client = dataiku.api_client()
        user_auth_info = user_client.get_auth_info()

        # Automatically create a privileged API key and obtain a privileged API client
        # that has administrator privileges.
        admin_client = utils.get_admin_dss_client("creation1", user_auth_info)

        # The project creation macro must create the project. Therefore, it must first assign
        # a unique project key. This helper makes this easy
        project_key = self.config["projectName"].upper()

        instance_id = self.plugin_config.get('group_prefix', '')

        progress_callback(0)
        factory = ADFactory(instance_id, admin_client, user_auth_info['authIdentifier'])
        factory.create_datasets_project_connection(project_key)
        factory.create_folders_project_connection(project_key)

        progress_callback(1)

        is_automation_node = self.plugin_config["is_automation_node"] in ['True', 'true', '1']

        template_project_key = self.config.get('templateProjectKey', None)
        if self.config.get('copyProject', False):
            factory.create_project(project_key, self.config["projectName"], template_project_key=template_project_key)
        else:
            factory.create_project(project_key, self.config["projectName"], is_automation_node)

        progress_callback(2)
        self._create_technical_user(project_key)

        progress_callback(3)

        return json.dumps({"projectKey": project_key})
