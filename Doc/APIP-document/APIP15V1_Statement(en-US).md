```
Type:APIP
SerialNumber:15
ProtocolName:Statement
Version:1
Description:定义链上声明查询接口。
Author:C_armX
Language:zh-CN
CreateDate: 2021-12-01
UpdateDate: 2023-05-23
```

# APIP15V1_Statement(zh-CN)

---
## 目录

[关于APIP](#关于APIP)

[主要共识](#主要共识)

[statements](#statements)

[statementSearch](#statementSearch)

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

1. 本协议接口提供链上保存的声明（statement）。

2. 本协议接口的数据来源遵循以下协议:

    - 《FEIP17_Statement》

3. 本协议接口数据采用JSON格式。

4. 本协议所有接口采用POST方法。

5. 本协议的接口的`urlTail`为: `apip15/v1/<接口名称>`。

6. 各接口具体共识如下:

---

## statements

### 说明

获取指定owner的链上声明。

### 默认排序

- birthHeight: desc
- statementId: asc

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "query":{
            "terms": {
                "fields": ["owner"],
                "values":<string array. fid of the owner. 必填>
            }
        }
		...
	}
}
```
### data in response body

```
[
	{
		"statementId": <string. txid where the statement was added.>,
		"title": <string. the title of the statement.>,
		"content": <string. the content of the statement.>,
		"owner": <string. the owner of this statement.>
		"birthTime": <long. the timestamp of the block in which the group was published> ,
		"birthHeight": <long. the height of the block in which the group was published>
	}
]

```
### 示例

- request body

```json
{
   "url": "http://localhost:8080/APIP/apip15/v1/statements",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "query":{
         "terms": {
            "fields": ["owner"],
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
   "balance": 35543928799,
   "got": 1,
   "total": 1,
   "bestHeight": 1757387,
   "data": [
      {
         "statementId": "c08f414a102b16e25782db8a24fdea0f4479f4950ccb527d8ea4b370df0a62c2",
         "title": "About Donation",
         "content": "I accept donations from any one.",
         "owner": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
         "birthTime": 1684802974,
         "birthHeight": 1757385
      }
   ],
   "last": [
      "1757385",
      "c08f414a102b16e25782db8a24fdea0f4479f4950ccb527d8ea4b370df0a62c2"
   ]
}
```

## statementSearch
### 说明

搜索链上声明。

### 默认排序

- birthHeight: desc
- statementId: asc

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
		"statementId": <string. txid where the statement was added.>,
		"title": <string. the title of the statement.>,
		"content": <string. the content of the statement.>,
		"owner": <string. the owner of this statement.>
		"birthTime": <long. the timestamp of the block in which the group was published> ,
		"birthHeight": <long. the height of the block in which the group was published>
	}
]

```
### 示例

- request body

```json
{
   "url": "http://localhost:8080/APIP/apip15/v1/statementSearch",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "query":{
         "match": {
            "fields": ["title","content"],
            "value": "any one"
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
   "balance": 35543428799,
   "got": 1,
   "total": 1,
   "bestHeight": 1757393,
   "data": [
      {
         "statementId": "c08f414a102b16e25782db8a24fdea0f4479f4950ccb527d8ea4b370df0a62c2",
         "title": "About Donation",
         "content": "I accept donations from any one.",
         "owner": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
         "birthTime": 1684802974,
         "birthHeight": 1757385
      }
   ],
   "last": [
      "1757385",
      "c08f414a102b16e25782db8a24fdea0f4479f4950ccb527d8ea4b370df0a62c2"
   ]
}
```
