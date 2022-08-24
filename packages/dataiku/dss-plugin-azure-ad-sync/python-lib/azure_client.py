import datetime
import logging
import adal
import dataiku
import pandas as pd
import requests
from abc import ABCMeta, abstractmethod
from dataiku.runnables import ResultTable
from dataikuapi.utils import DataikuException

logger = logging.getLogger(__name__)
logging.basicConfig(level=logging.INFO,
                    format='ad plugin %(levelname)s - %(message)s')


class AzureClient:
    __metaclass__ = ABCMeta

    # Relevant URLs
    authority_url = "https://login.microsoftonline.com/"
    graph_url = "https://graph.microsoft.com/"

    graph_group_url = "https://graph.microsoft.com/v1.0/groups?$filter=startsWith(displayName, \'{}\')&$select=id,displayName&$top=500"
    graph_members_url = "https://graph.microsoft.com/v1.0/groups/{}/members?$select=displayName,userPrincipalName&$top=500"

    # Define a translation dict that specifies how each credential should
    # be named in the user's secrets
    credentials_labels = {
        "graph_tenant_id": "Tenant ID",
        "graph_app_id": "Application ID",
        "graph_app_secret": "App secret",
        "graph_app_cert": "App certificate",
        "graph_app_cert_thumb": "App certificate thumbprint",
        "graph_user": "User principal",
        "graph_user_pwd": "User password",
    }
    # remapping due to license profiles names changes and the groups on azure AD already present
    PROFILES_REMAPPING = {
        "DATA-ANALYST": "DESIGNER",
        "DATA-SCIENTIST": "DESIGNER"
    }

    INSTANCE_GROUP_MATRIX = ()

    GROUP_NAME_PATTERN = '{}--{}'

    def __init__(self, project_key, config):
        self.project_key = project_key
        self.azure_ad_connection = config.get("azure_ad_connection", {})
        self.flag_simulate = config.get("flag_simulate")
        self.auth_method = self.azure_ad_connection.get("auth_method")

        self.client = dataiku.api_client()
        self.run_user = self.client.get_auth_info()["authIdentifier"]
        self.session = requests.Session()
        self.group_prefix = self.azure_ad_connection.get("group_prefix")

        # Configure auth method
        self.required_credentials = self.get_required_credentials(
            self.azure_ad_connection.get("auth_method")
        )
        # Read credentials
        if self.azure_ad_connection.get("flag_user_credentials"):
            self.credentials = self.get_credentials("user")
        else:
            self.credentials = self.get_credentials("parameters")

        self.logs = []

        # Connect to Graph API
        self.set_session_headers()

        self.available_dss_profiles = set(self.get_available_dss_profiles())

    @staticmethod
    def list_diff(list1, list2):
        """Return elements of list1 that are not present in list2."""
        return list(set(list1) - set(list2))

    def get_dss_profile(self, user):
        """
        Given a row with user data, extract the profile name from the azure ad profile group
        """
        license_group = user["licence_groups"]

        if license_group and not isinstance(license_group, float):
            # trim the licence group prefix from the profile name
            profile = license_group.replace("{}".format(self.azure_ad_connection["licence_group_prefix"]), "")
            # remap profiles to adjust to new license
            for old_profile, new_profile in self.PROFILES_REMAPPING.items():
                profile = profile.replace(old_profile, new_profile)
            # DWGM does not support group with "_" in them, we use "-" instead. Here we are mapping "-" back to "_" in the profile name
            return profile.replace("-", "_")

        return "NONE"

    def get_available_dss_profiles(self):
        licensing = self.client.get_licensing_status()
        user_profiles = licensing.get('base', []).get('userProfiles', [])
        user_profiles.append("NONE")

        self.add_log("Available profiles: {}".format(user_profiles))

        return user_profiles

    @staticmethod
    def get_required_credentials(auth_method):
        """Determine which credentials are required, based on the authentication method.

        :param auth_method: the selected authentication method
        """
        required_credentials = ["graph_tenant_id", "graph_app_id"]

        if auth_method == "auth_app_token":
            required_credentials.extend(["graph_app_secret"])
        elif auth_method == "auth_app_cert":
            required_credentials.extend(["graph_app_cert", "graph_app_cert_thumb"])
        elif auth_method == "auth_user_pwd":
            required_credentials.extend(["graph_user", "graph_user_pwd"])
        return required_credentials

    def get_credentials(self, source):
        """
        Returns a dictionary containing credentials for ADAL call to MS Graph.

        :param source: where the credentials are taken from, either 'user' or 'parameters'
        """
        # Empty list for missing credentials
        missing_credentials = []
        # Dictionary for present credentials
        credentials = {}

        if source == "user":
            # Load secrets from user profile [{key: value} ...]
            user_secrets = self.client.get_auth_info(with_secrets=True)["secrets"]
            secrets_dict = {secret["key"]: secret["value"] for secret in user_secrets}
        else:
            secrets_dict = self.azure_ad_connection
        # get token = secrets_dict.get("azure_ad_credentials")
        # For each required credential, check whether it is present
        for key in self.required_credentials:
            label = self.credentials_labels[key]
            try:
                if source == "user":
                    credentials[key] = secrets_dict[label]
                else:  # source == "parameters":
                    credentials[key] = secrets_dict[key]
                if not credentials[key]:
                    raise KeyError
            except (KeyError, IndexError):
                missing_credentials.append(label)
        if missing_credentials:
            raise KeyError("Please specify these credentials: {}".format(missing_credentials))
        return credentials

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

        logger.log(logging.getLevelName(log_type), message)

        self.logs.append(new_log)

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

    def set_session_headers(self):
        """
        Starts an ADAL session with Microsoft Graph.
        """
        auth_context = adal.AuthenticationContext(
            self.authority_url + self.credentials["graph_tenant_id"], api_version=None
        )

        if self.auth_method == "auth_app_token":
            token_response = auth_context.acquire_token_with_client_credentials(
                self.graph_url,
                self.credentials["graph_app_id"],
                self.credentials["graph_app_secret"],
            )
        elif self.auth_method == "auth_app_cert":
            token_response = auth_context.acquire_token_with_client_certificate(
                self.graph_url,
                self.credentials["graph_app_id"],
                self.credentials["graph_app_cert"],
                self.credentials["graph_app_cert_thumb"],
            )
        elif self.auth_method == "auth_user_pwd":
            token_response = auth_context.acquire_token_with_username_password(
                self.graph_url,
                self.credentials["graph_user"],
                self.credentials["graph_user_pwd"],
                self.credentials["graph_app_id"],
            )
        else:
            raise Exception("Invalid authentication method")
        self.session.headers.update(
            {"Authorization": 'Bearer {}'.format(token_response["accessToken"])}
        )

    def query_groups_by_group_prefix(self, group_prefix):
        """
        returns
        [
            {
                "id": "some-uuid",
                "displayName": "INSTANCE_PREFIX__ROLE__PROJECT_NAME"
            }
        ]
        """
        try:
            query_url = self.graph_group_url.format(group_prefix)
            query_result = self.session.get(query_url)
            query_result = query_result.json()
            if "value" in query_result:
                query_result = query_result["value"]
                if query_result:
                    return query_result
                else:
                    self.add_log(
                        "Groups with prefix {} has not been found in AAD".format(group_prefix), "WARNING",
                    )
            elif "error" in query_result:
                raise Exception(query_result["error"].get("message"))
        except Exception as e:
            self.add_log(
                'Error calling Graph API for group prefix "{}: {}'.format(group_prefix, str(e)),
                "WARNING",
            )

    def query_members(self, group_id, group_name):
        """
        Send query to Graph for members of a group, by ID.

        :param group_id: the ID of a group in Graph
        :param group_name_dss: DSS group name, returned in result
        :return: a dataframe with 4 columns: display name, email, groups, login
        """
        group_members = pd.DataFrame()

        try:
            query_url = self.graph_members_url.format(group_id)

            while query_url:
                query_result = self.session.get(query_url)

                query_result = query_result.json()
                query_url = query_result.get("@odata.nextLink", "")

                """
                [
                    {
                        '@odata.type': '#microsoft.graph.user',
                        'displayName': 'Tomasz Chojna',
                        'userPrincipalName': 'tomasz.chojna@unit8.co'
                    }
                ]
                """

                group_members = group_members.append(
                    pd.DataFrame(query_result["value"]), ignore_index=True
                )
            if not group_members.empty:
                # we are interested only in two columns: displayName and userPrincipalName
                group_members = group_members[['displayName', 'userPrincipalName']]
                group_members = group_members.rename(columns={'userPrincipalName': 'email'})
                group_members["groups"] = group_name
                group_members["login"] = group_members["email"]
            else:
                self.add_log("Group '{}' has no members in AAD".format(group_name))

            return group_members
        except Exception as e:
            self.add_log(
                'Group "{}" members cannot be retrieved from AAD: {}'.format(group_name, str(e)),
                "WARNING",
            )

    def user_create(self, user_id, display_name, email, groups, user_dss_profile):
        """
        Create a new DSS user.

        The parameters are taken from the parameters of dataiku.client.create_user.
        """
        if user_dss_profile == "NONE":
            self.add_log(
                'User "{}" will not be created, since he has no dss_profile.'.format(user_id)
            )
            return
        if self.flag_simulate:
            self.add_log(
                'User "{}" will be created and assigned groups "{}" with the "{}" profile'.format(user_id, groups,
                                                                                                  user_dss_profile)
            )
            return
        # Create the user in DSS
        user = self.client.create_user(
            login=user_id,
            display_name=display_name,
            groups=list(groups),
            password="",
            source_type="LOCAL_NO_AUTH",
            profile=user_dss_profile,
        )

        # Request and alter the user definition to set the e-mail address
        user_def = user.get_definition()
        user_def["email"] = email
        user.set_definition(user_def)

        self.add_log(
            'User "{}" has been created and assigned groups "{}" with the "{}" profile'.format(user_id, groups,
                                                                                               user_dss_profile)
        )

    def user_update(self, user_row, groups, message):
        """
        Update the group membership of a DSS user.

        :param user_row: the user row
        :param message: textual description of the changes
        """
        user_id = user_row["login"]
        if self.flag_simulate:
            self.add_log('User "{}" will be modified: {}'.format(user_id, message))
            return

        # Request and alter the user's definition
        user = self.client.get_user(user_id)
        settings = user.get_settings()

        user_def = settings.get_raw()

        user_def["groups"] = groups
        user_def["userProfile"] = user_row["userProfile"]
        user_def["displayName"] = user_row["displayName_aad"]
        user_def["email"] = user_row["email_aad"]

        settings.save()

        self.add_log('User "{}" has been modified: {}'.format(user_id, message))

    def user_delete(self, user_id, reason):
        """
        Remove an user from DSS
        :param user_id: The user's login
        :param reason: reason for deletion, e.g. "No dss_profile" or "Not found in AAD"
        """
        if self.flag_simulate:
            self.add_log('User "{}" will be deleted. Reason: {}'.format(user_id, reason))
            return
        user = self.client.get_user(user_id)
        user.delete()

        self.add_log('User "{}" has been deleted. Reason: {}'.format(user_id, reason))

    def _create_group_if_missing(self, group_name, existing_groups):
        if group_name not in existing_groups:
            self.client.create_group(name=group_name,
                                     description='Added by Azure AD Sync',
                                     source_type='LOCAL')
            self.add_log(
                'Group "{}" has been created.'.format(group_name)
            )

    def create_missing_groups(self, group_prefix, existing_group_names):
        for group_suffix, permissions in self.INSTANCE_GROUP_MATRIX:
            group_name = self.GROUP_NAME_PATTERN.format(group_prefix, group_suffix)

            self._create_group_if_missing(group_name, existing_group_names)

            dss_group = self.client.get_group(group_name)

            if not dss_group:
                raise Exception("Expected to get a group")

            dss_group_permissions = dss_group.get_definition()
            dss_group_permissions.update(permissions)

            dss_group.set_definition(dss_group_permissions)

    def sync_groups(self):
        if not self.group_prefix:
            raise Exception("Group prefix setting can not be empty!")

        ad_groups = self.query_groups_by_group_prefix(self.group_prefix)
        self.add_log("Detected {} group(s)".format(len(ad_groups)))

        existing_group_names = [group["name"] for group in self.client.list_groups()]

        self.create_missing_groups(self.group_prefix, existing_group_names)

        return ad_groups

    def get_group_members(self, ad_groups):
        # Init empty data frame
        group_members_df = pd.DataFrame(columns=["displayName", "email", "groups", "login"])
        group_members_df['groups'] = group_members_df['groups'].astype(str)

        for ad_group in ad_groups:
            group_id = ad_group['id']
            group_name = ad_group['displayName']

            group_name = group_name[:len(self.group_prefix)] + group_name[len(self.group_prefix):].upper()

            group_members = self.query_members(group_id, group_name)
            group_members_df = group_members_df.append(
                group_members, ignore_index=True
            )

        return group_members_df

    def assert_group_not_empty(self, group_members):
        if group_members.empty:
            raise Exception("There are no group members to synchronize")

    def get_aad_users(self, group_members_df):
        aad_users = (
            group_members_df.sort_values(by=["login", "groups"])
                .groupby(by=["login", "displayName", "email"])["groups"]
                .agg(['unique'])
                .rename(columns={'unique': 'groups'})
                .reset_index()
        )

        return aad_users

    def get_dss_users(self):
        # Read data about groups and users from DSS
        list_users = self.client.list_users()
        dss_users = pd.DataFrame(
            list_users,
            columns=[
                "login",
                "displayName",
                "email",
                "groups",
                "sourceType",
                "userProfile",
            ],
        )
        dss_users["email"] = dss_users["email"].astype(
            object)  # In case of fresh new DSS with one email free admin account
        return dss_users

    def compare_users(self, aad_users, dss_users):
        license_groups = self.get_group_members(
            self.query_groups_by_group_prefix(self.azure_ad_connection["licence_group_prefix"])
        ).rename(columns={"groups": "licence_groups"})
        # check for multiple licenses
        count_license = license_groups.groupby("login").count()
        multiple_license = count_license[count_license["licence_groups"] > 1]
        if multiple_license.empty:
            self.add_log("No multiple license cases found")
        else:
            self.add_log("Multiple license found for: {}".format(multiple_license.index))

        self.add_log("License group is: {}".format(license_groups))
        self.add_log("Prefix for License group is: {}".format(self.azure_ad_connection["licence_group_prefix"]))

        # Create a comparison table between AAD and DSS
        user_comparison = aad_users.merge(
            dss_users,
            how="outer",
            on=["login"],
            suffixes=("_aad", "_dss"),
            indicator=True,
        ).join(license_groups.set_index("login"), how="left", on="login")

        # # Replace NaN with empty lists in the dss_profile column
        # for row in user_comparison.loc[
        #     user_comparison.licence_groups.isnull(), "licence_groups"
        # ].index:
        #     user_comparison.at[row, "dss_profile"] = []

        self.add_log(user_comparison["licence_groups"].to_string())

        return user_comparison

    def update_group_memberships(self, user):
        user_dss_profile = self.get_dss_profile(user)
        log_message = ""

        groups = self.get_aad_groups(user)
        # check for new AD groups
        if set(groups) != set(user["groups_dss"]):
            log_message += " groups {}".format(list(groups))
        if user_dss_profile != user["userProfile"]:
            user["userProfile"] = user_dss_profile
            log_message += " profile {}".format(user_dss_profile)
        if user["displayName_aad"] != user["displayName_dss"]:
            log_message += " display name {}".format(user["displayName_aad"])
        if user["email_aad"] != user["email_dss"]:
            log_message += " email {}".format(user["email_aad"])

        if log_message:
            self.user_update(user_row=user, groups=groups, message=log_message)

    @abstractmethod
    def sync_user(self, user):
        pass

    @abstractmethod
    def get_aad_groups(self, user):
        pass

    @staticmethod
    def is_only_in_aad(user):
        # The _merge column was created by the indicator parameter of pd.merge.
        # It holds data about which sources contain this row.
        return user["_merge"] == "left_only"

    @staticmethod
    def is_only_in_dss(user):
        # The _merge column was created by the indicator parameter of pd.merge.
        # It holds data about which sources contain this row.
        return user["_merge"] == "right_only"

    @staticmethod
    def is_no_auth_user(user):
        return user["sourceType"] == "LOCAL_NO_AUTH"


