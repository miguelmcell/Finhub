import React, { useState, useEffect  } from 'react';
import { Button, Form, FormControl, Nav, Navbar, NavItem, MenuItem, NavDropdown, Modal } from 'react-bootstrap';
import Logo from '../resources/finHubLogo.svg';
import { useHistory } from "react-router-dom";
import { useCookies } from 'react-cookie';
import Cookies from 'js-cookie';
import './Header.css';

function Header(props) {
    const [searchInput, setSearchInput] = useState('');
    const [singleSelections, setSingleSelections] = useState([]);
    const [cookies, setCookie, removeCookie] = useCookies(['session']);
    let history = useHistory();

    useEffect(() => {
      // do something every time username changes, empty array only updates in mount time

      }
    )

    function handleOnChangeUserSearchForm(e) {
      setSearchInput(e.target.value);
      {/*query only if certain string length query entire dictionary once and store*/}
    }

    function onSubmit(e) {
      e.preventDefault();
      if(searchInput!='' && searchInput.length>=3){
        let query = "?q="+searchInput.replace(/[^\w\s]/gi, "");
        history.push('/search'+query);
      }
    }

    function signOutEvent(e) {
      e.preventDefault();
      // wipe out cookies
      Cookies.remove('Authorization');
      Cookies.remove('userId');
      Cookies.remove('user');
      // go to home page
      history.push('/home');
      history.go(0);
    }


    if(props.isLoggedIn) {
      return (
          <Navbar collapseOnSelect expand="lg" variant="dark" style={{backgroundColor:'#2E2E2E', marginBottom: '20px'}}>
            <Navbar.Brand href="/home">
              <img
                src={Logo}
                width="40"
                height="40"
                className="d-inline-block align-top"
              />
            <div className='logo'>FinHub</div>
            </Navbar.Brand>
            <Navbar.Toggle aria-controls="responsive-navbar-nav" />
            <Navbar.Collapse id="responsive-navbar-nav">
              <Nav className="nav-header-items mr-auto">
                <NavDropdown style={{fontWeight: 'bold'}} title={cookies['user']} id="collasible-nav-dropdown">
                    <NavDropdown.Item href={"/profile/"+cookies['user']}>View Profile</NavDropdown.Item>
                    <NavDropdown.Divider />
                    <NavDropdown.Item href="/accountSettings">Account Settings</NavDropdown.Item>
                    <NavDropdown.Divider />
                    <NavDropdown.Item onClick={(e)=>{signOutEvent(e)}}>Sign Out</NavDropdown.Item>
                </NavDropdown>
                <Nav.Link style={{}} href="/friends">Friends</Nav.Link>
                <Nav.Link style={{}} href="/global">Global</Nav.Link>
              </Nav>
              <Nav>
                <Form onSubmit={(e) => onSubmit(e)} inline>
                  <FormControl autoComplete="off" onChange={(e) => handleOnChangeUserSearchForm(e)} type="text" placeholder="Search users" className="mr-sm-2"/>
                  <Button onClick={(e) => onSubmit(e)} type="button" variant="outline-success" className="d-none d-sm-block">Search</Button>
                </Form>
              </Nav>
            </Navbar.Collapse>
          </Navbar>
      );
    } else {
      return (
        <Navbar collapseOnSelect expand="lg" variant="dark" style={{backgroundColor:'#2E2E2E', marginBottom: '20px'}}>
          <Navbar.Brand href="/home">
            <img
              src={Logo}
              width="40"
              height="40"
              className="d-inline-block align-top"
            />
          <div className='logo'>FinHub</div>
          </Navbar.Brand>
          <Navbar.Toggle aria-controls="responsive-navbar-nav" />
          <Navbar.Collapse id="responsive-navbar-nav">
            <Nav className="mr-auto">
              <NavDropdown title='Account' id="collasible-nav-dropdown">
                  <NavDropdown.Item href='/login'>Login</NavDropdown.Item>
                  <NavDropdown.Divider />
                  <NavDropdown.Item href="/register">Register</NavDropdown.Item>
              </NavDropdown>
              <Nav.Link style={{}} href="/global">Global</Nav.Link>
            </Nav>
            <Nav>
              <Form onSubmit={(e) => onSubmit(e)} inline>
                <FormControl autoComplete="off" onChange={(e) => handleOnChangeUserSearchForm(e)} type="text" placeholder="Search users" className="mr-sm-2"/>
                <Button onClick={(e) => onSubmit(e)} type="button" variant="outline-success">Search</Button>
              </Form>
            </Nav>
          </Navbar.Collapse>
        </Navbar>
      );
    }

}

export default Header;
