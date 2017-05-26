package webdata;

import java.io.IOException;
import java.util.Enumeration;

public class Main {
	static final String INPUT_DIR = "/home/roau78/Documents/WIR/ex2/src/webdata/";
	
    static final String INPUT_FILE = INPUT_DIR + "1000.txt";
    static final String OUT_DIR = "/home/roau78/Documents/WIR/ex2/tempFiles";
    
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

    public static void handleError(String msg) throws Exception{
    	throw new Exception(msg);
    }
    
    
    //NOTICE!! works only on "100.txt" file
    public static void main00(String[] args) throws Exception, IOException, ClassNotFoundException {

        SlowIndexWriter x = new SlowIndexWriter();
        x.slowWrite(INPUT_DIR + "100.txt", OUT_DIR); //NOTICE!! works only on "100.txt" file
        IndexReader y = new IndexReader(OUT_DIR);
        System.out.println("--------------------------------");
        
        
//        long startTime = System.nanoTime();
//        long estimatedTime = System.nanoTime() - startTime;
//        System.out.println("estimatedTime:" + estimatedTime + " in nano sec"); // 208159

        
        
        if(y.getProductId(0) != null) handleError("");
        if(!y.getProductId(1).equals("B001E4KFG0")) handleError("");
        if(!y.getProductId(2).equals("B00813GRG4")) handleError("");
        if(!y.getProductId(4).equals("B000UA0QIQ")) handleError("");
        if(!y.getProductId(5).equals("B006K2ZZ7K")) handleError("");
        if(y.getProductId(105) != null) handleError("");


    	System.out.println("getProductId Passed");
        System.out.println("--------------------------------");


        if(y.getReviewScore(0) != -1) handleError("");
        if(y.getReviewScore(1) != 5) handleError("");
        if(y.getReviewScore(2) != 1) handleError("");
        if(y.getReviewScore(4) != 2) handleError("");
        if(y.getReviewScore(5) != 5) handleError("");
        if(y.getReviewScore(105) != -1) handleError("");

    	System.out.println("getReviewScore Passed");
        System.out.println("--------------------------------");


        if(y.getReviewHelpfulnessNumerator(0) != -1) handleError("");
        if(y.getReviewHelpfulnessNumerator(1) != 1) handleError("");
        if(y.getReviewHelpfulnessNumerator(2) != 0) handleError("");
        if(y.getReviewHelpfulnessNumerator(4) != 3) handleError("");
        if(y.getReviewHelpfulnessNumerator(5) != 0) handleError("");
        if(y.getReviewHelpfulnessNumerator(105) != -1) handleError("");

    	System.out.println("getReviewHelpfulnessNumerator Passed");
        System.out.println("--------------------------------");

        if(y.getReviewHelpfulnessDenominator(0) != -1) handleError("");
        if(y.getReviewHelpfulnessDenominator(1) != 1) handleError("");
        if(y.getReviewHelpfulnessDenominator(2) != 0) handleError("");
        if(y.getReviewHelpfulnessDenominator(4) != 3) handleError("");
        if(y.getReviewHelpfulnessDenominator(5) != 0) handleError("");
        if(y.getReviewHelpfulnessDenominator(105) != -1) handleError("");

    	System.out.println("getReviewHelpfulnessDenominator Passed");
        System.out.println("--------------------------------");


        if(y.getTokenFrequency("WArdroBe") != 1) handleError("");
        if(y.getTokenFrequency("taffy") != 4) handleError("");
        if(y.getTokenFrequency("because") != 11) handleError("");
        if(y.getTokenFrequency("watermelon") != 1) handleError("");
        if(y.getTokenFrequency("fdgdfgdfgdfgdfgdfg") != 0) handleError("");
        if(y.getTokenFrequency("i") != 78) handleError("");


    	System.out.println("getTokenFrequency Passed");
        System.out.println("--------------------------------");

        if(y.getTokenCollectionFrequency("WArdroBe") != 1) handleError("");
        if(y.getTokenCollectionFrequency("because") != 13) handleError("");
        if(y.getTokenCollectionFrequency("taffy") != 8) handleError("");
        if(y.getTokenCollectionFrequency("watermelon") != 1) handleError("");
        if(y.getTokenCollectionFrequency("fdgdfgdfgdfgdfgdfg") != 0) handleError("");
        if(y.getTokenCollectionFrequency("i") != 227) handleError("");
        
    	System.out.println("getTokenCollectionFrequency Passed");
        System.out.println("--------------------------------");


        Enumeration<Integer> array4 = y.getReviewsWithToken("taffy");
        Integer[] array4answar = {5,3,6,3,7,1,8,1}; // (5,3),(6,3),(7,1),(8,1)
        int i = 0;
        while (array4.hasMoreElements()){
        	if(array4.nextElement() != array4answar[i]) handleError("taffy at i=" + i);
        	i++;
        } 


        Enumeration<Integer> array5 = y.getReviewsWithToken("WArdroBe");
        Integer[] array5answar = {3,1};// (3,1)
        i = 0;
        while (array5.hasMoreElements()){
        	if(array5.nextElement() != array5answar[i]) handleError("WArdroBe at i=" + i);
        	i++;
        }

    	System.out.println("getReviewsWithToken Passed");
        System.out.println("--------------------------------");
        
        
        if(y.getReviewLength(0) != -1) handleError("");
        if(y.getReviewLength(1) != 48) handleError("");
        if(y.getReviewLength(2) != 32) handleError("");
        if(y.getReviewLength(3) != 93) handleError("");
        if(y.getReviewLength(4) != 41) handleError("");
        if(y.getReviewLength(5) != 27) handleError("");
        if(y.getReviewLength(105) != -1) handleError("");
    	System.out.println("getReviewLength Passed");
        System.out.println("--------------------------------");
        
        

        if(y.getTokenSizeOfReviews() != 6903) handleError("");
    	System.out.println("getTokenSizeOfReviews Passed");
        System.out.println("--------------------------------");

        
        
        
        Enumeration<Integer> array1 = y.getProductReviews("B001E4KFG0");
        
        Integer[] array1answar = {1}; // 1
        i = 0;
        while (array1.hasMoreElements()){
        	if(array1.nextElement() != array1answar[i]) handleError("B001E4KFG0 at i=" + i);
        	i++;
        }


        Enumeration<Integer> array2 = y.getProductReviews("B001GVISJM");
        
        Integer[] array2answar = {14,15,16,17,18,19,20,21,22,23,24,25,26,27,28}; // 1
        i = 0;
        while (array2.hasMoreElements()){
        	if(array2.nextElement() != array2answar[i]) handleError("B001GVISJM at i=" + i);
        	i++;
        }


        Enumeration<Integer> array3 = y.getProductReviews("B0fdgdfgfdgdfg01GVISJM"); // null
        if (array3.hasMoreElements()){
        	handleError("suppose to be empty");
        }

    	System.out.println("getProductReviews Passed");
        System.out.println("--------------------------------");
        
        x.removeIndex(OUT_DIR);

    	System.out.println("\n\nTEST PASSED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }
}
