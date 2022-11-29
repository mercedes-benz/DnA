import mockData from '../components/data/data.json';

const getAllDataList = () => {
  return new Promise((resolve) => {
    resolve(mockData.result);
  });
};

const getDataById = (dataList, id) => {
  return new Promise((resolve) => {
    const filteredData = dataList.find((item) => item.id === id);
    resolve(filteredData);
  });
};

const createDataProduct = (data) => {
  return new Promise((resolve) => {
    console.log(data);
    resolve(data);
  });
};

const updateProvider = (data) => {
  return new Promise((resolve) => {
    resolve(data);
  });
};

const updateConsumer = (data) => {
  return new Promise((resolve) => {
    resolve(data);
  });
};

export const dataProductApi = {
  getAllDataList,
  getDataById,
  createDataProduct,
  updateProvider,
  updateConsumer,
};
