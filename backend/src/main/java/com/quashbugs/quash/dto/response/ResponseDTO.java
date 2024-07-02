package com.quashbugs.quash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data               // This will generate getters, setters, equals, hashCode, and toString methods.
@NoArgsConstructor  // This will generate a no-arg constructor.
@AllArgsConstructor // This will generate a constructor with all the fields.
public class ResponseDTO {

    private boolean success;
    private String message;
    private Object data;
}
