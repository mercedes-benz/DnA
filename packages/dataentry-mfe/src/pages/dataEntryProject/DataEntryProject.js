import classNames from 'classnames';
import React, { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import Styles from './data-entry-project.scss';
import Caption from 'dna-container/Caption';
// utils
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import DataEntrySheet from '../../components/dataEntrySheet/DataEntrySheet';
import { dataEntryApi } from '../../apis/dataentry.api';
import { DEFAULT_WORKBOOK_DATA } from '../../utilities/template';

const DataEntryProject = () => {
  const { id: projectId } = useParams();
  const univerRef = useRef();
  const [data] = useState(DEFAULT_WORKBOOK_DATA);

  const [loading, setLoading] = useState(true);
  const [project, setProject] = useState();

  useEffect(() => {
    getProject();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getProject = () => {      
      ProgressIndicator.show();
      dataEntryApi
        .getDataEntryProject(projectId)
        .then((res) => {
          setProject(res?.data);
          ProgressIndicator.hide();
          setLoading(false);
        })
        .catch((e) => {
          ProgressIndicator.hide();
          setLoading(false);
          Notification.show(
            e.response.data.errors?.length
              ? e.response.data.errors[0].message
              : 'Fetching data entry project failed!',
            'alert',
          );
        });
  };

  return (
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          {!loading ? 
            <Caption title={project?.name} /> : null
          }
          {/* <div onClick={toggleFullScreenMode}>
            <FullScreenModeIcon fsNeed={fullScreenMode} />
          </div> */}
          <div>
            <div>
              <DataEntrySheet style={{ flex: 1 }} ref={univerRef} data={data} />
            </div>
            <div className={Styles.btnContainer}>
              <button
                className={'btn btn-primary'}
                onClick={() => {
                  console.log(univerRef.current?.getData());
                }}
              >
                Save as Draft
              </button>
              <button
                className={'btn btn-tertiary'}
                onClick={() => {
                  console.log(univerRef.current?.getData());
                }}
              >
                Send to Fillers
              </button>
            </div>
          </div>
          <div className={Styles.content}>
            <h3 id="productName">Project Details</h3>
            <div className={Styles.firstPanel}>
              <div className={Styles.formWrapper}>
                {!loading &&
                  <>
                    <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                      <div id="productDescription">
                        <label className="input-label summary">Project Name</label>
                        <br />
                        {project?.name}
                      </div>
                      <div id="tags">
                        <label className="input-label summary">Created on</label>
                        <br />
                        {project?.createdOn !== undefined && regionalDateAndTimeConversionSolution(project?.createdOn)}
                      </div>
                      <div id="isExistingSolution">
                        <label className="input-label summary">Created by</label>
                        <br />
                        {project?.createdBy?.firstName} {project?.createdBy?.lastName}
                      </div>
                    </div>

                    <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                      <div id="typeOfProjectOption">
                        <label className="input-label summary">Type of Project</label>
                        <br />
                        {project?.typeOfProject ? project?.typeOfProject : 'N/A'}
                      </div>
                      <div id="description">
                        <label className="input-label summary">Description</label>
                        <br />
                        {project?.decription ? project?.decription : 'N/A'}
                      </div>
                      <div id="divisionField">
                      </div>
                    </div>

                    <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                      <div id="divisionField">
                        <label className="input-label summary">Division</label>
                        <br />
                        {project?.division === '0' || !project?.division ? 'N/A' : project?.division}
                      </div>
                      <div id="subDivisonField">
                        <label className="input-label summary">Sub Division</label>
                        <br />
                        {project?.subDivision === '0' || !project?.subDivision ? 'N/A' : project?.subDivision}
                      </div>
                    </div>

                    <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                      <div id="department">
                        <label className="input-label summary">Department</label>
                        <br />
                        {project?.department ? project?.department : 'N/A'}
                      </div>
                      <div id="tags">
                        <label className="input-label summary">Tags</label>
                        <br />
                        {project?.tags ? project.tags?.map((chip) => {
                          return (
                            <>
                              <label className="chips">{chip}</label>&nbsp;&nbsp;
                            </>
                          );
                        }) : 'N/A'}
                      </div>
                      <div id="dataClassificationField">
                        <label className="input-label summary">Data Classification</label>
                        <br />
                        {project?.dataClassification === '0' || !project?.dataClassification ? 'N/A' : project?.dataClassification}
                      </div>
                    </div>

                    <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                      <div id="PiiData">
                        <label className="input-label summary">PII</label>
                        <br />
                        {project?.piiData === true ? 'Yes' : 'No'}
                      </div>
                      <div id="archerId">
                        <label className="input-label summary">Archer ID</label>
                        <br />
                        {project?.archerId ? project?.archerId : 'N/A'}
                      </div>
                      <div id="procedureId">
                        <label className="input-label summary">Procedure ID</label>
                        <br />
                        {project?.procedureId ? project?.procedureId : 'N/A'}
                      </div>
                    </div>
                  </>
                }
              </div>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  );
}
export default DataEntryProject;
