import classNames from 'classnames';
import React from 'react';
import Styles from './matamo-projects.scss';
// dna-container
import Caption from 'dna-container/Caption';

const MatamoProjects = () => {
  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
        <>
              <Caption title={'Matamo Projects'} />

              <div className={classNames(Styles.caption)}>
                <h3>Matamo Projects</h3>
              </div>

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
export default MatamoProjects;
