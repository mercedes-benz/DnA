import * as React from 'react';
import { Link } from 'react-router-dom';
import { getTranslatedLabel } from 'globals/i18n/TranslationsProvider';
import { useEffect, useState } from 'react';

const UnAuthorised = () => {
  const [isUnauthorized, setIsUnauthorized] = useState(false);
  const currentPath = window.location?.hash?.split('/')?.length > 2 && window.location?.hash?.split('/')?.[1];
  const mfePaths = {
    data: 'Go to data page',
    storage: 'Go to my storage page',
  };
  
  useEffect(() => {
    const fullPath = window.location.hash;
    if (fullPath === '#/data/Unauthorized') {
      setIsUnauthorized(true);

    }
  }, []);
  return (
    <div className="container">
      <div className="mainContainer content full">
        <div className="message">
          <h1>{getTranslatedLabel('UnAuthorisedMessage')}</h1>
          {isUnauthorized ? (
            <div>
              <div style={{ height: '2rem' }} />
              <Link to="/datasharing">{mfePaths.data}</Link>
            </div>
          ) : (
          <div>
            <div style={{ height: '2rem' }} />
            <br />
            <Link to="/">{mfePaths[currentPath] || getTranslatedLabel('GoToHomePage')}</Link>
          </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default UnAuthorised;