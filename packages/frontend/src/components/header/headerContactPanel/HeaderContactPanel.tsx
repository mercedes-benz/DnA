import cn from 'classnames';
import React, { useEffect, useState } from 'react';
import { InfoModal } from '../../../components/formElements/modal/infoModal/InfoModal';
import About from '../../../components/mbc/About/About';
// import { getTranslatedLabel } from '../../../globals/i18n/TranslationsProvider';
import { history } from '../../../router/History';
import Styles from './HeaderContactPanel.scss';

const classNames = cn.bind(Styles);

export interface IHeaderContactPanelProps {
  show?: boolean;
  onClose?: () => void;
  toggleContactPanelCallBack: () => void;
}

let isTouch = false;

const HeaderContactPanel = (props: IHeaderContactPanelProps) => {
  const [showAboutModal, setShowAboutModal] = useState<boolean>(false);

  useEffect(() => {
    eventClenUp();

    if(props.show) {
      document.addEventListener('touchend', handleUserPanelOutside, true);
      document.addEventListener('click', handleUserPanelOutside, true);
    }
  }, [props.show]);

  useEffect(() => {
    return () => {
      eventClenUp();
    };
  }, []);

  const eventClenUp = () => {
    document.removeEventListener('touchend', handleUserPanelOutside, true);
    document.removeEventListener('click', handleUserPanelOutside, true);
  }

  const navigateToMyContactUs = () => {
    props.toggleContactPanelCallBack();
  };

  const handleUserPanelOutside = (event: MouseEvent | TouchEvent) => {
    const helpMenuWrapper = document?.querySelector('#helpMenuContentWrapper');

    if (event.type === 'touchend') {
      isTouch = true;
    }
    // Click event has been simulated by touchscreen browser.
    if (event.type === 'click' && isTouch === true) {
      return;
    }
    const target = event.target as Element;
    if (!helpMenuWrapper?.contains(target) && !target.classList.contains('help')) {
      eventClenUp();
      props.onClose();
    }
  };

  return (
    <React.Fragment>
    <div id="helpMenuContentWrapper" className={classNames(props.show ? Styles.userContexMenu : 'hide')}>
      <div className={Styles.upArrow} />
      <ul className={classNames(Styles.innerContainer)}>
        <li onClick={navigateToMyContactUs}>
          {/* {getTranslatedLabel('Contact Us')} */}
          Contact Us
        </li>
        <li onClick={() => setShowAboutModal(true)}>
          {/* {getTranslatedLabel('Licences')} */}
          About
        </li>
        <li onClick={() => { history.push('/license'); props.onClose(); }}>
          {/* {getTranslatedLabel('Licences')} */}
          Licences
        </li>
      </ul>
    </div>
    <InfoModal
      title={'About'}
      hiddenTitle={true}
      modalWidth={'35vw'}
      show={showAboutModal}
      content={<About />}
      onCancel={() => {
        setShowAboutModal(false);
        props.onClose();
      }}
    />
  </React.Fragment>
  );
};

export default HeaderContactPanel;
