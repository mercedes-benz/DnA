import classNames from 'classnames';
import React, { useEffect } from 'react';
import Styles from './recipe-card.scss';
import { useHistory } from 'react-router-dom';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';

const RecipeCard = ({recipe, additionalServices, onSelectRecipe, onDeleteRecipe}) => {
  const history = useHistory();
  
  useEffect(() => {
    Tooltip.defaultSetup();
  }, [recipe]);

  const handleOpenWorkspace = () => {
    onSelectRecipe(recipe);
  }

  const convertRam = (value) => {
    const ramValue = parseInt(value)/1000;
    return ramValue.toString();
  };

const chipsAdditionalServices =
  additionalServices && additionalServices?.length
    ? <label className={classNames('chips', Styles.chips)}>{additionalServices[0]?.serviceName} {additionalServices[0]?.version}</label> + '...'
    : 'N/A';

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
            {console.log('chips', recipe.software[0])}
            <div>
              {recipe?.software && recipe?.software?.length
                ? <label className={classNames('chips', Styles.chips)}>{recipe?.software[0]}</label>
                : 'N/A'}
            </div>
          </div>
          <div>
            <div>Additional Services</div>
            <div>{chipsAdditionalServices}</div>
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
              onClick={() => history.push(`/codespaceRecipes/${recipe?.recipeName}`)}
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