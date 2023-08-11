```
ProtocolType: FBBP
SerialNumber: 2
ProtocolName: Maker
Version: 1
Description : Group related actions on Freecash blockchain. 
Authors: C_armX, Deisler-JJ_Sboy
Language: en-US
CreateDate: 2021-04-03
UpdateDate: 2023-05-07
```

# BBP19V1_Group(en-US)

## Contents

[Consensuses of BBP](#consensuses-of-fbbp)

[Consensuses of this protocol](#consensuses-of-this-protocol)

[Create](#create)

[Update](#update)

[Join](#join)

[Leave](#leave)

[QR code](#qr-code)

## Consensuses of BBP

1. BBP(Free-consensus Basic Bussiness Protocol) type protocols define the bacic bussiness consensuses with data writed on-chain.

2. The SIGHASH flag of all transaction inputs must be ‘ALL’ (0x41).

3. The max size of OP_RETURN : 4096 bytes.

4. The format of the data in op_return: JSON.

5. Encoding : utf-8.

6. BBP protocols implement `FEIP1_Protocol` protocol.

## Consensuses of this protocol

1. This protocol publishes and manages market maker services on the blockchain.
2. Maker is a kind of service which implements the protocol`FEIP5V2_Service`.
3. Anyone can publish a maker service，or `maker` for short and update, stop, recover or close it according to `FEIP5V2_Service`.
4. When publish of update a maker service:
	- the value of `data.types` has to inclode `BBP` and `maker`.
	- the value of `data.protocols` has to inclode the `pid` or `did` of this protocol.
5. The owner of the service has to assign an FCH address as the seller of the service in `data.params.seller`.
6. The owner has to set:
	- `data.params.sell`: what is the maker selling.
	- `data.params.accept`what is the maker accepting.
	- `data.params.minPayment`what is the minimum payment this maker is willing to accept.
7. The owner has to set the `data.params.confirmation` parameters of the payment before excuting the order, which incloding:
	- `min`: the minimum number of confirmations in string.
	- `step`: the amount of what is being accepted in one step in string.
	- `raiseBy`: the number of additionally required confirmations in each tier in string.
	- `max`: the maximum number of confirmations.

* Example：
```
{
	"confirmation":{
		"min": "1",
		"step": "1000",
		"raiseBy": "2",
		"max": "30"
	}
}
If a customer buys 1000, 1 confirmations would be required.
If a customer buys 1100, 3 confirmations would be required: 1+2=3).
If a customer buys 12300, 25 confirmations would be required: 1+((int)22300/1000)*2 = 1+12*2 = 25.
If a customer buys 30000, 30 confirmations would be required.
```
10. The owner has to set the `data.params.price` parameters, which incloding:
	- `min`: the minimum price.
	- `stepHeight`: the amount of what is being sold in one step in string.
	- `raiseBy`: the additionally increased amount of the price in each step in string.
	- `dropInterval`: the number of blocks without any order of this maker to trigger a price drop in string.
	- `dropBy`: The amount by which the price drops each time in string.
	- `max`: the maximum number of confirmations in string.
* Example：
```
{
	"price":{
		"min": "10",
		"step": "100",
		"raiseBy": "3",
		"waitFor":"1000",
		"dropBy":"10",
		"max": "20"
	}
}
If a customer buys 100, the total payment has to be 1000 = 100*10.
If a customer buys 120, the total payment has to be 1260 = 100*10+20*(10+3) = 1000+260.
If a customer buys 230, the total payment has to be 2780 = 100*10+100*(10+3)+30*(10+3+3)= 1000+1300+480.
If a customer buys 450, the total payment has to be 6800 = 100*10+100*(10+3)+100*(10+3+3)+100*(10+3+3+3)+50*20 = 1000+1300+1600+1900+1000.

If no one buys in 1000 blocks, the price will be reduced by 10, but no less than 10.
```

11. The `current price` is the price of the last unit of the maker's last order. If no one buy again it would drop by `dropBy` every `waitFor` until the `min` price.

12. 请求方的`购买标记`，即购买交易的OP_RETURN内容，如下：

```
{
	"type": "BBP",
	"sn": "1",
	"ver": "1",
	"name": "Maker",
	"data": {
		"op": "buy",
		"sid": <string，所购买服务的sid，必填>,
        "via":<string array, 购买渠道，如app的aid，非必填>
	}
}
```

* Example of publishing a maker service
```
{
    "type": "FEIP",
    "sn": 5,
    "ver": 2,
    "name": "service",
    "pid": "",
    "did": "",
    "data":{
        "op": "publish",
        "stdName": FchForEth,
        "localName": <string array，服务的其他语言名称>,
        "desc": <string, 对服务的描述>,
        "type": <string array, 服务的分类，“BBP”和”maker“为必填，可多个>
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






## Create
Send a tx with the content of op_Return as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "create"|Y|
|name|String|Group name|Y|
|desc|String|Description of this group|Y|

* Example of creating a group

```
{
    "type": "BBP",
    "sn": 19,
    "ver": 1,
    "name": "Group",
    "pid": "",
    "data":{
        "op": "create",
        "name": "test",
        "desc": "This is a test group"
    }
}
```

## Update
Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "create"|Y|
|gid|string|Group ID|Y|
|name|String|Group name|Y|
|desc|String|Description of this group|Y|

* Example of updating group information

```
{
    "type": "BBP",
    "sn": 19,
    "ver": 1,
    "name": "Group",
    "pid": "",
    "data":{
        "op": "update",
        "gid": "6305d16c89fb98763b6968049096a984eea9334e12c514c9b72098c3f332d114",
        "name": "test1",
        "desc": "This is an updating test."
    }
}
```

## Join

Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "join"|Y|
|gid|string|Group ID|Y|

* Example of joining in a group

```
{
    "type": "BBP",
    "sn": 19,
    "ver": 1,
    "name": "Group",
    "pid": "",
    "data":{
        "op": "join",
        "gid": "6305d16c89fb98763b6968049096a984eea9334e12c514c9b72098c3f332d114"
    }
}
```

## Leave

Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "leave"|Y|
|gid|string|Group ID|Y|

* Example of leaving a group

```
{
    "type": "BBP",
    "sn": 19,
    "ver": 1,
    "name": "Group",
    "pid": "",
    "data":{
        "op": "leave",
        "gid": "6305d16c89fb98763b6968049096a984eea9334e12c514c9b72098c3f332d114"
    }
}
```
## QR code

The QR code of a published group has fields as following:

```
{
	"meta":"FC",
    "type": "BBP",
    "sn": 19,
    "ver": 1,
    "data":{
        "name": "test",
        "gid": "6305d16c89fb98763b6968049096a984eea9334e12c514c9b72098c3f332d114"
    }
}
```