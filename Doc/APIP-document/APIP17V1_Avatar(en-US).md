
```
APIP17V1_Avatar(zh-CN)
Version:1
Language:zh-CN
Author:C_armX
Status:draft
```
APIP17V1_Avatar(zh-CN)
Version:1
Language:zh-CN
Author:C_armX
Status:draft
Create:2022-03-07
Update:2022-05-01
PID:""
TXID:
```

# APIP17V1_Avatar(zh-CN)
---
## 目录

## characters
1. 背景，颜色
2. 背景，花纹，形状
3. 背景，花纹，颜色
4. 头发，形状
5. 头发，颜色

3. 园框，颜色
4. 园框，花纹
5. 

[关于APIP](#关于APIP)

[关于本协议](#关于本协议)

[getHatList](#getHatList)


---

```
Type:APIP
SerialNumber:17
ProtocolName:Avatar
Version:1
Description:定义文件管理的链上哈希属性表信息接口。
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

1. 本协议接口提供用于文件管理的哈希属性表（HAT）文件的链上信息。

2. 本协议接口的数据来源和相关规则遵循以下协议：

   - 《FEIP32_HAT》

3. 本协议接口数据采用JSON格式。

4. 示例数据：
`requester`：FEk41Kqjar45fLDriztUDTUkdki7mmcjWK

requester的公钥`pubKey`：030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a

requester的私钥`priKey`：L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8

requester获得的对称密钥`symKey`：d2c03bbc1ba1380eafc395374e8da61f92545a1aac5d30b0c19289a69bd34a09

服务方链上发布的`urlMainPart`: https://www.sign.cash/api/

接口url：https://www.sign.cash/api/apip17/v1/[接口名称]

## getHatList

获取链上当前有效的HAT信息。

### url

[服务方发布的urlMainPart]apip11/v1/getHatList

示例：

https://www.sign.cash/api/apip11/v1/getHatList


### 请求参数

