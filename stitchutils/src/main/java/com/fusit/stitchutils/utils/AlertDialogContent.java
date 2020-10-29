package com.fusit.stitchutils.utils;


import java.io.Serializable;

/**
 * Created by tamarraviv on 11/10/15.
 */
public class AlertDialogContent implements Serializable {
//    private static final long serialVersionUID = -5537701814867112838L;
    private String title;
    private String message;
    private String positiveLabel;
    private String neutralLabel;
    private String negativeLabel;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPositiveLabel() {
        return positiveLabel;
    }

    public void setPositiveLabel(String positiveLabel) {
        this.positiveLabel = positiveLabel;
    }

    public String getNeutralLabel() {
        return neutralLabel;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setNeutralLabel(String neutralLabel) {
        this.neutralLabel = neutralLabel;
    }

    public String getNegativeLabel() {
        return negativeLabel;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setNegativeLabel(String negativeLabel) {
        this.negativeLabel = negativeLabel;
    }
}
