```
Type:APIP
SerialNumber:13
ProtocolName:Secret
Version:1
Description:定义链上秘密查询接口。
Author:C_armX
Language:zh-CN
CreateDate: 2021-12-01
UpdateDate: 2023-05-22
```

# APIP13V1_Secret(zh-CN)

---
## 目录

[关于APIP](#关于APIP)

[主要共识](#主要共识)

[secrets](#secrets)

[secretsDeleted](#secretsDeleted)

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

1. 本协议接口提供链上保存的秘密（secret）。

2. 本协议接口的数据来源遵循以下协议:

   - 《FEIP17_Secret》

3. 本协议接口数据采用JSON格式。

4. 本协议所有接口采用POST方法。

5. 本协议的接口的`urlTail`为: `apip13/v1/<接口名称>`。

6. 各接口具体共识如下:

---

## secrets

### 说明

获取指定owner的有效的链上秘密。

### 默认排序

- birthHeight: desc
- secretId: asc

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
		...
	}
}
```
### data in response body

```
[
	{
		"secretId": <string. txid where the secret was added.>,
		"alg": <string. encrypting algorithm of the ciphertext.> ",
		"ciphertext": <string. encrypted details of the secret.>,
		"owner": <string. the owner of this secret.>
		"birthTime": <long. the timestamp of the block in which the group was published> ,
		"birthHeight": <long. the height of the block in which the group was published>,
		"lastHeight": <long. the height of the block in which the group was operated most recently except rating>,
		"active": <boolean. is the secret active?>
	}
]

```
### 示例

- request body

```json
{
   "url": "http://localhost:8080/APIP/apip13/v1/secrets",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "query":{
         "terms": {
            "fields": ["owner"],
            "values":["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"]
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
   "balance": 35544728799,
   "got": 1,
   "total": 11,
   "bestHeight": 1756633,
   "data": [
      {
         "secretId": "6234623ab3befbbca0610a1a8c2821665a4734c6db40c2504256d672f446dd3f",
         "alg": "ECC256k1-AES256CBC",
         "ciphertext": "Ap1G/crcuoG2WHpSoXIJ88y3pIKfXFSANe5Ht0BURS8NEf8iho8vn7ILXXz3dFWGqQ1dJCMoa5CceJSrmd7hDIKHQO11Xs7s4Fzkdgx/8mCWX/tiLWY24IQpw5csomwfcnINa6V4O0pfUeN0c+9uacLJB38moFz3jX3ameUvVFBEVRZBdPjfOyenmLjPTeq4XhcnB9Lyw4CyzHEtKSZjUcI2UmVhKywA3A9Iwqn+fZ4mczO0/GwoJhsZC+3gmUqKkB4oik1351eSmS9/jeUGioA=",
         "owner": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
         "birthTime": 1684734246,
         "birthHeight": 1756255,
         "lastHeight": 1756255,
         "active": true
      }
   ],
   "last": [
      "1756255",
      "6234623ab3befbbca0610a1a8c2821665a4734c6db40c2504256d672f446dd3f"
   ]
}
```

## secretsDeleted
### 说明

获取指定owner的已删除的链上秘密。

### 默认排序

- lastHeight: desc
- secretId: asc

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
		...
	}
}
```
### data in response body

```
[
	{
		"secretId": <string. txid where the secret was added.>,
		"alg": <string. encrypting algorithm of the ciphertext.> ",
		"ciphertext": <string. encrypted details of the secret.>,
		"owner": <string. the owner of this secret.>
		"birthTime": <long. the timestamp of the block in which the group was published> ,
		"birthHeight": <long. the height of the block in which the group was published>,
		"lastHeight": <long. the height of the block in which the group was operated most recently except rating>,
		"active": <boolean. is the secret active?>
	}
]

```
### 示例

- request body

```json
{
   "url": "http://localhost:8080/APIP/apip13/v1/secretsDeleted",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "query":{
         "terms": {
            "fields": ["owner"],
            "values":["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"]
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
   "balance": 35544528799,
   "got": 1,
   "total": 1,
   "bestHeight": 1756633,
   "data": [
      {
         "secretId": "fa5c2ca808fb6fabddd6d9b58f0c1467920e5ce5b25da59a26b1b77a33320eed",
         "alg": "ECC256k1-AES256CBC",
         "ciphertext": "A5YX+a/UTWL4/MPBhyOeCdkJLKmzShDZPDJOYQiQVanZ+8l3a/AdusX96V/zjboIA2hOHL2LMzyywRuJsYAi+qhf17Bt45xzVJC0mlKuQl7bi8LSCbOvu6SzErtOxefqI0TcBY/aktlNYgPcYF6t4WdKs6OgLF+7s7+78nvqV87KxsOBNbxKy2CXdIuWqky+ttcHwcZDag0573SdeAaeVNY=",
         "owner": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
         "birthTime": 1619135477,
         "birthHeight": 685746,
         "lastHeight": 1756274,
         "active": false
      }
   ],
   "last": [
      "1756274",
      "fa5c2ca808fb6fabddd6d9b58f0c1467920e5ce5b25da59a26b1b77a33320eed"
   ]
}
```
