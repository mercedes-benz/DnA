import cn from 'classnames';
import React, { useContext } from 'react';
import { Envs } from 'globals/Envs';
import { history } from '../../../router/History';
import Styles from './Footer.scss';
import AppContext from 'components/context/ApplicationContext';

export interface IFooterProps {
  isHome: boolean;
  isNotebook: boolean;
}

// @ts-ignore
const classNames = cn.bind(Styles);
const Footer = (props: IFooterProps) => {
  const enableAppFeedback = Envs.ENABLE_APP_FEEDBACK;
  const appFeedbackExternalUrl = Envs.APP_FEEDBACK_EXTERNAL_URL;
  const currentUrl = window.location.href;

  const showLicence = () => {
    history.push('/license');
  };

  const context = useContext(AppContext);
  const { setShowTermsModal } = context;

  return (
    <React.Fragment>
      {props.isHome || props.isNotebook ? null : (
        <div
          className={classNames(
            Styles.flexLayout,
            'footerSection',
            currentUrl.indexOf('summary') !== -1 ? Styles.forSummary : null,
          )}
        >
          {currentUrl.indexOf('license') === -1 ? (
            <>
              <div className={classNames(Styles.footer)}>
                For copyright and license information see <a onClick={showLicence}>Notice</a>
              </div>
              <div className={classNames(Styles.footer, Styles.feedback)}>
                <button className={Styles.termsBtn} onClick={() => setShowTermsModal(true)}>
                  Terms of Use
                </button>&nbsp;&nbsp;
                {enableAppFeedback && (
                  <a target="_blank" rel="noreferrer" href={appFeedbackExternalUrl}>
                    Feedback
                  </a>
                )}
              </div>
            </>
          ) : (
            <div />
          )}
        </div>
      )}
    </React.Fragment>
  );
};

export default Footer;
