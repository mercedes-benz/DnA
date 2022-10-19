import cn from 'classnames';
import * as React from 'react';
import { IResult, ISharing } from 'globals/types';
import Styles from './Sharing.scss';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import TextBox from 'components/mbc/shared/textBox/TextBox';
const classNames = cn.bind(Styles);

export interface ISharingProps {
  onSaveDraft: (tabToBeSaved: string) => void;
  sharing: ISharing;
  modifySharing: (analytics: ISharing) => void;
  results: IResult[];
}

export interface ISharingState {
  sharing: ISharing;
}

export default class Sharing extends React.Component<ISharingProps, ISharingState> {
  public static getDerivedStateFromProps(props: ISharingProps, state: ISharingState) {
    return {
      sharing: props.sharing,
    };
  }
  constructor(props: ISharingProps) {
    super(props);
    this.state = {
      sharing: this.props.sharing
        ? this.props.sharing
        : {
            gitUrl: null,
            result: {
              id: null,
              name: null,
            },
            resultUrl: null,
          },
    };
  }

  public componentDidMount() {
    SelectBox.defaultSetup();
  }

  public render() {
    return (
      <React.Fragment>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Sharing</h3>
            <div className={classNames(Styles.formWrapper)}>
              <div className={Styles.flexLayout}>
                <div>
                  <div>
                    <TextBox
                      type="text"
                      controlId={'gitrepoInput'}
                      labelId={'gitrepoLabel'}
                      label={'Git Repository'}
                      placeholder={'Type here'}
                      value={this.state.sharing.gitUrl}
                      required={false}
                      maxLength={200}
                      onChange={this.onGitUrl}
                    />
                  </div>
                  <div>
                    <TextBox
                      type="text"
                      controlId={'resultsurl'}
                      labelId={'resultsurllabel'}
                      label={'Comment'}
                      placeholder={'Type here'}
                      value={this.state.sharing.resultUrl}
                      required={false}
                      maxLength={200}
                      onChange={this.onResultUrlChange}
                    />
                  </div>
                </div>
                <div>
                  <div id="resultContainer" className={classNames('input-field-group')}>
                    <label id="resultlabel" className="input-label" htmlFor="resultSelect">
                      Results
                    </label>
                    <div id="division" className="custom-select">
                      <select id="resultSelect" onChange={this.onResultChange} value={this.state.sharing.result.id}>
                        <option id="resultDefault" value={0}>
                          Choose
                        </option>
                        {this.props.results.map((obj) => (
                          <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                            {obj.name}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="btnConatiner">
          <button className="btn btn-primary" type="button" onClick={this.onSharingSubmit}>
            Save & Next
          </button>
        </div>
      </React.Fragment>
    );
  }
  public resetChanges = () => {
    if (this.props.sharing) {
      const sharing = this.state.sharing;
      sharing.gitUrl = this.props.sharing.gitUrl;
      sharing.result = this.props.sharing.result;
      sharing.resultUrl = this.props.sharing.resultUrl;
    }
  };
  protected onSharingSubmit = () => {
    this.props.modifySharing(this.state.sharing);
    this.props.onSaveDraft('sharing');
  };

  protected onGitUrl = (e: React.FormEvent<HTMLInputElement>) => {
    const gitUrl = e.currentTarget.value;
    const sharing = this.state.sharing;
    sharing.gitUrl = gitUrl;
    this.setState({
      sharing,
    });
    this.props.modifySharing(sharing);
  };
  protected onResultChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const result: IResult = { id: null, name: null };
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        result.id = option.value;
        result.name = option.label;
      });
    }
    const sharing = this.state.sharing;
    sharing.result = result;
    this.setState({ sharing });
    this.props.modifySharing(sharing);
  };

  protected onResultUrlChange = (e: React.FormEvent<HTMLInputElement>) => {
    const resultUrl = e.currentTarget.value;
    const sharing = this.state.sharing;
    sharing.resultUrl = resultUrl;
    this.setState({
      sharing,
    });
    this.props.modifySharing(sharing);
  };
}
