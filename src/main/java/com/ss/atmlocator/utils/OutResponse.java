package com.ss.atmlocator.utils;

/**
 * Created by us8610 on 11/25/2014.
 */

import java.util.ArrayList;
import java.util.List;

public class OutResponse {

    private String status;
    private List<ErrorMessage> errorMessageList;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ErrorMessage> getErrorMessageList() {
        return this.errorMessageList;
    }

    public void setErrorMessageList(List<ErrorMessage> errorMessageList) {
        this.errorMessageList = errorMessageList;
    }

    public OutResponse(String status, ErrorMessage errorMessage) {
        this.status = status;
        this.errorMessageList = new ArrayList<ErrorMessage>(1);
        this.errorMessageList.add(errorMessage);
    }

    public OutResponse() {
    }
}