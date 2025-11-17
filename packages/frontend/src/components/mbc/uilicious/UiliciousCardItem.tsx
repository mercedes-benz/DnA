
import React, { useEffect, useState } from 'react';
import Styles from './UiliciousCardItem.scss';
//import { history } from '../../../router/History';
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import Modal from 'components/formElements/modal/Modal';
import CreateNewWorkspace, { WorkspaceFormModel } from './createNewWorkspace/CreateNewWorkspace';
import SelectBox from 'components/formElements/SelectBox/SelectBox';

type RoleType = 'Owner' | 'Collaborator' | 'Viewer';

interface IUiliciousProjectDetail {
  workspaceId: string;
  workspaceName: string;
  description?: string;
  createdBy: string;
  createdAt: string;
  role: RoleType;
  projectStatus?: 'ACTIVE' | 'CREATE_REQUESTED' | 'UPDATE_REQUESTED' | 'INACTIVE';
}

interface Props {
  project: IUiliciousProjectDetail;
  getRefreshedProject?: (projectId: string) => void;
  onEdit?: (project: IUiliciousProjectDetail) => void;
}

const formatDateShort = (iso?: string) => {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

const UiliciousCardItem: React.FC<Props> = ({ project, getRefreshedProject, onEdit }) => {
  const [editProject, setEditProject] = useState(false);

  useEffect(() => {
    // setup tooltips if the Tooltip module exists
    // @ts-ignore
    if (Tooltip && typeof Tooltip.defaultSetup === 'function') Tooltip.defaultSetup();
  }, [project]);

    useEffect(() => {
    SelectBox.defaultSetup();
  }, []);

  const goToEditProject = () => {
    if (typeof onEdit === 'function') {
      onEdit(project);
      return;
    }
    setEditProject(true);
  };

  const handleOpenWorkspaceExternal = () => {
    window.open(
      '',
      '_blank',
      'noopener,noreferrer'
    );
  };

  const onLocalFormSave = (workspace: WorkspaceFormModel) => {
    setEditProject(false);
    if (typeof getRefreshedProject === 'function') {
      try {
        getRefreshedProject(project.workspaceId);
      } catch (err) {
        // swallow errors — parent refresh optional
      }
    }
  };

  return (
    <>
      <div className={Styles.pipelineCard} style={{ cursor: 'pointer' }}>
        <div className={Styles.cardHead}>
          <div className={Styles.cardHeadInfo}>
            <div
              className={'btn btn-text forward arrow ' + Styles.cardHeadTitle}
              onClick={(e) => {
                e.stopPropagation();
                handleOpenWorkspaceExternal();
              }}
              role="button"
              tabIndex={0}
              title={project.workspaceName}
            >
              {project.workspaceName}
            </div>
          </div>
        </div>

        <hr />

        <div className={Styles.cardBodySection}>
          <div>
            <div>
              <div className={Styles.label}>Created By</div>
              <div className={Styles.value}>{project.createdBy || '—'}</div>
            </div>

            <div>
              <div className={Styles.label}>Created At</div>
              <div className={Styles.value}>{formatDateShort(project.createdAt)}</div>
            </div>

            <div>
              <div className={Styles.label}>Role</div>
              <div className={Styles.value}>{project.role || '—'}</div>
            </div>
          </div>
        </div>

        <div className={Styles.cardFooter}>
          <div />
          <div className={Styles.btnGrp}>
            {project.projectStatus === 'CREATE_REQUESTED' ? (
              <span className={`${Styles.statusIndicator} ${Styles.colloboration}`}>
                Creation in progress...
              </span>
            ) : project.projectStatus === 'UPDATE_REQUESTED' ? (
              <span className={`${Styles.statusIndicator} ${Styles.colloboration}`}>
                Updation in progress...
              </span>
            ) : (
              <>
                {project.role === 'Owner' && (
                  <button
                    className="btn btn-primary"
                    onClick={(e) => {
                      e.stopPropagation();
                      goToEditProject();
                    }}
                    title="Edit project"
                  >
                    <i className="icon mbc-icon edit" />
                    <span>Edit Project</span>
                  </button>
                )}
              </>
            )}
          </div>
        </div>
      </div>

      {editProject && (
        <Modal
          title={'Edit Uilicious Workspace'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={editProject}
          content={
            <CreateNewWorkspace
              edit={true}
              project={project}
              onSave={(workspace) => {
                onLocalFormSave(workspace);
              }}
              onCancel={() => {
                setEditProject(false);
              }}
            />
          }
          scrollableContent={true}
          onCancel={() => setEditProject(false)}
          modalStyle={{
            padding: '50px 35px 35px 35px',
            minWidth: 'unset',
            width: '60%',
            maxWidth: '50vw',
          }}
        />
      )}
    </>
  );
};

export default UiliciousCardItem;

