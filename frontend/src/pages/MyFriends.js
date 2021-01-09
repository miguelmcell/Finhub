import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import UserCard from '../components/UserCard';
import ReactPaginate from 'react-paginate';
import './MyFriends.css';
import ArrowBackIosIcon from '@material-ui/icons/ArrowBackIos';
import ArrowForwardIosIcon from '@material-ui/icons/ArrowForwardIos';
import UserSearchCard from '../components/UserSearchCard';
import axiosConfig from '../utils/axiosConfig';
import axios from 'axios';
import { useCookies } from 'react-cookie';
import { useHistory } from "react-router-dom";
import Pagination from "react-pagination-list";
import underconstruction from '../resources/construction2.png';

function FriendList(props) {
    const [friendNodes, setFriendNodes] = useState();

    useEffect(
        () => {
            setFriendNodes(props.displayedData.map(function (friend, index) {
                return <UserSearchCard username={friend} avatar={'baby'} key={index}></UserSearchCard>
            }));
        }, [props.displayedData]
    )
    return (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            {friendNodes}
        </div>
    );
}

function MyFriends() {
    const [data, setData] = useState([]);
    const [displayedData, setDisplayedData] = useState([]);
    const [curPage, setCurPage] = useState(0);
    const [pageCount, setPageCount] = useState(10);
    const [previousLabel, setPreviousLabel] = useState('previous');
    const [nextLabel, setNextLabel] = useState('next');
    const [cookies, setCookie, removeCookie] = useCookies(['session']);
    const [error, setError] = useState(null);

    const [busy, setBusy] = useState(true);

    let history = useHistory();
    const totalInPage = 3;

    const hostBase = process.env.NODE_ENV==='development'?'http://localhost:8080':'/api';

    useEffect(
        () => {
            axiosConfig.get(`${hostBase}/getFriends/${cookies['userId']}`)
                .then(response => {
                    let friendNameList = []
                    response.data.map((friend, index) => {
                        friendNameList.push(friend.username);
                    });
                    setCurPage(0);
                    setData(friendNameList);
                    setPageCount(Math.ceil(friendNameList.length / totalInPage));
                    setDisplayedData(data.slice(curPage * totalInPage, curPage * totalInPage + totalInPage));
                    setBusy(false);
                }).catch(error => {
                    if (error.message === 'Unauthorized') {
                        console.log("Token expired");
                        history.push('/login');
                        history.go(0);
                    } else {
                        console.log('System error');
                        // TODO show errors
                        setBusy(false);
                        setError(true);
                    }
                });
        }, []
    )
    useEffect(
        () => {
            //update displayed list
            //console.log("hello", data.slice(curPage, curPage + totalInPage));
            setDisplayedData(data.slice(curPage * totalInPage, curPage * totalInPage + totalInPage));
        }, [curPage, data, pageCount]
    )

    if(error) {
      return (
        <h2 style={{color: 'white', textAlign:'center'}}>
          {'Error Has Occured, please try again later'}
        </h2>
      );
    }
    if(busy) {
      return (
        <div></div>
      );
    }

    return (
        <div className='my-friends-container'>
            <h2> Friends </h2>
            <div className="analytical-part-of-friendspage" style={{display: 'flex', flexDirection: 'column', position: 'relative'}}>
              <div  style={{display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
                {"Nasdaw looking scrolling quotes for friend's gains/losses"}
              </div>
              <div style={{ display: 'flex', flexDirection: 'row'}}>
                <UserCard category='Top Daily Winner' username='test' percentage='+2.48%' />
                <UserCard category='Top Overall Winner' username='test' percentage='+5.38%' />
              </div>
              <div>
                <div style={{ fontWeight: 'bold', fontSize: '20px', color: 'white', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                  Top Daily Loser
                </div>
                <div style={{ display: 'flex', flexDirection: 'row', paddingLeft: '15px', marginTop: '-30px', marginLeft: '32%' }}>
                  <div style={{ transform: 'rotate(270deg)' }}>
                    <UserCard category='' username='test' percentage='-22.48%' />
                  </div>
                </div>
              </div>
              <img src={underconstruction} alt="UnderConstruction" style={{position:'absolute', width:'100%', height:'100%'}}/>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', paddingLeft: '0%' }}>
                <FriendList displayedData={displayedData} />
                {data==0&&<div>No Friends :(</div>}
                <div style={{ margin: '20px' }}></div>
                {/*
                 <Pagination
                    data={data}
                    pageSize={2}
                    renderItem={(item, key) => {
                        return (
                            <p key={key}>{item}</p>
                        );
                    }}
                    />
                 */}

                  <ReactPaginate
                      activeClassName={'item page-active'}
                      breakClassName={'item break-me '}
                      breakLabel={'...'}
                      containerClassName={'pagination pagination-friends'}
                      disabledClassName={'disabled-page'}
                      marginPagesDisplayed={2}
                      nextClassName={"item next "}
                      nextLabel={<ArrowForwardIosIcon style={{ fontSize: 18, width: 100, height: 40 }} />}
                      onPageChange={(e) => setCurPage(e.selected)}
                      pageCount={pageCount}
                      pageClassName={'item pagination-page '}
                      pageRangeDisplayed={1}
                      previousClassName={"item previous"}
                      previousLabel={<ArrowBackIosIcon style={{ fontSize: 18, width: 100, height: 40 }} />}
                  />

            </div>
        </div>
    );
}

export default MyFriends;
