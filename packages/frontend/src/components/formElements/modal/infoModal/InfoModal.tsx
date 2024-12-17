import cn from 'classnames';
import * as React from 'react';
import Modal from '../Modal';
import Styles from './InfoModal.scss';

const classNames = cn.bind(Styles);

export interface IInfoModalProps {
  title: string;
  hiddenTitle?: boolean;
  modalWidth?: string;
  content: string | React.ReactNode;
  show: boolean;
  moreInfoLink?: string;
  onCancel?: () => void;
  customHeader?: React.ReactNode;
  modalStyle?: React.CSSProperties;
}

const InfoModal = (props: IInfoModalProps) => {
  const content: React.ReactNode = (
    <div className={classNames(Styles.contentWrapper, 'mbc-scroll')}>
      {props.content}
      {props.moreInfoLink ? (
        <div className={Styles.moreInfoLinkWrapper}>
          <a className="btn btn-text arrow" href={props.moreInfoLink} target="_blank" rel="noreferrer">
            More information
          </a>
        </div>
      ) : null}
    </div>
  );
  return (
    <Modal
      title={props.title}
      hiddenTitle={props.hiddenTitle}
      modalWidth={props.modalWidth}
      modalStyle={props.modalStyle}
      showAcceptButton={false}
      showCancelButton={false}
      buttonAlignment="right"
      show={props.show}
      content={content}
      scrollableContent={true}
      onCancel={props.onCancel}
      customHeader={props.customHeader}
    />
  );
};

export default InfoModal;
