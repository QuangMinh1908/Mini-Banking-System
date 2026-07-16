package com.example.demo.service;

import com.example.demo.model.BankAccount;
import com.example.demo.repository.BankAccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.List;

@Service
public class BankAccountService {
    
    private final BankAccountRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;

    public BankAccountService(BankAccountRepository repository) {
        this.repository = repository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public List<BankAccount> getAllUsers() {
        return repository.findByRole("user");
    }

    public void createCustomer(BankAccount account) {
        String rawPassword = account.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        account.setPassword(encodedPassword);
        account.setRole("user");
        repository.save(account);
    }

    public void deleteCustomer(Long id) {
        repository.deleteById(id);
    }

    public void updateCustomer(BankAccount updatedAccount) {
        BankAccount existingAccount = repository.findById(updatedAccount.getId()).orElse(null);
        
        if (existingAccount != null) {
            existingAccount.setUsername(updatedAccount.getUsername());
            existingAccount.setPhoneNumber(updatedAccount.getPhoneNumber());
            
            repository.save(existingAccount);
        }
    }

    public List<BankAccount> searchCustomers(String keyword) {
        List<BankAccount> results;
        if (keyword != null && !keyword.trim().isEmpty()) {
            results = repository.searchByKeyword(keyword.trim());
        } else {
            results = repository.findAll();
        }
        return results.stream()
                .filter(account -> !"admin".equalsIgnoreCase(account.getRole()))
                .collect(Collectors.toList());
    }
}