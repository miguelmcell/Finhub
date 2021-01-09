import React, { useState, useEffect } from 'react';
import axios from 'axios';

// props.name,props.status true/false
function StatusComponent(props) {
  return (
    <h3>
      {props.name}: <div style={props.status ? {color:'lime', display:'inline'} : {color:'red', display:'inline'}}>{(props.status) ? 'Online':'Offline'}</div>
    </h3>
  );
}
export default StatusComponent;
