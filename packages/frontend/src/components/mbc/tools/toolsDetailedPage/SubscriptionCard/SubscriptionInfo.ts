import { Envs } from "globals/Envs";

export const SubscriptionDetails = [
    {
        "id": "powerPlatform",
        "name": "Power Platform",
        "tiles":[
            {
                "id": "default-environment",
                "title": "Default Environment",
                "subTitle": "New here? Get your first experience",
                "icon": "codespace",
                "info":{
                    "feature" : "Default environment",
                    "classification": "Confidential",
                    "cost": "0.2 FTE /Month"
                },
                "link": `${Envs.POWER_PLATFORM_DEFAULT_ENVIRONMENT_URL}`
            },
            {
                "id": "citizen-developer-account",
                "title": "Citizen Developer Account",
                "subTitle": "Are you ready to build the application?",
                "icon": "profile",
                "info":{
                    "feature" : "Citizen Developer Account",
                    "classification": "Confidential",
                    "cost": "0.2 FTE /Month"
                },
                "link": `${Envs.POWER_PLATFORM_CITIZEN_DEVELOPER_ACCOUNT_URL}`
            },
            {
                "id": "shared-development-account",
                "title": "Shared Development Account",
                "subTitle": "Want to use premium features?",
                "icon": "trino_shared-folder",
                "info":{
                    "feature" : "Shared Development Account",
                    "classification": "Confidential",
                    "cost": "0.2 FTE /Month"
                },
                "link":``
            },
            {
                "id": "full-development-account",
                "title": "Full Development Account",
                "subTitle": "All features for teams",
                "icon": "localcomplianceofficer",
                "info":{
                    "feature" : "Full Development Account",
                    "classification": "Confidential",
                    "cost": "0.2 FTE /Month"
                },
                "link": `${Envs.POWER_PLATFORM_FULL_DEVELOPER_ACCOUNT_URL}`
            }
        ]
    }
]