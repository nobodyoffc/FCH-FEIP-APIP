
```
APIP7:APP
Version:1
Language:zh-CN
Author:C_armX
Status:draft
Createddate:2021-12-01
Update:2022-12-23
PID:""
TXID:
```

# APIP7V1_APP(zh-CN)

## 目录

[关于APIP](#关于APIP)

[关于本协议](#关于本协议)

[getAppList](#getAppList)

[getAppOpHistory](#getAppOpHistory)

[getAppRateHistory](#getAppRateHistory)

---

```
Type:APIP
SerialNumber:7
ProtocolName:APP
Version:1
Description:定义应用信息查询接口。
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

1. 本协议接口提供链上注册的各中密码应用的相关信息。

2. 本协议接口的数据来源遵循以下协议：

   - 《FEIP15_APP》

3. 本协议接口数据采用JSON格式。

4. 示例数据：

`requester`：FEk41Kqjar45fLDriztUDTUkdki7mmcjWK

requester的公钥`pubKey`：030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a

requester的私钥`priKey`：L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8

requester获得的对称密钥`symKey`：d2c03bbc1ba1380eafc395374e8da61f92545a1aac5d30b0c19289a69bd34a09

服务方链上发布的`urlMainPart`: https://www.sign.cash/api/

接口url：https://www.sign.cash/api/apip7/v1/[接口名称]



---

## getAppList

查询APP的最新详细信息。

### url

[服务方发布的urlMainPart]apip7/v1/getAppList

示例

https://www.sign.cash/api/apip7/v1/getAppList

### 请求参数

通用请求参数参见[通用请求参数](通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询aid为"0000000000000000000000000011111111111"和"2222222200000000033333333333333333333"的应用最新详情。


```
{
	"requesrer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"time": 1635513688254,
	"url": "https://www.sign.cash/api/apip7/v1/getAppList",
	"query": {
		"terms": {
			"aid": [
				"0000000000000000000000000011111111111",
				"2222222200000000033333333333333333333"
			]
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

|name|type|description|key in FEIP15|
|:---|:---|:---|:---|
|aid|string|The ID of the APP,that is,the txid of the transaction in which the APP started up.||
|stdName|string|The name of the APP in english|data.stdName|
|localNames|string array|app names in different languages|data.localNames|
|desc|string|Description of this app|data.desc|
|types|string array|The types of the APP|data.types|
|urls|string array|URLs of the APP|data.urls|
|protocols|string array|The protocols followed by this APP|data.protocols|
|services|string array|the APPs used by this APP|data.services|
|pubKey|string|The public key designated by the publisher for this app|data.pubKey|
|signer|string|The signer of the first input of the transaction in which the APP starts up.||
|firstTime|timeStamp|The timeStamp of the transaction in which the APP was registered.||
|firstHeight|uint|The height of the block in which the service was first published.|
|lastTxid|string|The txid of the last operation of the APP.||
|lastTime|timeStamp|The transaction timestamp of the last operation of the APP.||
|lastHeight|uint|The height of the block of the last operation of the App.|
|tCdd|uint64|The total destroyed coindays of all transactions where the value of data.op is "rate".||
|tRate|float|Cumulative rating score. tRate=(tCdd * tRate + cdd * rate)/(tCdd+cdd)||
|status|bool|Is the APP in effect.||



* 示例
响应id为"0000000000000000000000000011111111111"和"2222222200000000033333333333333333333"的应用最新详情。

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
				"aid": "0000000000000000000000000011111111111",
				"stdName": "Free cid SignIn",
				"localNames": ["免费cid信息接口"],
				"desc": "提供cid的地址、公钥、homepage、通知费等详情信息",
				"types": ["CID", "Basic SignIn"],
				"urls": ["https://www.sign.cash/cidInfo"],
				"protocols": ["11111111111111111111111111", "22222222222222222222222"],
				"services": ["33333333333333333333333333", "44444444444444444444444"],
				"pubKey": "030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a",
				"signer": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
				"firstTime": 1610261291,
				"firstHeight":890000,
				"lastTime": 1636526891,
				"lastHeight":900234,
				"lastTxid": "11111122000000000000000000011",
				"tCdd": 12300000,
				"tRate": 4.65,
				"status": 1
			},
			{
				"aid": "2222222200000000033333333333333333333",
				"stdName": "Lianjia Housing agency",
				"localNames": "链家房屋中介",
				"desc": "在世界各地提供优质的房屋中介应用。",
				"types": ["House", "Agency"],
				"urls": ["https://www.lianjia.com/index.html"],
				"protocols": ["11111111111111111111111111", "22222222222222222222222"],
				"services": ["33333333333333333333333333", "44444444444444444444444"],
				"pubKey": "000002020022222222222",
				"signer": "F0000000000000008838dsjf",
				"firstTime": 1619261292,
				"firstHeight":890001,
				"lastTxid": "222222220000000033333333333",
				"lastTime": 1619261292,
				"lastHeight":900245,
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

## getAppOpHistory

查询除data.op值为“rate”之外的APP链上操作信息。


### url

[服务方发布的urlMainPart]apip7/v1/getAppOpHistory

示例

https://www.sign.cash/api/apip7/v1/getAppOpHistory


### 请求参数

通用请求参数参见[通用请求参数](通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询aid为"0000000011111111111111111111111222"的链上应用操作记录，按txTime降序，从第1个开始取20条。
```
{
	"requesrer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"time": 1635513688254,
	"url": "https://www.sign.cash/api/apip7/v1/getAppOpHistory",
	"query": {
		"term": {
			"aid": "0000000011111111111111111111111222"
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
|aid|string||
|signer|string|The signer of the first input of the transaction in which the APP starts up.||
|op|string|The operation, "initial", "update", "stop", or "restart".|
|stdName|string|The name of the APP in english|data.stdName|
|localNames|string array|app names in different languages|data.localNames|
|desc|string|Description of this app|data.desc|
|types|string array|The types of the APP|data.types|
|urls|string array|URLs of the APP|data.urls|
|protocols|string array|The protocols followed by this APP|data.protocols|
|services|string array|the APPs used by this APP|data.services|
|pubKey|string|The public key designated by the publisher for this app|data.pubKey|
|txid|string|The txid of this transaction|
|txTime|timestamp|The timestamp of this transaction|
|height|uint|The height of the block where the transaction is written|

* 示例

响应aid为“000000001111111122222222222222”的应用信息。

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
				"aid": "000000001111111122222222222222",
				"signer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
				"op": "stop",
				"txTime": 1613211239,
				"txid": "0460fc1a1ada6479037a07c05509cfcb0d2e8ead2d2b32a5a6784f558c448866",
				"height": 989239
			},

			{
				"op": "update",
				"stdName": "Freesign",
				"localNames": ["飞签"],
				"desc": "提供cid的地址、公钥、homepage、通知费等详情信息",
				"types": ["CID", "Basic SignIn"],
				"urls": ["https://www.sign.cash/cidInfo"],
				"protocols": ["11111111111111111111111111", "22222222222222222222222"],
				"services": ["33333333333333333333333333", "44444444444444444444444"],
				"pubKey": "030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a",
				"txTime": 1610261291,
				"txid": "3333333333333333333333333333388888888888888888888888888889",
				"height": 989021
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

## getAppRateHistory

查询对应用的链上评价信息，即data.op值为“rate”的记录。

### url

[服务方发布的urlMainPart]apip7/v1/getAppRateHistory

示例

https://www.sign.cash/api/apip7/v1/getAppRateHistory


### 请求参数

通用请求参数参见[通用请求参数](通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询aid为"0000000011111111111111111111111222"的应用的币天销毁大于等于1000的评分记录，按“rate”一级降序，交易时间二级降序，获取第1批20条。


```
{
	"requesrer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"time": 1635513688254,
	"url": "https://www.sign.cash/api/apip7/v1/getAppRateHistory",
	"query": {
		"bool": {
			"must": [{
					"term": {
						"aid": ""0000000011111111111111111111111222""
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
|aid|string||
|rater|uint|The address of the first input of the rating transaction|N|
|rate|uint|Rating value from 0 to 5|
|cdd|uint64||
|txid|string|The txid of this transaction|
|txTime|timestamp|The timestamp of this transaction|
|height|uint|The height of the block where the transaction is written|

* 示例
响应aid为"0000000011111111111111111111111222"的应用的币天销毁大于等于1000的评分记录，按“rate”一级降序，交易时间二级降序，获取第1批20条。

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
				"aid": "0000000011111111111111111111111222",
				"rater": "FS2AWq1dgdhCpNTwqfBbMBBJGNNj1LSboy",
				"rate": 5,
				"cdd": 3100,
				"txid": "333333333333333333338888889",
				"txTime": 1618832997321,
				"height": 989302
			},
			{
				"aid": "0000000011111111111111111111111222",
				"rater": "FLx88wdsbLQyZRmbqtpeXA9u5FG9EyCash",
				"rate": 4,
				"cdd": 10000,
				"txid": "44444443333333388888888888889",
				"txTime": 1618834297302,
				"height": 989300
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