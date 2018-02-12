package com.example.almaz.messenger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.Random;

public class Noise {
    public static File generateNoise(File file, double probability) throws IOException{
        FileInputStream in = new FileInputStream(file);
        byte[] buffer = new byte[in.available()];
        in.read(buffer, 0, in.available());
        in.close();
        BitSet bitset = BitSet.valueOf(buffer);

        Random random = new Random();

        for (int i = 0; i < (int) (1.0 * bitset.length() * probability); i++) {
            int index = random.nextInt(bitset.length());
            bitset.flip(index);
        }

        FileOutputStream out = new FileOutputStream(file);
        out.write(bitset.toByteArray());
        out.close();

        return file;
    }
}
