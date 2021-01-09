import React, { useState, useEffect } from 'react';
import './UserSearchCard.css'
import Logo from '../resources/finHubLogo.svg';
import {Link} from "react-router-dom";

function UserSearchCard(props) {
  const [avatar, setAvatar] = useState(props.avatar)
  const [backgroundColor, setBackgroundColor] = useState('#3b3b3b')

  useEffect(
    () => {

    }
  )

  function navigateToProfilePage() {

  }


  return (
      <Link to={{ pathname: '/profile/' + props.username, state: { username: props.username}}} className='UserSearchCard' onMouseLeave={() =>setBackgroundColor('#3b3b3b')} onMouseEnter={() =>setBackgroundColor('#808080') } style={{textDecoration: 'inherit', color: 'inherit', background: backgroundColor, display: 'flex', margin: '10px',alignItems: 'center', flexDirection: 'row'}}>
        <div style={{paddingLeft: '10px'}}>
          <img alt="cur" src={Logo} width='40px' height='40px' style={{marginRight: '10px'}}></img>
        </div>
        <div>
          <h4>{props.username}</h4>
        </div>
      </Link>
  );
}


export default UserSearchCard;
