#!/usr/bin/env bash

# setup mysql
apt-get update
DEBIAN_FRONTEND=noninteractive apt-get -y install mysql-server-5.5
mysql -uroot <<MYSQL_SCRIPT
  create database blog default charset = 'utf8mb4';
  grant all on blog.* to blog@localhost;
MYSQL_SCRIPT

# setup auto cd to project root
echo "cd /vagrant" >> /home/vagrant/.bashrc
