import classNames from 'classnames';
import React, { useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import Styles from './PromptCraftSubscriptionRow.scss';
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';
import { regionalDateAndTimeConversionSolution } from '../../../../services/utils';
import Spinner from '../spinner/Spinner';

export interface IPromptCraftSubscriptionRowProps {
  subscription: any,
  onSelectSubscription: (subscription: any) => void;
  onDeleteSubscription: (subscription: any) => void;
}

const PromptCraftSubscriptionRow = ({subscription, onSelectSubscription, onDeleteSubscription}: IPromptCraftSubscriptionRowProps) => {
  const history = useHistory();

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  const handleOpenSubscription = () => {
    history.push(`link here`);
  }

  return (
    <div className={Styles.projectRow} onClick={handleOpenSubscription}>
      <div className={Styles.col1}>
        <span>
          {subscription?.name || 'null'}
        </span>
        {subscription?.status?.state === 'IN_PROGRESS' &&
          <button className={Styles.stateBtn} tooltip-data={'Click for more information'} onClick={(e) => { e.stopPropagation(); onSelectSubscription(subscription) }}>
            <Spinner /> <span>&nbsp;</span>
          </button>
        }
        {subscription?.status?.state === 'COMPLETED' && 
          <button className={Styles.completedStatus}>
            <i className={'icon mbc-icon check circle'}></i> <span>Provisioned</span>
          </button>
        }
      </div>
      <div className={Styles.col2}>
        <a href={`https://app.fabric.microsoft.com/groups/${subscription.id}`} target='_blank' rel='noopener noreferrer'>
          Access Workspace
          <i className={classNames('icon mbc-icon new-tab')} />
        </a>
      </div>
      <div className={Styles.col3}>
        {subscription?.createdBy?.firstName} {subscription?.createdBy?.lastName}
      </div>
      <div className={Styles.col4}>
        {subscription?.createdOn && regionalDateAndTimeConversionSolution(subscription?.createdOn)}
      </div>
      <div className={Styles.col5}>
        {subscription?.dataClassification}
      </div>
      <div className={Styles.col6}>
        {/* {user?.id === subscription?.createdBy?.id && */}
          <div className={Styles.btnTblGrp}>
            <button
              className={classNames('btn btn-primary', Styles.projectLink)}
              onClick={(e) => { e.stopPropagation(); onDeleteSubscription(subscription); }}
            >
              <i className="icon delete"></i>
              <span>Delete</span>
            </button>
          </div>
        {/* } */}
      </div>
    </div>
  );
}

export default PromptCraftSubscriptionRow;