import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from './CodeSpaceCardItem.scss';
import { regionalDateAndTimeConversionSolution } from '../../../../services/utils';
import ConfirmModal from '../../../../components/formElements/modal/confirmModal/ConfirmModal';
import { history } from '../../../../router/History';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { ICodeSpaceData } from '../CodeSpace';

interface CodeSpaceCardItemProps {
  codeSpace: ICodeSpaceData;
}

const CodeSpaceCardItem = (props: CodeSpaceCardItemProps) => {
  const codeSpace = props.codeSpace;
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const deleteCodeSpaceContent = (
    <div>
      <h3>Are you sure you want to delete {codeSpace.name} Code Space?</h3>
    </div>
  );

  const deleteCodeSpaceAccept = () => {
    ProgressIndicator.show();
    //ApiClient.deleteCodeSpace(codeSpace.id).then(() => {
      //codeSpace.getCodeSpaces();
      setShowDeleteModal(false);
    //});
  };
  const deleteCodeSpaceClose = () => {
    setShowDeleteModal(false);
  };

  const onCardNameClick = () => {
    history.push('codespace');
  }

  return (
    <>
      <div className={Styles.codeSpaceCard}>
        <div
          className={Styles.cardHead}
          onClick={() => {
            /* navigate to summary page*/
          }}
        >
          <div className={Styles.cardHeadInfo}>
            <div className={classNames('btn btn-text forward arrow', Styles.cardHeadTitle)} onClick={onCardNameClick}>
              {codeSpace?.name}
            </div>
          </div>
        </div>
        <hr />
        <div className={Styles.cardBodySection}>
          <div>
            <div>
              <div>Code Recipe</div>
              <div>{codeSpace.recipe}</div>
            </div>
            <div>
              <div>Environment</div>
              <div>{codeSpace.environment}</div>
            </div>
            <div>
              <div>Created on</div>
              <div>{regionalDateAndTimeConversionSolution(codeSpace?.createdDate)}</div>
            </div>
            <div>
              <div>Last Deployed on</div>
              <div>{regionalDateAndTimeConversionSolution(codeSpace?.lastDeployedDate)}</div>
            </div>
            <div>
              <div>Code Space ID</div>
              <div>{codeSpace.id}</div>
            </div>
          </div>
        </div>
        <div className={Styles.cardFooter}>
          <div>{codeSpace.deployed && <><span className={Styles.deployedIndicator}>Deployed</span><a href="#" className={Styles.deployedLink}><i className="icon mbc-icon new-tab"/></a></>}</div>
          <div className={Styles.btnGrp}>
            <button className="btn btn-primary hide" onClick={() => history.push(`/edit/${codeSpace.id}`)}>
              <i className="icon mbc-icon edit"></i>
            </button>
            <button className="btn btn-primary" onClick={() => setShowDeleteModal(true)}>
              <i className="icon delete"></i>
            </button>
          </div>
        </div>
      </div>
      <ConfirmModal
        title={''}
        acceptButtonTitle="Yes"
        cancelButtonTitle="No"
        showAcceptButton={true}
        showCancelButton={true}
        show={showDeleteModal}
        content={deleteCodeSpaceContent}
        onCancel={deleteCodeSpaceClose}
        onAccept={deleteCodeSpaceAccept}
      />
    </>
  );
};
export default CodeSpaceCardItem;
