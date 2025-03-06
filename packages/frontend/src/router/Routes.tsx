import * as React from 'react';
import { Route, Router, Switch } from 'react-router-dom';
import { Envs } from 'globals/Envs';
import Progress from 'components/progress/Progress';
import { ProtectedRoute } from './../decorators/ProtectedRoute';
import { USER_ROLE } from 'globals/constants';
import { history } from './History';
import NotFoundPage from './NotFoundPage';
import { SessionExpired } from './SessionExpired';
import UnAuthorised from './UnAuthorised';

const Administration = React.lazy(() => import('components/mbc/admin/Administration'));
const LoginAuthRedirector = React.lazy(() => import('./LoginAuthRedirector'));
const AllSolutions = React.lazy(() => import('components/mbc/allSolutions/AllSolutions'));
const CreateNewSolution = React.lazy(() => import('components/mbc/createNewSolution/CreateNewSolution'));
// const DssProjectsList = React.lazy(() => import('components/mbc/dataiku/ListProjects'));
const License = React.lazy(() => import('components/mbc/footer/License/License'));
const UsageStatistics = React.lazy(() => import('components/mbc/usageStatistics/usageStatistics'));
const Home = React.lazy(() => import('components/mbc/home/Home'));
const Notebook = React.lazy(() => import('components/mbc/notebook/Notebook'));
const Portfolio = React.lazy(() => import('components/mbc/Portfolio'));
const SearchResults = React.lazy(() => import('components/mbc/searchResults/SearchResults'));
const Summary = React.lazy(() => import('components/mbc/summary/Summary'));
const MalwareScanService = React.lazy(() => import('components/mbc/malwareScanService/MalwareScanService'));
const ModelRegistry = React.lazy(() => import('components/mbc/modelRegistry/ModelRegistry'));
const Notifications = React.lazy(() => import('components/mbc/notification/Notifications'));
const Pipeline = React.lazy(() => import('components/mbc/pipeline/Pipeline'));
const Transparency = React.lazy(() => import('components/mbc/transparency/Transparency'));
const Tools = React.lazy(() => import('components/mbc/tools/Tools'));
const ToolsDetailedPage = React.lazy(() => import('components/mbc/tools/toolsDetailedPage/ToolsDetailedPage'));
const CreateNewPipeline = React.lazy(() => import('components/mbc/pipeline/createNewPipeline/CreateNewPipeline'));
const EditCode = React.lazy(() => import('components/mbc/pipeline/editCode/EditCode'));
const Comingsoon = React.lazy(() => import('components/mbc/comingsoon/Comingsoon'));
const AllReports = React.lazy(() => import('components/mbc/allReports/AllReports'));
const CreateNewReport = React.lazy(() => import('components/mbc/createNewReport/CreateNewReport'));
const ReportSummary = React.lazy(() => import('components/mbc/reportSummary/ReportSummary'));
const UserSettings = React.lazy(() => import('components/mbc/userSettings/userSettings'));
// const CodeSpace = React.lazy(() => import('components/mbc/codeSpace/CodeSpace'));
// const CodeSpaceSecurityConfig = React.lazy(() => import('components/mbc/codeSpace/securityConfig/SecurityConfig'));
// const ManageCodeSpaces = React.lazy(() => import('components/mbc/codeSpace/manageCodeSpace/ManageCodeSpace'));
// const AllCodeSpaces = React.lazy(() => import('components/mbc/codeSpace/AllCodeSpaces'));
const Trainings = React.lazy(() => import('components/mbc/trainings/Trainings'));
const GenAI = React.lazy(() => import('components/mbc/genAI/GenAI'));
// const CodeSpaceRecipe = React.lazy(() => import('components/mbc/codeSpace/codeSpaceRecipe/CodeSpaceRecipe'));
const AzureBlobService = React.lazy(() => import('components/mbc/azureBlobService/AzureBlobService')) ;
const AliceRoleRequest = React.lazy(()=> import('components/mbc/aliceRoleRequest/AliceRoleRequest'))
// Micro Front End Component
const StorageComponent = React.lazy(() => import('storage-mfe/Bucket'));
const DataProductComponent = React.lazy(() => import('data-product-mfe/DataProduct'));
const ChronosComponent = React.lazy(() => import('chronos-mfe/Chronos'));
const DataikuComponent = React.lazy(() => import('dss-mfe/Dataiku'));
const MatomoComponent = React.lazy(() => import('matomo-mfe/Matomo'));
const DatalakeComponent = React.lazy(() => import('datalake-mfe/Datalake'));
const FabricComponent = React.lazy(() => import('fabric-mfe/Fabric'));
const DataEntryComponent = React.lazy(() => import('data-entry-mfe/DataEntry'));
const CodeSpaceComponent = React.lazy(() => import('code-space-mfe/CodeSpace'));
const PowerPlatformComponent = React.lazy(() => import('power-platform-mfe/PowerPlatform'));
const PromptCraftComponent = React.lazy(() => import('components/mbc/promptCraft/promptCraftSubscriptions/PromptCraftSubscriptions'));

