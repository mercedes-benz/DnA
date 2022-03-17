import React from 'react';
import { Link } from 'react-router-dom';
import Styles from './BucketList.scss';
import classNames from 'classnames';

export const BucketList = ({ bucketList }) => {
  return (
    <div className={classNames('expanstion-table', Styles.bucketList)}>
      <div className={Styles.bucketGrp}>
        <div className={Styles.bucketGrpList}>
          <div className={Styles.bucketGrpListItem}>
            <div className={Styles.bucketCaption}>
              <div className={Styles.bucketTile}>
                <div className={Styles.bucketTitleCol}>
                  <label>
                    <i className="icon sort" />
                    Bucket Name
                  </label>
                </div>
                <div className={Styles.bucketTitleCol}>
                  <label>
                    <i className="icon sort" />
                    Permission
                  </label>
                </div>
                <div className={Styles.bucketTitleCol}>
                  <label>
                    <i className="icon sort" />
                    Created On
                  </label>
                </div>
                <div className={Styles.bucketTitleCol}>
                  <label>
                    <i className="icon sort" />
                    Last Modified On
                  </label>
                </div>
                <div className={Styles.bucketTitleCol}>Action</div>
              </div>
            </div>
            {bucketList?.map((item, index) => {
              return (
                <div
                  key={index}
                  className={'expansion-panel-group airflowexpansionPanel ' + Styles.bucketGrpListItemPanel}
                >
                  <div className={classNames('expansion-panel ', index === 0 ? 'open' : '')}>
                    <span className="animation-wrapper"></span>
                    <input type="checkbox" className="ff-only" id={index + '1'} defaultChecked={index === 0} />
                    <label className={Styles.expansionLabel + ' expansion-panel-label '} htmlFor={index + '1'}>
                      <div className={Styles.bucketTile}>
                        <div className={Styles.bucketTitleCol}>
                          <Link
                            to={{
                              pathname: `explorer/${item.name}`,
                            }}
                          >
                            {item.name}
                          </Link>
                        </div>
                        <div className={Styles.bucketTitleCol}>{item?.permission?.join(', ')}</div>
                        <div className={Styles.bucketTitleCol}>{item.createdOn}</div>
                        <div className={Styles.bucketTitleCol}>{item.modifiedOn}</div>
                        <div className={Styles.bucketTitleCol}></div>
                      </div>

                      <i tooltip-data="Expand" className="icon down-up-flip"></i>
                    </label>
                    <div className="expansion-panel-content">
                      <div className={Styles.bucketColContent}>
                        <div className={Styles.actionBtnGrp}>
                          <button className={'btn btn-primary'} type="button" onClick={() => {}}>
                            <i className="icon mbc-icon edit"></i>
                            <span>Edit Bucket</span>
                          </button>
                          <button className={'btn btn-primary'} type="button" onClick={() => {}}>
                            <i className="icon delete"></i>
                            <span>Delete Bucket </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
