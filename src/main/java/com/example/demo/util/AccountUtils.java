package com.example.demo.util;

import java.security.SecureRandom;

public class AccountUtils {
    
    // Khởi tạo SecureRandom tĩnh để tái sử dụng, tối ưu hiệu suất và tăng cường bảo mật
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Sinh số tài khoản ngẫu nhiên gồm 10 chữ số chuẩn thuật toán Luhn (Modulus 10)
     * Cấu trúc: [Prefix] + [7 số ngẫu nhiên] + [1 số checksum]
     * Prefix: 88 (PAYMENT) hoặc 99 (SAVING)
     */
    public static String generateLuhnAccountNumber(String prefix) {
        // 1. Khởi tạo chuỗi với tiền tố được truyền vào (88 hoặc 99)
        StringBuilder accNum = new StringBuilder(prefix);
        
        // 2. Sinh thêm 7 chữ số ngẫu nhiên (sử dụng SecureRandom)
        for (int i = 0; i < 7; i++) {
            accNum.append(secureRandom.nextInt(10));
        }

        // 3. Thuật toán Luhn tính toán con số Checksum (số thứ 10)
        int sum = 0;
        boolean isSecond = true;
        
        for (int i = accNum.length() - 1; i >= 0; i--) {
            int digit = accNum.charAt(i) - '0';
            
            if (isSecond) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            
            sum += digit;
            isSecond = !isSecond;
        }
        
        int checksum = (10 - (sum % 10)) % 10;
        
        // 4. Ghép con số Checksum vào cuối chuỗi kết quả
        return accNum.toString() + checksum;
    }
}