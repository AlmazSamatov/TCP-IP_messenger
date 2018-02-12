package com.example.almaz.messenger.HammingCoding;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class Hamming {

    private byte[] data;
    private boolean[][] bits;
    private boolean[][] bitsToDecode;
    private boolean[][] syndromes;
    private boolean[][] decoded;
    private boolean[][] mistakes;
    private byte[] encodedData;
    private boolean[] bitsToDecode2;

    public Hamming(String pathToFileToDecode, String pathToFileEncoded) throws IOException {
        encode(pathToFileToDecode, pathToFileEncoded);
    }

    public Hamming() {
    }

    public void encode(String pathToFileToDecode, String pathToFileEncoded) throws IOException {
        this.data = readFile(pathToFileToDecode);
        bits = toBit(data);
        calculateParity();
        sendBitsWithMistakes();
        writeToFile(bitsToDecode, pathToFileEncoded);
    }

    public void decode(String pathToFileEncoded, String pathToDecoded) throws IOException {
        fillMistakesTable();
        this.encodedData = readFile(pathToFileEncoded);
        bitsToDecode2 = toEncodedBit(encodedData);
        bitsToDecode = oneDToTwoD(bitsToDecode2);
        calculateSyndrome();
        analyseSyndrome();
        writeToFile(decoded, pathToDecoded);
    }

    private void fillMistakesTable() {
        mistakes = new boolean[8][4];
        mistakes[3][3] =true; // i4
        mistakes[5][0] =true; // i1
        mistakes[6][2] =true; //i3
        mistakes[7][1] =true; //i2
    }


    private byte[] readFile(String pathToFile) throws IOException {
        File file = new File(pathToFile);
        byte[] buff = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(pathToFile);
        BufferedInputStream buf = new BufferedInputStream(fileInputStream);
        buf.read(buff, 0, buff.length);
        fileInputStream.close();
        buf.close();
        return buff;
    }

    private boolean[][] toBit(byte[] bytes){
        boolean[][] matrix;
        matrix = new boolean[bytes.length][14];
        for (int i = 0; i < bytes.length; i++){
            matrix[i] = byteToBoolArr(bytes[i]);
        }
        return matrix;
    }
    private boolean[] toEncodedBit(byte[] bytes){
        boolean[][] matrix;
        matrix = new boolean[bytes.length][8];
        for (int i = 0; i < bytes.length; i++){
            matrix[i] = byteToBoolArr2(bytes[i]);
        }
        boolean[] array = flatArr(matrix);
        return array;
    }

    private void calculateSyndrome(){
        syndromes = new boolean[bitsToDecode.length][6];
        for (int i = 0; i < bitsToDecode.length; i++){
            boolean r1 = bitsToDecode[i][8];
            boolean r2 = bitsToDecode[i][9];
            boolean r3 = bitsToDecode[i][10];
            boolean r4 = bitsToDecode[i][11];
            boolean r5 = bitsToDecode[i][12];
            boolean r6 = bitsToDecode[i][13];
            syndromes[i][0] =r1^ bitsToDecode[i][0] ^ bitsToDecode[i][1] ^ bitsToDecode[i][2];
            syndromes[i][1] =r2^ bitsToDecode[i][1] ^ bitsToDecode[i][2] ^ bitsToDecode[i][3];
            syndromes[i][2] =r3^ bitsToDecode[i][0] ^ bitsToDecode[i][1] ^ bitsToDecode[i][3];

            syndromes[i][3] =r4^ bitsToDecode[i][4] ^ bitsToDecode[i][5] ^ bitsToDecode[i][6];
            syndromes[i][4] =r5^ bitsToDecode[i][5] ^ bitsToDecode[i][6] ^ bitsToDecode[i][7];
            syndromes[i][5] =r6^ bitsToDecode[i][4] ^ bitsToDecode[i][5] ^ bitsToDecode[i][7];

        }
    }

    void analyseSyndrome(){
        decoded = new boolean[bitsToDecode.length][8];
        for (int i = 0; i <syndromes.length; i++ ){
            boolean corrupted1 = false;
            for (int j= 0; j < 3; j++ ){
                boolean a = syndromes[i][j];
                if (a){
                   corrupted1 = true;
                }
            }
            if (corrupted1){
                detectMistake(i,1);
            }
            else setDecoded(i, 1);

            boolean corrupted2 = false;
            for (int j= 3; j < 5; j++ ){
                boolean a = syndromes[i][j];
                if (a){
                    corrupted2 = true;
                }
            }
            if (corrupted2){
                detectMistake(i,2);
            }
            else setDecoded(i, 2);

        }

    }

    private void setDecoded(int i, int i1) {
        if(i1 == 1){
            for (int j = 0; j < 4; j++){
                decoded[i][j] = bitsToDecode[i][j];
            }

        }
        else {
            for (int j = 4; j < 8; j++){
                decoded[i][j] = bitsToDecode[i][j];
            }
        }
    }

    private void detectMistake(int i, int i1) {
        boolean[] syndrome;
        syndrome = new boolean[3];
        for (int j = 0; j < syndrome.length; j++){
            if(i1 == 1){
                syndrome[j] = syndromes[i][j];
            }
            else {
                syndrome[j] = syndromes[i][j+3];
            }
        }
        int k = booleansToInt(syndrome);
        for (int j = 0; j < 4; j++){
            if (i1 == 1){
                decoded[i][j] = mistakes[k][j]^bitsToDecode[i][j] ;
            }
            else {
                decoded[i][j+4] = mistakes[k][j]^bitsToDecode[i][j+4] ;
            }

        }



    }

    // ^ == XOR
    void calculateParity(){
        for (int i = 0; i < bits.length; i++){
            bits[i][8]= bits[i][0] ^ bits[i][1] ^ bits[i][2];
            bits[i][9]= bits[i][1] ^ bits[i][2] ^ bits[i][3];
            bits[i][10]= bits[i][0] ^ bits[i][1] ^ bits[i][3];

            bits[i][11]=  bits[i][4] ^ bits[i][5] ^ bits[i][6];
            bits[i][12]= bits[i][5] ^ bits[i][6] ^ bits[i][7];
            bits[i][13]= bits[i][4] ^ bits[i][5] ^ bits[i][7];
        }

    }


    /*
    * Utility functions
    * */

    void sendBitsWithMistakes(){
        bitsToDecode = bits;
        bitsToDecode[0][3] = !bitsToDecode[0][3];
    }

    int booleansToInt(boolean[] arr){
        int n = 0;
        for (boolean b : arr)
            n = (n << 1) | (b ? 1 : 0);
        return n;
    }

    public boolean[] byteToBoolArr(byte b) {
        boolean boolArr[] = new boolean[14];
        for(int i=0;i<8;i++) boolArr[i] = (b & (byte)(128 / Math.pow(2, i))) != 0;
        return boolArr;
    }

    public boolean[] byteToBoolArr2(byte b) {
        boolean boolArr[] = new boolean[8];
        for(int i=0;i<8;i++) boolArr[i] = (b & (byte)(128 / Math.pow(2, i))) != 0;
        return boolArr;
    }

    public boolean[][] oneDToTwoD(boolean[] arr){
        int row = arr.length/14;
        int col = 14;
        boolean[][] temp = new boolean[arr.length/14][14];
        int i = 0;
        for(int r=0; r<row; r++){
            for( int c=0; c<col; c++){
                temp[r][c]=arr[i++];
            }
        }
        return temp;
    }

    void writeToFile(boolean[][] arr, String path) throws IOException {
        boolean[] flatArr = flatArr(arr);
        flatArr = arrMod8(flatArr);
        byte[] myByteArray = toBytes(flatArr);
        FileOutputStream fos = new FileOutputStream(path);
        fos.write(myByteArray);
        fos.close();
    }
    boolean[] arrMod8(boolean[] arr){
        if((arr.length % 8) != 0){
            boolean[] temp = new boolean[arr.length % 8 + arr.length ];
             for(int i = 0; i < arr.length; i++){
                 temp[i] = arr[i];
             }
             return temp;
        }else return arr;
    }

    boolean[] flatArr(boolean[][] arr){
        boolean[] oneDArray = new boolean[arr.length*arr[0].length];
        int s = 0;
        for(int i = 0; i < arr.length; i ++)
            for(int j = 0; j < arr[i].length; j ++){
                oneDArray[s] = arr[i][j];
                s++;
            }
        return oneDArray;
    }

    public byte[] toBytes(boolean[] input) {
        byte[] toReturn = new byte[input.length / 8];
        for (int entry = 0; entry < toReturn.length; entry++) {
            for (int bit = 0; bit < 8; bit++) {
                if (input[entry * 8 + bit]) {
                    toReturn[entry] |= (128 >> bit);
                }
            }
        }

        return toReturn;
    }


    void printBits(boolean[][] bits){
        System.out.print("***START***\n");
        for (int i = 0; i <bits.length; i++ ){
            //System.out.print(bits[i].toString() + "\n");
            for (int j = 0; j < bits[i].length; j++){

                System.out.print(bits[i][j] + " ");
                if (j == 3 || j==7 || j == 10){
                    System.out.print("\n");
                }
            }
            System.out.print("\n");
        }
        System.out.print("***END***\n\n\n\n");
    }
    void printSyndromes(boolean[][] bits){
        System.out.print("***START SYNDROME***\n");
        for (int i = 0; i <bits.length; i++ ){
            for (int j = 0; j < bits[i].length; j++){

                System.out.print(bits[i][j] + " ");
                if(j ==2){
                    System.out.print("\n");
                }
            }
            System.out.print("\n");
        }
        System.out.print("***END***\n\n\n\n");
    }

    void printMatrix(boolean[][] matrix){
        System.out.print("Print matrix: \n");
        for (int i = 0; i < matrix.length; i++){
            System.out.print(i + ": ") ;
            for (int j = 0; j < matrix[i].length; j++){
                System.out.print( matrix[i][j] + " ");
            }
            System.out.print("\n");
        }
        System.out.print("End of matrix\n");
    }

}
