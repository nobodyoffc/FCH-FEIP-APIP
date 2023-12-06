# 基于APIP的多签实现（MultiSign Java With APIP）

## 简介
1. 功能：基于APIP数据，实现无协商多签应用。
2. 适用：目前用于Freecash（FCH），理论上也适用于BTC、BCH、XEC、BSV、Doge、LTC等中本聪经典框架，需要在FCH链上存在签名交易记录。
3. 特点：
   * 任意构造多签地址：通过APIP获取地址和公钥信息，可无需协商构建16个公钥以内的多签地址。
   * 公开多签结构：一次支付交易后，其构成公钥、必需签名数量和赎回脚本可通过APIP公开获取。
   * 独立组织支付：交易各方无需多次协商，可分别独立完成交易的构建、签名、组合和广播。
## 示例
1. 多签地址：https://cid.cash/p2shlist.html
2. 构成
```text
春花
FEk41Kqjar45fLDriztUDTUkdki7mmcjWK
公钥：030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a
私钥WIF：L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8
私钥Hex： a048f6c843f92bfe036057f7fc2bf2c27353c624cf7ad97e98ed41432f700575
        
往事
F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW
公钥：02536e4f3a6871831fa91089a5d5a950b96a31c861956f01459c0cd4f4374b2f67
私钥WIF：L5DDxf3PkFwi1jArqYokpTsntthLvhDYg44FXyTSgdTx3XEFR1iB
私钥Hex： 0xee72e6dd4047ef7f4c9886059cbab42eaab08afe7799cbc0539269ee7e2ec30c

小楼
F9pcRpps3T2iHuNGzU3k5b2kWKMRukZP1U
公钥：03f0145ddf5debc7169952b17b5c6a8a566b38742b6aa7b33b667c0a7fa73762e2
私钥WIF：Kybd6FqL2xBEknFV2rcxvYsTZwqAbk99FyN3EBnWdi2M5UxiJL8A
```

