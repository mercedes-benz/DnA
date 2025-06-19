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
              <div dangerouslySetInnerHTML={{ __html: Envs.DNA_CONTACTUS_HTML }}></div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DeactivatedUser;
