import React, { useState } from 'react';
import CardBackground from './CardBackground';
import './UserCard.css';
import { Container } from 'react-bootstrap';
import Logo from '../resources/finHubLogo.svg';
import Crown from '../resources/crown.png';
import Jester from '../resources/jester.png';
import {Link} from "react-router-dom";

function UserCard(props) {
  const [username, setUsername] = useState(props.username);
  const [percentage, setPercentage] = useState(props.percentage)
  const [category, setCategory] = useState(props.category)


  return (
    <Link className='user-card' to={{pathname: '/profile/'+username, state: {username: username}}}>
      <div className='category-text'>
        {category}
      </div>
      <div className='card-background border'>
        <img alt="cur" className='hat' src={props.loser?Jester:Crown}></img>
        <img alt="cur" className='profile-pic' src={Logo}></img>
        <div className="" className='username-text'>
          {username}
        </div>
        <div className={props.loser?'percentage-text-loser':'percentage-text-winner'}>
          {percentage}
        </div>
      </div>
    </Link>
  );
}

export default UserCard;
