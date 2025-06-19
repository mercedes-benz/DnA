import * as React from 'react';
import { Envs } from 'globals/Envs';

const DeactivatedUser: React.FC = () => {
  return (
    <div className="container">
      <div className="mainContainer content full">
        <div className="message">
          <h1>Your account has been Deactivated</h1>
          <div>
            <p>
              You no longer have access to the DnA platform. Your account is currently marked as deactivated.
            </p>
            <p>
              Please contact the DnA team via{' '}
              <a href={Envs.DNA_EMAIL_LINK} target="_blank" rel="noopener noreferrer">Email</a>,{' '}
              <a href={Envs.DNA_TEAMS_LINK} target="_blank" rel="noopener noreferrer">Microsoft Teams</a>, or{' '}
              <a href={Envs.DNA_MATTERMOST_LINK} target="_blank" rel="noopener noreferrer">Mattermost</a> for assistance.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DeactivatedUser;
