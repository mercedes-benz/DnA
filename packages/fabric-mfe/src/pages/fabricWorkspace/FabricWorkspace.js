import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useParams, useHistory } from 'react-router-dom';
import Styles from './fabric-workspace.scss';
import Caption from 'dna-container/Caption';
import Modal from 'dna-container/Modal';
// utils
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import { fabricApi } from '../../apis/fabric.api';
import Spinner from '../../components/spinner/Spinner';
import RoleCreationModal from '../../components/roleCreationModal/RoleCreationModal';
import Lakehouses from '../../components/Lakehouses/Lakehouses';

const WorkspaceDetails = ({ workspace }) => {
  return (
    <>
      <h3>Workspace Details</h3>
      <div className={Styles.content}>
        <div className={Styles.firstPanel}>
          <div className={Styles.formWrapper}>
            <div className={classNames(Styles.flex)}>
              <div className={Styles.col3}>
                <p className={Styles.label}>Workspace Name</p> {workspace?.name || 'null'}
              </div>
              <div className={Styles.col3}>
                <p className={Styles.label}>Created on</p>
                {workspace?.createdOn !== undefined && regionalDateAndTimeConversionSolution(workspace?.createdOn)}
              </div>
              <div className={Styles.col3}>
                <p className={Styles.label}>Created by</p>
                {workspace?.createdBy?.firstName} {workspace?.createdBy?.lastName}
              </div>

              <div className={Styles.col3}>
                <p className={Styles.label}>Type of Project</p>
                {workspace?.typeOfProject ? workspace?.typeOfProject : 'N/A'}
              </div>
              <div className={Styles.col2}>
                <p className={Styles.label}>Description</p>
                {workspace?.description ? workspace?.description : 'N/A'}
              </div>

              <div className={Styles.col3}>
                <p className={Styles.label}>Cost Center</p>
                {workspace?.costCenter ? workspace?.costCenter : 'N/A'}
              </div>
              <div className={Styles.col3}>
                <p className={Styles.label}>Internal Order</p>
                {workspace?.internalOrder ? workspace?.internalOrder : 'N/A'}
              </div>
              <div className={Styles.col3}>
                <p className={Styles.label}>Related Solutions</p>
                {workspace?.relatedSolutions.length > 0 ? workspace.relatedSolutions?.map((chip) =>
                  <><label className="chips">{chip.name}</label>&nbsp;&nbsp;</>
                ) : 'N/A'}
              </div>

              <div className={Styles.col3}>
                <p className={Styles.label}>Related Reports</p>
                {workspace?.relatedReports.length > 0 ? workspace.relatedReports?.map((chip) => 
                    <><label className="chips">{chip.name}</label>&nbsp;&nbsp;</>
                 ) : 'N/A'}
              </div>
              <div className={Styles.col3}>
                <p className={Styles.label}>Division</p>
                {workspace?.division === '0' || !workspace?.division ? 'N/A' : workspace?.division}
              </div>
              <div className={Styles.col3}>
                <p className={Styles.label}>Sub Division</p>
                {workspace?.subDivision === '0' || !workspace?.subDivision ? 'N/A' : workspace?.subDivision}
              </div>

              <div className={Styles.col3}>
                <p className={Styles.label}>Department</p>
                {workspace?.department ? workspace?.department : 'N/A'}
              </div>
              <div className={Styles.col3}>
                <p className={Styles.label}>Tags</p>
                {workspace?.tags?.length > 0 ? workspace.tags?.map((chip) =>
                    <><label className="chips">{chip}</label>&nbsp;&nbsp;</>
                  ) : 'N/A'}
              </div>
              <div className={Styles.col3}>
                <p className={Styles.label}>Data Classification</p>
                {workspace?.dataClassification === '0' || !workspace?.dataClassification ? 'N/A' : workspace?.dataClassification}
              </div>

              <div className={Styles.col3}>
                <p className={Styles.label}>PII</p>
                {workspace?.hasPii === true ? 'Yes' : 'No'}
              </div>
              <div className={Styles.col3}>
                <p className={Styles.label}>Archer ID</p>
                {workspace?.archerId ? workspace?.archerId : 'N/A'}
              </div>
              <div className={Styles.col3}>
                <p className={Styles.label}>Procedure ID</p>
                {workspace?.procedureId ? workspace?.procedureId : 'N/A'}
              </div>
            
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

const FabricWorkspace = ({ user }) => {
  const { id: workspaceId } = useParams();
  const history = useHistory();

  const [loading, setLoading] = useState(true);
  const [workspace, setWorkspace] = useState();
  const [showStatusModal, setShowStatusModal] = useState(false);

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

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
          if(e?.response?.status === 403) {
            Notification.show('Unauthorized to view this page or not found', 'alert');
            history.push(`/`);
          } else {
            Notification.show(
              e.response.data.errors?.length
                ? e.response.data.errors[0].message
                : 'Fetching fabric workspace failed!',
              'alert',
            );
          }
        });
  };

  return (
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          {!loading && 
            <Caption title={`Fabric Workspace - ${workspace?.name || 'null'}`}>
              <div>
                <button className={classNames('btn btn-primary', Styles.refreshBtn)} tooltip-data="Refresh" onClick={getWorkspace}>
                  <i className="icon mbc-icon refresh"></i>
                </button>
              </div>
            </Caption>    
          }
          <div className={Styles.statusBtns}>
            {workspace?.status?.state === 'IN_PROGRESS' &&
              <button className={classNames('btn btn-secondary', Styles.createNewCard)} onClick={() => setShowStatusModal(true)}>
                <p className={Styles.addlabel}><Spinner /> <span>Provisioning in progress</span></p>
              </button>
            }
            {workspace?.status?.state === 'COMPLETED' &&
              <button className={classNames('btn btn-secondary', Styles.createNewCard)} onClick={() => setShowStatusModal(true)}>
                <p className={Styles.addlabel}><i className="icon mbc-icon check circle" /> <span>Provisioned</span></p>
              </button>
            }
            <button className={classNames('btn btn-secondary', Styles.createNewCard)} type="button" onClick={() => window.open(`https://app.fabric.microsoft.com/groups/${workspace?.id}`)}>
              <p className={Styles.addlabel}>Access Workspace <i className="icon mbc-icon new-tab" /></p>
            </button>
          </div>
          <Lakehouses 
            user={user} 
            workspace={workspace} 
            lakehouses={workspace?.lakehouses ? workspace?.lakehouses : []} 
            onDeleteLakehouse={getWorkspace} 
          />
          <WorkspaceDetails workspace={workspace} />
        </div>
      </div>
      { showStatusModal &&
        <Modal
          title={'Role Creation Status'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'80%'}
          buttonAlignment="right"
          show={showStatusModal}
          content={<RoleCreationModal workspace={workspace} onClose={() => setShowStatusModal(false)} />}
          scrollableContent={true}
          onCancel={() => setShowStatusModal(false)}
        />
      }
    </React.Fragment>
  );
}
export default FabricWorkspace;
