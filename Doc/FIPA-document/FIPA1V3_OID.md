OID

did有多个意义. 比如txid是tx的哈希，也是pid，aid
声明：(pid)536e4f3a6871831fa91089a5d5a950b96a31c861956f01459c0cd4f4374b2f67

Nid
eccAes256K1P7@C_armX=7a57e449940715c0d9531185c5f4a1555fc8a65b170ad561fa91cea44c36a87e;

eccAes256K1P7@C_armX=(pid)7a57e449940715c0d9531185c5f4a1555fc8a65b170ad561fa91cea44c36a87e;






```
FIPA1: NID
Version: 1
Language: en-US
Author: C_armX
Status: draft
Create: 2021-12-11
Update：2022-05-07
PID: ""
TXid: 
```

# FIPA2V3_Signature(en-US)

## Contents

[About FIPA](#about-fipa)

[About this protocol](#about-this-protocol)

[Data format](#data-format)

[Example](#Example)

## About FIPA

FIPA protocols make improvements to the FCH ecosystem. These improvements will not lead to a hard fork of the main network, and will not write information on the blockchain.

## About this protocol

1. This protocol defines the data format of Freecash ecological message signature.

2. The default signature algorithm is ecdsa signature algorithm with Base64 encoding.

3. Data is in JSON.

## Data format

The signatures are transmitted in the following format:

|name|type|description|required|
|:---|:---|:---|:---|