package com.example.tekhstor.model.jpa;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ContactRepository extends CrudRepository<Contact, Long> {

    public List<Contact> getContactByFolder(Folder folder);
}
