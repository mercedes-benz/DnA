import dataiku

api_client = dataiku.api_client()


def get_projects():
    """
    returns
    [
        {
            "projectKey": "",
            "name": ""
        }
    ]
    """
    return api_client.list_projects()


def do(payload, config, plugin_config, inputs):
    if payload.get('parameterName') == 'templateProjectKey':
        choices = [
            {'value': project['projectKey'], 'label': project['name']}
            for project in get_projects()
        ]
    else:
        choices = []

    return {'choices': choices}
