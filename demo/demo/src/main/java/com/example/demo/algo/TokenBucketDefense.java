package com.example.demo.algo;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class TokenBucketDefense {

    private final int MAX_ATTEMPTS = 3;             // 最大失敗次數
    private final long LOCK_TIME = 60 * 1000;       // 鎖定時間：60秒

    // 紀錄每個 IP 的失敗次數 [IP, 失敗次數]
    private final ConcurrentHashMap<String, Integer> attemptsMap = new ConcurrentHashMap<>();
    // 紀錄每個 IP 被鎖定的結束時間點 [IP, 鎖定結束的 Timestamp]
    private final ConcurrentHashMap<String, Long> lockTimeMap = new ConcurrentHashMap<>();
    
    // 【關鍵新增】：儲存即時資安日誌的清單（執行緒安全）
    private final List<String> securityLogs = new CopyOnWriteArrayList<>();

    /**
     * 新增資安日誌並自動加上時間戳記
     */
    public void addLog(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        securityLogs.add("[" + timestamp + "] " + message);
    }

    /**
     * 供 Controller 呼叫：取得所有日誌
     */
    public List<String> getLogs() {
        return securityLogs;
    }

    /**
     * 供 Controller 呼叫：取得目前所有被鎖定的 IP 與剩餘秒數
     */
    public Map<String, Long> getBlockedIps() {
        Map<String, Long> activeBlocks = new HashMap<>();
        long now = System.currentTimeMillis();
        
        lockTimeMap.forEach((ip, expiry) -> {
            if (now < expiry) {
                activeBlocks.put(ip, (expiry - now) / 1000); // 轉換成剩餘秒數回傳
            }
        });
        return activeBlocks;
    }

    /**
     * 檢查該 IP 目前是否處於被鎖定狀態
     */
    public boolean isBlocked(String ip) {
        if (lockTimeMap.containsKey(ip)) {
            long lockExpiry = lockTimeMap.get(ip);
            if (System.currentTimeMillis() < lockExpiry) {
                return true; 
            } else {
                // 鎖定時間已過，自動解鎖：清除紀錄並記下一條日誌
                lockTimeMap.remove(ip);
                attemptsMap.remove(ip);
                addLog("🔓 系統通知：IP " + ip + " 鎖定時間已滿，自動解除封鎖。");
            }
        }
        return false;
    }

    /**
     * 登入失敗時呼叫，增加失敗次數
     */
    public void loginFailed(String ip) {
        int attempts = attemptsMap.getOrDefault(ip, 0) + 1;
        attemptsMap.put(ip, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            long lockExpiryTime = System.currentTimeMillis() + LOCK_TIME;
            lockTimeMap.put(ip, lockExpiryTime);
            addLog("💥 [資安警報] 偵測到暴力破解！IP " + ip + " 連續失敗達 3 次，系統強制封鎖 60 秒！");
        }
    }

    /**
     * 登入成功時呼叫，重設失敗紀錄
     */
    public void loginSucceeded(String ip) {
        attemptsMap.remove(ip);
        lockTimeMap.remove(ip);
    }
}