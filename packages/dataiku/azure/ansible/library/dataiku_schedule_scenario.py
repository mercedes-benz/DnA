#!/usr/bin/python
import json
import logging

import dataikuapi
from ansible.module_utils.basic import *

logging.basicConfig(format='%(levelname)s:%(message)s', level=logging.INFO)


def main():
    fields = {
        'project_key': {'required': True, 'type': 'str'},
        'as_user': {'required': True, 'type': 'str'},
        'api_host': {'required': True, 'type': 'str'},
        'api_key': {'required': True, 'type': 'str'},
        'scenario_params_json': {'required': True, 'type': 'dict'}
    }

    module = AnsibleModule(argument_spec=fields)

    dss_client = dataikuapi.dssclient.DSSClient(module.params['api_host'],
                                                api_key=module.params['api_key'])

    project = dss_client.get_project(module.params['project_key'])

    scenario_name = module.params['scenario_params_json']['name']

    scenarios = project.list_scenarios()

    scenario_names = [s["name"] for s in scenarios]


    if scenario_name in scenario_names:
        logging.info("Scenario {} already exists on the instance. Updating.".format(scenario_name))

        scenario = project.get_scenario(scenario_name)
        scenario.delete()

        scenario = project.create_scenario(scenario_name, 'step_based',
                                               definition=module.params['scenario_params_json'])
    else:
        logging.info("Scenario {} doesnt exist exists on the instance. Creating.".format(scenario_name))

        scenario = project.create_scenario(scenario_name, 'step_based',
                                               definition=module.params['scenario_params_json'])

    module.exit_json(changed=True)


if __name__ == '__main__':
    main()
