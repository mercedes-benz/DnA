import { defineFileAction, reduxActions } from 'chonky';

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
    name: 'Download files/folders',
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
    name: 'Delete files/folders',
    toolbar: true,
    contextMenu: true,
    group: 'Actions',
    icon: 'trash',
  },
});

const CopyPath = defineFileAction({
  id: 'copy_files',
  hotkeys: ['ctrl+c'],
  button: {
    name: 'Copy file path',
    toolbar: true,
    contextMenu: true,
    tooltip: 'Copy Path',
    icon: 'copy',
    group: 'Actions',
  },
  requiresSelection: true,
  fileFilter: (file, index, selectedList) => selectedList?.length === 1 && file && !file.isDir,
});

const ResetSearchInput = defineFileAction(
  {
    id: 'reset_search_input',
  },
  ({ reduxDispatch }) => {
    // dispatch chonky redux action to reset the search key applied on files
    reduxDispatch(reduxActions.setSearchString(''));

    // clear search text
    const searchInput = document.querySelector('.chonky-searchFieldContainer input');
    searchInput.dispatchEvent(new KeyboardEvent('keyup', { key: 'Escape', bubbles: true, code: 'Escape' }));
  },
);

export const CustomActions = {
  PublishFolder,
  UploadFolder,
  DownloadFiles,
  DeleteFiles,
  CopyPath,
  ResetSearchInput,
};
