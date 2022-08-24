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

// disable download and delete options for Folders
const DownloadFiles = defineFileAction({
  id: 'download_files',
  requiresSelection: true,
  button: {
    name: 'Download files',
    toolbar: true,
    contextMenu: true,
    group: 'Actions',
    icon: 'download',
  },
  fileFilter: (file) => file && !file.isDir,
});

const DeleteFiles = defineFileAction({
  id: 'delete_files',
  requiresSelection: true,
  hotkeys: ['delete'],
  button: {
    name: 'Delete files',
    toolbar: true,
    contextMenu: true,
    group: 'Actions',
    icon: 'trash',
  },
  fileFilter: (file) => file && !file.isDir,
});

export const CustomActions = {
  PublishFolder,
  UploadFolder,
  DownloadFiles,
  DeleteFiles,
};
