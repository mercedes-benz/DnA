import React, { useState, useEffect } from 'react';
import { Pkce } from '../../services/Pkce';
import { ApiClient } from '../../services/ApiClient';
import { ConfirmModal } from '../formElements/modal/confirmModal/ConfirmModal';
let sessionTimeout: any;
const SessionAlert = () => {
  const [modalOpen, setModalOpen] = useState(false);
  const idleEvents = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click'];

  const eventCleanUp = () => {
    window.removeEventListener('load', resetTimer, true);
    idleEvents.forEach((event) => {
      document.removeEventListener(event, resetTimer, true);
    });
  };

  const poll = async () => {
    await ApiClient.verifyDigiLogin()
      .then((result) => {
        ApiClient.writeJwt(result.token);
      })
      .catch((error: Error) => {
        clearTimeout(sessionTimeout);
        Pkce.clearUserSession();
        setModalOpen(false);
        eventCleanUp();
        window.location.href = Pkce.getLogoutUrl();
      });
  };

  const resetTimer = () => {
    clearTimeout(sessionTimeout);
    sessionTimeout = setInterval(() => !modalOpen && poll(), 30000);
  };

  const reloadPage = () => {
    // Simple reload will start login flow since token in session got cleared
    window.location.reload();
  };

  useEffect(() => {
    window.addEventListener('load', resetTimer, true);
    idleEvents.forEach((event) => {
      document.addEventListener(event, resetTimer, true);
    });
  }, []);

  // Component unload cleanup
  useEffect(() => {
    return () => {
      eventCleanUp();
    };
  }, []);

  return (
    modalOpen && (
      <ConfirmModal
        title="Session Expired!"
        acceptButtonTitle="Try login again"
        showAcceptButton={true}
        showCancelButton={false}
        show={true}
        content={<h3 className="text-center">Session Expired!. Please login to continue.</h3>}
        onAccept={reloadPage}
      />
    )
  );
};
export default SessionAlert;
