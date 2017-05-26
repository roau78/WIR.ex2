package webdata;

import java.io.IOException;
import java.util.Enumeration;

public class Main {
	static final String INPUT_DIR = "/home/roau78/Documents/ex2/ex2/src/webdata/";
	
    static final String INPUT_FILE = INPUT_DIR + "1000.txt";
    static final String OUT_DIR = "/home/roau78/Documents/ex2/ex2/tempFiles";
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
    	SlowIndexWriter x = new SlowIndexWriter();
        x.slowWrite(INPUT_FILE, OUT_DIR);
    }
    
//    public static void main8(String[] args) throws IOException, ClassNotFoundException {
//    	RandomAccessFile raf = new RandomAccessFile("/home/tester/qq.txt", "r");
////    	raf.seek(2677842232);
//    	
//    	System.out.println((char)raf.read());
//    	
//    	/*
//    	BufferedReader inputBuffer = new BufferedReader(new FileReader("/home/tester/qq.txt"));
//    	String k = inputBuffer.readLine();
//    	System.out.println(k.length());
//    	*/
//    	
//    	
//    }
    
    
    public static void main00(String[] args) throws IOException, ClassNotFoundException {

        SlowIndexWriter x = new SlowIndexWriter();
        x.slowWrite(INPUT_FILE, OUT_DIR);
        IndexReader y = new IndexReader("/home/tester/workspace/info_ret/new");
        System.out.println("--------------------------------");
        long startTime = System.nanoTime();

        System.out.println("product id:" + y.getProductId(0)); // null
        System.out.println("product id:" + y.getProductId(1)); // B001E4KFG0
        System.out.println("product id:" + y.getProductId(2)); // B00813GRG4
        System.out.println("product id:" + y.getProductId(4)); // B000UA0QIQ
        System.out.println("product id:" + y.getProductId(5)); // B006K2ZZ7K
        System.out.println("product id:" + y.getProductId(105)); // null

        long estimatedTime = System.nanoTime() - startTime;
        System.out.println("estimatedTime:" + estimatedTime + " in nano sec"); // 208159

        System.out.println("--------------------------------");


        System.out.println("score id:" + y.getReviewScore(0)); // -1
        System.out.println("score id:" + y.getReviewScore(1)); // 5
        System.out.println("score id:" + y.getReviewScore(2)); // 1
        System.out.println("score id:" + y.getReviewScore(4)); // 2
        System.out.println("score id:" + y.getReviewScore(5)); // 5
        System.out.println("score id:" + y.getReviewScore(105)); // -1
        System.out.println("--------------------------------");

        System.out.println("Numerator id:" + y.getReviewHelpfulnessNumerator(0)); // -1
        System.out.println("Numerator id:" + y.getReviewHelpfulnessNumerator(1)); // 1
        System.out.println("Numerator id:" + y.getReviewHelpfulnessNumerator(2)); // 0
        System.out.println("Numerator id:" + y.getReviewHelpfulnessNumerator(4)); // 3
        System.out.println("Numerator id:" + y.getReviewHelpfulnessNumerator(5)); // 0
        System.out.println("Numerator id:" + y.getReviewHelpfulnessNumerator(105)); // -1
        System.out.println("--------------------------------");

        System.out.println("Denominator id:" + y.getReviewHelpfulnessDenominator(0)); // -1
        System.out.println("Denominator id:" + y.getReviewHelpfulnessDenominator(1)); // 1
        System.out.println("Denominator id:" + y.getReviewHelpfulnessDenominator(2)); // 0
        System.out.println("Denominator id:" + y.getReviewHelpfulnessDenominator(4)); // 3
        System.out.println("Denominator id:" + y.getReviewHelpfulnessDenominator(5)); // 0
        System.out.println("Denominator id:" + y.getReviewHelpfulnessDenominator(105)); // -1
        System.out.println("--------------------------------");

        startTime = System.nanoTime();

        System.out.println("Frequency token:" + y.getTokenFrequency("WArdroBe")); // 1
        System.out.println("Frequency token:" + y.getTokenFrequency("taffy")); // 4
        System.out.println("Frequency token:" + y.getTokenFrequency("because")); // 11
        System.out.println("Frequency token:" + y.getTokenFrequency("watermelon")); // 1
        System.out.println("Frequency token:" + y.getTokenFrequency("fdgdfgdfgdfgdfgdfg")); // 0
        System.out.println("Frequency token:" + y.getTokenFrequency("i")); // 76

        estimatedTime = System.nanoTime() - startTime;
        System.out.println("estimatedTime:" + estimatedTime + " in nano sec"); // 2418886

        System.out.println("--------------------------------");

        System.out.println("Collection Frequency token:" + y.getTokenCollectionFrequency("WArdroBe")); // 1
        System.out.println("Collection Frequency token:" + y.getTokenCollectionFrequency("because")); // 13
        System.out.println("Collection Frequency token:" + y.getTokenCollectionFrequency("taffy")); // 8
        System.out.println("Collection Frequency token:" + y.getTokenCollectionFrequency("watermelon")); // 1
        System.out.println("Collection Frequency token:" + y.getTokenCollectionFrequency("fdgdfgdfgdfgdfgdfg")); // 0
        System.out.println("Collection Frequency token:" + y.getTokenCollectionFrequency("i")); // 195
        System.out.println("--------------------------------");

        startTime =  System.nanoTime();

        Enumeration<Integer> array4 = y.getReviewsWithToken("taffy");
        estimatedTime = System.nanoTime() - startTime;
        System.out.println("estimatedTime:" + estimatedTime + " in nano sec"); // 397644

        while (array4.hasMoreElements()){
            System.out.println("reviews with token taffy:"+ array4.nextElement());
        } // (5,3),(6,3),(7,1),(8,1)



        System.out.println("--------------------------------");

        Enumeration<Integer> array5 = y.getReviewsWithToken("WArdroBe");
        while (array5.hasMoreElements()){
            System.out.println("reviews with token WArdroBe:"+ array5.nextElement());
        } // (3,1)
        System.out.println("--------------------------------");

        System.out.println("length id:" + y.getReviewLength(0)); // -1
        System.out.println("length id:" + y.getReviewLength(1)); // 39
        System.out.println("length id:" + y.getReviewLength(2)); // 25
        System.out.println("length id:" + y.getReviewLength(3)); // 63
        System.out.println("length id:" + y.getReviewLength(4)); // 35
        System.out.println("length id:" + y.getReviewLength(5)); // 20
        System.out.println("length id:" + y.getReviewLength(105)); // -1
        System.out.println("--------------------------------");

        System.out.println("tokens:" + y.getTokenSizeOfReviews()); // 6766
        System.out.println("--------------------------------");

        Enumeration<Integer> array1 = y.getProductReviews("B001E4KFG0");
        while (array1.hasMoreElements()){
            System.out.println("reviews with productId B001E4KFG0:"+ array1.nextElement());
        } // 1

        System.out.println("--------------------------------");


        Enumeration<Integer> array2 = y.getProductReviews("B001GVISJM");
        while (array2.hasMoreElements()){
            System.out.println("reviews with productId B001GVISJM:"+array2.nextElement());
        } // 14-28

        System.out.println("--------------------------------");

        Enumeration<Integer> array3 = y.getProductReviews("B0fdgdfgfdgdfg01GVISJM"); // null
        if (array3.hasMoreElements()){
            System.out.println("suppose to be empty");
        }

//        x.removeIndex(OUT_DIR);
    }
}
