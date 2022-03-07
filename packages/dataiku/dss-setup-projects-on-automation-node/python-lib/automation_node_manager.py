import dataiku
import dataikuapi
import datetime
from dataiku.runnables import utils
from dataikuapi.utils import DataikuException
from dataiku.runnables import ResultTable

INFRASTRUCTURE_PERMISSIONS_MATRIX = (
    ('ADMINISTRATOR', {'read': True, 'deploy': True, 'admin': False}),
    ('CONTRIBUTOR', {'read': True, 'deploy': False, 'admin': False}),
    ('READ-ONLY', {'read': True, 'deploy': False, 'admin': False})
)

PROJECT_PERMISSIONS_MATRIX = (
    ('ADMINISTRATOR', {'read': True, 'write': True,'deploy': True, 'admin': False}),
    ('CONTRIBUTOR', {'read': True, 'write': True, 'deploy': False, 'admin': False}),
    ('READ-ONLY', {'read': True, 'write': False, 'deploy': False, 'admin': False})
)

GROUP_NAME_PATTERN = '{}--{}--{}'


class AutomationNodeManager:

    def __init__(self, plugin_config, project_key):
        # impersonate admin user
        client = dataikuapi.DSSClient(plugin_config["dssHost"], plugin_config["dssApiKey"])
        admin_client = client.get_user("admin")
        self.design_client = admin_client.get_client_as()
        self.project_deployer = self.design_client.get_projectdeployer()
        self.group_prefix = plugin_config["group_prefix"]
        self.project_key = project_key
        self.run_user = "admin"
        self.logs = []
        self.automation_node_url = ""
        self.automation_node_admin_key = ""

    def update_permissions(self, permissions_list, permissions_matrix):
        # copy the list
        updated_permissions = permissions_list[:]
        for group_suffix, permissions in permissions_matrix:
            group_name = GROUP_NAME_PATTERN.format(self.group_prefix, self.project_key.replace("_", "-"), group_suffix)
            group_permissions = {"group": group_name}
            group_permissions.update(permissions)
            updated_permissions.append(group_permissions)
        return updated_permissions

    def get_automation_node_client(self, infrastructure_name):
        infrastructure = self.project_deployer.get_infra(infra_id=infrastructure_name)
        infrastructure_settings = infrastructure.get_settings()
        raw_settings = infrastructure_settings.get_raw()
        # grant permissions to infrastructure for project groups
        permissions = raw_settings.get("permissions")
        updated_permissions = self.update_permissions(permissions, INFRASTRUCTURE_PERMISSIONS_MATRIX)
        raw_settings["permissions"] = updated_permissions
        infrastructure_settings.save()
        # get the necessary settings for initializing automation node client
        self.automation_node_url = raw_settings["automationNodeUrl"]
        self.automation_node_admin_key = raw_settings["adminApiKey"]
        # initialize automation node client
        automation_node_client = dataikuapi.DSSClient(self.automation_node_url, self.automation_node_admin_key)
        return automation_node_client

    def create_and_publish_bundle(self, bundle_id):
        project_handle = self.design_client.get_project(self.project_key)
        project_handle.export_bundle(bundle_id)
        publish_info = project_handle.publish_bundle(bundle_id)
        # grant permissions to published project
        published_project_handle = self.project_deployer.get_project(self.project_key)
        project_settings = published_project_handle.get_settings()
        raw_settings = project_settings.get_raw()
        permissions = raw_settings.get("permissions")
        updated_permissions = self.update_permissions(permissions, PROJECT_PERMISSIONS_MATRIX)
        raw_settings["permissions"] = updated_permissions
        project_settings.save()
        self.add_log("Publish info: {}".format(publish_info))

    def deploy_bundle(self, infra_name, bundle_id):
        deployment_id = self.create_deployment_id(infra_name)
        deployment = self.project_deployer.create_deployment(deployment_id, self.project_key, infra_name, bundle_id)
        future = deployment.start_update()
        future.wait_for_result()
        self.add_log("Deployment: {}, for bundle: {}, was performed on automation node: {}".format(
            deployment_id, bundle_id, infra_name))

    def trigger_scenario(self, scenario_handle):
        try:
            self.add_log("Scenario triggered on automation node")
            scenario_handle.run_and_wait()
            self.add_log("The deployed project can be found here: {}projects/{}/".format(
                self.automation_node_url, self.project_key))

        except DataikuException:
            last_run = scenario_handle.get_last_finished_run()
            last_run_details = last_run.get_details()
            raise Exception("Scenario failed. Reason: {}".format(last_run_details.first_error_details["message"]))

    def create_resulttable(self):
        """
        Transforms the log dataframe into a ResultTable.
        """
        result_table = ResultTable()
        result_table.add_column("Date", "DATE", "STRING")
        result_table.add_column("User", "USER", "STRING")
        result_table.add_column("Type", "TYPE", "STRING")
        result_table.add_column("Message", "MESSAGE", "STRING")

        for log in self.logs:
            result_table.add_record([
                log.get('date'),
                log.get('user'),
                log.get('type'),
                log.get('message')
            ])

        return result_table

    def add_log(self, message, log_type="INFO"):
        """
        Add a record to the logging dataframe.

        :param message: The text to be logged
        :param log_type: The message type, 'INFO' by default.
        """
        new_log = {
            "date": str(datetime.datetime.now()),
            "user": self.run_user,
            "type": log_type,
            "message": message,
        }
        self.logs.append(new_log)

    def create_deployment_id(self, infra_name):
        return self.project_key + '-on-' + infra_name + '-init'

    def update_scenario_settings(self, scenario_settings):
        raw_scenario_settings = scenario_settings.get_raw()
        project_name = self.project_key.lower()
        raw_scenario_settings["params"]["steps"][0]["params"]["config"]["projectName"] = project_name
        scenario_settings.save()

