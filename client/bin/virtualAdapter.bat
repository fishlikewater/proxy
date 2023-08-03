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