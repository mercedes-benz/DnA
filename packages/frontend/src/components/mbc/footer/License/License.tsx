import cn from 'classnames';
import * as React from 'react';
import { history } from '../../../../router/History';
import Styles from './License.scss';
import { IUserInfo } from 'globals/types';
import ModuleUsage from '../License/moduleUsage/ModuleUsage';

// @ts-ignore
const classNames = cn.bind(Styles);

export default class License extends React.Component<{ user: IUserInfo }, any> {
  public render() {
    return (
      <React.Fragment>
        <div className={classNames(Styles.mainPanel)}>
          <button className="btn btn-text back arrow" type="submit" onClick={this.goback}>
            Back
          </button>
          <div className={Styles.wrapper}>
            <h3> Copyright and license information</h3>
            <div className={classNames(Styles.content)}>
              <p>
                MIT License
                <br />
                <br />
                Copyright (c) 2019 Daimler TSS GmbH
                <br />
                <br />
                Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
                associated documentation files (the "Software"), to deal in the Software without restriction, including
                without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
                copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
                following conditions:
                <br />
                <br />
                The above copyright notice and this permission notice shall be included in all copies or substantial
                portions of the Software.
                <br />
                <br />
                THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
                LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
                NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
                WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
                SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
              </p>
            </div>
          </div>
          <div className={Styles.wrapperPaddingZero}>
            <h5> Module Usage</h5>
            <div className={classNames(Styles.content)}>
              <ModuleUsage />
            </div>
          </div>
        </div>
      </React.Fragment>
    );
  }

  protected goback = () => {
    history.goBack();
  };
}
