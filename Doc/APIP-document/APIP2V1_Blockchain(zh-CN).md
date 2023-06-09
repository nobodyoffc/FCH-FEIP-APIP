# APIP2V1_Blockchain(zh-CN)
---
## 目录

[关于APIP](#关于APIP)

[主要共识](#主要共识)

[cashByIds](#cashByIds)

[cashSearch](#cashSearch)

[cashValid](#cashValid)

[txByIds](#txByIds)

[txSearch](#txSearch)

[blockByIds](#blockByIds)

[blockSearch](#blockSearch)

[opReturnByIds](#opReturnByIds)

[opReturnSearch](#opReturnSearch)

[addressByIds](#addressByIds)

[addressSearch](#addressSearch)

---

```
Type: APIP
SerialNumber: 2
ProtocolName: Blockchain
Version: 1
Description : Freecash主链信息相关API.
Author: C_armX, Write_cash, Free_cash, F7rspPvuPhrc6xYYXjBoCieKAWSz9ShSNp
Language: zh-CN
CreateDate: 2021-10-30
UpdateDate: 2023-05-03
```

## 关于APIP

`APIP`(Application Programming Interface Protocols)是自由共识生态协议的一种类型，用于创建和发布开放的API文档，供API服务方开发部署通用API服务，以实现数据服务的去中心化。
API需求方可以按照APIP协议,从遵循该协议的任何一个API服务方那里获取数据服务。

`《APIP1_OpenAPI》`规范了APIP类型协议的`协议发布`、`接口标识`、`接口URL构成`、`时间戳格式`、`密码算法`、`服务流程`、`商业模式`、`connect接口`，`数据接口`和`查询语法`的通用规则。
设计、开发或应用APIP协议前，应先参考《APIP1_OpenAPI》协议。

## 示例数据

  - `requester`: FEk41Kqjar45fLDriztUDTUkdki7mmcjWK
  - requester的公钥`pubKey`: 030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a
  - requester的私钥`priKey`: L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8
  - requester获得的对称密钥`symKey`（即`sessionKey`): d2c03bbc1ba1380eafc395374e8da61f92545a1aac5d30b0c19289a69bd34a09
  - 服务商链上发布的`urlHead`: https://localhost:8080/api/

## 主要共识

1. 本协议各接口提供Freecash主链上的主要信息。

2. 本协议接口的数据来源涉及以下协议: 

    - cd,cdd: 《FIPA4_Coindays》
    - guide: 《FIPA6_Guide》

3. 本协议接口数据采用JSON格式。

4. 本协议所有接口采用POST方法。

5. 本协议的接口的urlTail为: `apip2/v1/<接口名称>`

6. 各接口具体共识如下：

---

## cashByIds
### 说明

获取指定cash ID列表的cash详情列表。
  
### 默认排序

  - 无排序

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "ids":<string array. CashID数组. 必填>
    }
}
```
### data in response body
  
```
{
    <cashID>:{
        "cashId": <string. CashID>,
        "outIndex": <int. Cash出生于交易内的索引号>,
        "type": <string. Cash类型，P2PKH、P2SH、OpReturn等>,
        "fid": <string. 所属FCH地址>,
        "value": <long. 金额，单位聪>,
        "lockScript": <string. 锁定脚本>,
        "txId": <string. Cash出生所在交易ID>,
        "txIndex": <int. 出生交易在所在区块内的索引号>,
        "blockId": <string. 出生交易所在区块ID>,
        "birthTime": <long. 出生区块时间戳，单位秒>,
        "birthHeight": <long. 出生区块高度>,
        "spendTime": <long. 花费于区块时间戳>,
        "spendTxId": <string. 花费于交易ID>,
        "spendHeight": <long. 花费于区块高度>,
        "spendIndex": <int. 在花费交易中的输入索引号>,
        "unlockScript": <string. 解锁脚本>,
        "sigHash": <string. 花费签名的sigHash>,
        "sequence": <string. 花费的sequence>,
        "cdd": <long. 花费时的币天销毁量，单位cd>,
        "cd": <long. 未花费积累的币天量，单位cd>,
        "valid": <boolean，是否可用（是否尚未被花费）>
    },
    ...
}

```
### 示例

  - request body
  
```json
{
   "url": "http://localhost:8080/APIP/apip2/v1/cashByIds",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "ids":["c383eb3908415590e0bef3dee141e5f4b7dde335a8997db85e478464f3cfddce","f509814bbe7a642e4f8f0340650bc1cbc91c0c130c96cec2368099642b25530a"]
   }
}
```

  - response body
  
```json
{
   "code": 0,
   "message": "Success.",
   "nonce": 1987697,
   "balance": 35535528799,
   "got": 2,
   "total": 2,
   "bestHeight": 1768165,
   "data": {
      "f509814bbe7a642e4f8f0340650bc1cbc91c0c130c96cec2368099642b25530a": {
         "cashId": "f509814bbe7a642e4f8f0340650bc1cbc91c0c130c96cec2368099642b25530a",
         "birthIndex": 1,
         "type": "P2PKH",
         "fid": "FNDHT9qAjEpfsTK7Aj6LWsAEwZvCbYTJ2o",
         "value": 99208578600,
         "lockScript": "76a914b3b3807fb5b67ef43261afb3d452681f91ed384688ac",
         "birthTxId": "34b2ed1499fc086174e4b808fb6d0f2c0c3b876f79807752cb3eca7206ac5d94",
         "birthTxIndex": 1,
         "birthBlockId": "00000000000000de6c9c7bed09304b7ba02c6f35c0adbb300811031c147bed1d",
         "birthTime": 1682746464,
         "birthHeight": 1725312,
         "spendTime": 1682753900,
         "spendTxId": "f812ae208ac31f07e2c15af4c5b172042254194aaa5a2991e33c4ebe468ba46f",
         "spendHeight": 1725456,
         "spendIndex": 0,
         "unlockScript": "41aeb7adf881d7ec9c531d4d0a329e9cd3e4641147878d0f07064d1c0b8d1f8502c69fba8b90cec6dd24485755fb934191668bbd74ec5e8d49cd424f1605a46e36412102c00359832547be38ffa198545992d29aa492a3372b2cbc856ae21caa5dcdee71",
         "sigHash": "ALL",
         "sequence": "ffffffff",
         "cdd": 0,
         "cd": 0,
         "valid": false
      },
      "c383eb3908415590e0bef3dee141e5f4b7dde335a8997db85e478464f3cfddce": {
         "cashId": "c383eb3908415590e0bef3dee141e5f4b7dde335a8997db85e478464f3cfddce",
         "birthIndex": 1,
         "type": "P2PKH",
         "fid": "F7sm4BC94qPeMsXGi93QbFmrhfsQUtVLEd",
         "value": 1449658,
         "lockScript": "76a914167076daccaedfd23cd19242cd4160b6922ff82788ac",
         "birthTxId": "c82152d2a5a6f08010321faa3a407b74dd202d410825263a8f12292224becd2c",
         "birthTxIndex": 1,
         "birthBlockId": "0000000000000002437260ef1e1e299193ea5868c178a7d5d91c30c7dc409f14",
         "birthTime": 1670508493,
         "birthHeight": 1525430,
         "spendTime": 1670519475,
         "spendTxId": "27dfb4f3334c2f985834d354bbbdf0c4898ffd12edc9ddaf243b83101a9e7e87",
         "spendHeight": 1525614,
         "spendIndex": 41,
         "unlockScript": "419ad86aad9d397af55c305f6678f0e591efe78b9f4cd51e4220cc73874a56eebf0bee3ac9881663b87156dab7e6f67f63ea57c8bc57b3f7af3f2a47e9c747695541210287ba63d7970651fc1f437065f90b42d6502279adbd1f0c467751682863f9c533",
         "sigHash": "ALL",
         "sequence": "feffffff",
         "cdd": 0,
         "cd": 0,
         "valid": false
      }
   }
}
```

## cashSearch
### 说明

对Cash信息响应数据data内的各项进行fcdsl查询。
  
### 默认排序

  - valid: desc
  - birthHeight: desc
  - id: asc

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        <查询语句...>
    }
}
```
### data in response body
  
```
{
  [
    {
        "cashId": <string. CashID>,
        "outIndex": <int. Cash出生于交易内的索引号>,
        "type": <string. Cash类型，P2PKH、P2SH、OpReturn等>,
        "fid": <string. 所属FCH地址>,
        "value": <long. 金额，单位聪>,
        "lockScript": <string. 锁定脚本>,
        "txId": <string. Cash出生所在交易ID>,
        "txIndex": <int. 出生交易在所在区块内的索引号>,
        "blockId": <string. 出生交易所在区块ID>,
        "birthTime": <long. 出生区块时间戳，单位秒>,
        "birthHeight": <long. 出生区块高度>,
        "spendTime": <long. 花费于区块时间戳>,
        "spendTxId": <string. 花费于交易ID>,
        "spendHeight": <long. 花费于区块高度>,
        "spendIndex": <int. 在花费交易中的输入索引号>,
        "unlockScript": <string. 解锁脚本>,
        "sigHash": <string. 花费签名的sigHash>,
        "sequence": <string. 花费的sequence>,
        "cdd": <long. 花费时的币天销毁量，单位cd>,
        "cd": <long. 未花费积累的币天量，单位cd>,
        "valid": <boolean，是否可用（是否尚未被花费）>
    }
  ]
}
```
### 示例

  - request body
  
```json
{
   "url": "http://localhost:8080/APIP/apip2/v1/cashSearch",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "query":{
         "range": {
            "fields": ["value"],
            "gt": "500"
         }
      },
      "size":1
   }
}
```

  - response body
  
```json
{
   "code": 0,
   "message": "Success.",
   "nonce": 1987697,
   "balance": 35533928799,
   "got": 1,
   "total": 4458461,
   "bestHeight": 1750913,
   "data": [
      {
         "cashId": "b51b4c49fbc2de760e38c158e28176d81eb0938960a447f6b2c9abdf0c58e987",
         "birthIndex": 0,
         "type": "P2PKH",
         "fid": "FG39xQHTMsEDS1k4JfkszPHzAQpMVhfZkz",
         "value": 1280000000,
         "lockScript": "76a9146ff8549d9bb638de5620a0fdba61aa9019c98d4f88ac",
         "birthTxId": "d73da74b34482dfe29e0a02dcd78f2210140386458e8f99031f87932eca428ae",
         "birthTxIndex": 0,
         "birthBlockId": "00000000000019851f31f138e189d2d35ce6e9d2a425db7bac8e570bc71e843d",
         "birthTime": 1684393505,
         "birthHeight": 1750864,
         "spendTime": 0,
         "spendHeight": 0,
         "spendIndex": 0,
         "cdd": 0,
         "cd": 0,
         "valid": true
      }
   ],
   "last": [
      "1",
      "1750864",
      "b51b4c49fbc2de760e38c158e28176d81eb0938960a447f6b2c9abdf0c58e987"
   ]
}
```

## cashValid
### 说明

获取指定地址的可用cash列表。
  
### 默认排序

  - cd: desc
  - value: asc
  - id: asc

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "query":{
            "terms":{
                "fields":["fid"],
                "values":["<所查询的fch地址>"]
            }
        },
        <其他查询条件...>
    }
}
```
### data in response body

与cashSearch相同。
  
### 示例

  - request body
  
```json
{
   "url": "http://localhost:8080/APIP/apip2/v1/cashValid",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "query":{
         "terms": {
            "fields": ["fid"],
            "values":["FTqiqAyXHnK7uDTXzMap3acvqADK4ZGzts"]
         }
      },
      "size":1
   }
}
```

  - response body
  
```json
{
   "code": 0,
   "message": "Success.",
   "nonce": 1987697,
   "balance": 35534028799,
   "got": 1,
   "total": 144239,
   "bestHeight": 1749101,
   "data": [
      {
         "cashId": "76ab7bdb5bd7e41765ceb9dd534d9fecc0ca7efcd0ce42a44ddd9873390891c5",
         "birthIndex": 1,
         "type": "P2PKH",
         "fid": "FTqiqAyXHnK7uDTXzMap3acvqADK4ZGzts",
         "value": 2500000000,
         "lockScript": "76a914f1704a9663c1e530f82ca1bc7ff52f0f65abc1ca88ac",
         "birthTxId": "2fa578c425d4c6872d1ebd6a4b28c6d42a2d3c7826158956b8cd326de789f65a",
         "birthTxIndex": 0,
         "birthBlockId": "00000000cbe04361b1d6de82b893a7d8419e76e99dd2073ac0db2ba0e652eea8",
         "birthTime": 1577836802,
         "birthHeight": 0,
         "spendTime": 0,
         "spendHeight": 0,
         "spendIndex": 0,
         "cdd": 0,
         "cd": 31150,
         "valid": true
      }
   ],
   "last": [
      "31150",
      "2500000000",
      "76ab7bdb5bd7e41765ceb9dd534d9fecc0ca7efcd0ce42a44ddd9873390891c5"
   ]
}
```

## txByIds
### 说明

获取指定地址列表的详情列表。
  
### 默认排序

  - 无排序

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "ids":<string array. txid数组. 必填>
    }
}
```
### data in response body
  
```
{
  <txid>:{
        "txId": <string. 交易id>,
        "version": <string. 版本号>,
        "lockTime": <long. 锁定时间>,
        "blockTime": <long. 区块时间戳>>,
        "blockId": <string. 区块ID>,
        "txIndex": <int. 交易在区块中的索引号>,
        "inCount": <int. 输入cash数量>,
        "coinbase":<string. coinbase内容>,
        "opReBrief":<string. opReturn内容摘要>,
        "outCount": <int. 输出cash数量>,
        "height": <long. 区块高度>,
        "inValueT": <long. 输入总金额>,
        "outValueT": <long. 输出总金额>,
        "fee": <long. 交易费>,
        "cdd": <long. 币天销毁总额>,
        "spentCashes": [
                {
                    "cashId": <string. 花费cash ID>,
                    "fid": <string. 花费cash 所属fch地址>,
                    "value": <long. 花费cash 金额>,
                    "cdd": <long. 花费cash销毁币天数>
                }
            ],
        "issuedCashes": [
            {
                "cashId": <string. 发行cash ID>,
                "fid": <string. 发行cash 所属fch地址>,
                "value": <long. 发行cash 金额>,
            }
        ]
    },
    ...
]
```
### 示例

  - request body
  
```json
{
   "url": "http://localhost:8080/APIP/apip2/v1/txByIds",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "ids":["db42a2b9941e004474c74046b4a576484f28cf83bbf8c0bdf21e966202f7e2db","1deb2bb37288d57c4fcccfba35379c9a1c930bb132ce5f19294442564c934f5c"]
   }
}
```

  - response body
  
```json
{
   "code": 0,
   "message": "Success.",
   "nonce": 1987697,
   "balance": 35533828799,
   "got": 2,
   "total": 2,
   "bestHeight": 1752183,
   "data": [
      {
         "id": "db42a2b9941e004474c74046b4a576484f28cf83bbf8c0bdf21e966202f7e2db",
         "version": 1,
         "lockTime": 0,
         "blockTime": 1682764825,
         "blockId": "00000000000000cc3032f6a3c6de8e48cfbeb0c97c6b98e04e8dbe85fbe91a49",
         "txIndex": 0,
         "coinbase":"�T��Ld\b��mm��C��&���ք��j/*?�r���9Ųf�<        ڸ��  q/Mining-Dutch/-25",
         "outCount": 2,
         "inCount": 0,
         "height": 1725616,
         "inValueT": 0,
         "outValueT": 2225000000,
         "fee": 0,
         "cdd": 0,
         "spentCashes": [],
         "issuedCashes": [
            {
               "cashId": "dafe470b876a518132172fdb942160a86ce9f794f0b2ecda7ff719cd7a7ade2c",
               "fid": "FTvWfHjm538DYhCQr91N2KU4zGhqegP2ZK",
               "value": 1600000000,
               "cdd": 0
            },
            {
               "cashId": "779c984cc36ed3c81e4a670ec41fedcefd9289d203a3128d53ae8ea042dfa5b3",
               "fid": "FTqiqAyXHnK7uDTXzMap3acvqADK4ZGzts",
               "value": 625000000,
               "cdd": 0
            }
         ]
      },
      {
         "id": "1deb2bb37288d57c4fcccfba35379c9a1c930bb132ce5f19294442564c934f5c",
         "version": 2,
         "lockTime": 0,
         "blockTime": 1585379186,
         "blockId": "000000000000000093bc12f725daf5045e4e1bcacf41ad7b4d5f3dbc6602e6df",
         "txIndex": 1,
         "outCount": 2,
         "inCount": 3,
         "height": 126028,
         "opReBrief": "�FEIP|7|1||AvJxKExPi5nitH+jgZ",
         "inValueT": 1200000,
         "outValueT": 1098590,
         "fee": 101410,
         "cdd": 0,
         "spentCashes": [
            {
               "cashId": "e8c59a11844048ecab81a2cc5f6a2d671fdedbe90bedc95468bfe3a4b2989e2b",
               "fid": "F8FyPZKjQWQfdSLbxdJ14hx7SPsVW5nMeT",
               "value": 100000,
               "cdd": 0
            },
            {
               "cashId": "59313f09dde0faef41c8848c17a26c6a4e36db5788324ebd0ba99853c821d505",
               "fid": "F8FyPZKjQWQfdSLbxdJ14hx7SPsVW5nMeT",
               "value": 100000,
               "cdd": 0
            },
            {
               "cashId": "aa5635c0f74c4ebf8e6aa4beb8d408e83ff5390b7a599d6ae3397b698c0e7f05",
               "fid": "F8FyPZKjQWQfdSLbxdJ14hx7SPsVW5nMeT",
               "value": 1000000,
               "cdd": 0
            }
         ],
         "issuedCashes": [
            {
               "cashId": "3fd3fa087fb0bee3ae783e65fddd84352b4fed8e0f51691de9d8fc6b7e3b81e3",
               "fid": "F8FyPZKjQWQfdSLbxdJ14hx7SPsVW5nMeT",
               "value": 1098590,
               "cdd": 0
            },
            {
               "cashId": "14646fad539811de8726dbc7e80482095b4548cbdd4031413672049a4a697517",
               "fid": "OpReturn",
               "value": 0,
               "cdd": 0
            }
         ]
      }
   ]
}
```

## txSearch
### 说明

对交易信息响应数据data内的各项进行fcdsl查询。
  
### 默认排序

  - height: desc
  - txIndex: desc
  - id: asc

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        <查询语句...>
    }
}
```
### data in response body
  
```
[
	{
        "txId": <string. 交易id>,
        "version": <string. 版本号>,
        "lockTime": <long. 锁定时间>,
        "blockTime": <long. 区块时间戳>>,
        "blockId": <string. 区块ID>,
        "txIndex": <int. 交易在区块中的索引号>,
        "inCount": <int. 输入cash数量>,
        "coinbase":<string. coinbase内容>,
        "opReBrief":<string. opReturn内容摘要>,
        "outCount": <int. 输出cash数量>,
        "height": <long. 区块高度>,
        "inValueT": <long. 输入总金额>,
        "outValueT": <long. 输出总金额>,
        "fee": <long. 交易费>,
        "cdd": <long. 币天销毁总额>
    }
]
```
### 示例

  - request body
  
```json
{
   "url": "http://localhost:8080/APIP/apip2/v1/txSearch",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "query":{
         "range": {
            "fields": ["fee"],
            "gt": "500"
         }
      },
      "size":1
   }
}
```

  - response body
  
```json
{
   "code": 0,
   "message": "Success.",
   "nonce": 1987697,
   "balance": 35533728799,
   "got": 1,
   "total": 132583,
   "bestHeight": 1753324,
   "data": [
      {
         "txId": "cf5a8333c5c80cc8deac0835e0fe895f045f9bdd8ac03f9f17c293a61ad14cb4",
         "version": 2,
         "lockTime": 1752914,
         "blockTime": 1684533249,
         "blockId": "0000000000000ea9f7fa9b81ef743e0b0aae82eb4822f8e58982a9e1cf872a5e",
         "txIndex": 1,
         "outCount": 2,
         "inCount": 161,
         "height": 1752946,
         "inValueT": 100625000000,
         "outValueT": 100624977221,
         "fee": 22779,
         "cdd": 103103
      }
   ],
   "last": [
      "1752946",
      "1",
      "cf5a8333c5c80cc8deac0835e0fe895f045f9bdd8ac03f9f17c293a61ad14cb4"
   ]
}
```

## p2shByIds
	
### 说明

获取指定p2sh地址列表的详情列表。
  
### 默认排序

  - 无排序

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "ids":<string array. fid数组. 必填>
    }
}
```
### data in response body
```
{
  <fid>:{
        "fid": <string. 多签地址>,
        "redeemScript": <string. 赎回脚本>,
        "m": <int. 多签所需要的签名>,
        "n": <int. 多签总人数>,
        "pubKeys": <string array. 所有组成者公钥>,
		"fids": <string array. 所有组成者地址>,
		"birthTxId": <long. 出生交易ID>,
        "birthTime": <long. 出生区块时间戳，单位秒>,
        "birthHeight": <long. 出生区块高度>
    },
    ...
]
```
### 示例

  - request body
  
```json
{
   "url": "http://localhost:8080/APIP/apip2/v1/p2shByIds",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "ids":["3MRS39FX8bpV9CCjJHavw586q6a9Rogpw2"]
   }
}
```

  - response body
  
```json
{
   "code": 0,
   "message": "Success.",
   "nonce": 1987697,
   "balance": 35532928799,
   "got": 1,
   "total": 0,
   "bestHeight": 1746965,
   "data": {
      "3MRS39FX8bpV9CCjJHavw586q6a9Rogpw2": {
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
   }
}
```
## p2shSearch
	
### 说明

对p2sh响应数据data内的各项进行fcdsl查询。
  
### 默认排序

  - birthHeight: desc
  - id: asc

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        <查询语句...>
    }
}
```
### data in response body
```
[
   {
        "fid": <string. 多签地址>,
        "redeemScript": <string. 赎回脚本>,
        "m": <int. 多签所需要的签名>,
        "n": <int. 多签总人数>,
        "pubKeys": <string array. 所有组成者公钥>,
		"fids": <string array. 所有组成者地址>,
		"birthTxId": <long. 出生交易ID>,
        "birthTime": <long. 出生区块时间戳，单位秒>,
        "birthHeight": <long. 出生区块高度>
    },
    ...
]
```
### 示例

  - request body
  
```json
{
   "url": "http://localhost:8080/APIP/apip2/v1/p2shSearch",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "query":{
         "terms":{
            "fields":["fids"],
            "values":["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"]
         }
      },
      "size":1
   }
}
```

  - response body
  
```json
{
   "code": 0,
   "message": "Success.",
   "nonce": 1987697,
   "balance": 35532828799,
   "got": 1,
   "total": 1,
   "bestHeight": 1751056,
   "data": [
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
   ],
   "last": [
      "1738416",
      "3MRS39FX8bpV9CCjJHavw586q6a9Rogpw2"
   ]
}
```
## blockByIds
### 说明

获取指定区块列表的详情列表。
  
### 默认排序

  - 无排序

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "ids":<string array. 区块ID数组. 必填>
    }
}
```
### data in response body
  
