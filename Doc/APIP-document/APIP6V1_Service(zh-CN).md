```
Type: APIP
SerialNumber: 6
ProtocolName: Service
Version: 1
Description: 定义协议信息查询接口。
Author: C_armX
Language: zh-CN
CreateDate: 2023-04-20
UpdateDate: 2023-05-07
```

# APIP6V1_Service(zh-CN)

## 目录

[关于APIP](#关于APIP)

[主要共识](#主要共识)

[serviceByIds](#serviceByIds)

[serviceOpHistory](#serviceOpHistory)

[serviceRateHistory](#serviceRateHistory)

[serviceSearch](#serviceSearch)


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

2. 本协议接口的数据来源遵循以下协议：

   - 《FEIP5_Service》

3. 本协议接口数据采用JSON格式。

4. 本协议所有接口采用POST方法。

5. 本协议的接口的`urlTail`为: `apip6/v1/<接口名称>`。

6. 各接口具体共识如下：

---

## serviceByIds
### 说明

获取指定sid列表的service详情列表。
  
### 默认排序

  - 无排序

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "ids":<string array. sid数组. 必填>
    }
}
```
### data in response body
  
```
{
    <sid>:{
		"sid": <string. The sid of the service>,
		"stdName": <string. the name of the service in English>,
		"localName": <string array. service names in different languages>,
		"desc": <string. description of the service>,
		"types": <string arrar. The types of the service>,
		"urls": <string. URLs to get the service> ,
		"waiters": <string array. fch addresses of the waiters>,		
		"protocols": <string array. the PIDs of protocols this service applied,",
		"params": <object. Parameters customized by the service provider.>,
		"owner": <string. address of the publisher>,
		"birthTxid": <string. the txid in which the service was published>,
		"birthTime": <long. the timestamp of the block in which the service was published> ,
		"birthHeight": <long. the height of the block in which the service was published>,
		"lastTxid": <string. the txid in which the service was operated most recently except rating>,
		"lastTime": <long. the timestamp of the block in which the service was operated most recently except rating>,
		"lastHeight": <long. the height of the block in which the service was operated most recently except rating>,
		"tCdd": <long. total cdd all ratings on this service consumed>,
		"tRate": <float. the final rating score of the service>,
		"active": <boolean. is this service active now>,
		"closed": <boolean. is this service has been closed>
	},
    ...
}

```
### 示例

  - request body
  
```json
{
	"url": "http://localhost:8080/APIP/apip6/v1/serviceByIds",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "ids":["f656fa9532f8cbdda754365a609c466a7bef102768c0935f9b909aae5289e0a1"]
    }
}
```

  - response body
  
```json
{
    "code": 0,
    "message": "Success.",
    "nonce": 1987697,
    "balance": 35579828799,
    "got": 1,
    "total": 1,
    "bestHeight": 1738162,
    "data": {
        "f656fa9532f8cbdda754365a609c466a7bef102768c0935f9b909aae5289e0a1": {
            "sid": "f656fa9532f8cbdda754365a609c466a7bef102768c0935f9b909aae5289e0a1",
            "stdName": "ApipServer",
            "localNames": [
                "API??"
            ],
            "desc": "Server for the series API of APIP of free consensus.",
            "types": [
                "APIP",
                "FEIP"
            ],
            "urls": [
                "http://cid.cash/service/service.html"
            ],
            "protocols": [
                "b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553"
            ],
            "params": {
                "urlHead": "http://localhost:8080/APIPserver/",
                "currency": "fch",
                "account": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
                "pricePerRequest": "0.001",
                "minPayment": "10.0",
                "sessionDays": 365.0
            },
            "owner": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
            "birthTime": 1682131880,
            "birthHeight": 1715309,
            "lastTxid": "f656fa9532f8cbdda754365a609c466a7bef102768c0935f9b909aae5289e0a1",
            "lastTime": 1682131880,
            "lastHeight": 1715309,
            "tCdd": 0,
            "tRate": 0.0,
            "active": true,
            "closed": false
        }
    }
}
```

## serviceSearch
### 说明

对service信息响应数据data内的各项进行fcdsl查询。
  
### 默认排序

  - active: desc
  - tRate: desc
  - sid: asc

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
		"sid": <string. The sid of the service>,
		"stdName": <string. the name of the service in English>,
		"localName": <string array. service names in different languages>,
		"desc": <string. description of the service>,
		"types": <string arrar. The types of the service>,
		"urls": <string. URLs to get the service> ,
		"waiters": <string array. fch addresses of the waiters>,		
		"protocols": <string array. the PIDs of protocols this service applied,",
		"params": <object. Parameters customized by the service provider.>,
		"owner": <string. address of the publisher>,
		"birthTxid": <string. the txid in which the service was published>,
		"birthTime": <long. the timestamp of the block in which the service was published> ,
		"birthHeight": <long. the height of the block in which the service was published>,
		"lastTxid": <string. the txid in which the service was operated most recently except rating>,
		"lastTime": <long. the timestamp of the block in which the service was operated most recently except rating>,
		"lastHeight": <long. the height of the block in which the service was operated most recently except rating>,
		"tCdd": <long. total cdd all ratings on this service consumed>,
		"tRate": <float. the final rating score of the service>,
		"active": <boolean. is this service active now>,
		"closed": <boolean. is this service has been closed>
	}
]
```
### 示例

  - request body
  
```json
{
	"url": "http://localhost:8080/APIP/apip6/v1/serviceSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "part":{
                "fields":["stdName"],
                "value":"Apip"
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
    "balance": 35579628799,
    "got": 1,
    "total": 1,
    "bestHeight": 1738170,
    "data": [
        {
            "sid": "f656fa9532f8cbdda754365a609c466a7bef102768c0935f9b909aae5289e0a1",
            "stdName": "ApipServer",
            "localNames": [
                "API??"
            ],
            "desc": "Server for the series API of APIP of free consensus.",
            "types": [
                "APIP",
                "FEIP"
            ],
            "urls": [
                "http://cid.cash/service/service.html"
            ],
            "protocols": [
                "b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553"
            ],
            "params": {
                "urlHead": "http://localhost:8080/APIPserver/",
                "currency": "fch",
                "account": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
                "pricePerRequest": "0.001",
                "minPayment": "10.0",
                "sessionDays": "365"
            },
            "owner": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
            "birthTime": 1682131880,
            "birthHeight": 1715309,
            "lastTxid": "f656fa9532f8cbdda754365a609c466a7bef102768c0935f9b909aae5289e0a1",
            "lastTime": 1682131880,
            "lastHeight": 1715309,
            "tCdd": 0,
            "tRate": 0.0,
            "active": true,
            "closed": false
        }
    ],
    "last": [
        "1",
        "0.0",
        "f656fa9532f8cbdda754365a609c466a7bef102768c0935f9b909aae5289e0a1"
    ]
}
```
## serviceOpHistory

### 说明
获取service的操作记录，不包括rate记录。
  
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
		"sid": <string. The sid of the service>,
		"op": <string. operation>,
		"stdName": <string. the name of the service in English>,
		"localName": <string array. service names in different languages>,
		"desc": <string. description of the service>,
		"types": <string arrar. The types of the service>,
		"urls": <string. URLs to get the service> ,
		"waiters": <string array. fch addresses of the waiters>,		
		"protocols": <string array. the PIDs of protocos this service applied,",
		"params": <object. Parameters customized by the service provider.>,
		"owner": <string. address of the publisher>
	}
]

```
### 示例
  - request body
```json
{
	"url": "http://localhost:8080/APIP/apip6/v1/serviceOpHistory",
	"time": 1677673821267,
	"nonce": 1987697,
	"fcdsl": {
		"query":{
		    "terms":{
		        "fields":["sid"],
		        "values":["f656fa9532f8cbdda754365a609c466a7bef102768c0935f9b909aae5289e0a1"]
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
   "balance": 35527928799,
   "got": 1,
   "total": 1,
   "bestHeight": 1769456,
   "data": [
      {
         "txId": "f656fa9532f8cbdda754365a609c466a7bef102768c0935f9b909aae5289e0a1",
         "height": 1715309,
         "index": 1,
         "time": 1682131880,
         "signer": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
         "stdName": "ApipServer",
         "localNames": [
            "API服务"
         ],
         "desc": "Server for the series API of APIP of free consensus.",
         "types": [
            "APIP",
            "FEIP"
         ],
         "urls": [
            "http://cid.cash/service/service.html"
         ],
         "protocols": [
            "b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553"
         ],
         "params": {
            "urlHead": "http://localhost:8080/APIPserver/",
            "currency": "fch",
            "account": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
            "pricePerRequest": "0.001",
            "minPayment": "10.0",
            "sessionDays": 365.0
         },
         "sid": "f656fa9532f8cbdda754365a609c466a7bef102768c0935f9b909aae5289e0a1",
         "op": "publish",
         "rate": 0,
         "cdd": 0
      }
   ],
   "last": [
      "1715309",
      "1"
   ]
}
```


## serviceRateHistory

### 说明

获取service的rate历史记录。
  
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
		"sid": <string. sid of rated service>,
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
   "url": "http://localhost:8080/APIP/apip6/v1/serviceRateHistory",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl": {
      "query":{
         "terms":{
            "fields":["sid"],
            "values":["a4432217ea6b12e4b8095196cf9fe257a0db8e70b4a9acb4b1955d506f8cd1bb"]
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
   "balance": 35526228799,
   "got": 1,
   "total": 1,
   "bestHeight": 1769486,
   "data": [
      {
         "txId": "1154c23b912b7de7213dc2d03871391871290a7fca21138bffe649eec3dfb5be",
         "height": 1768106,
         "index": 1,
         "time": 1685504088,
         "signer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
         "sid": "a4432217ea6b12e4b8095196cf9fe257a0db8e70b4a9acb4b1955d506f8cd1bb",
         "op": "rate",
         "rate": 5,
         "cdd": 152
      }
   ],
   "last": [
      "1768106",
      "1"
   ]
}
```