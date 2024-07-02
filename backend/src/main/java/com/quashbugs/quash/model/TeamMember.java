package com.quashbugs.quash.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "team_members")
public class TeamMember {

    @Id
    private String id;

    @DBRef
    private Organisation organisation;

    @DBRef
    private User user;

    private String role;
    private Date joinedAt;
    private boolean hasAccepted;
    private String phoneNumber;
    private boolean isActive;


    public Organisation getOrganisation() {return organisation;}

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}