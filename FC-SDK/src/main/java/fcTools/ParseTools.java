package fcTools;

import com.google.gson.Gson;
import constants.Constants;
import cryptoTools.SHA;
import javaTools.BytesTools;
import javaTools.NumberTools;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static constants.Constants.FchToSatoshi;

public class ParseTools {

    public static VarintResult parseVarint(ByteArrayInputStream blockInputStream) throws IOException {
        //Byte[] List for merge all bytes read./用于保存所读取字节数组的列表。
        ArrayList<byte[]> bl = new ArrayList<byte[]>();
        //Read 1 byte and turn it into unsigned./读1个字节并转换成整型。
        byte[] b = new byte[1];
        blockInputStream.read(b);
        bl.add(b);

        //log.debug("Paring varint. first byte is :{}",(int)b[0]);

        int size = Byte.toUnsignedInt(b[0]);
        long number = 0;

        /*
            Value	        Storage length	    Format
            < 0xFD	        1	                uint8_t
            <= 0xFFFF	    3	                0xFD followed by the length as uint16_t
            <= 0xFFFFFFFF	5	                0xFE followed by the length as uint32_t
            -	            9	                0xFF followed by the length as uint64_t
         */

        if (size >= 0 && size <= 252) {
            number = (long) size;

        } else if (size == 253) {
            byte[] f = new byte[2];
            blockInputStream.read(f);
            bl.add(f);
            number = BytesTools.bytes2ToIntLE(f);//Unpooled.wrappedBuffer(f).readUnsignedShortLE();

        } else if (size == 254) {
            byte[] f = new byte[4];
            blockInputStream.read(f);
            bl.add(f);
            number = BytesTools.bytes2ToIntLE(f);
        } else {
            byte[] f = new byte[8];
            blockInputStream.read(f);
            bl.add(f);
            number = BytesTools.bytes2ToIntLE(f);
            System.exit(0);
        }
        //For return./将要返回的值。
        byte[] mergeBytes = BytesTools.bytesMerger(bl);

        VarintResult varint = new VarintResult();
        varint.rawBytes = mergeBytes;
        varint.number = number;

        return varint;
    }

    public static long cdd(long value, long birthTime, long spentTime) {
        return Math.floorDiv(value * Math.floorDiv((spentTime - birthTime), (60 * 60 * 24)), 100000000);
    }

    public static <T> ArrayList<T> deepListCopy(ArrayList<T> origList, Class<T> class1) {
        ArrayList<T> destList = new ArrayList<T>();
        Gson gson = new Gson();
        Iterator<T> iterAddr = origList.iterator();
        while (iterAddr.hasNext()) {
            T bm = iterAddr.next();
            String bmJson = gson.toJson(bm);
            T am = gson.fromJson(bmJson, class1);
            destList.add(am);
        }
        return destList;
    }

    public static String calcTxoIdFromBytes(byte[] b36PreTxIdAndIndex) {
        String txoId = BytesTools.bytesToHexStringLE(SHA.Sha256x2(b36PreTxIdAndIndex));
        return txoId;
    }

    public static String calcTxoId(String txId, int j) {
        byte[] txIdBytes = BytesTools.invertArray(BytesTools.hexToByteArray(txId));
        byte[] b4OutIndex = new byte[4];
        b4OutIndex = BytesTools.invertArray(BytesTools.intToByteArray(j));
        String outId = BytesTools.bytesToHexStringLE(
                SHA.Sha256x2(
                        BytesTools.bytesMerger(txIdBytes, b4OutIndex)
                ));
        return outId;
    }

    public static void waitForChangeInDirectory(String directoryPathStr, AtomicBoolean running) {
        try {
            Path directory = Paths.get(directoryPathStr);

            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

                while (running.get()) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        if (kind != StandardWatchEventKinds.OVERFLOW) {
                            if (event.context() instanceof Path) {
                                WatchEvent<Path> ev = (WatchEvent<Path>) event;
//                                System.out.println("Path affected: " + ev.context() + ", event kind: " + ev.kind());
                                return;
                            }
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            }
        } catch (IOException | InvalidPathException e) {
            System.err.println("Error while watching directory: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Waiting for directory interrupted: " + e.getMessage());
        }
    }

