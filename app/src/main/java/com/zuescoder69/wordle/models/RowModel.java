package com.zuescoder69.wordle.models;

/**
 * Created by Gagan Kumar on 22/03/22.
 */
public class RowModel {
    String row;
    String letter1;
    String letter2;
    String letter3;
    String letter4;
    String letter5;

    public RowModel(String row, String letter1, String letter2, String letter3, String letter4, String letter5) {
        this.row = row;
        this.letter1 = letter1;
        this.letter2 = letter2;
        this.letter3 = letter3;
        this.letter4 = letter4;
        this.letter5 = letter5;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public String getLetter1() {
        return letter1;
    }

    public void setLetter1(String letter1) {
        this.letter1 = letter1;
    }

    public String getLetter2() {
        return letter2;
    }

    public void setLetter2(String letter2) {
        this.letter2 = letter2;
    }

    public String getLetter3() {
        return letter3;
    }

    public void setLetter3(String letter3) {
        this.letter3 = letter3;
    }

    public String getLetter4() {
        return letter4;
    }

    public void setLetter4(String letter4) {
        this.letter4 = letter4;
    }

    public String getLetter5() {
        return letter5;
    }

    public void setLetter5(String letter5) {
        this.letter5 = letter5;
    }
}
