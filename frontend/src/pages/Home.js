import React, { useState, useEffect } from 'react';
import UserCard from '../components/UserCard';
import GlobalHome from '../components/GlobalHome.component';
import UserHome from '../components/UserHome.component';
import underconstruction from '../resources/underconstruction.png';
import './Home.css';
import { useHistory } from "react-router-dom";
import { useCookies } from 'react-cookie';
import Cookies from 'js-cookie';

function Home(props) {
  let history = useHistory();
  const [cookies, setCookie, removeCookie] = useCookies(['session']);
  const [log,setLog] = useState(false);
  const [loading,setIsLoading] = useState(true);

  useEffect(() => {
    // if(props.isLoggedIn && cookies['user']){
    //   history.push(`/profile/${cookies['user']}`);
    //   history.go(0);
    // }
    let authToken = cookies['Authorization'];

    if (!authToken) {
      console.log('user is not logged in');
      setLog(false);
      removeCookie('user');
    } else {
      console.log('user is logged in');
      setLog(true);
    }
    setIsLoading(false);
    },[]
  )

  if(loading) {
    return null;
  }
  if (log) {
    return ( <UserHome/> )
  } else {
    return ( <GlobalHome /> )
  }

}

export default Home;
