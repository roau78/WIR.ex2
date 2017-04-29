package webdata;

abstract class BinarySearch {
    int middle;
    private int dataStructLength;

    public abstract String getNextValue();

    BinarySearch(int dataStructLength) {
        this.dataStructLength = dataStructLength;
    }

    Integer search(String val) {
        int left = 0, right = this.dataStructLength - 1, compRes;
        String currentStr;
        while (left <= right) {
            this.middle = (left + right) / 2;
            currentStr = this.getNextValue();
            compRes = currentStr.compareTo(val);
            if (compRes < 0) {
                left = (this.middle + 1);
            }
            else if(compRes > 0) {
                right = (this.middle - 1);
            }
            else {
                return this.middle;
            }
        }
        return null;
    }
}
