import classNames from 'classnames';
import React, { useEffect } from 'react';
import Styles from './PromptCraftSubscriptionCard.scss';
import { regionalDateAndTimeConversionSolution } from '../../../../services/utils';
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';
import Popper from 'popper.js';
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import { PromptCraftApiClient } from '../../../../services/PromptCraftApiClient';

export interface IPromptCraftSubscriptionFormProps {
  subscription: any,
  // onSelectSubscription: (subscription: any) => void;
  onShowKeys: (subscription: any) => void;
}

const PromptCraftSubscriptionCard = ({ subscription, onShowKeys }: IPromptCraftSubscriptionFormProps) => {

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  const handleOpenSubscription = () => {
    window.open(subscription?.subscriptionLink, '_blank');
  }

  let popperObj: any, tooltipElem: any = null;

  const onCollabsIconMouseOver = (e: any) => {
    const targetElem = e.target;
    tooltipElem = targetElem.nextElementSibling;
    if (tooltipElem) {
      tooltipElem.classList.add('tooltip', 'show');
      tooltipElem.classList.remove('hide');
      popperObj = new Popper(targetElem, tooltipElem, {
        placement: 'top',
      });
    }
  };

  const onCollabsIconMouseOut = () => {
    if (tooltipElem) {
      tooltipElem.classList.add('hide');
      tooltipElem.classList.remove('tooltip', 'show');
    }
    popperObj?.destroy();
  };

  const handleRefreshSubscription = () => {
    ProgressIndicator.show();
      PromptCraftApiClient
        .refreshSubscriptionKeys(subscription?.projectName)
        .then(() => {
          Notification.show('Prompt Craft subscription refreshed');
          // getKeys(projectName);
          ProgressIndicator.hide();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          Notification.show(
            e?.response?.data?.errors[0]?.message || e?.response?.errors[0]?.message || 'Refreshing prompt craft subscription keys failed!',
            'alert',
          );
        });
  }

  return (
    <div className={classNames(Styles.projectCard)}>
      <div className={Styles.cardHead}>
        <div className={classNames(Styles.cardHeadInfo)}>
          <div
            className={classNames('btn btn-text forward arrow', Styles.cardHeadTitle)}
            onClick={subscription?.subscriptionLink ? handleOpenSubscription : undefined}
          >
            {subscription?.projectName || 'null'}
          </div>
        </div>
      </div>
      <hr />
      <div className={Styles.cardBodySection}>
        <div>
          {/* {subscription?.subscriptionLink !== null &&
            <div>
              <div>Subscription Link</div>
              <div>
                <a href={subscription?.subscriptionLink} target='_blank' rel='noopener noreferrer'>
                  Access Subscription
                  <i className={classNames('icon mbc-icon new-tab')}></i>
                </a>
              </div>
            </div>
          } */}
          <div>
            <div>Created on</div>
            <div>{subscription?.createdOn && regionalDateAndTimeConversionSolution(subscription?.createdOn)}</div>
          </div>
          <div>
            <div>Organization</div>
            <div>{subscription?.orgName || 'N/A'}</div>
          </div>
          <div>
            <div>Project Members</div>
            {subscription?.projectMembers?.length > 0 ? (
              <div>
                <i className="icon mbc-icon profile" />
                <span className={Styles.cardCollabIcon} onMouseOver={onCollabsIconMouseOver} onMouseOut={onCollabsIconMouseOut}>
                  {subscription?.projectMembers?.length}
                </span>
                <div className={classNames(Styles.membersList, 'hide')}>
                  <ul>
                    {subscription?.projectMembers?.map((member: any, index: any) => {
                      // Check if lastName is more than 12 characters
                      let lastName = member.lastName;
                      if (lastName?.length > 12) {
                        lastName = lastName.substring(0, 12) + " ...";
                      }
                      return (
                        <li key={'collab' + index}>
                          <span>
                            {`${member.firstName} ${lastName} (${member.id})`}
                          </span>
                        </li>
                      );
                    })}
                  </ul>
                </div>
              </div>
            ) : <div>None</div>}
          </div>
        </div>
      </div>
      <div className={Styles.cardFooter}>
        <>
          <div className={Styles.statusContainer}>
            <div className={Styles.statusItem}>
              {(subscription?.status !== 'COMPLETED' && subscription?.status !== 'FAILED') &&
                <p>
                  <i className={'icon mbc-icon info'}></i> <span>Subscription Requested</span>
                </p>
              }
              {subscription?.status === 'FAILED' &&
                <p className={Styles.failedStatus}>
                  <i className={'icon mbc-icon alert circle'}></i> <span>Failed</span>
                </p>
              }
              {subscription?.status === 'COMPLETED' &&
                <p className={Styles.completedStatus}>
                  <i className={'icon mbc-icon check circle'}></i> <span>Active</span>
                </p>
              }
              {/* {isRequestedWorkspace && subscription?.status?.state === 'IN_PROGRESS' && <p className={Styles.requestStatus}>Workspace Accesss Requested</p>} */}
            </div>
          </div>
          {/* {user?.id === subscription?.createdBy?.id && */}
          <div className={Styles.btnGrp}>
            {/* <button
                className={'btn btn-primary'}
                type="button"
                onClick={() => onDeleteSubscription(subscription)}
              >
                <i className="icon delete"></i>
                <span>Delete</span>
              </button> */}
            {subscription?.status !== 'COMPLETED' ?
              <button
                className={'btn btn-primary'}
                type="button"
                onClick={handleRefreshSubscription}
                tooltip-data={'Refresh Subscription if it is taking too long or is Failed.'}
              >
                <i className="icon mbc-icon refresh"></i>&nbsp;
                <span>Refresh</span>
              </button>
              :
              <button
                className={'btn btn-primary'}
                type="button"
                onClick={() => onShowKeys(subscription)}
              >
                <i className="icon mbc-icon visibility-show"></i>&nbsp;
                <span>View Key</span>
              </button>
            }
          </div>
          {/* } */}
        </>
      </div>
    </div>
  );
};
export default PromptCraftSubscriptionCard;