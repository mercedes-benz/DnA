import classNames from 'classnames';
import React, { useEffect } from 'react';
import Styles from './recipe-card.scss';
import { useHistory } from 'react-router-dom';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';

const RecipeCard = ({recipe, additionalServices, onSelectRecipe, onDeleteRecipe}) => {
  const history = useHistory();
  
  useEffect(() => {
    Tooltip.defaultSetup();
  }, [recipe, additionalServices]);

  const handleOpenWorkspace = () => {
    onSelectRecipe(recipe);
  }

  const convertRam = (value) => {
    const ramValue = parseInt(value)/1000;
    return ramValue.toString();
  };

  return (
    <div className={classNames(Styles.projectCard)}>
      <div className={Styles.cardHead}>
        <div className={classNames(Styles.cardHeadInfo)}>
          <div
            className={classNames('btn btn-text forward arrow', Styles.cardHeadTitle)}
            onClick={handleOpenWorkspace}
          >
            {recipe?.recipeName}
          </div>
        </div>
      </div>
      <hr />
      <div className={Styles.cardBodySection}>
        <div>
          <div>
            <div>Hardware Configuration</div>
            <div>
              Disk Space: {recipe?.diskSpace}GB | CPU: {recipe?.maxCpu}GHz | RAM: {convertRam(recipe?.maxRam)}GB
            </div>
          </div>
          <div>
            <div>Software Configuration</div>
            <div>
              {recipe?.software && recipe?.software.length > 0 ? (
                <label className={classNames('chips', Styles.reChips)}>
                  {recipe.software[0]}{" "}
                  {recipe.software.length > 1 && (
                    <span className={Styles.spanMore} onClick={() => onSelectRecipe(recipe)}>
                      +{recipe.software.length - 1} more
                    </span>
                  )}
                </label>
              ) : (
                "N/A"
              )}
            </div>
          </div>
          <div>
            <div>Additional Services</div>
            <div>
              {additionalServices && additionalServices?.length > 0 ? (
                <label className={classNames('chips', Styles.reChips)}>
                  {additionalServices[0]?.serviceName}{" "}
                  {additionalServices?.length > 1 && (
                    <span className={Styles.spanMore} onClick={() => onSelectRecipe(recipe)}>
                      +{additionalServices?.length - 1} more
                    </span>
                  )}
                </label>
              ) : (
                "N/A"
              )}
            </div>
          </div>
        </div>
      </div>
      <div className={Styles.cardFooter}>
        <>
          <div>&nbsp;</div>
          <div className={Styles.btnGrp}>
            <button
              className={'btn btn-primary'}
              type="button"
              onClick={() => history.push(`/codespaceRecipes/${recipe?.id}`)}
            >
              <i className="icon mbc-icon edit"></i>
              <span>Edit</span>
            </button>
            <button
              className={'btn btn-primary'}
              type="button"
              onClick={() => onDeleteRecipe(recipe)}
            >
              <i className="icon delete"></i>
              <span>Delete</span>
            </button>
          </div>
        </>
      </div>
    </div>
  );
};
export default RecipeCard;