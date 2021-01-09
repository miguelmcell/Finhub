import React, { useState, useEffect } from 'react';
import { render } from "react-dom";
import {
  BrowserRouter as Router,
  Switch,
  Route,
  Link,
  Redirect
} from "react-router-dom";
import './index.css';
import AccountSettings from "./pages/AccountSettings";
import ProfilePage from "./pages/ProfilePage";
import PageNotFound from "./pages/PageNotFound";
import Home from "./pages/Home";
import 'bootstrap/dist/css/bootstrap.min.css';
import Header from "./components/Header";
import UserSearchResult from "./pages/UsersSearchResult";
import MyFriendsPage from "./pages/MyFriends";
import LoginPage from "./pages/LoginPage";
import RegistrationPage from "./pages/RegistrationPage";
import StatusPage from "./pages/StatusPage";
import { useCookies } from 'react-cookie';
import GlobalHome from './components/GlobalHome.component';

function App() {
  const [cookies, setCookie, removeCookie] = useCookies(['session']);
  const [isLoggedIn,setIsLoggedIn] = useState(false);

  function checkLoginStatus() {
    //TODO pass a loggedIn state
    let authToken = cookies['Authorization'];

    if (!authToken) {
      console.log('user is not logged in');
      setIsLoggedIn(false);
      removeCookie('user');
    } else {
      console.log('user is logged in');
      setIsLoggedIn(true);
    }
  }


  useEffect(
    () => {
        // check if valid name with api, otherwise profile could not be found
        // only if valid username
        // development production
        checkLoginStatus();
    },[]
  )

  return (
    <div>
      <Router>
        <div>
          <Header isLoggedIn={isLoggedIn} username={cookies['user']}/>
          <Switch>
            <Route exact path="/" render={(props) => (<Home isLoggedIn={isLoggedIn} username={cookies['user']}/> )}/>
            <Route exact path="/home" render={(props) => (<Home isLoggedIn={isLoggedIn} username={cookies['user']} /> )} />
            <Route exact path="/global" render={(props) => (<GlobalHome isLoggedIn={isLoggedIn} username={cookies['user']} /> )} />
            <Route exact path="/accountSettings" render={(props) => (<AccountSettings username={cookies['user']}/> )} />
            <Route exact path="/profile/:username" component={ProfilePage} />
            <Route exact path="/search" component={UserSearchResult} />
            <Route exact path="/friends" component={MyFriendsPage} />
            <Route exact path="/login" component={LoginPage} />
            <Route exact path="/register" component={RegistrationPage} />
            <Route exact path="/status" component={StatusPage} />
            <Route component={PageNotFound} />
          </Switch>
        </div>
      </Router>
    </div>
  );
}

render(<App />, document.getElementById("root"));
