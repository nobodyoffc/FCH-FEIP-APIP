package fcTools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cryptoTools.SHA;
import io.netty.buffer.Unpooled;
import javaTools.BytesTools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Iterator;

public class ParseTools {

    public static VarintResult parseVarint(ByteArrayInputStream blockInputStream) throws IOException {
        //Byte[] List for merge all bytes readed./用于保存所读取字节数组的列表。
        ArrayList<byte[]> bl = new ArrayList<byte[]>();
        //Read 1 byte and turn it into unsigned./读1个字节并转换成整型。
        byte[] b = new byte[1];
        blockInputStream.read(b);
        bl.add(b);

        //log.debug("Paring varint. first byte is :{}",(int)b[0]);

        int size = Byte.toUnsignedInt(b[0]);
        long number = 0;

        if (size >= 0 && size <= 252) {
            number = (long) size;

        } else if (size == 253) {
            byte[] f = new byte[2];
            blockInputStream.read(f);
            bl.add(f);
            number = Unpooled.wrappedBuffer(f).readUnsignedShortLE();

        } else if (size == 254) {
            byte[] f = new byte[4];
            blockInputStream.read(f);
            bl.add(f);
            number = Unpooled.wrappedBuffer(f).readUnsignedIntLE();
        } else {
            byte[] f = new byte[8];
            blockInputStream.read(f);
            bl.add(f);
            number = Unpooled.wrappedBuffer(f).readLongLE();
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

    public static String hashTxo(String tHash, int index, boolean isRawOrder) {

        String verseTHash = BytesTools.bytesToHexStringBE(BytesTools.invertArray(BytesTools.hexToByteArray(tHash)));

        byte[] txHashBytes = BytesTools.invertArray(BytesTools.hexToByteArray(isRawOrder ? verseTHash : tHash));
        byte[] fromIndexBytes = new byte[4];
        fromIndexBytes = BytesTools.invertArray(BytesTools.intToByteArray(index));
        String oHash = BytesTools.bytesToHexStringLE(
                SHA.Sha256x2(
                        BytesTools.bytesMerger(txHashBytes, fromIndexBytes)
                ));
        return oHash;
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

    public static String gsonString(Object ob) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        return gson.toJson(ob);
    }

    public static void gsonPrint(Object ob) {
        System.out.println("***********\n" + ob.getClass().toString() + ": " + gsonString(ob) + "\n***********");
        return;
    }

    public static void waitForNewItemInDirectory(String directoryPathStr) {
        try {
            Path directory = Paths.get(directoryPathStr);

            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        if (kind != StandardWatchEventKinds.OVERFLOW) {
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;

                            //Path changedFilePath = directory.resolve(ev.context());
                            //System.out.printf("Event kind: %s. File affected: %s.%n", kind, changedFilePath);
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
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

    public static String strToJson(String rawStr) {

        if (!rawStr.contains("{")) return null;

        int begin = rawStr.indexOf("{");

        String goodStr = rawStr.substring(begin);

        goodStr.replaceAll("\r|\n|\t", "");

        return goodStr;
    }

    public static class VarintResult {
        public long number;
        public byte[] rawBytes;
    }
}
