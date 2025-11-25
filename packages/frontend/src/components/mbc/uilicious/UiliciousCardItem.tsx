
import React, { useEffect} from 'react';
import Styles from './UiliciousCardItem.scss';
//import { history } from '../../../router/History';
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import { Envs } from 'globals/Envs';

interface IUiliciousProjectDetail {
  spaceId: string,
  spaceName: string,
  userRole: string
}

interface Props {
  project: IUiliciousProjectDetail;
}

const UiliciousCardItem = ({ project}: Props) => {

  useEffect(() => {
    SelectBox.defaultSetup();
    Tooltip.defaultSetup();
  }, []);

  return (
    <>
      <div className={Styles.projectCard}>
        <div
          className={Styles.cardHead}
          onClick={() => {
            window.open(Envs.UILICIOUS_URL, '_blank');
          }}
        >
          <div className={Styles.cardHeadInfo}>
            <div>
              <div className={Styles.cardHeadTitle}>{project?.spaceName}</div>
              <div className="btn btn-text forward arrow"></div>
            </div>
          </div>
        </div>
        <hr />
        <div className={Styles.cardBodySection}>
          <div>
            <div>
              <div>Space Id</div>
              <div>{project?.spaceId}</div>
            </div>
            <div>
              <div>Permission</div>
              <div>{project?.userRole}</div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default UiliciousCardItem;

