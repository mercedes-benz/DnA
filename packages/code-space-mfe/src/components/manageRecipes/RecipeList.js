import cn from 'classnames';
import React, { useState } from 'react';
import Styles from './RecipeList.scss';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import ViewRecipe from '../codeSpaceRecipe/ViewRecipe';
import Modal from 'dna-container/Modal';
// import { history } from '../../store';

const RecipeList = ({ recipe, additionalServices }) => {
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const classNames = cn.bind(Styles);

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

  // const handleEditRecipe = (e) => {
  //   e.stopPropagation();
  //   history.push(`/codespaceRecipes/${recipe?.recipeName}`);
  // }

  const handleDeleteRecipe = (e) => {
    e.stopPropagation();
    setShowDeleteModal(true)
  }

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
      <tr 
        className={classNames('data-row', Styles.dataRow)}
        onClick={() => setShowDetailsModal(true)}>
        <td className={'wrap-text'}>
          <div className={Styles.securityConfigNameDivide}>{recipe.recipeName}</div>
        </td>
        <td className={'wrap-text'}>
          <span className={Styles.securityConfig}>
            {"DiskSpace- " + recipe.diskSpace + "GB" + " CPU- " + recipe.maxCpu + " RAM- " + convertRam(recipe.maxRam)+"GB"}
          </span>
        </td>
        <td className={'wrap-text'}>
          {chips}
        </td>
        <td className={'wrap-text'}>
          {chipsAdditionalServices}
        </td>
        <td className={'wrap-text'}>
          <span>{recipe.isPublic ? "Yes" : 'No'}</span>
        </td>
        <td className={classNames('wrap-text', Styles.actionColumn)}>
          <div>
          {/* <button className={'btn btn-primary ' + Styles.actionBtn} type="button" onClick={handleEditRecipe}>
              <i className='icon edit'></i>
            </button> */}
            <button className={'btn btn-primary ' + Styles.actionBtn} type="button" onClick={handleDeleteRecipe}>
              <i className='icon delete'></i>
            </button>
          </div>
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
export default RecipeList;
