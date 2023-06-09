```
FEIP13: HAT
Version: 1
Language: en-US
Author: C_armX, Deisler-JJ_Sboy, master_wang
Status: draft
Created date: 2021-06-12
Last modified date：2022-12-22
PID: ""
TXID: 
```

# FEIP13V1_HAT(zh-CN)

---
## Contents

[Introduction](#introduction)

[General rules of FEIP type protocols](#general-rules-of-feip-type-protocols)

[Rules specific to this protocol](#rules-specific-to-this-protocol)

[哈希属性](#哈希属性)

[哈希属性表](#哈希属性表)

[哈希属性表数据](#哈希属性表数据)

[哈希属性表数据链上索引](#哈希属性表数据链上索引)

[使用逻辑](#使用逻辑)


---

## Introduction

```
Protocol type: FEIP
Serial number: 13
Protocol name: HAT
Version: 1
Description : 基于数据哈希对数据进行描述、识别、索引、组织和定位的共识。
Author: C_armX, Deisler-JJ_Sboy，master_wang
Language: en-US
Previous version PID:""
```
---
## General consensus of FEIP type protocols

1. Write important data in OP_RETURN for public witness under FEIP type protocols.

2. The SIGHASH flag of all transaction inputs is ‘ALL’ (value 0x01).

3. The max size of OP_RETURN : 4096 bytes.

4. The format of the data in op_return: JSON.

5. Encoding : utf-8.
---

## Consensus of this protocol

1. 本协议以数据的哈希作为数据身份（DID，Data Identity）描述该数据的主要属性，作为自由共识生态的数据管理共识。

2. 哈希属性（HA）：指一项数据的17项属性描述值，用于识别、索引、组织和定位特定数据。参见[哈希属性](#哈希属性)。

3. 哈希属性表（HAT）：指存放多个数据的哈希属性的列表。

4. 哈希属性表数据（HAT data）：指专门用于保存哈希属性表的数据。

5. 编码：哈希属性的值的编码为utf-8
---

## 哈希属性

数据的哈希属性（HA)是对数据的全面描述，分为6类20项属性，除了did之外，其他均可为空。

|分类|序号|字段名|中文名|类型|值的说明|
|:----|:----|:----:|:----|:----|:----|
|basic|1|did|数据身份|string|数据的Sha256值|
||2|name|数据名|string|数据全名，含后缀|
||3|type|类型|string|应用自定义，兼容传统后缀|
||4|aid|应用id|string|数据归属的应用的aid|
||5|pids|协议id数组|string array|数据遵循的协议的pid，可多个|
|extend|6|description|描述|string|用户自定义|
||7|size|数据大小|Int32|数据实际大小byte|
||8|firstTime|首获时间|time stamp|时间戳|
||9|lastTime|最后修改属性时间|time stamp|时间戳，哈希修改视为新数据|
|version|10|srcDid|源哈希|string|Sha256|
||11|preVerDid|前版哈希|string|Sha256|
|slice|12|firstPartDid|首片哈希|string|sha256|
||13|prePartDid|前片哈希|string|sha256|
||14|total|分片总数|Int16|2~65535|
|crypto|15|cryptoAlg|加密算法|string|空为未加密|
||16|pubKey|加密公钥|string|hex|
||17|symKeyCypher|加密的对称密钥|string||
|manage|18|rank|重要性|int|1~5|
||19|Status|状态|int|0删除，1活跃，2归档|
||20|locations|存放位置|string array|url|
---
## 哈希属性表

当管理多个数据时，这些数据的哈希属性集合为一个列表，即哈希属性表（Hash Attributes Table)。

哈希属性表采用Json的方式保存和解析。

---
## 哈希属性表文件

由多个数据的哈希属性组成的哈希属性表（HAT）可以保存在文件中，称为HAT文件，该文件按以下方式组织：

    1. 以加入{"meta":"FC"}字段的哈希属性的协议部分Json开始，参见下表
    2. 以哈希属性的data部分Json构成的哈希属性表（HAT）。
    3. 文件结束标志：EOF

|field number|field name|type|content|required|
|:----|:----|:----|:----|:----|
|1|meta|String|Fixed: "FC"|Y|
|2|type|String|Fixed: "FEIP"|Y|
|3|sn|int|Serial number<br>Fixed: 13|Y|
|4|ver|int|Fixed: 1|Y|
|5|name|String|Fixed: "HAT"|N|
|6|pid|hex|Double Sha256 value of this protocol file|N|

### Example of the content of a HAT file my.hat
```
{
    "meta":"FC",
    "type": "FEIP",
    "sn": 30,
    "ver": 1,
    "name": "HAT",
    "pid": ""
}
{
	"did": "113334433335252533322334353232332223",
	"name":"workfile.txt",
	"type":"txt",
	"srcDid": "3335535334343535353535535335353535",
	"preVerDid": "11111111111224444445555555555666666",
	"firstPartDid": "",
	"prePartDid": "",
	"alg": "ECC256k1-AES256CBC",
	"pubKey": "",
	"symKeyCypher": "",
	"loca": ["https://bbs.cash/topic/558/"],
	"aid": "33442224342134121344443443333333",
	"pids": ["88888888888888888833333333333333"]
}
{
	"did": "11333443333525253332234444444444444",
	"name":"resume.docx",
	"type":"docx",
	"srcDid": "333553533434353535353554444444444",
	"preVerDid": "111111111112244444455555333333333",
	"firstPartDid": "",
	"prePartDid": "",
	"alg": "ECC256k1-AES256CBC",
	"pubKey": "",
	"symKeyCypher": "",
	"loca": ["https://bbs.cash/topic/559/"],
	"aid": "3344222434213412134444444444444",
	"pids": ["8888888888888888883344444444444444"]
}
EOF
```
---
## 哈希属性表数据链上索引
   Hat数据自身的哈希属性上链，成为一个cid的数据管理入口
### 上链信息

|field number|field name|type|content|required|
|:----|:----|:----|:----|:----|
|1|type|String|Fixed: "FEIP"|Y|
|2|sn|int|Serial number<br>Fixed: 13|Y|
|3|ver|int|Fixed: 1|Y|
|4|name|String|Fixed: "HAT"|N|
|5|pid|hex|Sha256 value of this protocol file|N|
|6|data.hatId|string|首版本上链的交易id|Y|
|7|data.did|string|数据身份,本数据的双Sha256值|Y|
|8|data.preDid|string|前版哈希，双Sha256|N|
|11|data.cryptoAlg|string|加密算法，空为未加密，建议"ECC256k1-AES256CBC"|N|
|12|data.pubKey|string|加密公钥,空为上链签名者公钥|N|
|13|data.symKeyCypher|string|加密后的对称密钥|N|
|14|data.loca|string array|存放位置，url|N|
|15|data.aid|string|上链HAT数据归属的应用的aid，aid of the app|N|
|16|data.pids|string array|上链HAT数据归属的协议的pid列表，pids of protocols|N|

#### Example of add a HAT on Chain
```
{
    "type": "FEIP",
    "sn": 13,
    "ver": 1,
    "name": "HAT",
    "pids": "",
    "data":{
        "did": "113334433335252533322334353232313223",
        "srcDid": "3335535334343535353535535335353535",
        "preVerDid": "11111111111224444445555555555666666",
        "firstPartDid": "",
        "prePartDid": "",
        "cryptoAlg": "ECC256k1-AES256CBC",
        "pubKey": "",
        "symKeyEncrypted": "",
        "loca": ["https://bbs.cash/topic/558/"],
        "aid": "33442224342134121344443443333333",
        "pids": ["88888888888888888833333333333333"]
    }
}
```
### 删除系列HAT数据信息
指定源哈希，将以此为源头的系列链上HAT数据信息标注为删除。

|field number|field name|type|length|content|required|
|:----|:----|:----|:----|:----|:----|
|1|type|String|4|Fixed: "FEIP"|Y|
|2|sn|int|2|Serial number<br>Fixed: 13|Y|
|3|ver|int|1|Fixed: 1|Y|
|4|name|String|3|Fixed: "HAT"|N|
|5|pid|hex|32|Sha256 value of this protocol file|N|
|6|data.op|string array|3|操作："del"|Y|
|7|data.srcDid|string|32|源哈希，Sha256|Y|

#### Example to delete a HAT series

```
{
    "type": "FEIP",
    "sn": 13,
    "ver": 1,
    "name": "HAT",
    "pid": "",
    "data":{
        "op": "del",
        "srcDid": "3335535334343535353535535335353535"
    }
}
```
---
## 使用逻辑
   1. 运行：App在运行时，HAT在内存中；
   2. 保存：App非运行期间，HAT在设备本地保存；
   3. 备份：App根据用户设定，定期或不定期将HAT数据加密备份到分布式存储系统；
   4. 上链：HAT数据的关键哈希属性保存在链上，成为获得和管理数据的入口。
