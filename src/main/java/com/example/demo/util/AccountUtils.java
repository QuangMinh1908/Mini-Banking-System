package com.example.demo.util;

import java.util.Random;

public class AccountUtils {

    /**
     * Sinh số tài khoản ngân hàng gồm 10 chữ số chuẩn thuật toán Luhn (Modulus 10)
     * Cấu trúc: [88] + [7 số ngẫu nhiên] + [1 số checksum]
     */
    public static String generateLuhnAccountNumber() {
        Random random = new Random();
        
        // 1. Khởi tạo chuỗi với mã ngân hàng mặc định là "88"
        StringBuilder accNum = new StringBuilder("88");
        
        // 2. Sinh thêm 7 chữ số ngẫu nhiên (Tổng cộng được 9 chữ số)
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
        
        // 4. Ghép con số Checksum vào cuối chuỗi và trả về kết quả
        return accNum.toString() + checksum;
    }
}