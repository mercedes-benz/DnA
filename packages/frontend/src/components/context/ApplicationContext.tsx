import * as React from 'react';

const contextVar = {
    message: 'COMPLETE_UPDATE_NOTIFICATIONS',
    setMessage: (event: string)=>{}
};
const AppContext = React.createContext(contextVar);

// export class AppContextComponent extends React.Component {
//     constructor(props: React.PropsWithChildren<{ children?: React.ReactNode | undefined }>) {
//         super(props);
//         this.setMessage = this.setMessage.bind(this);
//     }
    
//     state={
//         message: 'COMPLETE_UPDATE_NOTIFICATIONS',
//     }

//     setMessage(){
//         this.setState({message: 'POLL_NOTIFICATION'});
//     }

//     render(){

//         const {message} = this.state;
//         const {setMessage} = this;
//         /* tslint:disable */
//         const {children} = this.props

//         return (<AppContext.Provider value={{message, setMessage}}>{children}</AppContext.Provider>);    
//     }
// }

export default AppContext;