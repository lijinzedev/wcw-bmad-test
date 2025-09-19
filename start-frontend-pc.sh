#!/bin/bash

# 启动前端PC应用的脚本
echo "Starting frontend PC application..."

# 检查是否已安装npm
if ! command -v npm &> /dev/null
then
    echo "npm could not be found. Please install Node.js and npm to run the frontend application."
    exit 1
fi

# 进入前端PC目录并启动应用
cd apps/frontend-pc
npm install
npm run serve