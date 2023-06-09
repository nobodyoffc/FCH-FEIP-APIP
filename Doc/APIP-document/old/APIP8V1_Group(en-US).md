
```
APIP9V1_Group(zh-CN)
Version:1
Language:zh-CN
Author:C_armX
Status:draft
Create:2021-12-03
Update:2021-04-29
PID:""
TXID:
```

# APIP9V1_Group(zh-CN)

## 目录

[关于APIP](#关于APIP)

[关于本协议](#关于本协议)

[GroupSearch](#groupsearch)

[getGroupList](#getGroupList)

[getGroupMembers](#getGroupActiveMembers)

[getGroupExmembers](#getGroupExitedMembers)

[getMyGroups](#getMyGroups)

[getGroupOpHistory](#getGroupOpHistory)

---

```
Type:APIP
SerialNumber:9
ProtocolName:Group
Version:1
Description:定义群信息查询接口。
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

1. 本协议接口提供链上注册的各种群的相关信息。

2. 本协议接口的数据来源遵循以下协议：

   - 《FEIP19_Group》

3. 本协议接口数据采用JSON格式。

4. 示例数据：

`requester`：FEk41Kqjar45fLDriztUDTUkdki7mmcjWK

requester的公钥`pubKey`：030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a

requester的私钥`priKey`：L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8

requester获得的对称密钥`symKey`：d2c03bbc1ba1380eafc395374e8da61f92545a1aac5d30b0c19289a69bd34a09

服务方链上发布的`urlMainPart`: https://www.sign.cash/api/

接口url：https://www.sign.cash/api/apip9/v1/[接口名称]

---


## getGroupList

获取群信息列表。

### url

[服务方发布的urlMainPart]apip9/v1/getGroupList

示例：

https://www.sign.cash/api/apip9/v1/getGroupList


### 请求参数

通用请求参数参见[通用请求参数](#通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询gid为“222220000000000011111”和“33333000000002222222222”的群最新详情。

```
{
	"requesrer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"timestamp": 1635513688254,
	"url": "https://www.sign.cash/api/apip9/v1/getGroupList",
	"query": {
		"terms": {
			"gid": ["222220000000000011111","33333000000002222222222"]
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


|name|type|description|key in FEIP19|
|:---|:---|:---|:---|
|gid|string|The ID of the group,that is,the txid of the transaction in which the group was registered.||
|name|string|The name of the group in english|data.name|
|desc|string|Description of this group|data.desc|
|activeMemberNumber|uint64|The number of active members of this group|
|exitedMemberNumber|uint64|The number of exited members of this group|
|sponsor|string|The signer of the first input of the transaction in which the group was registered.||
|updater|string|The signer of the first input of the transaction in which the group was updated recently.||
|firstTimestamp|timeStamp|The timeStamp of the transaction in which the group was first published.||
|firstBlockHeight|uint|The height of the block in which the group was first published.|
|lastTxid|string|The txid of the last operation of the group.||
|lastTimestamp|timeStamp|The transaction timestamp of the last operation of the group.||
|lastBlockHeight|uint|The height of the block of the last operation of the group.|
|tCdd|uint64|All destroyed coindays accumulated by all transactions where the value of data.op is "create", "update", or "join".||



* 示例
响应查询gid为“222220000000000011111”和“33333000000002222222222”的群的除成员列表之外最新详情。

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
				"gid": "222220000000000011111",
				"name": "crypto socity",
				"desc": "欢迎来到密码社区",
				"activeMemberNumber": 2340,
				"exitedMemberNumber": 21,
				"sponsor": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
				"updater": "",
				"firstTimestamp": 1610261291,
				"firstBlockHeight": 963510,
				"lastTxid": "1111111111111111111111111",
				"lastTimestamp": 1636526891,
				"lastBlockHeight": 963512,
				"tCdd": 12300000
			},
			{
				"gid": "33333000000002222222222",
				"name": "digital socity",
				"desc": "数字社区欢迎你！",
				"activeMemberNumber": 20,
				"exitedMemberNumber": 3,
				"sponsor": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
				"updater": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
				"firstTimestamp": 1619261292,
				"firstBlockHeight": 963600,
				"lastTxid": "3333300000000222222233333",
				"lastTimestamp": 1619261292,
				"lastBlockHeight": 963612,
				"tCdd": 2300000
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

## getGroupActiveMembers

获取群当前有效成员信息。

### url

[服务方发布的urlMainPart]apip9/v1/getGroupActiveMembers

示例：

https://www.sign.cash/api/apip9/v1/getGroupActiveMembers


### 请求参数

通用请求参数参见[通用请求参数](#通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询gid为"222220000000000011111111111111"的当前有效成员。

```
{
	"requesrer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"timestamp": 1635513688254,
	"url": "https://www.sign.cash/api/apip9/v1/getGroupActiveMembers",
	"query": {
		"term": {
			"gid": "222220000000000011111111111111"
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
|gid|string|The ID of the group,that is,the txid of the transaction in which the group was registered.||
|activeMemberNumber|uint64|The number of active members of this group|
|activeMembers|string array|The address list of active members.||


* 示例
响应查询gid为"222220000000000011111111111111"的当前有效成员。

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
			"gid": "222220000000000011111",
			"activeMemberNumber": 4,
			"activeMembers": ["F1111111111111S", "F2222222222222222Z", "F33333333333333333333h", "F4444444444444444444NC7N"]
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
## getGroupExitedMembers

获取群已退出成员信息。

### url

[服务方发布的urlMainPart]apip9/v1/getGroupExitedMembers

示例：

https://www.sign.cash/api/apip9/v1/getGroupExitedMembers


### 请求参数

通用请求参数参见[通用请求参数](#通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询gid为"222220000000000011111111111111"的已退出成员。

```
{
	"requesrer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"timestamp": 1635513688254,
	"url": "https://www.sign.cash/api/apip9/v1/getGroupExitedMembers",
	"query": {
		"term": {
			"gid": "222220000000000011111111111111"
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
|gid|string|The ID of the group,that is,the txid of the transaction in which the group was registered.||
|exitedMemberNumber|uint64|The number of exited members of this group|
|exitedMembers|string array|The address list of exited members.||


* 示例
响应查询gid为"222220000000000011111111111111"的当前有效成员。

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
			"gid": "222220000000000011111",
			"exitedMemberNumber": 4,
			"exitedMembers": ["F1111111111111S", "F2222222222222222Z", "F33333333333333333333h", "F4444444444444444444NC7N"]
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

## getMyGroups

获取某地址所参加的群。

### url

[服务方发布的urlMainPart]apip9/v1/getMyGroups

示例：

https://www.sign.cash/api/apip9/v1/getMyGroups


### 请求参数

通用请求参数参见[通用请求参数](#通用请求参数)。其中：

* query

以单个fch地址`address`为查询条件，可增加`active`筛选当前有效的或已退出的群。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX所参加的当前有效的群。

```
{
	"requesrer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"timestamp": 1635513688254,
	"url": "https://www.sign.cash/api/apip9/v1/getMyGroups",
	"query": {
		"bool": {
			"must": [{
					"term": {
						"address": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX"
					}
				},
				{
					"match": {
						"active": 1
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
|gid|string|The ID of the group,that is,the txid of the transaction in which the group was registered.||
|name|string|The name of the group in english||
|active|bool|Is the address active in this group.||

* 示例
响应查询gid为"222220000000000011111111111111"的当前有效成员。

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
				"gid": "222220000000000011111",
				"name": "crypto circle",
				"active": 1
			},
			{
				"gid": "333300000000000111333",
				"name": "crypto world",
				"active": 1
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


## getGroupOpHistory

查询群链上操作历史信息。

### url

[服务方发布的urlMainPart]apip9/v1/getGroupOpHistory

示例：

https://www.sign.cash/api/apip9/v1/getGroupOpHistory


### 请求参数

通用请求参数参见[通用请求参数](#通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询gid为"222220000000000011111111111111"的群操作记录，按交易时间戳倒序，获取第1批20条。

```
{
	"requesrer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"timestamp": 1635513688254,
	"url": "https://www.sign.cash/api/apip9/v1/getGroupOpHistory",
	"query": {
		"term": {
			"gid": "222220000000000011111111111111"
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

|name|type|description|key in FEIP19|
|:---|:---|:---|:---|
|op|string|The operation.|data.op|
|gid|string|The ID of the group,that is,the txid of the transaction in which the group was registered.|data.gid|
|signer|string|The signer of the first input of the transaction||
|name|string|The name of the group|data.name|data.name|
|desc|string|Description of this group|data.desc|data.desc|
|cdd|uint64|Coindays destroyed of this transaction|
|txid|string|The txid of this transaction|
|txTimestamp|timestamp|The timestamp of this transaction|
|blockHeight|uint|The height of the block where the transaction is written|

* 示例

响应查询gid为"222220000000000011111111111111"的群操作记录，按交易时间戳倒序，获取第1批20条。

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
				"gid": "222220000000000011111111111111",
				"op": "join",
				"signer": "FT7P7SxogDhx3eSpFGqCsjnFNaMcfwPi7Z",
				"txTimestamp": 1613211239,
				"txid": "0460fc1a1ada6479037a07c05509cfcb0d2e8ead2d2b32a5a6784f558c448866",
				"cdd": 2300,
				"blockHeight": 989239
			},
			{
				"op": "create",
				"name": "crypto school",
				"desc": "密码学院讨论群",
				"signer": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
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