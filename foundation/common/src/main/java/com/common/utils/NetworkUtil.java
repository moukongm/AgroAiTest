package com.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 网络状态检测工具类
 */
public class NetworkUtil {
    
    /**
     * 判断网络是否可用
     * @param context 上下文
     * @return true=有网络连接, false=无网络连接
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;
        
        ConnectivityManager cm = (ConnectivityManager) 
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;
            
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            if (capabilities == null) return false;
            
            // 检查是否有 WiFi、移动网络或以太网连接
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
        } else {
            @SuppressWarnings("deprecation")
            android.net.NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }
    
    /**
     * 检测特定地址是否可达（用于检测是否能访问目标服务器）
     * @param host 主机地址或域名
     * @param port 端口号
     * @param timeoutMs 超时时间（毫秒）
     * @return true=可达, false=不可达
     */
    public static boolean isHostReachable(String host, int port, int timeoutMs) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 检测网络是否能访问百度（通用互联网可达性检测）
     * @param timeoutMs 超时时间（毫秒），默认 3000
     * @return true=可访问互联网, false=无法访问
     */
    public static boolean isInternetAvailable(int timeoutMs) {
        return isHostReachable("www.baidu.com", 80, timeoutMs > 0 ? timeoutMs : 3000);
    }
    
    /**
     * 判断是否为 WiFi 连接
     * @param context 上下文
     * @return true=WiFi连接, false=非WiFi
     */
    public static boolean isWifiConnected(Context context) {
        if (context == null) return false;
        
        ConnectivityManager cm = (ConnectivityManager) 
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;
            
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null && 
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        } else {
            @SuppressWarnings("deprecation")
            android.net.NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifiInfo != null && wifiInfo.isConnected();
        }
    }
    
    /**
     * 判断是否为移动网络连接
     * @param context 上下文
     * @return true=移动网络, false=其他
     */
    public static boolean isMobileConnected(Context context) {
        if (context == null) return false;
        
        ConnectivityManager cm = (ConnectivityManager) 
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;
            
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null && 
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
        } else {
            @SuppressWarnings("deprecation")
            android.net.NetworkInfo mobileInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            return mobileInfo != null && mobileInfo.isConnected();
        }
    }
}