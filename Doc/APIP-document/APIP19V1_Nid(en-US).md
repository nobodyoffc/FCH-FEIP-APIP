```
Type:APIP
SerialNumber:10
ProtocolName:Nid
Version:1
Description:定义链上容器查询接口。
Author:C_armX
Language:zh-CN
CreateDate: 2021-12-01
UpdateDate: 2023-05-23
```

# APIP10V1_Nid(zh-CN)

---
## 目录

[关于APIP](#关于APIP)

[主要共识](#主要共识)

[nidByIds](#nidByIds)

[nidSearch](#nidSearch)
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

1. 本协议接口提供链上的数据命名（nid）。

2. 本协议接口的数据来源遵循以下协议:

    - 《FEIP11_Nid》

3. 本协议接口数据采用JSON格式。

4. 本协议所有接口采用POST方法。

5. 本协议的接口的`urlTail`为: `apip10/v1/<接口名称>`。

6. 各接口具体共识如下:

---

## nidByIds

### 说明

获取指定bid列表的链上容器列表。

### 默认排序

- 无

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "nameIds":<string array. bid数组. 必填>
    }
}
```
### data in response body

```
{
    <nameId>:{
        "nameId": <string. sha256x2("name+owner").>,
        "name": <string. the name of the nid.>,
		"desc": <string. the description of the nid.>,
		"objectId": <string. the ID of the object being named.>,
        "namer": <string. the fid of the namer of this nid.>
        "birthTime": <long. the timestamp of the block in which the group was published> ,
        "birthHeight": <long. the height of the block in which the group was published>,
        "lastTxid": <string. the txid in which the nid was operated most recently except rating>,
        "lastTime": <long. the timestamp of the block in which the nid was operated most recently except rating>,
        "lastHeight": <long. the height of the block in which the nid was operated most recently except rating>,
        "active": <boolean. is this nid active now>
	}
}
```
### 示例

- request body

```json
{
   "url": "http://localhost:8080/APIP/apip10/v1/nidByIds",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "nameIds":["d421396c901111fda8442473ad03819e3c7a3e56cf63f1b2c3cb2a480ea3917d"]
   }
}
```

- response body

```json
{
   "code": 0,
   "message": "Success.",
   "nonce": 1987697,
   "balance": 35524828799,
   "got": 1,
   "total": 1,
   "bestHeight": 1769573,
   "data": {
      "d421396c901111fda8442473ad03819e3c7a3e56cf63f1b2c3cb2a480ea3917d": {
         "bid": "d421396c901111fda8442473ad03819e3c7a3e56cf63f1b2c3cb2a480ea3917d",
         "name": "my nid",
         "desc": "my updated test nid",
         "contain": "[{\"FEIP1V7\":\"ccdd9dc1aff5ba8a396101c93a9191900f240e8e7ec214c3ffc54e6c1817da0d\"},{\"FEIP2V1\":\"ede9f8e451bc4e5f1851bba2db36a1a0c77b531471832c4c01af4ad92544f58c\"}]",
         "active": true,
         "namer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
         "birthTime": 1684990019,
         "birthHeight": 1760409,
         "lastTxId": "bd62dd9838e44c5a418e2c25a3a8493f8d19350538777eb79eebba9c33baf0aa",
         "lastTime": 1684990826,
         "lastHeight": 1760422
      }
   }
}
```

## nidSearch
### 说明

搜索链上容器。

### 默认排序

- birthHeight: desc
- bid: asc

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
         <查询语句>
         ...
	}
}
```
### data in response body

```
[
	{
        "nameId": <string. sha256x2("name+owner").>,
        "name": <string. the name of the nid.>,
		"desc": <string. the description of the nid.>,
		"objectId": <string. the ID of the object being named.>,
        "namer": <string. the fid of the namer of this nid.>
        "birthTime": <long. the timestamp of the block in which the group was published> ,
        "birthHeight": <long. the height of the block in which the group was published>,
        "lastTxid": <string. the txid in which the nid was operated most recently except rating>,
        "lastTime": <long. the timestamp of the block in which the nid was operated most recently except rating>,
        "lastHeight": <long. the height of the block in which the nid was operated most recently except rating>,
        "active": <boolean. is this nid active now>
	}
]

```
### 示例

- request body

```json
{
   "url": "http://localhost:8080/APIP/apip10/v1/nidSearch",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "query":{
         "match": {
            "fields": ["name","desc"],
            "value": "test"
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
   "balance": 35524728799,
   "got": 1,
   "total": 2,
   "bestHeight": 1769577,
   "data": [
      {
         "bid": "d421396c901111fda8442473ad03819e3c7a3e56cf63f1b2c3cb2a480ea3917d",
         "name": "my nid",
         "desc": "my updated test nid",
         "contain": "[{\"FEIP1V7\":\"ccdd9dc1aff5ba8a396101c93a9191900f240e8e7ec214c3ffc54e6c1817da0d\"},{\"FEIP2V1\":\"ede9f8e451bc4e5f1851bba2db36a1a0c77b531471832c4c01af4ad92544f58c\"}]",
         "active": true,
         "namer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
         "birthTime": 1684990019,
         "birthHeight": 1760409,
         "lastTxId": "bd62dd9838e44c5a418e2c25a3a8493f8d19350538777eb79eebba9c33baf0aa",
         "lastTime": 1684990826,
         "lastHeight": 1760422
      }
   ],
   "last": [
      "1760422",
      "d421396c901111fda8442473ad03819e3c7a3e56cf63f1b2c3cb2a480ea3917d"
   ]
}
```
