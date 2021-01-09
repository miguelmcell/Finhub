import React, { useState, useEffect } from 'react';
import UserCard from '../components/UserCard';
import { Button, Container, Badge } from 'react-bootstrap';
import Avatar from '../resources/finHubLogo.svg';
import { useLocation } from 'react-router-dom';
import Registration from '../components/Registration';

function RegistrationPage(props) {
  const [error, setError] = useState(null);
  const [loginBusy, setLoginBusy] = useState(true);

  useEffect(
    () => {
        // check if valid name with api, otherwise profile could not be found
        // only if valid username
        // setUsername(props.match.params['username']);
    }
  );

  if(error && error !=='Incorrect confirmation password' && error !=='Username already exists' && error !=='Password is too Weak' && error !=='Username must be longer than 4 characters' && error !=='Username must be less than 15 characters' && error !=='Profanity detected in username' && error !=='Username cannot contain spaces') {
    return (
      <h2 style={{color: 'white', textAlign:'center'}}>
        {(error==='unknown')?'Error has occured, please try again later':error}
      </h2>
    );
  }
  return (
    <div style={{display: 'flex', color: 'white',alignItems: 'center', justifyContent: 'center',flexDirection:'column'}}>
      {(error && error ==='Incorrect confirmation password')&&<div style={{color:'red'}}>{error}</div>}
      {(error && error ==='Username already exists')&&<div style={{color:'red'}}>{error}</div>}
      {(error && error ==='Username must be less than 15 characters')&&<div style={{color:'red'}}>{error}</div>}
      {(error && error ==='Username must be longer than 4 characters')&&<div style={{color:'red'}}>{error}</div>}
      {(error && error ==='Profanity detected in username')&&<div style={{color:'red'}}>{error}</div>}
      {(error && error ==='Username cannot contain spaces')&&<div style={{color:'red'}}>{error}</div>}
      <Registration setError={setError}/>
    </div>
  );
}
export default RegistrationPage;
