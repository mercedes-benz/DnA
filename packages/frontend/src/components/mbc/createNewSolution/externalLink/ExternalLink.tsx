import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { ILink } from 'globals/types';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import Styles from './ExternalLink.scss';

const classNames = cn.bind(Styles);

export interface IExternalLinkProps {
  infoText?: string;
  links: ILink[];
  modifyLinks: (links: ILink[]) => void;
}

export interface IExternalLinkState {
  links: ILink[];
  showDeleteModal: boolean;
  indexToDelete: null | number;
  isLinkEmpty: boolean;
}

export interface IExternalLinkResponse {
  error?: any;
  linkDetails: ILink;
}

export default class ExternalLink extends React.Component<IExternalLinkProps, IExternalLinkState> {
  public static getDerivedStateFromProps(props: IExternalLinkProps, state: IExternalLinkState) {
    if (props && props.links) {
      return {
        links: props.links,
      };
    }
    return null;
  }
  protected linkField = React.createRef<HTMLInputElement>();
  constructor(props: IExternalLinkProps) {
    super(props);
    this.state = {
      links: [],
      showDeleteModal: false,
      indexToDelete: null,
      isLinkEmpty: false,
    };
  }

  public render() {
    const infoText = this.props.infoText;
    const requiredError = '*Missing entry';
    return (
      <div className={classNames(Styles.wrapper)}>
        <div className={classNames(Styles.firstPanel)}>
          <h3>Add Links</h3>
          <div className={Styles.linksWrapper}>
            <div className={Styles.flexLayout}>
              <div>
                {infoText ? <p className="info-message">{infoText}</p> : ''}
                <div className={classNames('input-field-group', this.state.isLinkEmpty ? 'error' : '')}>
                  <label id="gitrepoLabel" htmlFor="gitrepoInput" className="input-label">
                    Link
                  </label>
                  <div className={classNames('inputWrapper')}>
                    <input
                      type="text"
                      className="input-field"
                      required={this.state.isLinkEmpty}
                      id="gitrepoInput"
                      maxLength={200}
                      placeholder="Type here"
                      autoComplete="off"
                      onKeyDown={this.addLink}
                      ref={this.linkField}
                    />
                    <div className={Styles.linkButton}>
                      <button className="btn btn-primary" type="button" onClick={this.addLinkOnButtonClick}>
                        Add
                      </button>
                    </div>
                  </div>
                  {this.state.isLinkEmpty ? (
                    <span className={classNames('error-message-local')}>{requiredError}</span>
                  ) : (
                    ''
                  )}
                </div>
              </div>
              <div className={Styles.linkContainer}>
                {this.state.links.map((link: ILink, index: number) => {
                  const url = link.link;
                  const externalLink = url.toLocaleLowerCase().startsWith('http') ? url : 'http://' + url;
                  return (
                    <div key={index} className={Styles.linkWrapper}>
                      <i className={classNames(Styles.linkIcon, 'icon mbc-icon link')} />
                      <a className={Styles.fileNameText} target="_blank" href={externalLink} rel="noreferrer">
                        {link.link}
                      </a>
                      <i
                        onClick={this.openDeleteModal(link, index)}
                        className={classNames(Styles.deleteIcon, 'icon delete')}
                      />
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        </div>
        <ConfirmModal
          title={''}
          showAcceptButton={true}
          showCancelButton={false}
          acceptButtonTitle={'Confirm'}
          show={this.state.showDeleteModal}
          removalConfirmation={true}
          content={
            <div style={{ margin: '35px 0', textAlign: 'center' }}>
              <div>Remove Link</div>
              <div className={classNames(Styles.removeConfirmationContent, 'hide')}>
                Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt.
              </div>
            </div>
          }
          onCancel={this.onInfoModalCancel}
          onAccept={this.onAccept}
        />
      </div>
    );
  }

  protected addLink = (e: any) => {
    if (e.key === 'Enter') {
      const tempLink: ILink = { link: '', label: '', description: '' };
      const link = this.linkField.current.value;
      if (!link) {
        this.setState({ isLinkEmpty: true });
      } else {
        this.setState({ isLinkEmpty: false });
        tempLink.link = link;
        tempLink.label = link;
        tempLink.description = link;
        const links = this.state.links;
        links.push(tempLink);
        this.setState({ links }, () => {
          this.props.modifyLinks(this.state.links);
          this.linkField.current.value = '';
        });
      }
    }
  };

  protected addLinkOnButtonClick = (e: any) => {
    const tempLink: ILink = { link: '', label: '', description: '' };
    const link = this.linkField.current.value;
    if (!link) {
      this.setState({ isLinkEmpty: true });
    } else {
      this.setState({ isLinkEmpty: false });
      tempLink.link = link;
      tempLink.label = link;
      tempLink.description = link;
      const links = this.state.links;
      links.push(tempLink);
      this.setState({ links }, () => {
        this.props.modifyLinks(this.state.links);
        this.linkField.current.value = '';
      });
    }
  };

  protected deleteLink = (index: number) => {
    const links = this.state.links;
    links.splice(index, 1);
    this.setState({ links, showDeleteModal: false });
  };

  protected openDeleteModal = (link: ILink, index: number) => {
    return () => {
      this.setState({ showDeleteModal: true, indexToDelete: index });
    };
  };

  protected onAccept = () => {
    this.deleteLink(this.state.indexToDelete);
  };

  protected onInfoModalCancel = () => {
    this.setState({ showDeleteModal: false });
  };
}
