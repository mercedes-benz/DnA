import cn from 'classnames';
import * as React from 'react';
import { ISharing } from 'globals/types';
import Styles from './SharingSummary.scss';
const classNames = cn.bind(Styles);

export interface ITeamProps {
    sharing: ISharing;
}

const formatEmptyText = (displayVal: string) => {
    return displayVal && displayVal !== '' && displayVal.toLowerCase() !== "choose" ? displayVal : 'NA';
};

export default function SharingSummary(props: ITeamProps) {
    return (
        <React.Fragment>
            <div className={classNames(Styles.flexLayout, Styles.mainPanel, 'mainPanelSection')}>
                <div id="platformSummery" className={Styles.wrapper}>
                    <div>
                        <h3>Sharing</h3>
                    </div>
                    <div className={Styles.firstPanel}>
                        <div className={classNames(Styles.flexLayout)}>
                            <div id="gitRepoInfo" className={classNames(Styles.solutionSection)}>
                                <label className="input-label summary">Git Repository</label>
                                <br />
                                <div className={classNames(Styles.row)}>
                                    {props.sharing.gitUrl && props.sharing.gitUrl !== '' ? (
                                        <a href={props.sharing.gitUrl} target="_blank" rel="noreferrer">
                                            {props.sharing.gitUrl}
                                        </a>
                                    ) : (
                                        'NA'
                                    )}
                                </div>
                            </div>
                            <div id="results" className={classNames(Styles.solutionSection)}>
                                <label className="input-label summary">Results</label>
                                <br />
                                <div className={classNames(Styles.row)}>{formatEmptyText(props.sharing.result.name)}</div>
                            </div>
                            <div id="commentInfo" className={classNames(Styles.solutionSection)}>
                                <label className="input-label summary">Comment</label>
                                <br />
                                <div className={classNames(Styles.row)}>
                                    {props.sharing.resultUrl && props.sharing.resultUrl !== '' ? (
                                        <a href={props.sharing.resultUrl} target="_blank" rel="noreferrer">
                                            {props.sharing.resultUrl}
                                        </a>
                                    ) : (
                                        'NA'
                                    )}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>{' '}
        </React.Fragment>
    );
}
