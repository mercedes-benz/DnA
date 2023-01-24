import React from 'react';
import NotFoundPage from '../router/NotFoundPage';
import { Link } from 'react-router-dom';
import { getTranslatedLabel } from 'globals/i18n/TranslationsProvider';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';

interface IErrorState {
  hasError: boolean;
  error: any;
  errorInfo: any;
}

export default class ErrorBoundary extends React.Component<{}, IErrorState> {
  constructor(props: any) {
    super(props);
    this.state = { hasError: false, error: false, errorInfo: null };
  }

  componentDidCatch(error: any, errorInfo: any) {
    this.setState({
      hasError: true,
      error,
      errorInfo,
    });
  }

  render() {
    const file = /\/remoteEntry.js$/gi;
    const componentLoaded = file.test(this.state.error.request);
    const isChunkFailed = /Loading chunk [\d]+ failed/.test(this.state.error.message);

    if (this.state.hasError) {
      return componentLoaded ? (
        <NotFoundPage />
      ) : isChunkFailed ? (
        <ConfirmModal
          title="Version update"
          acceptButtonTitle="Reload"
          show={true}
          showAcceptButton={true}
          showCancelButton={false}
          content={
            <div>
              A new version has been released. Need to reload the page to apply changes.
              <p>
                <small>(After refresh, if modal is shown again, please clear cache)</small>
              </p>
            </div>
          }
          onAccept={() => window.location.reload()}
        />
      ) : (
        <div className="container">
          <div className="mainContainer">
            <div
              style={{
                backgroundColor: '#252a33',
                margin: '0 50px',
                padding: '50px',
              }}
            >
              <h2>Something went wrong.</h2>
              <details style={{ whiteSpace: 'pre-wrap' }}>
                {this.state.error && this.state.error.toString()}
                <br />
                {this.state.errorInfo?.componentStack}
              </details>
              <div style={{ height: '2rem' }} />
              <Link to="/">{getTranslatedLabel('GoToHomePage')}</Link>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
