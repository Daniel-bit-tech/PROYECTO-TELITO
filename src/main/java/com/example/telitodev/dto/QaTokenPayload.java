package com.example.telitodev.dto;

public class QaTokenPayload {
    private String dni;
    private int lim;
    private int used;


    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public int getLim() {
        return lim;
    }

    public void setLim(int lim) {
        this.lim = lim;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }
}
