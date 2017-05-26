package webdata;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

class ProductData {
    private int numberOfReviews;
    private ArrayList<Integer> reviewList;
    private ArrayList<Integer> arrayListPresentation;
    private boolean isDone;

    ProductData(int numberOfReviews, ArrayList<Integer> reviewList){
        this.numberOfReviews = numberOfReviews;
        this.reviewList = reviewList;
        this.isDone = true;
    }

    ProductData(int reviewId) {
        this.numberOfReviews = 1;
        this.reviewList = new ArrayList<>(1);
        this.reviewList.add(reviewId);
        this.isDone = false;
    }

    ArrayList<Integer> getArrayListPresentation() {
        if (!this.isDone) {
            return null;
        }
        if (this.arrayListPresentation == null) {
            this.arrayListPresentation = new ArrayList<>();
            this.arrayListPresentation.add(this.numberOfReviews);
            this.arrayListPresentation.addAll(this.reviewList);
        }
        return this.arrayListPresentation;
    }

    void update(int reviewId) {
        if (this.isDone) {
            return;
        }
        this.reviewList.add(reviewId);
        this.numberOfReviews++;
    }

    void done() {
        this.isDone = true;
    }

    Enumeration<Integer> getEnumorator () {
        if (!this.isDone) {
            return null;
        }
        final Iterator<Integer> it = this.reviewList.iterator();
        return new Enumeration<Integer>() {

            @Override
            public Integer nextElement() {
                return it.next();
            }

            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }
        };
    }
}
