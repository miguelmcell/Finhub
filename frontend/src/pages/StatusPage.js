import React, { useState, useEffect } from 'react';
import axios from 'axios';
import StatusComponent from '../components/StatusComponent';

function StatusPage(props) {
  const [backendStatus, setBackendStatus] = useState(null);
  const [robinhoodStatus, setRobinhoodStatus] = useState(null);
  const [webullStatus, setWebullStatus] = useState(false);
  const [isBusy, setisBusy] = useState(true);

  useEffect(
    () => {
      // ! use axiosConfig when wanting to pass auth token
      // !use ID since they'll know other people's IDs
      axios.get("/api/testStatus", {timeout: 1000})
      .then(response => {
        console.log("Backend is online");
        setBackendStatus(true);
      })
      .catch(error => {
        console.log('Unable to contact backend');
        setBackendStatus(false);
      });

      axios.get("/api/testRobinhoodStatus", {timeout: 1000})
      .then(response => {
        console.log("robinhood API is online");
        setRobinhoodStatus(true);
      })
      .catch(error => {
        console.log('Unable to contact robinhood API');
        setRobinhoodStatus(false);
      });

      axios.get("/api/testWebullStatus", {timeout: 1000})
      .then(response => {
        console.log("webull API is online");
        setWebullStatus(true);
      })
      .catch(error => {
        console.log('Unable to contact webull API');
        setWebullStatus(false);
      });
    }, []
  )
  useEffect(
    () => {
      if(backendStatus!=null && robinhoodStatus!=null && webullStatus!=null){
        setisBusy(false);
      }
    }, [backendStatus,robinhoodStatus,webullStatus]
  )
  if(isBusy) {
    return (
      <h1 style={{color: 'white', textAlign: 'center'}}>
        Loading Right Up
      </h1>
    );
  } else {
    return (
      <div style={{color: 'white', textAlign: 'center'}}>
        <h1>Status Page</h1>
        <StatusComponent name='Backend' status={backendStatus}/>
        <StatusComponent name='Robinhood API' status={robinhoodStatus}/>
        <StatusComponent name='Webull API' status={webullStatus}/>
      </div>
    );
  }
}
export default StatusPage;
