二维码协议


{
    "meta":"FC",
    "op":<sign,veri,encr,decr,show,go>,
    "fid":<>,
    "oid":<>,
    "data":<>,
    "url":<增加fc://fvl>
}


data:
{
alg
to
from
sign
msg
pubKey
cipher
iv
sum
}

签名：fid，msg，alg----alg，pubKey，fid，sign
验证：

{
"meta":"FV",
"op":"sign",
"data":{
"msg":{
"url":<string. 用户提交登录信息访问的url>,
"nonce":<int. 置0，防止重放攻击和识别登录用户>,
"time":<long. 置0，由用户端填入长度13的时间戳>,
"pubKey":<string. 置空串，由用户端填入hex公钥>
},
"sign":<string. 置空串，由用户端填入签名>
}
}

{
"meta": "FV",
"op": "sign",
"data": {
"msg": {
"url": "https://cid.cash/explorer/signin",
"nonce": 0,
"time": 0,
"pubKey": ""
},
"sign": ""
}
}


1. 检查op，成功则获取sign,veri,encr,decr之一：
   1. sign: 获取fid，获取data，查看是否fid是本地身份，是的话询问是否对data签名，否的话丢弃。签名后返回完整签名。
   2. veri：获取data，解析json，分别获取fid（address），sign（signature），msg（message），alg验证签名。返回是否成功。
   3. encr：获取fid，询问是否为该fid加密data内容，是的话获取该fid公钥进行加密，返回密文。
   4. decr: 获取fid，检查是否本地身份，是则询问是否解密，否则丢弃。
2. 检查fid，成功则获取对应cidInfoByIds
2. 检查oid，成功则解析oid
   1. 是否包含@，是则取@后的值，调用cidFidSeek获取fid，然后调用nidSearch在该fid的命名空间中查询@前面的值。返回oid。然后继续以下步骤。
   2. 是否以(idType)开始，是则获取idType，调用对应ByIds接口获取详情。
   3. 其他情况无效，忽略oid。
3. 检查data，成功则显示内容。
4. 检查url，成功则询问是否访问url

解析：oid
1. 64字符hex，did
2. 以()开始，获取()内的类型，获取64字符hex，在类型中查询。
3. 包含@
   4. 查找nid
   5. nid为空，

{
    "meta":"FC",
    "fid":<>,
    "oid":<>,
    "msg":<>,
    "op":<sign,veri,encr,decr>
}



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