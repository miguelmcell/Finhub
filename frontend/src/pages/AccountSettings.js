import React, { useState, useEffect } from 'react';
import UserCard from '../components/UserCard';
import { Button, Container, Badge, Spinner } from 'react-bootstrap';
import axiosConfig from '../utils/axiosConfig';
import brokerSyncAxiosConfig from '../utils/brokerSyncAxiosConfig';
import { useCookies } from 'react-cookie';
import { useHistory } from "react-router-dom";
import './AccountSettings.css';


function AccountSettings(props) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('*****');
    const [email, setEmail] = useState('');
    const [avatar, setAvatar] = useState('');
    const [visibility, setVisibility] = useState('');
    const [changePass, setChangePass] = useState(false);
    const [changeEmail, setChangeEmail] = useState(true);
    const [changeAvatar, setChangeAvatar] = useState(true);
    const [changeRobinhoodUsername, setChangeRobinhoodUsername] = useState(true);
    const [changeWebullUsername, setChangeWebullUsername] = useState(true);
    const [changeVisibility, setChangeVisibility] = useState(true);
    const [cookies, setCookie, removeCookie] = useCookies(['session']);

    const [usernameP, setUsernameP] = useState(props.username);
    const [passwordP, setPasswordP] = useState('*****');
    const [emailP, setEmailP] = useState('');
    const [avatarP, setAvatarP] = useState('');
    const [visibilityP, setVisibilityP] = useState('');

    const [robinhoodUsername, setRobinhoodUsername] = useState('');
    const [robinhoodUsernameP, setRobinhoodUsernameP] = useState('');
    const [robinhoodPassword, setRobinhoodPassword] = useState('');
    const [robinhoodToken, setRobinhoodToken] = useState('');
    const [robinhoodStatus, setRobinhoodStatus] = useState(false);
    const [robinhoodSyncLoading, setRobinhoodSyncLoading] = useState(false);

    const [webullUsername, setWebullUsername] = useState('');
    const [webullUsernameP, setWebullUsernameP] = useState('');
    const [webullPassword, setWebullPassword] = useState('');
    const [webullMFA, setWebullMFA] = useState('');
    const [webullMFAP, setWebullMFAP] = useState('');
    const [webullStatus, setWebullStatus] = useState(false);
    const [webullSyncLoading, setWebullSyncLoading] = useState(false);
    const [webullMFAStatus, setWebullMFAStatus] = useState('Idle');

    const [busy, setBusy] = useState(true);
    const [error, setError] = useState(null);

    const hostBase = process.env.NODE_ENV==='development'?'http://localhost:8080':'/api';

    let history = useHistory();

    useEffect(
        () => {
            axiosConfig.get(`${hostBase}/getAccount`)
                .then(response => {
                    console.log(response.data);
                    if(response.data.username)setUsername(response.data.username);
                    if(response.data.email)setEmail(response.data.email);
                    setEmailP(response.data.email);
                    if(response.data.avatar)setAvatar(response.data.avatar);
                    setAvatarP(response.data.avatar);
                    if(response.data.visibility)setVisibility(response.data.visibility);
                    setVisibilityP(response.data.visibility);
                    if(response.data.robinhoodUsername)setRobinhoodUsername(response.data.robinhoodUsername);
                    setRobinhoodUsernameP(response.data.robinhoodUsername);
                    if(response.data.webullUsername)setWebullUsername(response.data.webullUsername);
                    setWebullUsernameP(response.data.webullUsername);
                    setWebullStatus(response.data.webullStatus);
                    setRobinhoodStatus(response.data.robinhoodStatus);
                    setBusy(false);
                })
                .catch(error => {
                    if (error.message === 'Unauthorized') {
                        console.log("Token expired");
                        history.push('/login');
                        history.go(0);
                    } else {
                        console.log('System error');
                        // display some error text
                        setBusy(false);
                        setError('unknown');
                    }
                });
        }, []
    )

    function changeEmailEvent() {
        console.log(email, emailP);
        if (!changeEmail) {
            // check if emails arent the same
            if (email !== emailP) {
                console.log('setting new email');
                setEmail(emailP);
                axiosConfig.put(`${hostBase}/changeEmail`, { email: emailP, userId: cookies['userId'] })
                    .then(response => {
                        console.log("changed account email", response);
                        //TODO some green notification or something
                    })
                    .catch(error => {
                        console.log("failed to get account info", error);
                        setError('unable to change email');
                    });
            } else {
                //throw error for same email as before
                //TODO also cant be blank and has to be valid email(email validation)
            }
        }
        setChangeEmail(!changeEmail);
    }
    function changeAvatarEvent() {
        if (!changeAvatar) {
            // check if emails arent the same
            if (avatar !== avatarP) {
                console.log('setting new avatar');
                setAvatar(avatarP);
                axiosConfig.put(`${hostBase}/changeAvatar`, { avatar: avatarP, userId: cookies['userId'] })
                    .then(response => {
                        console.log("changed avatar", response);
                        // some green notification or something
                    })
                    .catch(error => {
                        console.log("failed to change avatar", error);
                        setError('unable to change avatar');
                    });
            } else {
                //throw error for same email as before
                // also cant be blank and has to be valid email
            }
        }
        setChangeAvatar(!changeAvatar);
    }
    function changeVisibilityEvent() {
        if (!changeVisibility) {
            // check if emails arent the same
            if (visibility !== visibilityP) {
                console.log('setting new visibility');
                setVisibility(visibilityP);
                axiosConfig.put(`${hostBase}/changeVisibility`, { visibility: visibilityP, userId: cookies['userId'] })
                    .then(response => {
                        console.log("changed visibility", response);
                        // some green notification or something
                    })
                    .catch(error => {
                        console.log("failed to change visibility", error);
                        setError('unable to change visibility');
                    });
            } else {
                //throw error for same email as before
                // also cant be blank and has to be valid email
            }
        }
        setChangeVisibility(!changeVisibility);
    }
    function changePasswordEvent() {
        //TODO needs to verify password with new password before changing
        // if(!changePass) {
        //   // check if emails arent the same
        //   if(password) {
        //     console.log('setting new password');
        //     setVisibility(visibilityP);
        //     axiosConfig.put(`http://localhost:8080/changeVisibility`,{visibility:visibilityP,userId:cookies['userId']})
        //     .then(response => {
        //       console.log("changed visibility", response);
        //       // some green notification or something
        //     })
        //     .catch(error => {
        //       console.log("failed to change visibility", error);
        //     });
        //   } else {
        //     //throw error for same email as before
        //     // also cant be blank and has to be valid email
        //   }
        //
        // }
        //TODO show error when failed to change password
        setChangePass(!changePass);
    }
    function deleteAccountEvent() {
        // make 2 tabs, account and sign out when logged in
        console.log('im boutta get deleted', cookies['userId']);
        axiosConfig.post(`${hostBase}/deleteAccount`, { email: '', userId: cookies['userId'] })
            .then(response => {
                console.log("account successfully deleted", response);
                removeCookie('user');
                removeCookie('Authorization');
                removeCookie('userId');
                history.push('/home');
                history.go(0);
            })
            .catch(error => {
                console.log("failed to get account info", error);
                setError('unable to contact server, please try again later');
            });
    }
    function changeRobinhoodUsernameEvent() {
      // do nothing if empty
      if (!changeRobinhoodUsername) {
          // check if emails arent the same
          if (robinhoodUsername !== robinhoodUsernameP) {
              console.log('setting new robinhood username');
              setRobinhoodUsername(robinhoodUsernameP);
              axiosConfig.put(`${hostBase}/changeRobinhoodUsername`, { email: robinhoodUsernameP, userId: cookies['userId'] })
                  .then(response => {
                      console.log("changed account email", response);
                      //TODO some green notification or something
                  })
                  .catch(error => {
                      console.log("failed to get account info", error);
                      setError('unable to change robinood username');
                  });
          } else {
              //throw error for same email as before
              //TODO also cant be blank and has to be valid email(email validation)
          }
      }
      setChangeRobinhoodUsername(!changeRobinhoodUsername);
    }
    function changeWebullUsernameEvent(){
      if (!changeWebullUsername) {
          if (webullUsername !== webullUsernameP) {
              setWebullUsername(webullUsernameP);
              axiosConfig.put(`${hostBase}/changeWebullUsername`, { email: webullUsernameP, userId: cookies['userId'] })
                  .then(response => {
                      console.log("changed account webull username", response);
                      //TODO some green notification or something
                  })
                  .catch(error => {
                      setError('unable to change webull username');
                  });
          } else {
              //throw error for same email as before
              //TODO also cant be blank and has to be valid email(email validation)
          }
      }
      setChangeWebullUsername(!changeWebullUsername);
    }
    function syncRobinhoodAccount() {
      setRobinhoodSyncLoading(true);
      if(robinhoodUsername !== robinhoodUsernameP){
        setRobinhoodUsername(robinhoodUsernameP);
        axiosConfig.put(`${hostBase}/changeRobinhoodUsername`, { email: robinhoodUsernameP, userId: cookies['userId'] })
            .then(response => {
                console.log("changed account email", response);
                //TODO some green notification or something
            })
            .catch(error => {
                console.log("failed to get account info", error);
                setError('unable to change robinood username');
            });
      }
      brokerSyncAxiosConfig.post(`${hostBase}/syncRobinhood`, { username: robinhoodUsernameP, password: robinhoodPassword, mfa_code: robinhoodToken })
          .then(response => {
              // TODO add exceptions instead of just returning 200s
              setRobinhoodSyncLoading(false);
              setRobinhoodStatus("Connected");
          })
          .catch(error => {
            setRobinhoodSyncLoading(false);
              console.log("failed to sync with given robinhood account");
        });
    }

    function syncWebullAccount() {
      setWebullSyncLoading(true);
      if(webullUsername !== webullUsernameP){
        setWebullUsername(webullUsernameP);
        axiosConfig.put(`${hostBase}/changeWebullUsername`, { email: webullUsernameP, userId: cookies['userId'] })
            .then(response => {
                console.log("changed account webull username", response);
                //TODO some green notification or something
                brokerSyncAxiosConfig.post(`${hostBase}/syncWebull`, { password: webullPassword, mfa: webullMFAP })
                    .then(response => {
                        setWebullSyncLoading(false);
                        setWebullStatus("Connected");
                    })
                    .catch(error => {
                        setWebullSyncLoading(false);
                        console.log("failed to sync with given webull account");
                  });
            })
            .catch(error => {
                setError('unable to change webull username');
            });
      } else {
        brokerSyncAxiosConfig.post(`${hostBase}/syncWebull`, { password: webullPassword, mfa: webullMFAP })
            .then(response => {
                setWebullSyncLoading(false);
                setWebullStatus("Connected");
            })
            .catch(error => {
                setWebullSyncLoading(false);
                console.log("failed to sync with given webull account");
          });
      }
    }

    function disconnectWebullAccount() {
      axiosConfig.post(`${hostBase}/disconnectWebull`)
          .then(response => {
              console.log("Webull Account removed!");
              setWebullStatus("Disconnected");
          })
          .catch(error => {
              console.log("failed to contact server");//TODO CHANGE TIMEOUT FOR WEBULLL SYNC
        });
    }

    function disconnectRobinhoodAccount() {
      axiosConfig.post(`${hostBase}/disconnectRobinhood`)
          .then(response => {
              console.log("Account removed!");
              setRobinhoodStatus("Disconnected");
          })
          .catch(error => {
              console.log("failed to contact server");
        });
    }

    function sendWebullMFAToken() {
      axiosConfig.get(`${hostBase}/getWebullMfa`, { email: webullUsernameP })
          .then(response => {
              console.log("Sent webull MFA token to email!");
              setWebullMFAStatus("Sent");
          })
          .catch(error => {
              console.log("failed to contact server");
        });
    }

    if(error) {
      return (
        <h2 className='error-message'>
          {(error==='unknown')?'Error has occured, please try again later':error}
        </h2>
      );
    }
    if(busy) {
      return (
        <div>
          {/*<ClimbingBoxLoader
            color={"#9c6eff"}
            loading={busy}
            css={'display: block; margin: 0 auto;'}
          />
          */}
        </div>
      );
    } else {
      return (
        <div className="account-setting-overlay">
          <h2> Account Settings </h2>
          <p>{`Username: ${username}`}</p>
          <div className="account-form-group">
            <div className='account-form-row'>
              <form type="submit" className='account-form' onSubmit={(e) => setPasswordP(e.target.value)}>Password: <input className='account-input' autoComplete="new-password" name="finHubPassword" type="password" placeholder={password}></input></form>
              <Button style={{marginLeft:'15px'}} onClick={() => { changePasswordEvent() }}>Save</Button>
            </div>
            <div className='account-form-row'>
              <div className='account-input'>Email: <input type="text" value={emailP} onChange={(e) => {setEmailP(e.target.value);(e.target.value===email)?setChangeEmail(true):setChangeEmail(false);}}></input></div>
              <Button disabled={changeEmail?true:false} style={{marginLeft:'15px'}} onClick={() => { changeEmailEvent() }}>{'Save'}</Button>
            </div>
            <div className='account-form-row'>
              <div className='account-input'>Avatar: <input type="text" value={avatarP} onChange={(e) => {setAvatarP(e.target.value);(e.target.value===avatar)?setChangeAvatar(true):setChangeAvatar(false);}}></input></div>
              <Button disabled={changeAvatar?true:false} style={{marginLeft:'15px'}} onClick={() => { changeAvatarEvent() }}>Save</Button>
            </div>
            <div className='account-form-row'>
              <div className='account-input'>Visibility: <input type="text" value={visibilityP} onChange={(e) => {setVisibilityP(e.target.value);(e.target.value===visibility)?setChangeVisibility(true):setChangeVisibility(false);}}></input></div>
              <Button disabled={changeVisibility?true:false} style={{marginLeft:'15px'}} onClick={() => { changeVisibilityEvent() }}>Save</Button>
            </div>
          </div>
          <Button style={{ display: 'flex',flexDirection: 'row', justifyContent: "center",alignItems: 'center', marginTop:'15px', marginBottom:'15px'}} onClick={(e) => deleteAccountEvent()} type="button" variant="danger">Delete Account</Button>
          <h2 style={{ color: 'white' }}> Robinhood Settings </h2>
          <div style={{marginBottom:'20px'}}>
            Status: <Badge variant={(robinhoodStatus==="Connected")?"success":"warning"}>{robinhoodStatus}</Badge>
          </div>
          <div className="robinhood-form-group">
            <div className="account-form-row">
              <div className='robinhood-input'>Robinhood Username: <input type="text" name="robinhoodUsername"  value={robinhoodUsernameP} onChange={(e) => {setRobinhoodUsernameP(e.target.value);(e.target.value===robinhoodUsername)?setChangeRobinhoodUsername(true):setChangeRobinhoodUsername(false);}}></input></div>
              <Button disabled={changeRobinhoodUsername?true:false} style={{marginLeft:'15px'}} onClick={() => { changeRobinhoodUsernameEvent() }}>Save</Button>
            </div>
            <div className="account-form-row">
              <div className='robinhood-input'>{'Robinhood Password'}: <input autoComplete="new-password" type="password" name="robinhoodPass" onChange={(e) => {setRobinhoodPassword(e.target.value);}}></input></div>
              <Button style={{marginLeft:'15px', visibility: 'hidden'}}>Save</Button>
            </div>
            <div className="account-form-row">
              <div className='robinhood-input'>{'Google MFA token:'} <input className="robinhood-input" type="text" name="robinhoodToken" onChange={(e) => {setRobinhoodToken(e.target.value);}}></input></div>
              <Button style={{marginLeft:'15px', visibility: 'hidden'}} disabled>Save</Button>
            </div>
          </div>
          <div className={(robinhoodStatus==='Connected')?'robinhood-buttons-account':'robinhood-buttons-no-account'}>
            {robinhoodSyncLoading&&<div style={{display:'flex', marginBottom:'10px'}}><Spinner style={{margin:'auto'}} animation="border" variant="success" /></div>}
            <div className='account-button'>
              <Button disabled={robinhoodPassword&&robinhoodToken&&robinhoodUsernameP&&!robinhoodSyncLoading?false:true} onClick={(e) => syncRobinhoodAccount()} type="button" variant="success">Sync Robinhood Account</Button>
            </div>
            <div className='account-button'>
              {(robinhoodStatus&&robinhoodStatus==='Connected')?<Button onClick={(e) => disconnectRobinhoodAccount()} type="button" variant="danger">Disconnect Account</Button>:<div/>}
            </div>
          </div>
          <h2 style={{ color: 'white' }}>Webull Settings</h2>
          <div style={{marginBottom:'20px'}}>
            Status: <Badge variant={(webullStatus==="Connected")?"success":"warning"}>{webullStatus}</Badge>
          </div>
          <div className="robinhood-form-group">
            <div className="account-form-row">
              <div className='robinhood-input'>Webull Email: <input type="text" name="webullUsername"  value={webullUsernameP} onChange={(e) => {setWebullUsernameP(e.target.value);(e.target.value===webullUsername)?setChangeWebullUsername(true):setChangeWebullUsername(false);}}></input></div>
              <Button disabled={changeWebullUsername?true:false} style={{marginLeft:'15px'}} onClick={() => { changeWebullUsernameEvent() }}>Save</Button>
            </div>
            <div className="account-form-row">
              <div className='robinhood-input'>{'Webull Password'}: <input autoComplete="new-password" type="password" name="webullPass" onChange={(e) => {setWebullPassword(e.target.value);}}></input></div>
              <Button style={{marginLeft:'15px', visibility: 'hidden'}}>Save</Button>
            </div>
            <div className="account-form-row">
              <div className='robinhood-input'>{'Webull MFA Token'}: <input type="text" name="webullMFA"  value={webullMFAP} onChange={(e) => {setWebullMFAP(e.target.value);}}></input></div>
              <Button disabled={(webullUsernameP&&!webullSyncLoading)?false:true} style={{marginLeft:'15px'}} onClick={() => { sendWebullMFAToken() }}>{'Send'}</Button>
            </div>
          </div>
          <div className={(webullStatus==='Connected')?'robinhood-buttons-account':'robinhood-buttons-no-account'}>
            {webullSyncLoading&&<div style={{display:'flex', marginBottom:'10px'}}><Spinner style={{margin:'auto'}} animation="border" variant="success" /></div>}
            <div className='account-button'>
              <Button disabled={(webullPassword&&webullUsernameP&&webullMFAP&&!webullSyncLoading)?false:true} onClick={(e) => syncWebullAccount()} type="button" variant="success">Sync Webull Account</Button>
            </div>
            <div className='account-button'>
              {(webullStatus&&webullStatus==='Connected')?<Button onClick={(e) => disconnectWebullAccount()} type="button" variant="danger">Disconnect Account</Button>:<div/>}
            </div>
          </div>
      </div>
    );
  }
}

export default AccountSettings;
