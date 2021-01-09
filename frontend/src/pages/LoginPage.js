import React, { useState, useEffect } from 'react';
import UserCard from '../components/UserCard';
import { Button, Container, Badge } from 'react-bootstrap';
import Avatar from '../resources/finHubLogo.svg';
import { useLocation } from 'react-router-dom';
import Login from '../components/Login';

function LoginPage(props) {
  const [error, setError] = useState(null);
  const [loginBusy, setLoginBusy] = useState(true);

  useEffect(
    () => {
        // check if valid name with api, otherwise profile could not be found
        // only if valid username
        // setUsername(props.match.params['username']);
    }
  );

  if(error && error !=='Invalid Login Credentials') {
    return (
      <h2 style={{color: 'white', textAlign:'center'}}>
        {(error==='unknown')?'Error has occured, please try again later':error}
      </h2>
    );
  }
  return (
    <div style={{display: 'flex', color: 'white',alignItems: 'center', justifyContent: 'center', flexDirection:'column'}}>
      {(error && error ==='Invalid Login Credentials')&&<div className='error-message'>Invalid Credentials</div>}
      <Login setError={setError} setLoginBusy={setLoginBusy}/>
      {/*<Registration setError={setError}/>*/}
    </div>
  );
}
export default LoginPage;
