```
Protocol type: FEIP
SerialNumber: 14
ProtocolName: Proof
Version: 1
Description :Issue, manager and transfer proofs on blockchain of freecash.
Authors: C_armX
Language: en-US
CreateDate: 2022-12-22
UpdateDate: 2023-05-25
```
# FEIP14V1_Proof(en-US)

## Contents

[Consensus of FEIP](#general-consensus-of-feip)

[Consensus of this protocol](#consensus-of-this-protocol)

[issue](#issue)

[sign](#sign)

[transfer](#transfer)

[destroy](#destroy)


## Consensus of FEIP

1. FEIP type protocols write data into freecash blockchain with OP_RETURN for public witness.

2. The SIGHASH flag of all transaction inputs must be ‘ALL’ (0x41).

3. The max size of OP_RETURN : 4096 bytes.

4. The format of the data in op_return: JSON.

5. Encoding : utf-8.

6. Since block height `2000000`, any operation writing FEIP data with OP_RETURN needs more than `1cd` consumed.

7. More consensuses of FEIP can be found in `FEIP1_Protocol`.

## Consensuses of this protocol

1. Issue, manager and transfer proofs on blockchain of freecash.

2. ID: `proofId` is the ID of a proof which is the txid where the proof was issued.

3. Issue
   - The address of the first input of the issuing transaction is the `issuer` and the owner of the proof.
   - The address of the first output is the `owner` of the proof who own all the rights and benefits and nothing of duty or responsibility.
   - If there is no any output, `owner` is the `issuer`.
   - The issuer takes the responsibilities of the proof. However, it can invite cosigners to share the responsibilities.
   - All the meaning of this proof and the responsibilities should be clearly expressed in the content of the proof.

4. Sign
   - Only invited cosigners can sign the proof.
   - Signing a proof means to agree and accept all the meaning and responsibilities of the proof.
   - If the `allSignsRequired` is true when issuing, all cosigners have to sign this proof to activate this proof.

5. transfer
   - Only the proof that the `transferable` was set true and the state is `active` can be transferred.
   - The address of the first input has to be the current owner of this proof.
   - The transferee is the address of the first output.
   - With the transferring, the transferee become the owner of the proof, and get all benefits and right that the issuer and cosigners signed.
   - The transferee do not take any duty or responsibility of the proof.

6. destroy
   - Only the current owner can destroy the proof.
   - The destroyed proof is no longer binding on any person.

7. Operations of this protocol write OP_RETURN with the same content structure:

| field | type    | content                                                                   | required |
|:------|:--------|:--------------------------------------------------------------------------|:---------|
| type  | string  | Fixed: "FEIP"                                                             | Y        |
| sn    | int     | Serial number. Fixed: 14                                                  | Y        |
| ver   | int     | Version. Fixed: 3                                                         | Y        |
| name  | string  | Fixed: "Proof"                                                            | N        |
| pid   | string  | PID of this protocol. The txid where this protocol was published.         | N        |
| did   | string  | DID of this protocol file. The sha256x2 hash of the protocol file in hex. | N        |
| data  | object  | Operation data which is different in different operations.                | Y        |

## issue

The data object contains:

|field|type|content|required|
|:----|:----|:----|:----|
|op        |操作|string|fixed："issue"|
|isTransferable  |转让标记|bool|true表示可转让，false表示不可转让|
|title       |标题|string|证明的标题|
|content      |内容|string|内容的文本|
|cosigners     |联合签署人|string array|应邀联合签署本证明的地址列表|
|allSignsRequired |全签名要求|bool|true表示必须所有cosigner签名后方可生效，false表示发行即生效|

### Example

```
{
    "type": "FEIP",
    "sn": 14,
    "ver": 1,
    "name": "Proof",
    "pid": "",
    "data":{
        "op": "issue",
        "transferable": true,
        "title": "借据",
        "content": "1. 此为借款证明；2. 发行者对本证明持有者负有还款责任，3. 其他签署者为本证明的担保人，在发行者不能履行还款责任时，代为还款，还款后成为该笔款项的债权人。4. 借款金额为￥20000元；5. 借款年利率为4%；6. 借款期限为1年；7. 此证明持有者可在到期后获得本息总计￥20800元；8. 此证明可转让。",
		"cosigners":["F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW","F9pcRpps3T2iHuNGzU3k5b2kWKMRukZP1U"],
        "allSignsRequired": true    
	}
}

```

## sign

The data object contains:

|field|type|content|required|
|:----|:----|:----|:----|
|op|操作|string|fixed："sign"|
|proofId|证明编号|string|证明的id|

### Example

```
{
    "type": "FEIP",
    "sn": 14,
    "ver": 1,
    "name": "Proof",
    "pid": "",
    "data":{
        "op": "sign",
        "proofId": "9f5d54e8e134b145753de35c2417b9d21df5e06a072a0f780a99af9d5298a515"
    }
}

```

## transfer

The data object contains:

| field   | type   | content                     | required |
|:--------|:-------|:----------------------------|:---------|
| op      | string | operation,fixed: "transfer" | Y        |
| proofId | string | ID of the proof             | Y        | 

### Example
 send to the transferee:
```
{
    "type": "FEIP",
    "sn": 14,
    "ver": 1,
    "name": "Proof",
    "pid": "",
    "data":{
        "op": "transfer",
        "proofId": "cc93ae0447a679a5cac85a3796b4cde5905e3953da3e0ff8957c253268d0c2f8"
    }
}

```
## destroy

The data object contains:

| field   | type   | content                     | required |
|:--------|:-------|:----------------------------|:---------|
| op      | string | operation,fixed: "transfer" | Y        |
| proofId | string | ID of the proof             | Y        | 

### Example

```
{
    "type": "FEIP",
    "sn": 14,
    "ver": 1,
    "name": "Proof",
    "pid": "",
    "data":{
        "op": "destroy",
        "proofId": "cc93ae0447a679a5cac85a3796b4cde5905e3953da3e0ff8957c253268d0c2f8"
    }
}
```
