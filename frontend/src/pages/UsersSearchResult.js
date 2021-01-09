import React, { useState, useEffect } from 'react';
import UserSearchCard from '../components/UserSearchCard';
import { Button, Container, Badge } from 'react-bootstrap';
import Avatar from '../resources/finHubLogo.svg';
import ReactPaginate from 'react-paginate';
import './MyFriends.css';
import ArrowBackIosIcon from '@material-ui/icons/ArrowBackIos';
import ArrowForwardIosIcon from '@material-ui/icons/ArrowForwardIos';
import { useHistory } from "react-router-dom";
import queryString from 'query-string';
import axios from 'axios';
import './UserSearchResult.css';

function UserList(props) {
    const [userNodes, setUserNodes] = useState();

    useEffect(
        () => {
            setUserNodes(props.data.map(function (user, index) {
                return <UserSearchCard username={user.username} avatar={user.avatar} key={index}></UserSearchCard>
            }));
        },
        [props.data]
    )

    return (
        <div>
            {userNodes}
        </div>
    )
}

function UserSearchResult(props) {
    const [nameSearched, setNameSearched] = useState('');
    const [data, setData] = useState([]);
    const [displayedData, setDisplayedData] = useState([]);
    const [offset, setOffset] = useState(0);
    const [curPage, setCurPage] = useState(0);
    const [pageCount, setPageCount] = useState(10);
    const [previousLabel, setPreviousLabel] = useState('previous');
    const [nextLabel, setNextLabel] = useState('next');

    const [busy, setBusy] = useState(true);
    const [error, setError] = useState(null);

    let history = useHistory();
    const totalInPage = 6;
    const hostBase = process.env.NODE_ENV==='development'?'http://localhost:8080':'/api';

    useEffect(
        () => {
            const values = queryString.parse(props.location.search);
            if (values.q == null || values.q == '') {
                history.push('/home');
            } else if (values.q !== nameSearched) {
                setBusy(true);
                setNameSearched(values.q);
                // call search query for values.q
                console.log('searching for name:', values.q);
                axios.get(`${hostBase}/search/${values.q}`, {timeout: 5000})
                    .then(response => {
                        console.log(`search ${values.q} res`, response);
                        setData(response.data);
                        setPageCount(Math.ceil(response.data.length / totalInPage));
                        setDisplayedData(response.data.slice(curPage * totalInPage, curPage * totalInPage + totalInPage));
                        setBusy(false);
                    }).catch(error => {
                        setBusy(false);
                        setError(true);
                    });
            }
        }
    );
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
      )
    }

    return (
        <div>
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', color: 'white' }}>
                <h1> Results for {nameSearched} </h1>
                <UserList data={displayedData} />
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', color: 'white' }}>
                <div style={{ position: 'absolute', bottom: '5px' }}>
                  <ReactPaginate
                      activeClassName={'item page-active '}
                      breakClassName={'item break-me '}
                      breakLabel={'...'}
                      containerClassName={'pagination pagination-search'}
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
        </div>
    );
}

export default UserSearchResult;
