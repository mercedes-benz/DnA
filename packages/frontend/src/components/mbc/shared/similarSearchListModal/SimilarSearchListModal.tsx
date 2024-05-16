// import classNames from 'classnames';
import React from 'react';
import Styles from './SimilarSearchListModal.scss';
import Modal from 'components/formElements/modal/Modal';
import { ISimilarSearchListItem } from 'globals/types';
import { attachEllipsis } from '../../../../services/utils';

interface SimilarSearchListModalProps {
  searchBasedOnInputType: string;
  selectedProductName?: string;
  similarSearchList: ISimilarSearchListItem[];
  setShowSimilarSearchListModal: (show: boolean) => void;
  isReportSearchType?: boolean;
}

const SimilarSearchListModal = (props: SimilarSearchListModalProps) => {
  const goToSummary = (productId: string) => {
    window.open(`${window.location.pathname}#/${props.isReportSearchType ? 'reportsummary' : 'summary'}/${productId}`, '_blank');
  };

  const { searchBasedOnInputType, selectedProductName } = props;

  return (
    <Modal
      title={`Similar ${props.isReportSearchType ? 'Reports' : 'Solutions'} found based on ${searchBasedOnInputType}`}
      showAcceptButton={false}
      showCancelButton={false}
      modalWidth="600px"
      buttonAlignment="center"
      show={true}
      scrollableContent={true}
      content={
        <div className={Styles.SimilarSearchListModal}>
          <p>
            Below are the similar {props.isReportSearchType ? 'Reports' : 'Solutions'} found based on the "{searchBasedOnInputType}" present in the{' '}
            {selectedProductName ? `${props.isReportSearchType ? 'report' : 'solution'} ` + selectedProductName : `currently creating/editing ${props.isReportSearchType ? 'report' : 'solution'}`}.
          </p>
          <div className={Styles.solContainer}>
            {props.similarSearchList.map((product) => {
              return (
                <div id={'card-' + product.id} key={product.id} className={Styles.solCard}>
                  <div className={Styles.solHead} onClick={() => goToSummary(product.id)}>
                    <div className={Styles.solTitleDiv}>
                      <div className={Styles.solTitle}>{product.productName} <i className='icon mbc-icon new-tab'/></div>
                    </div>
                    <div className={Styles.scoreIndicator}>Score: {(product.score * 100).toFixed(2)}%</div>
                  </div>
                  <div className={Styles.solbodysection}>
                    <div className={Styles.solInfo}>{attachEllipsis(searchBasedOnInputType === 'Business Need' ? product.businessNeed : product.description, 350)}</div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      }
      onCancel={() => props.setShowSimilarSearchListModal(false)}
    />
  );
};

export default SimilarSearchListModal;
