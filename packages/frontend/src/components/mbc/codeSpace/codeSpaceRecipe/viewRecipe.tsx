import cn from 'classnames';
import React, { useEffect, useState } from 'react';
import { ICodeCollaborator } from 'globals/types';
import { CodeSpaceApiClient } from '../../../../services/CodeSpaceApiClient';
import { ProgressIndicator } from '../../../../assets/modules/uilab/bundle/js/uilab.bundle';
import TeamMemberListItem from 'components/mbc/addTeamMember/teamMemberListItem/TeamMemberListItem';
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
  isPublic: boolean,
  oSName: string;
  gitPath:string,
  gitRepoLoc:string,
  deployPath:string,
  recipeName: string;
  recipeType: string;
  repodetails: string;
  software: string[];
}

const viewRecipe = (props: IViewRecipeProps) => {
  const [recipeField, setRecipeField] = useState<IRecipeField>();
  const [teamMembers, setTeamMembers] = useState([]);
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
  const teamMembersList = teamMembers?.map((member, index) => {
    return (
      <TeamMemberListItem
        key={index}
        itemIndex={index}
        teamMember={member}
        hidePosition={true}
        hideContextMenu={true}
        showInfoStacked={true}
        showMoveUp={false}
        showMoveDown={false}
        onMoveUp={() => {}}
        onMoveDown={() => {}}
        onEdit={()=>{}}
        onDelete={()=>{}}
      />
    );
  });
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
              <pre className={Styles.recipePre}>DiskSpace- {recipeField.diskSpace}GB CPU- {recipeField.maxCpu} Ram-{recipeField.maxRam}GB</pre>
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

export default viewRecipe;
