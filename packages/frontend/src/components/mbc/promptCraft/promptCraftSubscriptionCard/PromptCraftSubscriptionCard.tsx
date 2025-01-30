import classNames from 'classnames';
import React, { useEffect } from 'react';
import Styles from './PromptCraftSubscriptionCard.scss';
import { useHistory } from 'react-router-dom';
import { regionalDateAndTimeConversionSolution } from '../../../../services/utils';
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';
import Spinner from '../spinner/Spinner';

export interface IPromptCraftSubscriptionFormProps {
  subscription: any,
  onSelectSubscription: (subscription: any) => void;
  onDeleteSubscription: (subscription: any) => void;
}

const PromptCraftSubscriptionForm = ({subscription, onSelectSubscription, onDeleteSubscription}: IPromptCraftSubscriptionFormProps) => {
  const history = useHistory();
  
  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  const handleOpenSubscription = () => {
    history.push(`link here`);
  }

  return (
    <div className={classNames(Styles.projectCard)}>
      <div className={Styles.cardHead}>
        <div className={classNames(Styles.cardHeadInfo)}>
          <div
            className={classNames('btn btn-text forward arrow', Styles.cardHeadTitle)}
            onClick={handleOpenSubscription}
          >
            {subscription?.name || 'null'}
          </div>
        </div>
      </div>
      <hr />
      <div className={Styles.cardBodySection}>
        <div>
          <div>
            <div>Subscription Link</div>
            <div>
              <a href={`https://app.fabric.microsoft.com/groups/${subscription.id}`} target='_blank' rel='noopener noreferrer'>
                Access Subscription
                <i className={classNames('icon mbc-icon new-tab')}></i>
              </a>
            </div>
          </div>
          <div>
            <div>Created on</div>
            <div>{subscription?.createdOn && regionalDateAndTimeConversionSolution(subscription?.createdOn)}</div>
          </div>
          <div>
            <div>Created by</div>
            <div>{subscription?.createdBy?.firstName} {subscription?.createdBy?.lastName}</div>
          </div>
          {/* <div>
            <div>Role</div>
            <div>{subscription?.role || 'N/A'}</div>
          </div> */}
          <div>
            <div>Classification</div>
            <div>{subscription?.dataClassification || 'N/A'}</div>
          </div>
        </div>
      </div>
      <div className={Styles.cardFooter}>
        <>
          <div className={Styles.statusContainer}>
            <div className={Styles.statusItem}>
              <button tooltip-data={'Click for more information'} onClick={() => onSelectSubscription(subscription)}>
                {subscription?.status?.state === 'IN_PROGRESS' && <><Spinner /> <span>In progress</span></>}
              </button>
              {subscription?.status?.state === 'COMPLETED' && 
                <button className={Styles.completedStatus} onClick={() => onSelectSubscription(subscription)}>
                  <i className={'icon mbc-icon check circle'}></i> <span>Provisioned</span>
                </button>
              }
              {/* {isRequestedWorkspace && subscription?.status?.state === 'IN_PROGRESS' && <p className={Styles.requestStatus}>Workspace Accesss Requested</p>} */}
            </div>
          </div>
          {/* {user?.id === subscription?.createdBy?.id && */}
            <div className={Styles.btnGrp}>
              <button
                className={'btn btn-primary'}
                type="button"
                onClick={() => onDeleteSubscription(subscription)}
              >
                <i className="icon delete"></i>
                <span>Delete</span>
              </button>
            </div>
          {/* } */}
        </>
      </div>
    </div>
  );
};
export default PromptCraftSubscriptionForm;