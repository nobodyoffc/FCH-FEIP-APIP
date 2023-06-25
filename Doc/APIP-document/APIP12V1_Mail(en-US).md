```
Type:APIP
SerialNumber:12
ProtocolName:Mail
Version:1
Description:定义链上加密邮件相关接口。
Author:C_armX
Language:zh-CN
CreateDate: 2021-12-03
UpdateDate: 2023-05-22
```

# APIP8V1_Mail(zh-CN)

---
## 目录

[关于APIP](#关于APIP)

[主要共识](#主要共识)

[mails](#mails)

[mailsDeleted](#mailsDeleted)

[mailThread](#mailThread)

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

1. 本协议接口提供链上加密邮件（mail）的相关信息。

2. 本协议接口的数据来源遵循以下协议:

   - 《FEIP7_Mail》

3. 本协议接口数据采用JSON格式。

4. 本协议所有接口采用POST方法。

5. 本协议的接口的`urlTail`为: `apip12/v1/<接口名称>`。

6. 各接口具体共识如下:

---

## mails

### 说明

获取指定address的邮件信息。

### 默认排序

- birthHeight: desc
- mailId: asc

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "query":{
            "terms": {
                "fields": ["sender","recipient"],
                "values":<string. the address. 必填>
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
		"mailId": <string. txid where the mail was added.>,
		"alg": <string. encrypting algorithm of the cipher.> ",
		"cipherSend": <string. mail encrypted with the public key of the sender.>,
		"cipherReci": <string. mail encrypted with the public key of the recipient.>,
		"sender": <string. the sender of this mail.>
		"recipient": <string. the recipient of this mail.>
		"birthTime": <long. the timestamp of the block in which the group was published> ,
		"birthHeight": <long. the height of the block in which the group was published>,
		"lastHeight": <long. the height of the block in which the group was operated most recently except rating>,
		"active": <boolean. is the mail active?>
	}
]

```
### 示例

- request body

```json
{
   "url": "http://localhost:8080/APIP/apip13/v1/mails",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "query":{
         "terms": {
            "fields": ["sender","recipient"],
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
   "balance": 35524328799,
   "got": 1,
   "total": 7,
   "bestHeight": 1769615,
   "data": [
      {
         "mailId": "e3e69cb370d08b34bd02cfe48a3a5c7ee006211f2841ff495e314343b0769336",
         "alg": "ECC256k1-AES256CBC",
         "cipherSend": "AtpfHV/b+P2TMG8eFYpwdR7FCOhRjsK5I9UiZBIwUBtBAzi+B/tS5lMv27T9ofORhmSTYRYEfMIaffEwkwtfgEdormE5u4YKnBf001bWPeIQIXEz8HAj7XVt1rYLciJPdQ==",
         "cipherReci": "Avmexwg3OUjOPl7KyAcu1VcAYy1ln90DmqLH/POOPtwDD6PhiitBd/aRjRBZR9Tx9khysbQLHXKFL4BJ6Af0J6iKiDLZXOfvlzdXNM+TzADj0ryOsX9vtVXPsiljS6PjFg==",
         "textId": "c269f3b03960a695e1e2e4ab451bd645c7134d3328a327c98c00ed102f76b451",
         "sender": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
         "recipient": "F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW",
         "birthTime": 1684747806,
         "birthHeight": 1756449,
         "lastHeight": 1756449,
         "active": true
      }
   ],
   "last": [
      "1756449",
      "e3e69cb370d08b34bd02cfe48a3a5c7ee006211f2841ff495e314343b0769336"
   ]
}
```

## mailsDeleted
### 说明

获取指定address的已删除的邮件信息。

### 默认排序

- lastHeight: desc
- mailId: asc

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "query":{
            "terms": {
                "fields": ["sender","recipient"],
                "values":<string. the address. 必填>
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
		"mailId": <string. txid where the mail was added.>,
		"alg": <string. encrypting algorithm of the cipher.> ",
		"cipherSend": <string. mail encrypted with the public key of the sender.>,
		"cipherReci": <string. mail encrypted with the public key of the recipient.>,
		"sender": <string. the sender of this mail.>
		"recipient": <string. the recipient of this mail.>
		"birthTime": <long. the timestamp of the block in which the group was published> ,
		"birthHeight": <long. the height of the block in which the group was published>,
		"lastHeight": <long. the height of the block in which the group was operated most recently except rating>,
		"active": <boolean. is the mail active?>
	}
]

```
### 示例

- request body

```json
{
   "url": "http://localhost:8080/APIP/apip13/v1/mailsDeleted",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "query":{
         "terms": {
            "fields": ["sender","recipient"],
            "values":["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"]
         }
      },
      "size":2
   }
}
```

- response body

```json
{
   "code": 0,
   "message": "Success.",
   "nonce": 1987697,
   "balance": 35524228799,
   "got": 1,
   "total": 1,
   "bestHeight": 1769616,
   "data": [
      {
         "mailId": "e51d9e87341bdb4ee497d2252dd1db075accf61df8dccd353ed518a558a066ac",
         "alg": "ECC256k1-AES256CBC",
         "cipherSend": "AtpfHV/b+P2TMG8eFYpwdR7FCOhRjsK5I9UiZBIwUBtBAzi+B/tS5lMv27T9ofORhmSTYRYEfMIaffEwkwtfgEdormE5u4YKnBf001bWPeIQIXEz8HAj7XVt1rYLciJPdQ==",
         "cipherReci": "Avmexwg3OUjOPl7KyAcu1VcAYy1ln90DmqLH/POOPtwDD6PhiitBd/aRjRBZR9Tx9khysbQLHXKFL4BJ6Af0J6iKiDLZXOfvlzdXNM+TzADj0ryOsX9vtVXPsiljS6PjFg==",
         "textId": "c269f3b03960a695e1e2e4ab451bd645c7134d3328a327c98c00ed102f76b451",
         "sender": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
         "recipient": "F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW",
         "birthTime": 1684747476,
         "birthHeight": 1756446,
         "lastHeight": 1756473,
         "active": false
      }
   ],
   "last": [
      "1756473",
      "e51d9e87341bdb4ee497d2252dd1db075accf61df8dccd353ed518a558a066ac"
   ]
}
```


## mailThread
### 说明

获取指定fid与另一指定fid的往来邮件列表。

### 默认排序

- lastHeight: desc
- mailId: asc

### Request body
```
{
	"url": <string. urlHead+urlTail. 必填>,
	"time": <long. 时间戳,精确到毫秒. 必填>,
	"nonce": <long. 随机数. 必填>,
    "fcdsl":{
        "query":{
            "terms": {
                "fields": ["sender","recipient"],
                "values":<string. fid 1. 必填>
            }
        }，
        "filter":{
            "terms": {
                "fields": ["sender","recipient"],
                "values":<string. fid 2. 必填>
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
		"mailId": <string. txid where the mail was added.>,
		"alg": <string. encrypting algorithm of the cipher.> ",
		"cipherSend": <string. mail encrypted with the public key of the sender.>,
		"cipherReci": <string. mail encrypted with the public key of the recipient.>,
		"sender": <string. the sender of this mail.>
		"recipient": <string. the recipient of this mail.>
		"birthTime": <long. the timestamp of the block in which the group was published> ,
		"birthHeight": <long. the height of the block in which the group was published>,
		"lastHeight": <long. the height of the block in which the group was operated most recently except rating>,
		"active": <boolean. is the mail active?>
	}
]

```
### 示例

- request body

```json
{
   "url": "http://localhost:8080/APIP/apip13/v1/mailThread",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "query":{
         "terms": {
            "fields": ["sender","recipient"],
            "values":["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"]
         }
      },
      "filter":{
         "terms": {
            "fields": ["sender","recipient"],
            "values":["F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW"]
         }
      },
      "size":2
   }
}
```

- response body

```json
{
   "code": 0,
   "message": "Success.",
   "nonce": 1987697,
   "balance": 35524128799,
   "got": 2,
   "total": 3,
   "bestHeight": 1769617,
   "data": [
      {
         "mailId": "e3e69cb370d08b34bd02cfe48a3a5c7ee006211f2841ff495e314343b0769336",
         "alg": "ECC256k1-AES256CBC",
         "cipherSend": "AtpfHV/b+P2TMG8eFYpwdR7FCOhRjsK5I9UiZBIwUBtBAzi+B/tS5lMv27T9ofORhmSTYRYEfMIaffEwkwtfgEdormE5u4YKnBf001bWPeIQIXEz8HAj7XVt1rYLciJPdQ==",
         "cipherReci": "Avmexwg3OUjOPl7KyAcu1VcAYy1ln90DmqLH/POOPtwDD6PhiitBd/aRjRBZR9Tx9khysbQLHXKFL4BJ6Af0J6iKiDLZXOfvlzdXNM+TzADj0ryOsX9vtVXPsiljS6PjFg==",
         "textId": "c269f3b03960a695e1e2e4ab451bd645c7134d3328a327c98c00ed102f76b451",
         "sender": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
         "recipient": "F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW",
         "birthTime": 1684747806,
         "birthHeight": 1756449,
         "lastHeight": 1756449,
         "active": true
      },
      {
         "mailId": "e51d9e87341bdb4ee497d2252dd1db075accf61df8dccd353ed518a558a066ac",
         "alg": "ECC256k1-AES256CBC",
         "cipherSend": "AtpfHV/b+P2TMG8eFYpwdR7FCOhRjsK5I9UiZBIwUBtBAzi+B/tS5lMv27T9ofORhmSTYRYEfMIaffEwkwtfgEdormE5u4YKnBf001bWPeIQIXEz8HAj7XVt1rYLciJPdQ==",
         "cipherReci": "Avmexwg3OUjOPl7KyAcu1VcAYy1ln90DmqLH/POOPtwDD6PhiitBd/aRjRBZR9Tx9khysbQLHXKFL4BJ6Af0J6iKiDLZXOfvlzdXNM+TzADj0ryOsX9vtVXPsiljS6PjFg==",
         "textId": "c269f3b03960a695e1e2e4ab451bd645c7134d3328a327c98c00ed102f76b451",
         "sender": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
         "recipient": "F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW",
         "birthTime": 1684747476,
         "birthHeight": 1756446,
         "lastHeight": 1756473,
         "active": false
      }
   ],
   "last": [
      "1756446",
      "e51d9e87341bdb4ee497d2252dd1db075accf61df8dccd353ed518a558a066ac"
   ]
}
```
