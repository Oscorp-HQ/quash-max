package com.quashbugs.quash.util;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CryptoService {

    @Value("${jasypt.encryption.password}")
    private String JASYPT_PASSWORD;

    public String encrypt(String data) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(JASYPT_PASSWORD);
        return encryptor.encrypt(data);
    }

    public String decrypt(String encryptedData) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(JASYPT_PASSWORD);
        return encryptor.decrypt(encryptedData);
    }
}