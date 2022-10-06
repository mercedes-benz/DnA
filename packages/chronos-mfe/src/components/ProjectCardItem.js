import React from 'react';
import Styles from './ProjectCardItem.styles.scss';
import { withRouter } from 'react-router-dom';
import { regionalDateAndTimeConversionSolution } from '../Utility/utils';

const ProjectCardItem = ({project, history}) => {

  const displayPermission = (item) => {
    return Object.entries(item)
      ?.map(([key, value]) => {
        if (value === true) {
          return key;
        }
      })
      ?.filter((x) => x) // remove falsy values
      ?.map((perm) => perm?.charAt(0)?.toUpperCase() + perm?.slice(1)) // update first character to Uppercase
      ?.join(' / ');
  };

  return (
    <>
      <div className={Styles.projectCard}>
        <div
          className={Styles.cardHead}
          onClick={() => {
            history.push(`/project/${project?.id}`);
          }}
        >
          <div className={Styles.cardHeadInfo}>
            <div>
              <div className={Styles.cardHeadTitle}>{project?.name}</div>
              <div className="btn btn-text forward arrow"></div>
            </div>
          </div>
        </div>
        <hr />
        <div className={Styles.cardBodySection}>
          <div>
            <div>
              <div>Permission</div>
              <div>{project?.permission === null ? 'N/A' : displayPermission(project?.permission)}</div>
            </div>
            <div>
              <div>Created on</div>
              <div>{project?.createdOn !== undefined && regionalDateAndTimeConversionSolution(project?.createdOn)}</div>
            </div>
            <div>
              <div>Created by</div>
              <div>{`${project?.createdBy?.firstName} ${project?.createdBy?.lastName}`}</div>
            </div>
          </div>
        </div>
        <div className={Styles.cardFooter}>
          <div>&nbsp;</div>
          <div className={Styles.btnGrp}>
            <button className="btn btn-primary" disabled={true}>
              <i className="icon mbc-icon edit fill"></i>
            </button>
            <button className="btn btn-primary" disabled={true}>
              <i className="icon mbc-icon share"></i>
            </button>
            <button className="btn btn-primary" disabled={true}>
              <i className="icon mbc-icon data-sharing"></i>
            </button>
          </div>
        </div>
      </div>
    </>
  );
};
export default withRouter(ProjectCardItem);
