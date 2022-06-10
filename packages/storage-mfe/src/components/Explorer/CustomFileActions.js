import { defineFileAction } from 'chonky';

// Define custom file actions for the explorer

// export const UploadFolder = defineFileAction({
//   id: 'upload_folder',
//   button: {
//     name: 'Upload folder',
//     toolbar: true,
//     tooltip: 'Upload folder',
//     icon: 'upload',
//   },
// });

const PublishFolder = defineFileAction({
  id: 'publish_folder',
  button: {
    name: 'Publish to Trino',
    toolbar: true,
    contextMenu: true,
    tooltip: 'Publish to Trino',
    icon: 'folder',
  },
  requiresSelection: true,
  fileFilter: (file) => file && file.name.toLowerCase()?.split('.')?.[1] === 'parquet',
});

export const CustomActions = {
  PublishFolder,
};
