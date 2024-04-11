#!/bin/bash

echo "deb [trusted=yes] https://repo.zextras.io/release/ubuntu focal main" > /etc/apt/sources.list.d/zextras.list

apt update && apt upgrade -y