import React from 'react';
import classNames from 'classnames';
import Styles from './code-space-group-card.scss';

const CodeSpaceGroupCard = ({ group, onShowCodeSpacesModal, onShowCodeSpaceGroupModal }) => {
  return (
    <div
      className={classNames(Styles.group)}
      id={`group-${group?.id}`}
      onDrop={(e) => {
          e.preventDefault();
      }}
      onDragOver={(e) => {
          e.preventDefault();
          e.dataTransfer.dropEffect = "move";
      }}
    >
      <div className={classNames(Styles.groupHeader)}>
          <h2 onClick={() => onShowCodeSpacesModal(true, group)}>{group?.name}</h2>
          <button className="btn btn-primary" onClick={() => onShowCodeSpaceGroupModal(true)}><i className="icon mbc-icon plus"></i> Add Codespace to Group</button>
      </div>
      {group?.warning &&
        <div className={classNames(Styles.groupWarning)}>
            <button className={classNames('btn btn-primary')} onClick={() => onShowCodeSpacesModal(true, group)}>
                <i className="icon mbc-icon alert circle"></i>
                Start failed for some code spaces, click to view
            </button>
        </div>
      }
      <div className={classNames(Styles.groupBody)}>
        {group?.codeSpaces?.map((codeSpace) => 
          <div key={codeSpace?.id} className={classNames(Styles.codeSpaceItem)}>
              <div>
                  <h3>{codeSpace?.projectDetails?.projectName}</h3>
                  {/* ToDo
                      1. Add elements to show different codespace states
                  */}
                  <a href="#" target="_blank" rel="noreferrer" tooltip-data="Deployed to Staging on 11/25/2024, 09:53:10">Deployed <i className="icon mbc-icon new-tab"></i></a>
              </div>
              {/* ToDo
                  1. Dynamically change to start or stop
                  2. Implement a progress bar to show the starting progress
              */}
              <button className={classNames('btn btn-primary')} tooltip-data="Start the Codespace"><span></span></button>
              {/* <button className={classNames('btn btn-primary', Styles.stop)} tooltip-data="Stop the Codespace"><span></span></button> */}
          </div>
          )}
          <button className={classNames('btn btn-primary', Styles.btnViewAll)} onClick={() => onShowCodeSpacesModal(true, group)}>View all</button>
      </div>
  </div>
  )
}

export default CodeSpaceGroupCard;