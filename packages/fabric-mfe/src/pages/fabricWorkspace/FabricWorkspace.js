import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import Styles from './fabric-workspace.scss';
import Caption from 'dna-container/Caption';
// utils
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { fabricApi } from '../../apis/fabric.api';

const FabricWorkspace = () => {
  const { id: workspaceId } = useParams();

  const [loading, setLoading] = useState(true);
  const [workspace, setWorkspace] = useState();

  useEffect(() => {
    getWorkspace();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getWorkspace = () => {      
      ProgressIndicator.show();
      fabricApi
        .getFabricWorkspace(workspaceId)
        .then((res) => {
          setWorkspace(res?.data);
          ProgressIndicator.hide();
          setLoading(false);
        })
        .catch((e) => {
          ProgressIndicator.hide();
          setLoading(false);
          Notification.show(
            e.response.data.errors?.length
              ? e.response.data.errors[0].message
              : 'Fetching fabric workspace failed!',
            'alert',
          );
        });
  };

  return (
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          <Caption title={!loading ? workspace?.name : ''}>

          </Caption>
          <div className={Styles.content}>
            <h3 id="productName">Workspace Details</h3>
            <div className={Styles.firstPanel}>
              <div className={Styles.formWrapper}>
                {!loading &&
                  <>
                    <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                      <div id="productDescription">
                        <label className="input-label summary">Workspace Name</label>
                        <br />
                        {workspace?.name}
                      </div>
                      <div id="tags">
                        <label className="input-label summary">Created on</label>
                        <br />
                        {workspace?.createdOn !== undefined && regionalDateAndTimeConversionSolution(workspace?.createdOn)}
                      </div>
                      <div id="isExistingSolution">
                        <label className="input-label summary">Created by</label>
                        <br />
                        {workspace?.createdBy?.firstName} {workspace?.createdBy?.lastName}
                      </div>
                    </div>

                    <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                      <div id="typeOfProjectOption">
                        <label className="input-label summary">Type of Project</label>
                        <br />
                        {workspace?.typeOfProject ? workspace?.typeOfProject : 'N/A'}
                      </div>
                      <div id="description">
                        <label className="input-label summary">Description</label>
                        <br />
                        {workspace?.decription ? workspace?.decription : 'N/A'}
                      </div>
                      <div id="divisionField">
                      </div>
                    </div>

                    <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                      <div id="typeOfProjectOption">
                        <label className="input-label summary">Cost Center</label>
                        <br />
                        {workspace?.costCenter === '0' || !workspace?.costCenter ? 'N/A' : workspace?.costCenter}
                      </div>
                      <div id="description">
                        <label className="input-label summary">Internal Order</label>
                        <br />
                        {workspace?.internalOrder ? workspace?.internalOrder : 'N/A'}
                      </div>
                      <div id="tags">
                        <label className="input-label summary">Related Solutions</label>
                        <br />
                        {workspace?.relatedSolutions ? workspace.relatedSolutions?.map((chip) => {
                          return (
                            <>
                              <label className="chips">{chip}</label>&nbsp;&nbsp;
                            </>
                          );
                        }) : 'N/A'}
                      </div>
                    </div>

                    <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                      <div id="tags">
                        <label className="input-label summary">Related Reports</label>
                        <br />
                        {workspace?.relatedReports ? workspace.relatedReports?.map((chip) => {
                          return (
                            <>
                              <label className="chips">{chip}</label>&nbsp;&nbsp;
                            </>
                          );
                        }) : 'N/A'}
                      </div>
                      <div id="divisionField">
                        <label className="input-label summary">Division</label>
                        <br />
                        {workspace?.division === '0' || !workspace?.division ? 'N/A' : workspace?.division}
                      </div>
                      <div id="subDivisonField">
                        <label className="input-label summary">Sub Division</label>
                        <br />
                        {workspace?.subDivision === '0' || !workspace?.subDivision ? 'N/A' : workspace?.subDivision}
                      </div>
                    </div>

                    <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                      <div id="department">
                        <label className="input-label summary">Department</label>
                        <br />
                        {workspace?.department ? workspace?.department : 'N/A'}
                      </div>
                      <div id="tags">
                        <label className="input-label summary">Tags</label>
                        <br />
                        {workspace?.tags ? workspace.tags?.map((chip) => {
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
                        {workspace?.dataClassification === '0' || !workspace?.dataClassification ? 'N/A' : workspace?.dataClassification}
                      </div>
                    </div>

                    <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                      <div id="PiiData">
                        <label className="input-label summary">PII</label>
                        <br />
                        {workspace?.piiData === true ? 'Yes' : 'No'}
                      </div>
                      <div id="archerId">
                        <label className="input-label summary">Archer ID</label>
                        <br />
                        {workspace?.archerId ? workspace?.archerId : 'N/A'}
                      </div>
                      <div id="procedureId">
                        <label className="input-label summary">Procedure ID</label>
                        <br />
                        {workspace?.procedureId ? workspace?.procedureId : 'N/A'}
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
export default FabricWorkspace;
