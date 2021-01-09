import React, { useState } from 'react';
import CardBackground from './CardBackground';
import './UserCard.css';
import { Container } from 'react-bootstrap';
import Logo from '../resources/finHubLogo.svg';
import Crown from '../resources/crown.png';
import Jester from '../resources/jester.png';
import {Link} from "react-router-dom";
import UserCard from '../components/UserCard';

function UserHome(props) {


  return (
    <div style={{display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
      <div style={{color: 'white'}}>Friends Leaderboards</div>
      <UserCard category='Top Overall Winner' username='User1' percentage='+2.38%'/>
      <div className='second-row'>
        <UserCard category='Top Daily Winner' username='User2' percentage='+2.48%'/>
        <UserCard category='Top Weekly Winner' username='User3' percentage='+5.38%'/>
        <UserCard category='Top Monthly Winner' username='User4' percentage='+2.38%'/>
      </div>
      <div>
        <div className='daily-loser-row'>Top Daily Losers</div>
        <div className='loser-card-row'>
          <div className='loser-card loser-one'>
            {/*Get rid of crowns for losers and replace with jesters using some confition or category*/}
            <UserCard loser category='' username='User5' percentage='-10.78%'/>
          </div>
          <div className='loser-card loser-two'>
            <UserCard loser category='' username='User6' percentage='-4.18%'/>
          </div>
          <div className='loser-card loser-three'>
            <UserCard loser category='' username='User7' percentage='-12.48%'/>
          </div>
        </div>
      </div>
    </div>
  );
}

export default UserHome;