```
{
  <区块id>:{
    "blockId": <string. 区块id>,
    "size": <string. 区块字节大小>,
    "height": <string. 区块高度>,
    "version": <string. 区块版本>,
    "preId": <string. 前区块ID>,
    "merkleRoot": <string. merkle tree root>,
    "time": <long. 时间戳精确到秒>,
    "diffTarget": <long. 挖矿难度目标值>,
    "nonce": <long. 随机数>,
    "txCount": <int. 交易数量>,
    "inValueT": <long. 输入总金额，单位聪>,
    "outValueT": <long. 输出总金额，单位聪>,
    "fee": <long. 交易费，单位聪>,
    "cdd": <long. 币天销毁总额，单位cd>,
    "txList": [
        {
          "txId": <string. 交易ID>,
          "outValue": <long. 交易总金额>,
          "fee": <long. 交易费>,
          "cdd": <long. 交易销毁必填总额>
        }
    ]
  }
}
```
### 示例

  - request body

```json
{
   "url": "http://localhost:8080/APIP/apip2/v1/blockByIds",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "ids":["000000000000010a46a649834e55cf613bd1e1e913e0f03ba5d447e770ffdb1b"]
   }
}
```

  - response body
  
```json
{
   "code": 0,
   "message": "Success.",
   "nonce": 1987697,
   "balance": 35532728799,
   "got": 1,
   "total": 1,
   "bestHeight": 1758199,
   "data": {
      "000000000000010a46a649834e55cf613bd1e1e913e0f03ba5d447e770ffdb1b": {
         "size": 12063,
         "height": 1728528,
         "version": "3fffe000",
         "preId": "000000000000007dbf36d5220d8e14090d38d04211c7fb9b8f911a19164ecfa4",
         "merkleRoot": "4c0cb8ee89a0f00f2715ebffab23a7df185f17c7304122d7f03de2a540aa5109",
         "time": 1682944195,
         "diffTarget": 436278616,
         "nonce": 2049028201,
         "txCount": 2,
         "inValueT": 131205069835,
         "outValueT": 132797569835,
         "fee": 11781,
         "cdd": 13120,
         "id": "000000000000010a46a649834e55cf613bd1e1e913e0f03ba5d447e770ffdb1b",
         "txList": [
            {
               "txId": "19f089d3da5700f6190be93cea54ce98f24adf24b2cd92e05a3d378521aa3a99",
               "outValue": 1592511781,
               "fee": 0,
               "cdd": 0
            },
            {
               "txId": "1b0fc0d386cfc7c8c5ff78889bcccc09ada266dfb85f84afcf4e06a7c2f657af",
               "outValue": 131205058054,
               "fee": 11781,
               "cdd": 13120
            }
         ]
      }
   }
}
```

