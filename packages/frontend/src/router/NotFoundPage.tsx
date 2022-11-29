import * as React from 'react';
import { Link } from 'react-router-dom';
import { getTranslatedLabel } from 'globals/i18n/TranslationsProvider';

const NotFoundPage = () => {
  const currentPath = window.location?.hash?.split('/')?.length > 2 && window.location?.hash?.split('/')?.[1];
  const mfePaths = {
    data: 'Go to data page',
    storage: 'Go to my storage page',
  };
  return (
    <div className="container">
      <div className="mainContainer content full">
        <div className="message">
          <h1>{getTranslatedLabel('NotFound')}</h1>
          <div>
            <div style={{ height: '2rem' }} />
            <br />
            <Link to="/">{mfePaths[currentPath] || getTranslatedLabel('GoToHomePage')}</Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default NotFoundPage;
