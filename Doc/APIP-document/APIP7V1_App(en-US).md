```
Type:APIP
SerialNumber:5
ProtocolName:App
Version:1
Description:定义应用信息查询接口。
Author:C_armX
Language:zh-CN
CreateDate: 2023-04-20
UpdateDate: 2023-05-07
```

# APIP5V1_App(zh-CN)

## 目录

[关于APIP](#关于APIP)

[主要共识](#主要共识)

[AppByIds](#AppByIds)

[AppOpHistory](#AppOpHistory)

[AppRateHistory](#AppRateHistory)

[AppSearch](#AppSearch)

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

1. 本协议接口提供链上注册的各种应用的相关信息。

2. 本协议接口的数据来源遵循以下协议: 

   - 《FEIP15_App》

3. 本协议接口数据采用JSON格式。

4. 本协议所有接口采用POST方法。

5. 本协议的接口的`urlTail`为: `apip7/v1/<接口名称>`。

6. 各接口具体共识如下: 

---

## appByIds
### 说明

获取指定aid列表的app详情列表。
  
### 默认排序

  - 无排序

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "ids":<string array. aid数组. 必填>
    }
}
```
### data in response body
  
```
{
    <aid>:{
		"aid": <string. The aid of the app>,
		"stdName": <string. the name of the app in English>,
		"localName": <string array. app names in different languages>,
		"desc": <string. description of the app>,
		"types": <string arrar. The types of the app>,
		"urls": <string. URLs to get the app> ,
		"downloads":{
			"os":<string. the name of OS>,
			"link": <string. the link to download installing file>,
			"did": <string. the did to download installing file>
		},
		"waiters": <string array. fch addresses of the waiters>,		
		"protocols": <string array. the PIDs of protocols this app applied,",	
		"apps": <string array. the SIDs of apps this app applied,",	
		"codes": <string array. the CodeIDs of codes this app applied,",	
		"owner": <string. address of the publisher>,
		"birthTxid": <string. the txid in which the app was published>,
		"birthTime": <long. the timestamp of the block in which the app was published> ,
		"birthHeight": <long. the height of the block in which the app was published>,
		"lastTxid": <string. the txid in which the app was operated most recently except rating>,
		"lastTime": <long. the timestamp of the block in which the app was operated most recently except rating>,
		"lastHeight": <long. the height of the block in which the app was operated most recently except rating>,
		"tCdd": <long. total cdd all ratings on this app consumed>,
		"tRate": <float. the final rating score of the app>,
		"active": <boolean. is this app active now>,
		"closed": <boolean. is this app has been closed>
	},
    ...
}

```
### 示例

  - request body
  
```json
{
	"url": "http://localhost:8080/APIP/apip7/v1/appByIds",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "ids":["b7af2d8c32e8c46159af450226065c898d473321b4096a14ca293c0f86888ee2"]
    }
}
```

  - response body
  
```json
{
    "code": 0,
    "message": "Success.",
    "nonce": 1987697,
    "balance": 35579228799,
    "got": 1,
    "total": 0,
    "bestHeight": 1740548,
    "data": [
        {
            "aid": "b7af2d8c32e8c46159af450226065c898d473321b4096a14ca293c0f86888ee2",
            "stdName": "Crypto Signer",
            "localNames": [
                "密签",
                "密の署"
            ],
            "desc": "Save the private key offline and provide offline signature?and provide other functions.",
            "urls": [
                "https://sign.cash"
            ],
            "protocols": [
                "b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553",
                "37406e3e45750efccdb060ca2e748f9f026aebb7dadade8e8747340f380edaca"
            ],
            "services": [
                "c86e039f466434862585e38c0fd1a11f47dcc07839647a452424503b30f81b39",
                "403d3146bdd1edbd8d71b01ffbad75972e07617971acb767a9bae150d4154dc25"
            ],
            "owner": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
            "birthTime": 1672214126,
            "birthHeight": 1553774,
            "lastTxid": "91314953a7625045aaa07abb88940dfe8e89be305502d4441def1f4f84d21c40",
            "lastTime": 1673610464,
            "lastHeight": 1576139,
            "tCdd": 3985,
            "tRate": 4.0,
            "active": false,
            "closed": true
        }
    ]
}
```

## appSearch
### 说明

对app信息响应数据data内的各项进行fcdsl查询。
  
### 默认排序

  - active: desc
  - tRate: desc
  - aid: asc

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
		"aid": <string. The aid of the app>,
		"stdName": <string. the name of the app in English>,
		"localName": <string array. app names in different languages>,
		"desc": <string. description of the app>,
		"types": <string arrar. The types of the app>,
		"urls": <string. URLs to get the app> ,
		"downloads":{
			"os":<string. the name of OS>,
			"link": <string. the link to download installing file>,
			"did": <string. the did to download installing file>
		},
		"waiters": <string array. fch addresses of the waiters>,		
		"protocols": <string array. the PIDs of protocols this app applied,",	
		"apps": <string array. the SIDs of apps this app applied,",	
		"codes": <string array. the CodeIDs of codes this app applied,",	
		"owner": <string. address of the publisher>,
		"birthTxid": <string. the txid in which the app was published>,
		"birthTime": <long. the timestamp of the block in which the app was published> ,
		"birthHeight": <long. the height of the block in which the app was published>,
		"lastTxid": <string. the txid in which the app was operated most recently except rating>,
		"lastTime": <long. the timestamp of the block in which the app was operated most recently except rating>,
		"lastHeight": <long. the height of the block in which the app was operated most recently except rating>,
		"tCdd": <long. total cdd all ratings on this app consumed>,
		"tRate": <float. the final rating score of the app>,
		"active": <boolean. is this app active now>,
		"closed": <boolean. is this app has been closed>
	}
]
```
### 示例

  - request body
  
```json
{
	"url": "http://localhost:8080/APIP/apip7/v1/appSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "part":{
                "fields":["stdName"],
                "value":"Signer"
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
    "balance": 35578528799,
    "got": 1,
    "total": 2,
    "bestHeight": 1740993,
    "data": [
        {
            "aid": "f9286689fa3c46ff3463b7b7e482c4e2ea3f23ca8634276eb4753512ff799873",
            "stdName": "Signer",
            "localNames": [
                "??",
                "????????"
            ],
            "desc": "Save the private key offline and provide offline signature?and provide other functions.",
            "urls": [
                "https://sign.cash"
            ],
            "downloads": [
                {
                    "os": "android",
                    "link": "https://sign.cash/download/cryptosigner"
                }
            ],
            "protocols": [
                "b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553",
                "37406e3e45750efccdb060ca2e748f9f026aebb7dadade8e8747340f380edaca"
            ],
            "services": [
                "c86e039f466434862585e38c0fd1a11f47dcc07839647a452424503b30f81b39",
                "403d3146bdd1edbd8d71b01ffbad75972e07617971acb767a9bae150d4154dc25"
            ],
            "owner": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
            "birthTime": 1673847910,
            "birthHeight": 1580005,
            "lastTxid": "f9286689fa3c46ff3463b7b7e482c4e2ea3f23ca8634276eb4753512ff799873",
            "lastTime": 1673847910,
            "lastHeight": 1580005,
            "tCdd": 0,
            "tRate": 0.0,
            "active": true,
            "closed": false
        }
    ],
    "last": [
        "1",
        "0.0",
        "f9286689fa3c46ff3463b7b7e482c4e2ea3f23ca8634276eb4753512ff799873"
    ]
}
```
## appOpHistory

### 说明
获取App操作记录，不包括rate记录。
  
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
		"aid": <string. The aid of the app>,
		"op": <string. operation>,
		"stdName": <string. the name of the app in English>,
		"localName": <string array. app names in different languages>,
		"desc": <string. description of the app>,
		"types": <string arrar. The types of the app>,
		"urls": <string. URLs to get the app> ,
		"downloads":{
			"os":<string. the name of OS>,
			"link": <string. the link to download installing file>,
			"did": <string. the did to download installing file>
		},
		"waiters": <string array. fch addresses of the waiters>,		
		"protocols": <string array. the PIDs of protocols this app applied,",	
		"apps": <string array. the SIDs of apps this app applied,",	
		"codes": <string array. the CodeIDs of codes this app applied,"
	}
]

```
### 示例
  - request body
```json
{
	"url": "http://localhost:8080/APIP/apip7/v1/appOpHistory",
	"time": 1677673821267,
	"nonce": 1987697,
	"fcdsl": {
		"query":{
		    "terms":{
		        "fields":["aid"],
		        "values":["b7af2d8c32e8c46159af450226065c898d473321b4096a14ca293c0f86888ee2"]
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
    "balance": 35578628799,
    "got": 5,
    "total": 5,
    "bestHeight": 1740993,
    "data": [
        {
            "txId": "b7af2d8c32e8c46159af450226065c898d473321b4096a14ca293c0f86888ee2",
            "height": 1553774,
            "index": 2,
            "time": 1672214126,
            "signer": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
            "stdName": "Sign Cash",
            "localNames": [
                "??",
                "????????"
            ],
            "desc": "Save the private key offline and provide offline signature?and provide other functions.",
            "types": [
                "signer"
            ],
            "urls": [
                "https://sign.cash/download/cryptosigner"
            ],
            "protocols": [
                "b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553",
                "37406e3e45750efccdb060ca2e748f9f026aebb7dadade8e8747340f380edaca"
            ],
            "services": [
                "c86e039f466434862585e38c0fd1a11f47dcc07839647a452424503b30f81b39",
                "403d3146bdd1edbd8d71b01ffbad75972e07617971acb767a9bae150d4154dc25"
            ],
            "aid": "b7af2d8c32e8c46159af450226065c898d473321b4096a14ca293c0f86888ee2",
            "op": "publish"
        }
    ],
    "last": [
        "1553774",
        "2"
    ]
}
```


## appRateHistory

### 说明

获取App的rate记录。
  
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
		"aid": <string. aid of rated app>,
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
	"url": "http://localhost:8080/APIP/apip7/v1/appRateHistory",
	"time": 1677673821267,
	"nonce": 1987697,
	"fcdsl": {
		"query":{
		    "terms":{
		        "fields":["aid"],
		        "values":["b7af2d8c32e8c46159af450226065c898d473321b4096a14ca293c0f86888ee2"]
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
    "balance": 35578428799,
    "got": 1,
    "total": 1,
    "bestHeight": 1740994,
    "data": [
        {
            "txId": "be993c52c3fed10841654168ecd835b08432f4d21b00bcb030cc8339a30a3990",
            "height": 1553942,
            "index": 2,
            "time": 1672223683,
            "signer": "FJeRnehc2VvRMV3wRF2AtiXDVnaKnmnZhp",
            "aid": "b7af2d8c32e8c46159af450226065c898d473321b4096a14ca293c0f86888ee2",
            "op": "rate",
            "rate": 4,
            "cdd": 3985
        }
    ],
    "last": [
        "1553942",
        "2"
    ]
}
```