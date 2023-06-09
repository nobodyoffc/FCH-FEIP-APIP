```
Type:APIP
SerialNumber:4
ProtocolName:Protocol
Version:1
Description:定义协议信息查询接口。
Author:C_armX
Language:zh-CN
CreateDate: 2021-11-20
UpdateDate：2023-05-07
```

# APIP4V1_Protocol(zh-CN)

## 目录

[关于APIP](#关于APIP)

[主要共识](#主要共识)

[ProtocolByIds](#ProtocolByIds)

[ProtocolOpHistory](#ProtocolOpHistory)

[ProtocolRateHistory](#ProtocolRateHistory)

[ProtocolSearch](#ProtocolSearch)


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

1. 本协议接口提供链上注册的各种协议的相关信息。

2. 本协议接口的数据来源遵循以下协议：

   - 《FEIP1_Protocol》

3. 本协议接口数据采用JSON格式。

4. 本协议所有接口采用POST方法。

5. 本协议的接口的`urlTail`为: `apip4/v1/<接口名称>`。

6. 各接口具体共识如下：

---

## protocolByIds
### 说明

获取指定pid列表的protocol详情列表。
  
### 默认排序

  - 无排序

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "ids":<string array. pid数组. 必填>
    }
}
```
### data in response body
  
```
{
    <pid>:{
		"pid": <string. The pid of the protocol>,
		"type": <string. type of the protocol>,
		"sn": <string. ,serial number of the protocol>,
		"ver": <string. version number of the protocol>,
		"name": <string. name of the protocol>,
		"desc": <string. description of the protocol>,
		"waiters": <string array. fch addresses of the waiters>,
		"lang": <string. The language of the protocol formatted with i18n>,
		"fileUrls": <string. location URLs to get the protocol file> ,
		"title": <string. the full title of the protocos,like FEIP3V1_CID(zh-CN)",
		"owner": <string. address of the publisher>,
		"birthTxid": <string. the txid in which the protocol was published>,
		"birthTime": <long. the timestamp of the block in which the protocol was published> ,
		"birthHeight": <long. the height of the block in which the protocol was published>,
		"lastTxid": <string. the txid in which the protocol was operated most recently except rating>,
		"lastTime": <long. the timestamp of the block in which the protocol was operated most recently except rating>,
		"lastHeight": <long. the height of the block in which the protocol was operated most recently except rating>,
		"tCdd": <long. total cdd all ratings on this protocol consumed>,
		"tRate": <float. the final rating score of the protocol>,
		"active": <boolean. is this protocol active now>,
		"closed": <boolean. is this protocol has been closed>
	},
    ...
}

```
### 示例

  - request body
  
```json
{
	"url": "http://localhost:8080/APIP/apip4/v1/protocolByIds",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "ids":["bdaef41f270b0f71d3145396b459a7563bb349150fce7d882ce7a0c66e830d02"]
    }
}
```

  - response body
  
```json
{
    "code": 0,
    "message": "Success.", 
    "nonce": 1987697,
    "balance": 35583028799,
    "got": 1,
    "total": 1,
    "bestHeight": 1735063,
    "data": {
        "bdaef41f270b0f71d3145396b459a7563bb349150fce7d882ce7a0c66e830d02": {
            "pid": "bdaef41f270b0f71d3145396b459a7563bb349150fce7d882ce7a0c66e830d02",
            "type": "FIPA",
            "sn": "8",
            "ver": "1",
            "did": "7ba861e13d49ec6466a27a8fceb0513fb34a36203d11ea16a8ebaad8e09df1e4",
            "name": "AES256CBC??????",
            "lang": "zh-CN",
            "desc": "AES-256-CBC-PKCS7Padding????",
			"waiters": [
			    "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"
			],
            "fileUrls": [
                "https://github.com/nobodyoffc/FIPA/blob/main/FIPA8V1_AES256CBC????(zh-CN).md"
            ],
            "title": "FIPA8V1_AES256CBC??????(zh-CN)",
            "owner": "FJYN3D7x4yiLF692WUAe7Vfo2nQpYDNrC7",
            "birthTxid": "bdaef41f270b0f71d3145396b459a7563bb349150fce7d882ce7a0c66e830d02",
            "birthTime": 1680943783,
            "birthHeight": 1695695,
            "lastTxid": "bdaef41f270b0f71d3145396b459a7563bb349150fce7d882ce7a0c66e830d02",
            "lastTime": 1680943783,
            "lastHeight": 1695695,
            "tCdd": 0,
            "tRate": 0.0,
            "active": true,
            "closed": false
        }
    }
}
```

## protocolSearch
### 说明

对protocol信息响应数据data内的各项进行fcdsl查询。
  
### 默认排序

  - active: desc
  - tRate: desc
  - pid: asc

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
		"pid": <string. The pid of the protocol>,
		"type": <string. type of the protocol>,
		"sn": <string. ,serial number of the protocol>,
		"ver": <string. version number of the protocol>,
		"name": <string. name of the protocol>,
		"desc": <string. description of the protocol>,
		"waiters": <string array. fch addresses of the waiters>,
		"lang": <string. The language of the protocol formatted with i18n>,
		"fileUrls": <string. location URLs to get the protocol file> ,
		"title": <string. the full title of the protocos,like FEIP3V1_CID(zh-CN)",
		"owner": <string. address of the publisher>,
		"birthTxid": <string. the txid in which the protocol was published>,
		"birthTime": <long. the timestamp of the block in which the protocol was published> ,
		"birthHeight": <long. the height of the block in which the protocol was published>,
		"lastTxid": <string. the txid in which the protocol was operated most recently except rating>,
		"lastTime": <long. the timestamp of the block in which the protocol was operated most recently except rating>,
		"lastHeight": <long. the height of the block in which the protocol was operated most recently except rating>,
		"tCdd": <long. total cdd all ratings on this protocol consumed>,
		"tRate": <float. the final rating score of the protocol>,
		"active": <boolean. is this protocol active now>,
		"closed": <boolean. is this protocol has been closed>
	}
]
```
### 示例

  - request body
  
```json
{
	"url": "http://localhost:8080/APIP/apip4/v1/protocolSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "part":{
                "fields":["name"],
                "value":"AES"
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
    "balance": 35584828799,
    "got": 1,
    "total": 1,
    "bestHeight": 1735025,
    "data": [
        {
            "pid": "bdaef41f270b0f71d3145396b459a7563bb349150fce7d882ce7a0c66e830d02",
            "type": "FIPA",
            "sn": "8",
            "ver": "1",
            "did": "7ba861e13d49ec6466a27a8fceb0513fb34a36203d11ea16a8ebaad8e09df1e4",
            "name": "AES256CBC??????",
            "lang": "zh-CN",
            "desc": "AES-256-CBC-PKCS7Padding????",
            "fileUrls": [
                "https://github.com/nobodyoffc/FIPA/blob/main/FIPA8V1_AES256CBC????(zh-CN).md"
            ],
            "title": "FIPA8V1_AES256CBC??????(zh-CN)",
            "owner": "FJYN3D7x4yiLF692WUAe7Vfo2nQpYDNrC7",
            "birthTxid": "bdaef41f270b0f71d3145396b459a7563bb349150fce7d882ce7a0c66e830d02",
            "birthTime": 1680943783,
            "birthHeight": 1695695,
            "lastTxid": "bdaef41f270b0f71d3145396b459a7563bb349150fce7d882ce7a0c66e830d02",
            "lastTime": 1680943783,
            "lastHeight": 1695695,
            "tCdd": 0,
            "tRate": 0.0,
            "active": true,
            "closed": false
        }
    ]
}
```
## protocolOpHistory

### 说明
获取protocol的历史操作记录，rate除外。
  
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
        <except 不可用>
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
		"signer": <the signer of this transaction>,
		"pid": <string. The pid of the protocol>,
		"op": <string. operation>
		"type": <string. type of the protocol>,
		"sn": <string. ,serial number of the protocol>,
		"ver": <string. version number of the protocol>,
		"name": <string. name of the protocol>,
		"desc": <string. description of the protocol>,
		"waiters": <string array. fch addresses of the waiters>,
		"lang": <string. The language of the protocol formatted with i18n>,
		"fileUrls": <string. location URLs to get the protocol file> ,
	}
]

```
### 示例
  - request body
```json
{
	"url": "http://localhost:8080/APIP/apip4/v1/protocolOpHistory",
	"time": 1677673821267,
	"nonce": 1987697,
	"fcdsl": {
		"query":{
		    "terms":{
		        "fields":["pid"],
		        "values":["c50d307c3ac0c193dad6c671ad3cebb881c01c747e03abfeaecc378419739ff4"]
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
    "balance": 35583328799,
    "got": 1,
    "total": 6,
    "bestHeight": 1635037,
    "data": [
        {
            "txId": "c50d307c3ac0c193dad6c671ad3cebb881c01c747e03abfeaecc378419739ff4",
            "height": 1553773,
            "index": 1,
            "time": 1672214047,
            "signer": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
            "type": "FEIP",
            "sn": "3",
            "ver": "4",
            "name": "CID",
            "desc": "Register or unregister a human friendly identity for an address.",
            "lang": "zh-CN",
            "fileUrls": [
                "https://github.com/freecashorg/FEIP/FEIP3_CID/"
            ],
            "pid": "c50d307c3ac0c193dad6c671ad3cebb881c01c747e03abfeaecc378419739ff4",
            "op": "publish"
        }
    ],
    "last": [
        "1553773",
        "1"
    ]
}
```


## protocolRateHistory

### 说明

获取protocol的rate记录。
  
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
		"signer": <string. address rating>,
		"pid": <string. pid of rated protocol>,
		"op": "rate",
		"rate": <long. rating score from 0 to 5>,
		"cdd": <long. cdd when rating>
	}
]

```
### 示例
  - request body
```json
{
	"url": "http://localhost:8080/APIP/apip4/v1/protocolRateHistory",
	"time": 1677673821267,
	"nonce": 1987697,
	"fcdsl": {
		"query":{
		    "terms":{
		        "fields":["pid"],
		        "values":["c50d307c3ac0c193dad6c671ad3cebb881c01c747e03abfeaecc378419739ff4"]
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
    "balance": 35583228799,
    "got": 1,
    "total": 1,
    "bestHeight": 1735037,
    "data": [
        {
            "txId": "a2359e0fd1f89e920378d642be9f1d99dc59a7c7b2190902f23db6dba14f5199",
            "height": 1556597,
            "index": 2,
            "time": 1672383291,
            "signer": "FJeRnehc2VvRMV3wRF2AtiXDVnaKnmnZhp",
            "pid": "c50d307c3ac0c193dad6c671ad3cebb881c01c747e03abfeaecc378419739ff4",
            "op": "rate",
            "rate": 5,
            "cdd": 5
        }
    ],
    "last": [
        "1556597",
        "2"
    ]
}
```