const UserAndAdminRole = [
  USER_ROLE.USER,
  USER_ROLE.EXTENDED,
  USER_ROLE.ADMIN,
  USER_ROLE.REPORTADMIN,
  USER_ROLE.DIVISIONADMIN,
  USER_ROLE.DATACOMPLIANCEADMIN,
  USER_ROLE.CODESPACEADMIN,
];
const AdminRole = [USER_ROLE.ADMIN, USER_ROLE.REPORTADMIN, USER_ROLE.CODESPACEADMIN];

const publicRoutes = [
  {
    component: LoginAuthRedirector,
    exact: true,
    path: '/',
  },
  {
    component: SessionExpired,
    exact: true,
    path: '/SessionExpired',
  },
  {
    component: UnAuthorised,
    exact: true,
    path: '/UnAuthorised',
  },
];

const protectedRoutes = [
  {
    allowedRoles: UserAndAdminRole,
    component: Portfolio,
    exact: false,
    path: '/portfolio',
    title: 'Portfolio',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: Home,
    exact: true,
    path: '/home',
    title: 'Home',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: Notebook,
    exact: false,
    path: '/notebook',
    title: 'My Workspace',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: License,
    exact: false,
    path: '/license',
    title: 'License',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: UsageStatistics,
    exact: false,
    path: '/usage-statistics',
    title: 'Usage Statistics',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: Summary,
    exact: false,
    path: '/summary/:id',
    title: 'Solution Summary',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: ReportSummary,
    exact: false,
    path: '/reportsummary/:id',
    title: 'Report Summary',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: MalwareScanService,
    exact: false,
    path: '/malwarescanservice',
    title: 'Malware Scan Service',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: AzureBlobService,
    exact: false,
    path: '/azureBlobService',
    title: 'Azure Blob Service',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: AliceRoleRequest,
    exact: false,
    path: '/aliceRoleRequest',
    title: 'Alice Role Request',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: ModelRegistry,
    exact: false,
    path: '/modelregistry',
    title: 'Model Registry',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: CreateNewSolution,
    exact: false,
    path: '/createnewsolution',
    title: 'Create New Solution',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: CreateNewSolution,
    exact: false,
    path: '/createnewgenaisolution',
    title: 'Create New GenAI Solution',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: CreateNewSolution,
    exact: false,
    path: '/editSolution/:id?/:editable?',
    title: 'Edit Solution',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: CreateNewReport,
    exact: false,
    path: '/editreport/:id?/:editable?',
    title: 'Edit Report',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: AllSolutions,
    exact: false,
    path: '/allsolutions',
    title: 'All Solutions',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: AllSolutions,
    exact: false,
    path: '/viewsolutions/:kpi/:value?',
    title: 'View Solutions',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: AllSolutions,
    exact: false,
    path: '/bookmarks',
    title: 'My Bookmarks',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: AllSolutions,
    exact: false,
    path: '/mysolutions',
    title: 'My Solutions',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: SearchResults,
    exact: false,
    path: '/search/:query',
    title: 'Search',
  },
  {
    allowedRoles: AdminRole,
    component: Administration,
    exact: false,
    path: '/administration',
    title: 'Administration',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: DataikuComponent,
    exact: false,
    path: '/mydataiku',
    title: 'My Dataiku',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: Pipeline,
    exact: false,
    path: '/pipeline',
    title: 'Pipeline',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: Transparency,
    exact: false,
    path: '/transparency',
    title: 'Transparency',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: Tools,
    exact: false,
    path: '/tools/:tag?',
    title: 'Tools',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: ToolsDetailedPage,
    path: '/toolDetails/:id?',
    title: 'ToolsDetailedPage'
  },
  {
    allowedRoles: UserAndAdminRole,
    component: Trainings,
    exact: false,
    path: '/trainings',
    title: 'Trainings',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: GenAI,
    exact: false,
    path: '/genAI',
    title: 'GenAI',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: CreateNewPipeline,
    exact: false,
    path: '/createnewpipeline',
    title: 'New Pipeline Project',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: CreateNewPipeline,
    exact: false,
    path: '/createnewpipeline/:id?/:editable?',
    title: 'Edit Pipeline Project',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: EditCode,
    exact: false,
    path: '/editcode/:id?/:editable?',
    title: 'DAG Code Editor',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: Comingsoon,
    exact: false,
    path: '/comingsoon',
    title: 'Coming soon',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: CreateNewReport,
    exact: false,
    path: '/createnewreport',
    title: 'CreateNewReport',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: AllReports,
    exact: false,
    path: '/allreports',
    title: 'All Reports',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: Notifications,
    exact: false,
    path: '/notifications',
    title: 'Notifications',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: UserSettings,
    exact: false,
    path: '/usersettings',
    title: 'User Settings',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: StorageComponent,
    exact: false,
    path: '/storage',
    title: 'Storage',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: DataProductComponent,
    exact: false,
    path: '/data',
    title: 'Data',
  },
  // {
  //   allowedRoles: AdminRole ,
  //   component: ManageCodeSpaces,
  //   exact: false,
  //   path: '/codespace/manageCodespace',
  //   title: 'Manage codeSpace'
  // },
  // {
  //   allowedRoles: UserAndAdminRole,
  //   component: CodeSpaceSecurityConfig,
  //   exact: false,
  //   path: '/codespace/securityconfig/:id?',
  //   title: 'Security Config',
  // },
  // {
  //   allowedRoles: UserAndAdminRole,
  //   component: CodeSpaceSecurityConfig,
  //   exact: false,
  //   path: '/codespace/publishedSecurityconfig/:id?',
  //   title: 'Published Security Config',
  // },
  // {
  //   allowedRoles: AdminRole,
  //   component: CodeSpaceSecurityConfig,
  //   exact: false,
  //   path: '/codespace/adminSecurityconfig/:id?',
  //   title: 'View Security Config',
  // },
  // {
  //   allowedRoles: UserAndAdminRole,
  //   component: CodeSpace,
  //   exact: false,
  //   path: '/codespace/:id?',
  //   title: 'Code Space',
  // },
  // {
  //   allowedRoles: UserAndAdminRole,
  //   component: AllCodeSpaces,
  //   exact: false,
  //   path: '/codespaces',
  //   title: 'Your Code Spaces',
  // },
  // {
  //   allowedRoles: UserAndAdminRole,
  //   component:CodeSpaceRecipe ,
  //   exact: false,
  //   path: '/codespaceRecipes',
  //   title: 'Code Space recipes',
  // },
  {
    allowedRoles: UserAndAdminRole,
    component: CodeSpaceComponent,
    exact: false,
    path: '/codespaces',
    title: 'Your Code Spaces',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: ChronosComponent,
    exact: false,
    path: '/chronos',
    title: 'Chronos',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: MatomoComponent,
    exact: false,
    path: '/matomo',
    title: 'Matomo',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: DatalakeComponent,
    exact: false,
    path: '/datalake',
    title: 'Datalake',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: FabricComponent,
    exact: false,
    path: '/fabric',
    title: 'Fabric',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: DataEntryComponent,
    exact: false,
    path: '/dataentry',
    title: 'Data Entry as a Service',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: PowerPlatformComponent,
    exact: false,
    path: '/powerplatform',
    title: 'Power Platform',
  },
  {
    allowedRoles: UserAndAdminRole,
    component: PromptCraftComponent,
    exact: false,
    path: '/promptcraft',
    title: 'Prompt Craft',
  },
];

export const routes = [...publicRoutes, ...protectedRoutes];

export class Routes extends React.Component<{}, {}> {
  public render() {
    const appName = Envs.DNA_APPNAME_HEADER;
    document.title = appName;
    return (
      <React.Suspense fallback={<Progress show={true} />}>
        <Router history={history}>
          <Switch>
            {publicRoutes.map((route, index) => (
              <Route key={index} path={route.path} exact={route.exact} component={route.component} />
            ))}
            {protectedRoutes.map((route, index) => (
              // @ts-ignore: No overload matches this call.-
              <ProtectedRoute
                key={index}
                allowedRoles={route.allowedRoles}
                path={route.path}
                exact={route.exact}
                component={route.component}
                title={appName + ' - ' + route.title}
              />
            ))}
            <Route component={NotFoundPage} />
          </Switch>
        </Router>
      </React.Suspense>
    );
  }
}
