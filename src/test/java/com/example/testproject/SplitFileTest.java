package com.example.testproject;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;

@SpringBootTest
public class SplitFileTest {

    String fileName = "README.md";

    String location = "/Users/dongin-kim/test/";

    String path  = location + fileName;

    RandomAccessFile raf;

    {
        try {
            raf = new RandomAccessFile(path, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    long numSplits = 10; //TODO 분할 갯수 (BYTE 에 따른 분할갯수 조정 기능 추가해야됨.
    long sourceSize;

    {
        try {
            sourceSize = raf.length();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    long bytesPerSplit = sourceSize/numSplits ;
    long remainingBytes = sourceSize % numSplits;
    int maxReadBufferSize = 8 * 1024; //8KB


    @SneakyThrows
    @Test
    public void splitFile(){

        for(int destIx=1; destIx <= numSplits; destIx++) {
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream("split."+destIx));
            if(bytesPerSplit > maxReadBufferSize) {
                long numReads = bytesPerSplit/maxReadBufferSize;
                long numRemainingRead = bytesPerSplit % maxReadBufferSize;
                for(int i=0; i<numReads; i++) {
                    readWrite(raf, bw, maxReadBufferSize);
                }
                if(numRemainingRead > 0) {
                    readWrite(raf, bw, numRemainingRead);
                }
            }else {
                readWrite(raf, bw, bytesPerSplit);
            }
            bw.close();
        }
        if(remainingBytes > 0) {
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream("split."+(numSplits+1)));
            readWrite(raf, bw, remainingBytes);
            bw.close();
        }
        raf.close();

    }

    static void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if(val != -1) {
            bw.write(buf);
        }
    }


}
