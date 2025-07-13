package org.example.service;

import org.example.dao.LoanDAO;

public class LoanService {

    private final LoanDAO loanDAO;

    public LoanService(LoanDAO loanDAO) {
        this.loanDAO = loanDAO;
    }


}
