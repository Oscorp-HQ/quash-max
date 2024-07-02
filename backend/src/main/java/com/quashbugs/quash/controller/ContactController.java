package com.quashbugs.quash.controller;

import com.quashbugs.quash.dto.response.ResponseDTO;
import com.quashbugs.quash.model.Contact;
import com.quashbugs.quash.service.ContactService;
import io.swagger.annotations.Api;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts/")
@Api(tags = "My API")
public class ContactController {

    private final ContactService contactService;

    @Autowired
    public ContactController(ContactService contactService) {
        this.contactService = contactService;

    }

    /**
     * Adds a new contact.
     *
     * @param contact The contact information to be added.
     * @return ResponseEntity with a ResponseDTO indicating success or failure.
     */
    @PostMapping
    public ResponseEntity<ResponseDTO> addContact(@Valid @RequestBody Contact contact) {
        contactService.saveContact(contact);
        return new ResponseEntity<>(new ResponseDTO(true, "Contact successfully added", contact), HttpStatus.CREATED);
    }

    /**
     * Retrieves a list of all contacts.
     *
     * @return ResponseEntity with a ResponseDTO containing the list of contacts.
     */
    @GetMapping
    public ResponseEntity<ResponseDTO> getContacts() {
        List<Contact> contactList = contactService.getAllContacts();
        return new ResponseEntity<>(new ResponseDTO(true, "Contact List fetched", contactList), HttpStatus.CREATED);
    }
}