package webdata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;

public class ReadWriteUtils {
    //sizes
    static final int BYTE_SIZE = 8;
    static final int BITS_PER_CHAR = 6;
    static final int CHARS_AT_PRODUCT = 10;
    static final int BITS_FOR_SCORE = 3;

    //files names
    static final String PRODUCT_ID_FILE = "/product_id.bin";
    static final String PRODUCT_DATA_FILE = "/product_data.bin";
    static final String PRODUCT_INDEX_FILE = "/product_index.bin";
    static final String REVIEW_FILE = "/review_id.bin";
    static final String TOKENS_FILE = "/tokens_id.bin";
    static final String TOKENS_DATA_FILE = "/tokens_data.bin";
    static final String TOKENS_INDEX_FILE = "/tokens_index.bin";
    static final String TOKENS_DATA_INDEX_FILE = "/tokens_data_index.bin";
    static final String METADATA_FILE = "/metadata.bin";

    static final int NUM_OF_REVIEWS_INDEX = 0;
    static final int TOTAL_AMOUNT_OF_TOKENS_INDEX = 1;
    static final int TOTAL_AMOUNT_OF_PRODUCTS_INDEX = 2;
    static final int MAX_HELP_NUMERATOR_INDEX = 3;
    static final int MAX_HELP_DENOMINATOR_INDEX = 4;
    static final int TOKEN_STINRG_LENGTH_INDEX = 5;
    static final int TOKEN_DATA_LENGTH_INDEX = 6;
    static final int PRODUCT_DATA_LENGTH_INDEX = 7;
    static final int TOKENS_HEADER_BITS = 8;
    static final int PRODUCT_ID_HEADER_BITS = 9;
    static final int REVIEW_HEADER_BITS = 10;
    static final int MAX_NUM_OF_TOKENS_IN_REVIEW_INDEX = 11;

    static HashMap<Character, Byte> charCoder;