## blockSearch
### 说明

对区块信息响应数据data内的各项进行fcdsl查询。
  
### 默认排序

  - height:desc
  - id:asc

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        <查询语句...>
    }
}
```
### data in response body
  
```
[{
    "blockId": <string. 区块id>,
    "size": <string. 区块字节大小>,
    "height": <string. 区块高度>,
    "version": <string. 区块版本>,
    "preBlockId": <string. 前区块ID>,
    "merkleRoot": <string. merkle tree root>,
    "time": <long. 时间戳精确到秒>,
    "diffTarget": <long. 挖矿难度目标值>,
    "nonce": <long. 随机数>,
    "txCount": <int. 交易数量>,
    "inValueT": <long. 输入总金额，单位聪>,
    "outValueT": <long. 输出总金额，单位聪>,
    "fee": <long. 交易费，单位聪>,
    "cdd": <long. 币天销毁总额，单位cd>
}]
```
### 示例

  - request body
  
```json
{
	"url": "http://localhost:8080/APIP/apip2/v1/blockSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "range": {
                "fields": ["size"],
                "gt": "500"
            }
        },
        "size":1
    }
}
```

  - response body
  
```json
{
   "code": 0,
   "message": "Success.",
   "nonce": 1987697,
   "balance": 35532628799,
   "got": 1,
   "total": 93442,
   "bestHeight": 1759355,
   "data": [
      {
         "size": 608,
         "height": 1759095,
         "version": "2000e000",
         "preBlockId": "0000000000000c0d7460f384e52acc6fa0fac6d53a53eb29bf7fe8021b36b8cc",
         "merkleRoot": "6ae66e2c5e273be273b2f80751e5fb85b6e1515fc070332d5173328cca1df7c8",
         "time": 1684911645,
         "diffTarget": 437099486,
         "nonce": 2329326727,
         "txCount": 2,
         "blockId": "0000000000000418f297b9c4b6dbef591ee7e059796f8710a6e80f8abfbfde86",
         "inValueT": 9202834,
         "outValueT": 1601702834,
         "fee": 680,
         "cdd": 0
      }
   ],
   "last": [
      "1759095",
      "0000000000000418f297b9c4b6dbef591ee7e059796f8710a6e80f8abfbfde86"
   ]
}
```

## opReturnByIds
### 说明

获取若干指定id的OpReturn详情。
  
### 默认排序

  - 无排序

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "ids":<string array. OpReturnID（txid）数组. 必填>
    }
}
```
### data in response body
  