通用请求参数参见[通用请求参数](#通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX当前有效的链上HAT信息，按交易时间戳倒序，获取第1批20条。

```
{
	"requesrer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"timestamp": 1635513688254,
	"url": "https://www.sign.cash/api/apip11/v1/getHatList",
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

|name|type|description|key in FEIP28|
|:---|:---|:---|:---|
|address|string|fch地址，本HAT文件所有者||
|active|bool|标记同一srcFid的系列最新的条目（含所有分片）为1，其他为0||
|fid|string|本HAT文件的双sha256值|data.fid|
|srcFid|string|源HAT文件哈希，双Sha256，本文件为源版时等于fid|data.srcFid|
|preVerFid|string|前版哈希，双Sha256，本文件为源版时为空|data.preVerFid|
|firstPartFid|string|首哈希，双sha256，无分片为空|data.firstPartFid|
|prePartFid|string|前片哈希，双sha256，无分片为空|data.prePartFid|
|algo|string|加密算法，空为未加密，建议"ECC256k1-AES256CBC"|data.algo|
|pubKey|string|加密公钥,空为上链签名者公钥|data.pubKey|
|symKeyEncrypted|string|加密后的对称密钥|data.symKeyEncrypted|
|loca|string array|存放位置，url|data.loca|
|aid|string|上链HAT文件归属的应用的aid，aid of the app|data.aid|
|pid|string|上链HAT文件归属的协议的pid，pid of the protocol|data.pid|
|txid|string|The txid of this transaction|
|txTimestamp|timestamp|The timestamp of this transaction|
|blockHeight|uint|The height of the block where the transaction is written|

* 示例

响应查询FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX当前有效的链上HAT信息，按交易时间戳倒序，获取第1批20条。

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
			"address": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX",
			"active": 1,
			"fid": "1111111111111122222222222222",
			"srcFid": "0000000011222222222222222222222",
			"preVerFid": "0000000011222222222222222222222",
			"firstPartFid": "",
			"prePartFid": "",
			"algo": "",
			"pubKey": "0222222222222222222222222",
			"symKeyEncrypted": "",
			"loca": "http://www.cid.cash/file/02dssssssssssssssss,
			"aid": "2k32k323kkkkkkkkkkkkkkkkkkkkkkkk",
			"pid": "33333333333888888888888888888889",
			"txid": 1636526891228,
			"txTimestamp": 230000,
			"blockHeight": 1003034
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

Create:2022-03-07
Update:2021-05-01
PID:""
TXID:
```

# APIP11V1_Avatar(zh-CN)
---
## 目录

[关于APIP](#关于APIP)

[关于本协议](#关于本协议)

[getHatList](#getHatList)


---

```
Type:APIP
SerialNumber:11
ProtocolName:Avatar
Version:1
Description:定义文件管理的链上哈希属性表信息接口。
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

1. 本协议接口提供用于文件管理的哈希属性表（HAT）文件的链上信息。

2. 本协议接口的数据来源和相关规则遵循以下协议：

   - 《FEIP32_HAT》

3. 本协议接口数据采用JSON格式。

4. 示例数据：
`requester`：FEk41Kqjar45fLDriztUDTUkdki7mmcjWK

requester的公钥`pubKey`：030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a

requester的私钥`priKey`：L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8

requester获得的对称密钥`symKey`：d2c03bbc1ba1380eafc395374e8da61f92545a1aac5d30b0c19289a69bd34a09

服务方链上发布的`urlMainPart`: https://www.sign.cash/api/

接口url：https://www.sign.cash/api/apip11/v1/[接口名称]

## getHatList

获取链上当前有效的HAT信息。

### url

[服务方发布的urlMainPart]apip11/v1/getHatList

示例：

https://www.sign.cash/api/apip11/v1/getHatList


### 请求参数

通用请求参数参见[通用请求参数](#通用请求参数)。其中：

* query

响应参数`data.list`中各字段均可作为查询字段。

* sort

响应参数`data.list`中的数字类型字段均可作为排序字段。

* 示例

查询FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX当前有效的链上HAT信息，按交易时间戳倒序，获取第1批20条。

```
{
	"requesrer": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
	"timestamp": 1635513688254,
	"url": "https://www.sign.cash/api/apip11/v1/getHatList",
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

|name|type|description|key in FEIP28|
|:---|:---|:---|:---|
|address|string|fch地址，本HAT文件所有者||
|active|bool|标记同一srcFid的系列最新的条目（含所有分片）为1，其他为0||
|fid|string|本HAT文件的双sha256值|data.fid|
|srcFid|string|源HAT文件哈希，双Sha256，本文件为源版时等于fid|data.srcFid|
|preVerFid|string|前版哈希，双Sha256，本文件为源版时为空|data.preVerFid|
|firstPartFid|string|首哈希，双sha256，无分片为空|data.firstPartFid|
|prePartFid|string|前片哈希，双sha256，无分片为空|data.prePartFid|
|algo|string|加密算法，空为未加密，建议"ECC256k1-AES256CBC"|data.algo|
|pubKey|string|加密公钥,空为上链签名者公钥|data.pubKey|
|symKeyEncrypted|string|加密后的对称密钥|data.symKeyEncrypted|
|loca|string array|存放位置，url|data.loca|
|aid|string|上链HAT文件归属的应用的aid，aid of the app|data.aid|
|pid|string|上链HAT文件归属的协议的pid，pid of the protocol|data.pid|
|txid|string|The txid of this transaction|
|txTimestamp|timestamp|The timestamp of this transaction|
|blockHeight|uint|The height of the block where the transaction is written|

* 示例

响应查询FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX当前有效的链上HAT信息，按交易时间戳倒序，获取第1批20条。

```
{
	"code": 0,
	"message": "Success.",
	"data": {
		"list": [{
			"address": "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX",
			"active": 1,
			"fid": "1111111111111122222222222222",
			"srcFid": "0000000011222222222222222222222",
			"preVerFid": "0000000011222222222222222222222",
			"firstPartFid": "",
			"prePartFid": "",
			"algo": "",
			"pubKey": "0222222222222222222222222",
			"symKeyEncrypted": "",
			"loca": "http://www.cid.cash/file/02dssssssssssssssss,
			"aid": "2k32k323kkkkkkkkkkkkkkkkkkkkkkkk",
			"pid": "33333333333888888888888888888889",
			"txid": 1636526891228,
			"txTimestamp": 230000,
			"blockHeight": 1003034
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