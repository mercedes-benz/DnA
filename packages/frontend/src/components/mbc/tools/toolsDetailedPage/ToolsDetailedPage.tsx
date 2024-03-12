import React, { useEffect, useState } from 'react';
import Styles from './ToolsDetailedPage.scss'
import Breadcrumb from './breadcrumb/BreadCrumb'
import LogoImage from '../../../mbc/createNewSolution/description/logoManager/LogoImage/LogoImage';
import { getParams } from '../../../../router/RouterUtils';
import { SOLUTION_LOGO_IMAGE_TYPES } from 'globals/constants';
import { ToolsDetailedPageElements } from 'globals/landingPageElements';
//import TagSection from 'components/mbc/shared/landingSummary/tagSection/TagSection';

export interface IData {
  id?: string;
  name?: string;
  description?: string;
  tags?: [];
  url?: string;
  useCases?: [];
  toolPipeLine?: {};
  documentation?: {};
  templates?: {};
  trainings?: {};
}

const UseCaseTile = (props: any) => {
  const item = { ...props.item };
  return (
    <div className={Styles.useCaseTile}>
      <div className={Styles.useCaseTileVal}>
        <div className={Styles.serviceIcon}>
          <i className={`icon mbc-icon ${item.icon}`} />
        </div>
        <div className={Styles.serviceName}>
          <span>
            {item.title}
          </span>
          <h5>{item.description}</h5>
        </div>
      </div>
    </div>
  )
}

const InfoTile = (props: any) =>{
  const item = { ...props.item};
  return(
    <div className={Styles.infoTile}>
      <div className={Styles.serviceIcon}>
          <i className={`icon mbc-icon ${item.icon}`} />
      </div>
    </div>
  )
}

const ToolsDetailedPage = (IData: any) => {
  const [pageDetails, setPageDetails] = useState(IData);
  //const [tags, setTags] = useState([]);
  const useCases = [{
    icon: 'chronos',
    title: 'Data Preparation & Wrangling',
    description: 'eg: bring your excel files into a common format',
  },
  {
    icon: 'solutionoverview',
    title: 'Data Analysis',
    description: 'Identify patterns in your data through exploration and visualization',
  },
  {
    icon: 'portfolio',
    title: 'Machine Learning',
    description: 'Build predictive Models with your data and share the results with others',
  },]

  const info = []

  useEffect(() => {
    const params = getParams();
    const id = params.id;
    for (const data of ToolsDetailedPageElements) {
      if (data.id === id) {
        setPageDetails(data);
      }
    }
  }, []);

  useEffect(() => {
    //setTags(pageDetails.tags);
    console.log(pageDetails);
  }, [pageDetails])



  return (
    <div className={Styles.pageWrapper}>
      <LogoImage
        displayType={SOLUTION_LOGO_IMAGE_TYPES.BANNER}
        className={Styles.pageBannerimg}
      />
      <div className={Styles.pageContentWrapper}>
        <div className={Styles.backButtonWapper}>
          <Breadcrumb>
            <li>Dataiku</li>
          </Breadcrumb>
          {/* <TagSection
            tags={tags.map((item :any) => item)}
          ></TagSection> */}
          <div className={Styles.pageBannerTitle}>
            <h2>{pageDetails.name}</h2>
          </div>
          <div className={Styles.pageDescription}>
            <p>{pageDetails.description}</p>
          </div>
        </div>
        <div className={Styles.contentSection}>
          <h4>Use Cases</h4>
          <div className={Styles.portHeader}>
            {useCases.map((item: any, key: any) =>
              <UseCaseTile item={item} key={key} />
            )}
          </div>
        </div>
        <div className={Styles.contenSection}>
          <h4>Tool Pipeline</h4>
          <div className={Styles.portHeader}>
            <div className={Styles.toolPipeLine}>

            </div>
          </div>
        </div>
        <div className={Styles.contenSection}>
          <div className={Styles.portHeader}>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ToolsDetailedPage;
