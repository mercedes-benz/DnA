import cn from 'classnames';
import React, { useState } from 'react';
import Styles from './CodeSpaceList.scss';
import { history } from '../../store';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import ViewRecipe from '../codeSpaceRecipe/ViewRecipe';
import Modal from 'dna-container/Modal';

const CodeSpaceList = ({recipe, additionalServices, isConfigList}) => {
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const classNames = cn.bind(Styles);

  const onSecrityConfigClick = () => {
    history.push(`/codespace/adminSecurityconfig/${recipe?.recipeId}?name=${recipe?.recipeName}`);
  };

  const onNewRecipeClick = () => {
    setShowDetailsModal(true);
  };

  const chips =
    recipe?.software && recipe?.software?.length
      ? recipe?.software?.map((chip) => {
        return (
          <label key={chip} className={classNames('chips', Styles.chips)}>{chip}</label>
        );
      })
      : 'N/A';

  const chipsAdditionalServices =
    additionalServices && additionalServices?.length
      ? additionalServices?.map((chip) => {
        return (
          <label key={chip.serviceName} className={classNames('chips', Styles.chips)}>{chip?.serviceName} {chip?.version}</label>
        );
      })
      : 'N/A';

  const convertRam = (value) => {
    const ramValue = parseInt(value)/1000;
    return ramValue.toString();
  };

  const handleRecipeDelete = () => {
    ProgressIndicator.show();
    CodeSpaceApiClient.deleteCodeSpaceRecipe(recipe.recipeName)
      .then(() => {
        ProgressIndicator.hide();
        Notification.show("Recipe Deleted Successfully");
        setShowDeleteModal(false);
      }).catch((err) => {
        ProgressIndicator.hide();
        Notification.show(err?.response?.data?.errors[0]?.message, 'alert');
      });
  }

  return (
    <React.Fragment>
      <tr className={classNames('data-row', Styles.securityConfigRow)}>
        <td className={'wrap-text ' + classNames(Styles.securityConfigName)}>
          <div className={Styles.securityConfigNameDivide} onClick={isConfigList ? onSecrityConfigClick : onNewRecipeClick}>{recipe.recipeName}</div>
        </td>
        <td className={'wrap-text' + Styles.securityConfigCol}>
          <span className={Styles.securityConfig} onClick={isConfigList ? onSecrityConfigClick : onNewRecipeClick}>
            {"DiskSpace- " + recipe.diskSpace + "GB" + " CPU- " + recipe.maxCpu + " RAM- " + convertRam(recipe.maxRam)+"GB"}
          </span>
        </td>

        <td className={'wrap-text' + Styles.securityConfigCol} onClick={isConfigList ? onSecrityConfigClick : onNewRecipeClick}>
          {chips}
        </td>

        <td className={'wrap-text' + Styles.securityConfigCol} onClick={isConfigList ? onSecrityConfigClick : onNewRecipeClick}>
          {chipsAdditionalServices}
        </td>

        <td className={'wrap-text' + Styles.securityConfigCol} onClick={isConfigList ? onSecrityConfigClick : onNewRecipeClick}>
          <span>{recipe.isPublic ? "Yes" : 'No'}</span>
        </td>
        <td className={'wrap-text' + Styles.securityConfigCol + Styles.actionColumn}>
          <button
            className={
              'btn btn-primary ' + Styles.actionBtn
            }
            type="button"
            onClick={() => setShowDeleteModal(true)}
          >
            <i className='icon delete'></i>
          </button>

        </td>
      </tr>
      {showDetailsModal && (
        <Modal
          title={''}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth="60vw"
          show={showDetailsModal}
          scrollableContent={true}
          content={<ViewRecipe recipe={recipe} additionalServices={additionalServices} />}
          onCancel={() => {
            setShowDetailsModal(false);
          }}
        />
      )}
      {showDeleteModal && 
        <Modal
          title="Delete Recipe"
          show={showDeleteModal}
          showAcceptButton={false}
          showCancelButton={false}
          scrollableContent={false}
          hideCloseButton={true}
          content={
            <div>
              <header>
                <button className="modal-close-button" onClick={() => setShowDeleteModal(false)}><i className="icon mbc-icon close thin"></i></button>
              </header>
              <div>
                <p>The Recipe will be deleted permanently. Are you sure you want to delete it?</p>
              </div>
              <div className="btn-set footerRight">
                <button className="btn btn-primary" type="button" onClick={() => setShowDeleteModal(false)}>Cancel</button>
                <button className="btn btn-tertiary" type="button" onClick={handleRecipeDelete}>Confirm</button>
              </div>
            </div>
          } 
        />
      }
    </React.Fragment>
  );
};
export default CodeSpaceList;
