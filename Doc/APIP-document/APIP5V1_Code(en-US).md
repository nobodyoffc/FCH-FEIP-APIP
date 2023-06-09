```
Type:APIP
SerialNumber:5
ProtocolName:Code
Version:1
Description:定义代码信息查询接口。
Author:C_armX
Language:zh-CN
CreateDate: 2023-04-20
UpdateDate: 2023-05-07
```

# APIP5V1_Code(zh-CN)

## 目录

[关于APIP](#关于APIP)

[主要共识](#主要共识)

[CodeByIds](#CodeByIds)

[CodeOpHistory](#CodeOpHistory)

[CodeRateHistory](#CodeRateHistory)

[CodeSearch](#CodeSearch)


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

1. 本协议接口提供链上注册的各种代码的相关信息。

2. 本协议接口的数据来源遵循以下协议: 

   - 《FEIP2_Code》

3. 本协议接口数据采用JSON格式。

4. 本协议所有接口采用POST方法。

5. 本协议的接口的`urlTail`为: `apip5/v1/<接口名称>`。

6. 各接口具体共识如下: 

---

## codeByIds
### 说明

获取指定codeId列表的code详情列表。
  
### 默认排序

  - 无排序

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "ids":<string array. codeId数组. 必填>
    }
}
```
### data in response body
  
```
{
    <codeId>:{
		"codeId": <string. The codeId of the code>,
		"name": <string. name of the code>,
		"ver": <string. version number of the code>,
		"desc": <string. description of the code>,
		"waiters": <string array. fch addresses of the waiters>,
		"langs": <string array. The languages of the code formatted with i18n>,
		"urls": <string. URLs to get the code> ,
		"protocols": <string array. the PIDs of protocos this code applied,",
		"owner": <string. address of the publisher>,
		"birthTxid": <string. the txid in which the code was published>,
		"birthTime": <long. the timestamp of the block in which the code was published> ,
		"birthHeight": <long. the height of the block in which the code was published>,
		"lastTxid": <string. the txid in which the code was operated most recently except rating>,
		"lastTime": <long. the timestamp of the block in which the code was operated most recently except rating>,
		"lastHeight": <long. the height of the block in which the code was operated most recently except rating>,
		"tCdd": <long. total cdd all ratings on this code consumed>,
		"tRate": <float. the final rating score of the code>,
		"active": <boolean. is this code active now>,
		"closed": <boolean. is this code has been closed>
	},
    ...
}

```
### 示例

  - request body
  
```json
{
	"url": "http://localhost:8080/APIP/apip5/v1/codeByIds",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "ids":["b8e1a19eadb3f7a639bddf360fe253226439c32197c81242b69a1ca390f87151"]
    }
}
```

  - response body
  
```json
{
    "code": 0,
    "message": "Success.", 
    "nonce": 1987697,
    "balance": 35581228799,
    "got": 1,
    "total": 1,
    "bestHeight": 1736854,
    "data": {
        "b8e1a19eadb3f7a639bddf360fe253226439c32197c81242b69a1ca390f87151": {
            "codeId": "b8e1a19eadb3f7a639bddf360fe253226439c32197c81242b69a1ca390f87151",
            "name": "FreeChain",
            "ver": "2",
            "desc": "The code to parse basic information from the freecash blockchain.",
            "langs": [
                "java"
            ],
            "urls": [
                "https://github.com/nobodyoffc/FreeChain.git"
            ],
            "protocols": [],
            "owner": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
            "birthTime": 1672214126,
            "birthHeight": 1553774,
            "lastTxid": "7a04b3a4d13a1d2a2ffa733706e5e79e18d34b4a295e5eae6f5184d89597cf88",
            "lastTime": 1672223796,
            "lastHeight": 1553943,
            "tCdd": 3985,
            "tRate": 4.0,
            "active": true,
            "closed": false
        }
    }
}
```

## codeSearch
### 说明

对code信息响应数据data内的各项进行fcdsl查询。
  
### 默认排序

  - active: desc
  - tRate: desc
  - codeId: asc

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
		"codeId": <string. The codeId of the code>,
		"name": <string. name of the code>,
		"ver": <string. version number of the code>,
		"desc": <string. description of the code>,
		"waiters": <string array. fch addresses of the waiters>,
		"langs": <string array. The languages of the code formatted with i18n>,
		"urls": <string. URLs to get the code> ,
		"protocols": <string array. the PIDs of protocos this code applied,",
		"owner": <string. address of the publisher>,
		"birthTxid": <string. the txid in which the code was published>,
		"birthTime": <long. the timestamp of the block in which the code was published> ,
		"birthHeight": <long. the height of the block in which the code was published>,
		"lastTxid": <string. the txid in which the code was operated most recently except rating>,
		"lastTime": <long. the timestamp of the block in which the code was operated most recently except rating>,
		"lastHeight": <long. the height of the block in which the code was operated most recently except rating>,
		"tCdd": <long. total cdd all ratings on this code consumed>,
		"tRate": <float. the final rating score of the code>,
		"active": <boolean. is this code active now>,
		"closed": <boolean. is this code has been closed>
	}
]
```
### 示例

  - request body
  
```json
{
	"url": "http://localhost:8080/APIP/apip5/v1/codeSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "part":{
                "fields":["name"],
                "value":"Free"
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
    "balance": 35580728799,
    "got": 1,
    "total": 1,
    "bestHeight": 1736864,
    "data": [
        {
            "codeId": "b8e1a19eadb3f7a639bddf360fe253226439c32197c81242b69a1ca390f87151",
            "name": "FreeChain",
            "ver": "2",
            "desc": "The code to parse basic information from the freecash blockchain.",
            "langs": [
                "java"
            ],
            "urls": [
                "https://github.com/nobodyoffc/FreeChain.git"
            ],
            "protocols": [],
            "owner": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
            "birthTime": 1672214126,
            "birthHeight": 1553774,
            "lastTxid": "7a04b3a4d13a1d2a2ffa733706e5e79e18d34b4a295e5eae6f5184d89597cf88",
            "lastTime": 1672223796,
            "lastHeight": 1553943,
            "tCdd": 3985,
            "tRate": 4.0,
            "active": true,
            "closed": false
        }
    ],
    "last": [
        "1",
        "4.0",
        "b8e1a19eadb3f7a639bddf360fe253226439c32197c81242b69a1ca390f87151"
    ]
}
```
## codeOpHistory

### 说明
获取code的操作记录，不包括rate记录。
  
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
		"codeId": <string. The codeId of the code>,
		"op": <string. operation>,
		"name": <string. name of the code>,
		"ver": <string. version number of the code>,
		"desc": <string. description of the code>,
		"waiters": <string array. fch addresses of the waiters>,
		"langs": <string array. The languages of the code formatted with i18n>,
		"urls": <string. URLs to get the code> ,
		"protocols": <string array. the PIDs of protocos this code applied,",
		"owner": <string. address of the publisher>,
	}
]

```
### 示例
  - request body
```json
{
	"url": "http://localhost:8080/APIP/apip5/v1/codeOpHistory",
	"time": 1677673821267,
	"nonce": 1987697,
	"fcdsl": {
		"query":{
		    "terms":{
		        "fields":["codeId"],
		        "values":["b8e1a19eadb3f7a639bddf360fe253226439c32197c81242b69a1ca390f87151"]
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
    "balance": 35580528799,
    "got": 1,
    "total": 4,
    "bestHeight": 1736866,
    "data": [
        {
            "txId": "8eaadadd85ad7b3a351aea8535e7e2255024b012c60bc6239a159ee2b77b0fcc",
            "height": 1553928,
            "index": 2,
            "time": 1672223075,
            "signer": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
            "codeId": "b8e1a19eadb3f7a639bddf360fe253226439c32197c81242b69a1ca390f87151",
            "op": "recover"
        }
    ],
    "last": [
        "1553928",
        "2"
    ]
}
```


## codeRateHistory

### 说明

获取code的rate历史记录。
  
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
		"codeId": <string. codeId of rated code>,
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
	"url": "http://localhost:8080/APIP/apip5/v1/codeRateHistory",
	"time": 1677673821267,
	"nonce": 1987697,
	"fcdsl": {
		"query":{
		    "terms":{
		        "fields":["codeId"],
		        "values":["b8e1a19eadb3f7a639bddf360fe253226439c32197c81242b69a1ca390f87151"]
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
    "balance": 35580328799,
    "got": 1,
    "total": 1,
    "bestHeight": 1736868,
    "data": [
        {
            "txId": "7a04b3a4d13a1d2a2ffa733706e5e79e18d34b4a295e5eae6f5184d89597cf88",
            "height": 1553943,
            "index": 1,
            "time": 1672223796,
            "signer": "FJeRnehc2VvRMV3wRF2AtiXDVnaKnmnZhp",
            "codeId": "b8e1a19eadb3f7a639bddf360fe253226439c32197c81242b69a1ca390f87151",
            "op": "rate",
            "rate": 4,
            "cdd": 3985
        }
    ],
    "last": [
        "1553943",
        "1"
    ]
}
```