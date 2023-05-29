export const getDataForCSV = (currentTab, listData, onDataSuccess) => {
  const projectsCSVData = [];

  const csvHeaders = [
    { label: 'Name', key: 'name' },
    { label: 'Description', key: 'description' },
    { label: 'Status', key: 'status' },
    { label: 'Collaborators', key: 'collaborators' },
    { label: 'Created Date', key: 'createdDate' },
    { label: 'Instance', key: 'instance' },
  ];

  if (currentTab === 'training') {
    csvHeaders.push(
      { label: 'Tags', key: 'tags' },
      { label: 'Checklists', key: 'checklists' },
      { label: 'Last Used', key: 'lastUsed' }
    );
  }
  listData.forEach((project) => {
    const checklistItems = [];
    if (project?.checklists?.checklists) {
      if (project.checklists.checklists.length > 0) {
        project.checklists.checklists.forEach((checklist) => {
          checklistItems.push(checklist.title);
          checklist.items.forEach((item) => {
            checklistItems.push(item.text);
          });
        });
      }
    }
    const collaborators = [];
    if (project?.collaborators && project.collaborators.length > 0) {
      project.collaborators.forEach((user) => {
        collaborators.push(user?.userId);
      })
    }
    projectsCSVData.push({
      name: project.name ? sanitize(project.name) : 'NA',
      description: project.shortDesc ? sanitize(project.shortDesc) : 'NA',
      status: project?.status ? project.status : project?.projectStatus ? project?.projectStatus : 'NA',
      collaborators: collaborators.length > 0 ? collaborators : 'NA',
      createdDate: project?.creationTag
        ? project?.creationTag?.lastModifiedOn
          ? new Date(project?.creationTag?.lastModifiedOn).toUTCString().toString()
          : 'NA'
        : 'NA',
      instance: project?.cloudProfile ? project?.cloudProfile : 'NA'
    });
    if (currentTab === 'training') {
      projectsCSVData.push(
        { tags: project?.tags && project?.tags?.length > 0 ? sanitize(project.tags.join(', ')) : 'NA' },
        { checklists: checklistItems.length > 0 ? checklistItems : 'NA' },
        {
          lastUsed: project?.versionTag
            ? project?.versionTag?.lastModifiedOn
              ? new Date(project?.versionTag?.lastModifiedOn).toUTCString().toString()
              : 'NA'
            : 'NA'
        })
    }
  });

  onDataSuccess(projectsCSVData, csvHeaders);
};

export const sanitize = (text) => {
  return text.replace(/"/g, '""');
};
