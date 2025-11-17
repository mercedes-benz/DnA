
import cn from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './Uilicious.scss';
import Caption from '../shared/caption/Caption';
import UiliciousCardItem from './UiliciousCardItem';
import CreateNewWorkspace, { WorkspaceFormModel } from './createNewWorkspace/CreateNewWorkspace';
import Modal from 'components/formElements/modal/Modal';

const classNames = cn.bind(Styles);

export type Role = 'Owner' | 'Collaborator' | 'Viewer';

export interface IUiliciousProject {
  workspaceId: string;
  workspaceName: string;
  description?: string;
  createdBy: string;
  createdAt: string;
  role: Role;
  projectStatus?: 'ACTIVE' | 'CREATE_REQUESTED' | 'UPDATE_REQUESTED' | 'INACTIVE';
}


const ProgressIndicator = { show: () => {}, hide: () => {} };
const Notification = { show: (msg: string, _type?: string) => {} };


const MOCK_WORKSPACES: IUiliciousProject[] = [

 ];
  

const fetchMockWorkspaces = (): Promise<{ data: IUiliciousProject[] }> =>
  new Promise((resolve) => {
    setTimeout(() => resolve({ data: MOCK_WORKSPACES.map((d) => ({ ...d })) }), 120);
  });

const Uilicious: React.FC = () => {
  const [workspaceList, setWorkspaceList] = useState<IUiliciousProject[]>([]);
  const [loading, setLoading] = useState(false);


  const [showCreateWorkspaceModal, setShowCreateWorkspaceModal] = useState(false);
  const [editingProject, setEditingProject] = useState<IUiliciousProject | null>(null);


  const loadWorkspaces = () => {
    setLoading(true);
    ProgressIndicator.show();
    fetchMockWorkspaces()
      .then((res) => {
        const sorted = res.data.sort((a, b) => {
          const na = parseInt(a.workspaceId.replace(/[^0-9]/g, ''), 10) || 0;
          const nb = parseInt(b.workspaceId.replace(/[^0-9]/g, ''), 10) || 0;
          return nb - na;
        });
        setWorkspaceList(sorted);
      })
      .catch(() => {
        Notification.show('Failed to load workspaces', 'alert');
        setWorkspaceList([]);
      })
      .finally(() => {
        ProgressIndicator.hide();
        setLoading(false);
      });
  };

  useEffect(() => {
    loadWorkspaces();
  }, []);


  const handleFormSave = (workspace: WorkspaceFormModel) => {
  
    if (editingProject) {
      const updatedProject: IUiliciousProject = {
        workspaceId: workspace.workspaceId || editingProject.workspaceId,
        workspaceName: workspace.workspaceName || '',
        description: workspace.description,
        createdBy: workspace.createdBy || editingProject.createdBy,
        createdAt: workspace.createdAt || editingProject.createdAt,
        role: (workspace.role as Role) || editingProject.role,
        projectStatus: workspace.projectStatus || 'ACTIVE',
      };
      setWorkspaceList(prev => 
        prev.map(w => w.workspaceId === updatedProject.workspaceId ? updatedProject : w)
      );
    } else {
      const newProject: IUiliciousProject = {
        workspaceId: workspace.workspaceId || `WS-${Date.now()}`,
        workspaceName: workspace.workspaceName || '',
        description: workspace.description,
        createdBy: workspace.createdBy || 'Current User',
        createdAt: workspace.createdAt || new Date().toISOString(),
        role: (workspace.role as Role) || 'Owner',
        projectStatus: workspace.projectStatus || 'ACTIVE',
      };
      setWorkspaceList(prev => [newProject, ...prev]);
    }
    
    setShowCreateWorkspaceModal(false);
    setEditingProject(null);
  };

  const openCreateModal = () => {
    setEditingProject(null);
    setShowCreateWorkspaceModal(true);
  };

  const openEditModal = (project: IUiliciousProject) => {
    setEditingProject(project);
    setShowCreateWorkspaceModal(true);
  };

  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          <Caption title="Uilicious" />
        </div>

        <div className={classNames(Styles.content, Styles.pipelineCardView)}>
          <div className={Styles.cardContent}>
            {loading ? (
              <div className={Styles.emptyPipeline}>Loading workspaces...</div>
            ) : workspaceList.length === 0 ? (
              <>
                <div className={Styles.emptyPipeline}>
                  <span>
                    You don't have any Uilicious project at this time.
                    <br /> Please create a new one.
                  </span>
                </div>
                <div className={Styles.subscriptionListEmpty}>
                  <button
                    className={Styles.addNewSubcibtn + ' btn btn-tertiary'}
                    type="button"
                    onClick={openCreateModal}
                  >
                    <span>Onboard New Uilicious Workspace</span>
                  </button>
                </div>
              </>
            ) : (
              <div className={Styles.subscriptionList}>
                {workspaceList.map((project) => (
                  <UiliciousCardItem
                    key={project.workspaceId}
                    project={project}
                    onEdit={(proj) => openEditModal(proj)}
                  />
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {showCreateWorkspaceModal && (
        <Modal
          title={editingProject ? 'Edit Uilicious Workspace' : 'Create New Uilicious Workspace'}
          hiddenTitle={false}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={showCreateWorkspaceModal}
          content={
            <CreateNewWorkspace
              edit={!!editingProject}
              project={editingProject || undefined}
              onSave={(workspace) => handleFormSave(workspace)}
              onCancel={() => {
                setShowCreateWorkspaceModal(false);
                setEditingProject(null);
              }}
            />
          }
          scrollableContent={true}
          onCancel={() => {
            setShowCreateWorkspaceModal(false);
            setEditingProject(null);
          }}
        />
      )}
    </>
  );
};

export default Uilicious;
