#!/bin/bash

# 设置虚拟网卡的名称
adapterName="MyVirtualAdapter"

# 检测虚拟网卡是否存在
networksetup -listallhardwareports | grep "$adapterName"
if [ $? -eq 0 ]; then
    # 虚拟网卡存在，执行删除操作
    sudo networksetup -setnetworkserviceenabled "$adapterName" off
    sudo networksetup -deletevlan "$adapterName"
    echo "虚拟网卡删除成功"
else
    echo "虚拟网卡不存在"
fi

# 创建虚拟网卡
sudo networksetup -createVLAN "$adapterName" "$adapterName"

# 配置虚拟网卡的 IP 地址和子网掩码
sudo ifconfig "$adapterName" inet $localAddress netmask 255.255.255.0

# 启用虚拟网卡
sudo networksetup -setnetworkserviceenabled "$adapterName" on

echo "虚拟网卡创建成功"