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
  oSName: string;
  recipeName: string;
  recipeType: string;
  repodetails: string;
  software: [
    {
      name: string;
      version: string;
    },
  ];
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
        setRecipeField(res);
      })
      .catch((error) => {
        Notification.show(error.message, 'alert');
      });
  };

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
              <label className={classNames('input-label', Styles.recipeLable)}>Disk-Space :</label>
            </div>
            <div>
              <pre className={Styles.recipePre}>{recipeField.diskSpace} GB</pre>
            </div>
          </div>
          <div className={classNames(Styles.flexLayout, Styles.twoColumn)}>
            <div id="minCpu">
              <label className={classNames('input-label', Styles.recipeLable)}>Min CPU :</label>
            </div>
            <div>
              <pre className={Styles.recipePre}>{recipeField.minCpu} GB</pre>
            </div>
            <div id="maxCpu">
              <label className={classNames('input-label', Styles.recipeLable)}>Max CPU :</label>
            </div>
            <div>
              <pre className={Styles.recipePre}>{recipeField.maxCpu} GB</pre>
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
          <div className={classNames(Styles.titleWrapper)}>
            <span className={classNames(Styles.title)}>Software Details</span>
          </div>
          <div className={Styles.softwareList}>
            {recipeField.software.length > 0 ? (
              <div>
                <table className="ul-table users">
                  <thead>
                    <tr className="header-row">
                      <th>
                        <label>software Name</label>
                      </th>

                      <th>
                        <label>Version</label>
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {recipeField.software &&
                      recipeField.software.map((software, index) => {
                        return (
                          <tr className="data-row" key={index}>
                            <td>{software.name}</td>
                            <td>{software.version}</td>
                          </tr>
                        );
                      })}
                  </tbody>
                </table>
              </div>
            ) : (
              ''
            )}
          </div>
        </div>
      )}
    </React.Fragment>
  );
};

export default viewRecipe;
