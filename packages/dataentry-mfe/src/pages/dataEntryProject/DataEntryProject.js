import classNames from 'classnames';
import React, { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import Styles from './data-entry-project.scss';
// Container Components
import Modal from 'dna-container/Modal';
import Caption from 'dna-container/Caption';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import DataEntrySheet from '../../components/dataEntrySheet/DataEntrySheet';
import { dataEntryApi } from '../../apis/dataentry.api';
import { DEFAULT_WORKBOOK_DATA } from '../../utilities/template';
import ProjectDetails from '../../components/projectDetails/ProjectDetails';
import DeUsersInformation from '../../components/deUsersInformation/DeUsersInformation';
import DataEntryUsers from '../../components/dataEntryUsers/DataEntryUsers';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';

const DataEntryProject = ({ user }) => {
  const { id: projectId } = useParams();
  const univerRef = useRef();
  const [data, setData] = useState(DEFAULT_WORKBOOK_DATA);

  const [loading, setLoading] = useState(true);
  const [project, setProject] = useState();

  const [showDeUsersModal, setShowDeUsersModal] = useState(false);
  const [showDeUsersInformationModal, setShowDeUsersInformationModal] = useState(false);
  const [showProjectDetailsModal, setShowProjectDetailsModal] = useState(false);

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
          if(res?.data?.surveyData !== null) {
            const workbook = {...DEFAULT_WORKBOOK_DATA};
            workbook.sheets['sheet-01'].cellData = res?.data?.surveyData;
            setData({...workbook});
          }
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

  const handleUpdate = () => {
    ProgressIndicator.show(); 
    const surveyDataTemp = univerRef.current?.getData();
    const data = {
      dataLakeDetails: {
        id: '',
        type: 'DnA',
        name: '',
      },
      fillingInstructions: '',
      dueDate: null,
      dataEntryUsers: [],
      surveyData: surveyDataTemp.sheets['sheet-01'].cellData,
    }
    dataEntryApi.updateDataEntryProject(projectId, data).then(() => {
      ProgressIndicator.hide();
      Notification.show('Data Entry Project draft saved successfully');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || error?.response?.data?.responses?.errors?.[0]?.message || 'Error while creating data entry project',
        'alert',
      );
    });
  };

  const handlePublish = () => {
    ProgressIndicator.show(); 
    const surveyDataTemp = univerRef.current?.getData();
    const data = {
      dataLakeDetails: {
        id: project?.dataLakeDetails?.id,
        type: project?.dataLakeDetails?.type,
        name: project?.dataLakeDetails?.name,
      },
      fillingInstructions: project?.fillingInstructions,
      dueDate: project?.dueDate,
      dataEntryUsers: project?.dataEntryUsers,
      surveyData: surveyDataTemp.sheets['sheet-01'].cellData,
    }
    dataEntryApi.publishDataEntryProject(projectId, data).then(() => {
      ProgressIndicator.hide();
      Notification.show('Data Entry Project successfully published');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || error?.response?.data?.responses?.errors?.[0]?.message || 'Error while creating data entry project',
        'alert',
      );
    });
  };

  return (
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.flex)}>
          <div className={Styles.col2}>
            { !loading && 
              <Caption title={project?.name}>
                {project?.state === 'PUBLISHED' && <span className={Styles.dueDate}>(Survey due date: {project?.dueDate ? regionalDateAndTimeConversionSolution(project?.dueDate) : ''})</span>}
              </Caption> 
            }
          </div>
          <div className={Styles.col2}>
            <div className={Styles.actionsGroup}>
              <button className={classNames('btn', Styles.btnLess)} onClick={() => setShowProjectDetailsModal(true)}>
                <i className={'icon mbc-icon info'}></i>
                <span>Project Details</span>
              </button>
              {user.id === project?.createdBy?.id && project?.state === 'PUBLISHED' &&
                <button className={classNames('btn', Styles.btnLess)} onClick={() => setShowDeUsersInformationModal(true)}>
                  <i className={'icon mbc-icon profile'}></i>
                  <span>Data Entry Users</span>
                </button>
              }
            </div>
          </div>
        </div>
        <div>
          {project?.state === 'PUBLISHED' && <p className={Styles.fillingInstructions}>Filling Instructions</p>}
          <div>
            <DataEntrySheet style={{ flex: 1 }} ref={univerRef} data={data} />
          </div>
          <div className={Styles.footer}>
            {user.id === project?.createdBy?.id && project?.state === 'DRAFT' &&
              <>
                <button
                  className={'btn btn-primary'}
                  onClick={handleUpdate}
                >
                  Save as Draft
                </button>
                <button
                  className={'btn btn-tertiary'}
                  onClick={() => {
                    setShowDeUsersModal(true);
                  }}
                >
                  Send to Data Entry Users
                </button>
              </>
            }
            {project?.state === 'PUBLISHED' && 
              <button
                className={'btn btn-tertiary'}
                onClick={handlePublish}
              >
                Publish
              </button>
            }
          </div>
        </div>
      </div>
      { showProjectDetailsModal &&
        <Modal
          title={'Project Details'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          show={showProjectDetailsModal}
          content={<ProjectDetails project={project} />}
          scrollableContent={true}
          onCancel={() => setShowProjectDetailsModal(false)}
        />
      }
      { showDeUsersModal &&
        <Modal
          title={'Add Data Entry Users'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          show={showDeUsersModal}
          content={<DataEntryUsers user={user} surveyData={() => univerRef.current?.getData()} />}
          scrollableContent={true}
          onCancel={() => setShowDeUsersModal(false)}
        />
      }
      { showDeUsersInformationModal &&
        <Modal
          title={'Data Entry Users Information'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          show={showDeUsersInformationModal}
          content={<DeUsersInformation project={project} />}
          scrollableContent={true}
          onCancel={() => setShowDeUsersInformationModal(false)}
        />
      }
    </React.Fragment>
  );
}
export default DataEntryProject;
