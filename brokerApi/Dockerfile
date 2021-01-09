FROM ubuntu:latest

RUN apt-get update -y

RUN apt-get install -y python3-pip
RUN apt install git -y

COPY . /app
WORKDIR /app

RUN pip3 install -r requirements.txt

ENTRYPOINT ./runFlask.sh

# port= external:internal
