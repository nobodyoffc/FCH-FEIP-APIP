package api;

import java.util.ArrayList;

public class Constant {
    public static  final String UserDir = "user.dir";
    public static final int MaxRequestSize = 100;
    public static final int DefaultSize = 20;

    public static void main(String[] args) {
        Indices indices = Indices.cid;
        System.out.println(indices.name()+indices.ordinal());

        for(Indices in : Indices.values()){
            System.out.println(in.sn()+". "+in.name());
        }
    }
    public enum Indices{
        block("block",1), block_has("block_has",2), tx("tx",3), tx_has("tx_has",4), cash("cash",5), opreturn("opreturn",6), address("address",7), p2sh("p2sh",8), block_mark("block_mark",9),

        cid("cid",10), cid_history("cid_history",11), reputation_history("reputation_history",12),

        protocol("protocol",13),code("code",14),service("service",15),app("app",16), protocol_history("protocol_history",17),code_history("code_history",18), service_history("service_history",19),app_history("app_history",20),

        contact("contact",21),mail("mail",22),secret("secret",23),box("box",24),box_history("box_history",25),

        group("group",26),team("team",27),group_history("group_history",28),team_history("team_history",29),

        statement("statement",30),proof("proof",31),proof_history("proof_history",32),

        parse_mark("parse_mark",33),
        order("order",34);
        private int sn;

        Indices(String name, int sn) {
            this.sn=sn;
        }
        public final int sn(){
            return this.sn;
        }
    }

    //APIP path
    public static final String APIP1V1Path = "/apip1/v1/";
    public static final String APIP2V1Path = "/apip2/v1/";
    public static final String APIP3V1Path = "/apip3/v1/";
    public static final String APIP4V1Path = "/apip4/v1/";
    public static final String APIP5V1Path = "/apip5/v1/";
    public static final String APIP6V1Path = "/apip6/v1/";
    public static final String APIP7V1Path = "/apip7/v1/";
    public static final String APIP8V1Path = "/apip8/v1/";
    public static final String APIP9V1Path = "/apip9/v1/";
    public static final String APIP10V1Path = "/apip10/v1/";
    public static final String APIP11V1Path = "/apip11/v1/";
    public static final String APIP12V1Path = "/apip12/v1/";
    public static final String APIP13V1Path = "/apip13/v1/";
    public static final String APIP14V1Path = "/apip14/v1/";
    public static final String APIP15V1Path = "/apip15/v1/";
    public static final String APIP16V1Path = "/apip16/v1/";
    public static final String APIP17V1Path = "/apip17/v1/";
    public static final String APIP18V1Path = "/apip18/v1/";
    public static final String APIP19V1Path = "/apip19/v1/";
    public static final String APIP20V1Path = "/apip20/v1/";

    public static final String FreeGet = "/freeGet/v1/";

    //API names

    public static final String SignInAPI = "signIn";
    public static final String GeneralAPI = "general";

    public static final String BlockByIdsAPI = "blockByIds";
    public static final String BlockSearchAPI = "blockSearch";
    public static final String CashByIdsAPI = "cashByIds";
    public static final String GetCashesAPI = "getCashes";
    public static final String CashSearchAPI = "cashSearch";
    public static final String TxByIdsAPI = "txByIds";
    public static final String TxSearchAPI = "txSearch";
    public static final String OpReturnByIdsAPI = "opReturnByIds";
    public static final String OpReturnSearchAPI = "opReturnSearch";
    public static final String UnconfirmedAPI = "unconfirmed";
    public static final String BlockHasByIdsAPI = "blockHasByIds";
    public static final String TxHasByIdsAPI = "TxHasByIds";
    public static final String CashValidAPI = "cashValid";

    public static final String AddressByIdsAPI = "addressByIds";
    public static final String CidInfoByIdsAPI = "cidInfoByIds";
    public static final String GetFidCidAPI = "getFidCid";
    public static final String AddressSearchAPI = "addressSearch";
    public static final String FidCidSeekAPI = "fidCidSeek";
    public static final String CidInfoSearchAPI = "cidInfoSearch";
    public static final String UsedCidAPI = "usedCid";
    public static final String CidHistoryAPI = "cidHistory";
    public static final String HomepageHistoryAPI = "homepageHistory";
    public static final String NoticeFeeHistoryAPI = "noticeFeeHistory";
    public static final String ReputationHistoryAPI = "reputationHistory";
    public static final String P2shByIdsAPI = "p2shByIds";
    public static final String P2shSearchAPI = "p2shSearch";

    public static final String ProtocolByIdsAPI = "protocolByIds";
    public static final String ProtocolSearchAPI = "protocolSearch";
    public static final String ProtocolOpHistoryAPI = "protocolOpHistory";
    public static final String ProtocolRateHistoryAPI = "protocolRateHistory";

