package com.example.demo.util;

import java.util.Random;

public class AccountUtils {
    public static String generateLuhnAccountNumber(String prefix) {
        Random random = new Random();
        
        // 1. Khởi tạo chuỗi với tiền tố được truyền vào (88 hoặc 99)
        StringBuilder accNum = new StringBuilder(prefix);
        
        // 2. Sinh thêm 7 chữ số ngẫu nhiên (Tổng cộng 9 chữ số)
        for (int i = 0; i < 7; i++) {
            accNum.append(random.nextInt(10));
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