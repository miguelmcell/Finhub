import functools
import datetime
import calendar
import time
import sys
from datetime import date
import requests
from flask import (
    Blueprint, g, request, session, url_for
)
from flask_cors import cross_origin
from werkzeug.security import check_password_hash, generate_password_hash
from webull import webull

performance_url = 'https://tradeapi.webullfintech.com/api/trade/v2/profitloss/{acc_number}/account/summary?startDate={start_date}&endDate={end_date}'
overall_performance_url = 'https://tradeapi.webullfintech.com/api/trade/v2/profitloss/{acc_number}/account/summary?startDate=2018-05-01&endDate={end_date}'
portfolio_url = 'https://tradeapi.webullbroker.com/api/trade/v2/home/{account_id}'
refresh_url = 'https://userapi.webull.com/api/passport/refreshToken?refreshToken={refresh}'

bp = Blueprint('webullRepository', __name__, url_prefix='/webullRepository')


@bp.route('/login', methods=['POST'])
def login():
    if not ('email' in request.json and 'password' in request.json and 'mfa' in request.json):
        return 'Invalid Request', 400
    wb = webull()
    try:
        login_response = wb.login(request.json['email'], request.json['password'], 'finhub', request.json['mfa'])
    except Exception as e:
        return 'exception while logging in', 500
    if 'msg' in login_response and 'Incorrect password or username.' in login_response['msg']:
        return login_response['msg'], 400
    print('login_response:', login_response)
    account_id = wb.get_account_id()
    response = {'access_token': login_response['accessToken'], 'account_id': account_id,
                'refresh_token': login_response['refreshToken'], 'expirationTime': login_response['tokenExpireTime']}
    return response


@bp.route('/getMfa', methods=['GET'])
def get_mfa():
    email = request.args.get('email')
    if email is None:
        return 'Invalid Request', 400
    wb = webull()
    wb.get_mfa(email)
    return "ok"


@bp.route('/refresh', methods=['POST'])
def refresh():
    if not ('refreshToken' in request.json) and not ('accessToken' in request.json):
        return 'Invalid Request', 400
    data = {'refreshToken': request.json['refreshToken']}
    refresh_url = refresh_url.format(refresh=request.json['refreshToken'])
    refresh_response = requests.post(refresh_url, json=data, headers={'access_token': request.json['accessToken']})
    refresh_response = refresh_response.json()
    print('refresh response:', refresh_response)
    response = {'access_token': refresh_response['accessToken'], 'refreshToken': refresh_response['refreshToken'],
                'expirationTime': refresh_response['tokenExpireTime']}
    return response


@bp.route('/getPerformances', methods=['GET'])
def get_overall_performance():
    if not ('access_token' in request.headers) or not ('account_id' in request.headers):
        return 'Invalid Request', 400
    account_id = request.headers['account_id']
    access_token = request.headers['access_token']
    today = date.today()
    portfolio_request_url = portfolio_url.format(account_id=account_id)
    portfolio_response = requests.get(portfolio_request_url, headers={'access_token': access_token})
    portfolio_response = portfolio_response.json()
    if 'accountMembers' not in portfolio_response:
        print('token expired refresh token')
        return 'refresh token', 409
    total_market_value = 0
    cash_balance = 0
    for keyValues in portfolio_response['accountMembers']:
        if keyValues['key'] == 'totalMarketValue':
            total_market_value = keyValues['value']
        if keyValues['key'] == 'cashBalance':
            cash_balance = keyValues['value']

    total_value = float(total_market_value) + float(cash_balance)

    # Overall
    overall_end_date = today.strftime("%Y-%m-%d")
    overall_url = overall_performance_url.format(acc_number=account_id, end_date=overall_end_date)
    overall_response = requests.get(overall_url, headers={'access_token': access_token})
    overall_response = overall_response.json()
    overall_profit_loss = float(overall_response['profitLoss'])
    overall_percentage = overall_profit_loss / (overall_profit_loss + total_value)

    # Daily
    daily_end_date = today
    if today.weekday() == 6:
        daily_start_date = today - datetime.timedelta(days=2)
    elif today.weekday() == 0:
        daily_start_date = today - datetime.timedelta(days=3)
    else:
        daily_start_date = today - datetime.timedelta(days=1)
    daily_url = performance_url.format(acc_number=account_id, start_date=daily_start_date, end_date=daily_end_date)
    daily_response = requests.get(daily_url, headers={'access_token': access_token})
    daily_response = daily_response.json()
    daily_profit_loss = float(daily_response['profitLoss'])
    daily_percentage = daily_profit_loss / (daily_profit_loss + total_value)

    # Weekly
    weekly_start_date = today - datetime.timedelta(days=7)
    weekly_end_date = today.strftime("%Y-%m-%d")
    weekly_url = performance_url.format(acc_number=account_id, start_date=weekly_start_date, end_date=weekly_end_date)
    weekly_response = requests.get(weekly_url, headers={'access_token': access_token})
    weekly_response = weekly_response.json()
    weekly_profit_loss = float(weekly_response['profitLoss'])
    weekly_percentage = weekly_profit_loss / (weekly_profit_loss + total_value)

    # Monthly
    monthly_start_date = today - datetime.timedelta(days=30)
    monthly_end_date = today.strftime("%Y-%m-%d")
    monthly_url = performance_url.format(acc_number=account_id, start_date=monthly_start_date,
                                         end_date=monthly_end_date)
    monthly_response = requests.get(monthly_url, headers={'access_token': access_token})
    monthly_response = monthly_response.json()
    monthly_profit_loss = float(monthly_response['profitLoss'])
    monthly_percentage = monthly_profit_loss / (monthly_profit_loss + total_value)

    return {
        'overall': '{:0.3}'.format(overall_percentage * 100),
        'daily': '{:0.3}'.format(daily_percentage * 100),
        'weekly': '{:0.3}'.format(weekly_percentage * 100),
        'monthly': '{:0.3}'.format(monthly_percentage * 100)
    }


@bp.route('/getPositions', methods=['GET'])
def get_positions():
    if not ('access_token' in request.headers) or not ('account_id' in request.headers):
        return 'Invalid Request', 400
    account_id = request.headers['account_id']
    access_token = request.headers['access_token']
    portfolio_request_url = portfolio_url.format(account_id=account_id)
    portfolio_response = requests.get(portfolio_request_url, headers={'access_token': access_token})
    portfolio_response = portfolio_response.json()
    if 'positions' not in portfolio_response:
        print('token expired refresh token')
        return 'refresh token', 409
    my_portfolio = portfolio_response
    my_positions = portfolio_response['positions']
    cash = 0
    response = []
    for keyValues in my_portfolio['accountMembers']:
        if keyValues['key'] == 'cashBalance':
            cash = float(keyValues['value'])
    total_amount = cash

    for position in my_positions:
        total_amount += float(position['cost'])
    for position in my_positions:
        response.append({
            'stockName': position['ticker']['symbol'],
            'percentage': '{:0.3}'.format((float(position['cost']) / total_amount) * 100),
            'type': position['assetType']
        })

    response.append({
        'stockName': 'cash',
        'percentage': '{:0.3}'.format((cash / total_amount) * 100)
    })
    return {
        'positions': response,
    }


@cross_origin()
@bp.route('/status', methods=['GET'])
def getStatus():
    return ''