    public static final String CodeByIdsAPI = "codeByIds";
    public static final String CodeSearchAPI = "codeSearch";
    public static final String CodeOpHistoryAPI = "codeOpHistory";
    public static final String CodeRateHistoryAPI = "codeRateHistory";

    public static final String ServiceByIdsAPI = "serviceByIds";
    public static final String GetServicesAPI = "getServices";
    public static final String ServiceSearchAPI = "serviceSearch";
    public static final String ServiceOpHistoryAPI = "serviceOpHistory";
    public static final String ServiceRateHistoryAPI = "serviceRateHistory";

    public static final String AppByIdsAPI = "appByIds";
    public static final String GetAppsAPI = "getApps";
    public static final String AppSearchAPI = "appSearch";
    public static final String AppOpHistoryAPI = "appOpHistory";
    public static final String AppRateHistoryAPI = "appRateHistory";

    public static final String GroupByIdsAPI = "groupByIds";
    public static final String GroupSearchAPI = "groupSearch";
    public static final String GroupMembersAPI = "groupMembers";
    public static final String GroupExMembersAPI = "groupExMembers";
    public static final String MyGroupsAPI = "myGroups";
    public static final String GroupOpHistoryAPI = "groupOpHistory";


    public static final String TeamByIdsAPI = "teamByIds";
    public static final String TeamSearchAPI = "teamSearch";
    public static final String TeamMembersAPI = "teamMembers";
    public static final String TeamExMembersAPI = "teamExMembers";
    public static final String TeamOtherPersonsAPI = "teamOtherPersons";
    public static final String MyTeamsAPI = "myTeams";
    public static final String TeamOpHistoryAPI = "teamOpHistory";
    public static final String TeamRateHistoryAPI = "teamRateHistory";

    public static final String ContactsAPI = "contacts";
    public static final String ContactsDeletedAPI = "contactsDeleted";

    public static final String SecretsAPI = "secrets";
    public static final String SecretsDeletedAPI = "secretsDeleted";

    public static final String MailsAPI = "mails";
    public static final String MailsDeletedAPI = "mailsDeleted";
    public static final String MailThreadAPI = "mailThread";

    public static final String StatementsAPI = "statements";
    public static final String StatementSearchAPI = "statementSearch";

    public static final String ProofByIdsAPI = "proofByIds";
    public static final String ProofSearchAPI = "proofSearch";
    public static final String ProofHistoryAPI = "proofHistory";

    public static final String BoxByIdsAPI = "boxByIds";
    public static final String BoxSearchAPI = "boxSearch";
    public static final String BoxHistoryAPI = "boxHistory";
    public static final String AvatarsAPI = "avatars";
    public static final String GetAvatarAPI = "getAvatar";
    public static final String DecodeRawTxAPI = "decodeRawTx";
    public static final String BroadcastTxAPI = "broadcastTx";
    public static final String CashValidLiveAPI = "cashValidLive";
    public static final String CashValidForCdAPI = "cashValidForCd";
    public static final String CashValidForPayAPI = "cashValidForPay";

    public static final String GetAllSumsAPI = "getAllSums";


    //Header name
    public static final String CodeInHeader = "Code";
    public static final String SignInHeader = "Sign";
    public static final String SessionNameInHeader = "SessionName";

    //Code and messages
    public static final int Code0Success = 0;
    public static final String Msg0Success = "Success.";

    public static final int Code1000MissSign = 1000;
    public static final String Msg1000MissSign = "Miss sign in request header.";
    public static final int Code1001MissPubKey = 1001;
    public static final String Msg1001MissPubKey = "Miss pubKey in request header.";
    public static final int Code1002MissSessionName = 1002;
    public static final String Msg1002MissSessionName = "Miss sessionName in request header.";
    public static final int Code1003MissBody = 1003;
    public static final String Msg1003MissBody = "Miss request body.";

    public static final int Code1004InsufficientBalance = 1004;
    public static final String Msg1004InsufficientBalance = "Insufficient balance, please purchase service.";
    public static final int Code1005UrlUnequal = 1005;
    public static final String Msg1005UrlUnequal = "The request URL isn't the same as the one you signed.";
    public static final int Code1006RequestTimeExpired = 1006;
    public static final String Msg1006RequestTimeExpired = "Request expired.";
    public static final int Code1007UsedNonce = 1007;
    public static final String Msg1007UsedNonce = "Nonce had been used.";
    public static final int Code1008BadSign = 1008;
    public static final String Msg1008BadSign = "Failed to verify signature.";

