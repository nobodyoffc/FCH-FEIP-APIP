```
Protocol type: FEIP
SerialNumber: 8
ProtocolName: Statement
Version: 4
Description : 2021-06-18.
Authors: C_armX, Deisler-JJ_Sboy，Free_Cash
Language: en-US
CreateDate: 2021-04-03
UpdateDate: 2023-05-22
```

# FEIP8V5_Statement(en-US)

## Contents

[General consensus of FEIP](#general-consensus-of-feip)

[Consensus of this protocol](#consensus-of-this-protocol)

[Publish a statement](#publish-a-statement)

## Consensuses of this protocol

1. Anyone can sign and publish an on-chain statement.

2. statementId:Take the txid where the statement was published as the ID of this statement.

3. The content of the statement is that the signer clearly agrees and is willing to bear corresponding responsibilities.

4. Once a statement published, it can't be deleted or updated.

## Publish a statement

| field       | type    | content                                                           | required |
|:------------|:--------|:------------------------------------------------------------------|:---------|
| type        | string  | Fixed: "FEIP"                                                     | Y        |
| sn          | int     | Serial number. Fixed: 7                                           | Y        |
| ver         | int     | Version. Fixed: 4                                                 | Y        |
| name        | string  | Fixed: "Mail"                                                     | N        |
| pid         | string  | PID of this protocol. The txid where this protocol was published. | N        |
| data.title  | string  | the title of statement                                            | N        |
| data.content| string  | the content of statement                                          | Y        |
| data.confirm| string  | Fixed: "This is a formal and irrevocable statement."              | Y        |

* Example

```
{
	"type": "FEIP",
	"sn": 8,
	"ver": 5,
	"name": "Statement",
	"pid": "",
	"data": {
		"title": "About Donation",
		"content": "I accept donations from any one.",
		"confirm": "This is a formal and irrevocable statement."
	}
}
```