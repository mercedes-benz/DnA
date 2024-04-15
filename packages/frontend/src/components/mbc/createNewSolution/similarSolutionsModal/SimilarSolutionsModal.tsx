// import classNames from 'classnames';
import React from 'react';
import Styles from './SimilarSolutionsModal.scss';
import Modal from 'components/formElements/modal/Modal';
import { ISimilarSolutionsListItem } from 'globals/types';
import { attachEllipsis } from '../../../../services/utils';

interface SimilarSolutionsModalProps {
  searchBasedOnInputType: string;
  selectedSolutionName?: string;
  similarSolutionsList: ISimilarSolutionsListItem[];
  setShowSimilarSolutionsModal: (show: boolean) => void;
}

const SimilarSolutionsModal = (props: SimilarSolutionsModalProps) => {

  const goToSummary = (solutionId: string) => {
    window.open(`${window.location.pathname}#/summary/${solutionId}`, '_blank');
  };

  return (
    <Modal
      title={'Similar Solutions found based on ' + props.searchBasedOnInputType}
      showAcceptButton={false}
      showCancelButton={false}
      modalWidth="600px"
      buttonAlignment="center"
      show={true}
      scrollableContent={true}
      content={
        <div className={Styles.SimilarSolutionsModal}>
          <p>
            Below are the top 5 similar solutions found based on the "{props.searchBasedOnInputType}" present in the {props.selectedSolutionName ? ("solution " + props.selectedSolutionName) : "currently creating/editing solution"}.
          </p>
          <div className={Styles.solContainer}>
            {props.similarSolutionsList.map((solution) => {
              return <div id={'card-' + solution.id} key={solution.id} className={Styles.solCard}>
              <div className={Styles.solHead} onClick={()=> goToSummary(solution.id)}>
                <div className={Styles.solTitleDiv}>
                  <div className={Styles.solTitle}>{solution.productName}</div>
                </div>
                <div className={Styles.scoreIndicator}>
                  Score: {(solution.score * 100).toFixed(2)}%
                </div>
              </div>
              <div className={Styles.solbodysection}>
                <div className={Styles.solInfo}>{attachEllipsis(solution.description, 350)}</div>
              </div>
            </div>;
            })}
          </div>
        </div>
      }
      onCancel={() => props.setShowSimilarSolutionsModal(false)}
    />
  );
};

export default SimilarSolutionsModal;
