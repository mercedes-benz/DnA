import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import Styles from './CustomerSummary.scss';
import { ICustomers, ITeams } from 'globals/types';
import TeamMemberListItem from 'components/mbc/summary/team/teamMemberListItem/TeamMemberListItem';
const classNames = cn.bind(Styles);

interface ICustomerSummaryProps {
  customers: ICustomers;
}

export default class CustomerSummary extends React.Component<ICustomerSummaryProps> {
  protected isTouch = false;
  protected listRowElement: HTMLDivElement;

  constructor(props: any) {
    super(props);
  }

  public render() {
    const teamMembersList = this.props.customers.processOwners?.map((member: ITeams, index: number) => {
      return <TeamMemberListItem key={index} itemIndex={index} teamMember={member} />;
    });
    return (
      <React.Fragment>
        <div className={classNames(Styles.mainPanel, 'mainPanelSection')}>
          <div className={Styles.wrapper}>
            <div className={Styles.firstPanel}>
              {this.props.customers.customerDetails?.length
                ? this.props.customers.customerDetails?.map((customer, index) => {
                    return (
                      <React.Fragment key={index}>
                        <div className={Styles.customerListView}>
                          <span className={Styles.description}>{`Customer ${index + 1}`}</span>
                          <div className={Styles.flexLayout}>
                            <div id="hierarchy">
                              <label className="input-label summary">Hierarchy</label>
                              <br />
                              <div>{customer.hierarchy}</div>
                            </div>
                            <div id="department">
                              <label className="input-label summary">Department</label>
                              <br />
                              <div>{customer.department}</div>
                            </div>
                            <div id="ressort">
                              <label className="input-label summary">Ressort</label>
                              <br />
                              <div>{customer.ressort}</div>
                            </div>
                          </div>
                          <div className={Styles.commentSection}>
                            <label className="input-label summary">Comment</label>
                            <p>
                              <pre className={Styles.commentPre}>{customer.comment}</pre>
                            </p>
                          </div>
                          {(this.props.customers.customerDetails?.length > 1 ||
                            this.props.customers.processOwners?.length) && <hr className="divider1" />}
                        </div>
                      </React.Fragment>
                    );
                  })
                : null}
              {this.props.customers.processOwners?.length ? (
                <div className={Styles.customerListView}>
                  <div className={Styles.processOwnerCardSection}>
                    <label className="input-label summary">Process Owner</label>
                    <br />
                    <div>{teamMembersList}</div>
                  </div>
                </div>
              ) : null}
            </div>
          </div>
        </div>
      </React.Fragment>
    );
  }
}
