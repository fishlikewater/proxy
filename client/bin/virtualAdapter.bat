@REM
@REM Copyright © 2024 zhangxiang (fishlikewater@126.com)
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM     http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@echo off

REM 设置虚拟网卡的名称
set "adapterName=MyVirtualAdapter"
REM 检测虚拟网卡是否存在
netsh interface show interface name="%adapterName%" | findstr /C:"%adapterName%"
if %errorlevel%==0 (
    REM 虚拟网卡存在，执行删除操作
    netsh interface set interface name="%adapterName%" admin=disabled
    netsh interface delete interface name="%adapterName%"
    echo "虚拟网卡删除成功"
) else (
    echo "虚拟网卡不存在"
)
REM 创建虚拟网卡
netsh interface ip show config
netsh interface ip set address name="%adapterName%" source=static address=192.168.12.110 mask=255.255.255.0
netsh interface ip add dns name="%adapterName%" address=8.8.8.8

REM 启用虚拟网卡
netsh interface set interface name="%adapterName%" admin=enabled
echo "虚拟网卡创建成功"
pause