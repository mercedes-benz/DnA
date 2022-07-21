import React from 'react';
import { Link } from 'react-router-dom';

const SessionExpired = () => {
  return (
    <div className="container">
      <div className="mainContainer content full">
        <div className="message">
          <h1>Session Expired</h1>
          <div>
            <div style={{ height: '2rem' }} />
            <br />
            <Link to={'/'}>Refresh Session</Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SessionExpired;
