import classNames from 'classnames';
import React, { useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import Styles from './PromptCraftSubscriptionRow.scss';
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';
import { regionalDateAndTimeConversionSolution } from '../../../../services/utils';
import Spinner from '../spinner/Spinner';

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

  return (
    <div className={Styles.projectRow} onClick={handleOpenSubscription}>
      <div className={Styles.col1}>
        <span>
          {subscription?.projectName || 'null'}
        </span>
        {subscription?.state !== 'COMPLETED' &&
          <button className={Styles.stateBtn} tooltip-data={'Subscription Requested'}>
            <Spinner /> <span>&nbsp;</span>
          </button>
        }
        {subscription?.state === 'COMPLETED' && 
          <button className={Styles.completedStatus} tooltip-data={'Active'}>
            <i className={'icon mbc-icon check circle'}></i> <span>&nbsp;</span>
          </button>
        }
      </div>
      <div className={Styles.col2}>
        <a href={subscription?.subscriptionLink} target='_blank' rel='noopener noreferrer'>
          Access Subscription
          <i className={classNames('icon mbc-icon new-tab')} />
        </a>
      </div>
      <div className={Styles.col3}>
        {subscription?.orgname || 'NA'}
      </div>
      <div className={Styles.col4}>
        {subscription?.createdOn && regionalDateAndTimeConversionSolution(subscription?.createdOn)}
      </div>
      <div className={Styles.col5}>
        {subscription?.projectMembers?.map((member:any) => <p key={member?.id}>{member?.firstName} {member?.lastName} - {member?.id}</p>)}
      </div>
      <div className={Styles.col6}>
        {/* {user?.id === subscription?.createdBy?.id && */}
          <div className={Styles.btnTblGrp}>
            <button
              className={classNames('btn btn-primary', Styles.projectLink)}
              onClick={(e) => { e.stopPropagation(); onShowKeys(subscription); }}
            >
              <i className="icon mbc-icon visibility-show"></i>
              <span>View Keys</span>
            </button>
          </div>
        {/* } */}
      </div>
    </div>
  );
}

export default PromptCraftSubscriptionRow;