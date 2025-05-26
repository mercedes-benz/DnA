import classNames from 'classnames';
import React, { useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import Styles from './PromptCraftSubscriptionRow.scss';
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';
import { regionalDateAndTimeConversionSolution } from '../../../../services/utils';
import Spinner from '../spinner/Spinner';
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import { PromptCraftApiClient } from '../../../../services/PromptCraftApiClient';

export interface IPromptCraftSubscriptionRowProps {
  subscription: any,
  // onSelectSubscription: (subscription: any) => void;
  onShowKeys: (subscription: any) => void;
}

const PromptCraftSubscriptionRow = ({subscription, onShowKeys}: IPromptCraftSubscriptionRowProps) => {
  const history = useHistory();

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  const handleOpenSubscription = () => {
    history.push(subscription?.subscriptionLink);
  }

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
            e?.response?.data?.errors[0]?.message || e?.response?.errors[0]?.message || 'Refreshing prompt craft subscription failed!',
            'alert',
          );
        });
  }

  return (
    <div className={Styles.projectRow} onClick={subscription?.subscriptionLink ? handleOpenSubscription : undefined}>
      <div className={Styles.col1}>
        <span>
          {subscription?.projectName || 'null'}
        </span>
        {(subscription?.status !== 'COMPLETED' && subscription?.status !== 'FAILED') &&
          <button className={Styles.stateBtn} tooltip-data={'Subscription Requested'}>
            <Spinner /> <span>&nbsp;</span>
          </button>
        }
        {subscription?.status === 'FAILED' && 
          <button className={classNames(Styles.completedStatus, Styles.failed)} tooltip-data={'Failed'}>
            <i className={classNames('icon mbc-icon alert circle') }></i> <span>&nbsp;</span>
          </button>
        }
        {subscription?.status === 'COMPLETED' && 
          <button className={Styles.completedStatus} tooltip-data={'Active'}>
            <i className={'icon mbc-icon check circle'}></i> <span>&nbsp;</span>
          </button>
        }
      </div>
      {/* <div className={Styles.col2}>
      {subscription?.subscriptionLink !== null ?
        <a href={subscription?.subscriptionLink} target='_blank' rel='noopener noreferrer'>
          Access Subscription
          <i className={classNames('icon mbc-icon new-tab')} />
        </a> : 'NA'
      }
      </div> */}
      <div className={Styles.col3}>
        {subscription?.orgName || 'NA'}
      </div>
      <div className={Styles.col4}>
        {subscription?.createdOn && regionalDateAndTimeConversionSolution(subscription?.createdOn)}
      </div>
      <div className={Styles.col5}>
        {subscription?.projectMembers?.map((member:any) => <p key={member?.id}>{member?.firstName} {member?.lastName} - {member?.id}</p>)}
      </div>
      <div className={Styles.col6}>
          <div className={Styles.btnTblGrp}>
            {subscription?.status !== 'COMPLETED' ?
              <button
                className={classNames('btn btn-primary', Styles.projectLink)}
                onClick={(e) => { e.stopPropagation(); handleRefreshSubscription(); }}
              >
                <i className="icon mbc-icon refresh"></i>
                <span>Refresh Subscription</span>
              </button>
              :
              <button
                className={classNames('btn btn-primary', Styles.projectLink)}
                onClick={(e) => { e.stopPropagation(); onShowKeys(subscription); }}
              >
                <i className="icon mbc-icon visibility-show"></i>
                <span>View Key</span>
              </button>
            }
          </div>
      </div>
    </div>
  );
}

export default PromptCraftSubscriptionRow;