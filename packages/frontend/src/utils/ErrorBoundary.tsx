import React from 'react';
import NotFoundPage from '../router/NotFoundPage';
import { Link } from 'react-router-dom';
import { getTranslatedLabel } from 'globals/i18n/TranslationsProvider';
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
    if (this.state.hasError) {
      return componentLoaded ? (
        <NotFoundPage />
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
