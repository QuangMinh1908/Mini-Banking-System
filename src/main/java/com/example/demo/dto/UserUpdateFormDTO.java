package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserUpdateFormDTO {

    @NotNull(message = "Thiếu ID khách hàng")
    private Long id;

    @Size(min = 3, max = 100, message = "Họ và tên phải từ 3 đến 100 ký tự")
    @Pattern(regexp = "^[^<>&\"']*$", message = "Họ và tên chứa ký tự không hợp lệ")
    private String fullName;

    @Pattern(regexp = "^0[0-9]{9}$", message = "SĐT bắt đầu bằng số 0 và gồm 10 chữ số")
    private String phoneNumber;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 50, message = "Email không được vượt quá 50 ký tự")
    private String email;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    @Pattern(regexp = "^[^<>&\"']*$", message = "Địa chỉ chứa ký tự không hợp lệ")
    private String address;

    private String gender;

    public UserUpdateFormDTO() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}