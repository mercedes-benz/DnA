import cn from 'classnames';
import * as React from 'react';
import { OPEN_SOURCE_TOOLS } from 'globals/constants';
import { Envs } from 'globals/Envs';
import Styles from './About.scss';

// @ts-ignore
const classNames = cn.bind(Styles);

const About = () => {
  const applicationVersion = document?.querySelector('meta[name="application-version"]')?.getAttribute('content');
  return (
    <div className={Styles.content}>
      <div>
        <img className="logo" src={Envs.DNA_APP_LOGO_URL} />
        <br />
        <h4 className="name">
          {Envs.DNA_APPNAME_HEADER} - Version {applicationVersion}
        </h4>
        <p>
          Data and Analytics Platform provides A-Z solution for enterprises in Analytics area, from transparency
          <br />
          on planned and ongoing activities up to providing open source components to realize them.
          <br />
          All in self-service manner, cloud independent with minimum infrastructure footprint.
        </p>
        <p>
          Source -{' '}
          <a href="https://github.com/mercedes-benz/DnA" target="_blank" rel="noreferrer">
            GitHub
          </a>
          &nbsp;|&nbsp; License -{' '}
          <a href="https://github.com/mercedes-benz/DnA/blob/main/LICENSE" target="_blank" rel="noreferrer">
            MIT
          </a>
        </p>
        <label>--- Powered by ---</label>
        <ul className="list-item-group">
          {OPEN_SOURCE_TOOLS.map((item: any, index: number) => (
            <li className="list-item" key={index}>
              <div className="item-text-wrap">
                <h6 className="item-text-title">{item.name}</h6>
                <label className="item-text">Version - {item.version}</label>
                <br />
                <a href={item.github} target="_blank" rel="noreferrer">
                  GitHub
                </a>
                &nbsp;|&nbsp;{' '}
                <a href={item.license.link} target="_blank" rel="noreferrer">
                  {item.license.name}
                </a>
              </div>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
};

export default About;
