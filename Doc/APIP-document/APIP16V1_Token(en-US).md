```
Type:APIP
SerialNumber:16
ProtocolName:Token
Version:1
Description:定义链上凭证查询接口。
Author:C_armX
Language:zh-CN
CreateDate: 2021-12-01
UpdateDate: 2023-05-23
```

# APIP16V1_Token(zh-CN)

---
## 目录

[关于APIP](#关于APIP)

[主要共识](#主要共识)

[tokenByIds](#tokenByIds)

[tokenSearch](#tokenSearch)

[tokenHistory](#tokenHistory)

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

1. 本协议接口提供链上保存的凭证（token）。

2. 本协议接口的数据来源遵循以下协议:

    - 《FEIP14_Token》

3. 本协议接口数据采用JSON格式。

4. 本协议所有接口采用POST方法。

5. 本协议的接口的`urlTail`为: `apip16/v1/<接口名称>`。

6. 各接口具体共识如下:

---

## tokenByIds

### 说明

获取指定tokenId列表的链上凭证列表。

### 默认排序

- 无

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "ids":<string array. tokenId数组. 必填>
    }
}
```
### data in response body

```
{
    <tokenId>:{
        "tokenId": <string. txid where the token was added.>,
        "title": <string. the title of the token.>,
        "content": <string. the content of the token.>,
        "owner": <string. the owner of this token.>
        "cosignersInvited":<string array. fid of invited cosigners>
        "transferable": <boolean. is this token transferable.>
        "birthTime": <long. the timestamp of the block in which the group was published> ,
        "birthHeight": <long. the height of the block in which the group was published>,
        "lastTxid": <string. the txid in which the token was operated most recently except rating>,
        "lastTime": <long. the timestamp of the block in which the token was operated most recently except rating>,
        "lastHeight": <long. the height of the block in which the token was operated most recently except rating>,
        "active": <boolean. is this token active now>,
        "destroyed": <boolean. is this token destroyed>
	}
}
```
### 示例

- request body

```json
{
   "url": "http://localhost:8080/APIP/apip16/v1/tokenByIds",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "ids":["cc93ae0447a679a5cac85a3796b4cde5905e3953da3e0ff8957c253268d0c2f8"]
   }
}
```

- response body

```json
{
   "code": 0,
   "message": "Success.",
   "nonce": 1987697,
   "balance": 35541128799,
   "got": 1,
   "total": 1,
   "bestHeight": 1760303,
   "data": {
      "cc93ae0447a679a5cac85a3796b4cde5905e3953da3e0ff8957c253268d0c2f8": {
         "tokenId": "cc93ae0447a679a5cac85a3796b4cde5905e3953da3e0ff8957c253268d0c2f8",
         "title": "借据",
         "content": "1. 此为借款证明；2. 发行者对本证明持有者负有还款责任，3. 其他签署者为本证明的担保人，在发行者不能履行还款责任时，代为还款，还款后成为该笔款项的债权人。4. 借款金额为￥20000元；5. 借款年利率为4%；6. 借款期限为1年；7. 此证明持有者可在到期后获得本息总计￥20800元；8. 此证明可转让。",
         "cosignersInvited": [
            "F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW",
            "F9pcRpps3T2iHuNGzU3k5b2kWKMRukZP1U"
         ],
         "transferable": true,
         "active": false,
         "destroyed": true,
         "issuer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
         "owner": "F9pcRpps3T2iHuNGzU3k5b2kWKMRukZP1U",
         "birthTime": 1684982666,
         "birthHeight": 1760277,
         "lastTxid": "cc93ae0447a679a5cac85a3796b4cde5905e3953da3e0ff8957c253268d0c2f8",
         "lastTime": 1684982666,
         "lastHeight": 1760277
      }
   }
}
```

## tokenSearch
### 说明

搜索链上凭证。

### 默认排序

- birthHeight: desc
- tokenId: asc

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
        "tokenId": <string. txid where the token was added.>,
        "title": <string. the title of the token.>,
        "content": <string. the content of the token.>,
        "owner": <string. the owner of this token.>
        "cosignersInvited":<string array. fid of invited cosigners>
        "transferable": <boolean. is this token transferable.>
        "birthTime": <long. the timestamp of the block in which the group was published> ,
        "birthHeight": <long. the height of the block in which the group was published>,
        "lastTxid": <string. the txid in which the token was operated most recently except rating>,
        "lastTime": <long. the timestamp of the block in which the token was operated most recently except rating>,
        "lastHeight": <long. the height of the block in which the token was operated most recently except rating>,
        "active": <boolean. is this token active now>,
        "destroyed": <boolean. is this token destroyed>
	}
]

```
### 示例

- request body

```json
{
   "url": "http://localhost:8080/APIP/apip14/v1/tokenSearch",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "query":{
         "match": {
            "fields": ["content","title"],
            "value": "借据"
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
   "balance": 35540928799,
   "got": 1,
   "total": 6,
   "bestHeight": 1760304,
   "data": [
      {
         "tokenId": "cc93ae0447a679a5cac85a3796b4cde5905e3953da3e0ff8957c253268d0c2f8",
         "title": "借据",
         "content": "1. 此为借款证明；2. 发行者对本证明持有者负有还款责任，3. 其他签署者为本证明的担保人，在发行者不能履行还款责任时，代为还款，还款后成为该笔款项的债权人。4. 借款金额为￥20000元；5. 借款年利率为4%；6. 借款期限为1年；7. 此证明持有者可在到期后获得本息总计￥20800元；8. 此证明可转让。",
         "cosignersInvited": [
            "F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW",
            "F9pcRpps3T2iHuNGzU3k5b2kWKMRukZP1U"
         ],
         "transferable": true,
         "active": false,
         "destroyed": true,
         "issuer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
         "owner": "F9pcRpps3T2iHuNGzU3k5b2kWKMRukZP1U",
         "birthTime": 1684982666,
         "birthHeight": 1760277,
         "lastTxid": "cc93ae0447a679a5cac85a3796b4cde5905e3953da3e0ff8957c253268d0c2f8",
         "lastTime": 1684982666,
         "lastHeight": 1760277
      }
   ],
   "last": [
      "1760277",
      "cc93ae0447a679a5cac85a3796b4cde5905e3953da3e0ff8957c253268d0c2f8"
   ]
}
```

## tokenHistory

### 说明

搜索给定tokenId的链上凭证操作历史。

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
         <查询语句>
         ...
	}
}
```
### data in response body

```
[
	{
        "id": <string. txid where the token was added.>,
		"height": <long. block height>,
		"index": <int. index of the transaction in the block>,
		"time": <long. block timestamp>,
		"signer": <string. fid of the first input of the tx>,
		"tokenid": <string. tokenId of the token>,
		"op": <string. operation>,
		"title": <string. the title of the token.>,
        "content": <string. the content of the token.>,
        "cosigners":<string array. fid of invited cosigners>,
        "transferable": <boolean. is this token transferable.>,
        "allSignsRequired": <boolean. is all signatures of cosigners required to active the token.>
	}
]
```
### 示例

- request body

```json
{
   "url": "http://localhost:8080/APIP/apip16/v1/tokenHistory",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl": {
      "query":{
         "terms":{
            "fields":["tokenId"],
            "values":["cc93ae0447a679a5cac85a3796b4cde5905e3953da3e0ff8957c253268d0c2f8"]
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
   "balance": 35540828799,
   "got": 1,
   "total": 3,
   "bestHeight": 1760305,
   "data": [
      {
         "id": "4aa4645d584f5cb46c1c0a6b69f44b0ccf8fb6c5601e50f4e52d882d60ccc629",
         "height": 1760288,
         "index": 1,
         "time": 1684983233,
         "signer": "F9pcRpps3T2iHuNGzU3k5b2kWKMRukZP1U",
         "tokenId": "cc93ae0447a679a5cac85a3796b4cde5905e3953da3e0ff8957c253268d0c2f8",
         "op": "destroy",
         "transferable": false,
         "allSignsRequired": false
      }
   ],
   "last": [
      "1760288",
      "1"
   ]
}
```
## tokenHolderByIds
## myTokens
## tokenHolders