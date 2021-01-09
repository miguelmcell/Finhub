import os
from flask import Flask
from flask_cors import CORS
from blueprints import robinhoodRepository
from blueprints import webullRepository

app = Flask(__name__, instance_relative_config=True)

cors = CORS(app)
app.config.from_mapping(
    SECRET_KEY=os.urandom(24),
)

app.config.from_pyfile('config.py', silent=True)
app.register_blueprint(robinhoodRepository.bp)
app.register_blueprint(webullRepository.bp)

isDev = True if app.env == 'development' else False
app.run(host="0.0.0.0", port="5000", debug=isDev, threaded=True)
