import React, { useState, useEffect } from 'react';
import UserCard from '../components/UserCard';
import StatCard from '../components/StatCard';
import { Button, Container, Badge, Spinner } from 'react-bootstrap';
import Avatar from '../resources/finHubLogo.svg';
import { useLocation } from 'react-router-dom';
import axios from 'axios';
import axiosConfig from '../utils/axiosConfig';
import brokerSyncAxiosConfig from '../utils/brokerSyncAxiosConfig';
import { useCookies } from 'react-cookie';
import { useHistory } from "react-router-dom";
import PageNotFound from "./PageNotFound";
import Carousel, { Dots,slidesToShowPlugin, slidesToScrollPlugin } from '@brainhubeu/react-carousel';
import '@brainhubeu/react-carousel/lib/style.css';
import './ProfilePage.css';

function ProfilePage(props) {
    const [cookies, setCookie, removeCookie] = useCookies(['session']);
    const [username, setUsername] = useState('');
    const [avatar, setAvatar] = useState('baby');
    const [visibility, setVisibility] = useState('public');
    const [lastUpdate, setLastUpdate] = useState('n/a');
    const [overallChange, setOverallChange] = useState('n/a');
    const [dailyChange, setDailyChange] = useState('n/a');
    const [weeklyChange, setWeeklyChange] = useState('n/a');
    const [monthlyChange, setMonthlyChange] = useState('n/a');
    const [robinhoodStatus, setRobinhoodStatus] = useState('Disconnected');
    const [webullStatus, setWebullStatus] = useState('Disconnected');
    const [webullOverallChange, setWebullOverallChange] = useState('n/a');
    const [webullDailyChange, setWebullDailyChange] = useState('n/a');
    const [webullWeeklyChange, setWebullWeeklyChange] = useState('n/a');
    const [webullMonthlyChange, setWebullMonthlyChange] = useState('n/a');
    const [holdings, setHoldings] = useState(null);
    const [isSelf, setIsSelf] = useState(false);
    const [isFollowing, setIsFollowing] = useState(null);
    const [isProfileFound, setIsProfileFound] = useState(true);
    const [carouselVal, setCarouselVal] = useState(0);
    const [carouselLength, setCarouselLength] = useState(2);
    const [isLoadingSync, setIsLoadingSync] = useState(false);
    const [busy, setBusy] = useState(true);
    const [error, setError] = useState(null);
    const [showRobinhood, setShowRobinhood] = useState(false);
    const [showWebull, setShowWebull] = useState(false);
    const [carouselSlides, setCarouselSlides] = useState([]);
    const [shouldUpdateBrokers, setShouldUpdateBrokers] = useState(true);
    const [sampleData, setSampleData] = useState({
      id: 'No positions',
      children: []
    })
    const [webullSampleData, setWebullSampleData] = useState({
      id: 'No positions',
      children: []
    })
    const hostBase = process.env.NODE_ENV==='development'?'http://localhost:8080':'/api';

    let history = useHistory();
    // maybe add an overall?
    useEffect(
      () => {
        let carouselCount = 0;
        let carouselSlidesLocal = [];
        setShouldUpdateBrokers(true);
        if(showRobinhood){
          carouselCount += 1;
          carouselSlidesLocal.push((<StatCard sampleData={sampleData} broker={'robinhood'} status={robinhoodStatus} overallChange={overallChange} dailyChange={dailyChange} weeklyChange={weeklyChange} monthlyChange={monthlyChange}/>));
        }
        if(showWebull){
          carouselCount += 1;
          carouselSlidesLocal.push((<StatCard sampleData={webullSampleData} broker={'webull'} status={webullStatus} overallChange={webullOverallChange} dailyChange={webullDailyChange} weeklyChange={webullWeeklyChange} monthlyChange={webullMonthlyChange}/>));
        }
        if(carouselCount==0){
          setShouldUpdateBrokers(false);
        } else if (carouselCount==2) {
          // Pushing Expired card in the back
          if(robinhoodStatus!=='Connected') {
            let temp = carouselSlidesLocal[0];
            carouselSlidesLocal[0] = carouselSlidesLocal[1];
            carouselSlidesLocal[1] = temp;
          }
        }
        setCarouselLength(carouselCount);
        setCarouselSlides(carouselSlidesLocal);
      },[showWebull,showRobinhood, sampleData, overallChange, dailyChange, weeklyChange, monthlyChange, webullOverallChange, webullDailyChange, webullWeeklyChange, webullMonthlyChange]
    );
    useEffect(
        () => {
            // check if valid name with api, otherwise profile could not be found
            // only if valid username
            // console.log(props.match.params['username'], cookies['user']);
            if (props.match.params['username'] === cookies['user']) {
                setIsSelf(true);
            }
            axios.get(`${hostBase}/getProfile/${props.match.params['username']}`, {timeout: 1000})
                .then(response => {
                    console.log('the response is:', response);
                    if(response.data.positions && response.data.robinhoodStatus!=='Disconnected' ) {
                      setShowRobinhood(true);
                      response.data.positions.map((position) => {
                        let ok = sampleData;
                        ok.children.push({id:position.stockName, value:parseFloat(position.percentage)});
                        setSampleData(ok);
                      });
                    } else {
                      setShowRobinhood(false);
                    }
                    if(response.data.webullPositions && response.data.webullStatus!=='Disconnected') {
                      setShowWebull(true);
                      let webullPositionMap = {};
                      response.data.webullPositions.map((position) => {
                        if (position.stockName in webullPositionMap){
                          webullPositionMap[position.stockName] = (parseFloat(webullPositionMap[position.stockName]) + parseFloat(position.percentage)).toString();
                        } else {
                          webullPositionMap[position.stockName] = position.percentage;
                        }
                      });

                      for (const stockTicker in webullPositionMap){
                        let ok = webullSampleData;
                        ok.children.push({id:stockTicker, value:webullPositionMap[stockTicker]});
                        setWebullSampleData(ok);
                      }
                    } else {
                      setShowWebull(false);
                    }
                    setWebullStatus(response.data.webullStatus);
                    setRobinhoodStatus(response.data.robinhoodStatus);
                    setUsername(response.data.username);
                    setAvatar(response.data.avatar);
                    setVisibility(response.data.visibility);

                    if(response.data.lastUpdate||response.data.webullLastUpdate) {
                      let lastUpdatedDate = new Date();
                      if(response.data.lastUpdate&&response.data.webullLastUpdate){
                        const robinDate = new Date(response.data.lastUpdate);
                        const webullDate = new Date(response.data.webullLastUpdate);
                        if(robinDate>=webullDate){
                          lastUpdatedDate = new Date(response.data.lastUpdate);
                        } else {
                          lastUpdatedDate = new Date(response.data.webullLastUpdate);
                        }
                      } else if(response.data.lastUpdate){
                        lastUpdatedDate = new Date(response.data.lastUpdate);
                      } else if(response.data.webullLastUpdate){
                        lastUpdatedDate = new Date(response.data.webullLastUpdate);
                      }

                      let today = new Date();
                      let minutesAgo = Math.abs(today - lastUpdatedDate) / 1000;
                      minutesAgo = Math.floor(minutesAgo/60);
                      let displayedMinutes = '0';
                      if(minutesAgo<1){
                        displayedMinutes = '0';
                      } else if(minutesAgo > 120){
                        displayedMinutes = '>120';
                      } else {
                        displayedMinutes = minutesAgo;
                      }
                      setLastUpdate(displayedMinutes);
                    }

                    if (response.data.overallChange==null) {
                        setOverallChange('n/a');
                    }
                    else if (response.data.overallChange > 0) {
                        setOverallChange(`+${response.data.overallChange}%`);
                    }
                     else {
                        setOverallChange(`${response.data.overallChange}%`);
                    }

                    if (response.data.dailyChange==null) {
                        setDailyChange('n/a');
                    }
                    else if (response.data.dailyChange >= 0) {
                        setDailyChange(`+${response.data.dailyChange}%`);
                    } else {
                        setDailyChange(`${response.data.dailyChange}%`);
                    }
                    if (response.data.weeklyChange==null) {
                        setWeeklyChange('n/a');
                    }
                    else if (response.data.weeklyChange >= 0) {
                        setWeeklyChange(`+${response.data.weeklyChange}%`);
                    } else {
                        setWeeklyChange(`${response.data.weeklyChange}%`);
                    }
                    if (response.data.monthlyChange==null) {
                        setMonthlyChange('n/a');
                    }
                    else if (response.data.monthlyChange >= 0) {
                        setMonthlyChange(`+${response.data.monthlyChange}%`);
                    } else {
                        setMonthlyChange(`${response.data.monthlyChange}%`);
                    }





                    if (response.data.webullOverallChange==null) {
                        setWebullOverallChange('n/a');
                    }
                    else if (response.data.webullOverallChange > 0) {
                        setWebullOverallChange(`+${response.data.webullOverallChange}%`);
                    }
                     else {
                        setWebullOverallChange(`${response.data.webullOverallChange}%`);
                    }

                    if (response.data.webullDailyChange==null) {
                        setWebullDailyChange('n/a');
                    }
                    else if (response.data.webullDailyChange >= 0) {
                        setWebullDailyChange(`+${response.data.webullDailyChange}%`);
                    } else {
                        setWebullDailyChange(`${response.data.webullDailyChange}%`);
                    }
                    if (response.data.webullWeeklyChange==null) {
                        setWebullWeeklyChange('n/a');
                    }
                    else if (response.data.webullWeeklyChange >= 0) {
                        setWebullWeeklyChange(`+${response.data.webullWeeklyChange}%`);
                    } else {
                        setWebullWeeklyChange(`${response.data.webullWeeklyChange}%`);
                    }
                    if (response.data.webullMonthlyChange==null) {
                        setWebullMonthlyChange('n/a');
                    }
                    else if (response.data.webullMonthlyChange >= 0) {
                        setWebullMonthlyChange(`+${response.data.webullMonthlyChange}%`);
                    } else {
                        setWebullMonthlyChange(`${response.data.webullMonthlyChange}%`);
                    }

                    setHoldings(response.data.holdings);
                }).catch(error => {
                    if (error.response && error.response.status === 400) {
                        console.log(`Profile ${props.match.params['username']} does not exist`);
                        setIsProfileFound(false);
                        setBusy(false);
                    } else{
                        setBusy(false);
                        setError(true);
                    }
                });

            const userId = cookies['userId'];
            const friendUsername = props.match.params['username'];

            if(userId){
              axiosConfig.get(`${hostBase}/isFriend`, {
                  params: {
                      id: userId,
                      friendUsername: friendUsername
                  }
              })
                  .then(response => {
                      if (response.data === true) {
                          setIsFollowing(true);
                      } else {
                          setIsFollowing(false);
                      }
                  }).catch(error => {
                      if (error.message === 'Unauthorized') {
                          console.log("Token expired");
                          history.push('/login');
                          history.go(0);
                      } else {
                          console.log('System error');
                          // show something
                          setBusy(false);
                          setError(true);
                      }
                  });
            } else {
              setBusy(false);
            }

        }, []
    )

    useEffect(
      () => {
        if(holdings!==null && isFollowing !==null) {
          setBusy(false);
        }
      },[holdings, isFollowing]
    );

    // TODO move these to some util file
    function onFollowEvent(e) {
        // if following atm, unfollow
        if (isFollowing) {
            axiosConfig.post(`${hostBase}/followUser`, { target: props.match.params['username'], userId: cookies['userId'], unfollow: true })
                .then(response => {
                    console.log("Successfully unfollowed", response);
                    setIsFollowing(false);
                }).catch(error => {
                    console.log('System error has occured');
                });
        } else {
            // start following
            axiosConfig.post(`${hostBase}/followUser`, { target: props.match.params['username'], userId: cookies['userId'], unfollow: false })
                .then(response => {
                    console.log("Successfully followed", response);
                    setIsFollowing(true);
                }).catch(error => {
                    console.log('System error has occured');
                });
        }
        //only after confirm 200 response setIsFollowing(!isFollowing);
        e.preventDefault();
    }


    function updatePositionsPerformance() {
      if(!isSelf) {
        return;
      }
      setIsLoadingSync(true);
      brokerSyncAxiosConfig.post(`${hostBase}/updatePerformanceHoldings`)
        .then(response => {
            setIsLoadingSync(false);
            window.location.reload();
            // last updated, statistics, positions
        })
        .catch(error => {
            setIsLoadingSync(false);
            console.log("failed to sync with given robinhood account");
      });
    }

    if(error) {
      return (
        <h2 style={{color: 'white', textAlign:'center'}}>
          {'Error has occured, please try again later'}
        </h2>
      );
    }
    if(busy) {
      return (
        <div>
          {/*<ClimbingBoxLoader
            color={"#9c6eff"}
            loading={busy}
            css={'display: block; margin: 0 auto;'}
          />
          */}
        </div>
      );
    }
    if (isProfileFound) {
        return (
            <div className="profile-container">
                <div className="first-level">
                    {/*Div for Last Updated:*/}
                    <div className="last-updated">
                        Last Updated: {(!lastUpdate||lastUpdate=='n/a') ? 'n/a' : `${lastUpdate} minutes ago`}
                    </div>
                    {/*Div for avatar with username*/}
                    <div className="avatar-with-username">
                        <img className="avatar-size" src={Avatar} />
                        <div> {username} </div>
                    </div>
                    {/*Div for Friend request button*/}
                    <div className='profile-button'>
                        {(isSelf)?<Button disabled={cookies['user']==null||isLoadingSync||!shouldUpdateBrokers} onClick={() => updatePositionsPerformance()} type="button" variant="primary">{'Update'}</Button>:<Button onClick={(e) => onFollowEvent(e)} disabled={cookies['user']==null} type="button" variant="primary">{(isFollowing) ? 'Following' : 'Follow'}</Button>}
                    </div>
                </div>
                {/*Div for awards*/}
                <div>
                {isLoadingSync&&<div style={{display:'flex', marginBottom:'10px'}}><Spinner style={{margin:'auto'}} animation="border" variant="success" /></div>}
                </div>
                {(showWebull||showRobinhood)&&<div className='carousel-container' style={{cursor:'grab'}}>
                  <Carousel plugins={[
                      'centered',
                      {
                        resolve: slidesToShowPlugin,
                        options: {
                         numberOfSlides: 4,
                        },
                      },
                      {
                        resolve: slidesToScrollPlugin,
                        options: {
                         numberOfSlides: 4,
                        },
                      },
                    ]}
                    value = {carouselVal}
                    slides={carouselSlides}
                    onChange={(value)=>setCarouselVal(value)}
                  >
                  </Carousel>
                </div>}
                <Dots value={carouselVal} onChange={(value)=>setCarouselVal(value)} number={carouselLength} />
                {(!showWebull&&!showRobinhood)&&<div style={{color:'white', margin:'auto'}}>{'No Brokers Synced with User'} </div>}
            </div>
        );
    } else {
        return <PageNotFound />;
    }
}
export default ProfilePage;