    public static boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if(!file.delete()){
                System.out.println("Failed to delete " + file.getName());
                return false;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean sortedFilesMerger(String filePath1, String filePath2, String outfilePath, int bufSize) {
        try {
            BufferedReader f1 = new BufferedReader(new FileReader(filePath1));
            BufferedReader f2 = new BufferedReader(new FileReader(filePath2));
            BufferedWriter bw = new BufferedWriter(new FileWriter(outfilePath));
            String[] buf = new String[bufSize];
            String p1 = f1.readLine(), p2 = f2.readLine();
            int index = 0, res;
            while(true) {
                if (p1 == null && p2 == null) {
                    for (int i = 0; i < index; i++) {
                        bw.write(buf[i] + "\n");
                    }
                    break;
                } else if (p1 == null) {
                    buf[index] = p2;
                    p2 = f2.readLine();
                } else if (p2 == null) {
                    buf[index] = p1;
                    p1 = f1.readLine();
                } else {
                    res = p1.compareTo(p2);
                    if (res == 0) {
                        buf[index] = p1;
                        p1 = f1.readLine();
                        p2 = f2.readLine();
                    } else if (res < 0) {
                        buf[index] = p1;
                        p1 = f1.readLine();
                    } else {
                        buf[index] = p2;
                        p2 = f2.readLine();
                    }
                }
                index++;
                if (index == bufSize) {
                    String words = "";
                    for (String word : buf) {
                        words += word + "\n";
                    }
                    bw.write(words);
                    index = 0;
                }
            }
            f1.close();
            f2.close();
            bw.close();
        } catch (IOException e) {
            System.out.println("Merger failed");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //////////////////////////////////////////////////////////////////////////
    //  Write functions		Write functions		Write functions
    //////////////////////////////////////////////////////////////////////////

    static int writeToFile(byte[] data, String path) {
        FileOutputStream fout;
        try {
            fout = new FileOutputStream(path);
            fout.write(data);
            fout.close();
            return data.length;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    static byte[] variantEncoding(ArrayList<Integer> arr) {
        int unitSize = BYTE_SIZE - 1; //should be 7
        ArrayList<Byte> retVec = new ArrayList<>();
        byte[] retArr = null;

        for (int i = 0; i < arr.size(); i++) {
            String binRep = longToBinary(arr.get(i), unitSize);

            int bitsToAdd  = unitSize - (binRep.length() % unitSize);
            for (int j = 0; j < bitsToAdd; j++) {
                binRep = "0" + binRep;
            }

            int numOfExpectdBytes = binRep.length() / unitSize;
            for (int j = 0; j < numOfExpectdBytes; j++) {
                String beforeBin = binRep.substring(j * unitSize, j * unitSize + unitSize);

                if(j == numOfExpectdBytes - 1) {
                    retVec.add(binaryRepToByte("1" + beforeBin));
                }
                else {
                    retVec.add(binaryRepToByte("0" + beforeBin));
                }
            }
        }

        int count = 0;
        retArr = new byte[retVec.size()];
        for(byte curByte: retVec) retArr[count++] = curByte;

        return retArr;
    }

    //////////////////////////////////////////////////////////////////////////
    //  Read functions	Read functions	Read functions	Read functions
    //////////////////////////////////////////////////////////////////////////

    static byte[] readAllFile(String path) {
        try {
            FileInputStream fout= new FileInputStream(path);
            int len = fout.available();

            byte[] b = new byte[len];
            fout.read(b);
            fout.close();
            return b;
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static byte[] readFromFile(String path, int offset, int bytesToread) {
        try {
            byte[] b = new byte[bytesToread];
            RandomAccessFile raf = new RandomAccessFile(path, "r");
            raf.seek(offset);
            for (int i = 0; i < bytesToread; i++) {
                try {
                    b[i] = raf.readByte();
                }
                catch (EOFException e) {}
            }
            raf.close();
            return b;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    static int[] variantDecoding(byte[] readArr) {
        ArrayList<Integer> retVec = new ArrayList<>();
        int[] retArr = null;

        String curNumBinRep = "";
        for (byte b : readArr) {
            String binRep = byteToBinary(b);
            curNumBinRep += binRep.substring(1, BYTE_SIZE);
            if(binRep.charAt(0) == '1'){
                retVec.add(Integer.parseInt(curNumBinRep, 2));
                curNumBinRep = "";
            }
        }

        int count = 0;
        retArr = new int[retVec.size()];
        for(int curNum: retVec) retArr[count++] = curNum;

        return retArr;
    }

    //////////////////////////////////////////////////////////////////////////
    //  Interpretation functions	Interpretation functions
    //////////////////////////////////////////////////////////////////////////

    static byte[] intArrToByteArr(int[] arr) {
        byte[] ret = new byte[arr.length * 4];
        int index = 0;
        for (Integer val: arr) {
            byte[] b = ByteBuffer.allocate(4).putInt(val).array();
            ret[index] = b[0];
            ret[index + 1] = b[1];
            ret[index + 2] = b[2];
            ret[index + 3] = b[3];
            index += 4;
        }
        return ret;
    }

    static int[] byteArrToIntArr(byte[] b) {
        int[] ret = new int[b.length / 4];
        int index = 0, num;
        for (int i = 0; i < b.length; i += 4) {
            num = b[i] << 24 | (b[i + 1] & 0xff) << 16 | (b[i + 2] & 0xff) << 8 | (b[i + 3] & 0xff);
            ret[index] = num;
            index++;
        }
        return ret;
    }

    static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    static int getNumOfBitsToRepresent(int num) {
        if(num > 0) {
            return (int) (Math.log(num) / Math.log(2)) + 1;
        }
        return 0;
    }

    static int getNumOfBytesToRepresent(int num) {
        int bits = getNumOfBitsToRepresent(num);
        if(bits % 8 != 0) {
            return (bits / 8) + 1;
        }
        return bits / 8;
    }

    static byte binaryRepToByte(String binRep) {
        return (byte) Integer.parseInt(binRep, 2);
    }

    static byte[] binaryRepToByteArr(String binRep, int uslessBits) {
        if(uslessBits != 0){ //if we have more bits, add full byte
            for (int i = 0; i < uslessBits; i++) {
                binRep = "0" + binRep;
            }
        }
        int size = binRep.length() / BYTE_SIZE;
        byte[] retArr = new byte[size];
        int i;
        for (i = 0; i < binRep.length(); i += BYTE_SIZE) {
            String curByteStr = binRep.substring(i, i + BYTE_SIZE);
            byte curByte =  binaryRepToByte(curByteStr);
            retArr[i/BYTE_SIZE] = curByte;
        }
        return retArr;
    }

    static String byteToBinary (byte b) {
        return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }

    static byte[] longToByteArr(long num) {
        BigInteger bi = BigInteger.valueOf(num);
        byte[] bytes0 = bi.toByteArray();
        byte[] bytes = new byte[bytes0.length - 1];
        if(bytes0[0] == 0) {
            for (int i = 0; i < bytes0.length - 1; i++) {
                bytes[i] = bytes0[i+1];
            }
        }
        else {
            bytes = bytes0.clone();
        }
        return bytes;
    }

    static String longToBinary(long num, int byteSize) {
        return Long.toBinaryString(num);
    }

    static String charToBinRep(char chr) {
        String bitsRep = "";
        chr = Character.toLowerCase(chr);
        byte charCode = charCoder.get(chr);
        bitsRep += byteToBinary(charCode).
            substring(BYTE_SIZE - BITS_PER_CHAR , BYTE_SIZE); //only 6 bits
        return bitsRep;
    }

    static void printByteMode(String binRep, int byteSize) {
        for (int i = 0; i < binRep.length(); i++) {
            if(i % byteSize == 0 && i != 0){
                System.out.print("_");
            }
            System.out.print(binRep.substring(i, i+1));
        }
    }

    static String binNumSize(String binNum, int wantedSize) {
        int binNumSize = binNum.length();
        int zeroesToadd = wantedSize - binNumSize;
        for (int i = 0; i < zeroesToadd; i++) {
            binNum = "0" + binNum;
        }
        return binNum;
    }

    static String getRange(int firstBit, int lengthAtBits, String path) {
        int LastBit = firstBit + lengthAtBits;
        int firstByte = firstBit / BYTE_SIZE;
        int lastByte = LastBit / BYTE_SIZE;
        int firstBitFromTheFirstByte = firstBit % BYTE_SIZE;
        int lastBitFromTheLastByte = LastBit % BYTE_SIZE;

        int numberOfBytesToRead = lastByte - firstByte + 1;
        byte[] b = readFromFile(path, firstByte, numberOfBytesToRead);

        String binaryRep = "";
        //handle first byte
        binaryRep = byteToBinary(b[0]).substring(firstBitFromTheFirstByte, BYTE_SIZE);

        for (int i = 1; i < b.length - 1; i++) { //except the first and the last
            binaryRep += byteToBinary(b[i]);
        }

        //handle last byte
        if(b.length > 1) {
            binaryRep += byteToBinary(b[b.length - 1]).substring(0, lastBitFromTheLastByte);
        }
        else {
            binaryRep = binaryRep.substring(0, lastBitFromTheLastByte);
        }
        return binaryRep;
    }
}
