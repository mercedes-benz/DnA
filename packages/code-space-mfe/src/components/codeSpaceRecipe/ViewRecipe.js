import cn from 'classnames';
import React, { useEffect, useState } from 'react';
// import { ICodeCollaborator } from 'globals/types';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import { ProgressIndicator } from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import Styles from './ViewRecipe.scss';

// export interface IViewRecipeProps {
//   recipeName: string;
// }

// export interface IRecipeField {
//   createdBy: ICodeCollaborator;
//   createdOn: string;
//   diskSpace: string;
//   maxCpu: string;
//   maxRam: string;
//   minCpu: string;
//   minRam: string;
//   isPublic: boolean,
//   oSName: string;
//   gitPath:string,
//   gitRepoLoc:string,
//   deployPath:string,
//   recipeName: string;
//   recipeType: string;
//   repodetails: string;
//   software: string[];
// }

const ViewRecipe = (props) => {
  const [recipeField, setRecipeField] = useState();
   const classNames = cn.bind(Styles);
  useEffect(() => {
    getCodeSpaceRecipe(props.recipeName);
  }, []);
  
  // useEffect(() => {
  //   if (!recipeField?.isPublic && recipeField?.users !== null) {
  //     const members =recipeField?.users.map(member => ({ ...member, shortId: member.id, userType: 'internal' }));
  //     setTeamMembers(members);
  //   }
  // }, [recipeField]);

  const getCodeSpaceRecipe = (recipeName) => {
    ProgressIndicator.show();
    CodeSpaceApiClient.getCodeSpaceRecipe(recipeName)
      .then((res) => {
        ProgressIndicator.hide();
        setRecipeField(res.data.data);
      })
      .catch((error) => {
        Notification.show(error.message, 'alert');
      });
  };
  
  const convertRam = (value ) => {
    const ramValue = parseInt(value)/1000;
    return ramValue.toString();
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
              <label className={classNames('input-label', Styles.recipeLable)}>Recipe Name:</label>
            </div>
            <div>
              <pre className={Styles.recipePre}>{recipeField.recipeName}</pre>
            </div>
            <div id="recipeType">
              <label className={classNames('input-label', Styles.recipeLable)}>Recipe Type:</label>
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
            <div id="isPublic">
              <label className={classNames('input-label', Styles.recipeLable)}>Publicly Available :</label>
            </div>
            <div>
              <pre className={Styles.recipePre}>{recipeField.isPublic ? 'Yes' : 'No'}</pre>
            </div>
          </div>
          <div className={classNames(Styles.flexLayout, Styles.twoColumn)}>
            <div id="gitRepoLoc">
              <label className={classNames('input-label', Styles.recipeLable)}>Docker File Path :</label>
            </div>
            <div>
              <pre className={Styles.recipePre}>{recipeField.gitRepoLoc}</pre>
            </div>
            <div id="deployPath">
              <label className={classNames('input-label', Styles.recipeLable)}>Helm File Path :</label>
            </div>
            <div>
              <pre className={Styles.recipePre}>{recipeField.deployPath}</pre>
            </div>
          </div>
          <div className={classNames(Styles.flexLayout, Styles.twoColumn)}>
            <div id="hardwareConfiguration">
              <label className={classNames('input-label', Styles.recipeLable)}>Hardware Configuration:</label>
            </div>
            <div>
              <pre className={Styles.recipePre}>DiskSpace- {recipeField.diskSpace}GB CPU- {recipeField.maxCpu} Ram-{convertRam(recipeField.maxRamq)}GB</pre>
            </div>
            <div id="gitPath">
              <label className={classNames('input-label', Styles.recipeLable)}> Git Repository:</label>
            </div>
            <div>
              <pre className={Styles.recipePre}>{recipeField.gitPath}</pre>
            </div>
          </div>
          <div className={classNames(Styles.flexLayout, Styles.twoColumn)}>
            <div id="software">
              <label className={classNames('input-label', Styles.recipeLable)}>Software:</label>
            </div>
            <div>{chips}</div>
          </div>
          
        </div>
      )}
    </React.Fragment>
  );
};

export default ViewRecipe;
