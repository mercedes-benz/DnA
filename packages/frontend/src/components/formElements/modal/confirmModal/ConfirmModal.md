```jsx
import React, { useState } from 'react';
import ConfirmModal from './ConfirmModal';

const App = ()=>{
    const [showModal,setShowModal] = useState(false);
    const modalContent = (
        <div>Do you wish to continue?</div>
    );

    return (
        <>
        <button className={'btn btn-tertiary'} onClick={()=>setShowModal(true)}>Click Me</button>
        {showModal && 
            <ConfirmModal 
            title='Confirmation Modal'
            acceptButtonTitle='Yes'
            cancelButtonTitle='No'
            showAcceptButton={true}
            showCancelButton={true}
            show={showModal}
            content={modalContent}
            onCancel={()=>setShowModal(false)}
            onAccept={()=>setShowModal(false)}
            />
        }
   </> )
}
<App/>
