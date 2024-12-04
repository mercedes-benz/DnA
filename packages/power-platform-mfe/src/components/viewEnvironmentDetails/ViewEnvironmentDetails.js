import React from 'react';
import Styles from './view-environment-details.scss';

const ViewEnvironmentDetails = ({ environment }) => {  

  return (
    <div className={Styles.container}>
      <div className={Styles.header}>
        <h3>{environment?.name} Details</h3>
      </div>
      <div className={Styles.flex}>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Name of Project</p>
            <p className={Styles.value}>{environment?.name}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Environment</p>
            <p className={Styles.value}>{environment?.environment}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Environment Owner Name</p>
            <p className={Styles.value}>{environment?.envOwnerName?.length > 0 ? environment?.envOwnerName : 'N/A'}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Environment Owner User ID</p>
            <p className={Styles.value}>{environment?.envOwnerId}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Deputy Environment Owner Name</p>
            <p className={Styles.value}>{environment?.dyEnvOwnerName?.length > 0 ? environment?.dyEnvOwnerName : 'N/A'}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Deputy Environment Owner User ID</p>
            <p className={Styles.value}>{environment?.dyEnvOwnerId?.length > 0 ? environment?.dyEnvOwnerId : 'N/A'}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Department</p>
            <p className={Styles.value}>{environment?.department}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Billing Contact</p>
            <p className={Styles.value}>{environment?.billingContact?.length > 0 ? environment?.billingContact : 'N/A'}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Billing Plant</p>
            <p className={Styles.value}>{environment?.billingPlant}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Billing Cost Center</p>
            <p className={Styles.value}>{environment?.billingCostCentre}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Custom Requirements</p>
            <p className={Styles.value}>{environment?.customRequirements?.length > 0 ? environment?.customRequirements : 'N/A'}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Do you want the PROD environment immediately or later?</p>
            <p className={Styles.value}>{environment?.prodEnvAvailability}</p>
          </div>
        </div>
        <div className={Styles.col}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>User Licenses</p>
            {environment?.developers?.length > 0 && environment?.developers?.map((developer) => 
                <p key={developer?.userDetails?.id} className={Styles.value}>{developer?.userDetails?.userFirstName} {developer?.userDetails?.lastName} ({developer?.userDetails?.id}) - <span>({developer?.license})</span></p>
              )
            }
          </div>
        </div>
      </div>
    </div>
  );
};

export default ViewEnvironmentDetails;