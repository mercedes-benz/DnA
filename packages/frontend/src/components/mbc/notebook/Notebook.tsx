import * as React from 'react';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';

// @ts-ignore
import { Envs } from 'globals/Envs';
import { ProvisionSource } from 'globals/Enums';
import { IUserInfo, INotebookInfo, INotebookInfoData } from 'globals/types';
import { history } from '../../../router/History';
import { attachEllipsis, trackEvent } from '../../../services/utils';
import { ApiClient } from '../../../services/ApiClient';
import Modal from 'components/formElements/modal/Modal';
import NewSandbox from '../newSandbox/NewSandbox';
import { Editdetails } from './editdetails/Editdetails';
import Styles from './Notebook.scss';
import NotebookWorkspace from './NotebookWorkspace';
import Provisionsolution from '../provisionsolution/Provisionsolution';
import FullScreenModeIcon from 'components/icons/fullScreenMode/FullScreenModeIcon';
import Caption from '../shared/caption/Caption';

// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';

interface INotebookState {
  noteBookStatus: true;
  loading: boolean;
  fsNeed: boolean;
  notebookStart: boolean;
  editDetails: boolean;
  provisionsolution: boolean;
  modalWidth: string;
  modalWidthForProvision: string;
  openSolution: boolean;
  provisionCurrectStatus: boolean;
  alternateNotebookisNeed: boolean;
  sandBox: boolean;
  noteBookDefaultData: any;
  noteBookData: INotebookInfo;
  labView: boolean;
  provisionSolutionId: string;
  notebookId: string;
}
export interface INotebookProps {
  user: IUserInfo;
}
export default class Notebook extends React.Component<INotebookProps, INotebookState> {
  constructor(props: INotebookProps) {
    super(props);
    this.state = {
      noteBookStatus: true,
      loading: true,
      fsNeed: false,
      notebookStart: false,
      editDetails: false,
      provisionsolution: false,
      modalWidth: '500px',
      modalWidthForProvision: '80%',
      openSolution: false,
      provisionCurrectStatus: false,
      alternateNotebookisNeed: true,
      sandBox: false,
      noteBookDefaultData: {
        name: props.user.firstName + ' Workspace',
        description: 'Jupyter Notebook workspace of ' + props.user.firstName,
      },
      noteBookData: null,
      labView: true,
      provisionSolutionId: null,
      notebookId: null,
    };
  }

