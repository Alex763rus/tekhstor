package com.example.tekhstor.model.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@Entity(name = "contact")
public class Contact {
    @Id
    @Column(name = "contact_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long contactId;

    @Column(name = "chat_id")
    private String chatId;

    @Column(name = "username")
    private String username;

    @Column(name = "title")
    private String title;

    @Column(name = "is_delete")
    private Boolean isDelete;

    @ManyToOne(optional = false, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "folderId")
    private Folder folder;

}
