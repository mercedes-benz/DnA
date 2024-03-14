import React, { useEffect, useState } from 'react';
import Styles from './ToolsDetailedPage.scss'
import Breadcrumb from './breadcrumb/BreadCrumb'
import LogoImage from '../../../mbc/createNewSolution/description/logoManager/LogoImage/LogoImage';
import { getParams } from '../../../../router/RouterUtils';
import { SOLUTION_LOGO_IMAGE_TYPES } from 'globals/constants';
import { ToolsDetailedPageElements } from 'globals/landingPageElements';
import { history } from '../../../../router/History';
//import TagSection from 'components/mbc/shared/landingSummary/tagSection/TagSection';

export interface IData {
  id?: string;
  name?: string;
  description?: string;
  tags?: [];
  url?: string;
  isExternalLink?: string;
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

const InfoTile = (props: any) => {
  const item = { ...props.item };
  const links = [...item.links];
  console.log(links);
  return (
    <div className={Styles.infoTile}>
      <div className={Styles.serviceInfo}>
        <i className={`icon mbc-icon ${item.icon}`} />
        <div className={Styles.infoDescription}>
          <h3>{item.name}</h3>
          <h5>{item.description}</h5>
          <div className={Styles.infoLinks}>
          {item.links.map((item : any, key : any)=>{
             return  <a href={item.link} target="_blank" rel="noreferrer" key={key}>
             {item.title}
           </a>
          })}
          </div>
        </div>
      </div>
    </div>
  )
}

const AccessSteps = (props: any) => {
  const item = { ...props.item };
  return (
    <div className={Styles.AccessStep}>
      <div id="valueDriversWrapper" className={Styles.expansionListWrapper}>
        <div className="expansion-panel-group">
          <div id={'valueDriverPanel_' + props.key} key={props.key} className="expansion-panel">
            <span className="animation-wrapper" />
            <input type="checkbox" id={props.key} />
            <label className="expansion-panel-label" htmlFor={props.key}>
              {item.id}
              <i className="icon down-up-flip" />
            </label>
          </div>
          <div className="expansion-panel-content">
            <div className={Styles.expansionnPanelContent}>
              <h5>
                {item.info}
              </h5>
            </div>
          </div>
        </div>
      </div>
    </div>
  )

}


const ToolsDetailedPage = (IData: any) => {
  const [pageDetails, setPageDetails] = useState(IData);
  //const [tags, setTags] = useState([]);
  

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
    console.log(pageDetails.tags);

  }, [pageDetails])



  return (
    <div className={Styles.pageWrapper}>
      <LogoImage
        displayType={SOLUTION_LOGO_IMAGE_TYPES.BANNER}
        className={Styles.pageBannerimg}
      />
      <div className={Styles.pageContentWrapper}>
        <div className={Styles.backButtonWapper}>
          <div className={Styles.pageHeader}>
            <div className={Styles.breadcrumb}>
              <Breadcrumb>
                <li>Dataiku</li>
              </Breadcrumb>
            </div>
            <div className={Styles.tags}>
              {pageDetails.tags &&(
                pageDetails.tags.map((value : any , key :any) =>
                <div className={Styles.tag} key={key}>
                  <h5>{value}</h5>
                </div>
                )
              )}
            </div>
          </div>
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
            {pageDetails.useCases && (
              pageDetails.useCases.map((item: any, key: any) =>
                <UseCaseTile item={item} key={key} />
              )
            )}
          </div>
        </div>
        <div className={Styles.contentSection}>
          <h4>Tool Pipeline</h4>
          <div className={Styles.portHeader}>
            <div className={Styles.toolPipeLine}>

            </div>
          </div>
        </div>
        <div className={Styles.contentSection}>
          <div className={Styles.portHeader}>
            {pageDetails.info && (pageDetails.info.map((item: any, key: any) =>
              <InfoTile item={item} key={key} />
            ))}
          </div>
        </div>
        <div className={Styles.contentSection}>
          <div className={Styles.portHeader}>
            <div className={Styles.classificationSection}>
              <h3>classification</h3>
            </div>
            <div className={Styles.accessSection}>
              <div className={Styles.serviceInfo}>
                <div className={Styles.serviceIcon}>
                  <i className={`icon mbc-icon portfolio`} />
                </div>
                <h3>How To Access</h3>
                <h5>Follow these simple steps to gain access to this tool</h5>
              </div>
              <div className={Styles.accessSteps}>
                {pageDetails.accessSteps && (
                  pageDetails.accessSteps.map((item: any, key: any) =>
                    <AccessSteps item={item} key={key} />
                  )
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
      <div>
        <div style={{ textAlign: 'right', marginTop: '20px', marginRight: '20px' }}>
          <button className={'btn btn-tertiary'} onClick={() => pageDetails.isExternalLink ? window.open(pageDetails.url) : history.push(pageDetails.url)}>Open in Browser</button>
        </div>
      </div>
    </div>
  );
};

export default ToolsDetailedPage;