  public render() {
    return (
      <div className={this.state.fsNeed ? Styles.notenbookwarppaerFsmode : '' + ' ' + Styles.notebookwarpper}>
        {this.state.noteBookData == null ? (
          ''
        ) : (
          <React.Fragment>
            {!this.state.fsNeed && <Caption title="Jupyter Notebook Workspace" disableTitle={true} />}
            <div className={Styles.nbheader}>
              <div className={Styles.headerdetails}>
                <img src={Envs.DNA_BRAND_LOGO_URL} className={Styles.Logo} />
                <div className={Styles.nbtitle}>
                  <h6>Workspace Name</h6>
                  <h2>
                    {this.state.noteBookData === null
                      ? this.state.noteBookDefaultData.name
                      : this.state.noteBookData.name}
                  </h2>
                </div>
                <div className={Styles.nbtitle + ' ' + Styles.nbDescr}>
                  <h6>Description</h6>
                  <p>{attachEllipsis(this.state.noteBookData.description, 80)}</p>
                </div>
                {this.state.notebookStart && (
                  <div className={Styles.nbeditmode}>
                    {this.state.provisionCurrectStatus ? (
                      <React.Fragment>
                        <div className={Styles.notebookgrpicon}>
                          <i
                            className="icon mbc-icon edit small "
                            tooltip-data={'Edit Details'}
                            onClick={this.editdetails}
                          />
                          <span className={Styles.filterStatus}> &nbsp; </span>
                        </div>
                        <div className={Styles.openSolution} onClick={this.openSolution}>
                          Open solution <i className="icon mbc-icon arrow small right" />
                        </div>
                      </React.Fragment>
                    ) : (
                      <React.Fragment>
                        <div className={Styles.notebookgrpicon}>
                          <i
                            className="icon mbc-icon edit small "
                            tooltip-data={'Edit Details'}
                            onClick={this.editdetails}
                          />
                          <span className={Styles.filterStatus}> &nbsp; </span>
                        </div>
                        <div className={Styles.notebookgrpicon}>
                          <i
                            className="icon mbc-icon provision"
                            tooltip-data={'Provision to Solution'}
                            onClick={this.provisionsolution}
                          />
                          <span className={Styles.filterStatus}> &nbsp; </span>
                        </div>
                        {/* <div>
                          <i className="icon mbc-icon info " tooltip-data={'  Info  '} />
                        </div> */}
                      </React.Fragment>
                    )}
                  </div>
                )}
              </div>
              <div className={Styles.navigation}>
                {this.state.notebookStart && (
                  <div className={Styles.headerright}>
                    <div tooltip-data="Stop Notebook" onClick={this.stopNotebook} className={Styles.stopNbbtn}>
                      <span> &nbsp; </span>
                    </div>
                    <div className={this.state.labView ? Styles.active : ''}>
                      <div
                        tooltip-data={this.state.labView ? 'Tree View' : 'Lab View'}
                        onClick={this.toggleNotebook}
                        className={Styles.toggleNotebook}
                      >
                        {this.state.labView ? (
                          <i className="icon mbc-icon tree-view" />
                        ) : (
                          <i className="icon mbc-icon lab-view" />
                        )}
                      </div>
                    </div>
                    <div tooltip-data="Open NewTab" className={Styles.OpenNewTab} onClick={this.openNotebookNewtab}>
                      <i className="icon mbc-icon arrow small right" />
                      <span> &nbsp; </span>
                    </div>
                    <div onClick={this.fscreen}>
                      <FullScreenModeIcon fsNeed={this.state.fsNeed} />
                    </div>
                  </div>
                )}
              </div>
            </div>
            <div className={Styles.notenbookwarppaerContent}>
              {
                <div className={Styles.nbworkspace}>
                  {/* {!this.state.notebookStart && (
               <button className={' btn btn-tertiary ' + Styles.btnPrimary} onClick={this.startNotebook}>
                 {' '}
                 Start Notebook{' '}
               </button>
             )} */}
                  {this.state.loading ? (
                    <div className={'progress-block-wrapper ' + Styles.preloaderCutomnize}>
                      <div className="progress infinite" />
                    </div>
                  ) : (
                    this.state.notebookStart && (
                      <NotebookWorkspace
                        enableFullScreenMode={this.state.fsNeed}
                        isLabView={this.state.alternateNotebookisNeed}
                        userName={this.props.user.id}
                      />
                    )
                  )}
                </div>
              }
            </div>
          </React.Fragment>
        )}
        {this.state.provisionsolution && (
          <Modal
            title={''}
            showAcceptButton={false}
            showCancelButton={false}
            modalWidth={this.state.modalWidthForProvision}
            buttonAlignment="right"
            show={this.state.provisionsolution}
            content={
              <Provisionsolution
                provisionStatus={this.provisionStatus}
                provisionFrom={ProvisionSource.NOTEBOOK}
                provisionedSolutionId={this.provisionId}
              />
            }
            scrollableContent={false}
            onCancel={this.modalClose}
          />
        )}
        {this.state.noteBookData !== null && (
          <Modal
            title={''}
            showAcceptButton={false}
            showCancelButton={false}
            modalWidth={this.state.modalWidth}
            buttonAlignment="right"
            show={this.state.editDetails}
            content={
              <Editdetails
                notebookexistingDetails={this.state.noteBookData}
                isNotebookUpdateSuccess={this.isNotebookUpdateSuccess}
              />
            }
            scrollableContent={false}
            onCancel={this.modalClose}
          />
        )}
        {/* <InfoModal
          title={''}
          show={this.state.provisionsolution}
          content={<ProvisionSolution />}
          onCancel={this.provisionsolutionClose}
        /> */}
        {this.state.noteBookData === null ? (
          <Modal
            title={''}
            showAcceptButton={false}
            showCancelButton={false}
            modalWidth={this.state.modalWidth}
            buttonAlignment="right"
            show={this.state.sandBox}
            content={
              <NewSandbox
                namePrefix={this.props.user.firstName}
                isNotebookCreationSuccess={this.isNotebookCreationSuccess}
              />
            }
            scrollableContent={false}
            onCancel={this.sandBoxclose}
          />
        ) : (
          ''
        )}
      </div>
    );
  }
  public componentDidMount() {
    ApiClient.getNotebooksDetails()
      .then((res) => {
        if (!Array.isArray(res)) {
          this.setState(
            {
              loading: true,
              sandBox: false,
              noteBookData: res,
              provisionsolution: false,
              provisionSolutionId: res.solutionId,
              notebookId: res.id,
            },
            () => {
              const loginWindow = window.open(
                Envs.JUPYTER_NOTEBOOK_OIDC_POPUP_URL,
                'jupyterNotebookSessionWindow',
                'width=100,height=100,location=no,menubar=no,status=no,titlebar=no,toolbar=no',
              );
              setTimeout(() => {
                loginWindow?.close();

                this.setState(
                  {
                    loading: false,
                    notebookStart: true,
                  },
                  () => {
                    Notification.show('Notebook Started!');
                    if (res.solutionId != null) {
                      this.setState({
                        provisionCurrectStatus: true,
                      });
                    }
                    Tooltip.defaultSetup();
                  },
                );
              }, Envs.JUPYTER_NOTEBOOK_OIDC_POPUP_WAIT_TIME);
            },
          );
        } else if (Array.isArray(res)) {
          this.setState({
            sandBox: true,
            noteBookData: null,
          });
        }
      })
      .catch((error: Error) => {
        Notification.show(error.message, 'alert');
      });
  }

