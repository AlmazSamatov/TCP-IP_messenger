package com.example.almaz.messenger.ShannonCoding; /**
 * This class implements Shannon Coding algorithm. It has two methods: compress and decompress.
 *
 * @author Dinar Salakhutdinov
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class ShannonCodes {

    /**
     * This method gets File class object as an input. Then after performing algorithms it creates two files:
     * 1. *.SCDict file is a dictionary having this structure: first byte is reserved for the length of in how many bytes
     * each code is written, then (byte which was coded | length of the code | byte1 | [byte_i]) n times.
     * 2. *.SC file is compressed file having this structure: first byte is reserved for the last byte offset and other
     * bytes are coded bytes.
     *
     * @param file input file.
     * @return array of files with size 2. Element 0 is dictionary, element 1 is compressed file.
     * @throws IOException if problems occur.
     */
    public static File[] compressShannonCodes(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);

        // Read file by bytes and add each to the dictionary to count the number of occurrences of each byte:
        byte[] buffer = new byte[in.available()];
        in.read(buffer, 0, in.available()); // buffer storing file's contents in bytes
        in.close();
        HashMap<Byte, Integer> dictionary = new HashMap<>(); // byte -> number of occurrences
        for (int i = 0; i < buffer.length; i++) {
            if (dictionary.containsKey(buffer[i])) {
                dictionary.put(buffer[i], dictionary.get(buffer[i]) + 1);
            } else {
                dictionary.put(buffer[i], 1);
            }
        }

        ArrayList<Byte> arrayOfChars = new ArrayList<>(); // list representing bytes in specific order
        ArrayList<Double> arrayOfProbabilities = new ArrayList<>(); // list representing probabilities of bytes in order
        // as in 'arrayOfChars' array.

        // Fill arrays with contents:
        for (Map.Entry<Byte, Integer> pair : dictionary.entrySet()) {
            arrayOfChars.add(pair.getKey());
            arrayOfProbabilities.add(1.0 * pair.getValue() / buffer.length);
        }

        // Sort arrays so that most probable bytes come first:
        for (int i = 0; i < arrayOfProbabilities.size() - 1; i++) {
            for (int j = i + 1; j < arrayOfProbabilities.size(); j++) {
                if (arrayOfProbabilities.get(j) > arrayOfProbabilities.get(i)) {
                    byte tempChar = arrayOfChars.get(i);
                    arrayOfChars.set(i, arrayOfChars.get(j));
                    arrayOfChars.set(j, tempChar);

                    Double tempDouble = arrayOfProbabilities.get(i);
                    arrayOfProbabilities.set(i, arrayOfProbabilities.get(j));
                    arrayOfProbabilities.set(j, tempDouble);
                }
            }
        }

        // Create and fill list representing cumulative distribution:
        ArrayList<Double> arrayOfCumulativeDistribution = new ArrayList<>();
        arrayOfCumulativeDistribution.add(0.0);
        double currentSum = arrayOfProbabilities.get(0);
        for (int i = 1; i < arrayOfProbabilities.size(); i++) {
            arrayOfCumulativeDistribution.add(currentSum);
            currentSum += arrayOfProbabilities.get(i);
        }

        // Create array of bit sets, each representing code for a byte:
        BitSet[] arrayOfCodes = new BitSet[arrayOfChars.size()];
        byte[] arrayOfLengths = new byte[arrayOfChars.size()]; // array representing length of code for a byte
        for (int i = 0; i < arrayOfChars.size(); i++) {
            double currentCumulativeProbValue = arrayOfCumulativeDistribution.get(i);

            // length if calculated by formula:
            byte length = (byte) -(Math.log(arrayOfProbabilities.get(i)) / Math.log(2));
            length++;
            arrayOfLengths[i] = length;

            // converting to binary and then creating code for a byte by formula:
            double currentValue = currentCumulativeProbValue;
            BitSet currentCode = new BitSet();
            for (int j = 0; j < length; j++) {
                currentValue *= 2;
                if (currentValue >= 1.0) {
                    currentCode.set(j);
                    currentValue -= 1.0;
                }
            }
            arrayOfCodes[i] = currentCode;
        }

        // Write dictionary (byte - code_length - code) to output file:
        ArrayList<Byte> outBufferList = new ArrayList<>();
        byte bytesNeeded = (byte) Math.ceil(1.0 * arrayOfLengths[arrayOfLengths.length - 1] / 8); // byte representing
        // how many bytes the longest code takes.
        outBufferList.add(bytesNeeded);

        for (int i = 0; i < arrayOfCodes.length; i++) {
            outBufferList.add(arrayOfChars.get(i)); // adding which byte is coded
            outBufferList.add(arrayOfLengths[i]); // adding the length of the byte
            byte bytesAdded = 0; // how many bytes already added to buffer
            byte code = 0; // current code
            byte power = 7; // which bit of the current byte is being performed

            for (int j = 0; j < arrayOfLengths[i]; j++) {
                if (power < 0) {
                    // if byte is full, then write it to buffer and reset everything
                    outBufferList.add(code);
                    power = 7;
                    code = 0;
                    bytesAdded++;
                }
                if (arrayOfCodes[i].get(j)) code += Math.pow(2, power); // calculating code
                power--;
            }
            // addition of left bytes:
            outBufferList.add(code);
            bytesAdded++;
            for (int j = 0; j < bytesNeeded - bytesAdded; j++) {
                outBufferList.add((byte) 0);
            }
        }

        // Create output file for dictionary:
        byte[] outBufferArray = new byte[outBufferList.size()];
        for (int i = 0; i < outBufferList.size(); i++) {
            outBufferArray[i] = outBufferList.get(i);
        }
        File fileDictionary = new File(file.getPath() + ".SCDict");
        FileOutputStream outDictionary = new FileOutputStream(fileDictionary);
        outDictionary.write(outBufferArray, 0, outBufferList.size());
        outDictionary.close();


        //  Writing of compressed file contents:

        // Transfer codes array to dictionary to increase efficiency:
        HashMap<Byte, BitSet> dictOfCodes = new HashMap<>();
        for (int i = 0; i < arrayOfChars.size(); i++) {
            dictOfCodes.put(arrayOfChars.get(i), arrayOfCodes[i]);
        }

        // Transfer lengths array to dictionary to increase efficiency:
        HashMap<Byte, Byte> dictOfLengths = new HashMap<>();
        for (int i = 0; i < arrayOfChars.size(); i++) {
            dictOfLengths.put(arrayOfChars.get(i), arrayOfLengths[i]);
        }

        outBufferList = new ArrayList<>();

        byte lastOffset = 0; // this byte is used to show how many bits are used at last byte (due to reason that
        // in compressed file length of last byte can be less than 8 bit)
        outBufferList.add(lastOffset); // reserve place for offset byte

        // Fill buffer list with bytes of compressed codes:
        byte currentByte = 0;
        byte power = 7;
        boolean last = false;
        for (int i = 0; i < buffer.length; i++) { //bits to add remain
            byte currentChar = buffer[i];
            BitSet currentBitSet = dictOfCodes.get(currentChar);
            for (int j = 0; j < dictOfLengths.get(currentChar); j++) {
                last = false;
                if (power < 0) {
                    // if current byte is full. then write it to buffer and reset everything
                    outBufferList.add(currentByte);
                    power = 7;
                    currentByte = 0;
                    last = true;
                }
                if (currentBitSet.get(j)) currentByte += Math.pow(2, power);
                power--;
            }
        }
        if (!last) outBufferList.add(currentByte);

        outBufferList.set(0, power);

        outBufferArray = new byte[outBufferList.size()];
        for (int i = 0; i < outBufferList.size(); i++) {
            outBufferArray[i] = outBufferList.get(i);
        }

        File fileCompressed = new File(file.getPath() + ".SC");
        FileOutputStream out = new FileOutputStream(fileCompressed);
        out.write(outBufferArray, 0, outBufferArray.length);
        out.close();

        File[] pairOfFiles = new File[2];
        pairOfFiles[0] = fileDictionary;
        pairOfFiles[1] = fileCompressed;

        return pairOfFiles;
    }

    /**
     * This method gets two files created after compression. It performs decompression and returns initial file.
     *
     * @param file       compressed file
     * @return decompressed file
     * @throws IOException if problems occur
     */
    public static File decompressShannonCodes(File file, File dictionary) throws IOException {
        // Read dictionary file to fill dictionary of codes:
        FileInputStream in = new FileInputStream(dictionary);

        byte[] buffer = new byte[in.available()];
        in.read(buffer, 0, in.available());
        in.close();

        byte bytesCount = buffer[0]; // byte which represents the length of codes in bytes

        // Read codes:
        HashMap<String, Byte> codes = new HashMap<>();
        for (int i = 1; i < buffer.length; i++) {
            byte character = buffer[i]; //which was coded
            i++;
            byte length = buffer[i]; // length of the code
            i++;

            // Generate code from bytes found in file:
            StringBuilder codeInBinary = new StringBuilder();
            for (int j = 0; j < bytesCount; j++) {
                byte code = buffer[i];
                i++;
                String code16Temp = Integer.toBinaryString(code);
                StringBuilder code16 = new StringBuilder();
                if (code16Temp.length() < 8) {
                    for (int k = 0; k < 8 - code16Temp.length(); k++) {
                        code16.append("0");
                    }
                    code16.append(code16Temp);
                } else {
                    code16.append(code16Temp.substring(code16Temp.length() - 8, code16Temp.length()));
                }
                codeInBinary.append(code16.toString());
            }
            i--;
            codes.put(codeInBinary.substring(0, length), character);
        }

        // Read contents of compressed file:

        in = new FileInputStream(file);
        buffer = new byte[in.available()];
        in.read(buffer, 0, in.available());
        in.close();

        byte lastOffset = buffer[0];

        // Convert file contents to bit set:
        BitSet bitSet = new BitSet();
        int currentBit = 0;
        for (int i = 1; i < buffer.length; i++) {
            String convertedStringTemp = Integer.toBinaryString(buffer[i]);
            StringBuilder codeInBinary = new StringBuilder();
            if (convertedStringTemp.length() < 8) {
                for (int j = 0; j < 8 - convertedStringTemp.length(); j++) {
                    codeInBinary.append("0");
                }
                codeInBinary.append(convertedStringTemp);
            } else {
                codeInBinary.append(convertedStringTemp.substring(convertedStringTemp.length() - 8, convertedStringTemp.length()));
            }
            int lastIndex = codeInBinary.length();
            if (i == buffer.length - 1) {
                lastIndex = 7 - lastOffset;
            }
            for (int j = 0; j < lastIndex; j++) {
                if (codeInBinary.charAt(j) == '1') {
                    bitSet.set(currentBit);
                }
                currentBit++;
            }
        }

        ArrayList<Byte> outBufferList = new ArrayList<>();

        StringBuilder currentString = new StringBuilder();

        // Run through bit set and find out what was coded and write it to output buffer:
        for (int i = 0; i < currentBit; i++) {
            if (bitSet.get(i)) {
                currentString.append("1");
            } else {
                currentString.append("0");
            }

            if (codes.containsKey(currentString.toString())) {
                outBufferList.add(codes.get(currentString.toString()));
                currentString.delete(0, currentString.length());
            }
        }

        byte[] outBufferArray = new byte[outBufferList.size()];
        for (int i = 0; i < outBufferList.size(); i++) {
            outBufferArray[i] = outBufferList.get(i);
        }

        String fileName = file.getPath();
        fileName = fileName.replace(".SC", "");
        File fileDecompressed = new File(fileName);
        FileOutputStream out = new FileOutputStream(fileDecompressed);
        out.write(outBufferArray, 0, outBufferArray.length);
        out.close();

        return fileDecompressed;
    }
}
