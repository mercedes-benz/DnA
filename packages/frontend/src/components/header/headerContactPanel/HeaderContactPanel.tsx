import cn from 'classnames';
import React, { useEffect, useState, useContext } from 'react';
import { Envs } from 'globals/Envs';
import InfoModal from 'components/formElements/modal/infoModal/InfoModal';
import About from 'components/mbc/About/About';
// import { getTranslatedLabel } from '../../../globals/i18n/TranslationsProvider';
import { history } from '../../../router/History';
import Styles from './HeaderContactPanel.scss';
import AppContext from 'components/context/ApplicationContext';

const classNames = cn.bind(Styles);

interface IHeaderContactPanelProps {
  show?: boolean;
  onClose?: () => void;
}

let isTouch = false;

export default function HeaderContactPanel(props: IHeaderContactPanelProps) {
  const contactModalContent = <div dangerouslySetInnerHTML={{ __html: Envs.DNA_CONTACTUS_HTML }}></div>;
  const [showContactModal, setShowContactModal] = useState<boolean>(false);
  const [showAboutModal, setShowAboutModal] = useState<boolean>(false);
  const enableAppFeedback = Envs.ENABLE_APP_FEEDBACK;
  const appFeedbackExternalUrl = Envs.APP_FEEDBACK_EXTERNAL_URL;

  useEffect(() => {
    eventClenUp();

    if (props.show) {
      document.addEventListener('touchend', handleContactPanelOutside, true);
      document.addEventListener('click', handleContactPanelOutside, true);
    }
  }, [props.show]);

  useEffect(() => {
    return () => {
      eventClenUp();
    };
  }, []);

  const eventClenUp = () => {
    document.removeEventListener('touchend', handleContactPanelOutside, true);
    document.removeEventListener('click', handleContactPanelOutside, true);
  };

  const handleContactPanelOutside = (event: MouseEvent | TouchEvent) => {
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

  const context = useContext(AppContext);
  const { setShowTermsModal } = context;

  return (
    <div id="helpMenuContentWrapper" className={classNames(props.show ? Styles.userContexMenu : 'hide')}>
      <div className={Styles.upArrow} />
      <ul className={classNames(Styles.innerContainer)}>
        <li onClick={() => setShowContactModal(true)}>
          {/* {getTranslatedLabel('Contact Us')} */}
          Contact Us
        </li>
        <li onClick={() => setShowAboutModal(true)}>
          {/* {getTranslatedLabel('Licences')} */}
          About
        </li>
        <li
          onClick={() => {
            history.push('/license');
            props.onClose();
          }}
        >
          {/* {getTranslatedLabel('Licences')} */}
          Licences
        </li>
        {enableAppFeedback && (
          <li>
            <a target="_blank" rel="noreferrer" href={appFeedbackExternalUrl}>
              Feedback
            </a>
          </li>
        )}
        <li
          onClick={() => {
            history.push('/usage-statistics');
            props.onClose();
          }}
        >
          Usage Statistics
        </li>
        <li
          onClick={() => {
            setShowTermsModal(true);
          }}
        >
          Terms of Use
        </li>
      </ul>
      <InfoModal
        title={showContactModal ? 'Contact Us' : 'About'}
        hiddenTitle={showAboutModal}
        modalWidth={'35vw'}
        show={showAboutModal || showContactModal}
        content={showContactModal ? contactModalContent : <About />}
        onCancel={() => {
          setShowContactModal(false);
          setShowAboutModal(false);
          props.onClose();
        }}
      />
    </div>
  );
}
