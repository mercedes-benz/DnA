import classNames from 'classnames';
import React from 'react';
import Styles from './ChronosBanner.scss';
import { markdownParser } from 'dna-container/MarkdownParser';

const ChronosBanner = ({ bannerText, onBannerClose }) => {
    return (
        <div className={Styles.mainPanel}>
            <div className={classNames(Styles.banner)}>
                <div className={classNames(Styles.content)}>
                    <div className={classNames(Styles.placeholder)}>
                        <i className="icon mbc-icon info" />
                        <h5>Upcoming Features:</h5>
                    </div>
                    <div className={classNames(Styles.info)}>
                        <p
                            dangerouslySetInnerHTML={{ __html: markdownParser(bannerText) }}
                        />
                    </div>
                </div>
                <button className={classNames('btn btn-primary', Styles.button)} onClick={onBannerClose}>
                    <h4>don&apos;t show again</h4>
                    <i className="icon mbc-icon close thin" />
                </button>
            </div>
        </div>
    );
};

export default ChronosBanner;
