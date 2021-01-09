import axios from 'axios';
import Cookies from 'js-cookie';

const instance = axios.create({
    withCredentials: true
});

instance.interceptors.request.use(function (config) {
    let authToken = Cookies.get('Authorization');
    config.headers.common.Authorization = `Bearer ${authToken}`;
    config.timeout = 2000;
    return config;
}, function (error) {
    return Promise.reject(error);
}
);
instance.interceptors.response.use(
    res => res,
    err => {
        if (err.response.data.status == 401) {
            // deletes old cookies, page has to handle redirects
            console.log("oof token expired probs, deleting old token and redirecting to login page.");
            Cookies.remove('Authorization');
            Cookies.remove('userId');
            Cookies.remove('user');
        }
        throw new Error(err.response.data.message);
    }
)

export default instance;
