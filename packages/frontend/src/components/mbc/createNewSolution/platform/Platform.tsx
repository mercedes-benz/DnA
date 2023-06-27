import cn from 'classnames';
import * as React from 'react';
import { ComputeFixedTag, ProvisionSource } from 'globals/Enums';
import { IDataiku, INotebookInfo, IPortfolio, ITag, IUserInfo } from 'globals/types';
import Tags from 'components/formElements/tags/Tags';
import DataikuInfo, { IDataikuInfoRef } from './dataikuInfo/DataikuInfo';
import NotebookInfo, { INotebookInfoRef } from './notebookInfo/NotebookInfo';
import Styles from './Platform.scss';
import { Envs } from 'globals/Envs';
import { getDataikuInstanceTag } from '../../../../services/utils';
const classNames = cn.bind(Styles);

export interface IPlatformProps {
  solutionId: string;
  user: IUserInfo;
  portfolio: IPortfolio;
  platforms: ITag[];
  onSaveDraft: (tabToBeSaved: string) => void;
  modifyPortfolio: (portfolio: IPortfolio) => void;
}

export interface IPlatformState {
  portfolio: IPortfolio;
  dnaComputeMode: string;
}

export default class Platform extends React.Component<IPlatformProps, IPlatformState> {
  private notebookInfoRef = React.createRef<INotebookInfoRef>();
  private dataikuInfoRef = React.createRef<IDataikuInfoRef>();
  public static getDerivedStateFromProps(props: IPlatformProps, state: IPlatformState) {
    if (props.portfolio !== state.portfolio) {
      return { portfolio: props.portfolio };
    }
    return null;
  }
  constructor(props: IPlatformProps) {
    super(props);
    this.state = {
      portfolio: {
        solutionOnCloud: false,
        usesExistingInternalPlatforms: false,
        platforms: [],
        dnaNotebookId: null,
        dnaDataikuProjectId: null,
        dnaDataikuProjectInstance: null,
        dnaSubscriptionAppId: null,
      },
      dnaComputeMode: 'others',
    };
  }

