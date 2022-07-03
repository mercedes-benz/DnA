import { defineFileAction } from 'chonky';

// Define custom file actions for the explorer

const UploadFolder = defineFileAction({
  id: 'upload_folder',
  button: {
    name: 'Upload folder',
    toolbar: true,
    tooltip: 'Upload folder',
    icon: 'upload',
  },
});

const PublishFolder = defineFileAction({
  id: 'publish_folder',
  button: {
    name: 'Publish to Trino',
    toolbar: true,
    contextMenu: true,
    tooltip: 'Publish to Trino',
    icon: 'upload',
  },
  requiresSelection: true,
  fileFilter: (file, index, selectedList) =>
    selectedList?.length === 1 && file && file.name.toLowerCase()?.split('.')?.[1] === 'parquet',
});

export const CustomActions = {
  PublishFolder,
  UploadFolder,
};
