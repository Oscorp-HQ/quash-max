package com.quashbugs.quash.util;

import com.quashbugs.quash.model.Organisation;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static com.quashbugs.quash.constants.Constants.SECRET_KEY;

public class OrganisationKeyGenerator {

    public static String generateSecureHash(String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hasher = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hasher.init(keySpec);
        byte[] hash = hasher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hashString = new StringBuilder();
        for (byte b : hash) {
            hashString.append(String.format("%02x", b));
        }

        return hashString.toString();
    }

    public static String generateApiKey(Organisation organisation) {
        try {
            String organisationId = String.valueOf(organisation.getId());
            String organisationAbbreviation = organisation.getOrgAbbreviation();
            String organisationEnvironment = "dbg";
            String data = organisationId + organisationAbbreviation + organisationEnvironment;
            return data + generateSecureHash(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static String generateAbbreviation(Organisation organisation) {
        String orgName = organisation.getName().toUpperCase().replaceAll("[^A-Z ]", "");
        String[] words = orgName.split(" ");

        StringBuilder abbreviation = new StringBuilder();

        switch (words.length) {
            case 1:
                abbreviation.append(words[0], 0, Math.min(4, words[0].length()));
                break;
            case 2:
                abbreviation.append(words[0], 0, Math.min(2, words[0].length()));
                abbreviation.append(words[1], 0, Math.min(2, words[1].length()));
                break;
            default:
                for (int i = 0; i < 3 && i < words.length; i++) {
                    if (!words[i].isEmpty()) {
                        abbreviation.append(words[i].charAt(0));
                    }
                }
                break;
        }
        return abbreviation.toString();
    }
}