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
                "info": {
                    "text": "Are you new here? Get your first experience with the Power Platform",
                    "cost": "Free*"
                },
                "link": `${Envs.POWER_PLATFORM_DEFAULT_ENVIRONMENT_URL}`
            },
            {
                "id": "citizen-developer-account",
                "title": "Citizen Developer Account",
                "subTitle": "Are you ready to build the application?",
                "icon": "profile",
                "info": {
                    "text": "Did you already had first experience with Power Platform? And you want to build your first application/flow? Then  you can find information here about the Citizen Developer account",
                    "cost": "starting at 125€ per month*"
                },
                "link": `${Envs.POWER_PLATFORM_CITIZEN_DEVELOPER_ACCOUNT_URL}`
            },
            {
                "id": "shared-development-account",
                "title": "Shared Development Account",
                "subTitle": "Want to use premium features?",
                "icon": "trino_shared-folder",
                "info": {
                    "text": "Did you already develop a solution and want to use Premium features? Find here information about the Shared Development account (Suitable for single person)",
                    "cost": "starting 200€ per month*"
                },
                "link":``
            },
            {
                "id": "full-development-account",
                "title": "Full Development Account",
                "subTitle": "All features for teams",
                "icon": "localcomplianceofficer",
                "info": {
                    "text": "Are you interested in a Full Development account? You can find information here. (Suitable for teams)",
                    "cost": "starting 526€ per month*"
                },
                "link": `${Envs.POWER_PLATFORM_FULL_DEVELOPER_ACCOUNT_URL}`
            }
        ]
    }
]