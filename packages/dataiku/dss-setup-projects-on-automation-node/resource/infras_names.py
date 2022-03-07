import dataikuapi


def get_infras(project_deployer_handle):
    """
    returns
    [
        {
            "infraBasicInfo": "",
            "deployments": "",
            "isAdmin": "",
            "canDeploy": ""
        }
    ]
    """
    return project_deployer_handle.list_infras(as_objects=False)


def do(payload, config, plugin_config, inputs):
    client = dataikuapi.DSSClient(plugin_config["dssHost"], plugin_config["dssApiKey"])
    project_deployer = client.get_projectdeployer()
    if payload.get('parameterName') == 'infraName':
        choices = [
            {'value': infra['infraBasicInfo']['id'], 'label': infra['infraBasicInfo']['id']}
            for infra in get_infras(project_deployer)
        ]
    else:
        choices = []

    return {'choices': choices}
