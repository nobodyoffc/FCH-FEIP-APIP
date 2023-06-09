```
Type:APIP
SerialNumber:8
ProtocolName:Group
Version:1
Description:定义群信息查询接口。
Author:C_armX
Language:zh-CN
CreateDate: 2021-12-03
UpdateDate: 2023-05-07
```

# APIP8V1_Group(zh-CN)

---
## 目录

[关于APIP](#关于APIP)

[主要共识](#主要共识)

[GroupByIds](#GroupByIds)

[GroupSearch](#GroupSearch)

[GroupOpHistory](#GroupOpHistory)

[GroupMembers](#GroupMembers)

[MyGroups](#MyGroups)

---

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

1. 本协议接口提供链上注册的群（group）的相关信息。

2. 本协议接口的数据来源遵循以下协议: 

   - 《FEIP19_Group》

3. 本协议接口数据采用JSON格式。

4. 本协议所有接口采用POST方法。

5. 本协议的接口的`urlTail`为: `apip8/v1/<接口名称>`。

6. 各接口具体共识如下: 

---

## groupByIds
### 说明

获取指定gid列表的group详情列表。
  
### 默认排序

  - 无排序

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "ids":<string array. gid数组. 必填>
    }
}
```
### data in response body
  
```
{
    <gid>:{
		"gid": <string. the gid of the group>,
		"name": <string. name of the protocol>,
		"desc": <string. description of the protocol>,
		"namers": <string array. all addresses who named this group in naming order>,
		"memberNum": <long. the number of members of this group> ,
		"birthTime": <long. the timestamp of the block in which the group was published> ,
		"birthHeight": <long. the height of the block in which the group was published>,
		"lastTxid": <string. the txid in which the group was operated most recently except rating>,
		"lastTime": <long. the timestamp of the block in which the group was operated most recently except rating>,
		"lastHeight": <long. the height of the block in which the group was operated most recently except rating>,
		"cddToUpdate": <long. the cdd required when rename this group>,
		"tCdd": <long. total cdd all ratings on this group consumed>
	},
    ...
}

```
### 示例

  - request body
  
```json
{
	"url": "http://localhost:8080/APIP/apip8/v1/groupByIds",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "ids":["a417806906249b6badbc0a9af62446e2a54b189a79e5d98512a58776cc21654a"]
    }
}
```

  - response body
  
```json
{
    "code": 0,
    "message": "Success.",
    "balance": 35575828799,
    "got": 1,
    "total": 1,
    "bestHeight": 1741695,
    "data": {
        "a417806906249b6badbc0a9af62446e2a54b189a79e5d98512a58776cc21654a": {
            "gid": "a417806906249b6badbc0a9af62446e2a54b189a79e5d98512a58776cc21654a",
            "name": "Cryptocircle",
            "desc": "welcome to the future Cryptoworld",
            "namers": [
                "FQ9KvLMbDd3EJy92pZzMKXBF9XASF4Bb4P"
            ],
            "memberNum": 193,
            "birthTime": 1623114898,
            "birthHeight": 751754,
            "lastTxid": "b175886b66ccf8b0b6992ea185104edf6d51131c6e58b2b64cb2d5a59b1b7103",
            "lastTime": 1636031143,
            "lastHeight": 963969,
            "cddToUpdate": 1,
            "tCdd": 705
        }
    }
}
```

## groupSearch
### 说明

对group信息响应数据data内的各项进行fcdsl查询。
  
### 默认排序

  - tCdd: desc
  - gid: asc

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
		"gid": <string. the gid of the group>,
		"name": <string. name of the protocol>,
		"desc": <string. description of the protocol>,
		"namers": <string array. all addresses who named this group in naming order>,
		"memberNum": <long. the number of members of this group> ,
		"birthTime": <long. the timestamp of the block in which the group was published> ,
		"birthHeight": <long. the height of the block in which the group was published>,
		"lastTxid": <string. the txid in which the group was operated most recently except rating>,
		"lastTime": <long. the timestamp of the block in which the group was operated most recently except rating>,
		"lastHeight": <long. the height of the block in which the group was operated most recently except rating>,
		"cddToUpdate": <long. the cdd required when rename this group>,
		"tCdd": <long. total cdd all ratings on this group consumed>
	}
]
```
### 示例

  - request body
  
```json
{
	"url": "http://localhost:8080/APIP/apip8/v1/groupSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "part":{
                "fields":["name"],
                "value":"test"
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
    "balance": 35575628799,
    "got": 1,
    "total": 3,
    "bestHeight": 1741695,
    "data": [
        {
            "gid": "6305d16c89fb98763b6968049096a984eea9334e12c514c9b72098c3f332d114",
            "name": "test1",
            "desc": "This is a updating test.",
            "namers": [
                "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
                "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv"
            ],
            "memberNum": 1,
            "birthTime": 1672811526,
            "birthHeight": 1563390,
            "lastTxid": "f3b145dbaf7e8a9e9cd05b7ce26c2b695dbb28c17bbda79e35b69b90cf8bafde",
            "lastTime": 1683710581,
            "lastHeight": 1740989,
            "cddToUpdate": 1,
            "tCdd": 179
        }
    ],
    "last": [
        "179",
        "6305d16c89fb98763b6968049096a984eea9334e12c514c9b72098c3f332d114"
    ]
}
```
## groupOpHistory

### 说明
获取指定address的cid操作记录。
  
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
        <查询语句...>
    }
}
```
### data in response body
```
[
	{
		"id": <string. txid where the record is located>,
		"height": <long. block height>,
		"index": <int. index of the transaction in the block>,
		"time": <long. block timestamp>,
		"signer": <the signer of this transaction>,
		"gid": <string. The gid of the group>,
		"op": <string. operation>,
		"name": <string. name of the protocol>,
		"desc": <string. description of the protocol>,
		"namers": <string array. all addresses who named this group in naming order>,
		"members": <string array. all address of members of this group>,
		"cdd": <long. cdd when operating>
	}
]

```
### 示例
  - request body
```json
{
	"url": "http://localhost:8080/APIP/apip8/v1/groupOpHistory",
	"time": 1677673821267,
	"nonce": 1987697,
	"fcdsl": {
		"query":{
		    "terms":{
		        "fields":["gid"],
		        "values":["6305d16c89fb98763b6968049096a984eea9334e12c514c9b72098c3f332d114"]
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
    "balance": 35577028799,
    "got": 1,
    "total": 6,
    "bestHeight": 1741664,
    "data": [
        {
            "id": "f3b145dbaf7e8a9e9cd05b7ce26c2b695dbb28c17bbda79e35b69b90cf8bafde",
            "height": 1740989,
            "index": 2,
            "time": 1683710581,
            "signer": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
            "gid": "6305d16c89fb98763b6968049096a984eea9334e12c514c9b72098c3f332d114",
            "op": "leave",
            "cdd": 0
        }
    ],
    "last": [
        "1740989",
        "2"
    ]
}
```

## groupMembers

### 说明

获取指定gid的members。
  
### 默认排序

  无

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "ids":<string array. gid数组，长度1. 必填>
    }
}
```
### data in response body
```
[
	{
		<gid>:[
			<addresses of members>
		]
	}
]

```
### 示例
  - request body
```json
{
	"url": "http://localhost:8080/APIP/apip8/v1/groupMembers",
	"time": 1677673821267,
	"nonce": 1987697,
	"fcdsl": {
		"ids":["a7134cfec5dbdcb6b08e6860747ab63c70d98dd395e3df705d816ba32e9b9325"]
	}
}
```
  - response body
```json
{
    "code": 0,
    "message": "Success.",
    "balance": 35576728799,
    "got": 1,
    "total": 0,
    "bestHeight": 1741669,
    "data": {
        "a7134cfec5dbdcb6b08e6860747ab63c70d98dd395e3df705d816ba32e9b9325": [
            "FLDXdRPceMwK9oLPttb7Q6wUMEBrotU6ok",
            "FQajDEMGo6mePEVbBKYtcn68npTUk3yeT1",
            "FSPivzBHTMUmEiVMy7Pmq8oh5AGU3o8Ffr"
        ]
    }
}
```
## myGroups

### 说明

查询某address所在的所有group详情列表。
  
### 默认排序

  - lastHeight: desc
  - gid: asc

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
		"name": <string. name of the protocol>,
		"memberNum": <long. the number of members of this group> ,
		"gid": <string. the gid of the group>,
		"tCdd": <long. total cdd all ratings on this group consumed>
	}
]
```
### 示例

  - request body
  
```json
{
	"url": "http://localhost:8080/APIP/apip8/v1/myGroups",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "terms":{
                "fields":["members"],
                "values":["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"]
            }
        },
        "size":3
    }
}
```

  - response body
  
```json
{
    "code": 0,
    "message": "Success.",
    "balance": 35572528799,
    "got": 3,
    "total": 5,
    "bestHeight": 1741890,
    "data": [
        {
            "name": "FCH密圈群",
            "memberNum": 47,
            "gid": "85ac06c630df827d566938ed893d97e00ea49c259c28db4f1c8322b7b8da1898",
            "tCdd": 59911
        },
        {
            "name": "慕道社",
            "memberNum": 3,
            "gid": "1caeac07c65f2fdfb91f0e5bb7c89234940418cc4f77fa9b0d50d1608e06a3f1",
            "tCdd": 199
        },
        {
            "name": "Java3",
            "memberNum": 2,
            "gid": "6458581e7e42301834614643021c4591ca679e7fe85c7b1e45ad8a6770b6c125",
            "tCdd": 1
        }
    ],
    "last": [
        "690524",
        "6458581e7e42301834614643021c4591ca679e7fe85c7b1e45ad8a6770b6c125"
    ]
}
```