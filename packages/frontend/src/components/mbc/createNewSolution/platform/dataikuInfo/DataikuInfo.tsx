import cn from 'classnames';
import React, { useState, useEffect, forwardRef, Ref, useImperativeHandle } from 'react';
import { ApiClient } from '../../../../../services/ApiClient';
import { IDataiku } from 'globals/types';
import Styles from './DataikuInfo.scss';
import DataikuProjects from './dataikuProjects/DataikuProjects';
import { getDataikuInstanceTag, getDateFromTimestamp } from '../../../../../services/utils';
import { Envs } from 'globals/Envs';

const classNames = cn.bind(Styles);

export interface IDataikuInfoProps {
  currSolutionId: string;
  projectId: string;
  projectInstance: string;
  onProjectLinkSuccess: (project: IDataiku) => void;
  onProjectLinkRemove: () => void;
}

export interface IDataikuInfoRef {
  triggerDataikuProjectLink: () => void;
}

const DataikuInfo = forwardRef((props: IDataikuInfoProps, ref: Ref<IDataikuInfoRef>) => {
  const [dataikuInfo, setDataikuInfo] = useState<IDataiku>();
  const [dataikuProjectToLink, setDataikuProjectToLink] = useState<IDataiku>();
  const [selectionError, setSelectionError] = useState<boolean>(false);
  const [canOnlyViewDataiku, setcanOnlyViewDataiku] = useState<boolean>(false);

  const getDataikuInfo = () => {
    ApiClient.getDataikuProjectDetailsByProjectkey(props.projectId, props.projectInstance).then((res) => {
      if (res?.data) {
        setDataikuInfo(res.data);
        if (!res.data?.isProjectAdmin) {
          setcanOnlyViewDataiku(true);
        }
      }
    }).catch(() => {
      const dataikuInfo = {
        name: props.projectId,
        projectKey: props.projectId,
        cloudProfile: props.projectInstance,
      };
      setcanOnlyViewDataiku(true);
      setDataikuInfo(prevDataikuInfo => ({ ...prevDataikuInfo, ...dataikuInfo }));
    });
  };

  const onProjectToLink = (project: IDataiku) => {
    setDataikuProjectToLink(project);
  };

  useEffect(() => {
    if (props.projectId) {
      getDataikuInfo();
    } else {
      setDataikuInfo(null);
    }
    setSelectionError(false);
  }, [props.projectId]);

  useImperativeHandle(ref, () => ({
    triggerDataikuProjectLink() {
      if (dataikuProjectToLink) {
        props.onProjectLinkSuccess(dataikuProjectToLink);
      } else {
        setSelectionError(true);
      }
    },
  }));

  const removeProject = () => {
    setSelectionError(false);
    setDataikuProjectToLink(null);
    props.onProjectLinkRemove();
  };

  return (
    <React.Fragment>
      {dataikuInfo ? (
        <>
          <div className={classNames(Styles.projectCard)}>
            <div className={Styles.projectIcon}>
              <i className="icon mbc-icon dataiku" />
            </div>
            <div className={Styles.projectCardContent}>
              <h6>
                <a
                  href={dataikuInfo?.cloudProfile?.toLowerCase().includes('extollo')
                    ? Envs.DATAIKU_LIVE_APP_URL + '/projects/' + dataikuInfo.projectKey + '/'
                    : Envs.DATAIKU_LIVE_ON_PREMISE_APP_URL + '/projects/' + dataikuInfo.projectKey + '/'}
                  target="_blank"
                  rel="noreferrer"
                >
                  {dataikuInfo.name}
                </a>
                {' '}({getDataikuInstanceTag(dataikuInfo.cloudProfile)})
              </h6>
              <label>
                {dataikuInfo?.creationTag?.lastModifiedOn ? `Created on ${getDateFromTimestamp(dataikuInfo.creationTag?.lastModifiedOn, '.')}` : ''}
              </label>
              <div className={Styles.projectCardDesc}>{dataikuInfo.shortDesc}</div>
              {!canOnlyViewDataiku && <span className={Styles.closeICon} onClick={removeProject}>
                <i className="icon mbc-icon close thin" />
              </span>}
            </div>
          </div>
          {!canOnlyViewDataiku && <p className={Styles.computeInfo}>
            Click on close 'x' button and Save &amp; Next to unlink the dataiku project.
          </p>}
        </>
      ) : dataikuInfo !== null ? (
        <div className="text-center">
          <div className="progress infinite" />
        </div>
      ) : (
        <DataikuProjects
          currSolutionId={props.currSolutionId}
          showError={selectionError}
          onProjectSelection={onProjectToLink}
        />
      )}
    </React.Fragment>
  );
});

export default DataikuInfo;
