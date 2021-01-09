import React, { useState, useEffect } from 'react';
import UserCard from '../components/UserCard';

function PageNotFound() {

  useEffect(
    () => {
      // console.log("ok");
    }
  )

  return (
    <div style={{display: 'flex', flexDirection: 'column', alignItems: 'center', color: 'white'}}>
      Page Not Found 😔
    </div>
  );
}

export default PageNotFound;
