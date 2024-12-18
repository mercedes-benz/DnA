import React from 'react';
import Styles from './Tutorials.scss';
import { getParams } from '../../Utility/utils';
import CodeSpaceTutorials from './CodeSpaceTutorials';

const Tutorials = () => {
  const params = getParams();
  let id = params?.id;
  
  return (
    <div className={Styles.mainPanel}>
      <div className={Styles.wrapper}>
        <div className={Styles.header}>
          <div className={Styles.breadcrumb}>
            <ol>
              <li><a href='#/'>Start</a></li>
              <li><a href='#/tools'>Tools</a></li>
              <li><a href='#/codeSpaces'>Code Spaces</a></li>
              <li>Tutorials</li>
            </ol>
          </div>
          <h3>Code Space Tutorials</h3>
        </div>
        <div className={Styles.content}>
          <div>
            <CodeSpaceTutorials id={id} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default Tutorials;
