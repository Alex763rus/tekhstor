package com.example.tekhstor.model.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;


@Getter
@Setter
@ToString
@Entity(name = "folder")
public class Folder {
    @Id
    @Column(name = "folder_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long folderId;

    @Column(name = "name")
    private String name;

    @Column(name = "is_delete")
    private Boolean isDelete;

    @OneToMany(mappedBy="contactId", fetch= FetchType.EAGER)
    private List<Contact> contactList;
}