```
{
    <OpReturn ID>:{
        "txId": <string. OpReturnID，即所在交易id>,
        "height": <long. 所在区块高度>,
        "txIndex": <int. 所在交易在区块中的索引号>,
        "opReturn": <string. OpReturn内容>,
        "signer": <string. 签名人，即第一笔输入的fch地址>,
        "recipient": <string. 接受人，即第一个非签名人的收款地址>,
        "time": <long. 所在区块时间戳，单位秒>,
        "cdd": <long. 所在交易币天销毁总额，单位cd>
    },
    ...
}
```
### 示例

  - request body
  
```json
{
	"url": "http://localhost:8080/APIP/apip2/v1/opReturnByIds",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "ids":["1deb2bb37288d57c4fcccfba35379c9a1c930bb132ce5f19294442564c934f5c"]
    }
}
```

  - response body
  
```json
{
   "code": 0,
   "message": "Success.",
   "nonce": 1987697,
   "balance": 35532528799,
   "got": 1,
   "total": 1,
   "bestHeight": 1761549,
   "data": {
      "1deb2bb37288d57c4fcccfba35379c9a1c930bb132ce5f19294442564c934f5c": {
         "txId": "1deb2bb37288d57c4fcccfba35379c9a1c930bb132ce5f19294442564c934f5c",
         "height": 126028,
         "txIndex": 1,
         "opReturn": "�FEIP|7|1||AvJxKExPi5nitH+jgZ6zbi60XReKtlhvh46K2Fb3fua7WP7XPC90HMpGIJYtvB0bQ8V+HqooZxzQIb6i5H7ful1XdQavVucPzHcoxDVJ4YFcOL1su+Dciv1bhgipG0Qfgg==",
         "signer": "F8FyPZKjQWQfdSLbxdJ14hx7SPsVW5nMeT",
         "recipient": "nobody",
         "time": 1585379186,
         "cdd": 0
      }
   }
}
```

