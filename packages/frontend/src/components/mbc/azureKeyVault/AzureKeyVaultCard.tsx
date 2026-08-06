
import React, { useEffect} from 'react';
import Styles from './AzureKeyVaultCard.scss';
//import { history } from '../../../router/History';
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import { Envs } from 'globals/Envs';
import { regionalDateAndTimeConversion } from '../../../../src/services/utils';
import { IKeyVault } from 'globals/types';

interface Props {
  project: IKeyVault;
  onEditWorkspace: (project: IKeyVault) => void;
}

const AzureKeyVaultCard = ({ project, onEditWorkspace}: Props) => {

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
            window.open(Envs.AZURE_KEY_VAULT_URL+'/'+project?.keyVaultName+'/overview', '_blank');
          }}
        >
          <div className={Styles.cardHeadInfo}>
            <div>
              <div className={Styles.cardHeadTitle}>{project?.keyVaultName}</div>
              <div className="btn btn-text forward arrow"></div>
            </div>
          </div>
        </div>
        <hr />
        <div className={Styles.cardBodySection}>
          <div>
            <div>
              <div>Created By</div>
              <div>{project?.createdBy?.firstName + ' ' + project?.createdBy?.lastName}</div>
            </div>
            <div>
              <div>Collaborators</div>
              <div>{project?.collaborators?.map((item) => item.displayName || item.identifier).join(', ') || 'None'}</div>
            </div>
            <div>
              <div>Create On</div>
              <div>{regionalDateAndTimeConversion(project?.createdOn)}</div>
            </div>
          </div>
        </div>
        <div className={Styles.cardFooter}>
          <div>&nbsp;</div>
          <div className={Styles.btnGrp}>
            <button className="btn btn-primary" onClick={() => onEditWorkspace(project)}>
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

export default AzureKeyVaultCard;
