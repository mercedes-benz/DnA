import * as React from 'react';
import { IconAttention } from '../../../../components/icons/IconAttention';
import Styles from './ConfirmModal.scss';

export interface IConfirmModalProps {
  /** title of the modal */
  title: string;
  /** content to be displayed within the modal */
  content: string | React.ReactNode;
  /**  show/hide the modal */
  show: boolean;
  /**  show Accept button */
  showAcceptButton: boolean;
  /**  show Cancel button */
  showCancelButton: boolean;
  /** Label of the Accept button */
  acceptButtonTitle?: string;
  /** Label of the Cancel button */
  cancelButtonTitle?: string;
  /** should the content be scrollable */
  scrollableContent?: boolean;
  /** show removal confirmation modal */
  removalConfirmation?: boolean;
  /** show Icons */
  showIcon?: boolean;
  /** action to be done on clicking Accept button */
  onAccept?: () => void;
  /** action to be done on clicking Cancel button */
  onCancel?: () => void;
  /** accept button disabled */
  acceptButtonDisabled?: boolean;
}
/**
 * User Confirmation Modal
 * @visibleName Confirm Modal
 */
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
              disabled={props?.acceptButtonDisabled}
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
