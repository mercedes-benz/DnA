import React from 'react';
import classNames from 'classnames';
import Styles from './code-space-group-card.scss';

const CodeSpaceGroupCard = ({ group, onShowCodeSpacesModal, onShowCodeSpaceGroupModal, onCodeSpaceGroupDeleteModal }) => {
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
        {group?.workspaces?.map((workspace) => 
          <div key={workspace?.wsId} className={classNames(Styles.codeSpaceItem)}>
              <div>
                  <h3>{workspace?.name}</h3>
                  {/* ToDo
                      1. Add elements to show different codespace states
                  */}
                  {/* <a href="#" target="_blank" rel="noreferrer" tooltip-data="Deployed to Staging on 11/25/2024, 09:53:10">Deployed <i className="icon mbc-icon new-tab"></i></a> */}
              </div>
              {/* {!createInProgress && !creationFailed && !serverFailed && (
                <span
                  onClick={() => onStartStopCodeSpace(codeSpace)}
                  tooltip-data={(serverStarted ? 'Stop' : 'Start') + ' the Codespace'}
                  className={classNames(
                    Styles.statusIndicator,
                    Styles.wsStartStop,
                    serverStarted ? Styles.wsStarted : '',
                  )}
                >
                  {serverStarted ? 'Stop' : 'Start'}
                  {!serverStarted && serverProgress > 0 ? `ing... ${serverProgress}%` : ''}
                </span>
              )}
              {serverFailed && (
                <span
                  title={'Server Start Failed: Please contact Codespace Admin'}
                  className={classNames(Styles.statusIndicator, Styles.wsStartStop, Styles.wsStarted)}
                  onClick={() => onStartStopCodeSpace(codeSpace)}
                >
                  Start Failed
                </span>
              )} */}
          </div>
          )}
          <div className={Styles.btnContainer}>
            <button className={classNames('btn btn-primary')} onClick={() => onShowCodeSpaceGroupModal(true)}>
              <i className="icon mbc-icon plus"></i> Add Code Space
            </button>
            <button className={classNames('btn btn-primary')} onClick={() => onShowCodeSpacesModal(true, group)}>
              <i className="icon mbc-icon visibility-show"></i> View all
            </button>
            <button className={classNames('btn btn-primary')} onClick={() => onCodeSpaceGroupDeleteModal(true, group)}>
              <i className="icon delete"></i> Delete
            </button>
          </div>
      </div>
  </div>
  )
}

export default CodeSpaceGroupCard;