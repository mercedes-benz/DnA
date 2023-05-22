export const getDataForCSV = (listData, onDataSuccess) => {
  const projectsCSVData = [];

  const csvHeaders = [
    { label: 'Name', key: 'name' },
    { label: 'Description', key: 'description' },
    { label: 'Tags', key: 'tags' },
    { label: 'Project Status', key: 'projectStatus' },
    { label: 'Contributers', key: 'contributers' },
    { label: 'Checklists', key: 'checklists' },
    { label: 'Created Date', key: 'createdDate' },
    { label: 'Last Used', key: 'lastUsed' },
    { label: 'Instance', key: 'instance' },
  ];

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
    const contributors = [];
    if (project?.contributors && project.contributors.length > 0) {
      project.contributors.forEach((user) => {
        contributors.push(user?.userId);
      })
    }
    projectsCSVData.push({
      name: project.name ? sanitize(project.name) : 'NA',
      description: project.shortDesc ? sanitize(project.shortDesc) : 'NA',
      tags: project?.tags && project?.tags?.length > 0 ? sanitize(project.tags.join(', ')) : 'NA',
      projectStatus: project.projectStatus ? project.projectStatus : 'NA',
      contributers: contributors.length > 0 ? contributors : 'NA',
      checklists: checklistItems.length > 0 ? checklistItems : 'NA',
      createdDate: project?.creationTag
        ? project?.creationTag?.lastModifiedOn
          ? new Date(project?.creationTag?.lastModifiedOn).toUTCString().toString()
          : 'NA'
        : 'NA',
      lastUsed: project?.versionTag
        ? project?.versionTag?.lastModifiedOn
          ? new Date(project?.versionTag?.lastModifiedOn).toUTCString().toString()
          : 'NA'
        : 'NA',
      instance: project?.cloudProfile ?  project?.cloudProfile: 'NA'
    });
  });

  onDataSuccess(projectsCSVData, csvHeaders);
};

export const sanitize = (text) => {
  return text.replace(/"/g, '""');
};