## opReturnSearch
### 说明

对OpReturn响应数据data内的各项进行fcdsl查询。
  
### 默认排序

  - height: desc
  - txIndex: desc
  - id: asc

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        <查询语句...>
    }
}
```
### data in response body
  
```
{
    [
      {
        "txId": <string. OpReturnID，即所在交易id>,
        "height": <long. 所在区块高度>,
        "txIndex": <int. 所在交易在区块中的索引号>,
        "opReturn": <string. OpReturn内容>,
        "signer": <string. 签名人，即第一笔输入的fch地址>,
        "recipient": <string. 接受人，即第一个非签名人的收款地址>,
        "time": <long. 所在区块时间戳，单位秒>,
        "cdd": <long. 所在交易币天销毁总额，单位cd>
    },
    ...
   ]
}
```
### 示例

  - request body
  
```json
{
	"url": "http://localhost:8080/APIP/apip2/v1/opReturnSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "range": {
                "fields": ["cdd"],
                "gt": "100"
            }
        },
        "size":1
    }
}
```

  - response body
  
```json
{
   "code": 0,
   "message": "Success.",
   "nonce": 1987697,
   "balance": 35532428799,
   "got": 1,
   "total": 3583,
   "bestHeight": 1762451,
   "data": [
      {
         "txId": "49e37da370b2aa229ff133e358a232cd556a4bc65022b627379fb55354f7f14d",
         "height": 1759275,
         "txIndex": 1,
         "opReturn": "test",
         "signer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
         "recipient": "F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW",
         "time": 1684920105,
         "cdd": 465
      }
   ],
   "last": [
      "1759275",
      "1",
      "49e37da370b2aa229ff133e358a232cd556a4bc65022b627379fb55354f7f14d"
   ]
}
```

## addressByIds
### 说明

获取指定地址列表的详情列表。
  
### 默认排序

  - 无排序

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "ids":<string array. FCH地址数组. 必填>
    }
}
```
### data in response body
  
