package com.quashbugs.quash.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Document(collection = "contacts")
public class Contact {

    @Id
    private String id;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Phone number is mandatory")
    @Pattern(regexp = "(^$|[0-9]{10})", message = "Must be a valid phone number")
    private String phoneNumber;

    @Email(message = "Email should be valid")
    private String emailAddress;

    @NotBlank(message = "Company is mandatory")
    private String companyName;

    private String feedback;
}