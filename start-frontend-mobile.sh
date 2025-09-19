#!/bin/bash

# 启动移动端应用的脚本
echo "Starting mobile application..."

# 检查是否已安装npm
if ! command -v npm &> /dev/null
then
    echo "npm could not be found. Please install Node.js and npm to run the mobile application."
    exit 1
fi

# 进入移动端目录并启动应用
cd apps/frontend-mobile
npm install
echo "To run the mobile app in browser, use: npm run serve"
echo "To build the mobile app, use: npm run build:app"