  protected fscreen = () => {
    this.setState(
      {
        fsNeed: !this.state.fsNeed,
      },
      () => {
        trackEvent(
          'DnA Notebook',
          'View Mode',
          'Notebook iframe view changed to ' + (this.state.fsNeed ? 'Full Screen Mode' : 'Normal Mode'),
        );
      },
    );
  };

  protected stopNotebook = () => {
    ProgressIndicator.show();
    ApiClient.stopNotebook()
      .then((res) => {
        trackEvent('DnA Notebook', 'Notebook Status', 'Stopped');
        Notification.show('Notebook ' + res.success);
        ProgressIndicator.hide();
        window.location.href = window.location.origin;
      })
      .catch((err) => err);
  };
  protected editdetails = () => {
    this.setState({
      editDetails: true,
    });
  };
  protected provisionsolution = () => {
    this.setState({
      provisionsolution: true,
    });
  };
  protected modalClose = () => {
    this.setState({
      editDetails: false,
      provisionsolution: false,
      sandBox: false,
    });
  };
  protected modalCloseProvision = (status: boolean) => {
    this.setState({
      provisionsolution: status,
    });
  };
  protected provisionId = (dnaNotebookId: string) => {
    this.setState({
      provisionSolutionId: dnaNotebookId,
    });
  };
  protected sandBoxclose = () => {
    this.setState({ sandBox: false }, () => {
      history.goBack();
    });
  };
  protected provisionStatus = (status: boolean) => {
    this.setState({
      provisionCurrectStatus: true,
      provisionsolution: status,
    });
  };
  protected openSolution = () => {
    const provisionSolutionId = this.state.provisionSolutionId;
    trackEvent('DnA Notebook', 'View linked solution summary', 'Open solution' + provisionSolutionId);
    history.push('/summary/' + provisionSolutionId);
  };
  protected toggleNotebook = () => {
    this.setState(
      {
        alternateNotebookisNeed: !this.state.alternateNotebookisNeed,
        labView: !this.state.labView,
      },
      () => {
        trackEvent(
          'DnA Notebook',
          'View Mode',
          'Notebook view changed to ' + (this.state.labView ? 'Lab View' : 'Tree View'),
        );
      },
    );
  };

  protected isNotebookCreationSuccess = (status: boolean, notebookdata: INotebookInfo) => {
    this.setState(
      {
        sandBox: status,
        noteBookData: notebookdata,
      },
      () => {
        const loginWindow = window.open(
          Envs.JUPYTER_NOTEBOOK_OIDC_POPUP_URL,
          'jupyterNotebookSessionWindow',
          'width=100,height=100,location=no,menubar=no,status=no,titlebar=no,toolbar=no',
        );
        setTimeout(() => {
          loginWindow?.close();
          this.setState(
            {
              sandBox: status,
              loading: false,
              notebookStart: true,
            },
            () => {
              Notification.show('New Notebook Created!');
              Tooltip.defaultSetup();
            },
          );
        }, Envs.JUPYTER_NOTEBOOK_OIDC_POPUP_WAIT_TIME);
      },
    );
  };
  protected isNotebookUpdateSuccess = (status: boolean, notebookdata: INotebookInfoData) => {
    this.setState(
      {
        editDetails: status,
        noteBookData: notebookdata.data,
      },
      () => {
        Notification.show('Notebook Updated!');
      },
    );
  };
  protected openNotebookNewtab = (alternateUrl: any) => {
    // const origin = window.location.origin;
    if (this.state.alternateNotebookisNeed) {
      window.open(`${Envs.JUPYTER_NOTEBOOK_URL}/user/${this.props.user.id}/lab`);
    } else {
      window.open(`${Envs.JUPYTER_NOTEBOOK_URL}/`, '_blank');
    }
    trackEvent('DnA Notebook', 'Notebook Open', 'Open in New Tab');
  };
}
