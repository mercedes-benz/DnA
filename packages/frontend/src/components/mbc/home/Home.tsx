import * as React from 'react';
import { Link } from 'react-router-dom';
import { IRole, IUserInfo } from '../../../globals/types';
import Styles from './Home.scss';
import { InfoModal } from '../../../components/formElements/modal/infoModal/InfoModal';
import { USER_ROLE } from '../../../globals/constants';
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
    const contentForInfoModal = <div dangerouslySetInnerHTML={{ __html: Envs.DNA_CONTACTUS_HTML }}></div>;

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
            <div className={Styles.bannerQuickLinks}>
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
                      {this.props.user.roles.find((role: IRole) => role.id === USER_ROLE.ADMIN) !== undefined ? (
                        <Link to="Administration">
                          <span>
                            Administration <i className="icon mbc-icon arrow small right" />
                          </span>
                        </Link>
                      ) : null}
                    </li>
                    <li>
                      <Link to="createnewsolution">
                        <span>
                          Create new Solution <i className="icon mbc-icon arrow small right" />
                        </span>
                      </Link>
                    </li>
                  </ul>
                </div>
              </div>
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
                    <li>
                      <Link to="services">
                        <span>&nbsp;</span>
                      </Link>
                    </li>
                  </ul>
                </div>
              </div>
              <div className={Styles.quicklinkCard + ' ' + Styles.BgTrans}>
                <div className={Styles.quicklinkCardtitle + ' ' + Styles.qcardThree}>
                  <h6>Help Center</h6>
                  <p>We are here to help</p>
                </div>
                <div className={Styles.quicklinkCardNav}>
                  <ul className={Styles.qcardThreeul}>
                    <li onClick={this.showInfoModal}>
                      <Link to="#">
                        <span>
                          Contact Us <i className="icon mbc-icon arrow small right " />
                        </span>
                      </Link>
                    </li>
                    <li>
                      <Link to="license">
                        <span>
                          License <i className="icon mbc-icon arrow small right" />
                        </span>
                      </Link>
                    </li>
                    {/* <li className={Styles.isDisable}>
                      <Link to="#">
                        <span>
                          Community <i className="icon mbc-icon arrow small right" />
                        </span>
                      </Link>
                    </li>
                    <li className={Styles.isDisable}>
                      <Link to="#">
                        <span>
                          FAQ <i className="icon mbc-icon arrow small right" />
                        </span>
                      </Link>
                    </li> */}
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>
        <InfoModal
          title={'Contact Us'}
          modalWidth={'35vw'}
          show={this.state.showInfoModal}
          content={contentForInfoModal}
          onCancel={this.onInfoModalCancel}
        />
      </div>
    );
  }

  protected showInfoModal = () => {
    this.setState({ showInfoModal: true });
  };

  protected onInfoModalCancel = () => {
    this.setState({ showInfoModal: false });
  };
}
