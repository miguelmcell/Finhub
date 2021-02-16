import functools
import datetime
from os import error
import robin_stocks as robinhood
import calendar
import time
import sys
from flask import (
    Blueprint, g, request, session, url_for
)
from flask_cors import cross_origin
from werkzeug.security import check_password_hash, generate_password_hash

daily_performance_url = 'https://api.robinhood.com/historical/portfolio_v2/live/?account_number=' \
                        '{acc_number}&from={cur_time}&span=day'
weekly_performance_url = 'https://api.robinhood.com/portfolios/historicals/{acc_number}/?interval=day&span=week'
monthly_performance_url = 'https://api.robinhood.com/portfolios/historicals/{acc_number}/?interval=hour&span=month'
overall_performance_url = 'https://api.robinhood.com/historical/portfolio_v2/live/?account_number=' \
                          '{acc_number}&from&span=all'

bp = Blueprint('robinhoodRepository', __name__, url_prefix='/robinhoodRepository')


@bp.route('/login', methods=['POST'])
def login():
    if not ('username' in request.json and 'password' in request.json):
        return 'Invalid Request', 400

    if 'mfa_code' in request.json:
        try:
            login = robinhood.login(request.json['username'],
                                request.json['password'],
                                mfa_code=request.json['mfa_code'],
                                store_session=False)
        except Exception as e:
            error_message = str(e)
            if 'Please enter a valid code.' in error_message:
                return 'Invalid code', 400
            elif 'Unable to log in with provided credentials' in error_message:
                return 'Unable to log in with provided credentials', 400
    else:
        login = robinhood.login(request.json['username'],
                                request.json['password'],
                                store_session=False)
    response = {}
    response['access_token'] = login['access_token']
    response['refresh_token'] = login['refresh_token']
    response['expires_in'] = login['expires_in']
    robinhood.logout()
    del login
    return response, 200


@bp.route('/getPerformances', methods=['GET'])
def get_overall_performance():
    if not ('Authorization' in request.headers):
        print('Bad Request, no authorization header')
        return 'Invalid Request', 400
    access_token = request.headers['Authorization'].split()[1].strip()
    rh_account_number = robinhood.profiles.load_account_profile(access_token, info='account_number')
    overall_url = overall_performance_url.format(acc_number=rh_account_number)
    # daily only one that considers after hours
    daily_url = daily_performance_url \
        .format(acc_number=rh_account_number, cur_time=calendar.timegm(time.gmtime()).__str__())
    weekly_url = weekly_performance_url.format(acc_number=rh_account_number)
    monthly_url = monthly_performance_url.format(acc_number=rh_account_number)

    today_response = robinhood.request_get(daily_url, 'regular', access_token=access_token)
    if today_response is None:
        today_after_hours = 0
    else:
        today_after_hours = (float(today_response['after_hour_percentage_return']) * 100) if ('after_hour_percentage_return' in today_response) else 0

    overall_result = float(
        '{:0.3}'.format(float(robinhood.request_get(overall_url, 'regular', access_token=access_token)['percentage_return']) * 100))
    daily_result = float('{:0.3}'.format(float(today_response['percentage_return']) * 100))
    weekly_result = float('{:0.3}'.format(
        (float(robinhood.request_get(weekly_url, 'regular', access_token=access_token)['total_return']) * 100) + today_after_hours))
    monthly_result = float('{:0.3}'.format(
        (float(robinhood.request_get(monthly_url, 'regular', access_token=access_token)['total_return']) * 100) + today_after_hours))
    print('Get overall is:', overall_result)
    return {
        'overall': overall_result,
        'daily': daily_result,
        'weekly': weekly_result,
        'monthly': monthly_result
    }


@bp.route('/getPositions', methods=['GET'])
def get_positions():
    if not ('Authorization' in request.headers):
        return 'Invalid Request', 400
    access_token = request.headers['Authorization'].split()[1].strip()

    all_positions = robinhood.account.build_holdings(access_token)
    response = []
    # used to calculate cash at the end
    total_percentage = 0.0
    for stock_pos in all_positions:
        total_percentage += float(all_positions[stock_pos]['percentage'])
        response.append({
            'stockName': stock_pos,
            'percentage': all_positions[stock_pos]['percentage'],
        })
    response.append({
        'stockName': 'cash',
        'percentage': '{:0.3}'.format(100 - total_percentage)
    })
    return {
        'positions': response,
    }


@cross_origin()
@bp.route('/status', methods=['GET'])
def getStatus():
    return ''
