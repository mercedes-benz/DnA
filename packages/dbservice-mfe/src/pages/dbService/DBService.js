import classNames from 'classnames';
import React, { useEffect } from 'react';
import Styles from './db-service.scss';
import Caption from 'dna-container/Caption';
// utils
import Tooltip from '../../common/modules/uilab/js/src/tooltip';

const DBService = () => {
  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  return (
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          <Caption title={`DB Service`} />
        </div>
      </div>
    </React.Fragment>
  );
}
export default DBService;
