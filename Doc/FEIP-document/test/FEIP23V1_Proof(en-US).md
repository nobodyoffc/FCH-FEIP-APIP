```
FEIP33: Proof
Version: 1
Language: en-US
Authors: C_armX
Status: draft
Create: 2021-07-04
Update：2022-12-22
PID: ""
TXID: 
```

# FEIP33V1_Proof(en-US)


## Contents

[About FEIP](#about-feip)

[About this protocol](#about-this-protocol)

[发行证明与邀请签署](#发行证明与邀请签署)

[签署证明](#签署证明)

[转让证明](#转让证明)

[销毁证明](#销毁证明)


```
Protocol type: FEIP
Serial number: 33
Protocol name: Proof
Version: 1
Description : 链上签发、转让或销毁各种证明。
Author: C_armX
Language: en-US
Previous version PID:""
```

## About FEIP

1. Write important data in OP_RETURN for public witness under FEIP type protocols.

2. The SIGHASH flag of all transaction inputs is ‘ALL’ (value 0x01).

3. The max size of OP_RETURN : 4096 bytes.

4. The format of the data in op_return: JSON.

5. Encoding : utf-8.


## About this protocol

1. 本协议支持任何人签发、转让或销毁某种证明；

2. Proof ID

    该证明的唯一标识，即初始签发证书的交易id。

3. 发行与邀签

    1) 当发行证明时，第一个输入地址为证明的签发者和持有者；
    2) 发行意味着签发者会对所签发内容负责。
    3) 签发者可通过输出地址向他人提出签名的邀请，被邀请者签名的含义在证明内容中给出。

4. 签约

    1）接到邀约者可以对该证明进行签署，承担邀约内容中规定的责任或义务。
    2）未被邀约地址对证明的签署无意义。

5. 转让

    1) 证明发行时标注是否可转让。
    2) 证明转让时，第一个输入为转出方，不同于转出方的第一个输出为转入方，转让意味着该证明的所有权从转出方转移至转入方。不可转让给自己。
    3) 转让的相关细节在证明的内容中给出。

6. 销毁

   1) 只有证明的当前所有者可以销毁证明;
   2) 被销毁的证明不再具有任何约束力。

## 发行证明与邀请签名

第一个输入地址为发行者，发行者制定并同意本证明的所有内容，并愿意承担相应责任，op_retur内容格式为:

|序号|字段名|中文名|类型|值的说明|
|:----|:----:|:----|----|:----|
|1|type|协议类型|String|Fixed: "FEIP"|
|2|sn|协议编号|int|Serial number<br>Fixed: 33|
|3|ver|协议版本|int|Fixed: 1|
|4|name|协议名称|String|Fixed: "Proof"|
|5|data.op|操作|string|fixed："issue"|
|6|data.transferable|转让标记|bool|1表示可转让，0表示不可转让|
|7|data.title|标题|string|证明的标题|
|8|data.content|内容|string|内容的文本|
|9|data.cosigners|联合签署人|string array|应邀联合签署本证明的地址列表|

### Example
签发者发送交易，op_return内容如下：
```
{
    "type": "FEIP",
    "sn": 33,
    "ver": 1,
    "name": "Proof",
    "pid": "",
    "data":{
        "op": "issue",
        "transferable": 1,
        "title": "借据",
        "content": "1. 此为借款证明；2. 发行者对本证明持有者负有还款责任，3. 其他签署者为本证明的担保人，在发行者不能履行还款责任时，代为还款，还款后成为该笔款项的债权人。4. 借款金额为￥20000元；5. 借款年利率为4%；6. 借款期限为1年；7. 此证明持有者可在到期后获得本息总计￥20800元；8. 此证明可转让。",
		"cosigners":["F1111111111111","F222222222222"]
	}
}

```
假设该交易的id为：83df9bdf7539f698fec3e0199913d8a54401e2aa416ecbe5debc529c4fc84f62
则此交易id为此证明的唯一id，即proof ID。

## 签署证明

cosigners可签署证明，交易的第一个输入地址为签名者，对proofId进行签名，签名意义在证明内容中给出。op_retur内容格式为:

|序号|字段名|中文名|类型|值的说明|
|:----|:----:|:----|----|:----|
|1|type|协议类型|String|Fixed: "FEIP"|
|2|sn|协议编号|int|Serial number<br>Fixed: 33|
|3|ver|协议版本|int|Fixed: 1|1|
|4|name|协议名称|String|Fixed: "Proof"|
|5|data.op|操作|string|fixed："sign"|
|6|data.proofId|证明编号|string|证明的id|

### Example

转让者发送交易给接受者，op_return内容如下：

```
{
    "type": "FEIP",
    "sn": 33,
    "ver": 1,
    "name": "Proof",
    "pid": "",
    "data":{
        "op": "sign",
        "proofId": "83df9bdf7539f698fec3e0199913d8a54401e2aa416ecbe5debc529c4fc84f62"
    }
}

```

## 转让证明

对标注为可转让的证明可做转让操作。转让交易的第一个输入地址为当前持有者，即转出者，不同于当前持有者的第一个输出地址为转让的接受者，op_retur内容格式为:

|序号|字段名|中文名|类型|值的说明|
|:----|:----:|:----|----|:----|
|1|type|协议类型|String|Fixed: "FEIP"|
|2|sn|协议编号|int|Serial number<br>Fixed: 33|
|3|ver|协议版本|int|Fixed: 1|
|4|name|协议名称|String|Fixed: "Proof"|
|5|data.op|操作|string|fixed："transfer"|
|6|data.proofId|证明编号|string|证明的id|

### Example
转让者发送交易给接受者，op_return内容如下：
```
{
    "type": "FEIP",
    "sn": 33,
    "ver": 1,
    "name": "Proof",
    "pid": "",
    "data":{
        "op": "transfer",
        "proofId": "83df9bdf7539f698fec3e0199913d8a54401e2aa416ecbe5debc529c4fc84f62"
    }
}

```
## 销毁证明

仅证明的当前持有者可销毁证明，op_retur内容格式为:

|序号|字段名|中文名|类型|值的说明|
|:----|:----:|:----|----|:----|
|1|type|协议类型|String|Fixed: "FEIP"|
|2|sn|协议编号|int|Serial number<br>Fixed: 33|
|3|ver|协议版本|int|Fixed: 1|
|4|name|协议名称|String|Fixed: "Proof"|
|5|data.op|操作|string|fixed："destroy"|
|6|data.proofId|证明编号|string|证明的id|

### Example

证明的当前持有者发送交易，op_return内容如下：
```
{
    "type": "FEIP",
    "sn": 33,
    "ver": 1,
    "name": "Proof",
    "pid": "",
    "data":{
        "op": "destroy",
        "proofId": "83df9bdf7539f698fec3e0199913d8a54401e2aa416ecbe5debc529c4fc84f62"
    }
}
```