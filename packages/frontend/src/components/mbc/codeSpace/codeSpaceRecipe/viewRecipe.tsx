import cn from 'classnames';
import React, { useEffect, useState } from 'react';
import { ICodeCollaborator } from 'globals/types';
import { CodeSpaceApiClient } from '../../../../services/CodeSpaceApiClient';
import { ProgressIndicator } from '../../../../assets/modules/uilab/bundle/js/uilab.bundle';
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import Styles from './viewRecipe.scss';

export interface IViewRecipeProps {
  recipeName: string;
}

export interface IRecipeField {
  createdBy: ICodeCollaborator;
  createdOn: string;
  diskSpace: string;
  maxCpu: string;
  maxRam: string;
  minCpu: string;
  minRam: string;
  // isPublic: boolean,
  oSName: string;
  recipeName: string;
  recipeType: string;
  repodetails: string;
  software: string[];
}

const viewRecipe = (props: IViewRecipeProps) => {
  const [recipeField, setRecipeField] = useState<IRecipeField>();
  const classNames = cn.bind(Styles);
  useEffect(() => {
    getCodeSpaceRecipe(props.recipeName);
  }, []);

  const getCodeSpaceRecipe = (recipeName: string) => {
    ProgressIndicator.show();
    CodeSpaceApiClient.getCodeSpaceRecipe(recipeName)
      .then((res: any) => {
        ProgressIndicator.hide();
        setRecipeField(res.data);
      })
      .catch((error) => {
        Notification.show(error.message, 'alert');
      });
  };
  const chips =
    recipeField?.software && recipeField?.software?.length
        ? recipeField?.software?.map((chip) => {
            return (
              <>
                <label className="chips">{chip}</label>&nbsp;&nbsp;
              </>
            );
          })
        : 'N/A';
  return (
    <React.Fragment>
      {recipeField && (
        <div>
          <div className={classNames(Styles.titleWrapper)}>
            <span className={classNames(Styles.title)}>Recipe Details</span>
          </div>
          <div className={classNames(Styles.flexLayout, Styles.twoColumn)}>
            <div id="recipeName">
              <label className={classNames('input-label', Styles.recipeLable)}>Recipe Name :</label>
            </div>
            <div>
              <pre className={Styles.recipePre}>{recipeField.recipeName}</pre>
            </div>
            <div id="recipeType">
              <label className={classNames('input-label', Styles.recipeLable)}>Recipe Type :</label>
            </div>
            <div>
              <pre className={Styles.recipePre}>{recipeField.recipeType}</pre>
            </div>
          </div>
          <div className={classNames(Styles.flexLayout, Styles.twoColumn)}>
            <div id="gitUrl">
              <label className={classNames('input-label', Styles.recipeLable)}>Git Url :</label>
            </div>
            <div>
              <pre className={Styles.recipePre}>{recipeField.repodetails}</pre>
            </div>
            <div id="discSpace">
              {/* <label className={classNames('input-label', Styles.recipeLable)}>Disk-Space :</label> */}
            </div>
            <div>
              {/* <pre className={Styles.recipePre}>{recipeField.diskSpace} GB</pre> */}
            </div>
          </div>
          <div className={classNames(Styles.flexLayout, Styles.twoColumn)}>
            <div id="minCpu">
              <label className={classNames('input-label', Styles.recipeLable)}>Min CPU :</label>
            </div>
            <div>
              <pre className={Styles.recipePre}>{recipeField.minCpu}</pre>
            </div>
            <div id="maxCpu">
              <label className={classNames('input-label', Styles.recipeLable)}>Max CPU :</label>
            </div>
            <div>
              <pre className={Styles.recipePre}>{recipeField.maxCpu}</pre>
            </div>
          </div>
          <div className={classNames(Styles.flexLayout, Styles.twoColumn)}>
            <div id="minRam">
              <label className={classNames('input-label', Styles.recipeLable)}>Min RAM :</label>
            </div>
            <div>
              <pre className={Styles.recipePre}>{recipeField.minRam} GB</pre>
            </div>
            <div id="maxRam">
              <label className={classNames('input-label', Styles.recipeLable)}>Max RAM :</label>
            </div>
            <div>
              <pre className={Styles.recipePre}>{recipeField.maxRam} GB</pre>
            </div>
          </div>
          <div className={classNames(Styles.flexLayout, Styles.twoColumn)}>
            <div id="discSpace">
              <label className={classNames('input-label', Styles.recipeLable)}>Disk-Space :</label>
            </div>
            <div>
              <pre className={Styles.recipePre}>{recipeField.diskSpace} GB</pre>
            </div>
            <div id="software">
            <label className={classNames('input-label', Styles.recipeLable)}>Software:</label>
            </div>
            <div>{chips}</div>
            {/* <div>'NA'</div> */}

            {/* <div id="isPublic">
              <label className={classNames('input-label', Styles.recipeLable)}>Publicly Available:</label>
            </div>
            <div>
              <pre className={Styles.recipePre}>{recipeField.isPublic? 'Yes' : 'No'}</pre>
            </div> */}
          </div>
        </div>
      )}
    </React.Fragment>
  );
};

export default viewRecipe;
