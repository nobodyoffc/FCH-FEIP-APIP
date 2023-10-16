```
Type:APIP
SerialNumber:16
ProtocolName:Proof
Version:1
Description:定义链上凭证查询接口。
Author:C_armX
Language:zh-CN
CreateDate: 2021-12-01
UpdateDate: 2023-05-23
```

# APIP14V1_Proof(zh-CN)

---
## 目录

[关于APIP](#关于APIP)

[主要共识](#主要共识)

[proofByIds](#proofByIds)

[proofSearch](#proofSearch)

[proofHistory](#proofHistory)

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

1. 本协议接口提供链上保存的凭证（proof）。

2. 本协议接口的数据来源遵循以下协议:

    - 《FEIP14_Proof》

3. 本协议接口数据采用JSON格式。

4. 本协议所有接口采用POST方法。

5. 本协议的接口的`urlTail`为: `apip16/v1/<接口名称>`。

6. 各接口具体共识如下:

---

## newCashList

### 说明

获取指定地址列表的新cash列表。

### 默认排序

- 无

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "ids":<string array. proofId数组. 必填>
    }
}
```
### data in response body

```
{
    <proofId>:{
        "proofId": <string. txid where the proof was added.>,
        "title": <string. the title of the proof.>,
        "content": <string. the content of the proof.>,
        "owner": <string. the owner of this proof.>
        "cosignersInvited":<string array. fid of invited cosigners>
        "transferable": <boolean. is this proof transferable.>
        "birthTime": <long. the timestamp of the block in which the group was published> ,
        "birthHeight": <long. the height of the block in which the group was published>,
        "lastTxid": <string. the txid in which the proof was operated most recently except rating>,
        "lastTime": <long. the timestamp of the block in which the proof was operated most recently except rating>,
        "lastHeight": <long. the height of the block in which the proof was operated most recently except rating>,
        "active": <boolean. is this proof active now>,
        "destroyed": <boolean. is this proof destroyed>
	}
}
```
### 示例

- request body

```json
{
   "url": "https://qm.cash/APIP/apip20/v1/newCashByFids",
   "time": 1696220063472,
   "nonce": 14,
   "fcdsl":{
      "other":{
         "op":"subscribe",
         "endpoint":"http://localhost/newCashByFids/endpoint",
         "data":{
            "fids":["FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv","FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX"]
         }
      }
   }
}
```

- response body

```json
{
   "code": 0,
   "message": "Success.",
   "nonce": 14,
   "balance": 96000000,
   "got": 1,
   "total": 1,
   "bestHeight": 1939542,
   "data": "Done. The hookId is: c99316b668f06ee8af092cffdcd402a115351bb6cb12a8297d69fad0746e0a94"
}
```
