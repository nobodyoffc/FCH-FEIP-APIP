import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class P2PListener {

    private static final String NODE_IP = "127.0.0.1";
    private static final int NODE_PORT = 8333;

    public static void main(String[] args) throws IOException {
        // Connect to the Bitcoin node
        Socket socket = new Socket(InetAddress.getByName(NODE_IP), NODE_PORT);
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

        // Send the version message
        sendVersionMessage(outputStream);

        // Listen for messages
        while (true) {
            // Read message header
            byte[] header = new byte[24];
            inputStream.readFully(header);

            // Get the message length and command
            int length = ByteBuffer.wrap(header, 16, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            String command = new String(header, 4, 12).trim();

            // Read the message payload
            byte[] payload = new byte[length];
            inputStream.readFully(payload);

            if ("tx".equals(command)) {
                System.out.println("New transaction: " + bytesToHex(payload));
            }
        }
    }

    private static void sendVersionMessage(DataOutputStream outputStream) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(85).order(ByteOrder.LITTLE_ENDIAN);

        // Version (4 bytes)
        buffer.putInt(70015);

        // Services (8 bytes)
        buffer.putLong(0);

        // Timestamp (8 bytes)
        buffer.putLong(System.currentTimeMillis() / 1000L);

        // AddrRecv (26 bytes)
        buffer.put((byte) 1); // Services
        buffer.put(new byte[16]); // IP address
        buffer.putShort((short) 8333); // Port

        // AddrTrans (26 bytes)
        buffer.put((byte) 1); // Services
        buffer.put(new byte[16]); // IP address
        buffer.putShort((short) 8333); // Port

        // Nonce (8 bytes)
        buffer.putLong(0);

        // User agent (1 byte)
        buffer.put((byte) 0);

        // Start height (4 bytes)
        buffer.putInt(0);

        // Relay transactions (1 byte)
        buffer.put((byte) 1);

        byte[] payload = buffer.array();

        // Message header
        ByteBuffer header = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN);
        header.putInt(0xf9beb4d9); // Mainnet magic bytes
        header.put("version".getBytes()); // Command
        header.put(new byte[5]); // Command padding
        header.putInt(payload.length); // Payload length

        outputStream.write(header.array());
        outputStream.write(payload);
        outputStream.flush();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

