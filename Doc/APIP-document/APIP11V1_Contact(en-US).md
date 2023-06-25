```
Type:APIP
SerialNumber:11
ProtocolName:Contact
Version:1
Description:定义链上联系人信息查询接口。
Author:C_armX
Language:zh-CN
CreateDate: 2021-12-03
UpdateDate: 2023-05-22
```

# APIP11V1_Contact(zh-CN)

---
## 目录

[关于APIP](#关于APIP)

[主要共识](#主要共识)

[contacts](#contacts)

[contactsDeleted](#contactsDeleted)

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

1. 本协议接口提供链上注册的联系人（contact）的相关信息。

2. 本协议接口的数据来源遵循以下协议: 

   - 《FEIP12_Contact》

3. 本协议接口数据采用JSON格式。

4. 本协议所有接口采用POST方法。

5. 本协议的接口的`urlTail`为: `apip11/v1/<接口名称>`。

6. 各接口具体共识如下: 

---

## contacts

### 说明

获取指定owner的联系人信息。
  
### 默认排序

  - birthHeight: desc
  - contactId: asc

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
                "values":<string array. address of the owner. 必填>
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
		"contactId": <string. txid where the contact was added.>,
		"alg": <string. encrypting algorithm of the cipher.> ",
		"cipher": <string. encrypted details of the contact.>,
		"owner": <string. the owner of this contact.>
		"birthTime": <long. the timestamp of the block in which the group was published> ,
		"birthHeight": <long. the height of the block in which the group was published>,
		"lastHeight": <long. the height of the block in which the group was operated most recently except rating>,
		"active": <boolean. is the contact active?>
	}
]

```
### 示例

  - request body
  
```json
{
   "url": "https://cid.cash/APIP/apip11/v1/contacts",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "query":{
         "terms": {
            "fields": ["owner"],
            "values":["FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv"]
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
   "balance": 35524528799,
   "got": 1,
   "total": 2,
   "bestHeight": 1769615,
   "data": [
      {
         "contactId": "31b81c6d3d6015a8d53add5ce1cb97095db849ce789f39e2fe5c53ae1584c377",
         "alg": "ECC256k1-AES256CBC",
         "cipher": "AtrFmWIFjVgOAiJV9ecB0V8vpWeGbc8nZwmJUFjan5zfnLQWLl0NH5Sjh/qWBU849x8yTpn7v6V0Hgdm2zuNGk4flfU2wyYBG2sRFlmagSLZNJQ8T/meD3FX3EGXken+bbG9P6MmSWqWZsAqnx/MtIu/ngXy/+TB6UyyvH3/e1rvPzqfrNKpzVRpcfycUFSlHmm4xU15DA/SZu01PYDUI+AR/x2poKftABu7CxQinEp8bWBARYiDkvsplJLl7h+RJDtg5UgZZlAqG03GdgvmDEWkhHDuYrCKbWpoCILeEilW",
         "owner": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
         "birthTime": 1684728368,
         "birthHeight": 1756179,
         "lastHeight": 1756179,
         "active": true
      }
   ],
   "last": [
      "1756179",
      "31b81c6d3d6015a8d53add5ce1cb97095db849ce789f39e2fe5c53ae1584c377"
   ]
}
```

## contactsDeleted
### 说明

获取指定owner的已删除的联系人信息。
  
### 默认排序

  - lastHeight: desc
  - contactId: asc

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
                "values":<string array. address of the owner. 必填>
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
		"contactId": <string. txid where the contact was added.>,
		"alg": <string. encrypting algorithm of the cipher.> ",
		"cipher": <string. encrypted details of the contact.>,
		"owner": <string. the owner of this contact.>
		"birthTime": <long. the timestamp of the block in which the group was published> ,
		"birthHeight": <long. the height of the block in which the group was published>,
		"lastHeight": <long. the height of the block in which the group was operated most recently except rating>,
		"active": <boolean. is the contact active?>
	}
]

```
### 示例

  - request body
  
```json
{
   "url": "https://cid.cash/APIP/apip11/v1/contactsDeleted",
   "time": 1677673821267,
   "nonce": 1987697,
   "fcdsl":{
      "query":{
         "terms": {
            "fields": ["owner"],
            "values":["FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv"]
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
   "balance": 35524428799,
   "got": 1,
   "total": 1,
   "bestHeight": 1769615,
   "data": [
      {
         "contactId": "009c74b692e7792dea9c6b943e6e46731bb30bf81c5f6399d79c2d43237b60b2",
         "alg": "ECC256k1-AES256CBC",
         "cipher": "AtrFmWIFjVgOAiJV9ecB0V8vpWeGbc8nZwmJUFjan5zfnLQWLl0NH5Sjh/qWBU849x8yTpn7v6V0Hgdm2zuNGk4flfU2wyYBG2sRFlmagSLZNJQ8T/meD3FX3EGXken+bbG9P6MmSWqWZsAqnx/MtIu/ngXy/+TB6UyyvH3/e1rvPzqfrNKpzVRpcfycUFSlHmm4xU15DA/SZu01PYDUI+AR/x2poKftABu7CxQinEp8bWBARYiDkvsplJLl7h+RJDtg5UgZZlAqG03GdgvmDEWkhHDuYrCKbWpoCILeEilW",
         "owner": "FPL44YJRwPdd2ipziFvqq6y2tw4VnVvpAv",
         "birthTime": 1684723889,
         "birthHeight": 1756084,
         "lastHeight": 1756186,
         "active": false
      }
   ],
   "last": [
      "1756186",
      "009c74b692e7792dea9c6b943e6e46731bb30bf81c5f6399d79c2d43237b60b2"
   ]
}
```