    public static void waitForNewItemInFile(String filePathStr) {
        try {
            Path filePath = Paths.get(filePathStr);
            Path directory = filePath.getParent();
            if (directory == null) {
                directory = Paths.get(".");
            }

            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        if (kind != StandardWatchEventKinds.OVERFLOW) {
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path changedFilePath = directory.resolve(ev.context());

                            if (changedFilePath.equals(filePath)) {
                                return;
                            }
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error while watching file: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Waiting for file interrupted: " + e.getMessage());
        }
    }

    public static void waitForNewItemInFileOld(String filePathStr) {
        try {
            Path filePath = Paths.get(filePathStr);
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path directory = filePath.getParent();
            if (directory == null) {
                directory = Paths.get(".");
            }
            directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path changedFilePath = directory.resolve(ev.context());

                    if (changedFilePath.equals(filePath)) {
                        return;
                    }
                }
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error while watching file: " + e.getMessage());
        }
    }

    public static double satoshiToFch(long satoshis){
        return roundDouble8((double) satoshis /FchToSatoshi);
    }

    public static double weiToDouble(long longValue){
        return roundDouble16((double) longValue /(FchToSatoshi*FchToSatoshi));
    }
    public static long doubleToWei(double wei){
        return (long)(wei*FchToSatoshi*FchToSatoshi);
    }

    public static long fchToSatoshi(double fch){
        return (long)(fch*FchToSatoshi);
    }

    public static long coinToSatoshi(double amount) {
        BigDecimal coins = BigDecimal.valueOf(amount);
        BigDecimal satoshis = coins.multiply(new BigDecimal(100000000)); // Convert BTC to Satoshis
        return satoshis.setScale(0, RoundingMode.HALF_UP).longValueExact(); // Set scale to 0 (no fractional part) and use HALF_UP rounding
    }

    public static double satoshiToCoin(long satoshis) {
        return NumberTools.roundDouble8((double) satoshis / 100000000);
    }

    public static double roundDouble8(double raw){
        long i = 0;
        i = (long) (raw*FchToSatoshi);
        return (double) i/FchToSatoshi;
    }

    public static double roundDouble16(double raw){
        long i = 0;
        i = (long) (raw*FchToSatoshi*FchToSatoshi);
        return (double) i/(FchToSatoshi*FchToSatoshi);
    }

    public static double roundDouble4(double raw){
        long i = 0;
        i = (long) (raw*10000);
        double j = (double) i / 10000;
//        System.out.println(raw + " is rounded to "+ j);
        return j;
    }

    public static double roundDouble2(double raw){
        long i = 0;
        i = (long) (raw*100);
        double j = (double) i / 100;
//        System.out.println(raw + " is rounded to "+ j);
        return j;
    }

    public static double bitsToDifficulty(long bits) {
        // Decode the "bits" field
        int exponent = (int) ((bits >> 24) & 0xff);
        long mantissa = bits & 0xffffff;
        BigInteger target = BigInteger.valueOf(mantissa).shiftLeft((exponent - 3) * 8);

        // The maximum target based on the original Bitcoin difficulty adjustment system
        BigInteger maxTarget = new BigInteger("00000000FFFF0000000000000000000000000000000000000000000000000000", 16);

        // Calculate the difficulty as the ratio of the max target to the current target
        return maxTarget.divide(target).doubleValue();
    }

    public static double difficultyToHashRate(double difficulty){
        return difficulty * Constants.TWO_POWER_32/60;
    }

    public static double bitsToHashRate(long bits){
        return difficultyToHashRate(bitsToDifficulty(bits));
    }
    @Test
    public void test(){
        roundDouble2(1.343);
        return;
    }

    public static boolean isGoodShare(String consumeViaShare) {
        int index = consumeViaShare.indexOf('.');
        String str = consumeViaShare.substring(index+1);
        return str.length() <= 4;
    }

    public static boolean isGoodShareMap(Map<String, String> map) {
        long sum = 0;
        for(String key: map.keySet()){
            String valueStr = map.get(key);
            Double valueDb;
            try{
                valueDb = Double.parseDouble(valueStr);
                if(valueDb>1){
                    System.out.println("Share can't bigger than 1. "+key+"="+valueDb+ "Reset this share map.");
                    return false;
                }
                valueDb = roundDouble8(valueDb);
                sum += ((long)(valueDb*100));
            }catch (Exception ignore){}
        }
        System.out.println("The sum of shares is "+sum +"%");
        if(sum!=100){
            System.out.println("Builder shares didn't sum up to 100%. Reset it.");
            return false;
        }
        return true;
    }

    public static class VarintResult {
        public long number;
        public byte[] rawBytes;
    }

    public static String convertTimestampToDate(long timestamp) {
        if(timestamp<10000000000L){
            timestamp=timestamp*1000;
        }
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

}
