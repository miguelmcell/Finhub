import React, { useState,useEffect } from 'react';
import { useHistory } from "react-router-dom";
import { useCookies } from 'react-cookie';
import axiosConfig from '../utils/axiosConfig';
import axios from 'axios';
import { Button, Container, Badge } from 'react-bootstrap';
import './Login.css';

function Login(props) {
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [keepMeSignedIn, setKeepMeSignedIn] = useState('false');
  const [loginErrors, setLoginErrors] = useState('');
  const [cookies, setCookie, removeCookie] = useCookies(['session']);
  const hostBase = process.env.NODE_ENV==='development'?'http://localhost:8080':'/api';
  let history = useHistory();
  // TODO invalid signin will cause the page to freak out
  useEffect(
    () => {
      axiosConfig.get(`${hostBase}/isAuthenticated`, {timeout: 1000})
      .then(response => {
        console.log("user is already authenticate", response);
        history.push('/home');
        history.go(0);
      })
      .catch(error => {
        if(error.message==='Network Error') {
          console.log("Unable to communicate with backend");
          props.setError('Unable to contact server, please try again later');
          props.setLoginBusy(false);
        } else {
          // props.setError('Unable to contact server, please try again later');
          props.setLoginBusy(false);
        }
      });
    }, []
  )

  function onSubmit(e) {
      // call registration endpoint
      // TODO ADD LOADING ANIMATION WHILE WAITING FOR LOGIN/SIGNUP response
      // TODO ADD USERNAME AND PASSWORD CONFIRMATION TO BACKEND MODEL
      axios.post(`${hostBase}/authenticate`, {
          username: username,
          password: password,
          keepMeSignedIn: keepMeSignedIn
      },
      { withCredentials: true }
      ).then(response => {
        console.log("login res", response);
        if(response.status==200) {
          // TODO set secure to true once https is active
          setCookie('user', username, {httpOnly: false, sameSite: 'strict'});
          setCookie('userId', response.data.userId, {httpOnly: false, sameSite: 'strict'});
          setCookie('Authorization', response.data.token,{httpOnly: false, sameSite: 'strict'});
          history.push('/home');
          history.go(0);
          // handle the login
          // push user to profile
        }
      }).catch(error => {
        if(error.message==='Network Error') {
          console.log("Unable to communicate with backend");
          props.setError('Unable to contact server, please try again later');
        } else {
          console.log('System error has occured');
          props.setError('Invalid Login Credentials');
        }
      })
      e.preventDefault();
  }

  return (
    <div className='login-container'>
      <form onSubmit={(e) => onSubmit(e)}>
        <div className='login-username-row'>
          <div className='login-column'>{'Username: '}</div>
          <input className='login-column' type="username" name="username" placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} required />
        </div>
        <div className='login-password-row'>
          <div>{'Password: '}</div>
          <input type="password" name="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </div>
        <div className='login-button-row'>
          <Button variant="primary" type="submit">Login</Button>
        </div>
      </form>
    </div>
  );
}

export default Login;
