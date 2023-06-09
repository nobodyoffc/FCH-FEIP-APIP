
```
APIP13:SafeOnChain
Version:1
Language:zh-CN
Author:C_armX
Status:draft
Createddate:2021-12-01
Update:2022-04-27
PID:""
Txid:
```

# APIP13V1_SafeOnChain(zh-CN)
---
## 目录

[关于APIP](#关于APIP)

[关于本协议](#关于本协议)

[getValidSafeItems](#getValidSafeItems)

[getDeletedSafeItems](#getDeletedSafeItems)

[getSafeOpHistory](#getSafeOpHistory)

---

```
Type:APIP
SerialNumber:13
ProtocolName:SafeOnChain
Version:1
Description:定义获取链上保险柜信息的相关接口。
Author:C_armX
Language:zh-CN
preVersionHash:""
```

## 关于APIP

### 概述

`APIP`(Application Programming Interface Protocols)是自由共识生态协议的一种类型，用于创建和发布开放的API文档，供API服务方开发部署通用API服务，以实现数据服务的去中心化。API需求方可以按照APIP协议从遵循该协议的任何一个API服务方那里获取数据服务。

### 通用规则

`《APIP1_OpenAPI》`规范了APIP类型协议的`协议发布`、`接口标识`、`接口URL构成`、`时间戳格式`、`密码算法`、`服务流程`、`商业模式`、`连接接口`，`数据接口`和`查询语法`的通用规则。设计、开发或应用APIP协议前，应先参考《APIP1_OpenAPI》协议。

### 建立连接

使用APIP协议各接口，须先采用《APIP1_OpenAPI》的`connect`接口从API服务方获取symKey。参见`《APIP1_OpenAPI》`的“connect接口”。

### 通用请求参数

APIP类型协议接口的通用请求参数如下：

|name|type|description|requested|
|:---|:---|:---|:---|
|requester|string|请求方的fch地址|Y|
|url|string|当前所请求api的url|Y|
|timestamp|timestamp|发起请求的时间戳，精确到毫秒|Y|
|query|object|请求数据的查询语句，参见”APIP简易查询语法@APIP1“|N|
|sort|object|请求数据的排序语句，参见”APIP简易查询语法@APIP1“|N|
|from|int|请求列表数据时的起始位置，从0开始|N|
|size|int|请求列表数据时的每页条目数|N|
|sign|string|用symkey对其他参数所做的签名|Y|

### 通用响应方式

响应前所作的验证、通用响应状态码、通用响应参数参见`《APIP1_OpenAPI》`的“数据请求接口”的“5. 响应”。

请求成功的通用响应参数为：

|参数|类型|说明|适用状态|
|:---|:---|:---|:---|
|code|int|响应状态码|all|
|message|string|响应状态描述|all|
|data|object|正常返回数据，由具体APIP定义。|0|
|sign|string|将symKey加入响应参数做两次sha256哈希的签名值|all|

具体APIP协议的特定响应参数在`data`参数中给出，并定义具体接口的响应状态。

请求失败的通用响应码、响应信息和响应参数，参见`《APIP1_OpenAPI》`的“数据请求接口”的“5. 响应”。

### 签名与验证

APIP接口的请求和响应都需要签名和验证。

connect接口请求时用私钥签名，公钥验证，响应时无需签名。

所有数据请求和数据响应，采用以下方法：

`签名`：将对称密钥symKey加入参数压缩升序后计算两次sha256，得到sign的值，再用sign替换参数中的symKey。

`验证`：将sign从参数中取出，加入symKey，压缩升序后做两次sha256计算，将值与sign的值比较，一致则验证通过。

详见`《APIP1_OpenAPI》`

## 关于本协议

1. 本协议接口提供链上保险柜信息。

2. 本协议接口的数据来源遵循以下协议：

   - 《FEIP17_Safe》

3. 本协议接口数据采用JSON格式。

4. 示例数据：

`requester`：FEk41Kqjar45fLDriztUDTUkdki7mmcjWK

requester的公钥`pubKey`：030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a

requester的私钥`priKey`：L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8

requester获得的对称密钥`symKey`：d2c03bbc1ba1380eafc395374e8da61f92545a1aac5d30b0c19289a69bd34a09

服务方链上发布的`urlMainPart`: https://www.sign.cash/api/

接口url：https://www.sign.cash/api/apip13/v1/[接口名称]

---

## getValidSafeItems

获取链上当前有效的保险柜条目信息。


### url

[服务方发布的urlMainPart]apip13/v1/getValidSafeItems

示例

https://www.sign.cash/api/apip13/v1/getValidSafeItems


### 请求参数

通用请求参数参见[通用请求参数](#通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX的链上保险柜信息，按交易时间戳倒序，获取第1批20条。

```
{
	"requesrer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"timestamp": 1635513688254,
	"url": "https://www.sign.cash/api/apip13/v1/getValidSafeItems",
	"query": {
		"term": {
			"address": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX"
		}
	},
	"sort": [{
		"txTimestamp": {
			"order": "desc"
		}
	}],
	"from": 0,
	"size": 20,
	"sign": ""
}
```

### 响应参数

通用参数参见[通用响应方式](#通用响应方式)。本接口响应参数：

* data

|name|type|description|requested|
|:---|:---|:---|:---|
|list|object|See below|
|total|uint|The total number of items.|Y|
|bestHeight|uint|The latest block height when getting the data.|Y|

* data.list

|name|type|description|key in FEIP12|
|:---|:---|:---|:---|
|address|string|The address of the first input.|
|alg|string|The encrypt algorithm|data.alg|
|ciphertext|Safe item content encrypted with the public key of the host|data.ciphertext|
|txid|string|The txid of this transaction|
|txTimestamp|timestamp|The timestamp of this transaction|
|blockHeight|uint|The height of the block where the transaction is written|

* 示例

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
			"address": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX",
			"alg": "ECC256k1-AES256CBC",
			"ciphertext": "AjTU0rGQvDxhCs3F5x4Pcz3Bsiiry2LryPcKcPIZ2iDsD68U5he9FkM6AVUzEHTjmfBLkhfFu7rv4fveoyMi5YH+wQoiWDxgs/MYjGZBL/Fuq6XZ6IOCXfWyfwphE4uxhEg5TD9ZBRsrJbNxwbdfee5ev5Gvc8kwYROycs0sAG3rNdoJbEZZ7bs2DqvHbAWdG7w4gYLhP9o+C/xVTZHz7Ks9VHb6i04/1at40etlWXxPWSvkdDWxTtyWSSsY2jrbYjfe+ytXQRTRY4gYQdwg+9s=",
			"txid": "11111111110000000000",
			"txTimestamp": 1636180056756,
			"blockHeight": 963220
		}],
		"total": 1,
		"bestHeight": 963667
	},
	"sign": ""
}
```

* 响应状态

参见`《APIP1_OpenAPI》`的“数据请求接口”的“5. 响应”。

---

## getDeletedSafeItems

### 说明

获取链上已经删除的保险柜信息。

### url

[服务方发布的urlMainPart]apip13/v1/getDeletedSafeItems

示例

https://www.sign.cash/api/apip13/v1/getDeletedSafeItems


### 请求参数

通用请求参数参见[通用请求参数](#通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX链上已删除的保险柜信息，按交易时间戳倒序，获取第1批20条。

```
{
	"requesrer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"timestamp": 1635513688254,
	"url": "https://www.sign.cash/api/apip13/v1/getDeletedSafeItems",
	"query": {
		"term": {
			"address": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX"
		}
	},
	"sort": [{
		"txTimestamp": {
			"order": "desc"
		}
	}],
	"from": 0,
	"size": 20,
	"sign": ""
}
```
### 响应参数

通用参数参见[通用响应方式](#通用响应方式)。本接口响应参数：

* data

|name|type|description|requested|
|:---|:---|:---|:---|
|list|object|See below|
|total|uint|The total number of items.|Y|
|bestHeight|uint|The latest block height when getting the data.|Y|

* data.list

|name|type|description|key in FEIP12|
|:---|:---|:---|:---|
|address|string|The address of the first input.|
|alg|string|The encrypt algorithm|data.alg|
|ciphertext|Safe item content encrypted with the public key of the host|data.ciphertext|
|addTxid|string|The txid of this transaction in which the safe item was added||
|addTxTimestamp|timestamp|The timestamp of the transaction in which the safe item was added|
|addBlockHeight|uint|The height of the block in which the safe item was added|
|deleteTxid|string|The txid of this transaction in which the safe item was deleted|
|deleteTxTimestamp|timestamp|The timestamp of the transaction in which the safe item was deleted|
|deleteBlockHeight|uint|The height of the block in which the safe item was deleted|

* 示例

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
			"address": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX",
			"alg": "ECC256k1-AES256CBC",
			"ciphertext": "AjTU0rGQvDxhCs3F5x4Pcz3Bsiiry2LryPcKcPIZ2iDsD68U5he9FkM6AVUzEHTjmfBLkhfFu7rv4fveoyMi5YH+wQoiWDxgs/MYjGZBL/Fuq6XZ6IOCXfWyfwphE4uxhEg5TD9ZBRsrJbNxwbdfee5ev5Gvc8kwYROycs0sAG3rNdoJbEZZ7bs2DqvHbAWdG7w4gYLhP9o+C/xVTZHz7Ks9VHb6i04/1at40etlWXxPWSvkdDWxTtyWSSsY2jrbYjfe+ytXQRTRY4gYQdwg+9s=",
			"addTxid": "11111111110000000000",
			"addTxTimestamp": 1636180056756,
			"addBlockHeight": 963220,
			"deleteTxid": "22222111100111111111",
			"deleteTxTimestamp": 1636180838232,
			"deleteBlockHeight": 963333
		}],
		"total": 1,
		"bestHeight": 963667
	},
	"sign": ""
}
```

* 响应状态

参见`《APIP1_OpenAPI》`的“数据请求接口”的“5. 响应”。

## 3.getSafeOpHistory

查询保险柜链上操作历史信息。

### url

[服务方发布的urlMainPart]apip13/v1/getSafeOpHistory

示例

https://www.sign.cash/api/apip13/v1/getSafeOpHistory


### 请求参数

通用请求参数参见[通用请求参数](#通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX链上保险柜操作历史信息，按交易时间戳正序，获取第1批20条。

```
{
	"requesrer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"timestamp": 1635513688254,
	"url": "https://www.sign.cash/api/apip13/v1/getSafeOpHistory",
	"query": {
		"term": {
			"address": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX"
		}
	},
	"sort": [{
		"txTimestamp": {
			"order": "asc"
		}
	}],
	"from": 0,
	"size": 20,
	"sign": ""
}
```

### 响应参数

通用参数参见[通用响应方式](#通用响应方式)。本接口响应参数：

* data

|name|type|description|requested|
|:---|:---|:---|:---|
|list|object|See below|
|total|uint|The total number of items.|Y|
|bestHeight|uint|The latest block height when getting the data.|Y|

* data.list

|name|type|description|key in FEIP12|
|:---|:---|:---|:---|
|address|string|The address of the first input.|
|op|string|The operation|data.op|
|alg|string|The encrypt algorithm|data.alg|
|ciphertext|Safe item content encrypted with the public key of the host|data.ciphertext|
|addTxid|string|The txid of this transaction in which the item was added|data.addTxid|
|txid|string|The txid of this transaction|
|txTimestamp|timestamp|The timestamp of this transaction|
|blockHeight|uint|The height of the block where the transaction is written|


* 示例

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
				"address": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX",
				"op": "add",
				"alg": "ECC256k1-AES256CBC",
				"ciphertext": "AjTU0rGQvDxhCs3F5x4Pcz3Bsiiry2LryPcKcPIZ2iDsD68U5he9FkM6AVUzEHTjmfBLkhfFu7rv4fveoyMi5YH+wQoiWDxgs/MYjGZBL/Fuq6XZ6IOCXfWyfwphE4uxhEg5TD9ZBRsrJbNxwbdfee5ev5Gvc8kwYROycs0sAG3rNdoJbEZZ7bs2DqvHbAWdG7w4gYLhP9o+C/xVTZHz7Ks9VHb6i04/1at40etlWXxPWSvkdDWxTtyWSSsY2jrbYjfe+ytXQRTRY4gYQdwg+9s=",
				"txid": "11111111110000000000",
				"txTimestamp": 1636180000111,
				"blockHeight": 963220
			},
			{
				"address": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX",
				"op": "delete",
				"addTxid": "11111111110000000000",
				"txid": "33333333333333333333333333333333",
				"txTimestamp": 1636180000222,
				"blockHeight": 963201
			}
		],
		"total": 2,
		"bestHeight": 963667
	},
	"sign": ""
}
```