import classNames from 'classnames';
import React, { useEffect } from 'react';
import Tabs from '../../common/modules/uilab/js/src/tabs';
import Notification from '../../common/modules/uilab/js/src/notification';
import Styles from './connection-modal.scss';
import { Envs } from '../../utilities/envs';

const ConnectionModal = ({ dbservice, onOk }) => {
  useEffect(() => {
    Tabs.defaultSetup();
    // reset
    // ensure only single tab indicator is shown
    // on close , reset active tab to tab-1
    const activeTabIndicator = document.querySelectorAll('.active-indicator');

    if (activeTabIndicator?.length > 1) {
      activeTabIndicator?.[0]?.remove();
    }
  }, []);

  const copyToClipboard = (content) => {
    navigator.clipboard.writeText('');
    navigator.clipboard.writeText(content).then(() => Notification.show('Copied to Clipboard'));
  };

  const isDataikuEnabled = Envs.ENABLE_DATAIKU;

  const handleOk = () => {
    onOk();
  }

  // const copyToClipboard = (content) => {
  //   navigator.clipboard.writeText(content).then(() => Notification.show('Copied to Clipboard'));
  // };
  console.log(dbservice);
  return (
    <div className={Styles.dbServiceConnection}>
      <header>
        <h3>DB Connection Details</h3>
      </header>
      <section className={Styles.connectionSection}>
        <div className={Styles.item}>
          <div className={Styles.itemKey}>DB Host</div>
          <div className={Styles.itemValue}>123.223.23.3</div>
        </div>
        <div className={Styles.item}>
          <div className={Styles.itemKey}>Port</div>
          <div className={Styles.itemValue}>8990</div>
        </div>
        <div className={Styles.item}>
          <div className={Styles.itemKey}>Connection String</div>
          <div className={Styles.itemValue}>heres the connection string</div>
        </div>
      </section>
      <div>
        {dbservice?.collaborators?.length > 0 && (
          <>
            <div className={Styles.colHeader}>
                <div className={Styles.column1}>User ID</div>
                <div className={Styles.column2}>Name</div>
                <div className={Styles.column3}>Permissions</div>
            </div>
            <div>
              {dbservice?.collaborators?.length === 0 &&
                <div className={Styles.noLincense}>
                  <p>No Users Selected</p>
                </div>
              }
              {dbservice?.collaborators?.map((userLicense) => {
                return (
                    <div key={userLicense?.userDetails?.id} className={Styles.userRow}>
                        <div className={Styles.column1}>
                          <p>{userLicense?.userDetails?.id}</p>
                        </div>
                        <div className={Styles.column2}>
                          <p>{userLicense?.userDetails?.firstName + ' ' + userLicense?.userDetails?.lastName}</p>
                        </div>
                        <div className={classNames(Styles.column3, Styles.lincenseContainer)}>
                          <div className={classNames('input-field-group include-error ', Styles.inputGrp)}>
                            <label className={classNames('checkbox', Styles.checkBoxDisable)}>
                              <span className="wrapper">
                                <input
                                  type="checkbox"
                                  className="ff-only"
                                  value="read"
                                  checked={true}
                                  readOnly
                                />
                              </span>
                              <span className="label">Read</span>
                            </label>
                          </div>
                          &nbsp;&nbsp;&nbsp;
                          <div className={classNames('input-field-group include-error ', Styles.inputGrp, Styles.checkBoxDisable)}>
                            <label className={'checkbox'}>
                              <span className="wrapper">
                                <input
                                  type="checkbox"
                                  className="ff-only"
                                  value="write"
                                  checked={userLicense?.permission !== null ? userLicense?.permission?.write : false}
                                />
                              </span>
                              <span className="label">Write</span>
                            </label>
                          </div>
                        </div>
                    </div>
                  );
              })}
            </div>
          </>
        )}
      </div>
      <section>
        <div className={'tabs-panel'}>
          <div className="tabs-wrapper">
            <nav>
              <ul className="tabs">
                <li className={'tab active'}>
                  <a href="#tab-content-1" id="jupyterNotebook">
                    <strong>How to Connect with Python</strong>
                  </a>
                </li>
                <li className={'tab'}>
                  <a href="#tab-content-2" id="jupyterNotebook">
                    <strong>How to Connect with SpringBoot</strong>
                  </a>
                </li>
                <li className={'tab'}>
                  <a href="#tab-content-3" id="jupyterNotebook">
                    <strong>How to Connect from Fabric</strong>
                  </a>
                </li>
                <li className={`tab ${!isDataikuEnabled ? 'disable' : ''}`}>
                  <a href="#tab-content-2" id="dataiku">
                    <strong style={{ ...(!isDataikuEnabled && { color: '#99a5b3' }) }}>
                      {`Connect to Dataiku project(s)${!isDataikuEnabled ? ' ( Coming soon )' : ''}`}
                    </strong>
                  </a>
                </li>
              </ul>
            </nav>
          </div>
          <div className={classNames('tabs-content-wrapper', Styles.tabsContentWrapper)}>
            <div id="tab-content-1" className={classNames('tab-content mbc-scroll', Styles.tabContentContainer)}>
              <span
                className={Styles.copyIcon}
                onClick={() => {
                  const content = document.getElementById('tab-content-1')?.innerText;
                  copyToClipboard(content);
                }}
              >
                <i className="icon mbc-icon copy" />
              </span>
              <div className={Styles.connectionCode}>
                <h6>Install psycopg2 (PostgreSQL adapter)</h6>
                  <pre>
                    pip install psycopg2-binary
                  </pre>
                <h6>Import and Connect</h6>
                  <pre>
                  import psycopg2<br /><br />

                  conn = psycopg2.connect(<br />
                      host=&quot;your_host&quot;,       # e.g., &quot;localhost&quot; or cloud endpoint<br />
                      port=&quot;5432&quot;,<br />
                      database=&quot;your_db&quot;,<br />
                      user=&quot;your_username&quot;,<br />
                      password=&quot;your_password&quot;<br />
                  )
                  </pre>
                <h6>Create Cursor and Execute Query</h6>
                  <pre>
                  cur = conn.cursor()<br />
                  cur.execute(&quot;SELECT version();&quot;)<br />
                  print(cur.fetchone())
                  </pre>
              </div>
            </div>
            <div id="tab-content-2" className={classNames('tab-content mbc-scroll', Styles.tabContentContainer)}>
              <span
                className={Styles.copyIcon}
                onClick={() => {
                  const content = document.getElementById('tab-content-2')?.innerText;
                  copyToClipboard(content);
                }}
              >
                <i className="icon mbc-icon copy" />
              </span>
              <div className={Styles.connectionCode}>
                <h6>Add PostgreSQL Dependency (in pom.xml)</h6>
                  <pre>
{`<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <version>42.7.1</version> <!-- or latest -->
</dependency>`}
                  </pre>
                <h6>Configure application.properties or application.yml</h6>
                  <pre>
{`spring.datasource.url=jdbc:postgresql://localhost:5432/your_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true`}
                  </pre>
              </div>
            </div>
            <div id="tab-content-3" className={classNames('tab-content mbc-scroll', Styles.tabContentContainer)}>
              <span
                className={Styles.copyIcon}
                onClick={() => {
                  const content = document.getElementById('tab-content-3')?.innerText;
                  copyToClipboard(content);
                }}
              >
                <i className="icon mbc-icon copy" />
              </span>
              <div className={Styles.connectionCode}>
                <h6>Connect using OneLake</h6>
                <pre>
                <ol>
<li>
<p>In <strong>Fabric → Data Factory</strong>, create a new <strong>Pipeline</strong>.</p>
</li>
<li>
<p>Add a <strong>Copy Data activity</strong>.</p>
</li>
<li>
<p>Configure:</p>
<ul>
<li>
<p><strong>Source</strong>: PostgreSQL</p>
</li>
<li>
<p><strong>Sink</strong>: OneLake (Lakehouse or Parquet/Delta format)</p>
</li>
</ul>
</li>
<li>
<p>Schedule or trigger as needed.</p>
</li>
</ol>
</pre>
              </div>
            </div>
            <div id="tab-content-2" className={classNames('tab-content mbc-scroll', Styles.tabContentContainer)}>
              <div className={classNames(Styles.connectionCode)}>
                {/* {isLoading ? (
                  <div className={classNames('text-center', Styles.spinner)}>
                    <div className="progress infinite" />
                  </div>
                ) : (
                  connectToDataiku
                )} */}
              </div>
            </div>
          </div>
        </div>
      </section>
      <footer>
        <button className={classNames('btn btn-tertiary')} onClick={handleOk}>
          OK
        </button>
      </footer>
    </div>
  );
};

export default ConnectionModal;
