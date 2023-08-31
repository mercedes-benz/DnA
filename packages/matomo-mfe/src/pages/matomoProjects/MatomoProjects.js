import classNames from 'classnames';
import React from 'react';
import Styles from './matomo-projects.scss';
// dna-container
import Caption from 'dna-container/Caption';

const MatomoProjects = () => {
  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
        <>
              <Caption title={'Matomo Projects'} />

              <div className={Styles.allProjectContent}>
                <div className={Styles.newProjectCard}>
                  <div className={Styles.addicon}> &nbsp; </div>
                  <label className={Styles.addlabel}>Create new project</label>
                </div>
                <div></div>
              </div>
            </>
        </div>
      </div>
    </>
  );
};
export default MatomoProjects;
