package webdata;

import java.util.ArrayList;

class ReviewData {
    static final int NUM_OF_FIELDS = 5;
    private int productIdNum;
    private String productString;
    private int helpNumerator;
    private int helpDenominator;
    private int score;
    private int numOfTokens;
    private boolean isDone;
    private ArrayList<Integer> arrayListPresentation;

    ReviewData(int productIdNum, int helpNumerator, int helpDenominator, int score, int numOfTokens) {
        this.productIdNum = productIdNum;
        this.helpNumerator = helpNumerator;
        this.helpDenominator = helpDenominator;
        this.score = score;
        this.numOfTokens = numOfTokens;
        this.isDone = true;
        this.productString = null;
    }

    ReviewData(String productString, int helpNumerator, int helpDenominator, int score, int numOfTokens) {
        this.productString = productString;
        this.helpNumerator = helpNumerator;
        this.helpDenominator = helpDenominator;
        this.score = score;
        this.numOfTokens = numOfTokens;
        this.isDone = false;
    }

    ArrayList<Integer> getArrayListPresentation() {
        if (!this.isDone) {
            return null;
        }
        if (this.arrayListPresentation == null) {
            this.arrayListPresentation = new ArrayList<>();
            this.arrayListPresentation.add(this.productIdNum);
            this.arrayListPresentation.add(this.helpNumerator);
            this.arrayListPresentation.add(this.helpDenominator);
            this.arrayListPresentation.add(this.score);
            this.arrayListPresentation.add(this.numOfTokens);
        }
        return this.arrayListPresentation;
    }

    void done(int productIdNum) {
        if (this.isDone) {
            return;
        }
        this.productIdNum = productIdNum;
        this.productString = null;
        this.isDone = true;
    }

    String getProductString() {
        return this.productString;
    }

    int getProductIdNum() {
        return productIdNum;
    }

    int getHelpNumerator() {
        return helpNumerator;
    }

    int getHelpDenominator() {
        return helpDenominator;
    }

    int getScore() {
        return score;
    }

    int getNumOfTokens() {
        return numOfTokens;
    }
}
