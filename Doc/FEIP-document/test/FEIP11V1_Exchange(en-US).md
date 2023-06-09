# FEIP11V1_Exchange(en-US)

## Contents

[Summary](#summary)

[General consensus of FEIP](#general-consensus-of-feip)

[Consensus of this protocol](#consensus-of-this-protocol)

[Prepay](#prepay)

[Delivery](#delivery)

[Sub-prepay](#sub-prepay)

[Sub-delivery](#sub-delivery)

[Close](#close)

## Summary

```
Protocol type: FEIP
Serial number: 11
Protocol name: Exchange
Version: 3
Description :  perform an exchange with or without an agent on freecash blockchain.
Author: C_armX
Language: en-US
Created date: 2023-01-18
Last modified date：2023-01-18
```
## General consensus of FEIP

1. FEIP type protocols write data of consensus in `OP_RETURN` for public witness.

2. The SIGHASH flag of all transaction inputs: `ALL (value 0x01)`.

3. The max size of OP_RETURN : `4096 bytes`.

4. The format of the data in op_return: `JSON`.

5. Encoding : `utf-8`.

6. Since block height `2000000`, any operation of writing to freecash blockchain needs more than `1cd` consumed.

## Consensus of this protocol

eid
预付方，交付方，仲裁者构建ceil(n/2)多签地址，
系列支付

## Prepay

When starting an exchange by paying in advance, the prepayer send a tx with the content of OP_RETURN as following:

|field number|field name|type|content|required|
|:----|:----|:----|:----|:----|
|1|type|String|Fixed: "FEIP"|Y|
|2|sn|int|Serial number. Fixed: 11|Y|
|3|ver|int|Fixed: 1|Y|
|4|name|String|Fixed: "Exchange"|N|
|5|pid|hex|The PID of this protocol|N|
|6|data.op|string|operation: "prepay"|Y|
|7|data.method|string|The method of prepay. The default is FCH.|N|
|8|data.prepayId|string|The id of the payment. The default is this txid when paying with FCH.|N|
|9|data.contractId|string|The ID of the exchange contract.|N|
|10|data.arbitrators|string array|The list of arbitrators.|N|

## Delivery

When delivering, the prepayee send a tx with the content of OP_RETURN as following:

|field number|field name|type|content|required|
|:----|:----|:----|:----|:----|
|1|type|String|Fixed: "FEIP"|Y|
|2|sn|int|Serial number. Fixed: 11|Y|
|3|ver|int|Fixed: 1|Y|
|4|name|String|Fixed: "Exchange"|N|
|5|pid|hex|The PID of this protocol|N|
|6|data.op|string|operation: "delivery"|Y|
|8|data.eid|string|The txid of prepay.|N|
|7|data.method|string|The method of paying. The default is FCH.|N|
|9|data.deliveryId|string|The id of the payment. The default is this txid when paying with FCH.|N|

## Sub-prepay

A prepayment of an exchange can be split into  some sub-prepayments. To perform a sub-prepayment, the prepayer send a tx with the content of OP_RETURN as following:

|field number|field name|type|content|required|
|:----|:----|:----|:----|:----|
|1|type|String|Fixed: "FEIP"|Y|
|2|sn|int|Serial number. Fixed: 11|Y|
|3|ver|int|Fixed: 1|Y|
|4|name|String|Fixed: "Exchange"|N|
|5|pid|hex|The PID of this protocol|N|
|6|data.op|string|operation: "sub-prepay"|Y|
|7|data.eid|string|The txid of prepay.|N|


## Sub-delivery

A delivery of an exchange can be split into  some sub-deliveries. To perform a sub-delivery, the prepayee send a tx with the content of OP_RETURN as following:

|field number|field name|type|content|required|
|:----|:----|:----|:----|:----|
|1|type|String|Fixed: "FEIP"|Y|
|2|sn|int|Serial number. Fixed: 11|Y|
|3|ver|int|Fixed: 1|Y|
|4|name|String|Fixed: "Exchange"|N|
|5|pid|hex|The PID of this protocol|N|
|6|data.op|string|operation: "sub-delivery"|Y|
|7|data.eid|string|The txid of prepay.|N|


## Close
