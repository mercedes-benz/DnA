import * as React from 'react';
import { Link } from 'react-router-dom';
import { IUserInfo } from 'globals/types';
import Styles from './Home.scss';
import { Envs } from 'globals/Envs';

interface ILandingpageState {
  showInfoModal: boolean;
}

export interface ILandingpageProps {
  user: IUserInfo;
}

export default class Home extends React.Component<ILandingpageProps, ILandingpageState> {
  constructor(props: ILandingpageProps) {
    super(props);
    this.state = {
      showInfoModal: false,
    };
  }

  public render() {
    return (
      <div>
        <div className={Styles.landingPage}>
          <div className={Styles.datmdcBanner}>
            <div className={Styles.bannerDesc}>
              <h2>{Envs.DNA_APPNAME_HOME}</h2>
              <h5>Your solutions at a glance.</h5> <br />
              <Link to="portfolio">
                <button className={'btn btn-tertiary ' + Styles.btnPrimary}>Open Portfolio</button>
              </Link>
            </div>
            <div className={`${Styles.bannerQuickLinks} ${!Envs.ENABLE_REPORTS ? Styles.noReportsSection : ''}`}>
              <div className={Styles.quicklinkCard}>
                <div className={Styles.quicklinkCardtitle + ' ' + Styles.qcardOne}>
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
                        <Link to="dataproduct/datacompliancenetworklist">
                          <span>
                            Data Compliance Network List
                            <i className="icon mbc-icon arrow small right" />
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
            </div>
          </div>
        </div>
      </div>
    );
  }
}
