# APIP1V1_OpenAPI(zh-CN)

## 目录


[关于APIP](#关于APIP)

[APIP的发布](#APIP的发布)

[接口标识](#接口标识)

[接口url的构成](#接口url的构成)

[时间戳与密码算法](#时间戳与密码算法)

[示例身份信息](#示例身份信息)

[API服务流程](#API服务流程)

[谁是API的请求方](#谁是API的请求方)

[如何购买API服务](#如何购买API服务)

[connect接口](#connect接口)

[响应状态](#响应状态)

[数据请求接口](#数据请求接口)

[APIP简易查询语法](#APIP简易查询语法)

[general通用接口](#general通用接口)

---

```
Type: APIP
SerialNumber: 1
ProtocolName: OpenAPI
Version: 1
Description : 定义在Freecash主链上发布开放API协议的一般方式.
Author: C_armX,Write_cash
Language: zh-CN
CreateDate: 2021-10-30
UpdateDate: 2023-02-01
```

---

## 关于APIP

`APIP`(Application Programming Interface Protocols)是自由共识生态协议的一种类型，用于创建和发布开放的API文档，供API服务方开发部署通用API服务，以实现数据服务的去中心化。API需求方可以按照APIP协议从遵循该协议的任何一个API服务方那里获取数据服务。

---

## APIP协议的发布

APIP协议与自由共识生态的其他自由协议一样，由`协议作者`按照《FEIP1_Protocol》协议在链上发布注册，注册交易的OP_RETURN内容为：

```
{
    "type": "FEIP"
    "sn": 1,
    "ver": 7,
    "name": "Protocol",
    "pid": <string, FEIP1协议ID，非必填>,
    "did": <string, FEIP1协议文本的did，非必填>,
    "data": {
        "op": "publish",
        "type": "APIP",
        "sn": <string,协议编号，必填>,
        "ver": <string,协议版本号，必填>,
        "name": <string，协议名称，必填>,
        "desc": <string,对协议的描述>,
        "authors": <string array, 作者们的fch地址>,
        "waiters": <string array, 发布者为该协议指定的联系人fch地址>,
        "did": <string, 本协议文本的did，即sha256x2值>,
        "lang": <string，语言，基于i18n编码>,
        "preDid": <string，前一版本协议文本的pid>,
        "fileUrls": <string array, 协议文本存放位置>
    }
}
```

自由协议的更新、注销和评价参见《FEIP1_Protocol》。

---
## 接口标识

1. 通用标识

`<接口名称>@pid#<所属APIP协议的PID>`

* 接口名称中不得使用'@'或'#'

2. 简化标识

`<接口名称>@<协议类型+协议编号+V+协议版本号>`

简化标识不一定唯一，主要便于人类记忆和识别，重要场景或机器识别应采用唯一标识。

3. 示例：

APIP2的balance接口的通用标识为：`balance@pid#0000000000000000000000000000000`

简化标识为：`balance@APIP2V1`。

---
## 接口url的构成

接口的url分为两部分：`urlHead`和`urlTail`。

1. urlHead

`urlHead`为某API服务方提供的所有接口url的共同部分，由服务方依据《FEIP29_Service》在`data.params.urlHead`字段中发布在链上。格式为：

`<scheme 协议类型，如http、https、ws等>://<domain or IP>:<port>/<subdirectory>/`
* 示例
`http://localhost:8080/APIP/`


2. urlTail

`urlTail`是该API服务方按照APIP协议，在链上发布API服务时声明的具体接口的具体位置。协议格式为：

`apip<协议编号>/v<版本号>/<接口名称>`

* 示例：
`apip1/v1/connect`

3. 完整url

接口的完整url = urlHead + urlTail

* 示例：
`http://localhost:8080/APIP/apip1/v1/connect`

---
## 时间戳与密码算法

### 时间戳格式

1. APIP协议涉及的时间戳，若无特定说明，默认为13位，`精确到毫秒`。
2. 来自Freecash区块数据的时间戳为10位，`精确到秒`。

---

### 哈希算法

无特殊说明，哈希算法采用两次sha256算法，即:`sha256(sha256(byte[] message_bytes))`，简写为`sha256x2(msg)`。哈希值为Hex格式，字母小写.
* 示例：
bitcoin或freecash 的任何区块哈希（block id）和交易哈希（txid）。

### 对称签名算法
采用对数据(data)后拼接32字节对称密钥（symKey)后，做两次sha256哈希得到哈希值，即签名(sign)。
* 示例：
```
data = {"name":"test"}
symKey = 7904517bd0c5646aeb861b1475bc4d7801a156b9950d0fadaa3b2196c7cd4c08
sign = de655ec6cb95f61ca38f377a57e7baaef1ef32c711794e60a4cda91787589818
```
### 对称加密算法
采用《FIPA8V1_AES256CBC对称密码算法(zh-CN)》，pid#bdaef41f270b0f71d3145396b459a7563bb349150fce7d882ce7a0c66e830d02，did#7ba861e13d49ec6466a27a8fceb0513fb34a36203d11ea16a8ebaad8e09df1e4。
* 示例:
```
plain text = {"data":"test"}
symKey(hex) = 7904517bd0c5646aeb861b1475bc4d7801a156b9950d0fadaa3b2196c7cd4c08
ciphertext(Base64) = qmlLu07UZb7lnzWC4F9Yrg==
```

### 非对称密码签名
采用Bitcoin-Qt提供的椭圆曲线算法（ECDSA），以签发者私钥对数据进行签名。
* 示例：
```
pubKey(hex) = 030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a
message = {"data":"test"}
sign(Base64) = IMNLeiyEj2JA6nU04Tj/7rQoSokP2r+Ber5S3bXhsXJjc8uqgNnagwpBadJx45LFWd+9kKKgjP6/WmeDbckqXCw=
```

### 非对称加密算法：
采用《FIPA8V1_AES256CBC对称密码算法(zh-CN)》，pid#42b14a9a58a146ed8473a8badd53df2f2639feed5adc55643e0fbae8b1d75b77，did#360969090ee6951c7dde879062b9c3a39fa965474954b88be1b1ec85b10f3031。
* 示例：
```
priKey(hex) = a048f6c843f92bfe036057f7fc2bf2c27353c624cf7ad97e98ed41432f700575
message = {"data":"test"}
ciphertext(Base64) = yC8rQtc6FugEwJVoiDGYVB5I2N6qZjDYlw+TREWl9WRBeCF3y+S3ka0GijIKjZFMDn7k5in03lbUJ1lnXi8XA8/R5iqQ8jgKZnEfl6m+BOuyPsWxF2Quvay9hz9CENI8q2NahkQX6IBKrLTJ8SBVOg==
```
---
## 示例身份信息

APIP协议默认采用以下请求方身份信息:

`requester`：FEk41Kqjar45fLDriztUDTUkdki7mmcjWK

requester的公钥`pubKey`：030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a

requester的私钥`priKey`：L2bHRej6Fxxipvb4TiR5bu1rkT3tRp8yWEsUy4R1Zb8VMm2x7sd8

requester获得的对称密钥`symKey`（包括`sessionKey`）：7904517bd0c5646aeb861b1475bc4d7801a156b9950d0fadaa3b2196c7cd4c08

---

## API服务流程

按照APIP协议，`服务方`（API服务的提供者，简称APISP）为`请求方`（API服务的使用者，APP或用户个人）提供API服务的一般流程为：

1. 服务方 开发部署API服务

服务方选择一个或多个链上注册的APIP协议，开发和部署相应的API服务。

2. 服务方 链上注册API服务

服务方依据《FEIP29_Service》协议，在链上发布自己的API服务，并给出订购方式、API的urlHead。

发布者FCH地址为交易的第一个输入，发布交易的OP_RETURN内容为：

```
{
    "type": "FEIP",
    "sn": 5,
    "ver": 2,
    "name": "service",
    "pid": <string, FEIP29协议的pid，该协议正式发布前暂时留空，非必填>,
    "did": <string, FEIP29V2协议文本的did，该协议正式发布前暂时留空，非必填>,
    "data":{
        "op": "publish",
        "stdName": <string，服务的英文名>,
        "localName": <string array，服务的其他语言名称>,
        "desc": <string, 对服务的描述>,
        "type": <string array, 服务的分类，“APIP”为必填，可多个>
        "protocols": <string array, 该服务所遵循的协议>
        "urls": <string array, 该服务的相关资源链接>
        "waiters": <string,指定的客服>,
        "params":{
            "urlHead": <string, API访问位置的共同部分，如http://sign.cash/api/>,
            "account":<string,收款账号，缺省为发布者FCH'地址>,
            "currency": <string，支付的币种，缺省为FCH，法币则遵循ISO4217，如'CNY'>,
            "pricePerKBytes": <string, 对response body每1024字节收取的费用。计价方式二选一，优先采用>,
            "pricePerRequest": <string, 每次请求的服务收费，采用所选货币的标准单位。计价方式二选一>,
            "minPayment": <string, 最低单次购买金额，采用所选货币的标准单位>
            "sessionDays": <string, session key的有效整数天数>
    }
}
```

3. 请求方 链上查询API服务

请求方在链上查询自己所需要的API服务方，获得订购方式（data.params）和API路径（urlHead）。

4. 请求方 订购API服务

请求方按照订购方式，向服务方订购API服务。服务方记录请求方的身份（FCH地址）和充值余额，开始服务。

5. 请求方 发出连接服务请求

请求方通过connect接口，向服务方发起包含自身pubKey的连接请求和私钥签名，以获得后续数据请求使用的对称密钥`sessionKey`，详情参见<##connect接口>(##connect接口）。

6. 服务方 验证请求分配密钥

服务方对请求进行安全验证后，发放用请求方pubKey加密的对称密钥sessionKey。具有相同urlHead的接口共用同一个sessionKey。

7. 请求方 解密保存sessionKey

请求方用自己的私钥priKey解密密文，获得sessionKey，保存用于后续数据请求。 

1-7步骤的流程图：

![APIP1_OpenAPI插图 连接.png](/assets/uploads/files/1648720525053-apip1_openapi插图-连接-resized.png) 

8. 请求方 发出数据服务请求

请求方根据自身数据需求，选择服务方的对应接口，构造API的url和请求参数并用`sessionKey`签名后，向服务方发出数据请求。详情参见[##数据服务接口](##数据服务接口）

9. 服务方 验证请求返回数据

服务方收到请求后，进行安全验证无误后，返回响应数据和签名给请求方。

10. 请求方 验证签名接受数据

请求方收到响应数据后，验证签名是否无误则接受数据，服务完成。

8-10的流程图：

![APIP1_OpenAPI插图 数据.png](/assets/uploads/files/1648720532310-apip1_openapi插图-数据-resized.png) 
---

## 谁是API的请求方

APP有两种`请求方`设定逻辑：

### 1.用户作为请求方

* 1）用户通过APP或其他途径用自己的FCH地址购买服务。

* 2）用户进入APP需要获取数据时，用自己的私钥签名发起connect连接请求。

* 3）APP为用户在本地保存sessionKey，用于向API服务方发起数据请求。

* 4）特点：符合密码经济中“用户掌握数据”的安全准则，减少对APP的依赖，更加安全、开放、去中心化。

### 2.APP作为请求方

* 1）APP发布者用自己的地址向API服务方购买服务，获得sessionKey。

* 2）用户使用APP时，所需要的数据由APP向API服务方提出请求。

* 3）特点：符合传统开发习惯，便于APP运营者掌控数据，但是有中心化风险。


---

## 如何购买API服务

请求方购买API服务的方式由服务方在链上注册服务时，在`data.desc`字段描述，在`data.params`字段给出相关参数。服务方未特殊声明，则默认采用以下方式：

1. 服务方链上注册服务时在data.params中声明收费信息

```
{
	"urlHead": <string, API访问位置的共同部分，如http://sign.cash/api/>,
	"account":<string,收款账号，缺省为发布者FCH'地址>,
	"currency": <string，支付的币种，缺省为FCH，法币则遵循ISO4217，如'CNY'>,
    "pricePerKBytes": <string, 对response body每1024字节收取的费用。计价方式二选一，优先采用。>,
	"pricePerRequest": <string, 每次请求的服务收费，采用所选货币的标准单位。计价方式二选一>,
	"minPayment": <string, 最低单次购买金额，采用所选货币的标准单位>
	"sessionDays": <string, session key的有效整数天数>
}
```
* 以上为基本参数，服务方可制定不同的商业策略，构造自定义参数和处理逻辑。
* 采用"pricePerRequest"参数时，服务方可以对不同的API请求制定阶梯价格，对各API设置倍数参数"nPrice"，如对某API的每个成功请求收取 nPrice * pricePerRequest 的费用。

2. 请求方链上获取收费信息的两种方式

   - 1）通过APP获取。APP提供API服务方及价格列表，供请求方选择购买服务。

   - 2）请求方自行查找链上API服务方，获取API服务的收费信息。

3. 请求方链上购买服务的两种方式

   - 1）通过APP购买。APP在请求方同意购买，并填写或选择支付金额后，为请求方构造包含`购买标记`的交易，并可在"via"字段填入App的aid，由请求方确认后发送交易。

   - 2) 请求方自行构造包含`购买标记`的交易完成支付。

4. 请求方的`购买标记`，即购买交易的OP_RETURN内容，如下：

```
{
	"type": "APIP",
	"sn": "1",
	"ver": "1",
	"name": "OpenAPI",
	"data": {
		"op": "buy",
		"sid": <string，所购买服务的sid，必填>,
        "via":<string array, 购买渠道，如app的aid，非必填>
	}
}
```

5. API服务方启动服务

   - API服务方收到有购买标记的交易后，自动登记第一个输入的FCH地址为用户ID，并登记余额，开始服务。

   - 请求方的每次请求按`pricePerRequest`，`pricePerKBytes`, `nPrice`计减余额。

   - 余额一旦小于等于0则服务终止，删除该用户信息，以防止用户信息累计过多，并防止与此相关的攻击。

---

## connect接口

connect接口用来向服务方申请获得sessionKey，sessionKey用于后续数据请求的签名和验证。

### 1. 接口名称

connect

---

### 2. 请求方法

POST

---

### 3. 请求URL

url = urlHead + urlTail

= <服务方链上注册服务时的`data.params.urlHead`值> + "apip1/v1/connect"

示例:
```
data.params.urlHead = "http://localhost:8080/APIP/"
url: "http://localhost:8080/APIP/apip1/v1/connect"
```

---
### 4. 请求头部
|属性|类型|说明|必填|
|:---|:---|:---|:---|
|pubKey|hex|请求方的公钥，用于验证身份和授权|Y|
|sign|Base64|用请求方私钥priKey对请求参数的签名，防止身份伪造|Y|

### 5. 请求参数

|参数|类型|说明|必填|
|:---|:---|:---|:---|
|url|string|当前connect的url，防止重放攻击|Y|
|time|time stamp|精确到毫秒的时间戳，防止重放攻击|Y|
|nonce|long|随机数|Y|


### 6. 示例
请求头部：
```
	PubKey = 030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a
	Sign = IEOuhyK4D7o3xeo/7Ec40ioPnPxHBgrc/dIkOhEKuJs5WeeuQ4HwiB3Hu6cpb10dyOzjV1VnVnR9DfHRzP78fPo\u003d
```

请求参数
```
{"url":"http://localhost:8080/APIP/apip1/v1/connect","time":1677673821267,"nonce":838312}
```

### 请求方保存API服务信息

请求方保存API服务相关信息：

|名称| 类型           | 说明                                              |必填|
|:---|:-------------|:------------------------------------------------|:---|
|sid| string       | 接口服务的SID，即服务方在链上注册服务时的txid                      |Y|
|sessionKey| string       | 当前生效的sessionKey，相同urlHead的各接口使用同一个sessionKey    |N|
|sessionName| string       | sessionKey的双sha256哈希值的后6字节base64编码，请求时代表请求方     |N|
|sessionDays| int          | 当前生效的sessionKey的有效天数                            |N|
|startTime| long   | 当前生效的sessionKey获得的时间戳,单位毫秒                       |N|
|balance| long         | 请求方当前服务余额                                       |all|

---

### 5. 响应

* 验证请求。服务方对请求依次主要验证：

   - 1) 验证用户。用请求头部Pubkey的值计算requester的FCH地址，验证是否在有效用户列表中。不在列表返回1001.

   - 2) 验证请求对象。验证签名内容中的“url”值是否与服务方当前接口的url相同，防止重放攻击。不同则返回1005.

   - 3) 验证时间戳。验证当前时间戳减time的绝对值是否小于服务方设定的windowTime。过期返回1006.

   - 4) 验证nonce。验证nonce值是否在windowTime内已经使用过。已使用过返回1007.

   - 5) 验证签名。用pubKey验证签名sign是否正确，防止身份伪造。失败返回1008。

* 创建session和用户信息

服务端验证connect接口请求后：
   - 1) 生成32字节随机值为`sessionKey`（hex），相同urlHead的各接口使用同一个sessionKey；

   - 2) 取sessionKey的前12字符（即6字节）为`sessionName`；

   - 3) 按服务公布的`sessionDays`设定`sessionKey`和`sessionName`的`expireTime`，到期删除该session，用户需重新connect获取；

* 保存用户信息
服务端对每个用户保存以下信息：

|名称|类型| 说明                                                      |
|:---|:---|:--------------------------------------------------------|
|addr|string| 用户的FCH地址，即ID，应与充值FCH地址、connect请求参数中pubKey计算的FCH地址相同。    |
|balance|long| 用户余额，余额小于pricePerRequest值，则删除该用户所有信息，充值后重建用户信息          |
|pubKey|string| 用户的公钥，connect请求时获取，用于验证connect请求的签名。若对应FCH地址未保存，则视为新用户。 |
|sessionKey|string| 该用户当前的sessionKey                                        |
|sessionName|string| sessionKey的双sha256哈希值的后6字节base64编码，请求时代表请求方             |
|expireTime|long| 当前session的到期时间                                          |

* 响应头部

| 属性   | 类型     | 说明                  | 必填 |
|:-----|:-------|:--------------------|:---|
| Code | string | 响应参数code值转为string类型 | Y  |

* 响应参数

|参数|类型|说明|
|:---|:---|:---|
|code|int|响应状态码|
|message|string|响应状态描述|
|balance|int64|请求方当前服务余额|
|data.sessionKeyEncrypted|string|sessionKey的密文|
|data.sessionDays|string|sessionKey的有效天数|

响应码相关信息参见[响应码](#响应码)

* 示例

设服务方验证请求无误后，为该请求方分配`sessionKey`为："d2c03bbc1ba1380eafc395374e8da61f92545a1aac5d30b0c19289a69bd34a09"。

请求方公钥`pubKey`为："030be1d7e633feb2338a74a860e76d893bac525f35a5813cb7b21e27ba1bc8312a"。
  - 响应头部
```
    Code = 0
```
  - 响应参数
```
{
	"code": 0,
	"message": "Success.",
	"balance": 2000,
	"data": {
		"sessionKeyEncrypted": "A+wCC1gjAoWiF+it5xnE668eoJHHMnF5UpjI2fxKQeuJyOWAiFsl5RPX0HEIEPm5ygmOifwBpZ2Yh3e5NyH2BlDEPaYPFoUZqdedBTRWoVHR48hKFN088dZYX+/6f0w+jhOsjxylXlxdwe6p/kwXNIuQ3iEVPz9cyVGNdFm8vVMQkHtbZlIdDjj0L13CCOuIJnByjNBMsPbP4qCnNSunuAIV91z2XAGlofGrIozGA3AY",
		"sessionDays": 365
	}
}

```
---

## 响应状态

|code|message|说明|data|
|:---|:---|:---|:---|
|0|Success.|请求成功.返回数据|object|
|1000|Miss sign in request header.|未在请求头部找到签名Sign||
|1001|Miss pubKey in request header.|未在请求头部找到公钥PubKey||
|1002|Miss sessionName in request header.|未在请求头部找到SessionName||
|1003|Miss request body.|请求体缺失||
|1004|Insufficient balance, please purchase service.|余额不足，请购买服务||
|1005|The request URL isn't the same as the one you signed.|请求的URL与签名的URL不一致|requestedURL, signedURL|
|1006|Request expired.|请求已过期|windowTime|
|1007|Nonce had been used.|Nonce已被使用过||
|1008|Failed to verify signature.|验证签名失败||
|1009|NO such sessionName or it was expired, please connect again.|SessionName不存在或已过期，请重新连接。||
|1010|Too much data to be requested.|超过最大请求条目数|maxSize|
|1011|No data meeting the conditions.|未发现符合条件的条目||
|1012|Bad query. Check your request body referring related APIP document.|查询有误，请参考相关APIP协议文本检查你的查询||
|1013|Bad request. Please check request body.|请求错误，请检查你的请求体||
|1020|Other error，please contact the service provider.|其他错误，请联系服务商||
|2000~||API服务商自定义响应码、响应信息和响应参数.||

## 数据请求接口

### 1. 接口名称

接口名称在相应APIP协议中定义。

示例：

在协议APIP3V1中定义接口 `cidList`

---

### 2. 请求方法

在具体协议中定义，默认为POST。

---
### 3. URL

url = urlHead + urlTail

`urlHead`为 [服务方链上注册服务时的`data.params.urlHead`值],如："http://localhost:8080/APIP/"

`urlTail`在具体的APIP协议的具体接口处定义，格式为：`apip<协议编号>/v<协议版本号>/<接口名称>`。如："apip3/v1/cidList"

示例:

```
urlHead = "http://localhost:8080/APIP/"
urlTail = "apip3/v1/cidList"
url: "http://localhost:8080/APIP/apip3/v1/cidList"
```
---

### 4. 请求参数

* Request Header

SessionName = <请求方通过connect获得的sessionKey的前12字符>
Sign = <将32字节sessionKey加到Request Body后面，取双sha256值>

* Request Body

|name|type|description|requested|
|:---|:---|:---|:---|
|url|string|当前所请求api的url|Y|
|time|timestamp|发起请求的时间戳，精确到毫秒|Y|
|nonce|long|随机数|Y|
|fcdsl.query|object|请求数据的查询语句，参见[APIP简易查询语法](#APIP简易查询语法)|N|
|fcdsl.filter|object|请求数据的筛选语句，参见[APIP简易查询语法](#APIP简易查询语法)|N|\
|fcdsl.except|object|请求数据的剔除语句，参见[APIP简易查询语法](#APIP简易查询语法)|N|
|fcdsl.sort|object|请求数据的排序语句，参见[APIP简易查询语法](#APIP简易查询语法)|N|
|fcdsl.after|string array|从该排序位置后开始获取，参见[APIP简易查询语法](#APIP简易查询语法)|N|
|fcdsl.size|int|请求列表数据的条目数|N|

* 示例：
sessionKey = 9f41c796e51e07474ce56c76c343a707e00bfc532bd75a00c257caaba3f8196d
查询符合以下条件的条目：
    1）"cid"字段包含“arm”,不区分大小写
    2）按"nameTime"值升序排列
    3）请求1条

   - Request Header
```
    SessionName = 9f41c796e51e
    Sign = f2a70ac2e3a8acdfc0d4683b924c9b8a25c85a33d7ca586aaef0b1c8
```

   - Request Body
```
{
	"url": "http://localhost:8080/APIP/apip3/v1/cidSearch",
	"time": 1677673821267,
	"nonce": 1987697,
	"fcdsl": {
		"query": {
			"part": {
				"fields": ["cid"],
				"value": "*arm*",
				"isCaseInsensitive": "true"
			}
		},
        "sort": {
            "nameTime": "asc"
        },
        "size":"1"
	}
}
```
响应内容见下节。
---
### 5. 响应

* 验证请求
服务方按顺序验证请求

   - 1）验证用户。验证requester是否在用户列表中，用户列表由余额大于`pricePerRequest`的FCH地址构成。不在列表返回1000。

   - 2）验证请求对象。验证签名内容中的`url`值是否与服务方当前接口的url相同，防止重放攻击。不同则返回1001。

   - 3）验证时间戳。验证当前时间戳减请求参数中`time`的绝对值是否小于服务方设定的windowTime，防止重放攻击。过期返回1002.

   - 4）验证`sessionKey`时效。检查服务器保存的requeter的sessionKey是否过期。过期返回1004.

   - 5）验证签名。在请求参数中加入requester的`sessionKey`做两次sha256哈希，验证值是否与`sign`的值一致。不一致返回1003。

* Response Head
    Code = <code值转换为字符串>
    Sign = <Request Body 尾部追加sessionKey做两次sha256的哈希值>
    
* Response Body
|参数|类型|说明|
|:---|:---|:---|
|code|int|响应状态码|
|message|string|响应状态描述|
|balance|int64|请求方当前服务余额|
|got|int|本次返回数据条目数|
|total|int|查询条件命中总条目数|
|bestHeight|int|返回数据时的最新区块高度|
|data|object|响应命中数据或错误提示数据，由具体APIP定义|
|last|string Array|最后一个条目的sort值,用于下次请求|

响应码相关信息参见[响应码](#响应码)

* 示例
上节请求示例的响应如下：

   - 响应头部
```
Code = 0
Sign = 4d3242031e1a8caab81466734b379e07519ee652e9f096455afca3ea1efedd1e
```

```
{
    "code": 0,
    "message": "Success.",
    "balance": 15155589340,
    "got": 1,
    "total": 1,
    "bestHeight": 1725593,
    "data": {
        "FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX": [
            "C_armX"
        ]
    },
    "last": [
        "1620389960"
    ]
}
```
---

### 6. 请求方验证响应数据

请求方收到数据后，将sessionKey加入Response Body尾部做两次sha256得到哈希值，与sign相等则接受数据，否则放弃数据。

---
### 7. 请求方重置sessionKey

请求方在:
    1）希望重置sessionKey，或
    2）收到错误码1004，sessionKey过期时，或
    3）收到错误码1000，并完成充值时
    用`connect`接口申请新的sessionKey。

---

## APIP简易查询语法`fcdsl`

#### 规则
* `fcdsl`参考ElasticSearch DSL，进行简化，更多查询功能和语法由API服务商自行定义和发布。
* 语句包括
  - 独立查询语句：all和ids
  - 容器语句：query，filter，except
  - 容器查询语句：terms，part，match，range，exists，unexists
  - 分页语句：sort，after，size
  - 索引语句：index
  - 其他语句：other
* all、ids、query、filter、except不可相互包含。
* ids不与query、filter、except同时使用。
* 除了ids和all以外，所有查询命令均为多字段查询，字段名在"fields"数组中给出，跨字段关系见相应命令说明。
* 查询命令以"s"结尾，表示可以同时查询多个值，值在"values"数组中给出，否则为单值查询，在"value"中给出。
* 所有查询值均为字符串类型。
* query、filter和except可组合使用，组合关系为"and"。
* 除了ids和all以外的查询命令在query、filter或except内组合使用，组合方式为"and"。
* query、filter或except只是容器，包含查询语句后使用。
* 查询的优先次序为：ids > all > query、filter或except
* size、sort、after可省略,省略时采用默认值：
    - sort的默认值由各数据相应协议给出。
    - size默认值为20条
    - after缺省为从首条记录开始获取
* 查询命令执行的优先顺序为：
    1. 无fcdsl项的all查询
    2. ids查询
    3. 有fcdsl项的all查询
    4. 其他查询
* 查询命令如下：
    
#### all (全查询)
* 获取全部条目。	
* 无fcdsl对象, 或fcdsl对象中无query、filter、except项
* 基于ES DSL的matchAll语句
```
{
	"url": "http://localhost:8080/APIP/apip3/v1/cidSearch",
	"time": 1677673821267,
	"nonce": 1987697
}
//查询全部
```

```
{
	"url": "http://localhost:8080/APIP/apip3/v1/cidSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "size":20,
        "sort":{
            "lastHeight": "asc"
        },
        "after":["677611"]
    }
}
//查询全部，按lastHeight升序，从lastHeight=677611之后开始取20条。
```

#### ids （id查询）
* 按照ID获取多个值
* 不与其他语句组合使用
* 基于ES DSL的mget语句
```
{
	"url": "http://localhost:8080/APIP/apip2/v1/txList",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "ids":["754ecc6adc0556f7c79181abe28ebae2b2f7c4f8237c013f2ce149ed19965df8","0f96070e66dff2909570fc0b78fff572eff0c37cd17938e05c4c3a2fef0ef90f"]
    }
}
//查询id为"754ecc6adc0556f7c79181abe28ebae2b2f7c4f8237c013f2ce149ed19965df8"或"0f96070e66dff2909570fc0b78fff572eff0c37cd17938e05c4c3a2fef0ef90f"的条目
```

#### query （查询）
* `query`内可组合多个查询语句，组合关系为"and"
* 可包含的查询命令如下：

##### 1. terms （字符精确查询）
* 字符类型，多字段，多值，精确匹配。
* 多字段关系: or
* 多值关系: or
* 基于ES DSL的terms和bool语句

```
{
	"url": "http://localhost:8080/APIP/apip2/v1/addressSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "terms":{
                "fields":["id","guide"],
                "values":["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK","FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX"]
            }
        },
        "size":10
    }
}
//查询字符串字段“id”或"guide"中值为"FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"或"FMZsWGT5hEUqhnZhLhXrxNXXG6uDHcarmX"的条目。
```

```
{
	"url": "http://localhost:8080/APIP/apip2/v1/addressSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "terms":{
                "fields":["cd","cdd"],
                "values":["2","3"]
            }
        },
        "size":10
    }
}
//查询字段“cd”或“cdd”的值等于"2"或"3"的条目。
```
##### 2. part （部分查询）
* 字符类型，多字段，单值，部分匹配。	
* 多字段关系: or
* isCaseInsensitive为"true"时不区分大小写
* 基于ES DSL的wildcard和bool语句

```
{
	"url": "http://localhost:8080/APIP/apip3/v1/cidSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "part":{
                "fields":["cid","id"],
                "value":"arm",
                "isCaseInsensitive":"true"
            }
        }
    }
}
//查询“cid”和"id"字段,值在任何位置包含“arm“的条目,不区分大小写。
```
##### 3. match	（分词查询）
* 文本类型，多字段，单值，分词模糊查询
* 多字段关系: or
* 基于ES DSL的match和bool语句

```
{
	"url": "http://localhost:8080/APIP/apip2/v1/opReturnSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "match":{
                "fields":["opReturn"],
                "value":"free cash"
            }
        }
    }
}
//在"opReturn"字段，对“free cash”做分词查询，不区分大小写

```
##### 4. exists	（非空查询）
* 字符类型，多字段，全非空查询
* 多字段关系: and
* 基于ES DSL的exists和bool语句

```
{
	"url": "http://localhost:8080/APIP/apip2/v1/addressSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "exists":["pubKey","btcAddr"]
        }
    }
}
//查询“pubKey”和"btcAddr"字段都非null的条目。
```

##### 5. unexists（为空查询）
* 字符类型，多字段，全为空查询
* 多字段关系: and
* 基于ES DSL的exists和bool语句
```
{
	"url": "http://localhost:8080/APIP/apip2/v1/addressSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "unexists":["pubKey","btcAddr"]
        }
    }
}
//查询“pubKey”和"btcAddr"字段都为null的条目。
```

##### 6. range	（范围查询）
* 多字段同时满足多个相同条件组合查询
* 多字段关系: or
* 条件包括：
    gt（>，大于）
    gte（>=，大于等于）
    lt（<，小于）
    lte（<=，小于等于）
* 条件可组合使用，关系为：and
* 基于ES DSL的range语句
		
```
{
	"url": "http://localhost:8080/APIP/apip2/v1/addressSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "range": {
                "fields": ["cd","cdd"],
                "gt": "0",
                "lte": "10000"	
            }
        }
    }
}
//查询cd和cdd值均大0、小于等于10000的条目。
```

```
{
	"url": "http://localhost:8080/APIP/apip2/v1/addressSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "range": {
                "fields": ["id","guide"],
                "gt": "FA",
                "lte": "FE"
            }
        }
    }
} 
//查询id和guide字段,前两个字母为FB、FC或FE的条目。
```
#### filter （筛选）
* 按此部分的条件筛选条目
* 命令和用法与query相同，略
```
{
	"url": "http://localhost:8080/APIP/apip2/v1/addressSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "range": {
                "fields": ["cd","cdd"],
                "gt": "0",
                "lte": "10000"	
            }
        },
        "filter":{
            "range": {
                "fields": ["guide"],
                "gt": "FA",
                "lte": "FE"
            }
        }
    }
}
//查询cd和cdd值均大0、小于等于10000, 并且"guide"值前两个字母为FB、FC或FE的条目，
```
#### except （剔除）
* 按此部分的条件剔除条目
* 命令和用法与query相同，略
```
{
	"url": "http://localhost:8080/APIP/apip2/v1/addressSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "range": {
                "fields": ["cd","cdd"],
                "gt": "0",
                "lte": "10000"	
            }
        },
        "except":{
            "terms":{
                "fields":["guide"],
                "values":["FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"]
            }
        }
    }
}
//查询cd和cdd值均大0、小于等于10000, 排除"guide"为"FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"的条目，
```

#### size （请求数量）
* 请求返回的条目数量，以字符串形式提交。
* 基于ES DSL的size语句

局部示例：
```
"size":"1000"
//请求返回1000条。
```
	
#### sort	（请求排序）
* 查询结果的排序条件
* filed给出排序字段
* order值为“asc”时升序，“desc”降序
* 多个字段依次实现多级排序
* 基于ES DSL的sort语句
* 响应数据包含"last"字段，给出最后条目的sort值

局部示例：	
```
"sort":[{
		"field": "guide",
		"order": "asc"
	},
	{
		"field": "cdd",
		"order": "desc"
	}
]
//第一级按"guide"升序，第二级按cdd降序排列条目。
```
#### after	（上次末位）
* 定位从某项之后开始获取。
* 基于ES DSL的的after语句。
* after必须与sort一起使用，获取相应sort位置之后的条目。
* after的值的次序与sort的字段次序严格对应。
* after的所有值均转换为String。
* 没有“after”则从排序第一项开始获取。
* 连续请求时，after可取上次请求响应数据中`last`字段的值。

局部示例：	
```
"after":[
        "FAEYhvxWZPE522v6EogroXSTWqxNoBvp7K",
        "1429"
    ]
//按照address一级升序和cdd二级降序的排序，从guide为FAEYhvxWZPE522v6EogroXSTWqxNoBvp7K，cdd为1429之后的那一项开始获取。
```
#### index
仅用于general接口，用于指明查询的索引，只能是单一索引。其他接口无需用户给定索引。

#### other
由具体协议自定义查询内容。

#### 综合示例

查询符合以下条件的条目：
    1）"cd"和"cdd"字段值均大于0；
    2）"pubKey"字段不为空；
    3）排除"cash"字段值为0的条目；
    4) 按guide一级升序，cd二级降序；
    5）从guide="F5rhTTYgQYetQHM3k9xDRbbiNDZS61oAFa"，cd= "1121521"的条目之后开始获取；
    6）请求2条。
    
```
{
	"url": "http://localhost:8080/APIP/apip1/v1/addressSearch",
	"time": 1677673821267,
	"nonce": 1987697,
    "fcdsl":{
        "query":{
            "range": {
                "fields": ["cd","cdd"],
                "gt": "0"	
            }
        },
        "filter":{
            "query":{
                "exists":["pubKey"]
            }
        },
        "except":{
            "terms":{
                "fields":["cash"],
                "values":["0"]
            }
        },
        "sort":[{
            "field":"guide",
            "order":"asc"
            },
            {
            "field":"cd",
            "order":"desc"
        }],
        "after": [
        "F5rhTTYgQYetQHM3k9xDRbbiNDZS61oAFa",
        "1121521"
        ],
        "size":"2"
	}
}
```
响应数据
```
{
    "code": 0,
    "message": "Success.",
    "balance": 15137089340,
    "got": 2,
    "total": 2425,
    "bestHeight": 1726809,
    "data": [
        {
            "id": "FNiX79EiGQAajnbURHPE79arDButdzDQQE",
            "pubKey": "039f66926a9c4bbba4cca7028ba3a1593f74335d7af96e0f1f552283f33f289d1b",
            "balance": 99376620,
            "income": 1895797472,
            "expend": 1796420852,
            "guide": "F5xMDt2r929zNKqnmcEK9TuQrpA81Rcbhr",
            "birthHeight": 743167,
            "lastHeight": 752407,
            "cdd": 2,
            "cd": 685,
            "cash": 1,
        },
        {
            "id": "FQ9KvLMbDd3EJy92pZzMKXBF9XASF4Bb4P",
            "pubKey": "0323156856677ab2b1308fa1ede716a06ff03fd50aad338396d1b6ea8fa46a59ac",
            "balance": 29071127,
            "income": 10001894765853,
            "expend": 10001865694726,
            "guide": "F5zFVnLzzSaw2r8wrggT5eYPJHmHGiMPxq",
            "birthHeight": 660683,
            "lastHeight": 1538260,
            "cdd": 359,
            "cd": 38,
            "cash": 2,
        }
    ],
    "last": [
        "F5zFVnLzzSaw2r8wrggT5eYPJHmHGiMPxq",
        "38"
    ]
}
```

## 通用语法接口:general

按照fcdsl语法查询的通用接口。

示例：
URL：http://localhost:8080/APIP/apip1/v1/general

* 请求参数 
    在数据请求接口基础上，在"fcdsl"项下增加 "index"字段，用于指定所查询的索引库。
示例：
```
{
	"url": "http://localhost:8080/APIP/apip1/v1/general",
	"time": 1677673821267,
	"nonce": 1,
	"fcdsl": {
		"index": "cid",
		"query": {
			"part": {
				"fields": ["cid", "id"],
				"value": "*ar*",
				"isCaseInsensitive": "true"
			}
		},
        "sort":[{
            "field":"id",
            "order":"asc"
        }],
        "last": [
            "F6fLB35iTH1NUywnZ1MNQarCiW2TZxw2C6"
        ],
        "size":"2"
	}
}
```

* 响应参数
    与数据请求响应一致。
示例：
```
{
    "code": 0,
    "message": "Success.",
    "balance": 15136389340,
    "got": 2,
    "total": 38,
    "bestHeight": 1726961,
    "data": [
        {
            "id": "F6BGi3Hzo9GUgParKZzm66xRfcBxm2YX5Z",
            "cid": "?????_YX5Z",
            "usedCids": [
                "?????_YX5Z"
            ],
            "reputation": 0,
            "hot": 0,
            "nameTime": 1634804353,
            "lastHeight": 943560
        },
        {
            "id": "F6fLB35iTH1NUywnZ1MNQarCiW2TZxw2C6",
            "cid": "Maka_w2C6",
            "usedCids": [
                "Maka_w2C6"
            ],
            "reputation": 0,
            "hot": 0,
            "nameTime": 1623158294,
            "lastHeight": 772712
        }
    ],
    "last": [
        "F6fLB35iTH1NUywnZ1MNQarCiW2TZxw2C6"
    ]
}
```






