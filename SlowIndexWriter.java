package webdata;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.io.File;

public class SlowIndexWriter {
    private static final int RESULT_PRODUCT_ID_INDEX = 0;
    private static final int RESULT_HELPFULNESS_INDEX = 1;
    private static final int RESULT_SCORE_INDEX = 2;
    private static final int RESULT_TEXT_INDEX = 3;
    private static final int LAST_INPUT_INDEX = RESULT_TEXT_INDEX;

    private static final int PARSE_SUCCESSFUL_HAS_MORE_DATA = 0;
    private static final int PARSE_SUCCESSFUL_EOF = 1;
    private static final int FAILED_TO_PARSE = -1;

    private static final String[] PARSE_NAMES = {"product/productId: ", "review/helpfulness:", "review/score: ", "review/text: "};

    private int currentReviewId;
    private BufferedReader inputBuffer;
    private String dirPath;

    private int tokensHeaderBits;
    private int productIdHeaderBits;
    private int reviewsHeaderBits;

    private int totalAmountOfTokens;
    private int totalAmountOfDiffProducts;

    private int maxHelpNumerator;
    private int maxHelpDenominator;
    private int maxNumOfTokensInReview;

    private int productStringLen;
    private int tokenDataIndexLen;
    private int productDataIndexLen;

    /**
     * token structures:
     * @tokenMapTemp - holds the initial mapping between a token string and his TokenData object
     * @tokenStringArray - an array of all the token strings
     * @tokenDataArray - an array of all the TokenData objects
     * @tokenIndexArray - every entry is a pointer to the coded string of all the token strings
     * and marks the beginning of a token string
     * @tokenDataIndexArray - every entry is a pointer to the coded string of the data in tokenDataArray
     * and marks the beginning of a token's data
     */
    private HashMap<String, TokenData> tokenMapTemp;
    private ArrayList<String> tokenStringArray;
    private ArrayList<TokenData> tokenDataArray;
    private ArrayList<Integer> tokenIndexArray;
    private ArrayList<Integer> tokenDataIndexArray;

    /**
     * product structures:
     * @productMapTemp - holds the initial mapping between a product string and his ProductData object
     * @productStringArray - an array of all the product strings
     * @productDataArray - an array of all the ProductData objects
     * @productIndexArray - every entry is a pointer to the coded string of the data in productDataArray
     * and marks the beginning of a product's data
     */
    private HashMap<String, ProductData> productMapTemp;
    private ArrayList<String> productStringArray;
    private ArrayList<ProductData> productDataArray;
    private ArrayList<Integer> productIndexArray;

    /**
     * review structures:
     * @reviewMapTemp - holds the initial mapping between a reviewID and ReviewData object
     * @reviewDataList - ArrayList of all the data of all the ReviewData objects
     */
    private ArrayList<ReviewData> reviewListTemp;
    private ArrayList<Integer> reviewDataList;

    public SlowIndexWriter() {
        this.tokenMapTemp = new HashMap<>();
        this.currentReviewId = 1;
        this.reviewListTemp = new ArrayList<>();
        this.productMapTemp = new HashMap<>();
        this.totalAmountOfTokens = 0;
        this.maxHelpNumerator = 0;
        this.maxHelpDenominator = 0;
        this.totalAmountOfDiffProducts = 0;
        this.maxNumOfTokensInReview = 0;
        initCharCoder();
    }

