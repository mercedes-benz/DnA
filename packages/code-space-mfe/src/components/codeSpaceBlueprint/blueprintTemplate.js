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
  return str.replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
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
  { match: "redis", icon: "img/lib/mscae/Cache_Redis_Product.svg", x: 878, y: 282.4, width: 23.81, height: 20, textX: 909.5, textY: 278.86, isImage: true },
  { match: "rabbitmq", icon: `${rabbitmqIcon}`, x: 1027.2, y: 281.4, width: 20.8, height: 22, textX: 1057.44, textY: 281.36, isSvg: true },
  { match: "kafka", icon: `${kafkaIcon}`, x: 1160.3, y: 282.4, width: 15.4, height: 25, textX: 1183, textY: 280.86, isSvg: true },
  { match: "postgresql", icon: `${postgresIcon}`, x: 880.47, y: 320, width: 21.34, height: 22, textX: 911.5, textY: 316.46, isSvg: true },
  { match: "minio", icon: `${minioIcon}`, x: 1027.2, y: 320, width: 12.42, height: 25, textX: 1052.72, textY: 316.46, isSvg: true },
  { match: "zookeeper", icon: "mxgraph.aws3.toolkit_for_visual_studio", x: 1154.23, y: 319.5, width: 20, height: 25, textX: 1183, textY: 317.5, isShape: true, fillColor: "#53B1CB" },
];

const softwareConfig = [ 
  { match: "react", icon: `${reactIcon}`, x: 666.67, y: 334.31, width: 21.94, height: 25, textX: 696.44, textY: 333.5, isSvg: true }, 
  { match: "net", icon: `${dotNetIcon}`, x: 379.5, y: 376.54, width: 26.18, height: 25, textX: 408.13, textY: 377.27, isSvg: true }, 
  { match: "java", icon: `${javaIcon}`, x: 381, y: 280.4, width: 22.12, height: 30, textX: 408.13, textY: 286.86, isSvg: true }, 
  { match: "python", icon: `${pythonIcon}`, x: 379.5, y: 334.27, width: 22.11, height: 22, textX: 408.13, textY: 334.27, isSvg: true }, 
  { match: "gradle", icon: `${gradleIcon}`, x: 520.03, y: 335.81, width: 21.89, height: 22, textX: 545.44, textY: 333.5, isSvg: true }, 
  { match: "node", icon: `${nodejsIcon}`, x: 521, y: 288.4, width: 19.96, height: 22, textX: 544.44, textY: 287.63, isSvg: true }, 
  { match: "angular", icon: `${angularIcon}`, x: 666.67, y: 289.17, width: 20.75, height: 22, textX: 695.44, textY: 287.63, isSvg: true }, 
  { match: "go", icon: `${goIcon}`, x: 520.03, y: 381.54, width: 39.65, height: 15, textX: 565, textY: 375, isSvg: true }, 
  { match: "other software 1", icon: "mxgraph.aws3.toolkit_for_visual_studio", x: 666.67, y: 375, width: 22.6, height: 25, textX: 695.44, textY: 373, isShape: true, fillColor: "#53B1CB" }
];

const renderAdditionalServices = (additionalServices, isSoftware) => {
  let output = "";
  let config = isSoftware ? [...softwareConfig] : [...additionalServicesConfig];

  config.forEach((service, index) => {
    const matchValue = additionalServices?.find(
      s => s.toLowerCase().includes(service.match)
    );

    if (matchValue) {
      let iconStyle = '';
      if(service.isShape) iconStyle = `outlineConnect=0;dashed=0;verticalLabelPosition=bottom;verticalAlign=top;align=center;html=1;shape=${service.icon};fillColor=#53B1CB;gradientColor=none;aspect=fixed;`;

      if(service.isImage) iconStyle = `image;sketch=0;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=${service.icon};`;
      
      if(service.isSvg) iconStyle = `shape=image;verticalLabelPosition=bottom;labelBackgroundColor=default;verticalAlign=top;aspect=fixed;imageAspect=0;editableCssRules=.*;image=data:image/svg+xml,${service.icon};`;

      const textStyle = `text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontFamily=Verdana;fontStyle=0;fontSize=10;`;

      output += `
      <mxCell id="icon-${index}" value="" style="${iconStyle}" parent="1" vertex="1">
        <mxGeometry x="${service.x}" y="${service.y}" width="${service.width}" height="${service.height}" as="geometry" />
      </mxCell>
      <mxCell id="label-${index}" value="${matchValue}" style="${textStyle}" parent="1" vertex="1">
        <mxGeometry x="${service.textX}" y="${service.textY}" width="90.56" height="23.54" as="geometry" />
      </mxCell>\n`;
    }
  });

  return output;
}

