# This file is the actual code for the Python runnable prepare-for-deployment
import os
import json
import dataiku
import dataikuapi
from dataiku.runnables import Runnable
from dataiku.runnables import utils
from dataiku.runnables import ResultTable
from dataikuapi.utils import DataikuException
from automation_node_manager import AutomationNodeManager

class MyRunnable(Runnable):
    """The base interface for a Python runnable"""

    def __init__(self, project_key, config, plugin_config):
        """
        :param project_key: the project in which the runnable executes
        :param config: the dict of the configuration of the object
        :param plugin_config: contains the plugin settings
        """
        self.project_key = project_key
        self.config = config
        self.plugin_config = plugin_config
        
    def get_progress_target(self):
        """
        If the runnable will return some progress info, have this function return a tuple of 
        (target, unit) where unit is one of: SIZE, FILES, RECORDS, NONE
        """
        return (4, 'NONE')

    def run(self, progress_callback):
        """
        Do stuff here. Can return a string or raise an exception.
        The progress_callback is a function expecting 1 value: current progress
        """
        # set temporarily the env variable pointing to automation node ssl certificate chain.
        # Can not be set globally as other plugins would fail
        os.environ['REQUESTS_CA_BUNDLE'] = self.plugin_config['aut_node_ssl_chain']

        # Get the identity of the end DSS user
        progress_callback(0)

        automation_node_mgr = AutomationNodeManager(self.plugin_config, self.project_key)

        automation_node_client = automation_node_mgr.get_automation_node_client(self.config["infraName"])

        progress_callback(1)

        automation_node_mgr.create_and_publish_bundle(self.config["bundleId"])

        progress_callback(2)

        automation_node_mgr.deploy_bundle(self.config["infraName"], self.config["bundleId"])

        progress_callback(3)

        admin_macros = automation_node_client.get_project("ADMIN_MACROS")
        run_ad_project = admin_macros.get_scenario("run_ad_project")
        scenario_settings = run_ad_project.get_settings()
        automation_node_mgr.update_scenario_settings(scenario_settings)

        progress_callback(4)

        automation_node_mgr.trigger_scenario(run_ad_project)

        return automation_node_mgr.create_resulttable()
