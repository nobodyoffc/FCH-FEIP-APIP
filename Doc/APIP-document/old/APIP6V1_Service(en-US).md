
```
APIP6:Service
Version:1
Language:zh-CN
Author:C_armX
Status:draft
Create:2021-12-01
Update:2022-12-23
PID:""
TXID:
```

# APIP6V1_Service(zh-CN)


## 目录

[关于APIP](#关于APIP)

[关于本协议](#关于本协议)

[getServiceList](#getServiceList)

[getServiceOpHistory](#getServiceOpHistory)

[getServiceRateHistory](#getServiceRateHistory)

---

```
Type:APIP
SerialNumber:6
ProtocolName:Service
Version:1
Description:定义服务信息查询接口。
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
|time|timestamp|发起请求的时间戳，精确到毫秒|Y|
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

1. 本协议接口提供链上注册的各项服务的相关信息。

2. 本协议接口的数据来源遵循以下协议：

   - 《FEIP29_Service》

3. 本协议接口数据采用JSON格式。

4. 示例数据：

`requester`：FEk41Kqjar45fLDriztUDTUkdki7mmcjWK

requester的公钥`pubKey`：030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a

requester的私钥`priKey`：L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8

requester获得的对称密钥`symKey`：d2c03bbc1ba1380eafc395374e8da61f92545a1aac5d30b0c19289a69bd34a09

服务方链上发布的`urlMainPart`: https://www.sign.cash/api/

接口url：https://www.sign.cash/api/apip6/v1/[接口名称]

---

## getServiceList

获取服务信息列表。

### url

[服务方发布的urlMainPart]apip6/v1/getServiceList

示例：

https://www.sign.cash/api/apip6/v1/getServiceList


### 请求参数

通用请求参数参见[通用请求参数](#通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询sid为“222220000000000011111”和“33333000000002222222222”的服务最新详情。

```
{
	"requester": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"time": 1635513688254,
	"url": "https://www.sign.cash/api/apip6/v1/getServiceList",
	"query": {
		"terms": {
			"sid": ["222220000000000011111","33333000000002222222222"]
		}
	},
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

|name|type|description|key in FEIP29|
|:---|:---|:---|:---|
|sid|string|The ID of the service,that is,the txid of the transaction in which the service started up.||
|stdName|string|The name of the service in english|data.stdName|
|localName|string array|Service names in different languages|data.localName|
|desc|string|Description of this service|data.desc|
|types|string array|The types of the service|data.types|
|urls|string array|URLs of the service|data.urls|
|pubKeyAdmin|string|The public key designated by the publisher for this service|data.pubKeyAdmin|
|signer|string|The signer of the first input of the transaction in which the service starts up.||
|firstTime|timeStamp|The timeStamp of the transaction in which the service was first published.||
|firstHeight|uint|The height of the block in which the service was first published.|
|lastTxid|string|The txid of the last operation of the service.||
|lastTime|timeStamp|The transaction timestamp of the last operation of the service.||
|lastHeight|uint|The height of the block of the last operation of the service.|
|tCdd|uint64|The total destroyed coindays of all transactions where the value of data.op is "rate".||
|tRate|float|Cumulative rating score. tRate=(tCdd\*tRate+cdd\*rate)/(tCdd+cdd)||
|status|bool|Is the service in effect.||


* 示例

响应sid为“222220000000000011111”和“33333000000002222222222”的服务最新详情。

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
				"sid": "222220000000000011111",
				"stdName": "Free cid API",
				"localName": ["免费cid信息接口"],
				"desc": "提供cid的地址、公钥、homepage、通知费等详情信息",
				"types": ["CID", "Basic API"],
				"urls": ["https://www.sign.cash/cidInfo"],
				"pubKeyAdmin": "2222222222222222222222",
				"signer": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
				"firstTime": 1610261291,
				"firstHeight": 963510,
				"lastTxid": "1111111111111111111111111",
				"lastTime": 1636526891,
				"lastHeight": 963512,
				"tCdd": 12300000,
				"tRate": 4.65,
				"status": 1
			},
			{
				"sid": "33333000000002222222222",
				"stdName": "Lianjia Housing agency",
				"localName": "链家房屋中介",
				"desc": "在世界各地提供优质的房屋中介服务。",
				"types": ["House", "Agency"],
				"urls": ["https://www.lianjia.com/index.html"],
				"pubKeyAdmin": "000002020022222222222",
				"signer": "F0000000000000008838dsjf",
				"firstTime": 1619261292,
				"firstHeight": 963600,
				"lastTxid": "3333300000000222222233333",
				"lastTime": 1619261292,
				"lastHeight": 963612,
				"tCdd": 221800,
				"tRate": 4.12,
				"status": 1
			}

		],
		"total": 2,
		"bestHeight": 963667
	},
	"sign": ""
}
```

* 响应状态

参见`《APIP1_OpenAPI》`的“数据请求接口”的“5. 响应”。
---

## getServiceOpHistory

查询除data.op值为“rate”之外的服务链上操作历史信息。

### url

[服务方发布的urlMainPart]apip6/v1/getServiceOpHistory

示例：

https://www.sign.cash/api/apip6/v1/getServiceOpHistory


### 请求参数

通用请求参数参见[通用请求参数](#通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询SID为"222220000000000011111111111111"的服务操作记录，按交易时间戳倒序，获取第1批20条。

```
{
	"requester": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"time": 1635513688254,
	"url": "https://www.sign.cash/api/apip7/v1/getServiceOpHistory",
	"query": {
		"term": {
			"sid": "222220000000000011111111111111"
		}
	},
	"sort": [{
		"txTime": {
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

|name|type|description|
|:---|:---|:---|
|sid|string||
|op|string|The operation, "initial", "update", "stop", or "restart".|data.op|
|stdName|string|The name of the service in english|data.stdName|data.stdName|
|localName|string array|Service names in different languages|data.localName|
|desc|string|Description of this service|data.desc|
|type|string array|The types of the service|data.type|
|urls|string array|URLs of the service|data.urls|
|pubKeyAdmin|string|The public key designated by the publisher for this service|data.pubKeyAdmin|
|signer|string|The signer of the first input of the transaction in which the service starts up.||
|txid|string|The txid of this transaction|
|txTime|timestamp|The timestamp of this transaction|
|block|uint|The height of the block where the transaction is written|

* 示例
响应sid为"222220000000000011111111111111"的服务操作信息。

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
				"sid": "222220000000000011111111111111",
				"op": "stop",
				"txTime": 1613211239,
				"txid": "11111111111111111111111111111",
				"blockHeight": 989239
			},
			{
				"sid": "222220000000000011111111111111",
				"op": "update",
				"stdName": "Free cid API",
				"localName": ["免费cid信息接口"],
				"desc": "提供cid的地址、公钥、homepage、通知费等详情信息",
				"type": ["CID", "Basic API"],
				"urls": ["https://www.sign.cash/cidInfo"],
				"pubKeyAdmin": "033333333333333333333333",
				"txTime": 1610261291,
				"txid": "2222222222222222222222222222,
				"block": 989021
			}
		],
		"total": 2,
		"bestHeight": 993667
	},
	"sign": ""
}
```
* 响应状态

参见`《APIP1_OpenAPI》`的“数据请求接口”的“5. 响应”。

---

## getServiceRateList

### 说明

查询对服务的链上评价信息，即data.op值为“rate”的记录。

### url

[服务方发布的urlMainPart]apip6/v1/getServiceRateList

示例：

https://www.sign.cash/api/apip6/v1/getServiceRateList


### 请求参数

通用请求参数参见[通用请求参数](#通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询sid为"0000000011111111111111111111111222"的服务的币天销毁大于等于1000的评分记录，按“rate”一级降序，交易时间二级降序，获取第1批20条。

```
{
	"requester": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"time": 1635513688254,
	"url": "https://www.sign.cash/api/apip7/v1/getRateHistory",
	"query": {
		"bool": {
			"must": [{
					"term": {
						"sid": ""0000000011111111111111111111111222""
					}
				},
				{
					"range": {
						"cdd": {
							"gte": 1000
						}
					}
				}
			]
		}
	},
	"sort": [{
		"rate": {
			"order": "desc"
		},
		{"txTimeStamp": {
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

|name|type|description|
|:---|:---|:---|
|sid|string||
|rater|uint|The address of the first input of the rating transaction|N|
|rate|uint||
|cdd|uint64||
|txid|string|The txid of this transaction|
|txTime|timestamp|The timestamp of this transaction|
|blockHeight|uint|The height of the block where the transaction is written|

* 示例
响应sid为"0000000011111111111111111111111222"的服务信息。

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
				"sid": "0000000011111111111111111111111222",
				"rater": "FS2AWq1dgdhCpNTwqfBbMBBJGNNj1LSboy",
				"rate": 5,
				"cdd": 3100,
				"txid": "2222222222222222222222",
				"txTime": 1618832997321,
				"blockHeight": 989302
			},
			{
				"rater": "FLx88wdsbLQyZRmbqtpeXA9u5FG9EyCash",
				"rate": 4,
				"cdd": 10000,
				"txid": "333333333333333333333333",
				"txTime": 1618834297302,
				"blockHeight": 989302
			}

		],
		"total": 2,
		"bestHeight": 993667
	},
	"sign": ""
}

```
* 响应状态

参见`《APIP1_OpenAPI》`的“数据请求接口”的“5. 响应”。