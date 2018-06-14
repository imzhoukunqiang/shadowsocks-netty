#!/bin/bash
pid=$(cat .shadowsocks-netty.pid)
kill -9 $pid
