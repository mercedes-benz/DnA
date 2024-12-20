import React, { useEffect, useState } from 'react';
import Styles from './ToolsDetailedPage.scss'
import Breadcrumb from './breadcrumb/BreadCrumb'
import { getParams } from '../../../../router/RouterUtils';
import { ToolsPageImagesInfo } from 'globals/constants';
import { ToolsDetailedPageElements } from './toolDetaliedPageInfo';
import { history } from '../../../../router/History';
import { markdownParser } from '../../../../utils/MarkdownParser';
import SubscriptionCard from './SubscriptionCard/SubscriptionCard'

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
  return (
    <div className={Styles.infoTile}>
      {item.name !== 'classification' ? (
        <div className={Styles.serviceInfo}>
          <i className={`icon mbc-icon ${item.icon}`} />
          <div className={Styles.infoDescription}>
            <h3>{item.name}</h3>
            <h5>{item.description}</h5>
            {item.info && (
              <div className={Styles.infoContent}>
                {item.info.map((item: any, key: any) => {
                  return (<p key={key}>
                    {item}
                  </p>)
                })}
              </div>)
            }
            {item.links && (
              <div className={Styles.infoLinks}>
                {item.links.map((item: any, key: any) => {
                  return <a href={item.link} target="_blank" rel="noreferrer" key={key}>
                    {item.title}
                  </a>
                })}
              </div>)
            }
            {item.moreBtn && (
              <div className={Styles.moreBtn}>
                <button onClick={() => {localStorage.setItem('modal', 'tou'); history.push('/powerplatform')}}>More</button>
              </div>
            )}
          </div>
        </div>) : 
        <>
          <div className={Styles.serviceInfo}>
            <h3>classification</h3>
            <div className={Styles.classIcon}>
              <i className={`icon mbc-icon sec`} />
              <h4>{item.type}</h4>
            </div>
          </div>
        </>
      }
    </div>
  )
}


const AccessSteps = (props: any) => {
  const index = props.index;
  const item = props.item;
  return (
    <div className={Styles.AccessStep}>
      <div id="valueDriversWrapper" className={Styles.expansionListWrapper}>
        <div className="expansion-panel-group">
          <div id={'valueDriverPanel_' + index} className="expansion-panel">
            <span className="animation-wrapper" />
            <input type="checkbox" id={'checkbox_' + index} />
            <label className="expansion-panel-label" htmlFor={'checkbox_' + index}>
              {item.id}
              <i className="icon down-up-flip" />
            </label>
            <div className="expansion-panel-content">
              <div className={Styles.expansionnPanelContent}>
                <h5  dangerouslySetInnerHTML={{ __html: markdownParser(item.info) }}/>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};




const ToolsDetailedPage = (IData: any) => {
  const [pageDetails, setPageDetails] = useState(IData);
  const [bannerImageUrl, setBannerImageUrl] = useState('');
  const [pipeLineImage, setPipeLineImageUrl] = useState('');


  useEffect(() => {
    const params = getParams();
    const id = params.id;
    for (const data of ToolsDetailedPageElements) {
      if (data.id === id) {
        setPageDetails(data);
      }
    }

    for (const data of ToolsPageImagesInfo.images) {
      if (data.id === id) {
        setBannerImageUrl(`./${ToolsPageImagesInfo.folder}/${id}/${data.banner}`);
        setPipeLineImageUrl(`./${ToolsPageImagesInfo.folder}/${id}/${data.toolPipeline}`);
      }
    }


  }, []);


  return (
    <div>
      <div className={Styles.pageWrapper}>
        <img className={Styles.pageBannerimg} src={bannerImageUrl} />
        <div className={Styles.pageContentWrapper}>
          <div className={Styles.backButtonWapper}>
            <div className={Styles.pageHeader}>
              <div className={Styles.breadcrumb}>
                <Breadcrumb>
                  <li>{pageDetails.id}</li>
                </Breadcrumb>
              </div>
              <div className={Styles.tags}>
                {pageDetails.tags && (
                  pageDetails.tags.map((value: any, key: any) =>
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
            {pageDetails?.hasSubcription && (
              <>
                <SubscriptionCard />
                <p className={Styles.note}><sup>*</sup>Please be advised that the licensing cost may incur additional expenses.</p>
              </>
            )}
          </div>
          {pageDetails.useCases && (
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
          )}

          {pageDetails.toolPipeLine && (<div className={Styles.contentSection}>
            <h4>Tool Pipeline</h4>
            <div className={Styles.portHeader}>
              <div className={Styles.toolPipeLine}>
                <div className={Styles.pipeLineWrapper}>
                  <img className={Styles.pipeLineImage} src={pipeLineImage}></img>
                </div>
                <div className={Styles.pipeLineDescription}>
                  {pageDetails.toolPipeLine?.description && (
                    pageDetails.toolPipeLine.description.map((value: any, key: any) =>
                      <div className={Styles.tag} key={key}>
                        <h5>{value}</h5>
                      </div>
                    )
                  )}
                  <h4>Connected To</h4>
                  <div className={Styles.connectionWrapper}>
                    {pageDetails.toolPipeLine?.connectedTO && (
                      pageDetails.toolPipeLine.connectedTO.map((value: any, key: any) => {
                        return (
                          <div className={Styles.connectedToList} key={key}>
                            {/* <button
                          className={Styles.connectButton}
                          type="button"
                        > */}
                            <i className={`icon mbc-icon ${value.icon}`} />
                            <h5>{value.title}</h5>
                            {/* </button> */}

                          </div>
                        )
                      })
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>)}

          <div className={Styles.contentSection}>
            <div className={Styles.portHeader}>
              {pageDetails.info && (pageDetails.info.map((item: any, key: any) =>
                <InfoTile item={item} key={key} />
              ))}
            </div>
          </div>
          <div className={Styles.contentSection}>
            <div className={Styles.portHeader}>
              {pageDetails?.classification && (<div className={Styles.classificationSection}>
                <h3>classification</h3>
                <div className={Styles.classificationIcon}>
                  <i className={`icon mbc-icon sec`} />
                  <h4>{pageDetails.classification}</h4>
                </div>
              </div>)}
              {pageDetails?.accessSteps && (<div className={Styles.accessSection}>
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
                      <AccessSteps index={key} item={item} key={key} />
                    )
                  )}
                </div>
              </div>)}
            </div>
          </div>
        </div>
        <div>
        </div>
      </div>
      <div className={Styles.stickyPanel}>
        <div className={Styles.navButton}>
          <button className={'btn btn-tertiary'} onClick={() => pageDetails.isExternalLink ? window.open(pageDetails.url) : history.push(pageDetails.url)}>Open in browser</button>
        </div>
      </div>
    </div>
  );
};

export default ToolsDetailedPage;