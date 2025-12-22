
import React, { useEffect} from 'react';
import Styles from './UiliciousCardItem.scss';
//import { history } from '../../../router/History';
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import { Envs } from 'globals/Envs';
import { IUiliciousLeanGovernance } from 'globals/types';

interface IUiliciousProjectDetail {
  spaceId: string,
  spaceName: string,
  userRole: string,
  leanGovernance?: IUiliciousLeanGovernance
}

interface Props {
  project: IUiliciousProjectDetail;
  onEditWorkspace: (spaceId: string, leanGovernance:IUiliciousLeanGovernance) => void;
}

const UiliciousCardItem = ({ project, onEditWorkspace}: Props) => {

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
        <div className={Styles.cardFooter}>
          <div>&nbsp;</div>
          <div className={Styles.btnGrp}>
            <button className="btn btn-primary" onClick={() => onEditWorkspace(project?.spaceId, project?.leanGovernance)}>
              <i className="icon mbc-icon edit"></i>
            </button>
            {/* <button className="btn btn-primary" onClick={() => {}} disabled={true}>
              <i className="icon delete"></i>
            </button> */}
          </div>
        </div>
      </div>
    </>
  );
};

export default UiliciousCardItem;

