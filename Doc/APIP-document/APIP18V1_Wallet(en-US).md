```
Type:APIP
SerialNumber:17
ProtocolName:Wallet
Version:1
Description:定义钱包功能接口。
Author:C_armX
Language:zh-CN
CreateDate: 2023-05-10
UpdateDate: 2023-05-23
```

# APIP11V1_RPC(zh-CN)
---
## 目录

[关于APIP](#关于APIP)

[关于本协议](#关于本协议)

[cashValidForCd](#cashValidForCd)

[cashValidForPay](#cashValidForPay)

[cashValidLive](#cashValidLive)

[unconfirmed](#unconfirmed)

[decodeRawTx](#decodeRawTx)

[broadcastTx](#broadcastTx)

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

1. 本协议接口提供链上保存的容器（box）。

2. 本协议接口的数据来源遵循以下协议:

	- 《FEIP13_Box》

3. 本协议接口数据采用JSON格式。

4. 本协议所有接口采用POST方法。

5. 本协议的接口的`urlTail`为: `apip17/v1/<接口名称>`。

6. 各接口具体共识如下:

## cashValidLive

### 说明

获取指定fid的可用cash列表，包括未确认交易。

### 默认排序

- cd: asc
- value: asc
- cashId: asc

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
		<filter 不可用>
	}
}
```
### data in response body

```
[
    {
        "cashId": <string. CashID>,
        "outIndex": <int. Cash出生于交易内的索引号>,
        "type": <string. Cash类型，P2PKH、P2SH、OpReturn等>,
        "fid": <string. 所属FCH地址>,
        "value": <long. 金额，单位聪>,
        "lockScript": <string. 锁定脚本>,
        "txId": <string. Cash出生所在交易ID>,
        "txIndex": <int. 出生交易在所在区块内的索引号>,
        "blockId": <string. 出生交易所在区块ID>,
        "birthTime": <long. 出生区块时间戳，单位秒>,
        "birthHeight": <long. 出生区块高度>,
        "spendTime": <long. 花费于区块时间戳>,
        "spendTxId": <string. 花费于交易ID>,
        "spendHeight": <long. 花费于区块高度>,
        "spendIndex": <int. 在花费交易中的输入索引号>,
        "unlockScript": <string. 解锁脚本>,
        "sigHash": <string. 花费签名的sigHash>,
        "sequence": <string. 花费的sequence>,
        "cdd": <long. 花费时的币天销毁量，单位cd>,
        "cd": <long. 未花费积累的币天量，单位cd>,
        "valid": <boolean，是否可用（是否尚未被花费）>
    }
]

```
### 示例

- request body

```json
{
	"url": "http://localhost:8080/APIP/apip18/v1/cashValidLive",
	"time": 1677673821267,
	"nonce": 1987697,
	"fcdsl":{
		"query":{
			"terms": {
				"fields": ["fid"],
				"values":["FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX"]
			}
		},
		"size":10
	}
}
```

- response body

```json
{
	"code": 0,
	"message": "Success.",
	"nonce": 1987697,
	"balance": 35523028799,
	"got": 1,
	"total": 1581,
	"bestHeight": 1769646,
	"data": [
		{
			"cashId": "31dc5a081eab4f1d0232b5b31c591b0ad3dad4bc1bff3dad8ce8819997b4542e",
			"birthIndex": 0,
			"type": "P2PKH",
			"fid": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX",
			"value": 1000,
			"lockScript": "76a914aca01f3ace616b7172efac34fc2b6127460fe74e88ac",
			"birthTxId": "c9c81e2e14c552c962e5ac9428b6202b9468ebde2a63c4455911ea20d1514980",
			"birthTxIndex": 1,
			"birthBlockId": "0000000000000022ce024e1b3b469557d907bb53211eeb1edc6ff25ff1d67085",
			"birthTime": 1623124521,
			"birthHeight": 751922,
			"spendTime": 0,
			"spendHeight": 0,
			"spendIndex": 0,
			"cdd": 0,
			"cd": 0,
			"valid": true
		}
	],
	"last": [
		"0",
		"1000",
		"31dc5a081eab4f1d0232b5b31c591b0ad3dad4bc1bff3dad8ce8819997b4542e"
	]
}
```


## cashValidForCd

### 说明

从指定fid获取总和大于给定cd值的可用cash列表。

### 默认排序

- 无

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "query":{
            "terms": {
                "fields": ["fid"],
                "values":<string array. fid. 必填>
            }
        },
		"other":<long. 目标 cd 值>
	}
}
```
### data in response body

```
[
    {
        "cashId": <string. CashID>,
        "outIndex": <int. Cash出生于交易内的索引号>,
        "type": <string. Cash类型，P2PKH、P2SH、OpReturn等>,
        "fid": <string. 所属FCH地址>,
        "value": <long. 金额，单位聪>,
        "lockScript": <string. 锁定脚本>,
        "txId": <string. Cash出生所在交易ID>,
        "txIndex": <int. 出生交易在所在区块内的索引号>,
        "blockId": <string. 出生交易所在区块ID>,
        "birthTime": <long. 出生区块时间戳，单位秒>,
        "birthHeight": <long. 出生区块高度>,
        "spendTime": <long. 花费于区块时间戳>,
        "spendTxId": <string. 花费于交易ID>,
        "spendHeight": <long. 花费于区块高度>,
        "spendIndex": <int. 在花费交易中的输入索引号>,
        "unlockScript": <string. 解锁脚本>,
        "sigHash": <string. 花费签名的sigHash>,
        "sequence": <string. 花费的sequence>,
        "cdd": <long. 花费时的币天销毁量，单位cd>,
        "cd": <long. 未花费积累的币天量，单位cd>,
        "valid": <boolean，是否可用（是否尚未被花费）>
    }
]

```
### 示例

- request body

```json
{
	"url": "http://localhost:8080/APIP/apip18/v1/cashValidForCd",
	"time": 1677673821267,
	"nonce": 1987697,
	"fcdsl":{
		"query":{
			"terms": {
				"fields": ["fid"],
				"values":["FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX"]
			}
		},
		"other":"2",
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
	"balance": 35522428799,
	"got": 1,
	"total": 1552,
	"bestHeight": 1769656,
	"data": [
		{
			"cashId": "059ab66111cd72af31b2427a1335e77c65ca74d4065fc7d397eacd59fcc18eb7",
			"birthIndex": 18,
			"type": "P2PKH",
			"fid": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX",
			"value": 999176,
			"lockScript": "76a914aca01f3ace616b7172efac34fc2b6127460fe74e88ac",
			"birthTxId": "f0eadc2d3e5f21a0666786ff9eb23a2af168a32f651cdf2826585283d666a0d6",
			"birthTxIndex": 2,
			"birthBlockId": "000000000000001f273dbee9908a7dda2b49624065739dfce3f80947acd11866",
			"birthTime": 1618302858,
			"birthHeight": 671935,
			"spendTime": 0,
			"spendHeight": 0,
			"spendIndex": 0,
			"cdd": 0,
			"cd": 7,
			"valid": true
		}
	]
}
```
## cashValidForPay

### 说明

从指定fid，按照最小销毁币天原则，获取总和大于给定value值的可用cash列表。

### 默认排序

- 无

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "query":{
            "terms": {
                "fields": ["fid"],
                "values":<string array. fid. 必填>
            }
        },
		"other":<float. 目标 value 值,单位fch>
	}
}
```
### data in response body

```
[
    {
        "cashId": <string. CashID>,
        "outIndex": <int. Cash出生于交易内的索引号>,
        "type": <string. Cash类型，P2PKH、P2SH、OpReturn等>,
        "fid": <string. 所属FCH地址>,
        "value": <long. 金额，单位聪>,
        "lockScript": <string. 锁定脚本>,
        "txId": <string. Cash出生所在交易ID>,
        "txIndex": <int. 出生交易在所在区块内的索引号>,
        "blockId": <string. 出生交易所在区块ID>,
        "birthTime": <long. 出生区块时间戳，单位秒>,
        "birthHeight": <long. 出生区块高度>,
        "spendTime": <long. 花费于区块时间戳>,
        "spendTxId": <string. 花费于交易ID>,
        "spendHeight": <long. 花费于区块高度>,
        "spendIndex": <int. 在花费交易中的输入索引号>,
        "unlockScript": <string. 解锁脚本>,
        "sigHash": <string. 花费签名的sigHash>,
        "sequence": <string. 花费的sequence>,
        "cdd": <long. 花费时的币天销毁量，单位cd>,
        "cd": <long. 未花费积累的币天量，单位cd>,
        "valid": <boolean，是否可用（是否尚未被花费）>
    }
]

```
### 示例

- request body

```json
{
	"url": "http://localhost:8080/APIP/apip18/v1/cashValidForPay",
	"time": 1677673821267,
	"nonce": 1987697,
	"fcdsl":{
		"query":{
			"terms": {
				"fields": ["fid"],
				"values":["FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX"]
			}
		},
		"other":"0.001",
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
	"balance": 35521128799,
	"got": 2,
	"total": 1581,
	"bestHeight": 1769662,
	"data": [
		{
			"cashId": "dbcc5d6a3ee6c40770be423e11927b8dc774b7756d28d19cc0389c41879e067a",
			"birthIndex": 0,
			"type": "P2PKH",
			"fid": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX",
			"value": 100000,
			"lockScript": "76a914aca01f3ace616b7172efac34fc2b6127460fe74e88ac",
			"birthTxId": "a93eec1ad388b90d0394dd1e73b43573b4f060f3be08ef5d31b519d6e38e501f",
			"birthTxIndex": 1,
			"birthBlockId": "00000000000000123b15d126e0c87a1d1508cff8234db00f8fc046957b881c58",
			"birthTime": 1606901314,
			"birthHeight": 482974,
			"spendTime": 0,
			"spendHeight": 0,
			"spendIndex": 0,
			"cdd": 0,
			"cd": 0,
			"valid": true
		},
		{
			"cashId": "4493c0ce1aaf2961d2b963bc7c8e08197af46e8301d7d5adccc21a99145afc54",
			"birthIndex": 0,
			"type": "P2PKH",
			"fid": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX",
			"value": 100000,
			"lockScript": "76a914aca01f3ace616b7172efac34fc2b6127460fe74e88ac",
			"birthTxId": "463a5fb230b2f67d1b177a73f3e1f84d5291536f1744d2af1c1af08240bffa9a",
			"birthTxIndex": 1,
			"birthBlockId": "000000000000000d143be7694819e1021eb0ac81b3d572f78c06bc386aa2bdbe",
			"birthTime": 1607254189,
			"birthHeight": 488782,
			"spendTime": 0,
			"spendHeight": 0,
			"spendIndex": 0,
			"cdd": 0,
			"cd": 0,
			"valid": true
		}
	]
}
```
## uncofirmed

### 说明

获取指定fid列表的未确认交易汇总信息。

### 默认排序

- 无

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
    	"ids":<string array. fid>
	}
}
```
### data in response body

```
[
    {
        "fid": <string. 所属FCH地址>,
         "net": <long. 净变动金额，单位聪>,
         "spendCount": <int. 花费cash个数>,
         "spendValue": <long. 花费金额，单位聪>,
         "incomeCount": <int. 新增cash个数>,
         "incomeValue": <long. 收入金额，单位聪>
    },
    ...
]

```
### 示例

- request body

```json
{
	"url": "http://localhost:8080/APIP/apip18/v1/unconfirmed",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "ids":["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK","FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv"]
    }
}
```
- response body

```json
{
	"code": 0,
	"message": "Success.",
	"nonce": 1987697,
	"balance": 35518428799,
	"got": 2,
	"total": 2,
	"bestHeight": 1770887,
	"data": [
		{
			"fid": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
			"net": -100000360,
			"spendCount": 2,
			"spendValue": 1152129676,
			"incomeCount": 1,
			"incomeValue": 1052129316
		},
		{
			"fid": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
			"net": 100000000,
			"spendCount": 0,
			"spendValue": 0,
			"incomeCount": 1,
			"incomeValue": 100000000
		}
	]
}
```
	
## decodeRawTx

### 说明

对给出的原始交易进行解码。

### 默认排序

- 无

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
    	"other":<string. raw tx in hex>
	}
}
```
### data in response body

```
<string. the json string of the raw tx or an error message from RPC of freecash full node.>

```
### 示例

- request body

```json
{
	"url": "http://localhost:8080/APIP/apip18/v1/decodeRawTx",
	"time": 1677673821267,
	"nonce": 1987697,
	"fcdsl":{
		"other":"0200000001fdf5260ae2c3e90e495a3de5b0fbc24e0c49ab00ebe5414d5c9c843ae1d513ab0100000000ffffffff01a0c5b53e000000001976a91461c42abb6e3435e63bd88862f3746a3f8b86354288ac00000000"
	}
}
```
- response body

```json
{
	"code": 0,
	"message": "Success.",
	"nonce": 1987697,
	"balance": 35517228799,
	"got": 1,
	"total": 1,
	"bestHeight": 1770931,
	"data": "{\n  \"txid\": \"eb8ce15c3c43a46abbf9bde37b0259e46d2698a4429700a83eb356a74018f850\",\n  \"hash\": \"eb8ce15c3c43a46abbf9bde37b0259e46d2698a4429700a83eb356a74018f850\",\n  \"version\": 2,\n  \"size\": 85,\n  \"locktime\": 0,\n  \"vin\": [\n    {\n      \"txid\": \"ab13d5e13a849c5c4d41e5eb00ab490c4ec2fbb0e53d5a490ee9c3e20a26f5fd\",\n      \"vout\": 1,\n      \"scriptSig\": {\n        \"asm\": \"\",\n        \"hex\": \"\"\n      },\n      \"sequence\": 4294967295\n    }\n  ],\n  \"vout\": [\n    {\n      \"value\": 10.521,\n      \"n\": 0,\n      \"scriptPubKey\": {\n        \"asm\": \"OP_DUP OP_HASH160 61c42abb6e3435e63bd88862f3746a3f8b863542 OP_EQUALVERIFY OP_CHECKSIG\",\n        \"hex\": \"76a91461c42abb6e3435e63bd88862f3746a3f8b86354288ac\",\n        \"reqSigs\": 1,\n        \"type\": \"pubkeyhash\",\n        \"addresses\": [\n          \"FEk41Kqjar45fLDriztUDTUkdki7mmcjWK\"\n        ]\n      }\n    }\n  ]\n}"
}
```

## broadcastTx

### 说明

对给出的原始交易进行广播。

### 默认排序

- 无

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
    	"other":<string. raw tx in hex>
	}
}
```
### data in response body

```
<string. the txid or error message from RPC of freecash full node.>

```
### 示例

- request body

```json
{
	"url": "http://localhost:8080/APIP/apip18/v1/broadcastTx",
	"time": 1677673821267,
	"nonce": 1987697,
	"fcdsl":{
		"other":"0200000001c9e3b21610a41ba85120b4a47881aae8971bedeb448980d258b07afbff70b3010200000064414a6f2f0a7b46d0f82469e3983f23334f7ccc1aecd8f9bbb4b43b400aad3075a5e66541ac82f7ee27a031fb5a97fc8a8ddb75d74386978322ad69168f960c80e64121030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312affffffff0200ca9a3b000000001976a91461c42abb6e3435e63bd88862f3746a3f8b86354288ace182ca32000000001976a91461c42abb6e3435e63bd88862f3746a3f8b86354288ac00000000"
	}
}
```
- response body

```json
{
	"code": 0,
	"message": "Success.",
	"nonce": 1987697,
	"balance": 35517328799,
	"got": 0,
	"total": 0,
	"bestHeight": 1770927,
	"data": "077c6a013ddf2d575e9999001aaa980820d91dbc709644e7dd755f0f81aa5c93"
}
```

broadcast again, then get:
```
{
	"code": 0,
	"message": "Success.",
	"nonce": 1987697,
	"balance": 35517328799,
	"got": 0,
	"total": 0,
	"bestHeight": 1770927,
	"data": "{\"result\":null,\"error\":{\"code\":-25,\"message\":\"Missing inputs\"},\"id\":\"-143145100729439188\"}\n"
}
```
