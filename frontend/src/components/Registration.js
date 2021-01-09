import React, { useState,useEffect } from 'react';
import axios from 'axios';
// import {Form} from 'react-bootstrap';
import './Registration.css';
import { Button, Container, Badge } from 'react-bootstrap';
import { useHistory } from "react-router-dom";
import { useCookies } from 'react-cookie';

function Registration(props) {
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirmation, setPasswordConfirmation] = useState('');
  const [registrationErrors, setRegistrationErrors] = useState('');
  const [isPasswordValid, setIsPasswordValid] = useState(false);
  const [passwordColor, setPasswordColor] = useState('white');
  const [isFormValid, setIsFormValid] = useState(false);
  const [cookies, setCookie, removeCookie] = useCookies(['session']);
  const [isUsernameValid, setIsUsernameValid] = useState(false);
  const strongRegex = new RegExp("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%\^&\*])(?=.{8,})");
  const mediumRegex = new RegExp("^(((?=.*[a-z])(?=.*[A-Z]))|((?=.*[a-z])(?=.*[0-9]))|((?=.*[A-Z])(?=.*[0-9])))(?=.{6,})");
  let history = useHistory();

  useEffect(
    () => {
        setIsFormValid(isPasswordValid && username && password && passwordConfirmation && isUsernameValid);
    },[isPasswordValid,username,password,passwordConfirmation, isUsernameValid]
  );

  function onSubmit(e) {
      axios.post("/api/signUp", {
          username: username,
          email: email,
          password: password,
          passwordConfirmation: passwordConfirmation
      },
      { withCredentials: true, timeout: 1000 }
      ).then(response => {
        // response.data.data="OK", response.data.status=200,statusText=""
        console.log("registration res", response.status);
        axios.post("/api/authenticate", {
            username: username,
            password: password,
            keepMeSignedIn: true
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
            props.setError('Unable to contact server');
          }
        })
      }).catch((error) => {
        console.log("registration error", error);
        if(error.response) {
          console.log(error.response.data);
          props.setError(error.response.data);
        } else {
          props.setError('unknown');
        }
      })
      e.preventDefault();
  }
  function setAndValidatePassword(e){
    if(strongRegex.test(e.target.value)) {
      setIsPasswordValid(true);
      setPasswordColor('green');
    } else if(mediumRegex.test(e.target.value)) {
      setIsPasswordValid(true);
      setPasswordColor('orange');
    } else {
      setPasswordColor('red');
      setIsPasswordValid(false);
    }
    setPassword(e.target.value);
  }

  function onBlurValidateFormat(e) {
    setUsername(e.target.value);
    const value = e.target.value;
    const regex = /^[0-9a-zA-Z(\-)]+$/; //this will admit letters, numbers and dashes
    if (value.match(regex) || value === "") {
      //Show an error message or put a warning text under the input and set flag to prevent form submit
      setIsUsernameValid(true);
    } else {
      setIsUsernameValid(false);
    }
  }

  return (
    <div className='registration-container'>
      <form onSubmit={(e) => onSubmit(e)}>
        <div className='registration-row'>
          <div>{'Email: '}</div>
          <input type="email" name="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </div>
        <div className='registration-row'>
          <div>{'Username: '}</div>
          <input type="username" name="username" placeholder="Username" value={username} onChange={(e) => onBlurValidateFormat(e)} required />
        </div>
        <div className='registration-row'>
          <div style={{color: passwordColor}}>{'Password:'}</div>
          <input type="password" name="password" placeholder="Password" value={password} onChange={(e) => setAndValidatePassword(e)} required />
        </div>
        <div className='registration-row'>
          <div>{'Confirm Password: '}</div>
          <input type="password" name="password_confirmation" placeholder="Password confirmation" value={passwordConfirmation} onChange={(e) => setPasswordConfirmation(e.target.value)} required />
        </div>
        <div className='registration-button'>
          <Button disabled={!isFormValid} variant="primary" type="submit">Register</Button>
        </div>
      </form>
    </div>
  );
}

export default Registration;