export const blueprintTemplate = (codespace) => {
  const intDeploymentDetails = codespace?.projectDetails?.intDeploymentDetails;
  const prodDeploymentDetails = codespace?.projectDetails?.prodDeploymentDetails;
  const software = codespace?.projectDetails?.recipeDetails?.software;
  const additionalServices = codespace?.projectDetails?.recipeDetails?.additionalServices;
  const collaborators = codespace?.projectDetails?.projectCollaborators?.map((collaborator) => {
    return `${collaborator?.firstName} ${collaborator?.lastName} (${collaborator?.id})`;
  });

  const resourceUsageUrl = Envs.MONITORING_DASHBOARD_BASE_URL + `codespace-cpu-and-memory-usage?orgId=1&from=now-1h&to=now&var-namespace=${Envs.CODESERVER_NAMESPACE}&var-pod=${codespace.workspaceId}&var-container=notebook`;

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
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-4" value="&lt;a href=&quot;${codespace?.workspaceUrl}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Workspace URL&lt;/font&gt;&lt;/a&gt;" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontColor=light-dark(#000000,#00ADEF);fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="752" y="95.46" width="98" height="30" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-35" value="" style="verticalLabelPosition=bottom;html=1;verticalAlign=top;align=center;strokeColor=none;fillColor=#00BEF2;shape=mxgraph.azure.github_code;pointerEvents=1;" parent="1" vertex="1">
          <mxGeometry x="864" y="98.46000000000001" width="22" height="22" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-34" value="&lt;a href=&quot;${buildGitUrl(codespace.projectDetails?.gitRepoName)}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Go to Repo&lt;/font&gt;&lt;/a&gt;" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontColor=light-dark(#000000,#00ADEF);fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
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
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-7" value="${codespace?.projectDetails?.recipeDetails?.ramSize}GB RAM" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="1150" y="192.96" width="120" height="30" as="geometry" />
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
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-17" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=light-dark(#FFFFFF,#252A33);strokeColor=light-dark(#000000,#6C8EBF);fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
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
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-23" value="" style="pointerEvents=1;shadow=0;dashed=0;html=1;strokeColor=none;fillColor=#4495D1;labelPosition=center;verticalLabelPosition=bottom;verticalAlign=top;align=center;outlineConnect=0;shape=mxgraph.veeam.ram;" parent="1" vertex="1">
          <mxGeometry x="1070" y="200.36" width="30" height="15.2" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-24" value="" style="pointerEvents=1;shadow=0;dashed=0;html=1;strokeColor=none;fillColor=#4495D1;labelPosition=center;verticalLabelPosition=bottom;verticalAlign=top;align=center;outlineConnect=0;shape=mxgraph.veeam.cpu;" parent="1" vertex="1">
          <mxGeometry x="1230" y="195.56" width="30" height="24.8" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-25" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/management_governance/Blueprints.svg;" parent="1" vertex="1">
          <mxGeometry x="833" y="31" width="35" height="34.46" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-26" value="${codespace?.projectDetails?.recipeDetails?.cpuCapacity} CPU" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="1270" y="192.96" width="80" height="30" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-27" value="${codespace?.projectDetails?.recipeDetails?.operatingSystem}" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
          <mxGeometry x="980" y="192.96" width="120" height="30" as="geometry" />
        </mxCell>
        ${renderAdditionalServices(software, true)}
        ${renderAdditionalServices(additionalServices, false)}
        <mxCell id="yNrRxmF-IllQESKTyaMv-5" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=light-dark(#FFFFFF,#252A33);strokeColor=light-dark(#FFFFFF,#6C8EBF);fontFamily=Verdana;fontStyle=0" vertex="1" parent="1">
          <mxGeometry x="868" y="534" width="460" height="209" as="geometry" />
        </mxCell>
        <mxCell id="yNrRxmF-IllQESKTyaMv-1" value="Deployments" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontStyle=0;fontSize=13;fontFamily=Verdana;" vertex="1" parent="1">
          <mxGeometry x="384" y="461" width="156" height="30" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-14" value="Staging" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontStyle=0;fontSize=12;fontFamily=Verdana;" parent="1" vertex="1">
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
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-13" value="&lt;span style=&quot;&quot;&gt;Deployed: [Branch - ${intDeploymentDetails?.lastDeployedBranch}]&lt;/span&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;strokeWidth=0;spacing=0;" parent="1" vertex="1">
          <mxGeometry x="407.61" y="550" width="270" height="17" as="geometry" />
        </mxCell>

        <mxCell id="ujUrj7wP6LcdkLsmr5C5-38" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/management_governance/Activity_Log.svg;" parent="1" vertex="1">
          <mxGeometry x="379.61" y="667.5" width="18.39" height="22" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-37" value="&lt;a href=&quot;${intDeploymentDetails?.deploymentUrl}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Deployed App URL&lt;/font&gt;&lt;/a&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;spacing=0;strokeWidth=0;" parent="1" vertex="1">
          <mxGeometry x="407.61" y="669" width="270" height="19" as="geometry" />
        </mxCell>

        <mxCell id="ujUrj7wP6LcdkLsmr5C5-40" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/management_governance/Activity_Log.svg;" parent="1" vertex="1">
          <mxGeometry x="380.81" y="586.5" width="18.39" height="22" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-39" value="&lt;a href=&quot;${buildGitJobLogViewAWSURL(intDeploymentDetails?.gitjobRunID)}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Last Build &amp;amp; Deploy Logs&lt;/font&gt;&lt;/a&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;strokeWidth=0;spacing=0;" parent="1" vertex="1">
          <mxGeometry x="407.61" y="585" width="270" height="25" as="geometry" />
        </mxCell>

        <mxCell id="ujUrj7wP6LcdkLsmr5C5-42" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/management_governance/Activity_Log.svg;" parent="1" vertex="1">
          <mxGeometry x="379" y="707.5" width="18.39" height="22" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-41" value="&lt;a href=&quot;${buildLogViewAWSURL(intDeploymentDetails?.deploymentUrl || codespace.projectDetails?.projectName.toLowerCase(), true)}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Application Logs&lt;/font&gt;&lt;/a&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;strokeWidth=0;spacing=0;" parent="1" vertex="1">
          <mxGeometry x="407.61" y="709" width="270" height="19" as="geometry" />
        </mxCell>
        
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-44" value="" style="image;sketch=0;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/mscae/LogDiagnostics.svg;" parent="1" vertex="1">
          <mxGeometry x="381" y="630" width="18" height="20" as="geometry" />
        </mxCell>
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-43" value="&lt;a href=&quot;${escapeHTML(resourceUsageUrl)}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Deployed App Resource Usage&lt;/font&gt;&lt;/a&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;spacing=0;strokeWidth=0;" parent="1" vertex="1">
          <mxGeometry x="407.61" y="630.5" width="270" height="19" as="geometry" />
        </mxCell>
        ${{ /* Staging section end */ }}

        ${{ /* Production section start */ }}
        <mxCell id="yNrRxmF-IllQESKTyaMv-7" value="Production" style="text;html=1;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontStyle=0;fontSize=12;fontFamily=Verdana;" vertex="1" parent="1">
          <mxGeometry x="868" y="504" width="140" height="30" as="geometry" />
        </mxCell>
        
        <mxCell id="yNrRxmF-IllQESKTyaMv-8" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/general/Branch.svg;" vertex="1" parent="1">
          <mxGeometry x="879" y="547.5" width="22" height="22" as="geometry" />
        </mxCell>
        <mxCell id="yNrRxmF-IllQESKTyaMv-6" value="&lt;span style=&quot;&quot;&gt;Deployed: [Branch - ${prodDeploymentDetails?.lastDeployedBranch}]&lt;/span&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;strokeWidth=0;spacing=0;" vertex="1" parent="1">
          <mxGeometry x="907.61" y="550" width="270" height="17" as="geometry" />
        </mxCell>
        
        <mxCell id="yNrRxmF-IllQESKTyaMv-10" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/management_governance/Activity_Log.svg;" vertex="1" parent="1">
          <mxGeometry x="879.61" y="667.5" width="18.39" height="22" as="geometry" />
        </mxCell>
        <mxCell id="yNrRxmF-IllQESKTyaMv-9" value="&lt;a href=&quot;${prodDeploymentDetails?.deploymentUrl}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Deployed App URL&lt;/font&gt;&lt;/a&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;spacing=0;strokeWidth=0;" vertex="1" parent="1">
          <mxGeometry x="907.61" y="669" width="270" height="19" as="geometry" />
        </mxCell>
        
        <mxCell id="yNrRxmF-IllQESKTyaMv-12" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/management_governance/Activity_Log.svg;" vertex="1" parent="1">
          <mxGeometry x="880.81" y="586.5" width="18.39" height="22" as="geometry" />
        </mxCell>
        <mxCell id="yNrRxmF-IllQESKTyaMv-11" value="&lt;a href=&quot;${buildGitJobLogViewAWSURL(prodDeploymentDetails?.gitjobRunID)}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Last Build &amp;amp; Deploy Logs&lt;/font&gt;&lt;/a&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;strokeWidth=0;spacing=0;" vertex="1" parent="1">
          <mxGeometry x="907.61" y="585" width="270" height="25" as="geometry" />
        </mxCell>
        
        <mxCell id="yNrRxmF-IllQESKTyaMv-14" value="" style="image;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/azure2/management_governance/Activity_Log.svg;" vertex="1" parent="1">
          <mxGeometry x="879" y="707.5" width="18.39" height="22" as="geometry" />
        </mxCell>
        <mxCell id="yNrRxmF-IllQESKTyaMv-13" value="&lt;a href=&quot;${buildLogViewAWSURL(prodDeploymentDetails?.deploymentUrl || codespace.projectDetails?.projectName.toLowerCase(), true)}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Application Logs&lt;/font&gt;&lt;/a&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;strokeWidth=0;spacing=0;" vertex="1" parent="1">
          <mxGeometry x="907.61" y="709" width="270" height="19" as="geometry" />
        </mxCell>
        
        <mxCell id="yNrRxmF-IllQESKTyaMv-16" value="" style="image;sketch=0;aspect=fixed;html=1;points=[];align=center;fontSize=12;image=img/lib/mscae/LogDiagnostics.svg;" vertex="1" parent="1">
          <mxGeometry x="881" y="630" width="18" height="20" as="geometry" />
        </mxCell>
        <mxCell id="yNrRxmF-IllQESKTyaMv-15" value="&lt;a href=&quot;${escapeHTML(resourceUsageUrl)}&quot;&gt;&lt;font style=&quot;color: light-dark(rgb(0, 0, 0), rgb(0, 173, 239));&quot;&gt;Deployed App Resource Usage&lt;/font&gt;&lt;/a&gt;" style="rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=none;fontFamily=Verdana;fontStyle=0;align=left;spacingTop=0;spacingLeft=0;spacing=0;strokeWidth=0;" vertex="1" parent="1">
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
        <mxCell id="ujUrj7wP6LcdkLsmr5C5-31" value="" style="rounded=0;whiteSpace=wrap;html=1;fillColor=light-dark(#000000,#252A33);strokeColor=light-dark(#FFFFFF,#6C8EBF);fillStyle=auto;fontFamily=Verdana;fontStyle=0" parent="1" vertex="1">
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
  console.log(template);
  return template;
};