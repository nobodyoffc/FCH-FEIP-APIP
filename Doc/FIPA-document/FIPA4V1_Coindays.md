```
FIPA4: CoinDays
Version: 2
Language: zh-CN
Author: C_armX, Deisler-JJ_Sboy
Status: draft
Created date: 2022-10-01
Last modified date：2022-12-06
PID: "unknown"
TXid: 
```

# FIPA4_CoinDays(en-US)

## Contents

⌊Introduction⌋(#introduction)

⌊General rules of FIPA type protocols⌋(#general-rules-of-fipa-protocols)

⌊Rules specific to this protocol⌋(#rules-specific-to-this-protocol)

⌊Example⌋(#Example)



## Introduction

```

ProtocolType: FIPA
SerialNumber: 4
ProtocolName: CoinDays
VersionNumber: 1
Description : CoinDays is an basic property of FCH output.
Author: C_armX, Deisler-JJ_Sboy
Language: en-US
Tags: FIPA, CoinDays
PreVersionPid:"unknown"

```

## General consensus of FIPA protocols

FIPA protocols make improvements to the FCH ecosystem. These improvements will not lead to a hard fork of the main network, and will not write information on the blockchain.

## Rules specific to this protocol

1. 本协议定义`币天（CD, CoinDays）`和`币天销毁（CDD, CoinDays Destroyed）`两个指标，计算的基础实体为交易的`输出（output）`。
2. `Output`：计算币天的Output是链上交易产生的具有一定金额的输出，不包括op_return输出。
3. `CoinDays`(币天，缩写：CD) ：未被花费的Output的FCH的数量与该Output生存天数的乘积。FCH的数量先以`聪`为单位与`向下取整`后天数相乘，结果`除以100000000`折算为FCH与天的乘积，再`向下取整`：
```
	CD = ⌊(C * D)/100000000⌋

	C：FCH的数量，取Output的value值，单位为聪。1 FCH = 100000000 satoshi。
	D：生存天数，最新区块时间戳减去Output诞生的区块时间戳后按天取整的值。
	时间戳：Unix时间戳，精确到秒。
	
	D = ⌊(currentTimestamp - birthBlockTime)/(60*60*24)⌋
	
	CD = ⌊C * ⌊(currentTimestamp - birthBlockTime)/(60*60*24)⌋/100000000⌋
```	
4. `CoinDays Destroyed` (币天销毁，缩写：CDD)：已被花费的Output，其生存期间积累的全部币天在花费时被销毁了，称为币天销毁。
```
	CDD = ⌊C * ⌊(spentBlockTime - birthBlockTime)/(60*60*24)⌋/100000000⌋
```	
5. CD是`未花费交易输出（UTXO，unspent transaction output）`的属性，CDD是`已花费交易输出（STXO, spent transaction output)`的属性。
6. 单位：CD和CDD均以小写的`cd`为单位。
7. Output是计算币天的基础实体，交易、区块或区块链的币天由所包含的Output的币天汇总产生
8. FCH地址的CD为所有UTXO的CD的总和，CDD为所有STXO的CDD的总和。

## Example

### CD of UTXO
C = 1580000000 satoshi
birthBlockTime = 1577837394
currentTimestamp = 1664612535
D = ⌊(1664612535 -1577837394)/(60*60*24)⌋ = 1004 days
CD = ⌊C * D/100000000⌋ = ⌊2580000000*1004/100000000⌋ = ⌊15863.2⌋ = 15863 cd
```Java
	public void cd() {
		long c = 1580000000L;
		long birthBlockTime = 1577837394L;
		long bestBlockTime = 1664612535L;
		long d = Math.floorDiv(bestBlockTime-birthBlockTime,60*60*24);
		long cd = Math.floorDiv(c*d,100000000);
		System.out.println("CD: "+cd+"cd");	
	}//CD: 15863cd
```


### CDD of STXO
C = 625000000 satoshi
birthBlockTime = 1577837394
spentBlockTime = 1634026927
D = ⌊(1634026927 -1577837394)/(60*60*24)⌋ = 650 days
CDD = ⌊C * D/100000000⌋ = ⌊625000000*650/100000000⌋ = ⌊4062.5⌋ cd = 4062 cd
```Java
	public void cdd() {
		long c = 625000000L;
		long birthBlockTime = 1577837394L;
		long spentBlockTime = 1634026927L;
		long d = Math.floorDiv(spentBlockTime-birthBlockTime,60*60*24);
		long cdd = Math.floorDiv(c*d,100000000);
		System.out.println("CDD: "+cdd+"cd");	
	}//CDD: 4062cd
```