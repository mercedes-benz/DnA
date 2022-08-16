import cn from 'classnames';
import * as React from 'react';
import { Envs } from '../../../globals/Envs';
import { history } from '../../../router/History';
import Styles from './Footer.scss';

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
              {enableAppFeedback && (
                <div className={classNames(Styles.footer, Styles.feedback)}>
                  <a target="_blank" rel="noreferrer" href={appFeedbackExternalUrl}>
                    Feedback
                  </a>
                </div>
              )}
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