```
{
  <fid>:{
    "fid": <tring. address>,
    "pubKey": <tring. public key of this address>,
    "balance": <long. fch balance of this address in satoshi>,
    "income": <long. total income of this address in satoshi>,
    "expend": <long. total expend of this address in satoshi>,
    "guide": <tring. address of the guide of this address in satoshi>,
    "birthHeight": <long. the block height where this address born>,
    "lastHeight": <long. the block height where this address was updated most recently>,
    "cdd": <long. the total coindays this address destroied>,
    "cd": <long. the total coindays this address has now>,
    "cash": <int. the number of cashes this address has now>,
	"btcAddr": <tring. BTC classic address from pubKey above>,
	"ethAddr": <tring. ETH address from pubKey above>
  }
}
```
### 示例

  - request body
  
```json
{
   "url": "http://localhost:8080/APIP/apip2/v1/addressByIds",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "ids":["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"]
   }
}
```

  - response body
  
```json
{
   "code": 0,
   "message": "Success.",
   "nonce": 1987697,
   "balance": 35532328799,
   "got": 1,
   "total": 1,
   "bestHeight": 1764465,
   "data": {
      "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK": {
         "fid": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
         "pubKey": "030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a",
         "balance": 5052129138,
         "income": 133914424600,
         "expend": 160026612896,
         "guide": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
         "birthHeight": 67646,
         "lastHeight": 1760422,
         "cdd": 26407,
         "cd": 0,
         "weight": 4743,
         "cash": 14,
         "btcAddr": "19uwYXQejXqR3ALpsKEKF4xDc6h6xaBSA3",
         "ethAddr": "0x76057bbddc7011227cd894436556f74a09fa1a11"
      }
   }
}
```

