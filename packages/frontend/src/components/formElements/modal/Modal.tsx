import classNames from 'classnames';
import * as React from 'react';
import Styles from './Modal.scss';

export interface IModalProps {
  title: string;
  hiddenTitle?: boolean;
  titleIconImage?: React.ReactNode;
  content: string | React.ReactNode;
  show: boolean;
  buttonAlignment: string;
  showAcceptButton: boolean;
  showCancelButton: boolean;
  acceptButtonTitle?: string;
  cancelButtonTitle?: string;
  scrollableContent?: boolean;
  onAccept?: () => void;
  onCancel?: () => void;
  modalWidth?: string;
  modalStyle?: React.CSSProperties;
}

const Modal = (props: IModalProps) => {
  React.useEffect(() => {
    if (props.show) {
      document.querySelector('body').style.overflow = 'hidden';
      // Popup scroll needed to reset inital value 0 //
      const scrollableContent = document.querySelector('.mbc-modal-wrapper .scrollable');
      if (scrollableContent) {
        scrollableContent.scrollTop = 0;
      }
    } else {
      document.querySelector('body').style.overflow = 'auto';
    }
    return function cleanup() {
      document.querySelector('body').style.overflow = 'auto';
    };
  }, [props.show]);

  return (
    <div className={props.show ? 'mbc-modal-wrapper' : Styles.hide}>
      <div className={'mbc-modal'} style={{ minWidth: props.modalWidth ? props.modalWidth : '', ...props.modalStyle }}>
        <header>
          <h4 className={classNames(props.hiddenTitle ? 'hidden' : '')}>
            {props.titleIconImage ? props.titleIconImage : null}
            {props.title}
          </h4>
          <button className="modal-close-button" onClick={props.onCancel}>
            <i className="icon mbc-icon close thin" />
          </button>
        </header>
        <div className={props.scrollableContent ? 'modal-content scrollable mbc-scroll' : 'modal-content'}>
          {' '}
          {props.content}
        </div>
        <footer
          className={
            props.showAcceptButton || props.showCancelButton
              ? props.buttonAlignment === 'center'
                ? 'footerCenter'
                : 'footerRight'
              : Styles.hide
          }
        >
          <div className="btn-set">
            <button
              className={props.showCancelButton ? 'btn btn-secondary' : `${Styles.hide}`}
              type="button"
              onClick={props.onCancel}
            >
              {props.cancelButtonTitle}
            </button>
            <button
              className={props.showAcceptButton ? 'btn btn-primary' : `${Styles.hide}`}
              type="submit"
              onClick={props.onAccept}
            >
              {props.acceptButtonTitle}
            </button>
          </div>
        </footer>
      </div>
    </div>
  );
};

export default Modal;