  public render() {
    const platforms = this.props.platforms;
    const platformChips: string[] = [];
    if (platforms) {
      this.state.portfolio.platforms.forEach((lang) => {
        platformChips.push(lang.name);
      });
    }
    const { dnaNotebookId, dnaDataikuProjectId, dnaDataikuProjectInstance, solutionOnCloud, usesExistingInternalPlatforms } = this.props.portfolio;
    let { dnaComputeMode } = this.state;
    let fixedChips: string[] = [];
    if (dnaNotebookId) {
      fixedChips = [ComputeFixedTag.NOTEBOOK];
      dnaComputeMode = ProvisionSource.NOTEBOOK;
    }

    if (dnaDataikuProjectId) {
      fixedChips = [ComputeFixedTag.DATAIKU];
      if (dnaDataikuProjectInstance) {
        fixedChips.push(getDataikuInstanceTag(dnaDataikuProjectInstance));
      }
      dnaComputeMode = ProvisionSource.DATAIKU;
    }

    const selectOtherPlatform =
      usesExistingInternalPlatforms && !dnaNotebookId && !dnaDataikuProjectId && dnaComputeMode === 'others';

    let rightSectionContent = usesExistingInternalPlatforms ? (
      <p>Using other {Envs.DNA_COMPANY_NAME} platforms in the solution for computing.</p>
    ) : solutionOnCloud ? (
      <p>Using cloud solutions for compute.</p>
    ) : (
      <p>Solution not using any compute platform.</p>
    );

    if (usesExistingInternalPlatforms) {
      if (dnaNotebookId || dnaComputeMode === ProvisionSource.NOTEBOOK) {
        rightSectionContent = (
          <NotebookInfo
            ref={this.notebookInfoRef}
            notebookId={dnaNotebookId}
            solutionId={this.props.solutionId}
            userFirstName={this.props?.user?.firstName}
            onNoteBookCreationSuccess={this.onNoteBookCreationSuccess}
            onNoteBookLinkRemove={this.onNoteBookLinkRemove}
          />
        );
      } else if (dnaDataikuProjectId || dnaComputeMode === ProvisionSource.DATAIKU) {
        rightSectionContent = (
          <DataikuInfo
            ref={this.dataikuInfoRef}
            onProjectLinkSuccess={this.onDataikuProjectLinkSuccess}
            onProjectLinkRemove={this.onDataikuProjectLinkRemove}
            currSolutionId={this.props.solutionId}
            projectId={dnaDataikuProjectId}
            projectInstance={dnaDataikuProjectInstance}
          />
        );
      }
    }

    const disableExistingPlatformCheck = dnaNotebookId !== null || dnaDataikuProjectId !== null;

    return (
      <React.Fragment>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Compute</h3>
            <div className={classNames(Styles.formWrapper)}>
              <div className={Styles.platformLayout}>
                <div className={Styles.leftSection}>
                  <p>Please select the compute platform your solution using.</p>
                  <div>
                    <label className="checkbox">
                      <span className="wrapper">
                        <input
                          type="checkbox"
                          className="ff-only"
                          checked={solutionOnCloud}
                          onChange={this.onChangeOfSolutionOnCloud}
                        />
                      </span>
                      <span className="label">Compute using solution on cloud </span>
                    </label>
                  </div>
                  <div>
                    <label className="checkbox">
                      <span className="wrapper">
                        <input
                          type="checkbox"
                          className="ff-only"
                          checked={usesExistingInternalPlatforms}
                          onChange={this.onChangeOfusesExistingInternalPlatforms}
                          disabled={disableExistingPlatformCheck}
                        />
                      </span>
                      <span className="label">
                        Usage of existing {Envs.DNA_COMPANY_NAME}/Business Unit IT-Platforms
                      </span>
                    </label>
                  </div>
                  <div
                    className={
                      !this.state.portfolio.usesExistingInternalPlatforms ? Styles.hidden : Styles.itPlatformsWrapper
                    }
                  >
                    {disableExistingPlatformCheck && (
                      <p className={Styles.computeInfo}>
                        Please remove link of notebook or dataiku project to enable editing.
                      </p>
                    )}
                    {Envs.ENABLE_JUPYTER_WORKSPACE ? (
                      <div>
                        <label className="radio">
                          <span className="wrapper">
                            <input
                              type="radio"
                              className="ff-only"
                              name="compute"
                              value={ProvisionSource.NOTEBOOK}
                              checked={dnaComputeMode === ProvisionSource.NOTEBOOK}
                              onClick={this.onComputeOptionsChange}
                              disabled={disableExistingPlatformCheck}
                            />
                          </span>
                          <span className="label">Compute using DnA Jupyter Notebook</span>
                        </label>
                      </div>
                    ) : (
                      ''
                    )}

                    {Envs.ENABLE_DATAIKU_WORKSPACE ? (
                      <div>
                        <label className="radio">
                          <span className="wrapper">
                            <input
                              type="radio"
                              className="ff-only"
                              name="compute"
                              value={ProvisionSource.DATAIKU}
                              checked={dnaComputeMode === ProvisionSource.DATAIKU}
                              onClick={this.onComputeOptionsChange}
                              disabled={disableExistingPlatformCheck}
                            />
                          </span>
                          <span className="label">Compute using DnA Dataiku</span>
                        </label>
                      </div>
                    ) : (
                      ''
                    )}

                    <div>
                      <label className="radio">
                        <span className="wrapper">
                          <input
                            type="radio"
                            className="ff-only"
                            name="compute"
                            value="others"
                            checked={selectOtherPlatform}
                            onClick={this.onComputeOptionsChange}
                            disabled={disableExistingPlatformCheck}
                          />
                        </span>
                        <span className="label">Other platforms</span>
                      </label>
                    </div>
                    <div className={Styles.platformTags}>
                      <Tags
                        title={'Platform'}
                        max={100}
                        chips={platformChips}
                        setTags={this.setPlatforms}
                        tags={platforms}
                        showMissingEntryError={false}
                        fixedChips={fixedChips}
                      />
                    </div>
                  </div>
                </div>
                <div className={Styles.rightSection}>
                  <div className={Styles.rightSectionContentWrapper}>{rightSectionContent}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="btnConatiner">
          <button className="btn btn-primary" type="button" onClick={this.onPlatformSubmit}>
            Save &amp; Next
          </button>
        </div>
      </React.Fragment>
    );
  }

  protected onNoteBookCreationSuccess = (status: boolean, noteBookData: INotebookInfo) => {
    const portfolio = this.state.portfolio;
    portfolio.dnaNotebookId = noteBookData.id;

    if (!portfolio.platforms.find((tag: ITag) => tag.name === ComputeFixedTag.NOTEBOOK)) {
      portfolio.platforms.push({
        id: null,
        name: ComputeFixedTag.NOTEBOOK,
      } as ITag);
    }

    this.setState({ portfolio }, () => {
      this.savePlatformTabInfo();
    });
  };

