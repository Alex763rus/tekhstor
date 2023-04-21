package com.example.tekhstor.model.jpa;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ContactRepository extends CrudRepository<Contact, Long> {

    public List<Contact> getContatsByFolderAndIsDelete(Folder folder, boolean isDelete);

    public List<Contact> getContactsByIsDelete(boolean isDelete);
    public Contact getContactByIsDeleteAndUsernameIs(boolean isDelete, String username);

}
