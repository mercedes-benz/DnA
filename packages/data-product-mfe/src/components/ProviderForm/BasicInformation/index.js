import React from 'react';
import Styles from './styles.scss';

const ItemTitle = ({ title, displayLine = true }) => {
  return (
    <div className={Styles.itemTitle}>
      {displayLine ? (
        <span className={Styles.showHRLine}>
          {title}
          <hr />
        </span>
      ) : (
        <span style={{ height: '31px', display: 'block', marginTop: '5px' }}>{title}</span>
      )}
    </div>
  );
};

const BasicInformation = ({ onSave }) => {
  return (
    <>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div className={Styles.heading}>
            <i className={'icon mbc-icon info'} />
            <p>What is the purpose of this document?</p>
          </div>
          <p>
            At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus
            est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy
            eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et
            justo duo dolores et ea rebum. Stet clita kasd gubergren no sea takimata sanctus est Lorem ipsum dolor sit
            amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut
            labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea
            rebum.
          </p>
          <div className={Styles.itemContainer}>
            <div className={Styles.items}>
              <ItemTitle title="Step 1" />
              At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata
              sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet.
            </div>
            <div className={Styles.items}>
              <ItemTitle title="Step 2" />
              At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata
              sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet.
            </div>
            <div className={Styles.items}>
              <ItemTitle title="Step 3" />
              At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata
              sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet.
            </div>
            <div className={Styles.items}>
              <ItemTitle title="Step 4" displayLine={false} />
              At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata
              sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet.
            </div>
          </div>
          <div className="btnContainer">
            <button className="btn btn-primary" type="button" onClick={onSave}>
              Start
            </button>
          </div>
        </div>
      </div>
    </>
  );
};
export default BasicInformation;
