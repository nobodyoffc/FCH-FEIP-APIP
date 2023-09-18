```
ProtocolType: FEIP
SerialNumber: 18
ProtocolName: Team
Version: 1
Description : create and manage teams on freecash blockchain.
Authors: C_armX
Language: en-US
CreateDate: 2021-12-06
UpdateDate: 2023-05-07
```


# FEIP18V2_Team(zh-CN)

## Contents

[Consensuses of FEIP](#consensuses-of-feip)

[Consensuses of this protocol](#consensuses-of-this-protocol)

[Create a team](#create-a-team)

[Invite members](#invite-members)

[Join a team](#join-a-team)

[Withdraw invitation](#withdraw-invitation)

[Appoint manager](#appoint-manager)

[Leave a team](#leave-a-team)

[Transfer a team](#transfer-a-team)

[Take over a team](#take-over-a-team)

[Update team information](#update-team-information)

[Agree new consensus](#agree-new-consensus)

[Withdraw invitation](#withdraw-invitation)

[Cancel appointment](#cancel-appointment)

[Dismiss members](#dismiss-members)

[Rate a team](#rate-a-team)

[Disband a team](#disband-a-team)

[QR code](#qr-code)

## Consensuses of FEIP

1. FEIP type protocols write data into freecash blockchain with OP_RETURN for public witness.

2. The SIGHASH flag of all transaction inputs must be ‘ALL’ (0x41).

3. The max size of OP_RETURN : 4096 bytes.

4. The format of the data in op_return: JSON.

5. Encoding : utf-8.

6. Since block height `2000000`, any operation writing FEIP data with OP_RETURN needs more than `1cd` consumed.

7. More consensuses of FEIP can be found in `FEIP1_Protocol`.

## Consensuses of this protocol

1. This protocol is used to create and manage teams on freecash blockchain.
2. Anyone can [create](#create-a-team) a team.
3. The signer of tx creating the team is the `owner` of the team.
4. `TID`(Team identity) is the txid of the transaction in which the team is created. 
5. A team can be called as "[stdName]"+"@"+"[cid/address of the owner]", such as "Freers@Free_cash".
6. The owner should ensure that its teams have different stdNames.
7. Only owner can [disband](#disband-a-team) the team. Once a team was disbanded, it can't be recover forever.
8. Team consensus is required when creating a team. `consensusId` is the double sha256 value of the consensus.
9. Team consensus is accepted by all members when they [`join`](#join-a-team) the team.
10. Only owner can [update](#update-team-information) the "stdName", "localNames", "desc", and "consensusId" of the team. 
11. `consensusId` can be updated but can't be deleted.
12. If the "consensusId" was updated, it's necessary for all members to [agree](#agree-new-consensus) the new consensus.
13. The owner can [transfer](#transfer-a-team) the team to a `transferee`.
14. Transferee can [take over](#take-over-a-team) the team which also means that the transferee accepts the current team consensus.
15. When a team is transfered, all former managers is no longer in charge. The new owner is the only manager.
16. Before `take over` operation, a new `transfer` operation cancels the earlier one. So, owner can transfer team to itself to cancel unaccepted transfering.
17. The master (see "FEIP6_Master") of owner can transfer the team in case of the owner losing its private key.
18. Owner can [appoint](#appoint-manager) some active members as managers and also [Cancel appointment](#cancel-appointment) them.
19. `Owner` is inborn active member and manager of the team, and can not be dismissed or cancel appointment unless that it succesfully transfered the team to others.
20. Manager can [invite](#invite-members) others to join the team.
21. Only invitees can [join](#join-a-team) the team which also means that they accepted the current team consensus.
22. Before some invitees joining the team, manager can [withdraw](#withdraw-invitation) the invitations to them.
23. Active members can [leave](#leave-a-team) the team which will move them from active member list to existed member list.
24. Manager can dismiss active member including itself but owner.
25. Left or dismissed members are no longer managers.
26. Anyone but the owner can [rate](#rate-a-team) team with score of 0, 1, 2, 3, 4, or 5 and more than `1cd` consumed.
27. The total score(`tRate`) of a team is the cdd weighted average of all ratings to the team.
28. The disbanded team still can be rated.
29. Since block height `2000000`, `create` team needs more than `100cd` consumed.
30. Operations of this protocol write OP_RETURN with the same content structure:

|field|type|content|required|
|:----|:----|:----|:----|
|type|string|Fixed: "FEIP"|Y|
|sn|int|Serial number. Fixed: 18|Y|
|ver|int|Version. Fixed: 1|Y|
|name|string|Fixed: "Team"|N|
|pid|string|PID of this protocol. |N|
|did|string|DID of this protocol file. |N|
|data|object|Operation data which is different in different operations. |Y|

## Create a team
Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "create"|Y|
|stdlName|String|Name in English.|Y|
|localName|String array|Names in different languages|N|
|consensusId|String|sha256 value of the team consensus|N|
|desc|String|Description of this team|N|

* Example of creating a team

consensus：test

```
{
	"type": "FEIP",
	"sn": 18,
	"ver": 1,
	"name": "Team",
	"data": {
		"op": "create",
		"stdName": "test",
		"localNames": ["测试", "テスト"],
		"consensusId": "954d5a49fd70d9b8bcdb35d252267829957f7ef7fa6c74f88419bdc5e82209f4",
		"desc": "This is a test Team"
	}
}
```

## Invite members
Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "invite"|Y|
|tid|String|Team ID|Y|
|list|String array|Addresses of the applicants.|Y|

* Example of adding members

```
{
	"type": "FEIP",
	"sn": 18,
	"ver": 1,
	"name": "Team",
	"data": {
		"op": "invite",
		"tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d",
		"list": [
			"FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
			"F9pcRpps3T2iHuNGzU3k5b2kWKMRukZP1U",
			"F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW"
		]
	}
}
```

## Join a team
Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "join"|Y|
|tid|String|Team ID|Y|
|consensusId|String|the sha256 value of the new team consensus|Y|
|confirm|String|Fixed:"I join the team and agree with the team consensus."|Y|


* Example of joining a team
```
{
	"type": "FEIP",
	"sn": 18,
	"ver": 1,
	"name": "Team",
	"data": {
		"op": "join",
		"tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d",
		"confirm": "I join the team and agree with the team consensus.",
		"consensusId": "954d5a49fd70d9b8bcdb35d252267829957f7ef7fa6c74f88419bdc5e82209f4"
	}
}
```

## Appoint manager
Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "appoint"|Y|
|tid|String|Team ID|Y|
|list|String array|the list of addresses being appoint|Y|

* Example of appointing managers
```
{
	"type": "FEIP",
	"sn": 18,
	"ver": 1,
	"name": "Team",
	"data": {
		"op": "appoint",
		"tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d",
		"list": [
			"FEk41Kqjar45fLDriztUDTUkdki7mmcjWK"
		]
	}
}
```

## Transfer a team
Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "transfer"|Y|
|tid|String|Team ID|Y|
|transferee|String|Address of transferee|Y|
|confirm|String|Fixed:"I transfer the team to the transferee."|Y|

* Example of transferring a team

```
{
	"type": "FEIP",
	"sn": 18,
	"ver": 1,
	"name": "Team",
	"data": {
		"op": "transfer",
		"tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d",
		"transferee": "FEk41Kqjar45fLDriztUDTUkdki7mmcjWK",
		"confirm": "I transfer the team to the transferee."
	}
}
```

## Take over a team
Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "take over"|Y|
|tid|String|Team ID|Y|
|confirm|String|Fixed:"I take over the team and agree with the team consensus."|Y|

* Example of taking over a team

```
{
	"type": "FEIP",
	"sn": 18,
	"ver": 1,
	"name": "Team",
	"data": {
		"op": "take over",
		"tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d",
		"confirm": "I take over the team and agree with the team consensus.",
		"consensusId": "954d5a49fd70d9b8bcdb35d252267829957f7ef7fa6c74f88419bdc5e82209f4"
	}
}
```

## Update team information

Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "update"|Y|
|tid|String|Team ID|Y|
|stdlName|String|Name in English.|Y|
|localName|String array|Names in different languages|N|
|consensusId|String|sha256 value of the team consensus|N|
|desc|String|Description of this team|N|

* Example of updating a team

consensus: test update.

```
{
	"type": "FEIP",
	"sn": 18,
	"ver": 1,
	"name": "Team",
	"data": {
		"op": "update",
		"tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d",
		"stdName": "test update",
		"localNames": ["测试", "テスト"],
		"consensusId": "371f7f3ec56330109962f9fb1220fa836ebe89f07ed38515391376ea8e90a1b4",
		"desc": "New description for the test Team"
	}
}
```

## Agree new consensus

Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "agree consensus"|Y|
|tid|String|Team ID|Y|
|consensusId|String|the sha256 value of the new team consensus|Y|
|confirm|String|Fixed:""I agree with the new consensus."|Y|



* Example of agreeing new consensus

```
{
	"type": "FEIP",
	"sn": 18,
	"ver": 1,
	"name": "Team",
	"data": {
		"op": "agree consensus",
		"tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d",
		"consensusId": "371f7f3ec56330109962f9fb1220fa836ebe89f07ed38515391376ea8e90a1b4",
		"confirm": "I agree with the new consensus."
	}
}
```

## Withdraw invitation
Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "withdraw invitation"|Y|
|tid|String|Team ID|Y|
|list|String array|Addresses.|Y|

* Example of withdrawing invitation

```
{
	"type": "FEIP",
	"sn": 18,
	"ver": 1,
	"name": "Team",
	"data": {
		"op": "withdraw invitation",
		"tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d",
		"list": [
			"F9pcRpps3T2iHuNGzU3k5b2kWKMRukZP1U"
		]
	}
}
```

## Cancel appointment
Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "cancel appointment"|Y|
|tid|String|Team ID|Y|
|list|String array|the list of addresses being cancel appointment|Y|

* Example of canceling appointment

```
{
	"type": "FEIP",
	"sn": 18,
	"ver": 1,
	"name": "Team",
	"data": {
		"op": "cancel appointment",
		"tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d",
		"list": [
			"F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW"
		]
	}
}
```

## Leave a team
Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "leave"|Y|
|tid|String|Team ID|Y|

* Example of leaving a team

```
{
	"type": "FEIP",
	"sn": 18,
	"ver": 1,
	"name": "Team",
	"data": {
		"op": "leave",
		"tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d"
	}
}
```

## Dismiss members
Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "dismiss"|Y|
|tid|String|Team ID|Y|
|list|String array|3096|Addresses to be dismissed|Y|

* Example of dismiss members

```
{
	"type": "FEIP",
	"sn": 18,
	"ver": 1,
	"name": "Team",
	"data": {
		"op": "dismiss",
		"tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d",
		"list": [
			"F86zoAvNaQxEuYyvQssV5WxEzapNaiDtTW"
		]
	}
}
```

## Rate a team
Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|tid|hex|Txid when publishing the team|Y|
|op|String|operation: "rate"|Y|
|rate|int|Rating of the service from 0 to 5|Y|

### Example of rating a team

```
{
	"type": "FEIP",
	"sn": 18,
	"ver": 1,
	"name": "Team",
	"data": {
		"tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d",
		"op": "rate",
		"rate": 4
	}
}
```

## Disband a team
Send a tx with the content of data object as following:

|field|type|content|required|
|:----|:----|:----|:----|
|op|String|operation: "disband"|Y|
|tid|String|Team ID|Y|

* Example of disbanding a team

```
{
	"type": "FEIP",
	"sn": 18,
	"ver": 1,
	"name": "Team",
	"data": {
		"op": "disband",
		"tid": "e2703f9d8c0b36ec37af5a02f5f9733083d7471c9e26a952cf86f34646bce6f2"
	}
}
```

## QR code

The QR code of a published team has fields as following:

```
{
	"meta": "FC",
	"type": "FEIP",
	"sn": 18,
	"ver": 1,
	"data": {
		"stdName": "test",
		"tid": "317fa382a6d9a80bb422c2e367386c1533803839f013a9ead737dfa3c07d9a0d"
	}
}
```
