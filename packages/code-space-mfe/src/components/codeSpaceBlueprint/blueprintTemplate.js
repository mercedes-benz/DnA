import link from '../../assets/icons/link.svg?raw';
import react from '../../assets/icons/react.svg?raw';
import angular from '../../assets/icons/angular.svg?raw';
import dotnet from '../../assets/icons/dotnet.svg?raw';
import go from '../../assets/icons/go.svg?raw';
import gradle from '../../assets/icons/gradle.svg?raw';
import java from '../../assets/icons/java.svg?raw';
import kafka from '../../assets/icons/kafka.svg?raw';
import minio from '../../assets/icons/minio.svg?raw';
import nodejs from '../../assets/icons/nodejs.svg?raw';
import postgresql from '../../assets/icons/postgresql.svg?raw';
import python from '../../assets/icons/python.svg?raw';
import rabbitmq from '../../assets/icons/rabbitmq.svg?raw';
import { buildGitJobLogViewAWSURL, buildGitUrl, buildLogViewAWSURL } from '../../Utility/utils';
import { Envs } from '../../Utility/envs';

function escapeHTML(str) {
  if(str) {
    return str.replace(/&/g, '&amp;')
              .replace(/</g, '&lt;')
              .replace(/>/g, '&gt;')
              .replace(/"/g, '&quot;')
              .replace(/'/g, '&#39;');
  }
}

const linkIcon = btoa(link);
const reactIcon = btoa(react);
const goIcon = btoa(go);
const angularIcon = btoa(angular);
const kafkaIcon = btoa(kafka);
const postgresIcon = btoa(postgresql);
const rabbitmqIcon = btoa(rabbitmq);
const javaIcon = btoa(java);
const minioIcon = btoa(minio);
const gradleIcon = btoa(gradle);
const dotNetIcon = btoa(dotnet);
const pythonIcon = btoa(python);
const nodejsIcon = btoa(nodejs);

const additionalServicesConfig = [
  { match: "redis", icon: "img/lib/mscae/Cache_Redis_Product.svg", width: 23.81, height: 20, isImage: true },
  { match: "rabbitmq", icon: `${rabbitmqIcon}`, width: 20.8, height: 22, isSvg: true },
  { match: "kafka", icon: `${kafkaIcon}`, width: 15.4, height: 25, isSvg: true },
  { match: "postgresql", icon: `${postgresIcon}`, width: 21.34, height: 22, isSvg: true },
  { match: "minio", icon: `${minioIcon}`, width: 12.42, height: 25, isSvg: true },
];

const softwareConfig = [
  { match: "react", icon: `${reactIcon}`, width: 21.94, height: 25, isSvg: true },
  { match: "net", icon: `${dotNetIcon}`, width: 26.18, height: 25, isSvg: true },
  { match: "java", icon: `${javaIcon}`, width: 22.12, height: 30, isSvg: true },
  { match: "python", icon: `${pythonIcon}`, width: 22.11, height: 22, isSvg: true },
  { match: "gradle", icon: `${gradleIcon}`, width: 21.89, height: 22, isSvg: true },
  { match: "node", icon: `${nodejsIcon}`, width: 19.96, height: 22, isSvg: true },
  { match: "angular", icon: `${angularIcon}`, width: 20.75, height: 22, isSvg: true },
  { match: "go", icon: `${goIcon}`, width: 39.65, height: 15, isSvg: true },
];

const defaultOtherIcon = {
  match: "other",
  icon: "mxgraph.aws3.toolkit_for_visual_studio",
  width: 22.6,
  height: 25,
  isShape: true,
  fillColor: "#53B1CB"
};

const renderAdditionalServices = (additionalServices, isSoftware) => {
  let output = "";
  let baseConfig = isSoftware ? [...softwareConfig] : [...additionalServicesConfig];

  // Layout parameters
  const layout = {
    software: {
      baseX: 380,
      baseY: 280,
      gapX: 140,
      gapY: 55,
      columns: 3,
    },
    services: {
      baseX: 880,
      baseY: 280,
      gapX: 140,
      gapY: 45,
      columns: 3,
    }
  };

  const { baseX, baseY, gapX, gapY, columns } = isSoftware ? layout.software : layout.services;
  // let currentIndex = 0;

  const matchedServices = [];
  const unmatchedServices = [];

  additionalServices?.forEach(service => {
    const match = baseConfig.find(config => service.toLowerCase().includes(config.match));
    if (match) {
      matchedServices.push({ ...match, actualValue: service });
    } else {
      unmatchedServices.push({
        ...defaultOtherIcon,
        actualValue: service
      });
    }
  });

  const allServices = [...matchedServices, ...unmatchedServices];

  allServices.forEach((service, index) => {
    const col = index % columns;
    const row = Math.floor(index / columns);

    const x = baseX + col * gapX;
    const y = baseY + row * gapY;
    const textX = x + 30;
    const textY = y - 5;

    let iconStyle = '';
    if (service.isShape) {
      iconStyle = `outlineConnect=0;dashed=0;verticalLabelPosition=bottom;verticalAlign=top;align=center;html=1;shape=${service.icon};fillColor=${service.fillColor || "#53B1CB"};gradientColor=none;aspect=fixed;`;
    } else if (service.isImage) {
      iconStyle = `image;sketch=0;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=${service.icon};`;
    } else if (service.isSvg) {
      iconStyle = `shape=image;verticalLabelPosition=bottom;labelBackgroundColor=default;verticalAlign=top;aspect=fixed;imageAspect=0;editableCssRules=.*;image=data:image/svg+xml,${service.icon};`;
    }

    const textStyle = `text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontFamily=Verdana;fontStyle=0;fontSize=10;`;

    output += `
    <mxCell id="icon-${Math.random(index)}" value="" style="${iconStyle}" parent="1" vertex="1">
      <mxGeometry x="${x}" y="${y}" width="${service.width}" height="${service.height}" as="geometry" />
    </mxCell>
    <mxCell id="label-${Math.random(index)}" value="${service.actualValue}" style="${textStyle}" parent="1" vertex="1">
      <mxGeometry x="${textX}" y="${textY}" width="90.56" height="23.54" as="geometry" />
    </mxCell>\n`;
  });

  return output;
};

export const blueprintTemplate = (codespace) => {
  const resources = codespace?.projectDetails?.recipeDetails?.resource?.split(',');
  const intDeploymentDetails = codespace?.projectDetails?.intDeploymentDetails;
  const prodDeploymentDetails = codespace?.projectDetails?.prodDeploymentDetails;
  const software = codespace?.projectDetails?.recipeDetails?.software;
  const additionalServices = codespace?.projectDetails?.recipeDetails?.additionalServices;
  const collaborators = codespace?.projectDetails?.projectCollaborators?.map((collaborator) => {
    return `${collaborator?.firstName} ${collaborator?.lastName} (${collaborator?.id})`;
  });

  const intAppResourceUsageUrl = Envs.MONITORING_DASHBOARD_APP_BASE_URL + `codespace-app-cpu-and-memory-usage?orgId=1&var-namespace=${Envs.CODESERVER_APP_NAMESPACE}&var-app=${codespace?.projectDetails?.projectName}-int&var-container=`;
  const prodAppResourceUsageUrl = Envs.MONITORING_DASHBOARD_APP_BASE_URL + `codespace-app-cpu-and-memory-usage?orgId=1&var-namespace=${Envs.CODESERVER_APP_NAMESPACE}&var-app=${codespace?.projectDetails?.projectName}-prod&var-container=`;

  const template = `
    <mxGraphModel dx="1428" dy="751" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="1700" pageHeight="1100" math="0" shadow="0">
      <root>
        <mxCell id="0" />
        <mxCell id="1" parent="0" />
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-1" value="" style="rounded=0;whiteSpace=wrap;html=1;dashed=1;dashPattern=8 8;fillColor=none;strokeColor=light-dark(#000000,#00ADEF);fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="310" y="135.46" width="1080" height="915.54" as="geometry" />
        </mxCell>
        ${{/* Header Section Start */}}
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-3" value="${codespace?.projectDetails?.projectName}" style="text;html=1;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontStyle=0;fontSize=16;fontFamily=Verdana;" parent="1" vertex="1">
          <mxGeometry x="310" y="65.46" width="1080" height="30" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-4" value="&lt;a href=&quot;${escapeHTML(codespace?.workspaceUrl)}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Workspace URL&lt;/font&gt;&lt;/a&gt;" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontColor=light-dark(#000000,#00ADEF);fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="752" y="95.46" width="98" height="30" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-35" value="" style="verticalLabelPosition=bottom;html=1;verticalAlign=top;align=center;strokeColor=none;fillColor=#00BEF2;shape=mxgraph.azure.github_code;pointerEvents=1;" parent="1" vertex="1">
          <mxGeometry x="864" y="98.46000000000001" width="22" height="22" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-34" value="&lt;a href=&quot;${escapeHTML(buildGitUrl(codespace.projectDetails?.gitRepoName))}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Go to Repo&lt;/font&gt;&lt;/a&gt;" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontColor=light-dark(#000000,#00ADEF);fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="890.05" y="95.46000000000001" width="100" height="30" as="geometry" />
        </mxCell>
        ${{/* Header Section End */}}
        <mxCell id="yNrRxmF-IllQESKTyaMv-2" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=light-dark(#6C8EBF,#00ADEF);dashed=1;fillStyle=auto;fontFamily=Verdana;fontStyle=0" vertex="1" parent="1">
          <mxGeometry x="348" y="494" width="1000" height="276" as="geometry" />
        </mxCell>
        ${{/* Recipe Section Start */}}
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-6" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=light-dark(#6C8EBF,#00ADEF);dashed=1;fillStyle=auto;fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="350" y="225.46" width="1000" height="220" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-5" value="${codespace?.projectDetails?.recipeDetails?.cloudServiceProvider}" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontStyle=0;fontFamily=Verdana;" parent="1" vertex="1">
          <mxGeometry x="354" y="142.96" width="396" height="30" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-23" value="" style="pointerEvents=1;shadow=0;dashed=0;html=1;strokeColor=none;fillColor=#4495D1;labelPosition=center;verticalLabelPosition=bottom;verticalAlign=top;align=center;outlineConnect=0;shape=mxgraph.veeam.ram;" parent="1" vertex="1">
          <mxGeometry x="1100" y="200.36" width="30" height="15.2" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-7" value="${(resources[3]?.split('M')[0])/1000}GB RAM" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="1150" y="192.96" width="120" height="30" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-24" value="" style="pointerEvents=1;shadow=0;dashed=0;html=1;strokeColor=none;fillColor=#4495D1;labelPosition=center;verticalLabelPosition=bottom;verticalAlign=top;align=center;outlineConnect=0;shape=mxgraph.veeam.cpu;" parent="1" vertex="1">
          <mxGeometry x="1230" y="195.56" width="30" height="24.8" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-25" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/management_governance/Blueprints.svg;" parent="1" vertex="1">
          <mxGeometry x="833" y="31" width="35" height="34.46" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-26" value="${resources[4]} CPU" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="1270" y="192.96" width="80" height="30" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-27" value="${codespace?.projectDetails?.recipeDetails?.operatingSystem}" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="980" y="192.96" width="120" height="30" as="geometry" />
        </mxCell>

        <mxCell id="ujUrj7wP6LcdkLsmr5C5-2" value="Recipe: ${codespace?.projectDetails?.recipeDetails?.recipeName}" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontStyle=0;fontSize=13;fontFamily=Verdana;" parent="1" vertex="1">
          <mxGeometry x="392" y="192.46" width="528" height="30" as="geometry" />
        </mxCell>
        ${{/* Recipe Section End */}}
        
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-12" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=light-dark(#FFFFFF,#252A33);strokeColor=light-dark(#FFFFFF,#6C8EBF);fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="368" y="534" width="460" height="209" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-15" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=light-dark(#FFFFFF,#252A33);strokeColor=light-dark(#FFFFFF,#6C8EBF);fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="368" y="271" width="460" height="145.54" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-16" value="Software" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontStyle=0;fontFamily=Verdana;" parent="1" vertex="1">
          <mxGeometry x="368" y="241" width="460" height="30" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-17" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=light-dark(#FFFFFF,#252A33);strokeColor=light-dark(#FFFFFF,#6C8EBF);fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="868" y="271" width="460" height="89" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-18" value="Additional Services" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontStyle=0;fontFamily=Verdana;" parent="1" vertex="1">
          <mxGeometry x="868" y="241" width="460" height="30" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-19" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/general/Code.svg;fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="351" y="195.46" width="28.31" height="23" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-21" value="" style="shape=image;editableCssRules=.*;verticalLabelPosition=bottom;labelBackgroundColor=default;verticalAlign=top;aspect=fixed;imageAspect=0;image=data:image/svg+xml,${linkIcon};" parent="1" vertex="1">
          <mxGeometry x="729" y="100.46000000000001" width="18" height="18" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-22" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/compute/OS_Images_Classic.svg;" parent="1" vertex="1">
          <mxGeometry x="950" y="199.96" width="21.56" height="20" as="geometry" />
        </mxCell>
        ${renderAdditionalServices(software, true)}
        ${renderAdditionalServices(additionalServices, false)}
        <mxCell id="yNrRxmF-IllQESKTyaMv-5" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=light-dark(#FFFFFF,#252A33);strokeColor=light-dark(#FFFFFF,#6C8EBF);fontFamily=Verdana;fontStyle=0" vertex="1" parent="1">
          <mxGeometry x="868" y="534" width="460" height="209" as="geometry" />
        </mxCell>
        <mxCell id="yNrRxmF-IllQESKTyaMv-1" value="Deployments" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontStyle=0;fontSize=13;fontFamily=Verdana;" vertex="1" parent="1">
          <mxGeometry x="384" y="461" width="156" height="30" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-14" value="Staging - ${intDeploymentDetails?.lastDeployedBranch ? 'Deployed' : 'No Deployment'}" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontStyle=0;fontSize=12;fontFamily=Verdana;" parent="1" vertex="1">
          <mxGeometry x="368" y="504" width="140" height="30" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-30" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/other/Azure_Deployment_Environments.svg;" parent="1" vertex="1">
          <mxGeometry x="348" y="465" width="22.91" height="22" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-45" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/app_services/App_Service_Plans.svg;" parent="1" vertex="1">
          <mxGeometry x="324" y="142.96" width="24" height="24" as="geometry" />
        </mxCell>
        ${{ /* Staging section start */ }}
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-36" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/general/Branch.svg;" parent="1" vertex="1">
          <mxGeometry x="379" y="547.5" width="22" height="22" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-13" value="&lt;span style=&quot;&quot;&gt;Deployed: [Branch - ${intDeploymentDetails?.lastDeployedBranch ? intDeploymentDetails?.lastDeployedBranch : 'No Deployment'}]&lt;/span&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;strokeWidth=0;spacing=0;" parent="1" vertex="1">
          <mxGeometry x="407.61" y="550" width="270" height="17" as="geometry" />
        </mxCell>

        <mxCell id="ujUrj7wP6LcdkLsmr5C5-38" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/management_governance/Activity_Log.svg;" parent="1" vertex="1">
          <mxGeometry x="379.61" y="667.5" width="18.39" height="22" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-37" value="&lt;a href=&quot;${escapeHTML(intDeploymentDetails?.deploymentUrl)}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Deployed App URL&lt;/font&gt;&lt;/a&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;spacing=0;strokeWidth=0;" parent="1" vertex="1">
          <mxGeometry x="407.61" y="669" width="270" height="19" as="geometry" />
        </mxCell>

        <mxCell id="ujUrj7wP6LcdkLsmr5C5-40" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/management_governance/Activity_Log.svg;" parent="1" vertex="1">
          <mxGeometry x="380.81" y="586.5" width="18.39" height="22" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-39" value="&lt;a href=&quot;${escapeHTML(buildGitJobLogViewAWSURL(intDeploymentDetails?.gitjobRunID))}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Last Build &amp;amp; Deploy Logs&lt;/font&gt;&lt;/a&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;strokeWidth=0;spacing=0;" parent="1" vertex="1">
          <mxGeometry x="407.61" y="585" width="270" height="25" as="geometry" />
        </mxCell>

        <mxCell id="ujUrj7wP6LcdkLsmr5C5-42" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/management_governance/Activity_Log.svg;" parent="1" vertex="1">
          <mxGeometry x="379" y="707.5" width="18.39" height="22" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-41" value="&lt;a href=&quot;${escapeHTML(buildLogViewAWSURL(intDeploymentDetails?.deploymentUrl || codespace.projectDetails?.projectName.toLowerCase(), true))}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Application Logs&lt;/font&gt;&lt;/a&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;strokeWidth=0;spacing=0;" parent="1" vertex="1">
          <mxGeometry x="407.61" y="709" width="270" height="19" as="geometry" />
        </mxCell>
        
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-44" value="" style="image;sketch=0;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/mscae/LogDiagnostics.svg;" parent="1" vertex="1">
          <mxGeometry x="381" y="630" width="18" height="20" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-43" value="&lt;a href=&quot;${escapeHTML(intAppResourceUsageUrl)}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Deployed App Resource Usage&lt;/font&gt;&lt;/a&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;spacing=0;strokeWidth=0;" parent="1" vertex="1">
          <mxGeometry x="407.61" y="630.5" width="270" height="19" as="geometry" />
        </mxCell>
        ${{ /* Staging section end */ }}

        ${{ /* Production section start */ }}
        <mxCell id="yNrRxmF-IllQESKTyaMv-7" value="Production - ${prodDeploymentDetails?.lastDeployedBranch ? 'Deployed' : 'No Deployment'}" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontStyle=0;fontSize=12;fontFamily=Verdana;" vertex="1" parent="1">
          <mxGeometry x="868" y="504" width="140" height="30" as="geometry" />
        </mxCell>
        
        <mxCell id="yNrRxmF-IllQESKTyaMv-8" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/general/Branch.svg;" vertex="1" parent="1">
          <mxGeometry x="879" y="547.5" width="22" height="22" as="geometry" />
        </mxCell>
        <mxCell id="yNrRxmF-IllQESKTyaMv-6" value="&lt;span style=&quot;&quot;&gt;Deployed: [Branch - ${prodDeploymentDetails?.lastDeployedBranch ? prodDeploymentDetails?.lastDeployedBranch : 'No Deployment'}]&lt;/span&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;strokeWidth=0;spacing=0;" vertex="1" parent="1">
          <mxGeometry x="907.61" y="550" width="270" height="17" as="geometry" />
        </mxCell>
        
        <mxCell id="yNrRxmF-IllQESKTyaMv-10" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/management_governance/Activity_Log.svg;" vertex="1" parent="1">
          <mxGeometry x="879.61" y="667.5" width="18.39" height="22" as="geometry" />
        </mxCell>
        <mxCell id="yNrRxmF-IllQESKTyaMv-9" value="&lt;a href=&quot;${escapeHTML(prodDeploymentDetails?.deploymentUrl)}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Deployed App URL&lt;/font&gt;&lt;/a&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;spacing=0;strokeWidth=0;" vertex="1" parent="1">
          <mxGeometry x="907.61" y="669" width="270" height="19" as="geometry" />
        </mxCell>
        
        <mxCell id="yNrRxmF-IllQESKTyaMv-12" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/management_governance/Activity_Log.svg;" vertex="1" parent="1">
          <mxGeometry x="880.81" y="586.5" width="18.39" height="22" as="geometry" />
        </mxCell>
        <mxCell id="yNrRxmF-IllQESKTyaMv-11" value="&lt;a href=&quot;${escapeHTML(buildGitJobLogViewAWSURL(prodDeploymentDetails?.gitjobRunID))}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Last Build &amp;amp; Deploy Logs&lt;/font&gt;&lt;/a&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;strokeWidth=0;spacing=0;" vertex="1" parent="1">
          <mxGeometry x="907.61" y="585" width="270" height="25" as="geometry" />
        </mxCell>
        
        <mxCell id="yNrRxmF-IllQESKTyaMv-14" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/management_governance/Activity_Log.svg;" vertex="1" parent="1">
          <mxGeometry x="879" y="707.5" width="18.39" height="22" as="geometry" />
        </mxCell>
        <mxCell id="yNrRxmF-IllQESKTyaMv-13" value="&lt;a href=&quot;${escapeHTML(buildLogViewAWSURL(prodDeploymentDetails?.deploymentUrl || codespace.projectDetails?.projectName.toLowerCase(), true))}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Application Logs&lt;/font&gt;&lt;/a&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;strokeWidth=0;spacing=0;" vertex="1" parent="1">
          <mxGeometry x="907.61" y="709" width="270" height="19" as="geometry" />
        </mxCell>
        
        <mxCell id="yNrRxmF-IllQESKTyaMv-16" value="" style="image;sketch=0;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/mscae/LogDiagnostics.svg;" vertex="1" parent="1">
          <mxGeometry x="881" y="630" width="18" height="20" as="geometry" />
        </mxCell>
        <mxCell id="yNrRxmF-IllQESKTyaMv-15" value="&lt;a href=&quot;${escapeHTML(prodAppResourceUsageUrl)}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Deployed App Resource Usage&lt;/font&gt;&lt;/a&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;spacing=0;strokeWidth=0;" vertex="1" parent="1">
          <mxGeometry x="907.61" y="630.5" width="270" height="19" as="geometry" />
        </mxCell>
        ${{ /* Production section start */ }}
        ${{/* Team Section Start */}}
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-29" value="" style="image;sketch=0;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/mscae/Two_User_Icon.svg;" parent="1" vertex="1">
          <mxGeometry x="348" y="790" width="28" height="28" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-9" value="Team" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontStyle=0;fontSize=13;fontFamily=Verdana;" parent="1" vertex="1">
          <mxGeometry x="389" y="794" width="140" height="30" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-10" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=light-dark(#FFFFFF,#252A33);strokeColor=light-dark(#FFFFFF,#6C8EBF);fillStyle=auto;fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="371" y="864" width="322" height="40" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-8" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;dashed=1;strokeColor=light-dark(#6C8EBF,#00ADEF);fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="346" y="824" width="1002" height="196" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-20" value="Owner" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontStyle=0;fontFamily=Verdana;" parent="1" vertex="1">
          <mxGeometry x="371" y="834" width="140" height="30" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-11" value="${codespace?.projectDetails?.projectOwner?.firstName} ${codespace?.projectDetails?.projectOwner?.lastName} (${codespace?.projectDetails?.projectOwner?.id}) ${codespace?.projectDetails?.projectOwner?.isAdmin ? ' - Admin' : ''}" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="381" y="864" width="252" height="40" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-31" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=light-dark(#FFFFFF,#252A33);strokeColor=light-dark(#FFFFFF,#6C8EBF);fillStyle=auto;fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="368" y="954" width="962" height="40" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-33" value="Collaborator(s)" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontStyle=0;fontFamily=Verdana;" parent="1" vertex="1">
          <mxGeometry x="368" y="924" width="140" height="30" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-32" value="${collaborators ? collaborators.join(' | ') : 'No collaborators'}" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="378" y="954" width="942" height="40" as="geometry" />
        </mxCell>
        ${{/* Team Section End */}}
      </root>
    </mxGraphModel>
  `;
  
  return template;
};