import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.zip.DataFormatException;

public class pngSuite {

    //id.IHDR.name() for ex, gives me the string

    enum id{
        IHDR,
        PLTE,
        IDAT,
        IEND
    }

    public class signature {
        byte[] data;

        public signature(byte[] data) {
            this.data = data;
        }

        public signature() {
            this.data = new byte[8];
        }
    }


    public boolean isValidType(String input) {
        //System.out.println(input.equals(id.IHDR.name()));

        if(input.equals(id.IHDR.name())){
            return true;
        }
        if(input.equals(id.PLTE.name())){
            return true;
        }
        if(input.equals(id.IDAT.name())){
            return true;
        }
        if(input.equals(id.IEND.name())){
            return true;
        }

        return false;
    }

    public class chunk {

        int len;
        String type;
        byte[] data;
        byte[] crc;

        public chunk(int len, String type, byte[] data, byte[] crc) {
            this.len = len;
            this.type = type;
            this.data = data;
            this.crc = crc;
        }

        public chunk() {

        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public void setLen(int len) {
            this.len = len;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setCrc(byte[] crc) {
            this.crc = crc;
        }
    }

    public class pixel {
        int x;
        int y;
        int cBit;


        public pixel(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void setcBit(int bit) {
            if (bit == 0) cBit = 0;
            if (bit == 1) cBit = 1;
        }
    }

    public pixel convertToGreyScale(Byte[] b) {
        pixel p = new pixel(0, 0);

        if (b.length != 3) {

        } else {

            double red;
            double green;
            double blue;

            red = ((int) b[0]) * 0.299;
            green = ((int) b[1]) * 0.587;
            blue = ((int) b[2]) * 0.114;


        }

        return p;
    }

    public int returnDominantIndex(int p1, int p2, int p3, int p4) {
        HashMap<Integer, Integer> pixelFrequencyMap = new HashMap<>();
        pixelFrequencyMap.put(p1, 1);
        if (pixelFrequencyMap.get(p2) != null) {
            pixelFrequencyMap.put(p2, pixelFrequencyMap.get(p2) + 1);
        } else {
            pixelFrequencyMap.put(p2, 1);
        }
        if (pixelFrequencyMap.get(p3) != null) {
            pixelFrequencyMap.put(p3, pixelFrequencyMap.get(p3) + 1);
        } else {
            pixelFrequencyMap.put(p3, 1);
        }
        if (pixelFrequencyMap.get(p4) != null) {
            pixelFrequencyMap.put(p4, pixelFrequencyMap.get(p4) + 1);
        } else {
            pixelFrequencyMap.put(p4, 1);
        }

        int maxF = Integer.MIN_VALUE;
        int index = 2;

        for (int i : pixelFrequencyMap.keySet()) {
            if (pixelFrequencyMap.get(i) >= maxF) {
                maxF = pixelFrequencyMap.get(i);
                index = i;
            }
        }
        System.out.println("Return value is: " + index);
        return index;
    }

    public ArrayList<chunk> pngParse(byte[] buffer) {

        //declare the arr to be returned
        ArrayList<chunk> retVal = new ArrayList<>();

        //first decode the byte array

        //make the signature
        signature signature = new signature(Arrays.copyOfRange(buffer,0,7));

        String base64ByteString = new String(buffer);

        int byteArrPointer = 8;

        int base64ByteStringPointer = 8;


        //byte arr and string should be the same length?? 1 char = 1 byte

        //System.out.println(buffer.length + " " + base64ByteString.length());


        while (base64ByteStringPointer < base64ByteString.length()-4) {

            if (isValidType(base64ByteString.substring(base64ByteStringPointer, base64ByteStringPointer + 4))){
                System.out.println("found chunk");
                if(base64ByteString.substring(base64ByteStringPointer, base64ByteStringPointer + 4).equals("IEND")){
                    chunk end = new chunk();
                    end.setData(null);
                    end.setType("IEND");
                    end.setLen(0);

                    retVal.add(end);
                    break;
                }

                //begin new chunk
                chunk chunk = new chunk();

                //need to go back for length

                int lenPointer = base64ByteStringPointer - 4;

                String type = base64ByteString.substring(base64ByteStringPointer, base64ByteStringPointer + 4);

                int len = 0;

                byte[] uLen = base64ByteString.substring(lenPointer, base64ByteStringPointer).getBytes();

                for (byte b : uLen) { len = (len << 8) + (b & 0xFF); }

                //need to convert len to binary?


                //need to go forward for type and then data

                base64ByteStringPointer = base64ByteStringPointer + 4;

                //Parse data

                int dataPointer = 0;
                byte[] data = new byte[100000];

                while (dataPointer < len) {
                    data[dataPointer] = buffer[base64ByteStringPointer];
                    dataPointer++;
                    base64ByteStringPointer++;
                }
                //parse crc
                byte[] crc = new byte[4];

                int crcPointer = base64ByteStringPointer;

                dataPointer = 0;

                while (crcPointer < base64ByteStringPointer + 4) {
                    crc[dataPointer] = buffer[crcPointer];
                    dataPointer++;
                    crcPointer++;
                }

                base64ByteStringPointer = crcPointer;

                chunk.setType(type);
                chunk.setCrc(crc);
                chunk.setData(data);
                chunk.setLen(len);

                retVal.add(chunk);

            }

            base64ByteStringPointer++;

        }


        return retVal;

    }


    public void handle() throws IOException, DataFormatException {
        File file = new File("..refactored_png_suite\\src\\trans flag.png");
        byte[] bytes = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //parse out the chunks

        ArrayList<chunk> result = pngParse(bytes);

        for(int i = 0; i < result.size(); i++){
            System.out.println("Data Length: " + result.get(i).len + " Data type: " + result.get(i).type);
        }
    }

    public static void main(String[] args) throws DataFormatException, IOException {
        pngSuite p = new pngSuite();
        p.handle();
    }
}