/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): notification.js
 * --------------------------------------------------------------------------
 */

/**
 * ------------------------------------------------------------------------
 * Constants
 * ------------------------------------------------------------------------
 */

class Notification {
  static show(message, type = 'success', hideDelay = 6000) {
    const notificationElement = document.createElement('DIV');
    const messageElement = document.createElement('P');
    const closeElement = document.createElement('I');
    closeElement.classList.add('icon', 'close');
    notificationElement.classList.add('notification', 'toast');
    if (type) {
      notificationElement.classList.add(type);
    }

    messageElement.innerText = message;
    notificationElement.append(messageElement);
    notificationElement.append(closeElement);

    closeElement.addEventListener('click', (evt) => {
      evt.target.parentNode.classList.add('hide');
    });

    notificationElement.addEventListener('transitionend', (evt) => {
      const notificationElement = evt.target;
      const notificationWrapper = notificationElement.parentNode;
      if (notificationWrapper && notificationElement.classList.contains('hide')) {
        notificationWrapper.removeChild(notificationElement);
        if (!notificationWrapper.childNodes.length) {
          notificationWrapper.parentNode.removeChild(notificationWrapper);
        }

        if (notificationWrapper.childNodes.length > 3) {
          notificationWrapper.firstChild.classList.remove('show');
          notificationWrapper.firstChild.classList.add('hide');
        }
      }
    });

    const wrapper = document.getElementById('notification-wrapper');
    if (wrapper) {
      wrapper.append(notificationElement);
      if (wrapper.childNodes.length > 3) {
        wrapper.firstChild.classList.add('hide');
      }
    } else {
      const notificationWrapper = document.createElement('DIV');
      notificationWrapper.setAttribute('id', 'notification-wrapper');
      notificationWrapper.classList.add('notification-wrapper');
      notificationWrapper.append(notificationElement);
      document.body.append(notificationWrapper);
    }

    setTimeout(() => {
      notificationElement.classList.add('show');
      if (wrapper && wrapper.childNodes.length > 3) {
        notificationElement.classList.add('delay');
      }
    }, 20);

    if (wrapper && wrapper.childNodes.length > 3) {
      setTimeout(() => {
        notificationElement.classList.remove('delay');
      }, 100);
    }

    setTimeout(() => {
      notificationElement.classList.add('hide');
    }, hideDelay);
  }
}

export default Notification;
