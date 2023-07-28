import { Pkce } from '../../src/services/Pkce';
import { ApiClient } from '../../src/services/ApiClient';

declare global {
    interface Window {
        refreshState: {
            refreshApiTriggeredAt: number | null;
            newJwtToken: string | null;
            refreshTokenPromise: Promise<string | void> | null;
        };
    }
}

const ONE_MINUTE = 50000; // 1 minutes in milliseconds

export const refreshToken = (jwt: string) => {
    if (!window.refreshState) {
        window.refreshState = {
            refreshApiTriggeredAt: null,
            newJwtToken: null,
            refreshTokenPromise: null,
        };
    }

    if (window.refreshState.refreshTokenPromise) {
        // Return the existing refresh token promise
        return window.refreshState.refreshTokenPromise;
    }

    // Create a new refresh token promise
    window.refreshState.refreshTokenPromise = new Promise<string | void>((resolve, reject) => {
        // If 1st refresh api is success already don't call refresh api again instead reuse first api token.
        if (window.refreshState.refreshApiTriggeredAt && window.refreshState.newJwtToken &&
            (Date.now() - window.refreshState.refreshApiTriggeredAt < ONE_MINUTE)) {
            resolve(window.refreshState.newJwtToken);
            return window.refreshState.newJwtToken;
        }
        // Perform the token refresh operation here
        performRefreshToken(jwt)
            .then((newJwt: any) => {
                window.refreshState.newJwtToken = newJwt;
                window.refreshState.refreshApiTriggeredAt = Date.now();
                const timeout = setTimeout(() => {
                    console.log('Delayed function executed.');
                    window.refreshState.newJwtToken = null;
                    window.refreshState.refreshApiTriggeredAt = null;
                    clearTimeout(timeout); // Clear the timeout inside the setTimeout callback
                }, ONE_MINUTE);
                resolve(newJwt); // Resolve the refresh token promise
                return newJwt;
            })
            .catch((error) => {
                handleRefreshError(error);
                reject(error); // Reject the refresh token promise with the error
            });
    }).finally(() => {
        // Reset the refresh token promise once it's resolved or rejected
        window.refreshState.refreshTokenPromise = null;
    });

    return window.refreshState.refreshTokenPromise;
}

const performRefreshToken = async (jwt: string) => {
    return await fetch(`${'refresh'}?timestamp=${Date.now()}`, {
        headers: {
            Accept: 'application/json',
            'Content-Type': 'application/json',
            Authorization: jwt,
        },
    })
        .then((response) => {
            if (!response.ok) {
                return response.json().then((result) => {
                    throw new Error(result.message);
                });
            }

            if (response.headers.get('authorization')) {
                const newJwt = response.headers.get('authorization');
                ApiClient.writeJwt(newJwt);

                // Return the new JWT token
                return newJwt;
            }
        }).catch((error) => {
            handleRefreshError(error);
        })
}

const handleRefreshError = (error: any) => {
    // Handle error during refresh
    console.log('Error occurred during refresh with error', error);

    // Get the current URL
    const currentURL = window.location.href;
    const baseUrl = currentURL.split('#')[0];
    const sessionExpiredUrl = `${baseUrl}#/SessionExpired`;
    Pkce.clearUserSession();
    window.location.href = sessionExpiredUrl;
}