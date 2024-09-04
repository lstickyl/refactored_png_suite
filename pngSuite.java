import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.IDN;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.zip.DataFormatException;

public class pngSuite {

    //id.IHDR.name() for ex, gives me the string

    enum id {
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

        if (input.equals(id.IHDR.name())) {
            return true;
        }
        if (input.equals(id.PLTE.name())) {
            return true;
        }
        if (input.equals(id.IDAT.name())) {
            return true;
        }
        if (input.equals(id.IEND.name())) {
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

        public void printDataSegment() {
            if (this.data != null) {
                System.out.println(bytesToHexString(this.data));
            }
        }

        public void printCRC() {
            if (this.crc != null) {
                System.out.println(bytesToHexString(this.crc));
            }
        }
    }

    public class IHDR extends chunk {
        int width;
        int height;
        int bit_depth;
        int color_type;
        int compression_method;
        int filter_method;
        int interlace_method;

        public IHDR() {

        }

        public IHDR(chunk c) {
            setLen(c.len);
            setData(c.data);
            setType(c.type);
            setCrc(c.crc);

            this.height = Integer.parseInt(bytesToHexString(Arrays.copyOfRange(c.data, 0, 4)), 16);
            this.width = Integer.parseInt(bytesToHexString(Arrays.copyOfRange(c.data, 4, 8)), 16);
            this.bit_depth = Integer.parseInt(bytesToHexString(Arrays.copyOfRange(c.data, 8, 9)), 16);
            this.color_type = Integer.parseInt(bytesToHexString(Arrays.copyOfRange(c.data, 9, 10)), 16);
            this.compression_method = Integer.parseInt(bytesToHexString(Arrays.copyOfRange(c.data, 10, 11)), 16);
            this.filter_method = Integer.parseInt(bytesToHexString(Arrays.copyOfRange(c.data, 11, 12)), 16);
            this.interlace_method = Integer.parseInt(bytesToHexString(Arrays.copyOfRange(c.data, 12, 13)), 16);
        }

        public void printImageData() {
            if (this.data != null) {
                if (this.data.length == 13) {
                    System.out.println("----image specs---");
                    System.out.println("Chunk type: " + this.type + " " + "Chunk Data Length: " + this.len);
                    System.out.println("Height is: " + height);
                    System.out.println("Width is: " + width);
                    System.out.println("Bit depth is: " + bit_depth);
                    System.out.println("Color type is: " + color_type);
                    System.out.println("Compression method is: " + compression_method);
                    System.out.println("Filter method is: " + filter_method);
                    System.out.println("Interlace method is: " + interlace_method);
                    System.out.println("----image specs---");
                }
            }
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

    public pixel convertToGreyScale(Byte[] b, int bitDepth) {
        //2 bit depth = 4 bits
        //24 bit depth = 3 bytes

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

    //aux function to give me the hex values of a given range of bytes
    public String bytesToHexString(byte[] b) {
        StringBuilder s = new StringBuilder(b.length * 2);

        for (int i = 0; i < b.length; i++) {
            s.append(String.format("%02x", (b[i] & 0xFF)));
        }

        return s.toString();
    }


    public String hexStringToASCII(String hex) {

        String retVal = "";

        for (int i = 0; i < hex.length(); i = i + 2) {
            int hexToIntVal = Integer.parseInt(hex.substring(i, i + 2), 16);
            if (hexToIntVal >= 65 && hexToIntVal <= 90 || hexToIntVal >= 97 && hexToIntVal <= 122) {
                retVal += (char) hexToIntVal;
            } else {
                return "";
            }
        }
        return retVal;
    }

    public ArrayList<chunk> pngParse(byte[] buffer) {

        System.out.println("----Parsing-------");

        //declare the arr to be returned
        ArrayList<chunk> retVal = new ArrayList<>();

        //first decode the byte array

        //make the signature
        signature signature = new signature(Arrays.copyOfRange(buffer, 0, 7));

        int base64ByteStringPointer = 8;

        //byte arr and string should be the same length?? 1 char = 1 byte

        while (base64ByteStringPointer < buffer.length) {

            if (isValidType(hexStringToASCII(bytesToHexString(Arrays.copyOfRange(buffer, base64ByteStringPointer, base64ByteStringPointer + 4))))) {
                String potentialVal = hexStringToASCII(bytesToHexString(Arrays.copyOfRange(buffer, base64ByteStringPointer, base64ByteStringPointer + 4)));
                if (potentialVal.equals("IEND")) {
                    chunk end = new chunk();
                    end.setData(null);
                    end.setType("IEND");
                    end.setLen(0);

                    //find crc of IEND

                    byte[] crc = Arrays.copyOfRange(buffer, base64ByteStringPointer + 4, base64ByteStringPointer + 8);
                    end.setCrc(crc);

                    retVal.add(end);
                    break;
                }

                //begin new chunk
                chunk chunk = new chunk();

                //need to go back for length

                //int lenPointer = base64ByteStringPointer - 4;

                String type = potentialVal;

                int len = 0;

                byte[] uLen = Arrays.copyOfRange(buffer, base64ByteStringPointer - 4, base64ByteStringPointer);

                for (byte b : uLen) {
                    len = (len << 8) + (b & 0xFF);
                }

                //need to convert len to binary?

                //need to go forward for type and then data

                base64ByteStringPointer = base64ByteStringPointer + 4;

                //Parse data

                int dataPointer = 0;
                byte[] data = new byte[len];

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
		//Insert your path
        File file = new File("Your path here");
        byte[] bytes = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //parse out the chunks

        ArrayList<chunk> result = pngParse(bytes);

        System.out.println("List of Chunks: ");
        for (int i = 0; i < result.size(); i++) {
            if (!result.get(i).type.equals("IHDR")) {
                System.out.println("Chunk type: " + result.get(i).type + " " + "Chunk Data Length: " + result.get(i).len);
            } else {
                IHDR ihdr = new IHDR(result.get(i));
                result.set(i, ihdr);
                ((IHDR) result.get(i)).printImageData();
            }
            //result.get(i).printDataSegment();
            //result.get(i).printCRC();
        }

        System.out.println("----End Of--------");

    }

    public static void main(String[] args) throws DataFormatException, IOException {
        pngSuite p = new pngSuite();
        p.handle();
    }
}