```
Type:APIP
SerialNumber:9
ProtocolName:Team
Version:1
Description:定义群信息查询接口。
Author:C_armX
Language:zh-CN
CreateDate: 2021-12-03
UpdateDate: 2023-05-07
```

# APIP8V1_Team(zh-CN)

---
## 目录

[关于APIP](#关于APIP)

[主要共识](#主要共识)

[TeamByIds](#TeamByIds)

[TeamSearch](#TeamSearch)

[TeamOpHistory](#TeamOpHistory)

[TeamRateHistory](#TeamRateHistory)

[TeamMembers](#TeamMembers)

[TeamExMembers](#TeamExMembers)

[TeamOtherPersons](#TeamOtherPersons)

[MyTeams](#MyTeams)
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

1. 本协议接口提供链上注册的团队（team）的相关信息。

2. 本协议接口的数据来源遵循以下协议: 

   - 《FEIP18_Team》

3. 本协议接口数据采用JSON格式。

4. 本协议所有接口采用POST方法。

5. 本协议的接口的`urlTail`为: `apip8/v1/<接口名称>`。

6. 各接口具体共识如下: 

---

## teamByIds
### 说明

获取指定tid列表的team详情列表。
  
### 默认排序

  - 无排序

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "ids":<string array. tid数组. 必填>
    }
}
```
### data in response body
  
```
{
    <tid>:{
		"tid": <string. the tid of the team>,
		"stdName": <string. the name of the team in English>,
		"localName": <string array. team names in different languages>,
		"desc": <string. description of the team>,
		"owner": <string. address of the publisher>,
		"consensusId":<string. the sha256x2 hash of the team consensus>,
		"managers": <string array. addresses of all the team managers>,
		"memberNum": <long. the number of members of this team> ,
		"birthTime": <long. the timestamp of the block in which the team was published> ,
		"birthHeight": <long. the height of the block in which the team was published>,
		"lastTxid": <string. the txid in which the team was operated most recently except rating>,
		"lastTime": <long. the timestamp of the block in which the team was operated most recently except rating>,
		"lastHeight": <long. the height of the block in which the team was operated most recently except rating>,
		"tRate": <float. the final rating score of the team>,
		"tCdd": <long. total cdd all ratings on this team consumed>,
		"active": <boolean. is this team active now>
	},
    ...
}
```
### 示例

  - request body
  
```json
{
	"url": "http://localhost:8080/APIP/apip9/v1/teamByIds",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "ids":["317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d"]
    }
}
```

  - response body
  
```json
{
    "code": 0,
    "message": "Success.",
    "nonce": 1987697,
    "balance": 35564228799,
    "got": 1,
    "total": 1,
    "bestHeight": 1743577,
    "data": {
        "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d": {
            "tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d",
            "owner": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
            "stdName": "test update",
            "localNames": [
                "测试",
                "テスト"
            ],
            "consensusId": "371f7f3ec56330109962f9fb1220fa836ebe89f07ed38515391376ea8e90a1b4",
            "desc": "New description for the test Team",
            "memberNum": 1,
            "managers": [
                "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"
            ],
            "birthTime": 1684050469,
            "birthHeight": 1746167,
            "lastTxid": "8740afd92e6f1095155e7573093eea20bc236b26c9ee217a16420892ba322bc8",
            "lastTime": 1684055810,
            "lastHeight": 1746274,
            "tCdd": 1000,
            "tRate": 4.0,
            "active": true
        }
    }
}
```

## teamSearch
### 说明

对team信息响应数据data内的各项进行fcdsl查询。
  
### 默认排序

  - active: desc
  - tRate: desc
  - tid: asc

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
		"tid": <string. the tid of the team>,
		"stdName": <string. the name of the team in English>,
		"localName": <string array. team names in different languages>,
		"desc": <string. description of the team>,
		"owner": <string. address of the publisher>,
		"consensusId":<string. the sha256x2 hash of the team consensus>,
		"managers": <string array. addresses of all the team managers>,
		"memberNum": <long. the number of members of this team> ,
		"birthTime": <long. the timestamp of the block in which the team was published> ,
		"birthHeight": <long. the height of the block in which the team was published>,
		"lastTxid": <string. the txid in which the team was operated most recently except rating>,
		"lastTime": <long. the timestamp of the block in which the team was operated most recently except rating>,
		"lastHeight": <long. the height of the block in which the team was operated most recently except rating>,
		"tRate": <float. the final rating score of the team>,
		"tCdd": <long. total cdd all ratings on this team consumed>,
		"active": <boolean. is this team active now>
	}
]
```
### 示例

  - request body
  
```json
{
	"url": "http://localhost:8080/APIP/apip9/v1/teamSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "part":{
                "fields":["stdName"],
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
    "nonce": 1987697,
    "balance": 35564128799,
    "got": 1,
    "total": 1,
    "bestHeight": 1743577,
    "data": [
        {
            "tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d",
            "owner": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
            "stdName": "test update",
            "localNames": [
                "测试",
                "テスト"
            ],
            "consensusId": "371f7f3ec56330109962f9fb1220fa836ebe89f07ed38515391376ea8e90a1b4",
            "desc": "New description for the test Team",
            "memberNum": 1,
            "managers": [
                "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"
            ],
            "birthTime": 1684050469,
            "birthHeight": 1746167,
            "lastTxid": "8740afd92e6f1095155e7573093eea20bc236b26c9ee217a16420892ba322bc8",
            "lastTime": 1684055810,
            "lastHeight": 1746274,
            "tCdd": 1000,
            "tRate": 4.0,
            "active": true
        }
    ],
    "last": [
        "1",
        "4.0",
        "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d"
    ]
}
```
## teamOpHistory

### 说明
获取team的操作历史记录，rate除外。
  
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
		"id": <string. txid where the record is located>,
		"height": <long. block height>,
		"index": <int. index of the transaction in the block>,
		"time": <long. block timestamp>,
		"signer": <the signer of this transaction>,
		"tid": <string. The tid of the team>,
		"op": <string. operation>,
		"stdName": <string. the name of the team in English>,
		"localName": <string array. team names in different languages>,
		"desc": <string. description of the team>,
		"consensusId":<string. the sha256x2 hash of the team consensus>,		"members": <string array. all address of members of this team>,
		"list":<string array. address list>,
		"transferee": <the address of the transferee>,
		"rate": <int. rating score from 0 to 5>,
	}
]

```
### 示例
  - request body
```json
{
   "url": "http://localhost:8080/APIP/apip9/v1/teamOpHistory",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl": {
      "query":{
         "terms":{
            "fields":["tid"],
            "values":["317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d"]
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
   "balance": 35524928799,
   "got": 1,
   "total": 18,
   "bestHeight": 1769569,
   "data": [
      {
         "txId": "188a79cba31c38dfe2e1824845eaa6f09bd39d8e2b1b95c1cd6d87c73ba7827b",
         "height": 1747008,
         "index": 1,
         "time": 1684111729,
         "signer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
         "cdd": 0,
         "tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d",
         "op": "transfer",
         "transferee": "F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW",
         "rate": 0
      }
   ],
   "last": [
      "1747008",
      "1"
   ]
}
```

## teamRateHistory

### 说明

获取team的rate历史记录。
  
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
		...,
        "filter":{
          "terms":{
            "fields":["op"],
            "values":["rate"]
          }
		},
		...
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
   "url": "http://localhost:8080/APIP/apip9/v1/teamRateHistory",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl": {
      "query":{
         "terms":{
            "fields":["tid"],
            "values":["317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d"]
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
   "balance": 35525028799,
   "got": 1,
   "total": 1,
   "bestHeight": 1769567,
   "data": [
      {
         "txId": "a2a9f1a6dd66de8f244eb8fd7754e6541a4d5a4d28387d7035078b0c8616a5ed",
         "height": 1746257,
         "index": 1,
         "time": 1684055158,
         "signer": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
         "cdd": 1000,
         "tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d",
         "op": "rate",
         "rate": 4
      }
   ],
   "last": [
      "1746257",
      "1"
   ]
}
```

## teamMembers

### 说明

获取指定tid的members。
  
### 默认排序

  无

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "ids":<string array. tid数组，长度1. 必填>
    }
}
```
### data in response body
```
[
	{
		<tid>:[
			<addresses of members>
		]
	}
]

```
### 示例
  - request body
```json
{
	"url": "http://localhost:8080/APIP/apip9/v1/teamMembers",
	"time": 1677673821267,
	"nonce": 1987697,
	"fcdsl": {
		"ids":["317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d"]
	}
}
```
  - response body
```json
{
    "code": 0,
    "message": "Success.",
    "nonce": 1987697,
    "balance": 35563828799,
    "got": 1,
    "total": 1,
    "bestHeight": 1743577,
    "data": {
        "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d": [
            "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"
        ]
    }
}
```

## teamExMembers

### 说明

获取指定tid的members。
  
### 默认排序

  无

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "ids":<string array. tid数组，长度1. 必填>
    }
}
```
### data in response body
```
[
	{
		<tid>:[
			<addresses of members>
		]
	}
]

```
### 示例
  - request body
```json
{
	"url": "http://localhost:8080/APIP/apip9/v1/teamExMembers",
	"time": 1677673821267,
	"nonce": 1987697,
	"fcdsl": {
		"ids":["317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d"]
	}
}
```
  - response body
```json
{
    "code": 0,
    "message": "Success.",
    "nonce": 1987697,
    "balance": 35563728799,
    "got": 1,
    "total": 1,
    "bestHeight": 1743577,
    "data": {
        "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d": [
            "F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW",
            "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv"
        ]
    }
}
```

## teamOtherPersons

### 说明

获取指定tid的transferee、notAgreeMembers、invitees信息。
  
### 默认排序

  无

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "ids":<string array. tid数组，长度1. 必填>
    }
}
```
### data in response body
```

{
	"tid": <string. The tid of the team>,
	"transferee": <the address of the transferee>,
	"invitees":<string array. the addresses of invitees>,
	"notAgreeMembers":<string array. the addresses of members who do not agree with the new consensus yet.>
}


```
### 示例
  - request body
```json
{
	"url": "http://localhost:8080/APIP/apip9/v1/teamOtherPersons",
	"time": 1677673821267,
	"nonce": 1987697,
	"fcdsl": {
		"ids":["317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d"]
	}
}
```
  - response body
```json
{
    "code": 0,
    "message": "Success.",
    "nonce": 1987697,
    "balance": 35563628799,
    "got": 1,
    "total": 3,
    "bestHeight": 1743577,
    "data": {
		"tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d",
        "transferee": "F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW",
		"invitees": [
		    "F9pcRpps3T2iHuNGzU3k5b2kWKMRukZP1U"
		],
		"notAgreeMembers": [
		    "F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW"
		]
    }
}
```

## myTeams

### 说明

查询某fid所在的所有team详情列表。
  
### 默认排序

  - lastHeight: desc
  - tid: asc

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
		"tid": <string. the tid of the team>,
		"stdName": <string. the name of the team in English>,
		"desc": <string. description of the team>,
		"memberNum": <long. the number of members of this team> ,
	}
]
```
### 示例

  - request body
  
```json
{
	"url": "http://localhost:8080/APIP/apip9/v1/myTeams",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "terms":{
                "fields":["members"],
                "values":["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"]
            }
        },
        "sort":[{
            "field":"lastHeight",
            "order":"asc"
        }],
        "size":3
    }
}
```

  - response body
  
```json
{
    "code": 0,
    "message": "Success.",
    "nonce": 1987697,
    "balance": 35563328799,
    "got": 2,
    "total": 2,
    "bestHeight": 1743577,
    "data": [
        {
            "tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d",
            "stdName": "test update",
            "desc": "New description for the test Team",
            "memberNum": 1
        },
        {
            "tid": "e2703f9d8c0b36ec37af5a02f5f9733083d7471c9e26a952cf86f34646bce6f2",
            "stdName": "for disband",
            "desc": "This is a test Team",
            "memberNum": 1
        }
    ],
    "last": [
        "1746290"
    ]
}
```