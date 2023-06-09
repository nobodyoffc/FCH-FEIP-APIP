

```
APIP10V1_Team(zh-CN)
Version:1
Language:zh-CN
Author:C_armX
Status:draft
Create:2021-12-03
Update:2021-04-30
PID:""
TXID:
```

# APIP10V1_Team(zh-CN)
---
## 目录


[关于APIP](#关于APIP)

[关于本协议](#关于本协议)

[getOrgList](#getOrgList)

[getOrgMemberList](#getOrgMemberList)

[getMyOrgs](#getMyOrgs)

[getOrgOpHistory](#getOrgOpHistory)

[getOrgRateHistory](#getOrgRateHistory)

---

```
Type:APIP
SerialNumber:10
ProtocolName:Team
Version:1
Description:定义组织信息查询接口。
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

1. 本协议接口提供链上注册的各种组织的相关信息。

2. 创建组织的交易的`txid`为该组织的唯一身份，即`oid`。

3. 本协议接口的数据来源和相关规则遵循以下协议：

   - 《FEIP28_Team》

4. 本协议接口数据采用JSON格式。

5. 示例数据：

`requester`：FEk41Kqjar45fLDriztUDTUkdki7mmcjWK

requester的公钥`pubKey`：030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a

requester的私钥`priKey`：L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8

requester获得的对称密钥`symKey`：d2c03bbc1ba1380eafc395374e8da61f92545a1aac5d30b0c19289a69bd34a09

服务方链上发布的`urlMainPart`: https://www.sign.cash/api/

接口url：https://www.sign.cash/api/apip10/v1/[接口名称]

---

## getOrgList

获取组织信息列表。

### url

[服务方发布的urlMainPart]apip10/v1/getOrgList

示例：

https://www.sign.cash/api/apip10/v1/getOrgList


### 请求参数

通用请求参数参见[通用请求参数](#通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询oid为“222220000000000011111”和“33333000000002222222222”的组织最新详情。

```
{
	"requesrer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"timestamp": 1635513688254,
	"url": "https://www.sign.cash/api/apip10/v1/getOrgList",
	"query": {
		"terms": {
			"oid": ["222220000000000011111","33333000000002222222222"]
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


|name|type|description|key in FEIP28|
|:---|:---|:---|:---|
|oid|string|The ID of the Team,that is,the txid of the transaction in which the Team was registered.||
|owner|string|The address of the first input of the address of the transaction in which the team was created.|
|stdName|string array|Standard name of the team|data.stdName|
|localName|string array|Names in different languages|data.localName|
|consHash|string|The sha256 value of the team consensus|data.consHash|
|desc|string|Description of this team|data.desc|
|memberNumber|uint64|The number of active members of this team|
|firstTimestamp|timeStamp|The timeStamp of the transaction in which the Team was first published.||
|firstBlockHeight|uint|The height of the block in which the Team was first published.|
|lastTxid|string|The txid of the last operation of the Team.||
|lastTimestamp|timeStamp|The transaction timestamp of the last operation of the Team.||
|lastBlockHeight|uint|The height of the block of the last operation of the Team.|
|tCdd|uint64|The total destroyed coindays of all transactions where the value of data.op is "rate".||
|tRate|float|Cumulative rating score. tRate=(tCdd\*tRate+cdd\*rate)/(tCdd+cdd)||

* 示例

响应查询oid为“222220000000000011111”和“33333000000002222222222”的组织的除成员列表之外最新详情。

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
				"oid": "000000001111111111111111111111122222222222222222222222222",
				"owner": "F000000000000001",
				"stdName": "Love the earth",
				"localName": ["爱护地球", "地球を愛する"],
				"consHash": "11111111111111111111111111111",
				"desc": "欢迎来到密码公司",
				"memberNumber": 2340,
				"firstTimestamp": 1610261291,
				"firstBlockHeight": 963510,
				"lastTxid": "1111111111111111111111111",
				"lastTimestamp": 1636526891,
				"lastBlockHeight": 963512,
				"tCdd": 12300000,
				"tRate": 4.65
			},
			{
				"oid": "2222222200000000000000000001111111111111111333333333333333",
				"owner": "F000000000000002",
				"stdName": "Digital home",
				"localName": ["数字家园", "地球を愛する"],
				"consHash": "22222222222222222222222222",
				"desc": "数字社区欢迎你！",
				"memberNumber": 102,
				"firstTimestamp": 1619261292,
				"firstBlockHeight": 963600,
				"lastTxid": "3333300000000222222233333",
				"lastTimestamp": 1619261292,
				"lastBlockHeight": 963612,
				"tCdd": 2300000,
				"tRate": 4.35
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

## getOrgMemberList

获取各类组织成员列表

### url

[服务方发布的urlMainPart]apip10/v1/getOrgMemberList

示例：

https://www.sign.cash/api/apip10/v1/getOrgMemberList


### 请求参数

通用请求参数参见[通用请求参数](#通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询oid为"222220000000000011111111111111"的组织的各类成员。

```
{
	"requesrer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"timestamp": 1635513688254,
	"url": "https://www.sign.cash/api/apip10/v1/getOrgMemberList",
	"query": {
		"term": {
			"oid": "222220000000000011111111111111"
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

|name|type|description|key in FEIP28|
|:---|:---|:---|:---|
|oid|string|||
|owner|string|The address of the owner|
|admin|string array|The address list of administrators.||
|activeMembers|string array|The address list of active members.||
|quitMembers|string array|The address list of members who declared quit but have not been delisted.||
|delistedMembers|string array|The address list of delisted members.||


* 示例

响应查询oid为"222220000000000011111111111111"的组织当前有效成员。

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
			"oid": "222220000000000011111111111111",
			"owner": "F000000000000001",
			"admin": ["F1111111111111S", "F2222222222222222Z"],
			"activeMembers": ["F1111111111111S", "F2222222222222222Z", "F33333333333333333333h", "F4444444444444444444N"],
			"quitMembers": ["F555555555555555555x ", "F66666666666666666666666z "],
			"delistedMembers": ["F77777777777777777e"]
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

## getMyOrgs

获取某地址所参加的组织。

### url

[服务方发布的urlMainPart]apip10/v1/getMyOrgs

示例：

https://www.sign.cash/api/apip10/v1/getMyOrgs


### 请求参数

通用请求参数参见[通用请求参数](#通用请求参数)。其中：

* query

以单个fch地址`address`为查询条件，可增加`status`筛选在组织内的身份或状态。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX所参加并担任管理员的组织。

```
{
	"requesrer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"timestamp": 1635513688254,
	"url": "https://www.sign.cash/api/apip10/v1/getMyOrgs",
	"query": {
		"bool": {
			"must": [{
					"term": {
						"address": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX"
					}
				},
				{
					"match": {
						"status": "admin"
					}
				}
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

|name|type|description|requested|
|:---|:---|:---|:---|
|oid|string|The ID of the Team,that is,the txid of the transaction in which the Team was registered.||
|name|string|The name of the Team in english||
|status|enum|"owner", "admins", "active", "quit", "delisted"||

* 示例

响应查询FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX所参加并担任管理员的组织。

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
				"oid": "222220000000000011111",
				"name": "crypto circle",
				"status": "admin"
			},
			{
				"oid": "333300000000000111333",
				"name": "crypto world",
				"status": "admin"
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


## getOrgOpHistory

查询组织链上除data.op值为“rate”之外的操作历史信息。

### url

[服务方发布的urlMainPart]apip10/v1/getOrgOpHistory

示例：

https://www.sign.cash/api/apip10/v1/getOrgOpHistory


### 请求参数

通用请求参数参见[通用请求参数](#通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询oid为"222220000000000011111111111111"的组织操作记录，按交易时间戳倒序，获取第1批20条。

```
{
	"requesrer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"timestamp": 1635513688254,
	"url": "https://www.sign.cash/api/apip10/v1/getOrgOpHistory",
	"query": {
		"term": {
			"oid": "222220000000000011111111111111"
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

|name|type|description|key in FEIP28|
|:---|:---|:---|:---|
|op|string|The operation.|data.op|
|oid|string|The oid that the op targets.|data.oid|
|stdName|string array|Standard name of the team|data.stdName|
|localName|string array|Names in different languages.|data.localName|data.localName|
|consHash|string|The sha256 value of the team consensus|data.consHash|
|desc|string|Description of this team.|data.desc|
|inviteList|string array|Addresses of the applicants.|data.inviteList|
|days|int|2|Validity period of the invitation.|data.days|
|deliList|string array|Addresses to be delisted|data.deliList|
|authList|string array|the list of addresses being authorized|data.authList|
|deauList|string array|2048|the list of addresses being deauthorized|data.deauList|
|signer|string|The signer of the first input of the transaction.||
|recipients|string array|Addresses of the outputs except the signer.||
|cdd|uint64|Coindays destroyed of this transaction||
|txid|string|The txid of this transaction.||
|txTimestamp|timestamp|The timestamp of this transaction||
|blockHeight|uint|The height of the block where the transaction is written||

* 示例

响应第2批查询20项oid为000000001111111111111111111111122222222222222222222222222的组织操作记录，按txTimestamp降序。

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
				"op": "invite",
				"oid": "222220000000000011111111111111",
				"signer": "F000000000000",
				"inviteList": ["F1111111111", "F2222222222222", "F33333333333"],
				"recipients": ["F1111111111", "F2222222222222", "F33333333333"],
				"txTimestamp": 1613211239,
				"txid": "0460fc1a1ada6479037a07c05509cfcb0d2e8ead2d2b32a5a6784f558c448866",
				"cdd": 2300,
				"blockHeight": 989239
			},
			{
				"op": "update",
				"oid": "222220000000000011111111111111",
				"signer": "F11111111111",
				"recipients": ["F1111111111"],
				"stdName": "Love the earth",
				"localName": ["天文爱好者", "Astronomers"],
				"consHash": "11111111111111111111111111111",
				"desc": "开放的天文爱好者组织",
				"txTimestamp": 1610261291,
				"txid": "0460fc1a1ada6479037a07c05509cfcb0d2e8ead2d2b32a5a6784f558c448866",
				"cdd": 11300,
				"blockHeight": 989021
			}
		],
		"bestHeight": 989430
	}
}
```

* 响应状态

参见`《APIP1_OpenAPI》`的“数据请求接口”的“5. 响应”。

---

## getOrgRateHistory

查询对组织的链上评价信息，即data.op值为“rate”的记录。

### url

[服务方发布的urlMainPart]apip10/v1/getOrgRateHistory

示例

https://www.sign.cash/api/apip10/v1/getOrgRateHistory


### 请求参数

通用请求参数参见[通用请求参数](通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询oid为"0000000011111111111111111111111222"的组织的币天销毁大于等于1000的评分记录，按“rate”一级降序，交易时间二级降序，获取第1批20条。


```
{
	"requesrer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"timestamp": 1635513688254,
	"url": "https://www.sign.cash/api/apip10/v1/getOrgRateHistory",
	"query": {
		"bool": {
			"must": [{
					"term": {
						"oid": ""0000000011111111111111111111111222""
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
|oid|string||
|rater|uint|The address of the first input of the rating transaction|N|
|rate|uint|Rating value from 0 to 5|
|cdd|uint64||
|txid|string|The txid of this transaction|
|txTimestamp|timestamp|The timestamp of this transaction|
|blockHeight|uint|The height of the block where the transaction is written|

* 示例
响应oid为"0000000011111111111111111111111222"的组织的币天销毁大于等于1000的评分记录，按“rate”一级降序，交易时间二级降序，获取第1批20条。

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
				"oid": "0000000011111111111111111111111222",
				"rater": "FS2AWq1dgdhCpNTwqfBbMBBJGNNj1LSboy",
				"rate": 5,
				"cdd": 3100,
				"txid": "333333333333333333338888889",
				"txTimestamp": 1618832997321,
				"blockHeight": 989302
			},
			{
				"oid": "0000000011111111111111111111111222",
				"rater": "FLx88wdsbLQyZRmbqtpeXA9u5FG9EyCash",
				"rate": 4,
				"cdd": 10000,
				"txid": "44444443333333388888888888889",
				"txTimestamp": 1618834297302,
				"blockHeight": 989300
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