  protected onNoteBookLinkRemove = () => {
    const portfolio = this.state.portfolio;
    portfolio.dnaNotebookId = null;
    const tagIndex = portfolio.platforms.findIndex((tag: ITag) => tag.name === ComputeFixedTag.NOTEBOOK);
    if (tagIndex != -1) {
      portfolio.platforms.splice(tagIndex, 1);
    }
    const dnaComputeMode = ProvisionSource.NOTEBOOK;
    this.setState({ portfolio, dnaComputeMode });
  };

  protected onDataikuProjectLinkSuccess = (dataikuProject: IDataiku) => {
    const portfolio = this.state.portfolio;
    portfolio.dnaDataikuProjectId = dataikuProject.projectKey;
    portfolio.dnaDataikuProjectInstance = dataikuProject.cloudProfile;

    if (!portfolio.platforms.find((tag: ITag) => tag.name === ComputeFixedTag.DATAIKU)) {
      portfolio.platforms.push({
        id: null,
        name: ComputeFixedTag.DATAIKU,
      } as ITag);
      portfolio.platforms.push({
        id: null,
        name: getDataikuInstanceTag(dataikuProject.cloudProfile)
      } as ITag);
    }

    this.setState({ portfolio }, () => {
      this.savePlatformTabInfo();
    });
  };

  protected onDataikuProjectLinkRemove = () => {
    const portfolio = this.state.portfolio;
    portfolio.dnaDataikuProjectId = null;
    portfolio.dnaDataikuProjectInstance = null;
    if (portfolio.platforms.length > 0) {
      for (let i = portfolio.platforms.length - 1; i >= 0; i--) {
        const tagIndex = portfolio.platforms.findIndex((tag: ITag) =>
          tag.name === ComputeFixedTag.DATAIKU
          || tag.name === ComputeFixedTag.DATAIKUEXTELLO
          || tag.name === ComputeFixedTag.DATAIKUONPREMISE
        );
        if (tagIndex != -1) {
          portfolio.platforms.splice(tagIndex, 1);
        }
      }
    }
    const dnaComputeMode = ProvisionSource.DATAIKU;
    this.setState({ portfolio, dnaComputeMode });
  };

  protected onComputeOptionsChange = (event: React.FormEvent<HTMLInputElement>) => {
    this.setState({ dnaComputeMode: event.currentTarget.value });
  };

  public resetChanges = () => {
    if (this.props.portfolio) {
      const portfolio = this.state.portfolio;
      portfolio.solutionOnCloud = this.props.portfolio.solutionOnCloud;
      portfolio.usesExistingInternalPlatforms = this.props.portfolio.usesExistingInternalPlatforms;
      portfolio.platforms = this.props.portfolio.platforms;
    }
  };
  protected onPlatformSubmit = () => {
    const { dnaComputeMode, portfolio } = this.state;
    if (portfolio.usesExistingInternalPlatforms) {
      if (dnaComputeMode === ProvisionSource.NOTEBOOK) {
        this.notebookInfoRef.current.triggerNoteBookCreation();
      } else if (dnaComputeMode === ProvisionSource.DATAIKU) {
        this.dataikuInfoRef.current.triggerDataikuProjectLink();
      } else {
        this.savePlatformTabInfo();
      }
    } else {
      this.savePlatformTabInfo();
    }
  };

  protected savePlatformTabInfo = () => {
    this.props.modifyPortfolio(this.state.portfolio);
    this.props.onSaveDraft('platform');
  };

  protected onChangeOfSolutionOnCloud = (e: React.ChangeEvent<HTMLInputElement>) => {
    const portfolio = this.state.portfolio;
    portfolio.solutionOnCloud = e.target.checked;
    this.setState({
      portfolio,
    });
    this.props.modifyPortfolio(portfolio);
  };
  protected onChangeOfusesExistingInternalPlatforms = (e: React.ChangeEvent<HTMLInputElement>) => {
    const portfolio = this.state.portfolio;
    portfolio.usesExistingInternalPlatforms = e.target.checked;
    if (!e.target.checked) {
      portfolio.platforms = [];
    }
    this.setState({
      portfolio,
    });
    this.props.modifyPortfolio(portfolio);
  };

  protected setPlatforms = (arr: string[]) => {
    const temp: string[] = [];
    const portfolio = this.state.portfolio;
    const platforms = this.props.platforms;
    const plats = platforms.filter((l1) => {
      if (arr.includes(l1.name)) {
        temp.push(l1.name);
        return true;
      } else {
        return false;
      }
    });
    arr.forEach((a) => {
      if (!temp.includes(a)) {
        plats.push({ id: null, name: a });
      }
    });
    portfolio.platforms = plats;
    this.props.modifyPortfolio(this.state.portfolio);
  };
}
