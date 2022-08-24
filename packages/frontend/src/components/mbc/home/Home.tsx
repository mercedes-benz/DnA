import * as React from 'react';
import { Link } from 'react-router-dom';
import { IUserInfo } from '../../../globals/types';
import Styles from './Home.scss';
import { Envs } from '../../../globals/Envs';

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
                  <h6>Solutions</h6>
                  <p> Easily manage and share your solutions</p>
                </div>
                <div className={Styles.quicklinkCardNav}>
                  <ul>
                    <li>
                      <Link to="allsolutions">
                        <span>
                          All solutions <i className="icon mbc-icon arrow small right" />
                        </span>
                      </Link>
                    </li>
                    <li>
                      <Link to="createnewsolution">
                        <span>
                          Create new Solution <i className="icon mbc-icon arrow small right" />
                        </span>
                      </Link>
                    </li>
                    <li>
                      <Link to="dataproduct">
                        <span>
                          Provide Data Product <i className="icon mbc-icon arrow small right" />
                        </span>
                      </Link>
                    </li>
                    <li>
                      <Link to="datacompliancenetworklist">
                        <span>
                          Data Compliance Network List <i className="icon mbc-icon arrow small right" />
                        </span>
                      </Link>
                    </li>
                  </ul>
                </div>
              </div>
              {Envs.ENABLE_REPORTS && (
                <div className={Styles.quicklinkCard}>
                  <div className={Styles.quicklinkCardtitle + ' ' + Styles.qcardThree}>
                    <h6>Reports</h6>
                    <p>Quick access to any report running on any system</p>
                  </div>
                  <div className={Styles.quicklinkCardNav}>
                    <ul className={Styles.qcardThreeul}>
                      <li>
                        <Link to="allreports">
                          <span>
                            All Reports <i className="icon mbc-icon arrow small right " />
                          </span>
                        </Link>
                      </li>
                      <li>
                        <Link to="createnewreport">
                          <span>
                            Create Reports <i className="icon mbc-icon arrow small right" />
                          </span>
                        </Link>
                      </li>
                    </ul>
                  </div>
                </div>
              )}
              <div className={Styles.quicklinkCard}>
                <div className={Styles.quicklinkCardtitle + ' ' + Styles.qcardTwo}>
                  <h6>Workspaces &amp; Services</h6>
                  <p> Start working in a safe training environment and subscribe to services</p>
                </div>
                <div className={Styles.quicklinkCardNav}>
                  <ul>
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
                          My Services
                          <i className="icon mbc-icon arrow small right" />
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