## addressSearch
### 说明

对地址信息响应数据data内的各项进行fcdsl查询。
  
### 默认排序

  - lastHeight: desc
  - id: asc

### request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        <查询语句...>
    }
}
```
### data in response body
  
```
[{
    "fid": <tring. address>,
    "pubKey": <tring. public key of this address>,
    "balance": <long. fch balance of this address in satoshi>,
    "income": <long. total income of this address in satoshi>,
    "expend": <long. total expend of this address in satoshi>,
    "guide": <tring. address of the guide of this address in satoshi>,
    "birthHeight": <long. the block height where this address born>,
    "lastHeight": <long. the block height where this address was updated most recently>,
    "cdd": <long. the total coindays this address destroied>,
    "cd": <long. the total coindays this address has now>,
    "cash": <int. the number of cashes this address has now>,
	"btcAddr": <tring. BTC classic address from pubKey above>,
	"ethAddr": <tring. ETH address from pubKey above>
}]
```
### 示例

  - request body
  
```json
{
  "url": "http://localhost:8080/APIP/apip2/v1/addressSearch",
  "time": 1677673821267,
  "nonce": 1987697,
  "fcdsl":{
    "query":{
      "part":{
        "fields":["guide"],
        "value":"arm"
      }
    },
    "size":1
  }
}
```

  - response body
  
```json
{
   "code": 0,
   "message": "Success.",
   "nonce": 1987697,
   "balance": 35532228799,
   "got": 1,
   "total": 20,
   "bestHeight": 1765056,
   "data": [
      {
         "fid": "FRfwrTB7rjcKa8i8kzCZwXxgiYHK1izV3N",
         "pubKey": "033b01e4cc1a356f5195431a3b090df5efec2cc59c502e3c931416ae8a0942b90e",
         "balance": 36218855786,
         "income": 971692571385,
         "expend": 935473715599,
         "guide": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX",
         "birthHeight": 671935,
         "lastHeight": 1418775,
         "cdd": 42203,
         "cd": 0,
         "weight": 4219,
         "cash": 12,
         "btcAddr": "1LqqPek31RPewxq6uJYQy9S9gtGJ7nUtnJ",
         "ethAddr": "0x6982a8d2f515cc892abe1a18d31b11b57b572046"
      }
   ],
   "last": [
      "1418775",
      "FRfwrTB7rjcKa8i8kzCZwXxgiYHK1izV3N"
   ]
}
```