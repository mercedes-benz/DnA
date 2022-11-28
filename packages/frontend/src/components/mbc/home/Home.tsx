import React from 'react';
// import { Link } from 'react-router-dom';
import { IUserInfo } from 'globals/types';
import Styles from './Home.scss';
import { Envs } from 'globals/Envs';
import DNACard from 'components/card/Card';

export interface ILandingpageProps {
  user: IUserInfo;
}

const Home:React.FC<ILandingpageProps> = () => {

  // const [showInfoModal, setShowInfoModal] = useState<boolean>(false);

  return (
    <div>
      <div className={Styles.landingPage}>
        <div className={Styles.datmdcBanner}>
          <div className={Styles.bannerDesc}>
            <h2 className={Styles.appName}>{Envs.DNA_APPNAME_HOME}</h2>
            <h5 className={Styles.appDesc}>Self services, data insight and<br />
                analytics - all in one place.</h5>
          </div>
          <div className={Styles.dnaContainer}>
            <div className={Styles.dnaRow}>
              <div className={Styles.transparencyCol}>
                <h2 className={Styles.headline}>All Data- & AI- Solutions in MB</h2>
                <DNACard
                  title={'Transparency'}
                  description={'Explore all Data & AI Solutions in MB and view your Portfolio in a single click'}
                  url={'/transparency'}
                  isTextAlignLeft={false}
                  isDisabled={false}
                  isSmallCard={false}
                  isMediumCard={true} />
              </div>
              <div className={Styles.selfServiceCol}>
                <h2 className={Styles.headline}>FC Self-Service Zone for Data Insights</h2>
                <div className={Styles.dnaRow}>
                  <DNACard
                    title={'Data'}
                    description={'From Data Products to Data Governance - all you need to work is here'}
                    url={'/data'}
                    isTextAlignLeft={false}
                    isDisabled={false}
                    isSmallCard={false}
                    isMediumCard={true} />
                  <DNACard
                    title={'Tools'}
                    description={'Both FC users and Pro Developers find our standard Data & Analytics tools here'}
                    url={'/tools'}
                    isTextAlignLeft={false}
                    isDisabled={false}
                    isSmallCard={false}
                    isMediumCard={true} />
                  <DNACard
                    title={'Trainings'}
                    description={'Data and Tools are not enough - here we enable you to become even more productive'}
                    url={'/trainings'}
                    isTextAlignLeft={false}
                    isDisabled={!Envs.ENABLE_TRAININGS}
                    isSmallCard={false}
                    isMediumCard={true} />
                  </div>
              </div>
            </div>
          </div>
          {/* <div className={`${Styles.bannerQuickLinks} ${!Envs.ENABLE_REPORTS ? Styles.noReportsSection : ''}`}>
            <div className={Styles.quicklinkCard}>
              <div className={Styles.quicklinkCardtitle}>
                <div>
                  <i className="icon mbc-icon solutions" />
                </div>
                <div>
                  <h6>Solution Transparency</h6>
                  <p> Overview of running solutions and reports</p>
                </div>
              </div>
              <div className={Styles.quicklinkCardNav}>
                <ul>
                  <li>
                    <Link to="allsolutions">
                      <span>
                        All Solutions <i className="icon mbc-icon arrow small right" />
                      </span>
                    </Link>
                  </li>
                  <li>
                    <Link to="allreports">
                      <span>
                        All Reports <i className="icon mbc-icon arrow small right" />
                      </span>
                    </Link>
                  </li>
                  <li>
                    <Link to="createnewsolution">
                      <span>
                        Create Solution <i className="icon mbc-icon arrow small right" />
                      </span>
                    </Link>
                  </li>

                  <li>
                    <Link to="createnewreport">
                      <span>
                        Create Report <i className="icon mbc-icon arrow small right" />
                      </span>
                    </Link>
                  </li>
                </ul>
              </div>
            </div>
            {Envs.ENABLE_REPORTS && (
              <div className={Styles.quicklinkCard}>
                <div className={Styles.quicklinkCardtitle + ' ' + Styles.qcardThree}>
                  <div>
                    <i className="icon mbc-icon document" />
                  </div>
                  <div>
                    <h6>Data Transfers</h6>
                    <p>A22 Minimal Information Documentation</p>
                  </div>
                </div>
                <div className={Styles.quicklinkCardNav}>
                  <ul>
                    <li>
                      <Link to="dataproduct">
                        <span>
                          Find Data Transfer <i className="icon mbc-icon arrow small right " />
                        </span>
                      </Link>
                    </li>
                    <li>
                      <Link to="dataproduct/create">
                        <span>
                          Provide Data Transfer
                          <i className="icon mbc-icon arrow small right" />
                        </span>
                      </Link>
                    </li>
                    <li>
                      <Link to="dataproduct/datacompliancenetworklist">
                        <span>
                          Data Compliance Network List
                          <i className="icon mbc-icon arrow small right" />
                        </span>
                      </Link>
                    </li>
                  </ul>
                </div>
              </div>
            )}
            <div className={Styles.quicklinkCard}>
              <div className={Styles.quicklinkCardtitle + ' ' + Styles.qcardTwo}>
                <div>
                  <i className="icon mbc-icon workspace" />
                </div>
                <div>
                  <h6>Self Services</h6>
                  <p> Workflows for Data Workers</p>
                </div>
              </div>
              <div className={Styles.quicklinkCardNav}>
                <ul className={Styles.qcardTwoli}>
                  <li>
                    <Link to="workspaces">
                      <span>
                        My Workspaces <i className="icon mbc-icon arrow small right" />
                      </span>
                    </Link>
                  </li>
                  <li>
                    <Link to="services">
                      <span>
                        My Services <i className="icon mbc-icon arrow small right" />
                      </span>
                    </Link>
                  </li>
                </ul>
              </div>
            </div>
          </div> */}
        </div>
      </div>
    </div>
  );
}

export default Home;