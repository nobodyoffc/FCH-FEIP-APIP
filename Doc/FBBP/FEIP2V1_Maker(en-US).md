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

[Consensuses of FBBP](#consensuses-of-fbbp)

[Consensuses of this protocol](#consensuses-of-this-protocol)

[Create](#create)

[Update](#update)

[Join](#join)

[Leave](#leave)

[QR code](#qr-code)

## Consensuses of FBBP

1. BBP(Free-consensus Basic Bussiness Protocol) type protocols define the bacic bussiness consensuses with data writed on-chain.

2. The SIGHASH flag of all transaction inputs must be ‘ALL’ (0x41).

3. The max size of OP_RETURN : 4096 bytes.

4. The format of the data in op_return: JSON.

5. Encoding : utf-8.

6. BBP protocols implement `FEIP1_Protocol` protocol.

## Consensuses of this protocol

1. This protocol publishes and manages market fchTx services on the blockchain.
2. Maker is a kind of service which implements the protocol`FEIP5V2_Service`.

```json
{
  "type": "FEIP",
  "sn": "5",
  "ver": "2",
  "name": "Service",
  "data": {
    "op": "publish",
    "stdName": "FCH For ETH on arbitrum",
    "localNames": [
      "出售FCH接受arbETH"
    ],
    "desc": "Sell FCH and accept ETH on arbitrum",
    "types": [
      "Maker",
      "FCH",
      "arbETH"
    ],
    "urls": [
      "https://myshop.cash"
    ],
    "waiters": [
      "FErVBq2SzT4GGcH61wSvo6BofQ8U5JL3Cg"
    ],
    "params": {
      "sell": "FCH",
      "accept": "arbETH",
      "minSellAmount": "10000.0",
      "confirmation": {
        "min": "1",
        "step": "1",
        "raiseBy": "10000",
        "max": "6"
      },
      "price": {
        "min": "0.000025",
        "step": "10000",
        "raiseBy": "0.000005",
        "waitFor": "1440",
        "dropBy": "0.000005",
        "max": "0.00025"
      }
    }
  }
}
```

```json
{
	"type": "FEIP",
	"sn": 5,
	"ver": 2,
	"name": "Service",
	"data": {
		"op": "publish",
		"stdName": "SellFchForEthOnArb",
		"localName": ["售FCH换ARB上ETH"],
		"desc": "Free and open cloud storage service",
		"types": ["FBBP", "Maker","FCH","arbETH"],
		"urls": ["https://cid.cash/service"],
		"waiters": [""],
		"protocols": [""],
		"params": {
          "sell": "FCH",
          "accept": "arbETH",
          "minPayment": "0.01",
          "confirmation":{
            "min": "1",
            "step": "0.1",
            "raiseBy": "2",
            "max": "30"
          },
          "price":{
            "min": "10",
            "step": "100",
            "raiseBy": "3",
            "waitFor":"1000",
            "dropBy":"10",
            "max": "20"
          }
		}
	}
}
```
3. Anyone can publish a fchTx service，or `fchTx` for short and update, stop, recover or close it according to `FEIP5V2_Service`.
4. When publish of update a fchTx service:
	- the value of `data.types` has to include `FBBP` and `fchTx`.
	- the value of `data.protocols` has to include the `pid` or `did` of this protocol.
5. The owner of the service has to assign an FCH address as the seller of the service in `data.params.seller`.
6. The owner has to set:
	- `data.params.sell`: what is the fchTx selling.
	- `data.params.accept`what is the fchTx accepting.
	- `data.params.minPayment`what is the minimum payment this fchTx is willing to accept.
7. The owner has to set the `data.params.confirmation` parameters of the payment before executing the order, which including:
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
10. The owner has to set the `data.params.price` parameters, which including:
	- `min`: the minimum price.
	- `step`: the amount of what is being sold in one step in string.
	- `raiseBy`: the additionally increased amount of the price by each step in string.
	- `waitFor`: the number of blocks without any order of this fchTx to trigger a price drop in string.
	- `dropBy`: The amount by which the price drops each time in string.
	- `max`: the maximum price in string.
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

11. The `current price` is the price of the last unit of the fchTx's last order. If no one buy again it would drop by `dropBy` every `waitFor` until the `min` price.

12. maker的销售标记，上一笔交易的价格为下一笔交易价格的基准。如果最低购买量小于调价阶梯，就会存在拆分交易漏洞，因此应标记调价阶梯余额。



```
{
	"type": "FBBP",
	"sn": "1",
	"ver": "1",
	"name": "Maker",
	"data": {
		"op": "deliver",
		"sid": <string，所购买服务的sid，必填>,
		"price":<交付的实际价格>,
		"stepRemain":<阶梯剩余>,
		"buyId":<string，购买的交易id>,
        "via":<string array, 购买渠道，如app的aid，非必填>
	}
}
```

13. 请求方的`购买标记`，即购买交易的OP_RETURN内容，如下：

```
{
	"type": "FBBP",
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
14. Maker 状态对象

	* sid
    * stdName
    * localName
    * desc
    * owner
    * mode: {BuyerFirst,ThirdParty,MultiSign}
    * thirdParty
    * signers
    * seller
    * sell
    * accept
    * minPayment
    * price：{}
    * confirmation：{}
    * lastPrice
    * stepRemain
    * startTime
    * lastTime
    * active
    * closed

15. 模块
    * 界面
    * 解析
    * 订单
    * 交易
    * 交付

16. check

* Example of publishing a fchTx service
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
        "type": <string array, 服务的分类，“BBP”和”fchTx“为必填，可多个>
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