import React from 'react'; 
import { DrawIoEmbed } from 'react-drawio';
import classNames from 'classnames';
import Styles from './CodeSpaceBlueprint.scss';
import { blueprintTemplate } from './blueprintTemplate';

const CodeSpaceBlueprint = ({ codespace }) => {
  return (
    <div className={classNames(Styles.csbWrapper)}>
      <DrawIoEmbed
        xml={blueprintTemplate(codespace)}
        urlParameters={{
          ui: 'dark',
          spin: true,
          saveAndExit: false,
          noSaveBtn: true,
          noExitBtn: true,
          configure: true,
          lightbox: true,
        }}
        configuration={{
          sidebarWidth: 0,
        }}
      />
    </div>
  );
};

export default CodeSpaceBlueprint;
