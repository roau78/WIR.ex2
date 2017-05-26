package webdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class IndexReader {
    // meta data properties
    private int reviewsHeaderBits;
    private int tokensHeaderBits;
    private int productIdHeaderBits;
    private int numOfReviews;
    private int totalAmountOfTokens;
    private int totalAmountOfProducts;
    private int tokenStringLength;
    private int tokenDataLength;
    private int productDataLength;
    private int maxHelpNumerator;
    private int maxHelpDenominator;
    private int maxNumOfTokensInReview;

    private String dirPath;
    private String[] productIds;

    // indexes
    private int[] productIndex;
    private int[] tokenIndex;
    private int[] tokenDataIndex;

    // cache
    private LinkedHashMap<Integer, ReviewData> reviewCache;
    private LinkedHashMap<Integer, ProductData> productDataCache;
    private LinkedHashMap<String, Integer> tokenCache;
    private LinkedHashMap<Integer, TokenData> tokenDataCache;

    private BinarySearchToken tokenBinSearch;
    private BinarySearchProduct productBinSearch;

    /**
     * reads the tokenIndex binary file
     * initiate tokenIndex with the data from the binary file
     */
    private int[] readTokenIndex() {
        byte[] file = ReadWriteUtils.readAllFile(this.dirPath + ReadWriteUtils.TOKENS_INDEX_FILE);
        return ReadWriteUtils.variantDecoding(file);
    }

    /**
     * reads the tokenDataIndex binary file
     * initiate tokenDataIndex with the data from the binary file
     */
    private int[] readTokenDataIndex() {
        byte[] file = ReadWriteUtils.readAllFile(this.dirPath + ReadWriteUtils.TOKENS_DATA_INDEX_FILE);
        return ReadWriteUtils.variantDecoding(file);
    }

    /**
     * reads the productIndex binary file
     * @return an array of numbers where each number is a pointer to product data
     */
    private int[] readProductIndex() {
        byte[] file = ReadWriteUtils.readAllFile(this.dirPath + ReadWriteUtils.PRODUCT_INDEX_FILE);
        return ReadWriteUtils.variantDecoding(file);
    }

    /**
     * reads a review from review binary file
     * @return the ReviewData object of the reviewId input
     */
    private ReviewData readReview(int reviewIndex) {
        int bitsToRep[] = new int[ReviewData.NUM_OF_FIELDS];
        bitsToRep[0] = ReadWriteUtils.getNumOfBitsToRepresent(this.totalAmountOfProducts);
        bitsToRep[1] = ReadWriteUtils.getNumOfBitsToRepresent(this.maxHelpNumerator);
        bitsToRep[2] = ReadWriteUtils.getNumOfBitsToRepresent(this.maxHelpDenominator);
        bitsToRep[3] = ReadWriteUtils.BITS_FOR_SCORE; //numberBetween 1 to 5
        bitsToRep[4] = ReadWriteUtils.getNumOfBitsToRepresent(this.maxNumOfTokensInReview);

        int bitsPerReview = 0;
        for (int i = 0; i < bitsToRep.length; i++) {
            bitsPerReview += bitsToRep[i];
        }

        String bitRep = ReadWriteUtils.getRange(this.reviewsHeaderBits + (bitsPerReview * reviewIndex),
                bitsPerReview, this.dirPath + ReadWriteUtils.REVIEW_FILE);

        return new ReviewData(
                Integer.parseInt(bitRep.substring(0, bitsToRep[0]) , 2),
                Integer.parseInt(bitRep.substring(bitsToRep[0], bitsToRep[0] + bitsToRep[1]) , 2),
                Integer.parseInt(bitRep.substring(bitsToRep[0] + bitsToRep[1], bitsToRep[0] + bitsToRep[1] + bitsToRep[2]) , 2),
                Integer.parseInt(bitRep.substring(bitsToRep[0] + bitsToRep[1] + bitsToRep[2], bitsToRep[0] + bitsToRep[1] + bitsToRep[2] + bitsToRep[3]) , 2),
                Integer.parseInt(bitRep.substring(bitsToRep[0] + bitsToRep[1] + bitsToRep[2] + bitsToRep[3]) , 2));
    }

    /**
     * reads a product id from productId binary file
     * @return the product id of the reviewIdIndex input
     */
    private String[] readProductIds() {
        int curPtr = this.productIdHeaderBits;
        byte[] file = ReadWriteUtils.readAllFile(this.dirPath + ReadWriteUtils.PRODUCT_ID_FILE);
        String[] allProducts = new String[this.totalAmountOfProducts];

        String fileBinNumber = "";
        for (int i = 0; i < file.length; i++) {
            fileBinNumber += ReadWriteUtils.byteToBinary(file[i]);
        }

        String curProductBitRep = "";
        for (int i = 0; i < this.totalAmountOfProducts; i++) {
            curProductBitRep = fileBinNumber.substring(curPtr, curPtr + ReadWriteUtils.CHARS_AT_PRODUCT * ReadWriteUtils.BITS_PER_CHAR);
            String curProduct = "";
            for (int j = 0; j < curProductBitRep.length(); j += ReadWriteUtils.BITS_PER_CHAR) {
                String sVal = curProductBitRep.substring(j, ReadWriteUtils.BITS_PER_CHAR + j);
                byte intVal = (byte)Integer.parseInt(sVal, 2);
                curProduct += ReadWriteUtils.getKeyByValue(ReadWriteUtils.charCoder, intVal);
            }
            allProducts[i] = curProduct.toUpperCase();
            curPtr += ReadWriteUtils.CHARS_AT_PRODUCT * ReadWriteUtils.BITS_PER_CHAR;
        }
        return allProducts;
    }

    /**
     * reads a token from token binary file
     * @return the relevant token to the input
     */
    private String readToken(int index, int len) {
        String bitsRep = ReadWriteUtils.getRange(this.tokensHeaderBits + index,
                len, this.dirPath + ReadWriteUtils.TOKENS_FILE);

        String retStr = "";
        for (int i = 0; i < bitsRep.length(); i += ReadWriteUtils.BITS_PER_CHAR) {
            String sVal = bitsRep.substring(i, ReadWriteUtils.BITS_PER_CHAR + i);
            byte intVal = (byte)Integer.parseInt(sVal, 2);
            retStr += ReadWriteUtils.getKeyByValue(ReadWriteUtils.charCoder, intVal);
        }
        return retStr;
    }

    /**
     * reads a token data from tokenData binary file
     * @return the relevant TokenData object to the input
     */
    private TokenData readTokenData(int firstByte, int len) {
        byte[] curTokenDataTemp = ReadWriteUtils.readFromFile(this.dirPath + ReadWriteUtils.TOKENS_DATA_FILE, firstByte, len);
        int[] curTokenData = ReadWriteUtils.variantDecoding(curTokenDataTemp);

        int numberOfReviews = curTokenData[0];
        int numberOfInstances = curTokenData[1];
        ArrayList<Integer> idFreqList = new ArrayList<>();
        for (int i = 2; i < curTokenData.length; i++) {
            idFreqList.add(curTokenData[i]);
        }
        return new TokenData(numberOfReviews, numberOfInstances, idFreqList);
    }

    /**
     * reads a product data from the productData binary file
     * @return the relevant ProductData object to the input
     */
    private ProductData readProductData(int index, int len) {
        byte[] curProductDataTemp = ReadWriteUtils.readFromFile(this.dirPath + ReadWriteUtils.PRODUCT_DATA_FILE, index, len);
        int[] curProductData = ReadWriteUtils.variantDecoding(curProductDataTemp);
        int numberOfReviews = curProductData[0];
        ArrayList<Integer> reviews = new ArrayList<>();
        for (int i = 1; i < curProductData.length; i++) {
            reviews.add(curProductData[i]);
        }
        return new ProductData(numberOfReviews, reviews);
    }

    /**
     * reads the metadata file and extract all the relevant data
     */
    private void readMetaData() {
        byte[] b = ReadWriteUtils.readAllFile(this.dirPath + ReadWriteUtils.METADATA_FILE);
        int[] metadata = ReadWriteUtils.byteArrToIntArr(b);

        this.numOfReviews = metadata[ReadWriteUtils.NUM_OF_REVIEWS_INDEX];
        this.totalAmountOfTokens = metadata[ReadWriteUtils.TOTAL_AMOUNT_OF_TOKENS_INDEX];
        this.totalAmountOfProducts = metadata[ReadWriteUtils.TOTAL_AMOUNT_OF_PRODUCTS_INDEX];
        this.tokenStringLength = metadata[ReadWriteUtils.TOKEN_STINRG_LENGTH_INDEX];
        this.maxHelpNumerator = metadata[ReadWriteUtils.MAX_HELP_NUMERATOR_INDEX];
        this.maxHelpDenominator = metadata[ReadWriteUtils.MAX_HELP_DENOMINATOR_INDEX];
        this.tokenDataLength = metadata[ReadWriteUtils.TOKEN_DATA_LENGTH_INDEX];
        this.productDataLength = metadata[ReadWriteUtils.PRODUCT_DATA_LENGTH_INDEX];
        this.tokensHeaderBits = metadata[ReadWriteUtils.TOKENS_HEADER_BITS];
        this.productIdHeaderBits = metadata[ReadWriteUtils.PRODUCT_ID_HEADER_BITS];
        this.reviewsHeaderBits = metadata[ReadWriteUtils.REVIEW_HEADER_BITS];
        this.maxNumOfTokensInReview = metadata[ReadWriteUtils.MAX_NUM_OF_TOKENS_IN_REVIEW_INDEX];
    }

    private class BinarySearchProduct extends BinarySearch {
        BinarySearchProduct(int dataStructLength) {
            super(dataStructLength);
        }

        public String getNextValue() {
            return productIds[this.middle];
        }
    }

    private class BinarySearchToken extends BinarySearch {
        BinarySearchToken(int dataStructLength) {
            super(dataStructLength);
        }

        public String getNextValue() {
            return readToken(tokenIndex[this.middle], getTokenLength(this.middle));
        }
    }

    private int getTokenLength(int index) {
        if (index == this.tokenIndex.length - 1) {
            return this.tokenStringLength - this.tokenIndex[index];
        }
        return this.tokenIndex[index + 1] - this.tokenIndex[index];
    }

    private int getTokenDataLength(int index) {
        if (index == this.tokenDataIndex.length - 1) {
            return this.tokenDataLength - this.tokenDataIndex[index];
        }
        return this.tokenDataIndex[index + 1] - this.tokenDataIndex[index];
    }

    private int getProductDataLength(int index) {
        if (index == this.productIndex.length - 1) {
            return this.productDataLength - this.productIndex[index];
        }
        return this.productIndex[index + 1] - this.productIndex[index];
    }

    private static <K, V> LinkedHashMap<K, V> makeCache(int size) {
        final int CACHE_SIZE = size;
        return new LinkedHashMap<K, V>(CACHE_SIZE, 1F, true) {
            private static final long serialVersionUID = 1L;
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > CACHE_SIZE;
            }
        };
    }

    /**
     * Creates an IndexReader which will read from the given directory
     */
    public IndexReader(String dir) {
        this.dirPath = dir;
        this.readMetaData();
        this.productIndex = this.readProductIndex();
        this.tokenIndex = readTokenIndex();
        this.tokenDataIndex = readTokenDataIndex();
        this.productIds = this.readProductIds();
        this.reviewCache = makeCache(this.numOfReviews / 10);
        this.productDataCache = makeCache(this.totalAmountOfProducts / 10);
        this.tokenCache = makeCache(this.totalAmountOfTokens / 10);
        this.tokenDataCache = makeCache(this.totalAmountOfTokens / 10);
        this.tokenBinSearch = new BinarySearchToken(this.tokenIndex.length);
        this.productBinSearch = new BinarySearchProduct(this.productIds.length);
    }

    private class UpdateReviewCache implements Callable<ReviewData> {
        private final int reviewId;

        public UpdateReviewCache(int reviewId) {
            this.reviewId = reviewId;
        }

        public ReviewData call() {
            return readReview(this.reviewId);
        }
    }

    private class UpdateTokenCache implements Callable<Integer> {
        private final String token;

        public UpdateTokenCache(String token) {
            this.token = token;
        }

        public Integer call() { return tokenBinSearch.search(this.token); }
    }

    private class UpdateTokenDataCache implements Callable<TokenData> {
        private final int tokenIndex;

        public UpdateTokenDataCache(int tokenIndex) {
            this.tokenIndex = tokenIndex;
        }

        public TokenData call() {
            return readTokenData(tokenDataIndex[this.tokenIndex], getTokenDataLength(this.tokenIndex));
        }
    }

    private class UpdateProductDataCache implements Callable<ProductData> {
        private final int productDataIndex;

        public UpdateProductDataCache(int productDataIndex) {
            this.productDataIndex = productDataIndex;
        }

        public ProductData call() {
            return readProductData(productIndex[this.productDataIndex], getProductDataLength(this.productDataIndex));
        }
    }

    private static <K, V> V getValue (LinkedHashMap<K, V> cache, K key, Callable<V> readNewValue) {
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        V val = null;

        try {
            val = readNewValue.call();
        } catch (Exception e) {
            // non of our callables raise exceptions so this part of the code is unreachable
            e.printStackTrace();
        }

        if (val != null) {
            cache.put(key, val);
        }
        return val;
    }

    /**
     * Returns the product identifier for the given review
     * Returns null if there is no review with the given identifier
     */
    public String getProductId(int reviewId) {
        reviewId--;
        if (reviewId < 0 || reviewId >= this.numOfReviews) {
            return null;
        }
        ReviewData rev = getValue(this.reviewCache, reviewId, new UpdateReviewCache(reviewId));
        return this.productIds[rev.getProductIdNum()];
    }

    /**
     * Returns the score for a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewScore(int reviewId) {
        reviewId--;
        if (reviewId < 0 || reviewId >= this.numOfReviews) {
            return -1;
        }
        ReviewData rev = getValue(this.reviewCache, reviewId, new UpdateReviewCache(reviewId));
        return rev.getScore();
    }

    /**
     * Returns the numerator for the helpfulness of a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewHelpfulnessNumerator(int reviewId) {
        reviewId--;
        if (reviewId < 0 || reviewId >= this.numOfReviews) {
            return -1;
        }
        ReviewData rev = getValue(this.reviewCache, reviewId, new UpdateReviewCache(reviewId));
        return rev.getHelpNumerator();
    }

    /**
     * Returns the denominator for the helpfulness of a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewHelpfulnessDenominator(int reviewId) {
        reviewId--;
        if (reviewId < 0 || reviewId >= this.numOfReviews) {
            return -1;
        }
        ReviewData rev = getValue(this.reviewCache, reviewId, new UpdateReviewCache(reviewId));
        return rev.getHelpDenominator();
    }

    /**
     * Returns the number of tokens in a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewLength(int reviewId) {
        reviewId--;
        if (reviewId < 0 || reviewId >= this.numOfReviews) {
            return -1;
        }
        ReviewData rev = getValue(this.reviewCache, reviewId, new UpdateReviewCache(reviewId));
        return rev.getNumOfTokens();
    }

    /**
     * Return the number of reviews containing a given token (i.e., word)
     * Returns 0 if there are no reviews containing this token
     */
    public int getTokenFrequency(String token) {
        token = token.toLowerCase();
        Integer tokenIndex = getValue(this.tokenCache, token, new UpdateTokenCache(token));
        if (tokenIndex == null) {
            return 0;
        }
        return getValue(this.tokenDataCache, tokenIndex, new UpdateTokenDataCache(tokenIndex)).getNumberOfReviews();
    }

    /**
     * Return the number of times that a given token (i.e., word) appears in
     * the reviews indexed
     * Returns 0 if there are no reviews containing this token
     */
    public int getTokenCollectionFrequency(String token) {
        token = token.toLowerCase();
        Integer tokenIndex = getValue(this.tokenCache, token, new UpdateTokenCache(token));
        if (tokenIndex == null) {
            return 0;
        }
        return getValue(this.tokenDataCache, tokenIndex, new UpdateTokenDataCache(tokenIndex)).getNumberOfInstances();
    }

    /**
     * Return a series of integers of the form id-1, freq-1, id-2, freq-2, ... such
     * that id-n is the n-th review containing the given token and freq-n is the
     * number of times that the token appears in review id-n
     * Note that the integers should be sorted by id
     *
     * Returns an empty Enumeration if there are no reviews containing this token
     *
     */
    public Enumeration<Integer> getReviewsWithToken(String token) {
        token = token.toLowerCase();
        Integer tokenIndex = getValue(this.tokenCache, token, new UpdateTokenCache(token));
        if (tokenIndex == null) {
            return Collections.emptyEnumeration();
        }
        TokenData curr = getValue(this.tokenDataCache, tokenIndex, new UpdateTokenDataCache(tokenIndex));
        return curr.getEnumerator();
    }

    /**
     * Return the number of product reviews available in the system
     */
    public int getNumberOfReviews() {
        return this.numOfReviews;
    }

    /**
     * Return the number of number of tokens in the system
     * (Tokens should be counted as many times as they appear)
     */
    public int getTokenSizeOfReviews() {
        return this.totalAmountOfTokens;
    }

    /**
     * Return the ids of the reviews for a given product identifier
     * Note that the integers returned should be sorted by id
     *
     * Returns an empty Enumeration if there are no reviews for this product
     */
    public Enumeration<Integer> getProductReviews(String productId) {
        Integer productIdIndex = this.productBinSearch.search(productId);
        if (productIdIndex == null) {
            return Collections.emptyEnumeration();
        }
        return getValue(this.productDataCache, productIdIndex, new UpdateProductDataCache(productIdIndex)).getEnumorator();
    }
}
