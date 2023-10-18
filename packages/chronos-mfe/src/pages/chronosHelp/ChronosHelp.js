import classNames from 'classnames';
import React from 'react';
import { Link, useHistory } from 'react-router-dom';
import Styles from './chronos-help.scss';
import Breadcrumb from '../../components/breadcrumb/Breadcrumb';
// import help from '../../assets/help.png';
import { Envs } from '../../utilities/envs';

const ChronosHelp = () => {
  const history = useHistory();
  const goback = () => {
    history.goBack();
  };
  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <Breadcrumb>
          <li><Link to='/'>Chronos Forecasting</Link></li>
          <li>Help</li>
        </Breadcrumb>
        <h3 className={classNames(Styles.title)}>Chronos Help</h3>
        <div className={classNames(Styles.wrapper)}>
            <div className={Styles.container}>
              {/* <img src={help} height={'150px'} /> */}
              <p>This page is under construction. In case of questions, don&apos;t hesitate to reach out to the team behind Chronos under <a href={`mailto:${Envs.ADS_EMAIL}`}>{Envs.ADS_EMAIL}</a>. Meanwhile, here are some resources that might help you:</p>
              <ul>
                <li><a href={Envs.CHRONOS_SWAGGER_URL} target='_blank' rel="noreferrer">API swagger documentation</a></li>
                <li><a href={`/chronos-templates/Chronos_GUI_tutorial.pptx`} download={true}>Chronos Application tutorial</a></li>
                <li><a href={Envs.CHRONOS_DOCUMENTATION_URL} target='_blank' rel='noopener noreferrer'>Chronos technical documentation</a></li>
              </ul>
              <button className='btn btn-secondary' onClick={goback}>Go Back</button>
            </div>
        </div>
      </div>
    </>
  );
};
export default ChronosHelp;