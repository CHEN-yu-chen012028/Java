package com.example.demo.controller;

import com.example.demo.algo.TokenBucketDefense;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoginController {

    @Autowired
    private TokenBucketDefense tokenBucketDefense;

    // ==========================================
    // 接口 1：處理使用者登入驗證
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<String> doLogin(@RequestBody Map<String, String> loginData, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();

        // 1. 先檢查有沒有被鎖定
        if (tokenBucketDefense.isBlocked(clientIp)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("❌ 您的 IP 因連續登入失敗 3 次已被鎖定！請稍後再試。");
        }

        String username = loginData.get("username");
        String password = loginData.get("password");

        // 2. 驗證密碼 (為求專題簡單，直接寫死 admin / 123456)
        if ("admin".equals(username) && "123456".equals(password)) {
            tokenBucketDefense.loginSucceeded(clientIp); // 登入成功，重設計數器
            return ResponseEntity.ok("🎉 登入成功！歡迎進入後台管理系統。");
        } else {
            tokenBucketDefense.loginFailed(clientIp); // 登入失敗，累計次數
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("❌ 帳號或密碼錯誤，請重新輸入！");
        }
    }

    // ==========================================
    // 接口 2：【新增加的】供右側藍隊資安儀表板定時撈取數據
    // ==========================================
    @GetMapping("/security/status")
    public Map<String, Object> getSecurityStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("blockedIps", tokenBucketDefense.getBlockedIps());
        status.put("logs", tokenBucketDefense.getLogs());
        return status;
    }
} // <-- 這是原本檔案最後面的大括號，新接口一定要加在它前面喔！