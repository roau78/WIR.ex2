package webdata;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

class TokenData {
    private int numberOfReviews;
    private int numberOfInstances;
    private ArrayList<Integer> idFreqList;
    private HashMap<Integer, Integer> idFreqMapTemp;
    private ArrayList<Integer> arrayListPresentation;
    private boolean isDone;

    TokenData(int numberOfReviews, int numberOfInstances, ArrayList<Integer> idFreqList){
        this.numberOfReviews = numberOfReviews;
        this.numberOfInstances = numberOfInstances;
        this.idFreqList = idFreqList;
        this.isDone = true;
    }

    TokenData(int reviewId) {
        this.numberOfReviews = 1;
        this.numberOfInstances = 1;
        this.idFreqMapTemp = new HashMap<>();
        this.idFreqMapTemp.put(reviewId, 1);
        this.isDone = false;
    }

    void update(int reviewId) {
        if (this.isDone) {
            return;
        }
        if (this.idFreqMapTemp.containsKey(reviewId)) {
            this.idFreqMapTemp.put(reviewId, this.idFreqMapTemp.get(reviewId) + 1);
        }
        else {
            this.idFreqMapTemp.put(reviewId, 1);
            this.numberOfReviews++;
        }
        this.numberOfInstances++;
    }

    void done() {
        if (this.isDone) {
            return;
        }
        this.idFreqList = new ArrayList<>(2 * this.idFreqMapTemp.size());
        SortedSet<Integer> keys = new TreeSet<Integer>(this.idFreqMapTemp.keySet());
        for (Integer key : keys) {
            this.idFreqList.add(key);
            this.idFreqList.add(this.idFreqMapTemp.get(key));
        }
        this.idFreqMapTemp = null;
        this.isDone = true;
    }

    ArrayList<Integer> getArrayListPresentation() {
        if (!this.isDone) {
            return null;
        }
        if (this.arrayListPresentation == null) {
            this.arrayListPresentation = new ArrayList<>();
            this.arrayListPresentation.add(this.numberOfReviews);
            this.arrayListPresentation.add(this.numberOfInstances);
            this.arrayListPresentation.addAll(this.idFreqList);
        }
        return this.arrayListPresentation;
    }

    Enumeration<Integer> getEnumerator() {
        if (!this.isDone) {
            return null;
        }
        final Iterator<Integer> it = this.idFreqList.iterator();
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

    int getNumberOfReviews() {
        return numberOfReviews;
    }

    int getNumberOfInstances() {
        return numberOfInstances;
    }
}
