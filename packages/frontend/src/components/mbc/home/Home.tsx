import React from 'react';
// import { Link } from 'react-router-dom';
import { IUserInfo } from 'globals/types';
import Styles from './Home.scss';
import { Envs } from 'globals/Envs';
import DNACard from 'components/card/Card';
import IconTransparency from 'components/icons/IconTransparency';
import IconData from 'components/icons/IconData';
import IconTools from 'components/icons/IconTools';
import IconTrainings from 'components/icons/IconTrainings';

export interface ILandingpageProps {
  user: IUserInfo;
}

const Home: React.FC<ILandingpageProps> = () => {
  // const [showInfoModal, setShowInfoModal] = useState<boolean>(false);

  return (
    <div>
      <div className={Styles.landingPage}>
        <div className={Styles.datmdcBanner}>
          <div className={Styles.bannerDesc}>
            <h2 className={Styles.appName}>{Envs.DNA_APPNAME_HOME}</h2>
            <h5 className={Styles.appDesc}>
              Self services, data insight and
              <br />
              analytics - all in one place.
            </h5>
          </div>
          <div className={Styles.dnaContainer}>
            <div className={Styles.dnaRow}>
              <div className={Styles.transparencyCol}>
                <h2 className={Styles.headline}>
                  <span>MB</span>
                  <span>Data- & AI-Solutions</span>
                </h2>
                <DNACard
                  title={'Transparency'}
                  description={'Explore all Data & AI Solutions in MB and view your Portfolio in a single click'}
                  url={'/transparency'}
                  isTextAlignLeft={false}
                  isDisabled={false}
                  svgIcon={<IconTransparency />}
                />
              </div>
              <div className={Styles.selfServiceCol}>
                <h2 className={Styles.headline}>
                  <span>FC</span>
                  <span>Self-Service Zone</span>
                </h2>
                <div className={Styles.dnaRow}>
                  <DNACard
                    title={'Data'}
                    description={'From Data Products to Data Governance - all you need to work is here'}
                    url={'/data'}
                    isTextAlignLeft={false}
                    isDisabled={false}
                    svgIcon={<IconData />}
                  />
                  <DNACard
                    title={'Tools'}
                    description={'Our standard Data & Analytics for both FC Users and Pro Developers'}
                    url={'/tools'}
                    isTextAlignLeft={false}
                    isDisabled={false}
                    svgIcon={<IconTools />}
                  />
                  <DNACard
                    title={'Trainings'}
                    description={'Data and Tools are not enough - here we enable you to become even more productive'}
                    url={'/trainings'}
                    isTextAlignLeft={false}
                    isDisabled={!Envs.ENABLE_TRAININGS}
                    svgIcon={<IconTrainings />}
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;
