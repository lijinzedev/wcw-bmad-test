#!/bin/bash

# 启动后端服务的脚本
echo "Starting backend service..."

# 检查是否已安装Maven
if ! command -v mvn &> /dev/null
then
    echo "Maven could not be found. Please install Maven to run the backend service."
    exit 1
fi

# 进入后端目录并启动服务
cd apps/backend
mvn spring-boot:run