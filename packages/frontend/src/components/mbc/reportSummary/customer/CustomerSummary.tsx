import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import Styles from './CustomerSummary.scss';
import { ICustomers, 
  // ITeams 
} from 'globals/types';
// import TeamMemberListItem from 'components/mbc/summary/team/teamMemberListItem/TeamMemberListItem';
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
    // const teamMembersList = this.props.customers.processOwners?.map((member: ITeams, index: number) => {
    //   return <TeamMemberListItem key={index} itemIndex={index} teamMember={member} />;
    // });
    return (
      <React.Fragment>
        <div className={classNames(Styles.mainPanel, 'mainPanelSection')}>
          <div className={Styles.wrapper}>
            <div className={Styles.firstPanel}>
              {this.props.customers.internalCustomers?.length
                ? this.props.customers.internalCustomers?.map((customer, index) => {
                    return (
                      <React.Fragment key={index}>
                        <div className={Styles.customerListView}>
                          <span className={Styles.description}>{`Internal Customer ${index + 1}`}</span>
                          <div className={Styles.flexLayout}>
                            <div id="name">
                              <label className="input-label summary">Name</label>
                              <br />
                              <div>{customer.name?.firstName}{' '}{customer.name?.lastName}</div>
                            </div>
                            <div id="hierarchy">
                              <label className="input-label summary">Level</label>
                              <br />
                              <div>{customer.level}</div>
                            </div>
                            <div id="department">
                              <label className="input-label summary">E2-Department</label>
                              <br />
                              <div>{customer.department}</div>
                            </div>
                          </div>
                          <div className={Styles.flexLayout}>
                            <div id="ressort">
                              <label className="input-label summary">MB Legal Entity</label>
                              <br />
                              <div>{customer.legalEntity}</div>
                            </div>
                            <div id="customerRelation">
                              <label className="input-label summary">Customer Relation</label>
                              <br />
                              <div>{customer.customerRelation}</div>
                            </div>
                            <div id="comment">
                              <label className="input-label summary">Comment</label>
                              <p>
                                <pre className={Styles.commentPre}>{customer.comment}</pre>
                              </p>
                            </div>
                            
                          </div>
                          {(this.props.customers.internalCustomers?.length > 1 ||
                            this.props.customers.internalCustomers?.length) && <hr className="divider1" />}
                        </div>
                      </React.Fragment>
                    );
                  })
                : null}
              {this.props.customers.externalCustomers?.length
                ? this.props.customers.externalCustomers?.map((customer, index) => {
                    return (
                      <React.Fragment key={'external'+ index}>
                        <div className={Styles.customerListView}>
                          <span className={Styles.description}>{`External Customer ${index + 1}`}</span>
                          <div className={Styles.flexLayout}>
                            <div id="nameExternal">
                              <label className="input-label summary">Name</label>
                              <br />
                              <div>{customer.name?.firstName}{' '}{customer.name?.lastName}</div>
                            </div>
                            <div id="companyName">
                              <label className="input-label summary">Company Name</label>
                              <br />
                              <div>{customer.companyName}</div>
                            </div>
                            <div id="customerRelationExternal">
                              <label className="input-label summary">Customer Relation</label>
                              <br />
                              <div>{customer.customerRelation}</div>
                            </div>
                          </div>
                          <div className={Styles.commentSection}>
                            <label className="input-label summary">Comment</label>
                            <p>
                              <pre className={Styles.commentPre}>{customer.comment}</pre>
                            </p>
                          </div>
                          {(this.props.customers.externalCustomers?.length > 1 ||
                            this.props.customers.externalCustomers?.length) && <hr className="divider1" />}
                        </div>
                      </React.Fragment>
                    );
                  })
                : null}  
              {/* {this.props.customers.processOwners?.length ? (
                <div className={Styles.customerListView}>
                  <div className={Styles.processOwnerCardSection}>
                    <label className="input-label summary">Process Owner</label>
                    <br />
                    <div>{teamMembersList}</div>
                  </div>
                </div>
              ) : null} */}
            </div>
          </div>
        </div>
      </React.Fragment>
    );
  }
}
