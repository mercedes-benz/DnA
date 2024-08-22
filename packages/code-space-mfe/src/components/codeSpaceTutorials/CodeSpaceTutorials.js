import React, { useState, useEffect,useRef } from 'react'; 
import classNames from 'classnames';
import Styles from './CodeSpaceTutorials.scss';
import VideoJS from './VideoJS'
import { Envs } from '../../Utility/envs';


const CodeSpaceTutorials = () => {
  const codespaceTutorials = [
    {
      id: '1',
      title: "Introduction to codespaces",
      url:'code-space-tutorials/Intoduction.mp4',
      description: "An overview of what codespaces are and how they can be used."
    },
    {
      id: '2',
      title: "How to create codespace",
      url: "code-space-tutorials/Creation of codespaces.mp4",
      description: "Step-by-step guide on creating a new codespace."
    },
    {
      id: '3',
      title: "Alice configuration - Staging",
      url: "code-space-tutorials/Alice Configuration - Staging.mp4",
      description: "Configuring Alice for staging environments."
    },
    {
      id: '4',
      title: "Alice configuration - Production",
      url: "code-space-tutorials/Alice Configuration - Production.mp4",
      description: "Configuring Alice for production environments."
    },
    {
      id: '5',
      title: "Secret Management - Staging",
      url: "code-space-tutorials/Secret Management - Staging.mp4",
      description: "Managing secrets in staging environments."
    },
    {
      id: '6',
      title: "Secret Management - Production",
      url: "code-space-tutorials/Secret Management - Production.mp4",
      description: "Managing secrets in production environments."
    },
    {
      id: '7',
      title: "Deployment and view logs - Staging",
      url: "code-space-tutorials/Staging - Deployment and View Log.mp4",
      description: "How to deploy and view logs in staging environment."
    },
    {
      id: '8',
      title: "Deployment and view logs - Production",
      url: "code-space-tutorials/Production - Deployment and view logs.mp4",
      description: "How to deploy and view logs in production environment."
    },
    {
      id: '9',
      title: "Python_Recipe",
      url: "code-space-tutorials/Python_Recipe.mp4",
      description: "A tutorial on Python recipes and use cases."
    },
  ];

  const [selectedVideo, setSelectedVideo] = useState(codespaceTutorials[0]);
  const [videoJsOptions, setVideoJsOptions] = useState(
    {
      autoplay: true,
      controls: true,
      responsive: true,
      fluid: true,
      sources: [{
        src: Envs.CODESPACE_TUTORIALS_BASE_URL + codespaceTutorials[0].url,
        type: 'video/mp4'
      }]
    }
  );
  
  useEffect(()=>{
    setVideoJsOptions({
      autoplay: true,
      controls: true,
      responsive: true,
      fluid: true,
      sources: [{
        src: Envs.CODESPACE_TUTORIALS_BASE_URL + selectedVideo.url,
        type: 'video/mp4'
      }]
    })

  },[selectedVideo])


  const playerRef = useRef(null);


  const handlePlayerReady = (player) => {
    playerRef.current = player;
    player.on('waiting', () => {
      console.log('player is waiting');
    });

    player.on('dispose', () => {
      console.log('player will dispose');
    });
  };


  return (
    <div className={classNames(Styles.wrapper)}>
      <h5 className={classNames(Styles.Modeltitle)}>Code Space Tutorials</h5>
      <div className={classNames(Styles.codeSpaceTutorials)}>
        <div className={classNames(Styles.leftPlane)}>
          <VideoJS options={videoJsOptions} onReady={handlePlayerReady} />
          <h5 className={classNames(Styles.selectedTitle)}>{selectedVideo.title}</h5>
          <span className={classNames(Styles.selectedDes)}>{selectedVideo.description}</span>
        </div>
        <div className={classNames(Styles.rightPlane)}>
          <table className={classNames('ul-table', Styles.tutorialsTable)}>
            <tbody>
              {codespaceTutorials?.map((item) => (
                <tr id={item.id} key={item.id} className={classNames('data-row', Styles.tutorialsRow, item.id === selectedVideo.id ? Styles.selectedRow : '')}>
                  <td className={'wrap-text'}>
                    <div className={Styles.tutorialtitle} onClick={() => { setSelectedVideo(item) }}><i className={classNames('icon mbc-icon sort' ,Styles.videoIcon)}/><span className={Styles.title} >{item.title}</span></div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default CodeSpaceTutorials;