## Java 实现概况
1. 项目模块：位于[FCH-FEIP-APIP项目的FC-SDK模块](https://github.com/nobodyoffc/FCH-FEIP-APIP/tree/master/FC-SDK)。该模块成熟后，考虑提取未独立项目。
2. 依赖：FCH-FEIP-APIP项目的pom.xml和FC-SDK模块的pom.xml。
3. 核心类：[FchTool](https://github.com/nobodyoffc/FCH-FEIP-APIP/blob/master/FC-SDK/src/main/java/txTools/FchTool.java)
4. 测试应用：[StartWallet.multiSign(sessionKey,symKey,br)](https://github.com/nobodyoffc/FCH-FEIP-APIP/blob/master/FC-SDK/src/main/java/walletTools/startWallet.java#L110)

## 流程
1. 连接：选择APIP服务商，获得购买方式，支付FCH购买服务，SignIn后获得sessionKey，用于后续APIP访问。
2. 构造：选择任意16个以内，曾经签发过交易的FCH地址，从APIP获取公钥，构造多签地址。如从未签发过交易，则需要提供公钥。
2. 接收：从任意地址向该多签地址发送FCH。
3. 构建：发起支付，按构造顺序提供FCH地址，从APIP获取公钥后，构造未签名交易。
4. 签名：未签名交易分别发送给构成私钥所有者，分别独立签名，生成已签名交易。
5. 组合：任何人得到多签所需签名数量的已签名交易，组合成完整交易。
6. 广播：广播完整交易，完成从多签地址的支付。

## 连接APIP
1. 获得APIP服务的urlHead，默认为 https://cid.cash/APIP 。
2. 获取APIP服务信息：
   调用`apipClass.ApipParamsForClient.checkApipParams`方法检查、获取所需的APIP服务信息对象。
```java
ApipParamsForClient apipParamsForClient = ApipParamsForClient.checkApipParams(br, passwordBytes);
```
   该方法会在当前位置读取/创建 `ApipParams.json`文件，保存APIP服务所需数据。 
   该方法将获得访问APIP所需的sessionKey，并加密保存在`apipParamsForClient`和本地`ApipParams.json`文件中。
   
   * 注意事项：
   1）此过程需要导入APIP服务购买地址（FID）的私钥，也可新建私钥地址。
   2）私钥和sessionKey均使用password的两次Sha256哈希值进行加密后，保存密文。每次使用时解密。
   3）加密算法实现在特定的java环境和版本下会报证书错误，需要调整版本。
   4）安全考虑，每次加密或解密后，所使用的加密密钥均被置零，如需继续使用，可用`.clone()`方式引用。

   * 示例：https://github.com/nobodyoffc/FCH-FEIP-APIP/blob/master/FC-SDK/src/main/java/walletTools/startWallet.java#L45
3. 请求APIP数据
   * 请求方法位于`FC-SDK`模块的 `apipClient`包内。
   * 所有方法为静态方法。
   * 所有方法返回`ApipClient`类，包含了所有请求和响应数据。
   * 数据位于`apipClient.responseBody.data`中。
   * Map和List类型的返回数据可通过`ApipDataGetter`中的相应方法获取，如`List<Address> addrList = getAddressList(apipClient.getResponseBody().getData())`。

## 构造多签地址
1. 输入地址，获取公钥
```java
        ApipClient apipClient = BlockchainAPIs.fidByIdsPost(apipParamsForClient.getUrlHead(), fids, apipParamsForClient.getVia(), sessionKey);
        if(apipClient.isBadResponse("fidByIds"))return;
        Map<String, Address> fidPubKeyMap = ApipDataGetter.getAddressMap(apipClient.getResponseBody().getData());
```
2. 输入公钥列表，产生多签地址
```java
        P2SH p2SH = FchTool.genMultiP2sh(pubKeyList, 2);
        String mFid = p2SH.getFid();
```
   * 示例：https://github.com/nobodyoffc/FCH-FEIP-APIP/blob/master/FC-SDK/src/main/java/walletTools/startWallet.java#L330
## 接收FCH（略）

## 获取多签地址信息
```java
        ApipClient apipClient = BlockchainAPIs.p2shByIdsPost(apipParamsForClient.getUrlHead(), new String[]{fid}, apipParamsForClient.getVia(), sessionKey);
        if(apipClient.isBadResponse("get multiSign FID info"))return;;
        Map<String, P2SH> p2shMap = ApipDataGetter.getP2SHMap(apipClient.getResponseBody().getData());
        P2SH p2sh = p2shMap.get(fid);
        JsonTools.gsonPrint(p2sh);
```
示例：
```json
{
  "fid": "3MRS39FX8bpV9CCjJHavw586q6a9Rogpw2",
  "redeemScript": "5221030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a2102536e4f3a6871831fa91089a5d5a950b96a31c861956f01459c0cd4f4374b2f672103f0145ddf5debc7169952b17b5c6a8a566b38742b6aa7b33b667c0a7fa73762e253ae",
  "m": 2,
  "n": 3,
  "pubKeys": [
    "030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a",
    "02536e4f3a6871831fa91089a5d5a950b96a31c861956f01459c0cd4f4374b2f67",
    "03f0145ddf5debc7169952b17b5c6a8a566b38742b6aa7b33b667c0a7fa73762e2"
  ],
  "fids": [
    "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
    "F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW",
    "F9pcRpps3T2iHuNGzU3k5b2kWKMRukZP1U"
  ],
  "birthHeight": 1738416,
  "birthTime": 1683554794,
  "birthTxId": "58fa6a754a73f20a2fdb08ee7f98f49b208cf8a2cf019d1cebf969b886dc5992"
}
```

## 发起原始交易
1. 如上获取多签地址信息：P2SH p2sh。
2. 构造输出列表： List<SendTo> sendToList
```SendTo
public class SendTo {
    private String fid;
    private Double amount;
    }
```
3. 如果需要给出OP_Return内容 `msg`
4. 根据sendTo和msg计算不包含输入的手续费：
```java
    long fee = FchTool.calcFeeMultiSign(0,sendToList.size(),msgLen, p2sh.getM(),p2sh.getN());
```
   * 注意：这里尚未获得输入，所以输入数量为0。
5. 向APIP请求输入
```java
        double feeDouble = fee/FchToSatoshi;//FchToSatoshi=100000000
        int msgLen = msg.getBytes().length;
        apipClient = WalletAPIs.cashValidForPayPost(urlHead, mFid, sum+feeDouble, via, sessionKey);
        // urlHead和via（渠道商）来自Apip参数，sum为总输出金额，mFid为多签地址，sessionKey从访问APIP所需）
        if(apipClient.isBadResponse("get cash list"))return;
        List<Cash> cashList = ApipDataGetter.getCashList(apipClient.getResponseBody().getData());
```
   * 注意：此时返回的cashList的总金额能够覆盖全部支出和费用，输入的手续费已经服务端计算，并包含在内。
6. 构造原始交易
```java
        byte[] rawTx = FchTool.createMultiSignRawTx(cashList, sendToList, msg, p2sh);
        MultiSigData multiSignData = new MultiSigData(rawTx,p2sh,cashList);
        String multiSignDataJson = multiSignData.toJson(); //包含了签名所需所有数据
```
7. 示例：https://github.com/nobodyoffc/FCH-FEIP-APIP/blob/master/FC-SDK/src/main/java/walletTools/startWallet.java#L273

## 独立签名

   将 `multiSignDataJson`分别发给每个私钥持有者。
   私钥持有者收到后:
1. 验证信息：将需要签名的输入和输出展示给签名者验证
   示例：https://github.com/nobodyoffc/FCH-FEIP-APIP/blob/master/FC-SDK/src/main/java/walletTools/startWallet.java#L214
2. 签名：
```java
String partSignedDataJson = FchTool.signSchnorrMultiSignTx(multiSignDataJson, priKey))
```
   示例：https://github.com/nobodyoffc/FCH-FEIP-APIP/blob/master/FC-SDK/src/main/java/walletTools/startWallet.java#L172

### 组合交易
   获得超过m个人独立签名后，即可组合交易。
   * 注意
      组合时一定要按照组建多签时的公钥顺序提供签名。可缺不可乱。顺序不同，多签地址不同！
```java
    String[] partSignedDataJsons = new String[]{partSignedDataJson1,partSignedDataJsons2,...}
    String signedTx = FchTool.buildSignedTx(partSignedDataJsons);
```

### 广播交易
   可以用任何方式广播`signedTx`。