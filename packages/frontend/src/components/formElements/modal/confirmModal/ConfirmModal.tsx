import * as React from 'react';
import { IconAttention } from '../../../../components/icons/IconAttention';
import Styles from './ConfirmModal.scss';

export interface IConfirmModalProps {
  title: string;
  content: string | React.ReactNode;
  show: boolean;
  showAcceptButton: boolean;
  showCancelButton: boolean;
  acceptButtonTitle?: string;
  cancelButtonTitle?: string;
  scrollableContent?: boolean;
  removalConfirmation?: boolean;
  showIcon?: boolean;
  onAccept?: () => void;
  onCancel?: () => void;
}

const ConfirmModal = (props: IConfirmModalProps) => (
  <div className={props.show ? 'mbc-cfm-modal-wrapper' : Styles.hide}>
    <div className={'mbc-cfm-modal'}>
      <header>
        <div>{props.showIcon === false ? '' : <IconAttention />}</div>
        {props.showCancelButton && (
          <button className="modal-close-button" onClick={props.onCancel}>
            <i className="icon mbc-icon close thin" />
          </button>
        )}
      </header>

      <div className={props.scrollableContent ? 'modal-content scrollable mbc-scroll' : 'modal-content'}>
        {props.content}
      </div>
      <footer className={props.showAcceptButton || props.showCancelButton ? '' : Styles.hide}>
        <div className="btn-set">
          <button
            className={props.showCancelButton ? 'btn btn-secondary' : `${Styles.hide}`}
            type="button"
            onClick={props.onCancel}
          >
            {props.cancelButtonTitle}
          </button>
          {props.removalConfirmation ? (
            <button
              className={props.showAcceptButton ? 'btn btn-tertiary' : `${Styles.hide}`}
              type="submit"
              onClick={props.onAccept}
            >
              {props.acceptButtonTitle}
            </button>
          ) : (
            <button
              className={props.showAcceptButton ? 'btn btn-primary' : `${Styles.hide}`}
              type="submit"
              onClick={props.onAccept}
            >
              {props.acceptButtonTitle}
            </button>
          )}
        </div>
      </footer>
    </div>
  </div>
);
export default ConfirmModal;
