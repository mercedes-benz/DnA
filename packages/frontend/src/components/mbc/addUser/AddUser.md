```jsx 
import React from 'react';
import AddUser from './AddUser';
import Styles from './AddUser.styleguide.scss';

<div className={Styles.dagCollContentList}>
    <div className={Styles.dagCollContentListAdd} >
        <AddUser getCollabarators={(collaborators,dagId)=>{
            return {
                department: 'ITP/IG',
                email: 'demouser@mercedes-benz.com',
                firstName: 'Demo',
                id: 'DEMOUSER',
                lastName: 'User',
            }
        }} datId={''} isUserprivilegeSearch={false} />
    </div>
</div>