    public static final int Code1009SessionTimeExpired = 1009;
    public static final String Msg1009SessionTimeExpired = "NO such sessionName or it was expired, please sign in again.";
    public static final int Code1010TooMuchData = 1010;
    public static final String Msg1010TooMuchData = "Too much data to be requested.";
    public static final int Code1011DataNotFound = 1011;
    public static final String Msg1011DataNotFound = "No data meeting the conditions.";
    public static final int Code1012BadQuery = 1012;
    public static final String Msg1012BadQuery = "Bad query. Check your request body referring related APIP document.";

    public static final int Code1013BadRequest = 1013;
    public static final String Msg1013BadRequest = "Bad request. Please check request body.";

    public static final int Code1014ApiSuspended = 1013;//The API is suspended
    public static final String Msg1014ApiSuspended  = "The API is suspended";

    public static final int Code1020OtherError = 1020;
    public static final String Msg1020OtherError = "Other error，please contact the service provider.";

    public static ArrayList<String> apiList = new ArrayList<String>();

    static {
        apiList.add(SignInAPI);
        apiList.add(GeneralAPI);

        apiList.add(BlockByIdsAPI);
        apiList.add(BlockSearchAPI);
        apiList.add(CashByIdsAPI);
        apiList.add(CashSearchAPI);
        apiList.add(TxHasByIdsAPI);
        apiList.add(CashValidAPI);
        apiList.add(TxByIdsAPI);
        apiList.add(TxSearchAPI);
        apiList.add(BlockHasByIdsAPI);
        apiList.add(OpReturnByIdsAPI);
        apiList.add(OpReturnSearchAPI);
        apiList.add(AddressByIdsAPI);
        apiList.add(AddressSearchAPI);


        apiList.add(CidInfoByIdsAPI);
        apiList.add(FidCidSeekAPI);
        apiList.add(CidInfoSearchAPI);
        apiList.add(UsedCidAPI);
        apiList.add(CidHistoryAPI);
        apiList.add(HomepageHistoryAPI);
        apiList.add(NoticeFeeHistoryAPI);
        apiList.add(ReputationHistoryAPI);


        apiList.add(P2shByIdsAPI);
        apiList.add(P2shSearchAPI);

        apiList.add(ProtocolByIdsAPI);
        apiList.add(ProtocolSearchAPI);
        apiList.add(ProtocolOpHistoryAPI);
        apiList.add(ProtocolRateHistoryAPI);

        apiList.add(CodeByIdsAPI);
        apiList.add(CodeSearchAPI);
        apiList.add(CodeOpHistoryAPI);
        apiList.add(CodeRateHistoryAPI);

        apiList.add(ServiceByIdsAPI);
        apiList.add(ServiceSearchAPI);
        apiList.add(ServiceOpHistoryAPI);
        apiList.add(ServiceRateHistoryAPI);

        apiList.add(AppByIdsAPI);
        apiList.add(AppSearchAPI);
        apiList.add(AppOpHistoryAPI);
        apiList.add(AppRateHistoryAPI);

        apiList.add(GroupByIdsAPI);
        apiList.add(GroupSearchAPI);
        apiList.add(GroupOpHistoryAPI);
        apiList.add(GroupMembersAPI);
        apiList.add(GroupExMembersAPI);
        apiList.add(MyGroupsAPI);

        apiList.add(TeamByIdsAPI);
        apiList.add(TeamSearchAPI);
        apiList.add(TeamOpHistoryAPI);
        apiList.add(TeamMembersAPI);
        apiList.add(TeamExMembersAPI);
        apiList.add(TeamOtherPersonsAPI);
        apiList.add(MyTeamsAPI);
        apiList.add(TeamRateHistoryAPI);

        apiList.add(ContactsAPI);
        apiList.add(ContactsDeletedAPI);

        apiList.add(SecretsAPI);
        apiList.add(SecretsDeletedAPI);

        apiList.add(MailsAPI);
        apiList.add(MailsDeletedAPI);
        apiList.add(MailThreadAPI);

        apiList.add(StatementsAPI);
        apiList.add(StatementSearchAPI);

        apiList.add(ProofByIdsAPI);
        apiList.add(ProofSearchAPI);
        apiList.add(ProofHistoryAPI);

        apiList.add(BoxByIdsAPI);
        apiList.add(BoxSearchAPI);
        apiList.add(BoxHistoryAPI);

        apiList.add(UnconfirmedAPI);
        apiList.add(BroadcastTxAPI);
        apiList.add(DecodeRawTxAPI);
        apiList.add(CashValidLiveAPI);
        apiList.add(AvatarsAPI);

        apiList.add(CashValidForCdAPI);
        apiList.add(CashValidForPayAPI);
    }

}