    private boolean dirPathCheck(String inputFile, String dir) {
        this.dirPath = dir;
        try {
            this.inputBuffer = new BufferedReader(new FileReader(inputFile));
        } catch (FileNotFoundException e) {
            System.out.println("No such file " + inputFile);
            return false;
        }

        File theDir = new File(dir);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            try {
                theDir.mkdir();
            }
            catch(SecurityException se) {
                System.out.println("Bad input " + dir);
                return false;
            }
        }
        return true;
    }

    private boolean parseReviews() {
        int ret;
        while (true) {
            ret = this.parseReview();
            if (ret != PARSE_SUCCESSFUL_HAS_MORE_DATA){
                break;
            }
        }

        if (ret == FAILED_TO_PARSE) {
            return false;
        }

        this.currentReviewId--;
        return true;
    }

    /**
     * Given product review data, creates an on disk index
     * inputFile is the path to the file containing the review data
     * dir is the directory in which all index files will be created
     * if the directory does not exist, it should be created
     */
    public void slowWrite(String inputFile, String dir) {
        if (!this.dirPathCheck(inputFile, dir)) {
            return;
        }

        if (!this.parseReviews()) {
            return;
        }

        this.tokenDone();
        this.productDone();
        this.reviewDone();

        encodeIndices();
    }

    /**
     * Delete all index files by removing the given directory
     */
    public void removeIndex(String dir) {
        File dirPtr = new File(dir);
        if (dirPtr.isDirectory()) {
            File[] files = dirPtr.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f != null) {
                        f.delete();
                    }
                }
                dirPtr.delete();
            }
        }
    }

    /**
     * This method is to be used once all the reviews has been received.
     * This method organize the tokens information for compression
     */
    private void tokenDone() {
        for (TokenData token : this.tokenMapTemp.values()) {
            token.done();
        }
        this.tokenDataArray = new ArrayList<>();
        this.tokenStringArray = new ArrayList<>();
        SortedSet<String> keys = new TreeSet<>(this.tokenMapTemp.keySet());
        for (String key : keys) {
            this.tokenStringArray.add(key);
            this.tokenDataArray.add(this.tokenMapTemp.get(key));
        }
        this.tokenMapTemp = null;
    }

    /**
     * This method is to be used once all the reviews has been received.
     * This method organize the products information for compression
     */
    private void productDone() {
        for (ProductData product : this.productMapTemp.values()) {
            product.done();
        }
        this.productDataArray = new ArrayList<>();
        this.productStringArray = new ArrayList<>();
        SortedSet<String> keys = new TreeSet<>(this.productMapTemp.keySet());
        for (String key : keys) {
            this.productStringArray.add(key);
            this.productDataArray.add(this.productMapTemp.get(key));
        }
        this.totalAmountOfDiffProducts = this.productStringArray.size();
        this.productMapTemp = null;
    }

    /**
     * This method is to be used once all the reviews has been received.
     * This method organize the products information for compression.
     * This method must be called after productDone
     */
    private void reviewDone() {
        BinarySearchProduct productIdBinFinder = new BinarySearchProduct(this.totalAmountOfDiffProducts);
        this.reviewDataList = new ArrayList<>();
        for (ReviewData rev : this.reviewListTemp) {
            rev.done(productIdBinFinder.search(rev.getProductString()));
            this.reviewDataList.addAll(rev.getArrayListPresentation());
        }
        this.reviewListTemp = null;
    }

    private class BinarySearchProduct extends BinarySearch {
        BinarySearchProduct(int dataStructLength) {
            super(dataStructLength);
        }

        public String getNextValue() {
            return productStringArray.get(this.middle);
        }
    }

    private void updateTokenMap(String token) {
        this.totalAmountOfTokens++;
        if (this.tokenMapTemp.containsKey(token)) {
            this.tokenMapTemp.get(token).update(this.currentReviewId);
        }
        else {
            this.tokenMapTemp.put(token, new TokenData(this.currentReviewId));
        }
    }

    private void updateProductMap (String productString) {
        if (this.productMapTemp.containsKey(productString)) {
            this.productMapTemp.get(productString).update(this.currentReviewId);
        }
        else {
            this.productMapTemp.put(productString, new ProductData(this.currentReviewId));
        }
    }

    private void updateReviewMap(String productId, String helpfulnessStr, String scoreStr, int numOfTokens) {
        String[] helpfulness = helpfulnessStr.toLowerCase().split("[^a-z0-9]");
        int helpNumerator = Integer.parseInt(helpfulness[0]);
        int helpDenominator = Integer.parseInt(helpfulness[1]);
        this.maxHelpNumerator = Math.max(this.maxHelpNumerator, helpNumerator);
        this.maxHelpDenominator = Math.max(this.maxHelpDenominator, helpDenominator);
        this.maxNumOfTokensInReview = Math.max(this.maxNumOfTokensInReview, numOfTokens);
        int score = (int) Double.parseDouble(scoreStr);
        ReviewData rev = new  ReviewData(productId, helpNumerator, helpDenominator, score, numOfTokens);
        this.reviewListTemp.add(rev);
    }

    /**
     * this method parse a single review from the input tile
     * @return
     * 0 if parsed the review successfully and still didn't reach the end of the file
     * 1 if parsed the review successfully and reached the end of the file
     * -1 if failed to parse the next review from the file
     */
    private int parseReview() {
        String line;
        String[] res = new String[4];
        int resIndex = 0;
        int retVal = PARSE_SUCCESSFUL_HAS_MORE_DATA;
        boolean flag =  false, firstRun = true;
        try {
            while (true) {
                line = this.inputBuffer.readLine();
                if (line == null) {
                    if (firstRun) {
                        return PARSE_SUCCESSFUL_EOF;
                    }
                    retVal = PARSE_SUCCESSFUL_EOF;
                    this.inputBuffer.close();
                    break;
                }
                firstRun = false;
                if (line.equals("") && flag) {
                    break;
                }
                if (resIndex <= LAST_INPUT_INDEX && line.startsWith(PARSE_NAMES[resIndex])) {
                    res[resIndex] = line.substring(PARSE_NAMES[resIndex].length()).trim();
                    if(resIndex == LAST_INPUT_INDEX) {
                        flag = true;
                    }
                    resIndex++;
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to parse file");
            e.printStackTrace();
            return FAILED_TO_PARSE;
        }
        String[] tokenList = res[RESULT_TEXT_INDEX].toLowerCase().replaceAll("[\\W]|_", " ").split(" +");
        this.updateReviewMap(res[RESULT_PRODUCT_ID_INDEX],
                res[RESULT_HELPFULNESS_INDEX],
                res[RESULT_SCORE_INDEX],
                tokenList.length);
        this.updateProductMap(res[RESULT_PRODUCT_ID_INDEX]);
        for (int i = 0; i < tokenList.length; i++) {
            if (tokenList[i].length() > 0) {
                this.updateTokenMap(tokenList[i]);
            }
        }
        this.currentReviewId++;
        return retVal;
    }

    private void writeMetadata() {
        int [] metadataArray = {
            this.currentReviewId,
            this.totalAmountOfTokens,
            this.totalAmountOfDiffProducts,
            this.maxHelpNumerator,
            this.maxHelpDenominator,
            this.productStringLen,
            this.tokenDataIndexLen,
            this.productDataIndexLen,
            this.tokensHeaderBits,
            this.productIdHeaderBits,
            this.reviewsHeaderBits,
            this.maxNumOfTokensInReview
        };
        ReadWriteUtils.writeToFile(ReadWriteUtils.intArrToByteArr(metadataArray), this.dirPath + ReadWriteUtils.METADATA_FILE);
    }

    //////////////////////////////////////////////////////////////////
    // ENCODING     ENCODING     ENCODING     ENCODING     ENCODING
    //////////////////////////////////////////////////////////////////

    /**
     * Inits the hash map that gives every char, it's binary code.
     */
    private void initCharCoder() {
        ReadWriteUtils.charCoder = new HashMap<>();
        char[] abc = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        for (int i = 0; i < abc.length; i++) {
            ReadWriteUtils.charCoder.put(abc[i], (byte)i);
        }
    }

    /**
     * Take the set of product ids, compress it, and writes it to the disk
     */
    private void encodeProductIds() {
        String finalBinNumToWrite = "";
        for (String productString : this.productStringArray) {
            for (int i = 0; i < productString.length(); i++) {
                finalBinNumToWrite += ReadWriteUtils.charToBinRep(productString.charAt(i));
            }
        }

        int uselessBits = ReadWriteUtils.BYTE_SIZE - (finalBinNumToWrite.length() % ReadWriteUtils.BYTE_SIZE);
        uselessBits = (uselessBits == ReadWriteUtils.BYTE_SIZE) ? 0: uselessBits;
        byte[] toWrite = ReadWriteUtils.binaryRepToByteArr(finalBinNumToWrite, uselessBits);
        ReadWriteUtils.writeToFile(toWrite, this.dirPath + ReadWriteUtils.PRODUCT_ID_FILE);
        this.productStringLen = toWrite.length;
        this.productIdHeaderBits =  uselessBits;
    }

    /**
     * Take the set of Tokens, compress it, and writes it to the disk
     * Also saves pointers to each token
     */
    private void encodeTokens() {
        String finalBinNumToWrite = "";
        int curPtr = 0;
        this.tokenIndexArray = new ArrayList<>();
        for (Iterator<String> iterator = this.tokenStringArray.iterator(); iterator.hasNext();) {
            this.tokenIndexArray.add(curPtr);
            String token = iterator.next();
            for (int j = 0; j < token.length(); j++) {
                char curChar = token.charAt(j);
                finalBinNumToWrite += ReadWriteUtils.charToBinRep(curChar);
            }
            curPtr += ReadWriteUtils.BITS_PER_CHAR * token.length();
        }
        int uselessBits = ReadWriteUtils.BYTE_SIZE - (finalBinNumToWrite.length() % ReadWriteUtils. BYTE_SIZE);
        uselessBits = (uselessBits == ReadWriteUtils.BYTE_SIZE) ? 0: uselessBits;
        byte[] toWrite = ReadWriteUtils.binaryRepToByteArr(finalBinNumToWrite, uselessBits);
        this.tokensHeaderBits = uselessBits;
        ReadWriteUtils.writeToFile(toWrite, this.dirPath + ReadWriteUtils.TOKENS_FILE);
    }

    /**
     * Take the set of Reviews, compress it, and writes it to the disk
     */
    private void encodeReviews() {
        int bitsToRep[] = new int[ReviewData.NUM_OF_FIELDS];
        bitsToRep[0] = ReadWriteUtils.getNumOfBitsToRepresent(this.totalAmountOfDiffProducts);
        bitsToRep[1] = ReadWriteUtils.getNumOfBitsToRepresent(this.maxHelpNumerator);
        bitsToRep[2] = ReadWriteUtils.getNumOfBitsToRepresent(this.maxHelpDenominator);
        bitsToRep[3] = ReadWriteUtils.BITS_FOR_SCORE; //numberBetween 1 to 5
        bitsToRep[4] = ReadWriteUtils.getNumOfBitsToRepresent(this.maxNumOfTokensInReview);
        // 'SUM(bitsToRep)' bits per review
        String finalBinNumToWrite = "";
        for (int i = 0; i < this.reviewDataList.size(); i++) {
            int numOfBits = bitsToRep[i % 5];
            String tempBinRep = ReadWriteUtils.longToBinary(reviewDataList.get(i), ReadWriteUtils.BYTE_SIZE);
            finalBinNumToWrite += ReadWriteUtils.binNumSize(tempBinRep, numOfBits);
        }

        int uselessBits = ReadWriteUtils.BYTE_SIZE - (finalBinNumToWrite.length() % ReadWriteUtils.BYTE_SIZE);
        uselessBits = (uselessBits == ReadWriteUtils.BYTE_SIZE) ? 0: uselessBits;
        byte[] toWrite = ReadWriteUtils.binaryRepToByteArr(finalBinNumToWrite, uselessBits);
        ReadWriteUtils.writeToFile(toWrite, this.dirPath + ReadWriteUtils.REVIEW_FILE);
        this.reviewsHeaderBits = uselessBits;
    }

    /**
     * Take the product data for each product, compress it, and writes it to the disk
     * Also saves index to the each token
     */
    private void encodeProductsData() {
        ArrayList<Byte> byteArr = new ArrayList<>();
        ArrayList<Integer> curProductData;
        this.productIndexArray = new ArrayList<>();
        int curPtr = 0;
        for (int i = 0; i < this.productDataArray.size(); i++) {
            curProductData = this.productDataArray.get(i).getArrayListPresentation();
            this.productIndexArray.add(curPtr);
            byte[] x = ReadWriteUtils.variantEncoding(curProductData);
            for (int k = 0; k < x.length; k++) {
                byteArr.add(x[k]);
            }
            curPtr += x.length;
        }
        this.productDataIndexLen = curPtr;

        byte[] finalByte = new byte[byteArr.size()];
        for (int i = 0; i < finalByte.length; i++) {
            finalByte[i] = byteArr.get(i);
        }
        ReadWriteUtils.writeToFile(finalByte, this.dirPath + ReadWriteUtils.PRODUCT_DATA_FILE);
    }

    /**
     * Take the product index for each product, compress it, and writes it to the disk
     */
    private void encodeProductIndex() {
        byte[] finalByte = ReadWriteUtils.variantEncoding(this.productIndexArray);
        ReadWriteUtils.writeToFile(finalByte,  this.dirPath + ReadWriteUtils.PRODUCT_INDEX_FILE);
    }

    /**
     * Take the token data index for each token, compress it, and writes it to the disk
     */
    private void encodeTokenDataIndex() {
        byte[] finalByte = ReadWriteUtils.variantEncoding(this.tokenDataIndexArray);
        ReadWriteUtils.writeToFile(finalByte,  this.dirPath + ReadWriteUtils.TOKENS_DATA_INDEX_FILE);
    }

    /**
     * Take the token index for each token, compress it, and writes it to the disk
     */
    private void encodeTokenIndex() {
        byte[] finalByte = ReadWriteUtils.variantEncoding(this.tokenIndexArray);
        ReadWriteUtils.writeToFile(finalByte,  this.dirPath + ReadWriteUtils.TOKENS_INDEX_FILE);
    }

    /**
     * Take the token data for each token, compress it, and writes it to the disk
     */
    private void encodeTokensData() {
        ArrayList<Byte> byteArr = new ArrayList<>();
        ArrayList<Integer> curTokenData;
        this.tokenDataIndexArray = new ArrayList<>();
        int curPtr = 0;
        for (int i = 0; i < this.tokenDataArray.size(); i++) {
            curTokenData = this.tokenDataArray.get(i).getArrayListPresentation();
            this.tokenDataIndexArray.add(curPtr);
            byte[] x = ReadWriteUtils.variantEncoding(curTokenData);
            for (int k = 0; k < x.length; k++) {
                byteArr.add(x[k]);
            }
            curPtr += x.length;
        }
        this.tokenDataIndexLen = curPtr;
        byte[] finalByte = new byte[byteArr.size()];
        for (int i = 0; i < finalByte.length; i++) {
            finalByte[i] = byteArr.get(i);
        }
        ReadWriteUtils.writeToFile(finalByte, this.dirPath + ReadWriteUtils.TOKENS_DATA_FILE);
    }

    private void encodeIndices() {
        encodeProductIds();
        encodeProductsData();
        encodeProductIndex();

        encodeTokens();
        encodeTokensData();
        encodeTokenIndex();
        encodeTokenDataIndex();

        encodeReviews();

        writeMetadata();
    }
}
