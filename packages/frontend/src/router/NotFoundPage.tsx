import * as React from 'react';
import { Link } from 'react-router-dom';
import { getTranslatedLabel } from 'globals/i18n/TranslationsProvider';

const NotFoundPage = () => (
  <div className="container">
    <div className="mainContainer content full">
      <div className="message">
        <h1>{getTranslatedLabel('NotFound')}</h1>
        <div>
          <div style={{ height: '2rem' }} />
          <br />
          <Link to="/">{getTranslatedLabel('GoToHomePage')}</Link>
        </div>
      </div>
    </div>
  </div>
);

export default NotFoundPage;
