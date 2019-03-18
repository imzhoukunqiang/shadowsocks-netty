function FindProxyForURL(url, host) {
    let proxyArray = [
        "HTTPS hgc.ghelper.net:2443;",
        "HTTPS pccw7.ghelper.net:443;",
        // "HTTPS www.tcpbbr.net:443;",
        // "HTTPS www.pickdown.net:1443;",
        // "HTTPS www.51netflix.com:1443;",
        "HTTPS us02.ghelper.net:443;",
        "HTTPS us.onhop.net:443;",
        "HTTPS gia.onhop.net:443;",
        "HTTPS hgc.tcpbbr.net:443;",
        "HTTPS ru.onhop.net:443;",
        "DIRECT"
    ];

    let proxyList = proxyArray.join("");
    let fastProxy = "HTTPS www.letswe.bid:443;HTTPS www.wanniba.xyz:443;HTTPS www.oktrade.online:7766;" + proxyList;

    if (host == "android.com" || shExpMatch(host, "*.android.com")) {
        return fastProxy;
    }
    if (host == "gstatic.com" || shExpMatch(host, "*.gstatic.com") || host == "googleusercontent.com" || shExpMatch(host, "*.googleusercontent.com") || host == "gvt2.com" || shExpMatch(host, "*.gvt2.com") || host == "gvt3.com" || shExpMatch(host, "*.gvt3.com") || host == "ggpht.com" || shExpMatch(host, "*.ggpht.com") || host == "googleapis.com" || shExpMatch(host, "*.googleapis.com") || host == "google.com.hk" || shExpMatch(host, "*.google.com.hk") || host == "google.com.tw" || shExpMatch(host, "*.google.com.tw") || host == "google.com" || shExpMatch(host, "*.google.com") || host == "gmail.com" || shExpMatch(host, "*.gmail.com") || host == "ggpht.com" || shExpMatch(host, "*.ggpht.com") || host == "chrome.com" || shExpMatch(host, "*.chrome.com") || host == "googleadservices.com" || shExpMatch(host, "*.googleadservices.com") || host == "googleusercontent.com" || shExpMatch(host, "*.googleusercontent.com") || host == "googletagservices.com" || shExpMatch(host, "*.googletagservices.com") || host == "googlesyndication.com" || shExpMatch(host, "*.googlesyndication.com") || host == "google.co.jp" || shExpMatch(host, "*.google.co.jp") || host == "googlesource.com" || shExpMatch(host, "*.googlesource.com") || host == "googleblog.com" || shExpMatch(host, "*.googleblog.com")) {
        return fastProxy;
    }
    if (host == "wikipedia.org" || shExpMatch(host, "*.wikipedia.org")) {
        return fastProxy;
    }
    if (host == "dropbox.com" || shExpMatch(host, "*.dropbox.com")) {
        return fastProxy;
    }

    return proxyList;

}