class ProdEnvironment(AzureClient):
    INSTANCE_GROUP_MATRIX = (
        ('ADMINISTRATOR', {'admin': True}),
        ('POWER-USER', {
            "admin": False,
            "mayCreateProjects": True,
            "mayCreateProjectsFromMacros": True,
            "mayCreateProjectsFromTemplates": True,
            "mayCreateProjectsFromDataikuApps": True,
            "mayWriteUnsafeCode": True,
            "mayCreateAuthenticatedConnections": True,
            "mayCreateCodeEnvs": True,
            "mayDevelopPlugins": True,
            "mayEditLibFolders": True
        }),
        ('AKS--CPULIGHT', {
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
        }),
        ('AKS--GPU', {
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
    )


    def sync_user(self, user):
        user_id = user["login"]
        user_dss_profile = self.get_dss_profile(user)

        # If user only exists in AAD, create the user.
        # The user_create function checks whether the user has a dss_profile.
        if self.is_only_in_aad(user):
            self.user_create(
                user_id=user_id,
                display_name=user["displayName_aad"],
                email=user["email_aad"],
                groups=user["groups_aad"],
                user_dss_profile=user_dss_profile,
            )
            return

        if self.is_only_in_dss(user):
            # The user exists only in DSS as a LOCAL_NO_AUTH account: delete.
            if self.is_no_auth_user(user):
                self.user_delete(user_id, "Not found in AAD.")
            return

        # The user exists in AAD, and in DSS as LOCAL or LDAP type.
        # This is strange, and it is logged as a warning.
        if not self.is_no_auth_user(user):
            self.add_log(
                "User {} has DSS user type {}, while LOCAL_NO_AUTH was expected".format(user_id, user["sourceType"]),
                "WARNING",
            )
            return

        # The user exists in DSS, but its AAD memberships don't grant a dss_profile: delete.
        if user_dss_profile == "NONE":
            self.user_delete(user_id, "No dss_profile.")
            return

        # Compare group memberships in DSS & AAD. If any discrepancies are found: update.
        self.update_group_memberships(user)

    def get_aad_groups(self, user):
        return list(user['groups_aad'])


class TrainingEnvironment(AzureClient):
    INSTANCE_GROUP_MATRIX = (
        ('ADMINISTRATOR', {'admin': True}),
        ('AKS--CPULIGHT', {
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
        }),
        ('AKS--GPU', {
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
        }),
        ('TRAINEE', {
            "admin": False,
            "mayCreateProjects": False,
            "mayCreateProjectsFromMacros": False,
            "mayCreateProjectsFromTemplates": False,
            "mayCreateProjectsFromDataikuApps": False,
            "mayWriteUnsafeCode": True,
            "mayCreateAuthenticatedConnections": True,
            "mayCreateCodeEnvs": True,
            "mayDevelopPlugins": False,
            "mayEditLibFolders": True
        })
    )

    def sync_user(self, user):
        user_id = user["login"]
        user_dss_profile = self.get_dss_profile(user)

        # If user only exists in AAD, create the user.
        # The user_create function checks whether the user has a dss_profile.
        if self.is_only_in_aad(user):
            self.user_create(
                user_id=user_id,
                display_name=user["displayName_aad"],
                email=user["email_aad"],
                groups=user["groups_aad"],
                user_dss_profile=user_dss_profile,
            )
            self.sync_projects_with_user(user)
            return

        if self.is_only_in_dss(user):
            # The user exists only in DSS as a LOCAL_NO_AUTH account: delete.
            if self.is_no_auth_user(user):
                if self.azure_ad_connection.get("del_user_and_training_projects"):
                    self.user_delete(user_id, "Not found in AAD.")
                else:
                    self.add_log('Found user {} to delete (not found in AAD). '
                                 'Flag del_user_and_training_projects unchecked - the user remains'.format(user_id))
                self.delete_user_projects(user)
            return

        # The user exists in AAD, and in DSS as LOCAL or LDAP type.
        # This is strange, and it is logged as a warning.
        if not self.is_no_auth_user(user):
            self.add_log(
                "User {} has DSS user type {}, while LOCAL_NO_AUTH was expected".format(user_id, user["sourceType"]),
                "WARNING",
            )
            return

        # The user exists in DSS, but its AAD memberships don't grant a dss_profile: delete.
        if user_dss_profile == "NONE":
            if self.azure_ad_connection.get("del_user_and_training_projects"):
                self.user_delete(user_id, "No dss profile.")
            else:
                self.add_log('Found user {} to delete (No dss profile). '
                             'Flag del_user_and_training_projects unchecked - the user remains'.format(user_id))
            self.delete_user_projects(user)
            return

        # Compare group memberships in DSS & AAD. If any discrepancies are found: update.
        self.update_group_memberships(user)

        self.sync_projects_with_user(user)

    def get_aad_groups(self, user):
        # trainee is replacing any training project group
        groups = list(set([x if x.split('--')[2] in [self.INSTANCE_GROUP_MATRIX[0][0],
                                                     self.INSTANCE_GROUP_MATRIX[1][0],
                                                     self.INSTANCE_GROUP_MATRIX[2][0]]
                           else x.partition('TRAINING--')[0] + x.partition('TRAINING--')[1] +
                                self.INSTANCE_GROUP_MATRIX[3][0] for x in user['groups_aad']]))
        return groups

    def delete_user_projects(self, user, skip_groups=None):
        ad_groups = self.query_groups_by_group_prefix(self.group_prefix)
        ad_groups = [group['displayName'] for group in ad_groups]

        projects_to_be_deleted = self.get_training_modules_from_groups_name(ad_groups, skip_groups=skip_groups)

        self.add_log('Looking for old projects to delete')

        for project_name in projects_to_be_deleted:
            target_project_key = self.encode_target_project_key(project_name, user['login'])
            project = self.client.get_project(target_project_key)
            try:
                if self.azure_ad_connection.get("del_user_and_training_projects"):
                    project.delete()
                    self.add_log('Found and deleted project {} for user {}'.format(project_name, user['login']))
                else:
                    project.get_permissions()
                    self.add_log('Found project {} to delete for user {}. '
                                 'Flag del_user_and_training_projects unchecked - the project remains'.format(project_name, user['login']))
            except DataikuException:
                self.add_log('Project {} already absent for user {}'.format(project_name, user['login']))

    def sync_projects_with_user(self, user):
        training_modules = self.get_training_modules_from_groups_name(user['groups_aad'])

        # delete the projects that user should not own
        self.delete_user_projects(user, skip_groups=training_modules + ['ADMINISTRATOR'])

        for training_module in training_modules:

            target_project_key = self.encode_target_project_key(training_module, user['login'])

            # check if the template for the project exist
            template_target_key = training_module + '_TEMPLATE'
            try:
                template_project = self.client.get_project(template_target_key)
                template_project.get_permissions()
            except DataikuException:
                self.add_log('Template for the project {} does not exist.'.format(template_target_key), 'WARNING')
                continue

            # check if the project from the existing template was already created for user
            try:
                user_project = self.client.get_project(target_project_key)
                permissions = user_project.get_permissions()

                if permissions['owner'] != user['login']:
                    self.add_log('Project {} already exist but has wrong ownership. Setting owner to {}'.format(
                        training_module, user['login']))
                self.add_log('Project {} already exist for user {}'.format(training_module, user['login']))
            except DataikuException:
                self.add_log("Creating project for user {} with a target key: {}".format(user['login'],
                                                                                         target_project_key))

                project = self.client.get_project(template_target_key)
                metadata = project.get_metadata()
                # keep the same name as in the template
                target_project_name = metadata['label']
                project.duplicate(target_project_key=target_project_key, target_project_name=target_project_name,
                                  duplication_mode='FULL')

                project = self.client.get_project(target_project_key)

                permissions = project.get_permissions()
                permissions['owner'] = user['login']
                project.set_permissions(permissions)

    @staticmethod
    def encode_target_project_key(module_name, login):
        # combine module name with user name extracted from login
        valid_login = login.replace('.', '_').split('@')[0]
        return module_name.upper() + '_' + valid_login.upper()

    @staticmethod
    def get_training_modules_from_groups_name(groups, skip_groups=None):
        skip_groups = skip_groups if skip_groups else ['ADMINISTRATOR']
        return [group.split('--')[2] for group in groups if group.split('--')[2] not in skip_groups]
