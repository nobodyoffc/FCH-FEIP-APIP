```
FIPA2: Signature
Version: 3
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

---

ProtocolType: FIPA
SerialNumber: 2
ProtocolName: Signature
VersionNumber: 3
Description : Define the format of the signature data.
Author: C_armX，Free_Cash
Language: en-US
Tags: FIPA, Signature
PreVersionPid:"c1238989a08029c6abf3caf294c5c6589abaf80c64bd1dd2ce3e7bfc44e3a83a"


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
|algorithm|string|Signature algorithm, the default is "ecdsa". Not case sensitive.|N|
|message|string|Raw message|Y|
|address|string|FCH address of the signer|Y|
|signature|string|The signature encoded with Base64|Y|

## example

signer: `FEk41Kqjar45fLDriztUDTUkdki7mmcjWK`

pubKey: `030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a`

priKey: `L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8`


```
{
"algorithm": "ecdsa",
"message":"test", 
"address":"FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
"signature":"HzNqRToLHsY6TrhVB+RjWuk7PqeJhUESINxcL0521mWqU3+rw+NeIGNnOV06ngCLwHD69jCfqHcXWCXuCnIkCGo\u003d"
}
```