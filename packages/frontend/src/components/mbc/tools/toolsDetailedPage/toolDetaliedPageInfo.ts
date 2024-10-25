export const ToolsDetailedPageElements = [
  {
    "id": "dataikuDSS",
    "name": "Dataiku DSS",
    "description": "Dataiku Data Science Studio is a low-code/ no-code data wrangling and machine learning platform. Typical use cases are data preparation, analysis and the development of machine learning models",
    "tags": ["Data Engineering", "Data Science", "No / Low Code", "Cloud"],
    "isExternalLink": false,
    "url": "/mydataiku",
    "useCases": [
      {
        "icon": "dataPreparation",
        "title": "Data Preparation & Wrangling",
        "description": "eg: bring your excel files into a common format"
      },
      {
        "icon": "machineLearning",
        "title": "Machine Learning",
        "description": "Build predictive Models with your data and share the results with others"
      }
    ],
    "toolPipeLine": {
      "description": ["With Dataiku you can ingest large amounts of data, clean it up and prepare it for large-scale analyses and ML use cases. You can automate the preparation process by scheduling it or triggering it through events. Dataiku is a low-code/ no-code data wrangling and machine learning platform. Typical use cases are clean, join, transform and enrich datasets for analytics and ML use cases."],
      "connectedTO": [
        {
          "title": "SAP ERPs",
          "icon": "sac",
          "url": "url"
        },
        {
          "title": "Cloud Data Sources",
          "icon": "afo",
          "url": "url"
        },
        {
          "title": "On-prem Data Sources",
          "icon": "data",
          "url": "url"
        }

      ]
    },
    "info": [
      {
        "icon": "doc",
        "name": "Documentation",
        "description": "Explore all documented information",
        "links": [
          {
            "title": "Dataiku SI Page",
            "link": "YOUR_URL"
          },
          {
            "title": "Get onboarded",
            "link": "YOUR_URL"
          },
          {
            "title": "Usefull links ",
            "link": "YOUR_URL"
          },
          {
            "title": "Terms of Use (cloud)",
            "link": "YOUR_URL"
          }
        ]
      },
      {
        "icon": "trainings",
        "name": "Trainings",
        "description": "Get tailor-made trainings from experts to master this tool with ease",
        "links": [
          {
            "title": "Dataiku Academy",
            "link": "https://academy.dataiku.com/"
          },
          {
            "title": "Try it out on our free training environment",
            "link": "YOUR_URL"
          }
        ]
      },
      {
        "icon": "tem",
        "name": "Costs",
        "description": " Get familiar with the costs.",
        "info":["Dataiku reader: cost-free​" ,"Dataiku visual designer: 2700 EUR user/year​","Dataiku designer: 5100 EUR user/year"]
      }
    ],
    "classification": "Confidential",
    "accessSteps": [
      {
        "id": "Step 1",
        "info": "If you are not onboarded already follow: **[YOUR_URL](YOUR_URL)**"
      },
      {
        "id": "Step 2",
        "info": "Create new Dataiku project (on-prem or cloud) via your application **[YOUR_URL](YOUR_URL)** "
      },
      {
        "id": "Step 3",
        "info": "Open Dataiku project via link in your application"
      }
    ]
  },
  {
    "id": "powerBI",
    "name": "Power BI",
    "description": "Power BI Desktop is an application you install on your local computer that lets you connect to, transform, and visualize your data. With Power BI Desktop, you can connect to multiple different sources on data, and combine them into a data model. This data model lets you build visuals, and collections of visuals you can share as reports, with other people inside your organization. You get Power BI in the ITShop. Most users who work on business intelligence projects use Power BI Desktop to create reports, and then use the Power BI service called PBOS to share their reports with others.",
    "tags": ["Frontend Reporting", "No / Low Code", "Onprem"],
    "isExternalLink": true,
    "url": "powerBIUrl",
    "useCases": [
      {
        "icon": "case3",
        "title": "Data Analysis",
        "description": "Building Reports and Dashboards with csv data"
      }
    ],
    "toolPipeLine": {
      "description": ["With power Bi Desktop you can visualize your data. You can use your data directly in the report (Import) or you can connect to a Fileshare and bring your data files there. with Power BI you can than combine your data into a data model. This data model lets you build visiuals, and collections of visuals you can share as reports, with other people.", "Most users who work on business intelligence projects use Power BI Desktop to create reports, and then use the Power BI service called PBOS to share their reports with others"],
      "connectedTO": [
        {
          "title": "Fileshare",
          "icon": "trino_shared-folder",
          "url": "url"
        }
      ]
    },
    "info": [
      {
        "icon": "doc",
        "name": "Documentation",
        "description": "Explore all documented information ",
        "links": [
          {
            "title": "Power BI SI Page",
            "link": "YOUR_URL"
          },
          {
            "title": "User manual",
            "link": "YOUR_URL"
          },
          {
            "title": "Terms of Use",
            "link": "YOUR_URL"
          },
          {
            "title": "Power BI Templates",
            "link": "YOUR_URL"
          },
          {
            "title": "Custom Visuals",
            "link": "YOUR_URL"
          }
        ]
      },
      {
        "icon": "trainings",
        "name": "Trainings",
        "description": "Get tailor-made trainings from experts to master this tool with ease",
        "links": [
          {
            "title": "Microsoft Learn Power BI",
            "link": "https://learn.microsoft.com/en-us/training/browse/?products-power-bi"
          },
          {
            "title": "Get started with Power BI (80 min)",
            "link": "https://learn.microsoft.com/de-de/training/modules/get-started-with-power-bi/"
          },
          {
            "title": "Create Dashboards (40 min)",
            "link": "https://learn.microsoft.com/de-de/training/modules/create-dashboards-power-bi/"
          },
          {
            "title": "Clean Data in Power BI (120 min)",
            "link": "https://learn.microsoft.com/de-de/training/modules/clean-data-power-bi/"
          },
          {
            "title": "LinkedIn Learning Power BI (105 min)",
            "link": "https://de.linkedin.com/learning/power-bi-desktop-lernen/daten-aufbereiten-analysieren-und-visualisieren?u=141000850"
          }
        ]
      },
      {
        "icon": "tem",
        "name": "Costs",
        "description" : "Get familiar with the costs",
        "info":["Power BI Desktop RS installation: cost-free (IT-Shop)" ," Azure Power BI Pro licence (for publisher): monthly costs of 5.25 EUR (IT Shop)","Azure PowerBI Free licence (for reader): cost-free (IT Shop)"]
      }
    ],
    "classification" : "Confidential",
    "accessSteps":[
      {
        "id": "Step 1",
        "info" : "Create AD Groups via ServiceNow (see User manual chapter 5)"
      },
      {
        "id": "Step 2",
        "info" : "send an Email to ssa@mercedes-benz.com with all required information + E3 confirmation to the ToU (see User manual chapter 2)"
      },
      {
        "id": "Step 3",
        "info" : "Manage permissions across your AD groups on a need-to-know basis (see User manual chapter 5.3)"
      }
    ]
  },
  {
    "id": "fabric",
    "name": "Fabric",
    "description": "Fabric offers a comprehensive suite of services, including data lake, data engineering, and data integration, all in one place.",
    "tags": ["Data Engineering", "Data Science","No / Low Code", "Cloud", "Frontend Reporting", "Data Storage", "Data Pipeline"],
    "isExternalLink": false,
    "url": "/fabric",
    "useCases": [
      {
        "icon": "dataPreparation",
        "title": "Data Preparation & Wrangling",
        "description": " Build End2End Data Pipelines in (Py)Spark (preferred); T-SQL and with Low-Code/ No-Code Tools"
      },
      {
        "icon": "dataAnalysis",
        "title": "Data Analysis",
        "description": " Analyze Data in Notebooks and PowerBI "
      },
      {
        "icon": "machineLearning",
        "title": "Machine Learning",
        "description": "Train and operate machine learning models"
      }
    ],
    "toolPipeLine": {
      "description": ["Microsoft Fabric is a SaaS Analytics Platform build on an open lakehouse architecture. It is a cloud platform that allows you to ingest, wrangle and visualize any kind of data. It offers Machine Learning capabilities and includes PowerBI, one of the most loved dashboarding tools in Mercedes-Benz"],
      "connectedTO": [
        {
          "title": "SAP ERPs",
          "icon": "trino_shared-folder",
          "url": "url"
        }
      ]
    },
    "info": [
      {
        "icon": "doc",
        "name": "Documentation",
        "description": "Explore all documented information ",
        "links": [
          {
            "title": "Microsoft Fabric Documentation ",
            "link": "YOUR_URL"
          },
          {
            "title": "FC.OS Data Platform Wiki",
            "link": "YOUR_URL"
          },
          {
            "title": "Terms of Use",
            "link": "YOUR_URL"
          },
          {
            "title": "Power BI Templates ",
            "link": "YOUR_URL"
          },
          {
            "title": "Power BI Custom Visuals ",
            "link": "YOUR_URL"
          }
        ]
      },
      {
        "icon": "trainings",
        "name": "Trainings",
        "description": "Get tailor-made trainings from experts to master this tool with ease",
        "links": [
          {
            "title": "Microsoft Fabric Learning ",
            "link": "YOUR_URL"
          },
          {
            "title": "Dataflow Tutorial",
            "link": "https://microsoftlearning.github.io/mslearn-fabric/Instructions/Labs/05-dataflows-gen2.html"
          },
          {
            "title": "Become a Fabric Analytics Engineer",
            "link": "https://community.fabric.microsoft.com/t5/custom/page/page-id/CareerHubPage?ocid=fabric24_careerhub_fabric_updateblog_clphttp://"
          },
          {
            "title": "Microsoft Learn Power BI",
            "link": "https://learn.microsoft.com/en-us/training/browse/?products=power-bi"
          },
          {
            "title": "Get started with Power BI (80 min)",
            "link": "https://learn.microsoft.com/de-de/training/modules/get-started-with-power-bi/"
          },

          {
            "title": "Create Dashboards (40 min)",
            "link": "https://learn.microsoft.com/de-de/training/modules/create-dashboards-power-bi/"
          }
        ]
      },
      {
        "icon": "tem",
        "name": "Costs",
        "description" : "Get familiar with the costs",
        "info":["Microsoft Fabric infrastructure and storage: Pay-as-you-use "," Microsoft Fabric operation costs ","Power BI Desktop RS: cost-free (IT-Shop)"," Azure PowerBI Free licence (viewer): cost-free (IT-Shop) ", "Azure Power BI Pro licence (developer): 5.25 EUR user/month (IT-Shop)"]
      }
    ],
    "classification" : "Confidential",
    "accessSteps":[
      {
        "id": "Step 1",
        "info" : " Create Fabric Workspace via DnA"
      },
      {
        "id": "Step 2",
        "info" : "Assign needed Fabric Workspace role (Admin/Member/Contributor/ViewerWorkspace) via Alice"
      },
      {
        "id": "Step 3",
        "info" : " Open Microsoft Fabric (https://app.fabric.microsoft.com/home)"
      }
    ]
  },
  {
    "id": "powerPlatform",
    "name": "Power Platform",
    "description": "Microsoft Power Platform is a low-code platform for building customized end-to-end business solutions. It consists of five product areas: Power Apps, Power Automate, Power BI, Copilot Studio, and Power Pages.",
    "tags":  ["No / Low Code"],
    "isExternalLink": false,
    "url": "/powerplatform",
    "hasSubcription": true,
    "info": [
      {
        "icon": "trainings",
        "name": "Trainings",
        "description": "Get tailor-made trainings from experts to master this tool with ease",
        "links": [
          {
            "title": "PL-900: Microsoft Power Platform Fundamentals (udemy.com)",
            "link": `UDEMY_URL/course/pl-900-microsoft-power-platform-fundamentals-r/`
          },
          {
            "title": "Power Apps - Complete Guide to Microsoft PowerApps",
            "link": `UDEMY_URL/course/complete-guide-to-microsoft-powerapps-basic-to-advanced/`
          },
          {
            "title": "Build Solutions with Power Apps, Power Automate & SharePoint (udemy.com)",
            "link": `UDEMY_URL/course/build-solutions-with-power-platform/`
          },
          {
            "title": "PL-200: The follow-up Power Platform course to the PL-100 (udemy.com)",
            "link": `UDEMY_URL/course/pl-200-microsoft-power-platform-functional-consultant-course/`
          },
          {
            "title": "Microsoft Power Automate for Power Apps developers (udemy.com)",
            "link": `UDEMY_URL/course/microsoft-power-automate-for-power-apps-developers/`
          }
        ]
      },
      {
        "icon": "profile",
        "name": "support",
        "info": ["Feel free to reach out for more information and personal support by our team. We have a highly skilled and performant team specialized in providing support and development services in the field of Automation, Digitalization and Analytics. We are happy to help you with any open question about the Usage and Costs of the Power Platform. Also we offer a development service."],
        "links": [
          {
            "title": "Contact Email",
            "link": "YOUR_URL"
          },
          {
            "title": "Social Intranet Page",
            "link": "YOUR_URL"
          },
        ]
      },
      {
        "icon": "tem",
        "name": "classification",
        "type" : "Confidential"
      },
      {
        "icon": "tem",
        "name": "Costs",
        "isInfoIcon": "true",
        "description" : "Get familiar with the costs",
        "info":["Default Environment: Free", "Citizen Developer Account: starting at 125€ per month", "Shared Development Account: starting 200€ per month", "Full Development Account: starting 526€ per month"]
      },
      {
        "icon": "doc",
        "name": "Terms Of Service",
        "isInfoIcon": "true",
        "moreBtn": true,
        "info":[`Welcome to the "Power Platform Shared Account" service (the "Service"), provided by Team. Our Service offers a streamlined solution for customers to request and manage environments and licenses necessary for developing on the Power Platform. this innovative service is designed to facilitate your access to a suite of development tools, enabling you to create, collaborate, and deploy applications efficiently.`]
      }
    ]
  }
]