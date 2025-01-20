import cn from 'classnames';
import React from 'react';
import Styles from './RecipeList.scss';
import { history } from '../../store';

const RecipeList = ({ recipe, additionalServices, onDeleteRecipe, onSelectRecipe }) => {
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

  const handleEditRecipe = (e) => {
    e.stopPropagation();
    history.push(`/codespaceRecipes/${recipe?.id}`);
  }

  const handleDeleteRecipe = (e) => {
    e.stopPropagation();
    onDeleteRecipe(recipe);
  }

  return (
    <React.Fragment>
      <tr 
        className={classNames('data-row', Styles.dataRow)}
        onClick={() => onSelectRecipe(recipe)}>
        <td className={'wrap-text'}>
          <div className={Styles.securityConfigNameDivide}>{recipe.recipeName}</div>
        </td>
        <td className={'wrap-text'}>
          <span className={Styles.securityConfig}>
            Disk Space: {recipe.diskSpace}GB | CPU: {recipe.maxCpu}GHz | RAM: {convertRam(recipe.maxRam)}GB
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
            <button className={'btn btn-primary ' + Styles.actionBtn} type="button" onClick={handleEditRecipe}>
              <i className='icon edit'></i>
            </button>
            <button className={'btn btn-primary ' + Styles.actionBtn} type="button" onClick={handleDeleteRecipe}>
              <i className='icon delete'></i>
            </button>
          </div>
        </td>
      </tr>
    </React.Fragment>
  );
};
export default RecipeList;
