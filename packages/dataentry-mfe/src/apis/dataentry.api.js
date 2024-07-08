import { server, hostServer, reportsServer, storageServer, datalakeServer } from '../server/api';
import { v4 as uuidv4 } from 'uuid';

const getDataEntryProjects = () => {
  // return server.get(`/dataentries?limit=${limit}&offset=${offset}`, {
  //   data: {},
  // });
  const dataEntries = JSON.parse(localStorage.getItem('dataEntries') ? localStorage.getItem('dataEntries') : '[]');
  return Promise.resolve({
    status: 200,
    data: {
      records: dataEntries,
      totalCount: dataEntries?.length || 0,
    }
  });
};

const createDataEntryProject = (data) => {
  const dataEntries = JSON.parse(localStorage.getItem('dataEntries') ? localStorage.getItem('dataEntries') : '[]');
  const tempData = {
    ...data,
    id: uuidv4(),
    createdBy: {...data.user},
    createdOn: new Date().toISOString().replace(/\.\d{3}Z$/, '+00:00'),
    state: 'DRAFT',
    fillingInstructions: 'string',
    dueDate: '',
    dataLakeDetails: null, 
    dataEntryUsers: [],
    surveyData: 'null',
  }
  dataEntries.push(tempData);
  localStorage.setItem('dataEntries', JSON.stringify(dataEntries));
  return Promise.resolve({
    status: 200,
    data: {
      id: tempData?.id
    }
  });
  // return server.post(`/dataentries`, data);
};

const getDataEntryProject = (id) => {
  // return server.get(`/dataentries/${id}`, {
  //   data: {},
  // });
  const dataEntries = JSON.parse(localStorage.getItem('dataEntries') ? localStorage.getItem('dataEntries') : '[]');
  const project = dataEntries.filter(project => project.id === id);
  return Promise.resolve({
    data: project[0]
  });
};

const updateDataEntryProject = (id, data) => {
  return server.put(`/dataentries/${id}`, data);
};

const saveSheetAsDraft = (id, data) => {
  const dataEntries = JSON.parse(localStorage.getItem('dataEntries') ? localStorage.getItem('dataEntries') : '[]');
  const updatedProjects = dataEntries.map(project => {
    if(project.id === id) {
      return {...project, surveyData: data.surveyData }
    } else {
      return {...project}
    }
  });
  localStorage.setItem('dataEntries', JSON.stringify(updatedProjects));
  // return server.patch(`/dataentries/${id}/survey`, data);
  return Promise.resolve({
    status: 200
  });
};

const publishDataEntryProject = (id, data) => {
  const dataEntries = JSON.parse(localStorage.getItem('dataEntries') ? localStorage.getItem('dataEntries') : '[]');
  const updatedProjects = dataEntries.map(project => {
    if(project.id === id) {
      return {
        ...project, 
        state: 'PUBLISHED',
        fillingInstructions: data.fillingInstructions,
        dueDate: new Date(data.dueDate).toISOString().replace(/\.\d{3}Z$/, '+00:00'),
        dataLakeDetails: data.dataLakeDetails, 
        dataEntryUsers: data.dataEntryUsers,
        surveyData: data.surveyData,
      }
    } else {
      return {...project}
    }
  });
  localStorage.setItem('dataEntries', JSON.stringify(updatedProjects));
  // return server.patch(`/dataentries/${id}/publish`, data);
  return Promise.resolve({
    status: 200
  });
};

const publishDataEntrySheet = (id, data) => {
  const dataEntries = JSON.parse(localStorage.getItem('dataEntries') ? localStorage.getItem('dataEntries') : '[]');
  const updatedProjects = dataEntries.map(project => {
    if(project.id === id) {
      return {...project, surveyData: data.surveyData }
    } else {
      return {...project}
    }
  });
  localStorage.setItem('dataEntries', JSON.stringify(updatedProjects));
  return Promise.resolve({
    status: 200
  });
  // return server.patch(`/dataentries/${id}/survey/publish`, data);
};

const deleteDataEntryProject = (id) => {
  return server.delete(`/dataentries/${id}`, {
    data: {},
  });
};

const getDatalakeProjects = (offset, limit) => {
  return datalakeServer.get(`/datalakes?limit=${limit}&offset=${offset}`, {
    data: {},
  });
};

const getLovData = () => {
  return Promise.all([
    storageServer.get(`/classifications`, {
      data: {},
    }),
    hostServer.get('/divisions'),
    reportsServer.get('/departments', {
      data: {},
    }),
  ]);
}

export const dataEntryApi = {
  getDataEntryProjects,
  getDataEntryProject,
  createDataEntryProject,
  updateDataEntryProject,
  saveSheetAsDraft,
  publishDataEntryProject,
  publishDataEntrySheet,
  deleteDataEntryProject,
  getDatalakeProjects,
  getLovData,
};
