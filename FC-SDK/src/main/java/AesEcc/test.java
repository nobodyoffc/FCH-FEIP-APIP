package AesEcc;

//import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;


import org.junit.Test;

public class test {

    //测试数字信封的加密模块
    @Test
    public void testEncrypt() throws Exception {
        Envelop env = new Envelop();
        String msg = "hello.hello world!";
        String encMsg = env.encryptMsg(msg);
        String symKey = env.getSymKey();
        String encKey = env.encryptKey(symKey);

        String decKey = env.decryptKey(encKey);
        env.setSymKey(decKey);
        String decMsg = env.decryptMsg(encMsg);

        System.out.println("##decKey");
        System.out.println(decKey);
        System.out.println("##symKey:");
        System.out.println(symKey);
        System.out.println("##encMsg:");
        System.out.println(encMsg);
        System.out.println("##encKey:");
        System.out.println(encKey);
        System.out.println("##secKey:");
        System.out.println(env.getEciesSecKey());
        System.out.println(decMsg);
    }

    //测试数字信封的解密模块
    @Test
    public void testDecrypt() throws Exception {
        Envelop env = new Envelop();
        String sk = "5965f0ac0cbc8c1de77d788df14586b494485816ede997ca3ac1365b52076eb9";
        env.setEciesSecKey(sk);
        String encMsg = "45f15773c2d848e6529f7982729c01fb0c1409de130146977b393851f13afe630093e4ff0f0943ecd235edf2a2745475";
        String encKey = "53225863dbddfb98ae018c9790e57057cddd8a1b0ecf48bf12238da1c90be013b0397cc9ea8f0a0630a8dc903a9c7d3f93e8fa8a150e720eab4cf9af00ce1d12229ba87a99f8440e22d6ed3aec85d4fa964b6b508fee05ea18f5ab152b8f6543d68b0cedbb4ad5e97ede7c38089b34ad76bb47581aa89b0e0cb261e929edba7d55dd671cad8c56004fe16068ea540aad";

        String decKey = env.decryptKey(encKey);
        System.out.println("##decKey:");
        System.out.println(decKey);
        env.setSymKey(decKey);

        String decMsg = env.decryptMsg(encMsg);
        System.out.println("##decMsg:");
        System.out.println(decMsg);
    }

    //测试AES加解密
    @Test
    public void testAES() throws Exception {
        String kee = "89A6CF35E3216F363B7D1AED3A5393AA";
        byte[] ct = AES256.hexStringToBytes("e3867e2bb2ebf13b8f95b16a81bb571370f998b85fba8b1297af1ddfa613063edfa1bd67332b8fef3d40d61ca41e61f4");
//        byte[] ct = AES256.encrypt(plaintext, kee);
        byte[] pt = AES256.decrypt(AES256.byteToHexString(ct), kee);
        System.out.println(AES256.byteToHexString(ct));
        System.out.println(new String(pt));

    }

    //测试ECIES加解密
    @Test
    public void testECIES() throws Exception {
        EXPList.set_EXP_List();
        ECIES ecies = new ECIES();
        System.out.println("# Generate Key Pair:");
        ecies.generateKeyPair();

//        String sk = "6951bdaa6ab71ec155fe6949e230fe16eebe63e22e0ac6afe2471a371c0dc990";
//        ecies.generateKeyPair();

        System.out.println("# Secret Key Hex Code:");
        byte[] sk = ecies.secretKey2Bytes();
        for (int i = 0; i < sk.length; i++) {
            System.out.printf("%02X", sk[i]);
        }
        System.out.println();

        byte[] pk = ecies.publicKey2Bytes();
        System.out.println("# Public Key Hex Code:");
        for (int i = 0; i < pk.length; i++) {
            System.out.format("%02X", pk[i]);
        }
        System.out.println();

        String str = "Hello world!";
        System.out.println("# String to be Encrypted:");
        System.out.println(str);
        byte[] ct = ecies.encrypt(str);

//        byte[] ct = AES256.hexStringToBytes("4c3182004f6dd9eca3f3ffeed5d148c8e15ee663355aa584523609aeee212c97bbc4d114e6d97ab1a887dfe4b5f59e97263618bde845e7019536bb21b35d7f9635e9c926af8895d0c8e922894323c18a2b35dd12ef28ec39921fdbfc203de2a3e8c0475a0b9abcb779c3cce5244f2227");

        System.out.println("# Cypher Hex Code:");
        for (int i = 0; i < ct.length; i++) {
            System.out.format("%02X", ct[i]);
        }
        System.out.println();
        byte[] pt = ecies.decrypt(ct);
        String string = new String(pt);
        System.out.println("# Decrypted String:");
        System.out.println(string);
    }

}
