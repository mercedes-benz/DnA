/**
 * Wrapper component for the MFE local dev setup
 */
import React, { lazy } from 'react';

const Header = lazy(() => import('dna-container/Header'));
const MainNavigation = lazy(() => import('dna-container/MainNavigation'));
const Footer = lazy(() => import('dna-container/Footer'));

export const LocalWrapper = ({ children }) => {
  return (
    <div className="container">
      <Header
        user={{
          department: 'TE/ST',
          eMail: 'demouser@web.de',
          firstName: 'Demo',
          lastName: 'User',
          id: 'DEMOUSER',
          mobileNumber: '',
          roles: [
            {
              id: '3',
              name: 'Admin',
            },
          ],
        }}
      />
      <main id="mainContainer" className="mainContainer">
        <aside>
          <MainNavigation showExpandEffect={false} isMaximized={false} onNavOpen={() => {}} onNavClose={() => {}} />
        </aside>
        <section>{children}</section>
        <Footer />
      </main>
    </div>
  );
};
