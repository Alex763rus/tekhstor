package com.example.tekhstor.model.comparator;

import com.example.tekhstor.model.jpa.Contact;

import java.util.Comparator;

public class ContactComparator implements Comparator<Contact> {

    @Override
    public int compare(Contact o1, Contact o2) {
        return o1.getFolder().getFolderId().compareTo(o2.getFolder().getFolderId());
    }
}
