```
Type: APIP
SerialNumber: 3
ProtocolName: Cid
Version: 1
Description : Cid的主要信息.
Author: C_armX
Language: zh-CN
CreateDate: 2021-10-30
UpdateDate：2023-05-04
```

# APIP3V1_Cid(zh-CN)

## 目录

[关于APIP](#关于APIP)

[主要共识](#主要共识)

[cidAddrByIds](#)

[cidAddrSeek](#)

[cidSearch](#)

[cidHistory](#)

[comepageHistory](#)

[coticeFeeHistory](#)

[ceputationHistory](#)

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

1. 本协议接口提供Identity相关的主要信息。

2. 本协议接口的数据来源遵循以下协议: 
    - cid: 《FEIP3_CID》
    - homepage: 《FEIP26_Homepage》
    - noticeFee: 《FEIP27_NoticeFee》
    - master: 《FEIP6_Master》
	- weight: 《FIPA10_Weight》

3. 本协议接口数据采用JSON格式。

4. 本协议所有接口采用POST方法。

5. 本协议的接口的`urlTail`为: `apip3/v1/<接口名称>`。

6. 各接口具体共识如下：

---

## cidInfoByIds

### 说明

获取指定fid列表的address与cid合并详情列表。
  
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
    "fid": <string. address>,
	"cid": <string. cid>,
	"usedCid": <string array. used cid, no more than 4 elements>
    "pubKey": <string. public key of this address>,
	"priKey": <string. private key of this address>,
    "balance": <long. fch balance of this address in satoshi>,
    "income": <long. total income of this address in satoshi>,
    "expend": <long. total expend of this address in satoshi>,
    "guide": <string. address of the guide of this address in satoshi>,
	"reputation": <long. total reputation rated by others>,
	"hot": <long. total cdd all the reputation costs>,
	"weight": <long. weight caculated with cd, cdd and reputation, see FIPA10_Weight>,
	"master": <string. master of this cid, see FEIP6_Master>,
	"homepages":<string array. homepages>,
	"noticeFee":<string, the fee the message sender has to pay. amount of fch in string.>,
	"nameTime": <long. the block height where this address was named with its first cid>,
    "birthHeight": <long. the block height where this address born>,
    "lastHeight": <long. the block height where this address was updated most recently>,
    "cdd": <long. the total coindays this address destroied>,
    "cd": <long. the total coindays this address has now>,
    "cash": <int. the number of cashes this address has now>,
	"btcAddr": <string. BTC classic address from pubKey above>,
	"ethAddr": <string. ETH address from pubKey above>
  }
}

```
### 示例

  - request body

```json
{
  "url": "https://cid.cash/APIP/apip3/v1/cidInfoByIds",
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
  "balance": 35531928799,
  "got": 1,
  "total": 1,
  "bestHeight": 1768551,
  "data": {
    "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK": {
      "fid": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
      "cid": "春花_cjWK",
      "usedCids": [
        "chunhua_cjWK",
        "春花_cjWK"
      ],
      "pubKey": "030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a",
      "priKey": "L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8",
      "balance": 5052128258,
      "cash": 16,
      "income": 137166556506,
      "expend": 163278745682,
      "cd": 0,
      "cdd": 26568,
      "reputation": 381,
      "hot": 23239,
      "weight": 2847,
      "master": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX",
      "guide": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
      "btcAddr": "19uwYXQejXqR3ALpsKEKF4xDc6h6xaBSA3",
      "ethAddr": "0x76057bbddc7011227cd894436556f74a09fa1a11",
      "birthHeight": 67646,
      "nameTime": 1619134827,
      "lastHeight": 1768106
    }
  }
}

```
## fidCidSeek

### 说明

查询fid，cid，或usedCids包含指定内容的fid与usedCids列表。
  
### 默认排序

  - lastHeight：desc
  - id：asc

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "query":{
          "part":{
            "fields":["id","usedCids"],
            "value":<string, 查询内容>
          },
		  ...
        },
		...
    }
}
```
### data in response body
```
{
	<fid>: <usedCids. string array. used cid array>,
	...
}

```
### 示例
  - request body
```json
{
  "url": "https://cid.cash/APIP/apip3/v1/fidCidSeek",
  "time": 1677673821267,
  "nonce": 1987697,
  "fcdsl":{
    "query":{
      "part":{
        "fields":["fid","usedCids"],
        "value":"ar"
      }
    },
    "size":2
  }
}
```
  - response body
```json
{
  "code": 0,
  "message": "Success.",
  "nonce": 1987697,
  "balance": 35531328799,
  "got": 2,
  "total": 16,
  "bestHeight": 1768553,
  "data": {
    "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX": [
      "C_armX"
    ],
    "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK": [
      "chunhua_cjWK",
      "春花_cjWK"
    ]
  },
  "last": [
    "1567767",
    "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"
  ]
}
```
## cidInfoSearch

### 说明

对Cid信息响应数据data内的各项进行fcdsl查询。
  
### 默认排序

  - lastHeight: desc
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
    "fid": <string. address>,
	"cid": <string. cid>,
	"usedCid": <string array. used cid, no more than 4 elements>
    "pubKey": <string. public key of this address>,
	"priKey": <string. private key of this address>,
    "balance": <long. fch balance of this address in satoshi>,
    "income": <long. total income of this address in satoshi>,
    "expend": <long. total expend of this address in satoshi>,
    "guide": <string. address of the guide of this address in satoshi>,
	"reputation": <long. total reputation rated by others>,
	"hot": <long. total cdd all the reputation costs>,
	"weight": <long. weight caculated with cd, cdd and reputation, see FIPA10_Weight>,
	"master": <string. master of this cid, see FEIP6_Master>,
	"homepages":<string array. homepages>,
	"noticeFee":<string, the fee the message sender has to pay. amount of fch in string.>,
	"nameTime": <long. the block height where this address was named with its first cid>,
    "birthHeight": <long. the block height where this address born>,
    "lastHeight": <long. the block height where this address was updated most recently>,
    "cdd": <long. the total coindays this address destroied>,
    "cd": <long. the total coindays this address has now>,
    "cash": <int. the number of cashes this address has now>,
	"btcAddr": <string. BTC classic address from pubKey above>,
	"ethAddr": <string. ETH address from pubKey above>
	}
]
```
### 示例
  - request body
```json
{
  "url": "https://cid.cash/APIP/apip3/v1/cidInfoSearch",
  "time": 1677673821267,
  "nonce": 1987697,
  "fcdsl": {
    "query": {
      "part": {
        "fields": ["cid"],
        "value": "arm",
        "isCaseInsensitive": "true"
      }
    },
    "size":"2"
  }
}
```
  - response body
```json
{
    "code": 0,
    "message": "Success.",
    "nonce": 1987697,
    "balance": 35531228799,
    "got": 1,
    "total": 1,
    "bestHeight": 1768553,
    "data": [
        {
            "fid": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX",
            "cid": "C_armX",
            "usedCids": [
                "C_armX"
            ],
            "pubKey": "03c1c6b2c2a473ffa0cf92a11ffd0196414078e1a454d69e0a996aaca6ec953ad0",
            "balance": 164634385327388,
            "cash": 1579,
            "income": 9529508579494,
            "expend": 9080355579494,
            "cd": 0,
            "cdd": 498959,
            "reputation": 77,
            "hot": 77,
            "weight": 49934,
            "guide": "F6nqSoNvZrJG6UZtnXDkFAZo63Et34PDSG",
            "btcAddr": "1Gjm3U1zqvGB5cgfV1shyyzzEStCSV1ni8",
            "ethAddr": "0x102e09e6fb98a6ba6c7f1a6e99332f694705a0ec",
            "birthHeight": 131408,
            "nameTime": 1620389960,
            "lastHeight": 1765037
        }
    ],
    "last": [
        "1755960",
        "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX"
    ]
}
```
## cidHistory

### 说明

获取指定fid的cid操作记录。
  
### 默认排序
- height:desc
- index:desc

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "query":{
          "terms":{
            "fields":["signer"],
            "values":[<fid>]
          },
          <filter 不可用>
		...
    }
}
```
### data in response body
```
[
	{
		"txId": <string. txid where the record is located>,
		"height": <long. block height>,
		"index": <int. index of the transaction in the block>,
		"time": <long. block timestamp>,
		"sn": "3",
		"ver": <int. protocol version>,
		"signer": <the signer of this transaction>,
		"op": <string. operation>
	}
]

```
### 示例
  - request body
```json
{
  "url": "https://cid.cash/APIP/apip3/v1/cidHistory",
  "time": 1677673821267,
  "nonce": 1987697,
  "fcdsl": {
    "query": {
      "terms": {
        "fields": ["signer"],
        "values": ["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"]
      }
    },
    "size":"2"
  }
}
```
  - response body
```json
{
  "code": 0,
  "message": "Success.",
  "nonce": 1987697,
  "balance": 35528828799,
  "got": 2,
  "total": 4,
  "bestHeight": 1768581,
  "data": [
    {
      "txId": "2f97bf160bbab46bf9fe062b51eb5bc8aba8d90b6125b25b73358eea2d548ab6",
      "height": 1540631,
      "index": 1,
      "time": 1671424321,
      "sn": "3",
      "ver": "4",
      "signer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
      "op": "unregister"
    },
    {
      "txId": "54c05b57b4b42d5b6907bf1a89d59b3c652c2cae4ee392a73b5a9800756c7b78",
      "height": 710372,
      "index": 1,
      "time": 1620621694,
      "sn": "3",
      "ver": "4",
      "signer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
      "op": "register",
      "name": "春花"
    }
  ],
  "last": [
    "710372",
    "1"
  ]
}
```

## homepageHistory

### 说明
获取指定fid的homepages历史记录。
  
### 默认排序

  - height:desc
  - index:desc

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "query":{
          "terms":{
            "fields":["signer"],
            "values":[<fid>]
          },
        <filter 不可用>
		...
    }
}
```
### data in response body
```
[
	{
		"txId": <string. txid where the record is located>,
		"height": <long. block height>,
		"index": <int. index of the transaction in the block>,
		"time": <long. block timestamp>,
		"sn": "9",
		"ver": <int. protocol version>,
		"signer": <the signer of this transaction>,
		"op": <string. operation>,
		"homepages":<string array. homepages>
	}
]

```
### 示例
  - request body
```json
{
  "url": "https://cid.cash/APIP/apip3/v1/homepageHistory",
  "time": 1677673821267,
  "nonce": 1987697,
  "fcdsl": {
    "query": {
      "terms": {
        "fields": ["signer"],
        "values": ["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"]
      }
    },
    "size":"2"
  }
}
```
  - response body
```json
{
  "code": 0,
  "message": "Success.",
  "nonce": 1987697,
  "balance": 35528728799,
  "got": 1,
  "total": 1,
  "bestHeight": 1768581,
  "data": [
    {
      "txId": "4e41ce80627ef4c6aa42bba53d1dbf4d897ae7eba217793737ca0998bd7e8372",
      "height": 1732750,
      "index": 2,
      "time": 1683205158,
      "sn": "9",
      "ver": "1",
      "signer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
      "op": "register",
      "homepages": [
        "https://cid.cash",
        "127.0.0.1:8080"
      ]
    }
  ],
  "last": [
    "1732750",
    "2"
  ]
}
```
## noticeFeeHistory

### 说明

获取指定fid的noticeFee声明历史记录。
  
### 默认排序

  - height: desc
  - index: desc

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "query":{
          "terms":{
            "fields":["signer"],
            "values":[<fid>]
          },
		<filter 不可用>
    }
}
```
### data in response body
```
[
	{
		"txId": <string. txid where the record is located>,
		"height": <long. block height>,
		"index": <int. index of the transaction in the block>,
		"time": <long. block timestamp>,
		"sn": "9",
		"ver": <int. protocol version>,
		"signer": <the signer of this transaction>,
		"noticeFee": <string. noticeFee>
	}
]

```
### 示例
  - request body
```json
{
  "url": "https://cid.cash/APIP/apip3/v1/noticeFeeHistory",
  "time": 1677673821267,
  "nonce": 1987697,
  "fcdsl": {
    "query": {
      "terms": {
        "fields": ["signer"],
        "values": ["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"]
      }
    },
    "size":"2"
  }
}
```

  - response body
```json
{
  "code": 0,
  "message": "Success.",
  "nonce": 1987697,
  "balance": 35528528799,
  "got": 2,
  "total": 2,
  "bestHeight": 1768587,
  "data": [
    {
      "txId": "d387e0b16ba295f427c57416872a7bfae7d825fd3d57b368079cded9bc41c8c2",
      "height": 1732797,
      "index": 1,
      "time": 1683207107,
      "sn": "10",
      "ver": "1",
      "signer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
      "noticeFee": "0.0001"
    },
    {
      "txId": "0eb60e05e8e445904285a75866235a200e33eedb815fcaea0adb2d6628d7724e",
      "height": 1732788,
      "index": 1,
      "time": 1683206673,
      "sn": "10",
      "ver": "1",
      "signer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
      "noticeFee": "1.0E-4"
    }
  ],
  "last": [
    "1732788",
    "1"
  ]
}
```
## reputationHistory

### 说明

获取指定cash ID列表的cash详情列表。
  
### 默认排序

  - height: desc
  - index: desc

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "query":{
          "terms":{
            "fields":["ratee"],
            "values":[<fid>]
          }
		},
        <filter 不可用>
    }
}
```
### data in response body
```
[
	{
		"txId": <string. txid where the record is located>,
		"height": <long. block height>,
		"index": <int. index of the transaction in the block>,
		"time": <long. block timestamp>,
		"ratee": <string. address of first output>,
		"rater": <string. address of first input>
		"reputation": <long. cdd of the rating tx mutiplied by 1 for good or -1 for bad>,
		"hot": <long. cdd of the rating tx>,
		"cause": <string. the cause why rating>
	}
]

```
### 示例
  - request body
```json
{
  "url": "https://cid.cash/APIP/apip3/v1/reputationHistory",
  "time": 1677673821267,
  "nonce": 1987697,
  "fcdsl": {
    "query":{
      "terms":{
        "fields":["ratee"],
        "values":["FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX"]
      }
    },
    "size":"1"
  }
}
```
  - response body
```json
{
  "code": 0,
  "message": "Success.",
  "nonce": 1987697,
  "balance": 35528428799,
  "got": 1,
  "total": 1,
  "bestHeight": 1768587,
  "data": [
    {
      "txId": "f6c358e566ddb7d33ea2e99e2c3670786636285031838c751535368e108818a0",
      "height": 1755960,
      "index": 1,
      "time": 1684716572,
      "ratee": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX",
      "rater": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
      "reputation": 77,
      "hot": 77
    }
  ],
  "last": [
    "1755960",
    "1"
  ]
}
```